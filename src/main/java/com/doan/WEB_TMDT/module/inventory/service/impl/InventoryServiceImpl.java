package com.doan.WEB_TMDT.module.inventory.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.dto.*;
import com.doan.WEB_TMDT.module.inventory.entity.*;
import com.doan.WEB_TMDT.module.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements com.doan.WEB_TMDT.module.inventory.service.InventoryService {

    private final ProductRepository productRepo;
    private final SupplierRepository supplierRepo;
    private final InventoryStockRepository stockRepo;
    private final InventoryTransactionRepository txnRepo;
    private final InventoryTransactionItemRepository itemRepo;
    private final ProductDetailRepository productDetailRepo;

    // ===========================================================
    // 🧩 TẠO SẢN PHẨM
    // ===========================================================
    @Override
    public ApiResponse createProduct(CreateProductRequest req) {
        if (productRepo.existsBySku(req.getSku())) {
            return ApiResponse.error("SKU đã tồn tại!");
        }
        Product p = Product.builder()
                .sku(req.getSku())
                .name(req.getName())
                .brand(req.getBrand())
                .category(req.getCategory())
                .unit(req.getUnit())
                .price(req.getPrice() == null ? 0L : req.getPrice())
                .active(true)
                .build();
        productRepo.save(p);

        // tạo bản ghi tồn kho = 0
        InventoryStock stock = InventoryStock.builder()
                .product(p)
                .onHand(0L)
                .reserved(0L)
                .build();
        stockRepo.save(stock);

        return ApiResponse.success("Tạo sản phẩm thành công!", p);
    }

    // ===========================================================
    // 🧾 TẠO NHÀ CUNG CẤP
    // ===========================================================
    @Override
    public ApiResponse createSupplier(CreateSupplierRequest req) {
        if (supplierRepo.existsByName(req.getName())) {
            return ApiResponse.error("Nhà cung cấp đã tồn tại!");
        }
        Supplier s = Supplier.builder()
                .name(req.getName())
                .contactName(req.getContactName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .address(req.getAddress())
                .active(true)
                .build();
        supplierRepo.save(s);
        return ApiResponse.success("Tạo nhà cung cấp thành công!", s);
    }

    // ===========================================================
    // 📦 NHẬP KHO (IMPORT)
    // ===========================================================
    @Transactional
    @Override
    public ApiResponse importStock(ImportStockRequest req, String actorEmail) {
        Supplier supplier = null;
        if (req.getSupplierName() != null) {
            supplier = supplierRepo.findByName(req.getSupplierName()).orElse(null);
        }

        String code = genCode("IM");
        InventoryTransaction txn = InventoryTransaction.builder()
                .type(TransactionType.IMPORT)
                .code(code)
                .supplier(supplier)
                .createdAt(LocalDateTime.now())
                .createdBy(actorEmail)
                .note(req.getNote())
                .build();

        List<InventoryTransactionItem> items = new ArrayList<>();

        for (StockItemDTO dto : req.getItems()) {
            Product p = productRepo.findById(dto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm id=" + dto.getProductId()));

            long q = dto.getQuantity() == null ? 0 : dto.getQuantity();
            if (q <= 0) throw new IllegalArgumentException("Số lượng phải > 0 cho productId=" + p.getId());

            if (dto.getSerialNumbers() == null || dto.getSerialNumbers().size() != q) {
                throw new IllegalArgumentException("Danh sách serial phải khớp với số lượng nhập cho sản phẩm: " + p.getName());
            }

            // cập nhật tồn kho tổng
            InventoryStock stock = stockRepo.findByProduct(p)
                    .orElseGet(() -> stockRepo.save(InventoryStock.builder().product(p).onHand(0L).reserved(0L).build()));
            stock.setOnHand(stock.getOnHand() + q);
            stockRepo.save(stock);

            // tạo chi tiết phiếu
            InventoryTransactionItem item = InventoryTransactionItem.builder()
                    .transaction(txn)
                    .product(p)
                    .quantity(q)
                    .unitCost(dto.getUnitCost())
                    .build();
            itemRepo.save(item);
            items.add(item);

            // tạo chi tiết serial/IMEI
            for (String serial : dto.getSerialNumbers()) {
                if (productDetailRepo.findBySerialNumber(serial).isPresent()) {
                    throw new IllegalArgumentException("Serial/IMEI đã tồn tại: " + serial);
                }
                ProductDetail detail = ProductDetail.builder()
                        .serialNumber(serial)
                        .product(p)
                        .transactionItem(item)
                        .status(ProductStatus.IN_STOCK)
                        .build();
                productDetailRepo.save(detail);
            }
        }

        txn.setItems(items);
        txnRepo.save(txn);
        return ApiResponse.success("Nhập kho thành công! Mã phiếu: " + code, txn);
    }

    // ===========================================================
    // 🚚 XUẤT KHO (EXPORT)
    // ===========================================================
    @Transactional
    @Override
    public ApiResponse exportStock(ExportStockRequest req, String actorEmail) {
        String code = genCode("EX");
        InventoryTransaction txn = InventoryTransaction.builder()
                .type(TransactionType.EXPORT)
                .code(code)
                .createdAt(LocalDateTime.now())
                .createdBy(actorEmail)
                .note(req.getNote())
                .build();

        List<InventoryTransactionItem> items = new ArrayList<>();

        for (StockItemDTO dto : req.getItems()) {
            Product p = productRepo.findById(dto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm id=" + dto.getProductId()));

            if (dto.getSerialNumbers() == null || dto.getSerialNumbers().isEmpty()) {
                throw new IllegalArgumentException("Phải cung cấp danh sách serial khi xuất hàng cho sản phẩm: " + p.getName());
            }

            // kiểm tra serial tồn tại và hợp lệ
            for (String serial : dto.getSerialNumbers()) {
                ProductDetail detail = productDetailRepo.findBySerialNumber(serial)
                        .orElseThrow(() -> new IllegalArgumentException("Serial không tồn tại: " + serial));

                if (!detail.getProduct().equals(p)) {
                    throw new IllegalArgumentException("Serial " + serial + " không thuộc sản phẩm " + p.getName());
                }
                if (detail.getStatus() != ProductStatus.IN_STOCK) {
                    throw new IllegalArgumentException("Serial " + serial + " không còn trong kho");
                }

                // cập nhật trạng thái serial
                detail.setStatus(ProductStatus.SOLD);
                productDetailRepo.save(detail);
            }

            // cập nhật tồn kho tổng
            InventoryStock stock = stockRepo.findByProduct(p)
                    .orElseThrow(() -> new IllegalStateException("Chưa có tồn kho cho sản phẩm " + p.getName()));
            stock.setOnHand(stock.getOnHand() - dto.getSerialNumbers().size());
            stockRepo.save(stock);

            InventoryTransactionItem item = InventoryTransactionItem.builder()
                    .transaction(txn)
                    .product(p)
                    .quantity((long) dto.getSerialNumbers().size())
                    .build();
            itemRepo.save(item);
            items.add(item);
        }

        txn.setItems(items);
        txnRepo.save(txn);
        return ApiResponse.success("Xuất kho thành công! Mã phiếu: " + code, txn);
    }

    // ===========================================================
    // 📊 LẤY DANH SÁCH TỒN KHO
    // ===========================================================
    @Override
    public ApiResponse getStocks() {
        var list = stockRepo.findAll().stream().map(s -> Map.of(
                "productId", s.getProduct().getId(),
                "sku", s.getProduct().getSku(),
                "name", s.getProduct().getName(),
                "onHand", s.getOnHand(),
                "reserved", s.getReserved(),
                "available", s.getAvailable()
        )).toList();
        return ApiResponse.success("Danh sách tồn kho", list);
    }

    // ===========================================================
    // ⚙️ SINH CODE TỰ ĐỘNG
    // ===========================================================
    private String genCode(String prefix) {
        return prefix + "-" + System.currentTimeMillis();
    }
}
