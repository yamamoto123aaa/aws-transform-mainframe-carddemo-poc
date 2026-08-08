package com.carddemo.interestcalc.repository;

import com.carddemo.interestcalc.domain.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Backs XREF-FILE (VSAM KSDS, primary key = XREF-CARD-NUM, alternate key =
 * XREF-ACCT-ID). CBACT04C (paragraph 1110-GET-XREF-DATA) only ever reads by
 * the alternate key, hence {@link #findFirstByXrefAcctId}.
 */
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    Optional<CardXref> findFirstByXrefAcctId(String xrefAcctId);
}
