package com.carddemo.interestcalc.repository;

import com.carddemo.interestcalc.domain.TransactionCategoryBalance;
import com.carddemo.interestcalc.domain.TransactionCategoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Backs TCATBAL-FILE (VSAM KSDS, key = TRAN-CAT-KEY). CBACT04C reads this
 * file sequentially in key order (account id major), which is what makes
 * its control-break account-switch detection work. {@link
 * #findAllByOrderByIdAcctIdAscIdTranTypeCdAscIdTranCatCdAsc()} reproduces
 * that key order.
 */
public interface TransactionCategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceId> {

    List<TransactionCategoryBalance> findAllByOrderByIdAcctIdAscIdTranTypeCdAscIdTranCatCdAsc();
}
