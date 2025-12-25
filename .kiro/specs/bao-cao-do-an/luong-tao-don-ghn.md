# Luồng Tạo Đơn Giao Hàng Nhanh (GHN)

## Tổng Quan

Đơn GHN được tạo **KHI XUẤT KHO**, không phải khi đặt hàng. Luồng: Đặt hàng → Xác nhận → Chuẩn bị hàng → **Xuất kho** → Tạo đơn GHN → Giao hàng.

---

## LUỒNG CHI TIẾT

### Bước 1: Nhân Viên Kho Xuất Kho

**Frontend**: Nhân viên kho vào trang quản lý đơn hàng → Click "Xuất kho"

**API Call**: `POST /api/inventory/export-orders/sales`

**Body**:
```json
{
  "orderId": 123,
  "warehouseId": 1,
  "note": "Xuất kho cho đơn ORD20231223XXXX",
  "items": [
    {
      "productId": 456,
      "quantity": 2,
      "serialNumbers": ["SN001", "SN002"]
    }
  ]
}
```

---

### Bước 2: InventoryController Nhận Request

**File**: `InventoryController.java`

**Method**: `createSalesExportOrder()`

**Code**:
```java
@PostMapping("/export-orders/sales")
@PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
public ApiResponse createSalesExportOrder(@RequestBody CreateSalesExportRequest request) {
    return inventoryService.createSalesExportOrder(request);
}
```

---

### Bước 3: InventoryService Xử Lý Xuất Kho

**File**: `InventoryServiceImpl.java`

**Method**: `createSalesExportOrder()`

**Annotation**: `@Transactional`

#### 3.1. Validate Order
```java
Order order = orderRepository.findById(request.getOrderId())
    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

if (order.getStatus() != OrderStatus.PREPARING) {
    return ApiResponse.error("Đơn hàng chưa sẵn sàng xuất kho");
}
```

#### 3.2. Create Export Order
```java
ExportOrder exportOrder = ExportOrder.builder()
    .exportCode(generateExportCode())
    .warehouse(warehouse)
    .order(order)
    .type(ExportType.SALES)
    .status(ExportStatus.COMPLETED)
    .note(request.getNote())
    .build();

exportOrderRepository.save(exportOrder);
```

**SQL**:
```sql
INSERT INTO export_orders (export_code, warehouse_id, order_id, type, status, note, created_at)
VALUES ('EXP20231223XXXX', 1, 123, 'SALES', 'COMPLETED', 'Xuất kho...', NOW());
```

#### 3.3. Create Export Items & Update Stock
```java
for (ExportItemRequest itemReq : request.getItems()) {
    // Create export item
    ExportItem exportItem = ExportItem.builder()
        .exportOrder(exportOrder)
        .product(product)
        .quantity(itemReq.getQuantity())
        .serialNumbers(String.join(",", itemReq.getSerialNumbers()))
        .build();
    
    exportItemRepository.save(exportItem);
    
    // Update stock: onHand giảm, reserved giảm
    InventoryStock stock = inventoryStockRepository.findByProductId(product.getId());
    stock.setOnHand(stock.getOnHand() - itemReq.getQuantity());
    stock.setReserved(stock.getReserved() - itemReq.getQuantity());
    inventoryStockRepository.save(stock);
}
```

**SQL**:
```sql
-- Insert export items
INSERT INTO export_items (export_order_id, product_id, quantity, serial_numbers)
VALUES (789, 456, 2, 'SN001,SN002');

-- Update stock
UPDATE inventory_stock 
SET on_hand = on_hand - 2,
    reserved = reserved - 2
WHERE product_id = 456;
```

#### 3.4. Gọi Method Tạo Đơn GHN
```java
try {
    createGHNOrderForExport(order.getId(), exportOrder);
} catch (Exception e) {
    log.error("Failed to create GHN order: {}", e.getMessage());
    // Không fail export, admin có thể tạo GHN sau
}
```

---

### Bước 4: InventoryService Tạo Đơn GHN

**Method**: `createGHNOrderForExport(Long orderId, ExportOrder exportOrder)`

#### 4.1. Get Order Details
```java
Order order = orderRepository.findById(orderId)
    .orElseThrow(() -> new RuntimeException("Order not found"));
```

**SQL**:
```sql
SELECT o.*, c.*, oi.* 
FROM orders o
JOIN customers c ON o.customer_id = c.id
LEFT JOIN order_items oi ON o.id = oi.order_id
WHERE o.id = 123;
```

#### 4.2. Check if Already Has GHN Order
```java
if (order.getGhnOrderCode() != null && !order.getGhnOrderCode().isEmpty()) {
    log.warn("Order already has GHN order code: {}", order.getGhnOrderCode());
    return;
}
```

#### 4.3. Check if Need GHN Shipping
```java
// Nếu miễn phí ship hoặc nội thành Hà Nội → Không cần GHN
if (order.getShippingFee() == 0 || 
    shippingService.isHanoiInnerCity(order.getProvince(), order.getDistrict())) {
    
    log.info("Order uses internal shipping (no GHN)");
    
    // Update status to READY_TO_SHIP
    order.setStatus(OrderStatus.READY_TO_SHIP);
    order.setShippedAt(LocalDateTime.now());
    orderRepository.save(order);
    
    return;
}
```

