package com.carddemo.interestcalc.repository;

import com.carddemo.interestcalc.domain.DiscountGroup;
import com.carddemo.interestcalc.domain.DiscountGroupId;
import org.springframework.data.jpa.repository.JpaRepository;

/** Backs DISCGRP-FILE (VSAM KSDS, key = DIS-GROUP-KEY). */
public interface DiscountGroupRepository extends JpaRepository<DiscountGroup, DiscountGroupId> {
}
