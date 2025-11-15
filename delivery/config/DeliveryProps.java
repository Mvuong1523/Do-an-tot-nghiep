package com.doan.WEB_TMDT.module.delivery.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Bind các key ghtk.* trong application.properties */
@Data
@Component
@ConfigurationProperties(prefix = "ghtk")   // hoặc "delivery" nếu bạn đổi key
public class DeliveryProps {
    private String baseUrl;
    private String token;
    private String partnerCode;
    private Pickup pickup;

    @Data
    public static class Pickup {
        private String name;
        private String address;
        private String province;
        private String district;
        private String ward;
        private String tel;
    }
}
