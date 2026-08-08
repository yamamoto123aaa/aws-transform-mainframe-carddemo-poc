package com.carddemo.interestcalc.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for TRAN-CAT-KEY (copybook CVTRA01Y):
 * TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD.
 */
@Embeddable
public class TransactionCategoryBalanceId implements Serializable {

    @Column(name = "acct_id", length = 11)
    private String acctId;

    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    @Column(name = "tran_cat_cd", length = 4)
    private String tranCatCd;

    public TransactionCategoryBalanceId() {
    }

    public TransactionCategoryBalanceId(String acctId, String tranTypeCd, String tranCatCd) {
        this.acctId = acctId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    public String getAcctId() {
        return acctId;
    }

    public void setAcctId(String acctId) {
        this.acctId = acctId;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public String getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(String tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionCategoryBalanceId)) return false;
        TransactionCategoryBalanceId that = (TransactionCategoryBalanceId) o;
        return Objects.equals(acctId, that.acctId)
                && Objects.equals(tranTypeCd, that.tranTypeCd)
                && Objects.equals(tranCatCd, that.tranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctId, tranTypeCd, tranCatCd);
    }
}
