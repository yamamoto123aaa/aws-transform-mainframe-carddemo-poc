package com.carddemo.interestcalc.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for DIS-GROUP-KEY (copybook CVTRA02Y):
 * DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD.
 */
@Embeddable
public class DiscountGroupId implements Serializable {

    @Column(name = "acct_group_id", length = 10)
    private String acctGroupId;

    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    @Column(name = "tran_cat_cd", length = 4)
    private String tranCatCd;

    public DiscountGroupId() {
    }

    public DiscountGroupId(String acctGroupId, String tranTypeCd, String tranCatCd) {
        this.acctGroupId = acctGroupId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    public String getAcctGroupId() {
        return acctGroupId;
    }

    public void setAcctGroupId(String acctGroupId) {
        this.acctGroupId = acctGroupId;
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
        if (!(o instanceof DiscountGroupId)) return false;
        DiscountGroupId that = (DiscountGroupId) o;
        return Objects.equals(acctGroupId, that.acctGroupId)
                && Objects.equals(tranTypeCd, that.tranTypeCd)
                && Objects.equals(tranCatCd, that.tranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctGroupId, tranTypeCd, tranCatCd);
    }
}
