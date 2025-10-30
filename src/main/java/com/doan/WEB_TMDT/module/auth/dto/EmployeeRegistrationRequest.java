package com.doan.WEB_TMDT.module.auth.dto;

import com.doan.WEB_TMDT.module.auth.entity.Position;
import lombok.Data;

@Data
public class EmployeeRegistrationRequest {
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private Position position;
    private String note;
}