**SQL** (nếu không cần GHN):
```sql
UPDATE orders 
SET status = 'READY_TO_SHIP',
    shipped_at = NOW()
WHERE id = 123;
```

#### 4.4. Build Full Address
```java
String wardDisplay = (order.getWardName() != null && !order.getWardName().isEmpty()) 
    ? order.getWardName() 
    : order.getWard();

String fullAddress = String.join(", ", 
    order.getAddress(),
    wardDisplay,
    order.getDistrict(),
    order.getProvince()
);
// Example: "123 Nguyễn Trãi, Phường Thanh Xuân Trung, Thanh Xuân, Hà Nội"
```

#### 4.5. Build GHN Order Request
```java
CreateGHNOrderRequest ghnRequest = CreateGHNOrderRequest.builder()
    .toName(order.getCustomer().getFullName())
    .toPhone(order.getCustomer().getPhone())
    .toAddress(fullAddress)
    .toWardCode(order.getWard())  // Ward code (e.g., "26734")
    .toDistrictId(getDistrictIdForGHN(order.getProvince(), order.getDistrict()))
    .note(order.getNote())
    .codAmount("COD".equals(order.getPaymentMethod()) ? order.getTotal().intValue() : 0)
    .weight(1000)  // 1kg default
    .length(20)
    .width(20)
    .height(10)
    .serviceTypeId(2)  // Standard service
    .paymentTypeId("COD".equals(order.getPaymentMethod()) ? 2 : 1)
    .items(buildGHNItemsFromOrder(order))
    .build();
```

**Helper method**: `buildGHNItemsFromOrder()`
```java
private List<GHNOrderItem> buildGHNItemsFromOrder(Order order) {
    List<GHNOrderItem> items = new ArrayList<>();
    
    for (OrderItem item : order.getItems()) {
        items.add(GHNOrderItem.builder()
            .name(item.getProductName())
            .code(item.getProduct().getSku())
            .quantity(item.getQuantity())
            .price(item.getPrice().intValue())
            .build());
    }
    
    return items;
}
```

#### 4.6. Call ShippingService
```java
CreateGHNOrderResponse ghnResponse = shippingService.createGHNOrder(ghnRequest);
```

---

### Bước 5: ShippingService Gọi GHN API

**File**: `ShippingServiceImpl.java`

**Method**: `createGHNOrder(CreateGHNOrderRequest request)`

#### 5.1. Setup Headers
```java
String url = ghnApiUrl + "/v2/shipping-order/create";

HttpHeaders headers = new HttpHeaders();
headers.set("Token", ghnApiToken);
headers.set("ShopId", ghnShopId.toString());
headers.setContentType(MediaType.APPLICATION_JSON);
```

**Config** (từ `application.properties`):
```properties
ghn.api.url=https://dev-online-gateway.ghn.vn
ghn.api.token=your_ghn_token
ghn.shop.id=123456
```

#### 5.2. Build Request Body
```java
Map<String, Object> body = new HashMap<>();
body.put("to_name", request.getToName());
body.put("to_phone", request.getToPhone());
body.put("to_address", request.getToAddress());
body.put("to_ward_code", request.getToWardCode());
body.put("to_district_id", request.getToDistrictId());
body.put("note", request.getNote());
body.put("required_note", "KHONGCHOXEMHANG");
body.put("cod_amount", request.getCodAmount());
body.put("weight", request.getWeight());
body.put("length", request.getLength());
body.put("width", request.getWidth());
body.put("height", request.getHeight());
body.put("service_type_id", request.getServiceTypeId());
body.put("payment_type_id", request.getPaymentTypeId());

// Add items
List<Map<String, Object>> items = new ArrayList<>();
for (GHNOrderItem item : request.getItems()) {
    Map<String, Object> itemMap = new HashMap<>();
    itemMap.put("name", item.getName());
    itemMap.put("code", item.getCode());
    itemMap.put("quantity", item.getQuantity());
    itemMap.put("price", item.getPrice());
    items.add(itemMap);
}
body.put("items", items);
```

#### 5.3. Call GHN API
```java
HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
```

**GHN API Request**:
```
POST https://dev-online-gateway.ghn.vn/v2/shipping-order/create
Headers:
  Token: your_ghn_token
  ShopId: 123456
  Content-Type: application/json

Body:
{
  "to_name": "Nguyễn Văn A",
  "to_phone": "0912345678",
  "to_address": "123 Nguyễn Trãi, Phường Thanh Xuân Trung, Thanh Xuân, Hà Nội",
  "to_ward_code": "26734",
  "to_district_id": 1485,
  "note": "Giao giờ hành chính",
  "required_note": "KHONGCHOXEMHANG",
  "cod_amount": 60030000,
  "weight": 1000,
  "length": 20,
  "width": 20,
  "height": 10,
  "service_type_id": 2,
  "payment_type_id": 2,
  "items": [
    {
      "name": "iPhone 15 Pro Max",
      "code": "IP15PM",
      "quantity": 2,
      "price": 30000000
    }
  ]
}
```

