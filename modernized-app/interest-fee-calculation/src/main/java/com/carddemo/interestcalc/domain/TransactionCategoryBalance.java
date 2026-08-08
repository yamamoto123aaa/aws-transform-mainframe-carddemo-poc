package com.carddemo.interestcalc.domain;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * Maps to copybook CVTRA01Y (TRAN-CAT-BAL-RECORD, RECLN 50), read
 * sequentially by CBACT04C via TCATBAL-FILE (VSAM KSDS keyed on
 * TRAN-CAT-KEY = account id + type code + category code). Sequential read
 * order is account-id-major, which is what the control-break logic in
 * InterestCalcProcessor relies on.
 */
@Entity
@Table(name = "transaction_category_balance")
public class TransactionCategoryBalance {

    @EmbeddedId
    private TransactionCategoryBalanceId id;

    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal tranCatBal;

    public TransactionCategoryBalanceId getId() {
        return id;
    }

    public void setId(TransactionCategoryBalanceId id) {
        this.id = id;
    }

    public BigDecimal getTranCatBal() {
        return tranCatBal;
    }

    public void setTranCatBal(BigDecimal tranCatBal) {
        this.tranCatBal = tranCatBal;
    }
}
