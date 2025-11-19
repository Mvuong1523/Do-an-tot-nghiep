package com.doan.WEB_TMDT.module.order.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.entity.User;
import com.doan.WEB_TMDT.module.auth.repository.UserRepository;
import com.doan.WEB_TMDT.module.cart.entity.Cart;
import com.doan.WEB_TMDT.module.cart.entity.CartItem;
import com.doan.WEB_TMDT.module.cart.repository.CartRepository;
import com.doan.WEB_TMDT.module.order.dto.CreateOrderRequest;
import com.doan.WEB_TMDT.module.order.dto.OrderItemResponse;
import com.doan.WEB_TMDT.module.order.dto.OrderResponse;
import com.doan.WEB_TMDT.module.order.entity.Order;
import com.doan.WEB_TMDT.module.order.entity.OrderItem;
import com.doan.WEB_TMDT.module.order.entity.OrderStatus;
import com.doan.WEB_TMDT.module.order.entity.PaymentStatus;
import com.doan.WEB_TMDT.module.order.repository.OrderRepository;
import com.doan.WEB_TMDT.module.order.service.OrderService;
import com.doan.WEB_TMDT.module.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse createOrderFromCart(Long userId, CreateOrderRequest request) {
        // 1. Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 2. Get cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));

        if (cart.getItems().isEmpty()) {
            return ApiResponse.error("Giỏ hàng trống");
        }

        // 3. Validate stock for all items
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() == null || 
                product.getStockQuantity() < cartItem.getQuantity()) {
                return ApiResponse.error("Sản phẩm " + product.getName() + " không đủ số lượng");
            }
        }

        // 4. Calculate totals
        Double subtotal = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        Double shippingFee = request.getShippingFee();
        Double discount = 0.0; // TODO: Apply voucher
        Double total = subtotal + shippingFee - discount;

        // 5. Create order
        String orderCode = generateOrderCode();
        String fullAddress = String.format("%s, %s, %s, %s",
                request.getAddress(), request.getWard(), 
                request.getDistrict(), request.getProvince());

        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .shippingAddress(fullAddress)
                .note(request.getNote())
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discount(discount)
                .total(total)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        // 6. Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .price(cartItem.getPrice())
                    .quantity(cartItem.getQuantity())
                    .subtotal(cartItem.getPrice() * cartItem.getQuantity())
                    .build();
            
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);

        // 7. Save order
        Order savedOrder = orderRepository.save(order);

        // 8. Clear cart
        cart.clearItems();
        cartRepository.save(cart);

        log.info("Created order {} for user {}", orderCode, userId);

        // 9. Return response
        OrderResponse response = toOrderResponse(savedOrder);
        return ApiResponse.success("Đặt hàng thành công", response);
    }

    @Override
    public ApiResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Verify ownership
        if (!order.getUser().getId().equals(userId)) {
            return ApiResponse.error("Bạn không có quyền xem đơn hàng này");
        }

        OrderResponse response = toOrderResponse(order);
        return ApiResponse.success("Chi tiết đơn hàng", response);
    }

    @Override
    public ApiResponse getOrderByCode(String orderCode, Long userId) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Verify ownership
        if (!order.getUser().getId().equals(userId)) {
            return ApiResponse.error("Bạn không có quyền xem đơn hàng này");
        }

        OrderResponse response = toOrderResponse(order);
        return ApiResponse.success("Chi tiết đơn hàng", response);
    }

    @Override
    public ApiResponse getMyOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        List<OrderResponse> responses = orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
        return ApiResponse.success("Danh sách đơn hàng", responses);
    }

    @Override
    @Transactional
    public ApiResponse cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Verify ownership
        if (!order.getUser().getId().equals(userId)) {
            return ApiResponse.error("Bạn không có quyền hủy đơn hàng này");
        }

        // Check if can cancel
        if (order.getStatus() != OrderStatus.PENDING && 
            order.getStatus() != OrderStatus.CONFIRMED) {
            return ApiResponse.error("Không thể hủy đơn hàng ở trạng thái này");
        }

        // Check payment status
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return ApiResponse.error("Đơn hàng đã thanh toán, vui lòng liên hệ CSKH để hoàn tiền");
        }

        // Cancel order
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(reason);
        orderRepository.save(order);

        log.info("Cancelled order {} by user {}", order.getOrderCode(), userId);

        OrderResponse response = toOrderResponse(order);
        return ApiResponse.success("Đã hủy đơn hàng", response);
    }

    // Helper methods

    private String generateOrderCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = new Random().nextInt(9999);
        String code = "ORD" + date + String.format("%04d", random);

        // Check if exists
        if (orderRepository.existsByOrderCode(code)) {
            return generateOrderCode(); // Retry
        }

        return code;
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .note(order.getNote())
                .items(items)
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discount(order.getDiscount())
                .total(order.getTotal())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .cancelReason(order.getCancelReason())
                .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .itemId(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .productImage(item.getProduct().getImageUrl())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .serialNumber(item.getSerialNumber())
                .build();
    }
}
