package com.doan.WEB_TMDT.module.support.repository;

import com.doan.WEB_TMDT.module.support.entities.SupportRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportRatingRepository extends JpaRepository<SupportRating, Long> {

}
