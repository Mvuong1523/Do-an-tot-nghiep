package com.doan.WEB_TMDT.scheduler;

import com.doan.WEB_TMDT.module.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks cho payment
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentService paymentService;

    /**
     * Tự động hết hạn các payment cũ
     * Chạy mỗi 10 phút (tối ưu tài nguyên)
     */
    @Scheduled(fixedRate = 600000) // 600 seconds = 10 minutes
    public void expireOldPayments() {
        log.debug("Running scheduled task: expireOldPayments");
        try {
            paymentService.expireOldPayments();
        } catch (Exception e) {
            log.error("Error in expireOldPayments scheduled task", e);
        }
    }
}
