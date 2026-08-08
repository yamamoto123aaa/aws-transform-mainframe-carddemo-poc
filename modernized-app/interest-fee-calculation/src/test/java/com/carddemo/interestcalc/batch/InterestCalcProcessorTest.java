package com.carddemo.interestcalc.batch;

import com.carddemo.interestcalc.domain.Account;
import com.carddemo.interestcalc.domain.CardXref;
import com.carddemo.interestcalc.domain.DiscountGroup;
import com.carddemo.interestcalc.domain.DiscountGroupId;
import com.carddemo.interestcalc.domain.Transaction;
import com.carddemo.interestcalc.domain.TransactionCategoryBalance;
import com.carddemo.interestcalc.domain.TransactionCategoryBalanceId;
import com.carddemo.interestcalc.repository.AccountRepository;
import com.carddemo.interestcalc.repository.CardXrefRepository;
import com.carddemo.interestcalc.repository.DiscountGroupRepository;
import com.carddemo.interestcalc.repository.TransactionCategoryBalanceRepository;
import com.carddemo.interestcalc.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for InterestCalcProcessor, isolated from Spring Batch/JPA via
 * Mockito. Business-date "2022071800" throughout matches the sample value
 * used by INTCALC.jcl in the original source.
 */
class InterestCalcProcessorTest {

    private static final String BUSINESS_DATE = "2022071800";

    private TransactionCategoryBalanceRepository tranCatBalRepository;
    private AccountRepository accountRepository;
    private CardXrefRepository cardXrefRepository;
    private DiscountGroupRepository discountGroupRepository;
    private TransactionRepository transactionRepository;
    private InterestCalcProcessor processor;

