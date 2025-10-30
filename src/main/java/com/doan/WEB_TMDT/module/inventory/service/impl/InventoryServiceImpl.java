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

//    private final PurchaseOrderRepository poRepo;
//    private final PurchaseOrderItemRepository poItemRepo;
    private final ProductRepository productRepo;
    private final ProductDetailRepository detailRepo;
    private final InventoryStockRepository stockRepo;
    private final SupplierRepository supplierRepo;

    @Override
    public ApiResponse createPurchaseOrder(CreatePORequest req, String actor) {
        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(Supplier.builder().id(req.getSupplierId()).build())
                .status(POStatus.PENDING)
                .note(req.getNote())
                .createdDate(LocalDateTime.now())
                .build();

        List<PurchaseOrderItem> items = req.getItems().stream().map(itemReq -> {
            Product product = productRepository.findBySku(itemReq.getProductSku())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + itemReq.getProductSku()));

            return PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .build();
        }).toList();

        po.setItems(items);
        purchaseOrderRepository.save(po);
        return ApiResponse.success("Tạo phiếu nhập thành công, trạng thái PENDING");
    }

    @Override
    public ApiResponse addSerialToPO(Long poId, String serial, String productSku) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));
        if (po.getStatus() == POStatus.COMPLETED) {
            return ApiResponse.error("Phiếu đã hoàn tất, không thể thêm serial mới");
        }

        Product product = productRepository.findBySku(productSku)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        ProductDetail detail = ProductDetail.builder()
                .serialNumber(serial)
                .product(product)
                .poItem(po.getItems().stream()
                        .filter(i -> i.getProduct().getId().equals(product.getId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong PO")))
                .status(ProductStatus.IN_STOCK)
                .build();

        productDetailRepository.save(detail);
        return ApiResponse.success("Thêm serial " + serial + " vào PO thành công");
    }

    @Override
    public ApiResponse completePO(Long poId, LocalDateTime receivedDate) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));
        po.setStatus(POStatus.COMPLETED);
        po.setCompletedDate(receivedDate);
        purchaseOrderRepository.save(po);
        return ApiResponse.success("Phiếu nhập #" + po.getId() + " đã hoàn tất");
    }
}
