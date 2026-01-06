package com.doan.WEB_TMDT.module.support.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDTO {
    private Long ticketId;
    private String userName;
    private String userType;
    private String status;
    private String email;
}
