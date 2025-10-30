package com.doan.WEB_TMDT.module.auth.repository;

import com.doan.WEB_TMDT.module.auth.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByEmailAndOtpCode(String email, String otpCode);
}
