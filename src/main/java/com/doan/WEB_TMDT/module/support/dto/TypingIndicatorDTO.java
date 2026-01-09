package com.doan.WEB_TMDT.module.support.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicatorDTO {
    private Long ticketId;
    private String senderName;
    private Boolean isTyping;
}
