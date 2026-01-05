package com.doan.WEB_TMDT.module.support.repository;

import com.doan.WEB_TMDT.module.support.entities.SupportReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportReplyRepository  extends JpaRepository<SupportReply, Long> {

}
