package com.doan.WEB_TMDT.module.support.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long ticketId;
    private Long messageId;
    private String content;
    private String senderType;
    private String senderName;
    private String senderEmail;
    private LocalDateTime timestamp;
}
