package com.doan.WEB_TMDT.module.auth.repository;

import com.doan.WEB_TMDT.module.auth.entity.EmployeeRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRegistrationRepository extends JpaRepository<EmployeeRegistration, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
