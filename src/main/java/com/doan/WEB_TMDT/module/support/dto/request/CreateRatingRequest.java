package com.doan.WEB_TMDT.module.support.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

// Đánh giá
@Builder
@Data
public class CreateRatingRequest {
    @Min(1) @Max(5)
    private Integer rating;

    private String comment;
}
