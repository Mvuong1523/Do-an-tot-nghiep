package com.doan.WEB_TMDT.module.inventory.service.impl;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.inventory.dto.*;
import com.doan.WEB_TMDT.module.inventory.entity.*;
import com.doan.WEB_TMDT.module.inventory.repository.*;
import com.doan.WEB_TMDT.module.inventory.service.InventoryService;
import com.doan.WEB_TMDT.module.product.entity.Product;
import com.doan.WEB_TMDT.module.product.entity.ProductDetail;
import com.doan.WEB_TMDT.module.product.entity.ProductStatus;
import com.doan.WEB_TMDT.module.product.repository.ProductDetailRepository;
import com.doan.WEB_TMDT.module.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final ExportOrderRepository exportOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository poItemRepo;
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public  ApiResponse createSupplier(CreateSupplierRequest req){

        // Kiểm tra trùng tên
        if (supplierRepository.existsByName(req.getName())) {
            return ApiResponse.error("Tên nhà cung cấp đã tồn tại trong hệ thống!");
        }

        // Kiểm tra trùng email
        if (req.getEmail() != null && supplierRepository.existsByEmail(req.getEmail())) {
            return ApiResponse.error("Email nhà cung cấp đã tồn tại!");
        }

        // Kiểm tra trùng số điện thoại
        if (req.getPhone() != null && supplierRepository.existsByPhone(req.getPhone())) {
            return ApiResponse.error("Số điện thoại nhà cung cấp đã tồn tại!");
        }

        // Kiểm tra trùng TaxCode
        if (req.getEmail() != null && supplierRepository.existsByTaxCode(req.getEmail())) {
            return ApiResponse.error("Mã số thuếs nhà cung cấp đã tồn tại!");
        }

        // Kiểm tra trùng BankAccount
        if (req.getPhone() != null && supplierRepository.existsByBankAccount(req.getPhone())) {
            return ApiResponse.error("Số tài khoản nhà cung cấp đã tồn tại!");
        }

        Supplier supplier = Supplier.builder()
                .name(req.getName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .address(req.getAddress())
                .taxCode(req.getTaxCode())
                .bankAccount(req.getBankAccount())
                .paymentTerm(req.getPaymentTerm())
                .active(true)
                .build();
        supplierRepository.save(supplier);
        return ApiResponse.success("Tạo nhà cung cấp thành công!", supplier);
    }


    @Override
    public ApiResponse createPurchaseOrder(CreatePORequest req) {
        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(Supplier.builder().id(Long.valueOf(req.getSupplierId())).build())
                .status(POStatus.PENDING)
                .note(req.getNote())
                .orderDate(LocalDateTime.now())
                .build();

        List<Product> supplierProducts = productRepository.findAllBySupplier_Id(Long.valueOf(req.getSupplierId()));

        List<PurchaseOrderItem> items = req.getItems().stream().map(itemReq -> {
            // tìm sản phẩm theo SKU trong danh sách sản phẩm của nhà cung cấp
            Product product = supplierProducts.stream()
                    .filter(p -> p.getSku().equals(itemReq.getProductSku()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Sản phẩm không thuộc nhà cung cấp hoặc không tồn tại: " + itemReq.getProductSku()
                    ));
            return PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .quantity((long) itemReq.getQuantity())
                    .unitCost(itemReq.getUnitPrice())
                    .build();
        }).toList();

        po.setItems(items);
        purchaseOrderRepository.save(po);
        return ApiResponse.success("Tạo phiếu nhập thành công, trạng thái PENDING");
    }

    @Override
    public ApiResponse completePurchaseOrder(CompletePORequest req) {
        //  Tìm phiếu nhập
        PurchaseOrder po = purchaseOrderRepository.findById(req.getPoId())
                .orElseThrow(() -> (RuntimeException)
                        new IllegalArgumentException("Không tìm thấy phiếu nhập hàng #" + req.getPoId()));



        if (po.getStatus() != POStatus.CREATED) {
            return ApiResponse.error("Phiếu nhập không ở trạng thái chờ nhập hàng!");
        }

        //  Cập nhật ngày thực nhập và trạng thái
        po.setReceivedDate(req.getReceivedDate());
        po.setStatus(POStatus.RECEIVED);

        //  Duyệt từng sản phẩm gửi lên (mỗi sản phẩm có danh sách serial riêng)
        for (ProductSerialRequest serialReq : req.getSerials()) {

            // Tìm item trong phiếu nhập (theo productSku)
            PurchaseOrderItem item = po.getItems().stream()
                    .filter(i -> i.getProduct().getSku().equals(serialReq.getProductSku()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Sản phẩm SKU " + serialReq.getProductSku() + " không thuộc phiếu nhập #" + po.getId()
                    ));

            Product product = item.getProduct();

            // Kiểm tra số lượng serial nhập có khớp quantity không
            if (serialReq.getSerialNumbers().size() != item.getQuantity()) {
                throw new RuntimeException("Số lượng serial (" + serialReq.getSerialNumbers().size()
                        + ") không khớp số lượng phiếu nhập (" + item.getQuantity()
                        + ") cho sản phẩm: " + product.getName());
            }

            // Kiểm tra trùng serial trong DB (đảm bảo serial duy nhất)
            for (String sn : serialReq.getSerialNumbers()) {
                if (productDetailRepository.existsBySerialNumber(sn)) {
                    throw new RuntimeException("Serial " + sn + " đã tồn tại trong hệ thống!");
                }
            }

            // Tạo danh sách ProductDetail (serial cụ thể)
            List<ProductDetail> details = serialReq.getSerialNumbers().stream()
                    .map(sn -> ProductDetail.builder()
                            .serialNumber(sn)
                            .product(product)
                            .poItem(item)
                            .status(ProductStatus.IN_STOCK)
                            .build())
                    .toList();

            // Gán vào item và lưu
            item.setProductDetails(details);

            // Cập nhật tồn kho (số lượng = số serial nhập)
            InventoryStock stock = inventoryStockRepository.findByProductId(product.getId())
                    .orElse(InventoryStock.builder()
                            .product(product)
                            .onHand(0L)
                            .reserved(0L)
                            .damaged(0L)
                            .build());

//  Khi nhập hàng về, toàn bộ hàng mới nhập đều ở trạng thái "bán được" (chưa hư, chưa giữ chỗ)
            stock.setOnHand(stock.getOnHand() + details.size());

//  Lưu lại thay đổi
            inventoryStockRepository.save(stock);
        }

        //  Lưu lại phiếu nhập
        purchaseOrderRepository.save(po);

        return ApiResponse.success("Hoàn tất nhập hàng thành công", po.getId());
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
                    .unitCost(product.getImportPrice()) // nếu bạn có field này
                    .build();

            exportItems.add(item);
        }

        // 6️⃣ Lưu phiếu xuất và các dòng chi tiết
        exportOrder.setItems(exportItems);
        exportOrderRepository.save(exportOrder);

        return ApiResponse.success("Xuất kho thành công!", exportOrder.getExportCode());
    }



}