#### 5.4. Parse GHN Response
```java
if (response != null && response.get("code").equals(200)) {
    Map<String, Object> data = (Map<String, Object>) response.get("data");
    
    String orderCode = data.get("order_code").toString();
    String sortCode = data.get("sort_code") != null ? data.get("sort_code").toString() : null;
    Double totalFee = data.get("total_fee") != null ? 
        ((Number) data.get("total_fee")).doubleValue() : null;
    
    // Parse expected_delivery_time
    LocalDateTime expectedDeliveryTime = null;
    Object timeValue = data.get("expected_delivery_time");
    if (timeValue instanceof Number) {
        long timestamp = ((Number) timeValue).longValue();
        expectedDeliveryTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp), 
            ZoneId.systemDefault()
        );
    }
    
    return CreateGHNOrderResponse.builder()
        .orderCode(orderCode)
        .status("created")
        .expectedDeliveryTime(expectedDeliveryTime)
        .sortCode(sortCode)
        .totalFee(totalFee)
        .build();
}
```

**GHN API Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "order_code": "GHN123456",
    "sort_code": "HN-01-A",
    "total_fee": 30000,
    "expected_delivery_time": 1703404800
  }
}
```

---

### Bước 6: InventoryService Cập Nhật Order

**Quay lại**: `createGHNOrderForExport()`

**Code**:
```java
// Update order with GHN info
order.setGhnOrderCode(ghnResponse.getOrderCode());
order.setGhnShippingStatus("created");
order.setGhnCreatedAt(LocalDateTime.now());
order.setGhnExpectedDeliveryTime(ghnResponse.getExpectedDeliveryTime());

// Update order status to READY_TO_SHIP
order.setStatus(OrderStatus.READY_TO_SHIP);
order.setShippedAt(LocalDateTime.now());

orderRepository.save(order);
```

**SQL**:
```sql
UPDATE orders 
SET ghn_order_code = 'GHN123456',
    ghn_shipping_status = 'created',
    ghn_created_at = NOW(),
    ghn_expected_delivery_time = '2023-12-25 10:00:00',
    status = 'READY_TO_SHIP',
    shipped_at = NOW()
WHERE id = 123;
```

---

### Bước 7: Return Response

**InventoryService** trả về:
```java
return ApiResponse.success("Xuất kho bán hàng thành công", exportOrder.getExportCode());
```

**Response**:
```json
{
  "success": true,
  "message": "Xuất kho bán hàng thành công",
  "data": "EXP20231223XXXX"
}
```

---

## TÓM TẮT DATABASE CHANGES

### Sau khi xuất kho:
```sql
-- export_orders table
INSERT INTO export_orders (export_code, warehouse_id, order_id, type, status, ...)
VALUES ('EXP20231223XXXX', 1, 123, 'SALES', 'COMPLETED', ...);

-- export_items table
INSERT INTO export_items (export_order_id, product_id, quantity, serial_numbers)
VALUES (789, 456, 2, 'SN001,SN002');

-- inventory_stock table
UPDATE inventory_stock 
SET on_hand = on_hand - 2,
    reserved = reserved - 2
WHERE product_id = 456;
```

### Sau khi tạo đơn GHN:
```sql
-- orders table
UPDATE orders 
SET ghn_order_code = 'GHN123456',
    ghn_shipping_status = 'created',
    ghn_created_at = NOW(),
    ghn_expected_delivery_time = '2023-12-25 10:00:00',
    status = 'READY_TO_SHIP',
    shipped_at = NOW()
WHERE id = 123;
```

---

## FLOW NGẮN GỌN

1. **Xuất kho** → `InventoryController.createSalesExportOrder()` → `InventoryService.createSalesExportOrder()`

2. **Tạo export order** → Lưu `export_orders`, `export_items` → Update `inventory_stock`

3. **Gọi tạo GHN** → `createGHNOrderForExport()`

4. **Check điều kiện** → Nếu miễn phí ship/nội thành → Update status = READY_TO_SHIP → Return

5. **Build GHN request** → `CreateGHNOrderRequest` với thông tin khách hàng, địa chỉ, sản phẩm

6. **Call ShippingService** → `shippingService.createGHNOrder()`

7. **Call GHN API** → `POST https://dev-online-gateway.ghn.vn/v2/shipping-order/create`

8. **Parse response** → Lấy `order_code`, `expected_delivery_time`

9. **Update order** → Lưu `ghn_order_code`, status = READY_TO_SHIP

10. **Return** → Trả về export code

---

## KEY POINTS

- **Thời điểm tạo**: Khi xuất kho, không phải khi đặt hàng
- **Điều kiện**: Chỉ tạo GHN nếu có phí ship và không phải nội thành
- **GHN API**: Gọi REST API của GHN với token và shop_id
- **Status flow**: PREPARING → (Xuất kho) → READY_TO_SHIP → (Tài xế lấy hàng) → SHIPPING → DELIVERED
- **Rollback**: Nếu GHN API fail, export vẫn thành công, admin có thể tạo GHN sau

---

**Document Version**: 1.0  
**Last Updated**: 2024-12-25
