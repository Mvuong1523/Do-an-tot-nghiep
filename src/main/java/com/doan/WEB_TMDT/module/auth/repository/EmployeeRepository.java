package com.doan.WEB_TMDT.module.auth.repository;

import com.doan.WEB_TMDT.module.auth.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUserId(Long userId);
}
