package com.doan.WEB_TMDT.module.support.dto;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessageDTO {
    private String message;
    private String code;
}
