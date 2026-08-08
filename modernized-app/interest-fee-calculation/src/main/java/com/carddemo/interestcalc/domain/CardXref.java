package com.carddemo.interestcalc.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Maps to copybook CVACT03Y (CARD-XREF-RECORD, RECLN 50), read by CBACT04C
 * via XREF-FILE using the alternate key FD-XREF-ACCT-ID (READ ... KEY IS
 * FD-XREF-ACCT-ID). This program only ever looks records up by account id,
 * so {@code xrefAcctId} is modeled as unique for that lookup.
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @Column(name = "xref_card_num", length = 16)
    private String xrefCardNum;

    @Column(name = "xref_cust_id", length = 9)
    private String xrefCustId;

    @Column(name = "xref_acct_id", length = 11)
    private String xrefAcctId;

    public String getXrefCardNum() {
        return xrefCardNum;
    }

    public void setXrefCardNum(String xrefCardNum) {
        this.xrefCardNum = xrefCardNum;
    }

    public String getXrefCustId() {
        return xrefCustId;
    }

    public void setXrefCustId(String xrefCustId) {
        this.xrefCustId = xrefCustId;
    }

    public String getXrefAcctId() {
        return xrefAcctId;
    }

    public void setXrefAcctId(String xrefAcctId) {
        this.xrefAcctId = xrefAcctId;
    }
}
