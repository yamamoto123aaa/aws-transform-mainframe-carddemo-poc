package com.carddemo.interestcalc.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EmbeddedId;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * Maps to copybook CVTRA02Y (DIS-GROUP-RECORD, RECLN 50), read by CBACT04C
 * via DISCGRP-FILE. If a lookup on (groupId, tranTypeCd, tranCatCd) misses,
 * CBACT04C retries with groupId = "DEFAULT" (see
 * InterestCalcProcessor#lookupInterestRate).
 */
@Entity
@Table(name = "discount_group")
public class DiscountGroup {

    @EmbeddedId
    private DiscountGroupId id;

    @Column(name = "int_rate", precision = 6, scale = 2)
    private BigDecimal intRate;

    public DiscountGroupId getId() {
        return id;
    }

    public void setId(DiscountGroupId id) {
        this.id = id;
    }

    public BigDecimal getIntRate() {
        return intRate;
    }

    public void setIntRate(BigDecimal intRate) {
        this.intRate = intRate;
    }
}