    @BeforeEach
    void setUp() {
        tranCatBalRepository = mock(TransactionCategoryBalanceRepository.class);
        accountRepository = mock(AccountRepository.class);
        cardXrefRepository = mock(CardXrefRepository.class);
        discountGroupRepository = mock(DiscountGroupRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        processor = new InterestCalcProcessor(tranCatBalRepository, accountRepository,
                cardXrefRepository, discountGroupRepository, transactionRepository);

        // save() must return its argument, as the real JPA repository would.
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static TransactionCategoryBalance tcatBal(String acctId, String typeCd, String catCd, String bal) {
        TransactionCategoryBalance rec = new TransactionCategoryBalance();
        rec.setId(new TransactionCategoryBalanceId(acctId, typeCd, catCd));
        rec.setTranCatBal(new BigDecimal(bal));
        return rec;
    }

    private static Account account(String acctId, String groupId, String currBal) {
        Account a = new Account();
        a.setAcctId(acctId);
        a.setGroupId(groupId);
        a.setCurrBal(new BigDecimal(currBal));
        a.setCurrCycCredit(new BigDecimal("50.00"));
        a.setCurrCycDebit(new BigDecimal("25.00"));
        return a;
    }

    private static CardXref xref(String acctId, String cardNum) {
        CardXref x = new CardXref();
        x.setXrefCardNum(cardNum);
        x.setXrefAcctId(acctId);
        x.setXrefCustId("100000001");
        return x;
    }

    private static DiscountGroup group(String groupId, String typeCd, String catCd, String rate) {
        DiscountGroup g = new DiscountGroup();
        g.setId(new DiscountGroupId(groupId, typeCd, catCd));
        g.setIntRate(new BigDecimal(rate));
        return g;
    }

    @Test
    void computesMonthlyInterestByTruncationNotRounding() {
        // (100.00 * 123.42) / 1200 = 10.285 exactly.
        // Truncation (COBOL COMPUTE without ROUNDED) -> 10.28.
        // HALF_UP rounding would have given 10.29 -- this test fails if that mistake is reintroduced.
        when(tranCatBalRepository.findAllByOrderByIdAcctIdAscIdTranTypeCdAscIdTranCatCdAsc())
                .thenReturn(List.of(tcatBal("11111111111", "01", "0001", "100.00")));
        when(accountRepository.findById("11111111111"))
                .thenReturn(Optional.of(account("11111111111", "GRP1", "0.00")));
        when(cardXrefRepository.findFirstByXrefAcctId("11111111111"))
                .thenReturn(Optional.of(xref("11111111111", "4000000000000001")));
        when(discountGroupRepository.findById(new DiscountGroupId("GRP1", "01", "0001")))
                .thenReturn(Optional.of(group("GRP1", "01", "0001", "123.42")));

        processor.run(BUSINESS_DATE);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getTranAmt()).isEqualByComparingTo("10.28");
    }

    @Test
    void fallsBackToDefaultGroupWhenSpecificGroupNotFound() {
        when(tranCatBalRepository.findAllByOrderByIdAcctIdAscIdTranTypeCdAscIdTranCatCdAsc())
                .thenReturn(List.of(tcatBal("22222222222", "01", "0002", "200.00")));
        when(accountRepository.findById("22222222222"))
                .thenReturn(Optional.of(account("22222222222", "GRP2", "0.00")));
        when(cardXrefRepository.findFirstByXrefAcctId("22222222222"))
                .thenReturn(Optional.of(xref("22222222222", "4000000000000002")));
        when(discountGroupRepository.findById(new DiscountGroupId("GRP2", "01", "0002")))
                .thenReturn(Optional.empty());
        when(discountGroupRepository.findById(new DiscountGroupId("DEFAULT", "01", "0002")))
                .thenReturn(Optional.of(group("DEFAULT", "01", "0002", "12.00")));

        InterestCalcResult result = processor.run(BUSINESS_DATE);

        assertThat(result.getTransactionsWritten()).isEqualTo(1);
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        // (200.00 * 12.00) / 1200 = 2.00
        assertThat(captor.getValue().getTranAmt()).isEqualByComparingTo("2.00");
    }

    @Test
    void abendsWhenDefaultGroupAlsoMissing() {
        when(tranCatBalRepository.findAllByOrderByIdAcctIdAscIdTranTypeCdAscIdTranCatCdAsc())
                .thenReturn(List.of(tcatBal("33333333333", "01", "0003", "50.00")));
        when(accountRepository.findById("33333333333"))
                .thenReturn(Optional.of(account("33333333333", "GRP3", "0.00")));
        when(cardXrefRepository.findFirstByXrefAcctId("33333333333"))
                .thenReturn(Optional.of(xref("33333333333", "4000000000000003")));
        when(discountGroupRepository.findById(any())).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(InterestCalcAbendException.class,
                () -> processor.run(BUSINESS_DATE));
    }

    @Test
    void skipsInterestWhenRateIsZero() {
        when(tranCatBalRepository.findAllByOrderByIdAcctIdAscIdTranTypeCdAscIdTranCatCdAsc())
                .thenReturn(List.of(tcatBal("44444444444", "01", "0004", "300.00")));
        when(accountRepository.findById("44444444444"))
                .thenReturn(Optional.of(account("44444444444", "GRP4", "0.00")));
        when(cardXrefRepository.findFirstByXrefAcctId("44444444444"))
                .thenReturn(Optional.of(xref("44444444444", "4000000000000004")));
        when(discountGroupRepository.findById(new DiscountGroupId("GRP4", "01", "0004")))
                .thenReturn(Optional.of(group("GRP4", "01", "0004", "0.00")));

        InterestCalcResult result = processor.run(BUSINESS_DATE);

        assertThat(result.getTransactionsWritten()).isZero();
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void accumulatesMultipleCategoriesForSameAccountBeforeSwitching() {
        when(tranCatBalRepository.findAllByOrderByIdAcctIdAscIdTranTypeCdAscIdTranCatCdAsc())
                .thenReturn(List.of(
                        tcatBal("55555555555", "01", "0001", "100.00"),
                        tcatBal("55555555555", "01", "0002", "200.00"),
                        tcatBal("66666666666", "01", "0001", "50.00"))); // triggers switch away from acct 5
        when(accountRepository.findById("55555555555"))
                .thenReturn(Optional.of(account("55555555555", "GRP5", "1000.00")));
        when(accountRepository.findById("66666666666"))
                .thenReturn(Optional.of(account("66666666666", "GRP6", "500.00")));
        when(cardXrefRepository.findFirstByXrefAcctId("55555555555"))
                .thenReturn(Optional.of(xref("55555555555", "4000000000000005")));
        when(cardXrefRepository.findFirstByXrefAcctId("66666666666"))
                .thenReturn(Optional.of(xref("66666666666", "4000000000000006")));
        when(discountGroupRepository.findById(new DiscountGroupId("GRP5", "01", "0001")))
                .thenReturn(Optional.of(group("GRP5", "01", "0001", "12.00"))); // 100*12/1200 = 1.00
        when(discountGroupRepository.findById(new DiscountGroupId("GRP5", "01", "0002")))
                .thenReturn(Optional.of(group("GRP5", "01", "0002", "12.00"))); // 200*12/1200 = 2.00
        when(discountGroupRepository.findById(new DiscountGroupId("GRP6", "01", "0001")))
                .thenReturn(Optional.of(group("GRP6", "01", "0001", "0.00"))); // rate 0 -> no txn for acct 6

        processor.run(BUSINESS_DATE);

        // Account 5's balance update is only triggered by the switch to account 6.
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getAcctId()).isEqualTo("55555555555");
        // 1000.00 + (1.00 + 2.00) = 1003.00
        assertThat(captor.getValue().getCurrBal()).isEqualByComparingTo("1003.00");
        assertThat(captor.getValue().getCurrCycCredit()).isEqualByComparingTo("0.00");
        assertThat(captor.getValue().getCurrCycDebit()).isEqualByComparingTo("0.00");
    }

    @Test
    void lastAccountInFileIsNeverBalanceUpdated_reproducesSourceDefect() {
        // Single account, single record: the account-switch condition that
        // triggers 1050-UPDATE-ACCOUNT never fires because there is no
        // subsequent record. This is the simplest reproduction of the
        // defect documented on InterestCalcProcessor and in the module
        // README: the last (here, only) account processed never has its
        // ACCOUNT-FILE balance rewritten, even though its interest was
        // both accrued and posted as a transaction.
        when(tranCatBalRepository.findAllByOrderByIdAcctIdAscIdTranTypeCdAscIdTranCatCdAsc())
                .thenReturn(List.of(tcatBal("77777777777", "01", "0001", "1000.00")));
        when(accountRepository.findById("77777777777"))
                .thenReturn(Optional.of(account("77777777777", "GRP7", "500.00")));
        when(cardXrefRepository.findFirstByXrefAcctId("77777777777"))
                .thenReturn(Optional.of(xref("77777777777", "4000000000000007")));
        when(discountGroupRepository.findById(new DiscountGroupId("GRP7", "01", "0001")))
                .thenReturn(Optional.of(group("GRP7", "01", "0001", "12.00")));

        InterestCalcResult result = processor.run(BUSINESS_DATE);

        assertThat(result.getTransactionsWritten()).isEqualTo(1); // interest WAS calculated and posted...
        verify(accountRepository, never()).save(any());           // ...but the account balance was NOT updated.
    }
}
