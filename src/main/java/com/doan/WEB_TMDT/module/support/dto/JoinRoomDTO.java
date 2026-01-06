package com.doan.WEB_TMDT.module.support.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomDTO {
    private Long ticketId;
    private String userName;
    private String userType;
}