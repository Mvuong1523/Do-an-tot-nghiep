package com.doan.WEB_TMDT.module.inventory.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.dto.*;
import com.doan.WEB_TMDT.module.inventory.entity.*;
import com.doan.WEB_TMDT.module.inventory.repository.*;
import com.doan.WEB_TMDT.module.inventory.service.InventoryService;
import com.doan.WEB_TMDT.module.inventory.entity.ProductDetail;
import com.doan.WEB_TMDT.module.inventory.entity.ProductStatus;
import com.doan.WEB_TMDT.module.product.repository.ProductDetailRepository;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle.detail;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final ExportOrderRepository exportOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository poItemRepo;
    private final WarehouseProductRepository warehouseProductRepository;
    private final ProductDetailRepository productDetailRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final SupplierRepository supplierRepository;
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
        Supplier supplier;

        // 1️⃣ Nếu có supplierId → lấy NCC có sẵn
        if (req.getSupplierId() != null) {
            supplier = supplierRepository.findById(Long.valueOf(req.getSupplierId()))
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp ID: " + req.getSupplierId()));
        }
        // 2️⃣ Nếu không có supplierId → kiểm tra / tạo mới NCC
        else if (req.getSupplier() != null) {
            CreateSupplierRequest sreq = req.getSupplier();

            supplier = supplierRepository.findByTaxCode(sreq.getTaxCode())
                    .or(() -> supplierRepository.findByEmail(sreq.getEmail()))
                    .or(() -> supplierRepository.findByPhone(sreq.getPhone()))
                    .orElseGet(() -> supplierRepository.save(
                            Supplier.builder()
                                    .name(sreq.getName())
                                    .taxCode(sreq.getTaxCode())
                                    .email(sreq.getEmail())
                                    .phone(sreq.getPhone())
                                    .address(sreq.getAddress())
                                    .bankAccount(sreq.getBankAccount())
                                    .paymentTerm(sreq.getPaymentTerm())
                                    .active(true)
                                    .autoCreated(true)
                                    .build()
                    ));
        }
        else {
            throw new IllegalArgumentException("Cần chọn nhà cung cấp hoặc nhập thông tin nhà cung cấp mới.");
        }

        // 3️⃣ Tạo phiếu nhập hàng
        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(supplier)
                .status(POStatus.CREATED)
                .orderDate(LocalDateTime.now())
                .createdBy(req.getCreatedBy())
                .note(req.getNote())
                .build();

        // 4️⃣ Gắn sản phẩm
        List<PurchaseOrderItem> items = req.getItems().stream().map(i -> {
            WarehouseProduct wp = warehouseProductRepository.findBySku(i.getSku())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy SKU: " + i.getSku()));

            return PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .warehouseProduct(wp)
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
                    .filter(i -> i.getWarehouseProduct().getSku().equals(sku))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Sản phẩm SKU " + sku + " không thuộc phiếu nhập #" + po.getId()));

            // Kiểm tra số lượng serial có khớp số lượng đặt
            if (serialReq.getSerialNumbers().size() != item.getQuantity()) {
                throw new RuntimeException("Số serial (" + serialReq.getSerialNumbers().size() +
                        ") không khớp với số lượng nhập (" + item.getQuantity() + ") cho SKU: " + sku);
            }

            // 3️⃣ Kiểm tra trùng serial
            for (String sn : serialReq.getSerialNumbers()) {
                if (productDetailRepository.existsBySerialNumber(sn)) {
                    throw new RuntimeException("Serial " + sn + " đã tồn tại trong hệ thống!");
                }
            }

            // 4️⃣ Tạo danh sách ProductDetail (serial cụ thể)
            List<ProductDetail> details = serialReq.getSerialNumbers().stream()
                    .map(sn -> ProductDetail.builder()
                            .serialNumber(sn)
                            .importPrice(item.getUnitCost())
                            .importDate(LocalDateTime.now())
                            .warrantyMonths(item.getWarrantyMonths())
                            .status(ProductStatus.IN_STOCK)
                            .warehouseProduct(item.getWarehouseProduct())
                            .purchaseOrderItem(item)
                            .build())
                    .toList();

            // Gắn vào item và lưu
            item.getProductDetails().addAll(details);

            // 5️⃣ Cập nhật tồn kho
            WarehouseProduct wp = item.getWarehouseProduct();

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

        // 6️⃣ Cập nhật phiếu nhập
        po.setReceivedDate(req.getReceivedDate());
        po.setStatus(POStatus.RECEIVED);
        purchaseOrderRepository.save(po);

        return ApiResponse.success("Hoàn tất nhập hàng thành công!", po.getId());
    }

    @Override
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

        // 2️⃣ Duyệt từng sản phẩm cần xuất
        WarehouseProduct product = null;
        int exportCount = 0;
        double totalCost = 0;
        for (ExportItemRequest itemReq : req.getItems()) {
            product = warehouseProductRepository.findBySku(itemReq.getProductSku())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm SKU: " + itemReq.getProductSku()));

            exportCount = itemReq.getSerialNumbers().size();

            // 3️⃣ Kiểm tra tồn kho
            InventoryStock stock = inventoryStockRepository.findByWarehouseProduct_Id(product.getId())
                    .orElseThrow(() -> new RuntimeException("Không có dữ liệu tồn kho cho sản phẩm: " + product.getSku()));

            if (stock.getOnHand() < exportCount) {
                throw new RuntimeException("Không đủ hàng trong kho. Sẵn có: " + stock.getOnHand() +
                        ", yêu cầu xuất: " + exportCount + " (" + product.getSku() + ")");
            }

            totalCost = 0;

            // 4️⃣ Xử lý từng serial
            for (String serial : itemReq.getSerialNumbers()) {
                ProductDetail detail = productDetailRepository.findBySerialNumber(serial)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy serial: " + serial));

                if (detail.getStatus() != ProductStatus.IN_STOCK) {
                    throw new RuntimeException("Serial " + serial + " không ở trạng thái IN_STOCK, không thể xuất kho!");
                }

                detail.setStatus(ProductStatus.SOLD);
                detail.setSoldDate(LocalDateTime.now());
                productDetailRepository.save(detail);
                totalCost += detail.getImportPrice();

            }

            // 5️⃣ Cập nhật tồn kho
            stock.setOnHand(stock.getOnHand() - exportCount);
            inventoryStockRepository.save(stock);

        }

        // 6️⃣ Ghi chi tiết phiếu xuất
        ExportOrderItem item = ExportOrderItem.builder()
                .exportOrder(exportOrder)
                .warehouseProduct(product)
                .quantity((long) exportCount)
                .serialNumbers(String.join(",", itemReq.getSerialNumbers()))
                .totalCost(totalCost)
                .build();
        exportItems.add(item);


        // 7️⃣ Lưu phiếu xuất
        exportOrder.setItems(exportItems);
        exportOrder.setStatus(ExportStatus.COMPLETED);
        exportOrderRepository.save(exportOrder);

        return ApiResponse.success("Xuất kho thành công!", exportOrder.getExportCode());
    }

    @Override
    public ApiResponse exportInventory(ExportInventoryRequest req) {

        //  Tạo phiếu xuất kho mới
        ExportOrder exportOrder = ExportOrder.builder()
                .exportCode("PX" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                        + "-" + String.format("%03d", new Random().nextInt(999)))
                .exportDate(LocalDateTime.now())
                .createdBy(req.getCreatedBy())     // nếu có
                .reason(req.getReason())
                .note(req.getNote())
                .build();

        List<ExportOrderItem> exportItems = new ArrayList<>();

        //  Duyệt từng sản phẩm cần xuất
        for (ExportItemRequest itemReq : req.getItems()) {

            Product product = productRepository.findBySku(itemReq.getProductSku())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm SKU: " + itemReq.getProductSku()));

            // Kiểm tra tồn kho
            InventoryStock stock = inventoryStockRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new RuntimeException("Không có dữ liệu tồn kho cho sản phẩm: " + product.getName()));

            int exportCount = itemReq.getSerialNumbers().size();
            if (stock.getOnHand() < exportCount) {
                throw new RuntimeException("Không đủ hàng trong kho. Sẵn có: " + stock.getOnHand() +
                        ", yêu cầu xuất: " + exportCount + " (" + product.getName() + ")");
            }

            //  Kiểm tra và cập nhật serial của từng sản phẩm
            for (String serial : itemReq.getSerialNumbers()) {
                ProductDetail detail = productDetailRepository.findBySerialNumber(serial)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy serial: " + serial));

                if (!detail.getStatus().equals(ProductStatus.IN_STOCK)) {
                    throw new RuntimeException("Serial " + serial + " không ở trạng thái IN_STOCK, không thể xuất kho!");
                }

                // Cập nhật trạng thái serial
                detail.setStatus(ProductStatus.SOLD);
                productDetailRepository.save(detail);
            }

            //  Cập nhật tồn kho
            stock.setOnHand(stock.getOnHand() - exportCount);
            inventoryStockRepository.save(stock);

            // 5️⃣ Ghi chi tiết phiếu xuất
            ExportOrderItem item = ExportOrderItem.builder()
                    .exportOrder(exportOrder)
                    .product(product)
                    .sku(product.getSku())
                    .quantity((long) exportCount)
                    .serialNumbers(String.join(",", itemReq.getSerialNumbers()))
                    .build();

            exportItems.add(item);
        }

        // 6️⃣ Lưu phiếu xuất và các dòng chi tiết
        exportOrder.setItems(exportItems);
        exportOrderRepository.save(exportOrder);

        return ApiResponse.success("Xuất kho thành công!", exportOrder.getExportCode());
    }



}
