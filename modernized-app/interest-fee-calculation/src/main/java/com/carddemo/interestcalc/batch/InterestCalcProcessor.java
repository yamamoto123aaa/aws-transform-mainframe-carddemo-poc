package com.carddemo.interestcalc.batch;

import com.carddemo.interestcalc.domain.Account;
import com.carddemo.interestcalc.domain.CardXref;
import com.carddemo.interestcalc.domain.DiscountGroup;
import com.carddemo.interestcalc.domain.DiscountGroupId;
import com.carddemo.interestcalc.domain.Transaction;
import com.carddemo.interestcalc.domain.TransactionCategoryBalance;
import com.carddemo.interestcalc.repository.AccountRepository;
import com.carddemo.interestcalc.repository.CardXrefRepository;
import com.carddemo.interestcalc.repository.DiscountGroupRepository;
import com.carddemo.interestcalc.repository.TransactionCategoryBalanceRepository;
import com.carddemo.interestcalc.repository.TransactionRepository;
import com.carddemo.interestcalc.util.Db2TimestampFormatter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Reimplementation of the PROCEDURE DIVISION of CBACT04C.cbl (interest and
 * fee calculation). Each private method is annotated with the COBOL
 * paragraph and requirement IDs (from
 * requirements-spec/ja/InterestandFeeCalculation/requirements.md) it
 * replaces, for traceability back to source.
 *
 * <p><b>Known defect reproduced intentionally:</b> in the original program,
 * the loop structure ({@code PERFORM UNTIL END-OF-FILE = 'Y'} wrapping an
 * {@code IF END-OF-FILE = 'N' ... ELSE PERFORM 1050-UPDATE-ACCOUNT}) makes
 * the {@code ELSE} branch unreachable: by the time {@code END-OF-FILE}
 * becomes {@code 'Y'}, the surrounding {@code PERFORM UNTIL} has already
 * exited the loop, so 1050-UPDATE-ACCOUNT is never invoked for the very
 * last account in the file. That account's accrued interest is still
 * posted as individual rows to TRANSACT-FILE, but its ACCOUNT-FILE balance
 * is never updated. This was confirmed against the source and the AI
 * generated requirements.md (whose REQ-F-024 describes the *intended*
 * behavior, not the actual one). Per an explicit decision made with the
 * business/engineering owner during this migration, this defect is
 * reproduced as-is rather than silently fixed, to avoid changing posted
 * balances unexpectedly during a migration. See module README for details.
 */
@Service
public class InterestCalcProcessor {

    private static final BigDecimal ZERO_2DP = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
    private static final String DEFAULT_GROUP_ID = "DEFAULT";

    private final TransactionCategoryBalanceRepository tranCatBalRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final DiscountGroupRepository discountGroupRepository;
    private final TransactionRepository transactionRepository;

