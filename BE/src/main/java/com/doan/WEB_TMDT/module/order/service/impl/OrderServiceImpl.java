package com.doan.WEB_TMDT.module.order.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.auth.entity.Customer;
import com.doan.WEB_TMDT.module.auth.repository.CustomerRepository;
import com.doan.WEB_TMDT.module.inventory.entity.*;
import com.doan.WEB_TMDT.module.inventory.repository.*;
import com.doan.WEB_TMDT.module.order.dto.*;
import com.doan.WEB_TMDT.module.order.entity.*;
import com.doan.WEB_TMDT.module.order.repository.*;
import com.doan.WEB_TMDT.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final ProductDetailRepository productDetailRepo;
    private final InventoryStockRepository stockRepo;

    @Transactional
    @Override
    public ApiResponse createOrder(CreateOrderRequest req) {
        Customer customer = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng!"));

        long total = 0L;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDTO dto : req.getItems()) {
            Product product = productRepo.findById(dto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm!"));

            ProductDetail detail = productDetailRepo.findAll().stream()
                    .filter(d -> d.getSerialNumber().equals(dto.getSerialNumber()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy serial: " + dto.getSerialNumber()));

            if (detail.getStatus() != ProductStatus.IN_STOCK)
                throw new IllegalStateException("Sản phẩm với serial " + dto.getSerialNumber() + " không còn trong kho!");

            // đánh dấu giữ hàng
            detail.setStatus(ProductStatus.RESERVED);
            productDetailRepo.save(detail);

            // cập nhật tồn
            InventoryStock stock = stockRepo.findByProduct(product)
                    .orElseThrow(() -> new IllegalStateException("Không có tồn kho cho sản phẩm này!"));
            stock.setReserved(stock.getReserved() + 1);
            stockRepo.save(stock);

            // tính tổng
            total += dto.getPrice();

            orderItems.add(OrderItem.builder()
                    .product(product)
                    .productDetail(detail)
                    .price(dto.getPrice())
                    .build());
        }

        Order order = Order.builder()
                .customer(customer)
                .shippingAddress(req.getShippingAddress())
                .note(req.getNote())
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepo.save(order);
        for (OrderItem i : orderItems) {
            i.setOrder(order);
            itemRepo.save(i);
        }

        return ApiResponse.success("Tạo đơn hàng thành công!", order);
    }

    @Override
    public ApiResponse getOrdersByCustomer(Long customerId) {
        return ApiResponse.success("Danh sách đơn hàng", orderRepo.findByCustomerId(customerId));
    }
}
