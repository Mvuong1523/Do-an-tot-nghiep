package com.doan.WEB_TMDT.module.support.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SupportRatingResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}