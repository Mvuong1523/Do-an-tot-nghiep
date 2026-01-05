package com.doan.WEB_TMDT.module.support.repository;

import com.doan.WEB_TMDT.module.support.entities.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportTicketRepository  extends JpaRepository<SupportTicket,Long> {

    Page<SupportTicket> findAll(Specification<SupportTicket> spec, Pageable pageable);
}
