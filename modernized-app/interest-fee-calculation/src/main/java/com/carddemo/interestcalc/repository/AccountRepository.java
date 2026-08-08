package com.carddemo.interestcalc.repository;

import com.carddemo.interestcalc.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

/** Backs ACCOUNT-FILE (VSAM KSDS, key = ACCT-ID). */
public interface AccountRepository extends JpaRepository<Account, String> {
}
