package com.doan.WEB_TMDT.module.accounting.listener;

import com.doan.WEB_TMDT.module.accounting.service.FinancialTransactionService;
import com.doan.WEB_TMDT.module.order.entity.Order;
import com.doan.WEB_TMDT.module.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final FinancialTransactionService financialTransactionService;

    @TransactionalEventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        Order order = event.getOrder();
        OrderStatus newStatus = event.getNewStatus();
        
        log.info("Processing order status change: {} -> {}", order.getOrderCode(), newStatus);
        
        // Tạo giao dịch tài chính khi đơn hàng được giao thành công (DELIVERED)
        // hoặc khi đơn được xác nhận và đã thanh toán
        if (newStatus == OrderStatus.DELIVERED || 
            (newStatus == OrderStatus.CONFIRMED && order.getPaymentStatus() == com.doan.WEB_TMDT.module.order.entity.PaymentStatus.PAID)) {
            try {
                financialTransactionService.createTransactionFromOrder(order.getOrderCode());
                log.info("Created financial transactions for order: {}", order.getOrderCode());
            } catch (Exception e) {
                log.error("Failed to create financial transactions for order: {}", order.getOrderCode(), e);
            }
        }
        
        // Tạo giao dịch hoàn tiền khi đơn hàng bị hủy sau khi đã thanh toán
        if (newStatus == OrderStatus.CANCELLED && 
            order.getPaymentStatus() == com.doan.WEB_TMDT.module.order.entity.PaymentStatus.PAID) {
            try {
                financialTransactionService.createRefundTransaction(
                    order.getOrderCode(), 
                    String.valueOf(order.getTotal())
                );
                log.info("Created refund transaction for cancelled order: {}", order.getOrderCode());
            } catch (Exception e) {
                log.error("Failed to create refund transaction for order: {}", order.getOrderCode(), e);
            }
        }
    }
}