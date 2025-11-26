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
    public Long getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));
        return user.getId();
    }

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
                .status(OrderStatus.CONFIRMED)  // Tự động xác nhận
                .paymentStatus(PaymentStatus.UNPAID)
                .confirmedAt(LocalDateTime.now())  // Set thời gian xác nhận
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
    public ApiResponse cancelOrderByCustomer(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Verify ownership
        if (!order.getUser().getId().equals(userId)) {
            return ApiResponse.error("Bạn không có quyền hủy đơn hàng này");
        }

        // Chỉ cho phép hủy khi chưa giao hàng (CONFIRMED hoặc SHIPPING)
        if (order.getStatus() == OrderStatus.DELIVERED) {
            return ApiResponse.error("Không thể hủy đơn hàng đã giao thành công");
        }
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return ApiResponse.error("Đơn hàng đã bị hủy trước đó");
        }

        // Check payment status - nếu đã thanh toán thì cần hoàn tiền
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            // TODO: Tích hợp API hoàn tiền
            log.warn("Order {} đã thanh toán, cần xử lý hoàn tiền", order.getOrderCode());
        }

        // Cancel order
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(reason != null ? reason : "Khách hàng hủy đơn");
        orderRepository.save(order);

        log.info("Cancelled order {} by customer {}", order.getOrderCode(), userId);

        OrderResponse response = toOrderResponse(order);
        return ApiResponse.success("Đã hủy đơn hàng" + 
            (order.getPaymentStatus() == PaymentStatus.PAID ? ". Tiền sẽ được hoàn lại trong 3-5 ngày làm việc" : ""), 
            response);
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

    // Admin/Staff methods

    @Override
    public ApiResponse getAllOrders(String status, int page, int size) {
        List<Order> orders;
        
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByStatus(orderStatus);
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("Trạng thái không hợp lệ");
            }
        } else {
            orders = orderRepository.findAll();
        }
        
        // Sort by created date desc
        orders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
        
        List<OrderResponse> responses = orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success("Danh sách đơn hàng", responses);
    }

    @Override
    @Transactional
    public ApiResponse confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != OrderStatus.PENDING) {
            return ApiResponse.error("Chỉ có thể xác nhận đơn hàng ở trạng thái chờ xác nhận");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Confirmed order {}", order.getOrderCode());

        OrderResponse response = toOrderResponse(order);
        return ApiResponse.success("Đã xác nhận đơn hàng", response);
    }

    @Override
    @Transactional
    public ApiResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
            
            // Update timestamps based on status
            switch (newStatus) {
                case CONFIRMED:
                    if (order.getConfirmedAt() == null) {
                        order.setConfirmedAt(LocalDateTime.now());
                    }
                    break;
                case SHIPPING:
                    if (order.getShippedAt() == null) {
                        order.setShippedAt(LocalDateTime.now());
                    }
                    break;
                case DELIVERED:
                    if (order.getDeliveredAt() == null) {
                        order.setDeliveredAt(LocalDateTime.now());
                    }
                    order.setPaymentStatus(PaymentStatus.PAID);
                    break;
                case CANCELLED:
                    if (order.getCancelledAt() == null) {
                        order.setCancelledAt(LocalDateTime.now());
                    }
                    break;
            }
            
            orderRepository.save(order);
            log.info("Updated order {} status to {}", order.getOrderCode(), newStatus);

            OrderResponse response = toOrderResponse(order);
            return ApiResponse.success("Đã cập nhật trạng thái đơn hàng", response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("Trạng thái không hợp lệ");
        }
    }

    @Override
    @Transactional
    public ApiResponse markAsShipping(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            return ApiResponse.error("Chỉ có thể chuyển sang đang giao hàng từ trạng thái đã xác nhận");
        }

        order.setStatus(OrderStatus.SHIPPING);
        order.setShippedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Marked order {} as shipping", order.getOrderCode());

        OrderResponse response = toOrderResponse(order);
        return ApiResponse.success("Đã chuyển đơn hàng sang đang giao", response);
    }

    @Override
    @Transactional
    public ApiResponse markAsDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != OrderStatus.SHIPPING) {
            return ApiResponse.error("Chỉ có thể chuyển sang đã giao từ trạng thái đang giao hàng");
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        order.setPaymentStatus(PaymentStatus.PAID); // Mark as paid when delivered (COD)
        orderRepository.save(order);

        log.info("Marked order {} as delivered", order.getOrderCode());

        OrderResponse response = toOrderResponse(order);
        return ApiResponse.success("Đã xác nhận giao hàng thành công", response);
    }

    @Override
    public ApiResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        OrderResponse response = toOrderResponse(order);
        return ApiResponse.success("Chi tiết đơn hàng", response);
    }

    @Override
    public ApiResponse getOrderStatistics() {
        List<Order> allOrders = orderRepository.findAll();
        
        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long confirmedOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED).count();
        long shippingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPING).count();
        long deliveredOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelledOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        
        Double totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(Order::getTotal)
                .sum();
        
        var statistics = new java.util.HashMap<String, Object>();
        statistics.put("totalOrders", totalOrders);
        statistics.put("pendingOrders", pendingOrders);
        statistics.put("confirmedOrders", confirmedOrders);
        statistics.put("shippingOrders", shippingOrders);
        statistics.put("deliveredOrders", deliveredOrders);
        statistics.put("cancelledOrders", cancelledOrders);
        statistics.put("totalRevenue", totalRevenue);
        
        return ApiResponse.success("Thống kê đơn hàng", statistics);
    }

    @Override
    @Transactional
    public ApiResponse cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Admin/Staff can cancel any order except delivered
        if (order.getStatus() == OrderStatus.DELIVERED) {
            return ApiResponse.error("Không thể hủy đơn hàng đã giao thành công");
        }

        // Cancel order
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(reason != null ? reason : "Hủy bởi nhân viên");
        orderRepository.save(order);

        log.info("Cancelled order {} by admin/staff", order.getOrderCode());

        OrderResponse response = toOrderResponse(order);
        return ApiResponse.success("Đã hủy đơn hàng", response);
    }
}
