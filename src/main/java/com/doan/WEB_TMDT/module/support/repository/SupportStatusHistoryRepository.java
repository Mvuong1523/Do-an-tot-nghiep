package com.doan.WEB_TMDT.module.support.repository;

import com.doan.WEB_TMDT.module.support.entities.SupportStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportStatusHistoryRepository extends JpaRepository<SupportStatusHistory,Long> {

    List<SupportStatusHistory> findBySupportTicketIdOrderByCreatedAtDesc(Long ticketId);

}