    public InterestCalcProcessor(TransactionCategoryBalanceRepository tranCatBalRepository,
                                  AccountRepository accountRepository,
                                  CardXrefRepository cardXrefRepository,
                                  DiscountGroupRepository discountGroupRepository,
                                  TransactionRepository transactionRepository) {
        this.tranCatBalRepository = tranCatBalRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.discountGroupRepository = discountGroupRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Top-level PROCEDURE DIVISION main loop (lines 188-222 of
     * CBACT04C.cbl). {@code businessDate} corresponds to PARM-DATE
     * (EXTERNAL-PARMS), the 10-character parameter INTCALC.jcl passes to
     * the program (e.g. "2022071800").
     */
    public InterestCalcResult run(String businessDate) {
        List<TransactionCategoryBalance> tranCatBalRecords =
                tranCatBalRepository.findAllByOrderByIdAcctIdAscIdTranTypeCdAscIdTranCatCdAsc();

        String lastAcctNum = null;
        boolean firstTime = true;
        BigDecimal totalInterest = ZERO_2DP;
        Account currentAccount = null;
        CardXref currentXref = null;
        int tranIdSuffix = 0;
        int transactionsWritten = 0;

        for (TransactionCategoryBalance tranCatBal : tranCatBalRecords) {
            String acctId = tranCatBal.getId().getAcctId();

            if (!acctId.equals(lastAcctNum)) {
                if (!firstTime) {
                    // 1050-UPDATE-ACCOUNT, invoked on account switch for the
                    // PREVIOUS account (REQ-F-003/004, REQ-F-015/016).
                    updateAccount(currentAccount, totalInterest);
                } else {
                    firstTime = false;
                }
                totalInterest = ZERO_2DP;
                lastAcctNum = acctId;
                currentAccount = getAccountData(acctId);   // 1100-GET-ACCT-DATA (REQ-F-005/017)
                currentXref = getXrefData(acctId);          // 1110-GET-XREF-DATA (REQ-F-006/018)
            }

            BigDecimal rate = lookupInterestRate(
                    currentAccount.getGroupId(),
                    tranCatBal.getId().getTranTypeCd(),
                    tranCatBal.getId().getTranCatCd()); // 1200-GET-INTEREST-RATE (REQ-F-007/019/020)

            if (rate.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal monthlyInterest = computeMonthlyInterest(tranCatBal.getTranCatBal(), rate); // 1300-COMPUTE-INTEREST (REQ-F-008/009/021)
                totalInterest = totalInterest.add(monthlyInterest);
                tranIdSuffix++;
                writeInterestTransaction(businessDate, tranIdSuffix, acctId, monthlyInterest, currentXref); // 1300-B-WRITE-TX (REQ-F-009/010/022/023)
                transactionsWritten++;
                computeFees(); // 1400-COMPUTE-FEES
            }
        }

        // Intentionally NOT calling updateAccount() again here for the last
        // account processed -- see class Javadoc "Known defect reproduced
        // intentionally".

        return new InterestCalcResult(tranCatBalRecords.size(), transactionsWritten);
    }

    /** 1100-GET-ACCT-DATA. ABENDs (REQ-N/A, mirrors 9999-ABEND-PROGRAM) if the account is missing. */
    private Account getAccountData(String acctId) {
        return accountRepository.findById(acctId)
                .orElseThrow(() -> new InterestCalcAbendException(
                        "ERROR READING ACCOUNT FILE: ACCOUNT NOT FOUND " + acctId));
    }

    /** 1110-GET-XREF-DATA, looked up by the XREF-ACCT-ID alternate key. ABENDs if missing. */
    private CardXref getXrefData(String acctId) {
        return cardXrefRepository.findFirstByXrefAcctId(acctId)
                .orElseThrow(() -> new InterestCalcAbendException(
                        "ERROR READING XREF FILE: ACCOUNT NOT FOUND " + acctId));
    }

    /**
     * 1200-GET-INTEREST-RATE + 1200-A-GET-DEFAULT-INT-RATE. Looks up the
     * discount group by (groupId, tranTypeCd, tranCatCd); if not found
     * (COBOL file status '23'), retries with groupId="DEFAULT". ABENDs if
     * even the default group is missing.
     */
    private BigDecimal lookupInterestRate(String groupId, String tranTypeCd, String tranCatCd) {
        Optional<DiscountGroup> group =
                discountGroupRepository.findById(new DiscountGroupId(groupId, tranTypeCd, tranCatCd));
        if (group.isPresent()) {
            return group.get().getIntRate();
        }
        DiscountGroup defaultGroup = discountGroupRepository
                .findById(new DiscountGroupId(DEFAULT_GROUP_ID, tranTypeCd, tranCatCd))
                .orElseThrow(() -> new InterestCalcAbendException(
                        "ERROR READING DEFAULT DISCLOSURE GROUP: " + tranTypeCd + "/" + tranCatCd));
        return defaultGroup.getIntRate();
    }

    /**
     * 1300-COMPUTE-INTEREST: {@code (TRAN-CAT-BAL * DIS-INT-RATE) / 1200}.
     * COBOL {@code COMPUTE} without {@code ROUNDED} truncates to the
     * receiving field's decimal places (WS-MONTHLY-INT is
     * {@code PIC S9(09)V99}, 2 decimals) -- so this uses
     * {@link RoundingMode#DOWN}, not HALF_UP.
     */
    private BigDecimal computeMonthlyInterest(BigDecimal tranCatBal, BigDecimal intRate) {
        return tranCatBal.multiply(intRate).divide(BigDecimal.valueOf(1200), 2, RoundingMode.DOWN);
    }

    /** 1050-UPDATE-ACCOUNT (REQ-F-012/025). */
    private void updateAccount(Account account, BigDecimal totalInterest) {
        account.setCurrBal(account.getCurrBal().add(totalInterest));
        account.setCurrCycCredit(ZERO_2DP);
        account.setCurrCycDebit(ZERO_2DP);
        accountRepository.save(account);
    }

    /** 1300-B-WRITE-TX (REQ-F-009/010/022/023). */
    private void writeInterestTransaction(String businessDate, int tranIdSuffix, String acctId,
                                           BigDecimal monthlyInterest, CardXref xref) {
        Transaction tx = new Transaction();
        tx.setTranId(businessDate + String.format("%06d", tranIdSuffix)); // PARM-DATE(10) + WS-TRANID-SUFFIX(6) = 16
        tx.setTranTypeCd("01");
        tx.setTranCatCd("05");
        tx.setTranSource("System");
        tx.setTranDesc("Int. for a/c " + acctId);
        tx.setTranAmt(monthlyInterest);
        tx.setTranMerchantId("000000000");
        tx.setTranMerchantName("");
        tx.setTranMerchantCity("");
        tx.setTranMerchantZip("");
        tx.setTranCardNum(xref.getXrefCardNum());
        String timestamp = Db2TimestampFormatter.formatNow();
        tx.setTranOrigTs(timestamp);
        tx.setTranProcTs(timestamp);
        transactionRepository.save(tx);
    }

    /**
     * 1400-COMPUTE-FEES. In CBACT04C.cbl this paragraph's body is just the
     * comment "To be implemented" -- fee calculation was never built in
     * the source. Left as a no-op here; do not add fee logic without a
     * corresponding requirement and source change to trace it back to.
     */
    private void computeFees() {
        // Intentionally empty -- matches source.
    }
}
