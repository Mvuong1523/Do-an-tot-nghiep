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
                        
                        // Lấy thông tin từ request
                        String internalName = i.getInternalName() != null && !i.getInternalName().isEmpty()
                                ? i.getInternalName()
                                : "Sản phẩm mới - " + i.getSku();
                        
                        String techSpecs = i.getTechSpecsJson() != null && !i.getTechSpecsJson().isEmpty()
                                ? i.getTechSpecsJson()
                                : "{}";
                        
                        WarehouseProduct newWp = WarehouseProduct.builder()
                                .sku(i.getSku())
                                .internalName(internalName)
                                .supplier(supplier)
                                .lastImportDate(LocalDateTime.now())
                                .description(i.getNote())
                                .techSpecsJson(techSpecs)
                                .build();
                        WarehouseProduct savedWp = warehouseProductRepository.save(newWp);
                        
                        // Parse và lưu specifications vào bảng riêng
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
        try {
            return doCompletePurchaseOrder(req);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Lỗi trùng lặp serial khi nhập hàng", e);
            String message = e.getMessage();
            if (message != null && message.contains("Duplicate entry")) {
                // Extract serial number from error message
                return ApiResponse.error("Serial bị trùng lặp! Vui lòng kiểm tra lại các serial đã nhập.");
            }
            return ApiResponse.error("Lỗi dữ liệu: " + e.getMessage());
        }
    }

    private ApiResponse doCompletePurchaseOrder(CompletePORequest req) {
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
                if (sn == null || sn.trim().isEmpty()) {
                    throw new RuntimeException("Serial không được để trống cho SKU: " + sku);
                }
                if (productDetailRepository.existsBySerialNumber(sn)) {
                    throw new RuntimeException("Serial " + sn + " đã tồn tại trong hệ thống! Vui lòng kiểm tra lại.");
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

    @Override
    public ApiResponse getPurchaseOrderDetail(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập #" + id));
        
        // Map to DTO to avoid circular reference
        com.doan.WEB_TMDT.module.inventory.dto.PurchaseOrderDetailResponse dto = mapToPurchaseOrderDetailDTO(po);
        return ApiResponse.success("Chi tiết phiếu nhập", dto);
    }
    
    private com.doan.WEB_TMDT.module.inventory.dto.PurchaseOrderDetailResponse mapToPurchaseOrderDetailDTO(PurchaseOrder po) {
        // Map supplier
        com.doan.WEB_TMDT.module.inventory.dto.PurchaseOrderDetailResponse.SupplierInfo supplierInfo = null;
        if (po.getSupplier() != null) {
            supplierInfo = com.doan.WEB_TMDT.module.inventory.dto.PurchaseOrderDetailResponse.SupplierInfo.builder()
                    .id(po.getSupplier().getId())
                    .name(po.getSupplier().getName())
                    .taxCode(po.getSupplier().getTaxCode())
                    .phone(po.getSupplier().getPhone())
                    .email(po.getSupplier().getEmail())
                    .address(po.getSupplier().getAddress())
                    .build();
        }
        
        // Map items
        List<com.doan.WEB_TMDT.module.inventory.dto.PurchaseOrderDetailResponse.PurchaseOrderItemInfo> itemInfos = 
                po.getItems().stream().map(item -> {
            // Map warehouse product
            com.doan.WEB_TMDT.module.inventory.dto.PurchaseOrderDetailResponse.WarehouseProductInfo wpInfo = null;
            if (item.getWarehouseProduct() != null) {
                wpInfo = com.doan.WEB_TMDT.module.inventory.dto.PurchaseOrderDetailResponse.WarehouseProductInfo.builder()
                        .id(item.getWarehouseProduct().getId())
                        .sku(item.getWarehouseProduct().getSku())
                        .internalName(item.getWarehouseProduct().getInternalName())
                        .description(item.getWarehouseProduct().getDescription())
                        .techSpecsJson(item.getWarehouseProduct().getTechSpecsJson())
                        .build();
            }
            
            // Map product details (serials)
            List<com.doan.WEB_TMDT.module.inventory.dto.PurchaseOrderDetailResponse.ProductDetailInfo> detailInfos = null;
            if (item.getProductDetails() != null) {
                detailInfos = item.getProductDetails().stream()
                        .map(detail -> com.doan.WEB_TMDT.module.inventory.dto.PurchaseOrderDetailResponse.ProductDetailInfo.builder()
                                .id(detail.getId())
                                .serialNumber(detail.getSerialNumber())
                                .importPrice(detail.getImportPrice())
                                .importDate(detail.getImportDate())
                                .status(detail.getStatus().name())
                                .warrantyMonths(detail.getWarrantyMonths())
                                .build())
                        .toList();
            }
            
            return com.doan.WEB_TMDT.module.inventory.dto.PurchaseOrderDetailResponse.PurchaseOrderItemInfo.builder()
                    .id(item.getId())
                    .sku(item.getSku())
                    .quantity(item.getQuantity().intValue())
                    .unitCost(item.getUnitCost())
                    .warrantyMonths(item.getWarrantyMonths())
                    .note(item.getNote())
                    .warehouseProduct(wpInfo)
                    .productDetails(detailInfos)
                    .build();
        }).toList();
        
        return com.doan.WEB_TMDT.module.inventory.dto.PurchaseOrderDetailResponse.builder()
                .id(po.getId())
                .poCode(po.getPoCode())
                .status(po.getStatus().name())
                .orderDate(po.getOrderDate())
                .receivedDate(po.getReceivedDate())
                .createdBy(po.getCreatedBy())
                .note(po.getNote())
                .supplier(supplierInfo)
                .items(itemInfos)
                .build();
    }

    @Override
    public ApiResponse getExportOrderDetail(Long id) {
        ExportOrder eo = exportOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất #" + id));
        
        // Map to DTO
        com.doan.WEB_TMDT.module.inventory.dto.ExportOrderDetailResponse dto = mapToExportOrderDetailDTO(eo);
        return ApiResponse.success("Chi tiết phiếu xuất", dto);
    }
    
    private com.doan.WEB_TMDT.module.inventory.dto.ExportOrderDetailResponse mapToExportOrderDetailDTO(ExportOrder eo) {
        List<com.doan.WEB_TMDT.module.inventory.dto.ExportOrderDetailResponse.ExportOrderItemInfo> itemInfos = 
                eo.getItems().stream().map(item -> {
            // Map warehouse product
            com.doan.WEB_TMDT.module.inventory.dto.ExportOrderDetailResponse.WarehouseProductInfo wpInfo = null;
            if (item.getWarehouseProduct() != null) {
                wpInfo = com.doan.WEB_TMDT.module.inventory.dto.ExportOrderDetailResponse.WarehouseProductInfo.builder()
                        .id(item.getWarehouseProduct().getId())
                        .sku(item.getWarehouseProduct().getSku())
                        .internalName(item.getWarehouseProduct().getInternalName())
                        .description(item.getWarehouseProduct().getDescription())
                        .techSpecsJson(item.getWarehouseProduct().getTechSpecsJson())
                        .build();
            }
            
            // Parse serial numbers
            List<String> serialNumbers = item.getSerialNumbers() != null 
                    ? List.of(item.getSerialNumbers().split(","))
                    : List.of();
            
            return com.doan.WEB_TMDT.module.inventory.dto.ExportOrderDetailResponse.ExportOrderItemInfo.builder()
                    .id(item.getId())
                    .sku(item.getSku())
                    .quantity(item.getQuantity())
                    .totalCost(item.getTotalCost())
                    .serialNumbers(serialNumbers)
                    .warehouseProduct(wpInfo)
                    .build();
        }).toList();
        
        return com.doan.WEB_TMDT.module.inventory.dto.ExportOrderDetailResponse.builder()
                .id(eo.getId())
                .exportCode(eo.getExportCode())
                .status(eo.getStatus().name())
                .exportDate(eo.getExportDate())
                .createdBy(eo.getCreatedBy())
                .reason(eo.getReason())
                .note(eo.getNote())
                .items(itemInfos)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse cancelPurchaseOrder(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập #" + id));
        
        if (po.getStatus() != POStatus.CREATED) {
            return ApiResponse.error("Chỉ có thể hủy phiếu ở trạng thái chờ xử lý");
        }
        
        po.setStatus(POStatus.CANCELLED);
        purchaseOrderRepository.save(po);
        
        return ApiResponse.success("Đã hủy phiếu nhập thành công", po);
    }

    @Override
    @Transactional
    public ApiResponse cancelExportOrder(Long id) {
        ExportOrder eo = exportOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất #" + id));
        
        if (eo.getStatus() != ExportStatus.CREATED) {
            return ApiResponse.error("Chỉ có thể hủy phiếu ở trạng thái chờ xử lý");
        }
        
        eo.setStatus(ExportStatus.CANCELLED);
        exportOrderRepository.save(eo);
        
        return ApiResponse.success("Đã hủy phiếu xuất thành công", eo);
    }

    @Override
    public ApiResponse getStocks() {
        List<InventoryStock> stocks = inventoryStockRepository.findAll();
        
        // Map to DTO to include warehouse product info
        List<Map<String, Object>> stockData = stocks.stream().map(stock -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", stock.getId());
            data.put("onHand", stock.getOnHand());
            data.put("reserved", stock.getReserved());
            data.put("damaged", stock.getDamaged());
            data.put("sellable", stock.getSellable());
            data.put("available", stock.getAvailable());
            
            if (stock.getWarehouseProduct() != null) {
                WarehouseProduct wp = stock.getWarehouseProduct();
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("id", wp.getId());
                productInfo.put("sku", wp.getSku());
                productInfo.put("internalName", wp.getInternalName());
                productInfo.put("description", wp.getDescription());
                productInfo.put("techSpecsJson", wp.getTechSpecsJson());
                productInfo.put("lastImportDate", wp.getLastImportDate());
                
                if (wp.getSupplier() != null) {
                    Map<String, Object> supplierInfo = new HashMap<>();
                    supplierInfo.put("id", wp.getSupplier().getId());
                    supplierInfo.put("name", wp.getSupplier().getName());
                    supplierInfo.put("taxCode", wp.getSupplier().getTaxCode());
                    productInfo.put("supplier", supplierInfo);
                }
                
                data.put("warehouseProduct", productInfo);
            }
            
            return data;
        }).toList();
        
        return ApiResponse.success("Danh sách tồn kho", stockData);
    }

}