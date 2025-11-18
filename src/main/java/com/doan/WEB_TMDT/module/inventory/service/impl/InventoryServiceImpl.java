package com.doan.WEB_TMDT.module.inventory.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.dto.*;
import com.doan.WEB_TMDT.module.inventory.entity.*;
import com.doan.WEB_TMDT.module.inventory.repository.*;
import com.doan.WEB_TMDT.module.inventory.service.InventoryService;
// ❌ Dòng này đã bị xóa/thay thế vì nó xung đột với ProductDetail của Product module:
// import com.doan.WEB_TMDT.module.inventory.entity.ProductDetail;
import com.doan.WEB_TMDT.module.inventory.entity.ProductStatus; // Giữ lại

// 💡 Thêm import entity ProductDetail đúng từ Product module
import lombok.extern.slf4j.Slf4j;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final ExportOrderRepository exportOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final WarehouseProductRepository warehouseProductRepository;
    private final ProductDetailRepository productDetailRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final SupplierRepository supplierRepository;
    private final com.doan.WEB_TMDT.module.inventory.service.ProductSpecificationService productSpecificationService;
    private String generateExportCode() {
        return "PX" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + String.format("%03d", new Random().nextInt(999));
    }

    @Override
    public  ApiResponse getOrCreateSupplier(CreateSupplierRequest req){

        if (req.getTaxCode() != null) {
            Optional<Supplier> byTax = supplierRepository.findByTaxCode(req.getTaxCode());
            if (byTax.isPresent()) {
                return ApiResponse.success("OK", byTax.get());
            }
        }

        if (req.getEmail() != null) {
            Optional<Supplier> byEmail = supplierRepository.findByEmail(req.getEmail());
            if (byEmail.isPresent()) {
                return ApiResponse.success("OK", byEmail.get());
            }
        }

        if (req.getPhone() != null) {
            Optional<Supplier> byPhone = supplierRepository.findByPhone(req.getPhone());
            if (byPhone.isPresent()) {
                return ApiResponse.success("OK", byPhone.get());
            }
        }
        Supplier supplier = Supplier.builder()
                .name(req.getName())
                .taxCode(req.getTaxCode())
                .email(req.getEmail())
                .phone(req.getPhone())
                .address(req.getAddress())
                .bankAccount(req.getBankAccount())
                .paymentTerm(req.getPaymentTerm())
                .active(true)
                .autoCreated(true)
                .build();
        Supplier savedSupplier = supplierRepository.save(supplier);
        return ApiResponse.success("OK", savedSupplier);

    }


    @Override
    public ApiResponse createPurchaseOrder(CreatePORequest req) {
        // 1️⃣ Kiểm tra dữ liệu đầu vào
        if (req.getSupplier() == null || req.getSupplier().getTaxCode() == null) {
            throw new IllegalArgumentException("Thiếu thông tin nhà cung cấp hoặc mã số thuế.");
        }

        CreateSupplierRequest sreq = req.getSupplier();

        // 2️⃣ Tìm NCC theo mã số thuế
        Supplier supplier = supplierRepository.findByTaxCode(sreq.getTaxCode())
                .orElseGet(() -> {
                    log.info("🆕 Tạo nhà cung cấp mới với mã số thuế: {}", sreq.getTaxCode());
                    return supplierRepository.save(
                            Supplier.builder()
                                    .name(sreq.getName())
                                    .contactName(sreq.getContactName())
                                    .taxCode(sreq.getTaxCode())
                                    .email(sreq.getEmail())
                                    .phone(sreq.getPhone())
                                    .address(sreq.getAddress())
                                    .bankAccount(sreq.getBankAccount())
                                    .paymentTerm(sreq.getPaymentTerm())
                                    .active(true)
                                    .autoCreated(true)
                                    .build()
                    );
                });

        // 3️⃣ Tạo phiếu nhập hàng (chỉ gắn theo taxCode)
        PurchaseOrder po = PurchaseOrder.builder()
                .poCode(req.getPoCode())
                .supplier(supplier) // join qua tax_code
                .status(POStatus.CREATED)
                .orderDate(LocalDateTime.now())
                .createdBy(req.getCreatedBy())
                .note(req.getNote())
                .build();

        // 4️⃣ Gắn sản phẩm — tạo WarehouseProduct nếu chưa có
        List<PurchaseOrderItem> items = req.getItems().stream().map(i -> {
            WarehouseProduct wp = warehouseProductRepository.findBySku(i.getSku())
                    .orElseGet(() -> {
                        log.info("🆕 Tạo WarehouseProduct mới cho SKU: {}", i.getSku());
                        WarehouseProduct newWp = WarehouseProduct.builder()
                                .sku(i.getSku())
                                .internalName("Sản phẩm mới - " + i.getSku())
                                .supplier(supplier)
                                .lastImportDate(LocalDateTime.now())
                                .description(i.getNote())
                                .techSpecsJson("{}")
                                .build();
                        WarehouseProduct savedWp = warehouseProductRepository.save(newWp);
                        
                        // Parse và lưu specifications
                        productSpecificationService.parseAndSaveSpecs(savedWp);
                        
                        return savedWp;
                    });

            return PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .sku(i.getSku())
                    .warehouseProduct(wp) // ✅ luôn có giá trị
                    .quantity(i.getQuantity())
                    .unitCost(i.getUnitCost())
                    .warrantyMonths(i.getWarrantyMonths())
                    .note(i.getNote())
                    .build();
        }).toList();

        po.setItems(items);
        purchaseOrderRepository.save(po);

        return ApiResponse.success("Tạo phiếu nhập hàng thành công", po);
    }


    @Override
    @Transactional
    public ApiResponse completePurchaseOrder(CompletePORequest req) {
        // 1️⃣ Lấy phiếu nhập hàng
        PurchaseOrder po = purchaseOrderRepository.findById(req.getPoId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập #" + req.getPoId()));

        if (po.getStatus() != POStatus.CREATED) {
            return ApiResponse.error("Phiếu nhập #" + req.getPoId() + " không ở trạng thái chờ nhập hàng (CREATED).");
        }

        // 2️⃣ Duyệt từng sản phẩm trong request
        for (ProductSerialRequest serialReq : req.getSerials()) {
            String sku = serialReq.getProductSku();

            // Tìm dòng item trong PO tương ứng với SKU
            PurchaseOrderItem item = po.getItems().stream()
                    .filter(i -> i.getSku().equals(sku))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Sản phẩm SKU " + sku + " không thuộc phiếu nhập #" + po.getId()));

            // Lấy WarehouseProduct (đã được tạo sẵn khi tạo PO)
            WarehouseProduct wp = item.getWarehouseProduct();
            if (wp == null) {
                throw new IllegalStateException("WarehouseProduct không tồn tại cho SKU: " + sku);
            }

            // 3️⃣ Kiểm tra số lượng serial có khớp số lượng đặt
            if (serialReq.getSerialNumbers().size() != item.getQuantity()) {
                throw new RuntimeException("Số serial (" + serialReq.getSerialNumbers().size() +
                        ") không khớp với số lượng nhập (" + item.getQuantity() + ") cho SKU: " + sku);
            }

            // 4️⃣ Kiểm tra trùng serial
            for (String sn : serialReq.getSerialNumbers()) {
                if (productDetailRepository.existsBySerialNumber(sn)) {
                    throw new RuntimeException("Serial " + sn + " đã tồn tại trong hệ thống!");
                }
            }
            final WarehouseProduct finalWp = wp;


            // 5️⃣ Tạo danh sách ProductDetail (serial cụ thể)
            List<ProductDetail> details = serialReq.getSerialNumbers().stream()
                    .map(sn -> ProductDetail.builder()
                            .serialNumber(sn)
                            .importPrice(item.getUnitCost())
                            .importDate(LocalDateTime.now())
                            .warrantyMonths(item.getWarrantyMonths())
                            .status(ProductStatus.IN_STOCK)
                            .warehouseProduct(finalWp )
                            .purchaseOrderItem(item)
                            .build())
                    .toList();

            // Gắn vào item và lưu
            if (item.getProductDetails() == null)
                item.setProductDetails(new ArrayList<>());
            item.getProductDetails().addAll(details);

            // 6️⃣ Cập nhật tồn kho
            InventoryStock stock = inventoryStockRepository.findByWarehouseProduct_Id(wp.getId())
                    .orElse(InventoryStock.builder()
                            .warehouseProduct(wp)
                            .onHand(0L)
                            .reserved(0L)
                            .damaged(0L)
                            .build());

            stock.setOnHand(stock.getOnHand() + details.size());
            inventoryStockRepository.save(stock);
        }

        // 7️⃣ Cập nhật phiếu nhập
        po.setReceivedDate(req.getReceivedDate());
        po.setStatus(POStatus.RECEIVED);
        purchaseOrderRepository.save(po);

        return ApiResponse.success("Hoàn tất nhập hàng thành công!", po.getId());
    }


    @Override
    @Transactional
    public ApiResponse createExportOrder(CreateExportOrderRequest req) {

        // 1️⃣ Tạo phiếu xuất
        ExportOrder exportOrder = ExportOrder.builder()
                .exportCode(generateExportCode())
                .exportDate(LocalDateTime.now())
                .createdBy(req.getCreatedBy())
                .reason(req.getReason())
                .note(req.getNote())
                .status(ExportStatus.CREATED)
                .build();

        List<ExportOrderItem> exportItems = new ArrayList<>();

        // 2️⃣ Duyệt từng sản phẩm trong danh sách xuất
        for (ExportItemRequest itemReq : req.getItems()) {

            WarehouseProduct product = warehouseProductRepository.findBySku(itemReq.getProductSku())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm SKU: " + itemReq.getProductSku()));

            int exportCount = itemReq.getSerialNumbers().size();
            double totalCost = 0;

            // 3️⃣ Kiểm tra tồn kho
            InventoryStock stock = inventoryStockRepository.findByWarehouseProduct_Id(product.getId())
                    .orElseThrow(() -> new RuntimeException("Không có dữ liệu tồn kho cho sản phẩm: " + product.getSku()));

            if (stock.getOnHand() < exportCount) {
                throw new RuntimeException("Không đủ hàng trong kho. Sẵn có: " + stock.getOnHand() +
                        ", yêu cầu xuất: " + exportCount + " (" + product.getSku() + ")");
            }

            // 4️⃣ Xử lý từng serial: cập nhật trạng thái & tính giá vốn
            for (String serial : itemReq.getSerialNumbers()) {
                ProductDetail detail = productDetailRepository.findBySerialNumber(serial)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy serial: " + serial));

                if (detail.getStatus() != ProductStatus.IN_STOCK) {
                    throw new RuntimeException("Serial " + serial + " không ở trạng thái IN_STOCK, không thể xuất kho!");
                }

                // cập nhật trạng thái serial
                detail.setStatus(ProductStatus.SOLD);
                detail.setSoldDate(LocalDateTime.now());
                productDetailRepository.save(detail);

                // cộng dồn giá nhập thật
                totalCost += detail.getImportPrice();
            }

            // 5️⃣ Cập nhật tồn kho
            stock.setOnHand(stock.getOnHand() - exportCount);
            inventoryStockRepository.save(stock);

            // 6️⃣ Ghi dòng chi tiết phiếu xuất
            ExportOrderItem item = ExportOrderItem.builder()
                    .exportOrder(exportOrder)
                    .warehouseProduct(product)
                    .sku(product.getSku())
                    .quantity((long) exportCount)
                    .serialNumbers(String.join(",", itemReq.getSerialNumbers()))
                    .totalCost(totalCost)
                    .build();

            exportItems.add(item);
        }

        // 7️⃣ Lưu phiếu xuất
        exportOrder.setItems(exportItems);
        exportOrder.setStatus(ExportStatus.COMPLETED);
        exportOrderRepository.save(exportOrder);

        return ApiResponse.success("Xuất kho thành công!", exportOrder.getExportCode());
    }

    @Override
    public ApiResponse getPurchaseOrders(POStatus status) {
        List<PurchaseOrder> orders;
        if (status != null) {
            orders = purchaseOrderRepository.findByStatus(status);
        } else {
            orders = purchaseOrderRepository.findAll();
        }
        return ApiResponse.success("Danh sách phiếu nhập", orders);
    }

    @Override
    public ApiResponse getExportOrders(ExportStatus status) {
        List<ExportOrder> orders;
        if (status != null) {
            orders = exportOrderRepository.findByStatus(status);
        } else {
            orders = exportOrderRepository.findAll();
        }
        return ApiResponse.success("Danh sách phiếu xuất", orders);
    }

}