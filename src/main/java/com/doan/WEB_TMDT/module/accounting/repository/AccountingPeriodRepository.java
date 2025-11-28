package com.doan.WEB_TMDT.module.accounting.repository;

import com.doan.WEB_TMDT.module.accounting.entity.AccountingPeriod;
import com.doan.WEB_TMDT.module.accounting.entity.PeriodStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriod, Long> {
    List<AccountingPeriod> findByStatus(PeriodStatus status);
    
    Optional<AccountingPeriod> findByStartDateAndEndDate(LocalDate startDate, LocalDate endDate);
    
    List<AccountingPeriod> findAllByOrderByStartDateDesc();
}
