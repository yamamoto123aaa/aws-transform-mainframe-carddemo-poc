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
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test of interestCalcJob against an H2-backed Spring context,
 * covering the full control-break flow across two accounts. Data is seeded
 * so that ACCT2 is the last account in key order, specifically to exercise
 * (and pin down) the source defect documented on {@link
 * InterestCalcProcessor}: only the non-last account's balance gets
 * updated.
 */
@SpringBootTest
@SpringBatchTest
class InterestCalcJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CardXrefRepository cardXrefRepository;
    @Autowired
    private DiscountGroupRepository discountGroupRepository;
    @Autowired
    private TransactionCategoryBalanceRepository tranCatBalRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void runsFullJobAcrossMultipleAccountsAndReproducesLastAccountDefect() throws Exception {
        seedData();

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(3); // 2 categories for ACCT1 + 1 for ACCT2
        assertThat(transactions).allMatch(t -> t.getTranDesc().startsWith("Int. for a/c "));
        assertThat(transactions).allMatch(t -> t.getTranTypeCd().equals("01") && t.getTranCatCd().equals("05"));

        Account acct1 = accountRepository.findById("10000000001").orElseThrow();
        // seed 1000.00 + (1.00 from cat 0001 + 2.00 from cat 0002) = 1003.00
        assertThat(acct1.getCurrBal()).isEqualByComparingTo("1003.00");
        assertThat(acct1.getCurrCycCredit()).isEqualByComparingTo("0.00");
        assertThat(acct1.getCurrCycDebit()).isEqualByComparingTo("0.00");

        // ACCT2 is the LAST account in key order -> balance must be UNCHANGED,
        // even though its interest was computed and posted as a transaction.
        // This reproduces CBACT04C.cbl's own defect; see InterestCalcProcessor.
        Account acct2 = accountRepository.findById("10000000002").orElseThrow();
        assertThat(acct2.getCurrBal()).isEqualByComparingTo("500.00");
        assertThat(acct2.getCurrCycCredit()).isEqualByComparingTo("1.00"); // untouched, still the seed value
    }

    private void seedData() {
        Account acct1 = new Account();
        acct1.setAcctId("10000000001");
        acct1.setGroupId("GRP1");
        acct1.setCurrBal(new BigDecimal("1000.00"));
        acct1.setCurrCycCredit(new BigDecimal("10.00"));
        acct1.setCurrCycDebit(new BigDecimal("5.00"));
        accountRepository.save(acct1);

        Account acct2 = new Account();
        acct2.setAcctId("10000000002");
        acct2.setGroupId("GRP2");
        acct2.setCurrBal(new BigDecimal("500.00"));
        acct2.setCurrCycCredit(new BigDecimal("1.00"));
        acct2.setCurrCycDebit(new BigDecimal("1.00"));
        accountRepository.save(acct2);

        CardXref xref1 = new CardXref();
        xref1.setXrefCardNum("4000000000000001");
        xref1.setXrefAcctId("10000000001");
        xref1.setXrefCustId("900000001");
        cardXrefRepository.save(xref1);

        CardXref xref2 = new CardXref();
        xref2.setXrefCardNum("4000000000000002");
        xref2.setXrefAcctId("10000000002");
        xref2.setXrefCustId("900000002");
        cardXrefRepository.save(xref2);

        discountGroupRepository.save(rate("GRP1", "01", "0001", "12.00"));
        discountGroupRepository.save(rate("GRP1", "01", "0002", "12.00"));
        discountGroupRepository.save(rate("GRP2", "01", "0001", "6.00"));

        tranCatBalRepository.save(balance("10000000001", "01", "0001", "100.00"));  // 100*12/1200 = 1.00
        tranCatBalRepository.save(balance("10000000001", "01", "0002", "200.00"));  // 200*12/1200 = 2.00
        tranCatBalRepository.save(balance("10000000002", "01", "0001", "1000.00")); // 1000*6/1200 = 5.00 (never applied to balance)
    }

    private static DiscountGroup rate(String groupId, String typeCd, String catCd, String rate) {
        DiscountGroup g = new DiscountGroup();
        g.setId(new DiscountGroupId(groupId, typeCd, catCd));
        g.setIntRate(new BigDecimal(rate));
        return g;
    }

    private static TransactionCategoryBalance balance(String acctId, String typeCd, String catCd, String bal) {
        TransactionCategoryBalance t = new TransactionCategoryBalance();
        t.setId(new TransactionCategoryBalanceId(acctId, typeCd, catCd));
        t.setTranCatBal(new BigDecimal(bal));
        return t;
    }
}
