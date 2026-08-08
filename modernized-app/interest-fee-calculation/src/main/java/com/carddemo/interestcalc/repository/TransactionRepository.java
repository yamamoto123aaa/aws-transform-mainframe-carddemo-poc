package com.carddemo.interestcalc.repository;

import com.carddemo.interestcalc.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

/** Backs TRANSACT-FILE (sequential output). */
public interface TransactionRepository extends JpaRepository<Transaction, String> {
}
