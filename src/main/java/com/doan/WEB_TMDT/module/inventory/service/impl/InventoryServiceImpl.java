package com.doan.WEB_TMDT.module.inventory.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.dto.*;
import com.doan.WEB_TMDT.module.inventory.entity.*;
import com.doan.WEB_TMDT.module.inventory.repository.*;
import com.doan.WEB_TMDT.module.inventory.service.InventoryService;
import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.entity.ProductDetail;
import com.doan.WEB_TMDT.module.product.entity.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final PurchaseOrderRepository poRepo;
    private final PurchaseOrderItemRepository poItemRepo;
    private final ProductRepository productRepo;
    private final ProductDetailRepository detailRepo;
    private final InventoryStockRepository stockRepo;
    private final SupplierRepository supplierRepo;

    @Override
    public ApiResponse createPurchaseOrder(CreatePORequest req, String actor) {
        Supplier s = supplierRepo.findByName(req.getSupplierName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy NCC!"));
        String code = "PO-" + System.currentTimeMillis();

        PurchaseOrder po = PurchaseOrder.builder()
                .poCode(code)
                .supplier(s)
                .orderDate(LocalDateTime.now())
                .status(PurchaseStatus.CREATED)
                .createdBy(actor)
                .note(req.getNote())
                .build();

        List<PurchaseOrderItem> items = new ArrayList<>();
        for (POItemDTO dto : req.getItems()) {
            Product p = productRepo.findBySku(dto.getSku())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm SKU=" + dto.getSku()));
            items.add(PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .product(p)
                    .quantity(dto.getQuantity())
                    .unitCost(dto.getUnitCost())
                    .build());
        }
        po.setItems(items);
        poRepo.save(po);

        return ApiResponse.success("Tạo đơn nhập hàng thành công!", po);
    }

    @Override
    public ApiResponse addSerialToPO(Long poId, String serial, String sku) {
        PurchaseOrder po = poRepo.findById(poId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy PO!"));
        if (po.getStatus() == PurchaseStatus.COMPLETED)
            return ApiResponse.error("PO đã hoàn tất!");

        PurchaseOrderItem item = po.getItems().stream()
                .filter(i -> i.getProduct().getSku().equals(sku))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không có sản phẩm này trong PO!"));

        ProductDetail detail = ProductDetail.builder()
                .serialNumber(serial)
                .product(item.getProduct())
                .poItem(item)
                .status(ProductStatus.IN_STOCK)
                .build();

        detailRepo.save(detail);
        return ApiResponse.success("Đã thêm serial vào PO!", detail);
    }

    @Override
    public ApiResponse completePO(Long poId, LocalDateTime receivedDate) {
        PurchaseOrder po = poRepo.findById(poId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy PO!"));

        po.setStatus(PurchaseStatus.COMPLETED);
        po.setReceivedDate(receivedDate);

        for (PurchaseOrderItem item : po.getItems()) {
            InventoryStock stock = stockRepo.findByProduct(item.getProduct())
                    .orElseGet(() -> stockRepo.save(
                            InventoryStock.builder().product(item.getProduct()).onHand(0L).reserved(0L).build()
                    ));
            long added = item.getProductDetails().size();
            stock.setOnHand(stock.getOnHand() + added);
            stockRepo.save(stock);
        }

        poRepo.save(po);
        return ApiResponse.success("Hoàn tất đơn nhập kho!", po);
    }

    @Override
    public ApiResponse performStockAudit(StockAuditRequest req, String actor) {
        StringBuilder log = new StringBuilder();
        for (StockAuditItemDTO dto : req.getItems()) {
            Product p = productRepo.findBySku(dto.getSku())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy SKU=" + dto.getSku()));
            InventoryStock stock = stockRepo.findByProduct(p)
                    .orElseThrow(() -> new IllegalStateException("Không có tồn kho!"));

            stock.setOnHand(dto.getActualCount());
            stockRepo.save(stock);
            log.append("✔ ").append(p.getName()).append(" -> ").append(dto.getActualCount()).append("\n");
        }
        return ApiResponse.success("Đã cập nhật kiểm kê tồn kho", log.toString());
    }
}
