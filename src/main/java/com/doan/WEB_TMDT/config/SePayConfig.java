package com.doan.WEB_TMDT.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class SePayConfig {

    @Value("${sepay.merchant-id}")
    private String merchantId;

    @Value("${sepay.secret-key}")
    private String secretKey;

    @Value("${sepay.api-url}")
    private String apiUrl;
}
