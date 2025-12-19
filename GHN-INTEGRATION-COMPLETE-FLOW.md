# Luồng tích hợp GHN hoàn chỉnh

## Tổng quan

Hệ thống tích hợp với Giao Hàng Nhanh (GHN) để tự động tạo đơn vận chuyển khi xuất kho.

## Luồng nghiệp vụ chi tiết

### Phase 1: Khách đặt hàng (Frontend + Backend)

#### 1.1. Khách chọn địa chỉ giao hàng
**Frontend:** `/checkout`

- Chọn Tỉnh/Thành phố → API: `GET /api/shipping/provinces`
- Chọn Quận/Huyện → API: `GET /api/shipping/districts/{provinceId}`
- Chọn Phường/Xã → API: `GET /api/shipping/wards/{districtId}`
- Nhập địa chỉ cụ thể (số nhà, tên đường)

**Dữ liệu gửi lên:**
```json
{
  "province": "Hà Nội",
  "district": "Cầu Giấy", 
  "ward": "20308",  // Ward code from GHN
  "address": "123 Đường Trần Đại Nghĩa"
}
```

#### 1.2. Tính phí vận chuyển
**API:** `POST /api/shipping/calculate-fee`

```json
{
  "province": "Hà Nội",
  "district": "Cầu Giấy",
  "weight": 1000,
  "value": 30000000
}
```

**Response:**
```json
{
  "fee": 30000,
  "shipMethod": "GHN",
  "estimatedTime": "2-3 ngày",
  "isFreeShip": false
}
```

**Logic:**
- Nội thành HN (12 quận) → Free ship, shipper nội bộ
- Ngoài nội thành → Gọi GHN API tính phí

#### 1.3. Tạo đơn hàng
**API:** `POST /api/orders`

```json
{
  "province": "Hà Nội",
  "district": "Cầu Giấy",
  "ward": "20308",
  "address": "123 Đường Trần Đại Nghĩa",
  "note": "Gọi trước khi giao",
  "shippingFee": 30000,
  "paymentMethod": "COD"
}
```

**Backend xử lý:**
```java
// 1. Tạo Order
Order order = Order.builder()
    .orderCode(generateOrderCode())
    .province(request.getProvince())
    .district(request.getDistrict())
    .ward(request.getWard())  // ✅ Lưu ward code
    .address(request.getAddress())
    .shippingFee(request.getShippingFee())
    .status(OrderStatus.CONFIRMED)  // COD → CONFIRMED ngay
    .build();

// 2. Giảm reservedQuantity (giữ hàng)
product.setReservedQuantity(
    product.getReservedQuantity() + quantity
);

// 3. KHÔNG gọi GHN ở đây
// GHN sẽ được gọi khi xuất kho
```

**Kết quả:**
- ✅ Order được tạo với status `CONFIRMED`
- ✅ `reservedQuantity` giảm (hàng được giữ)
- ✅ `onHand` CHƯA giảm (chưa xuất kho)
- ❌ CHƯA có `ghnOrderCode` (chưa gọi GHN)

---

### Phase 2: Quản lý kho xem đơn cần xuất

#### 2.1. Xem danh sách đơn cần xuất
**API:** `GET /api/inventory/orders/pending-export`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "orderId": 123,
      "orderCode": "ORD20231119001",
      "customerName": "Nguyễn Văn A",
      "customerPhone": "0123456789",
      "province": "Hà Nội",
      "district": "Cầu Giấy",
      "ward": "20308",
      "address": "123 Đường Trần Đại Nghĩa",
      "shippingAddress": "123 Đường Trần Đại Nghĩa, Phường Dịch Vọng, Cầu Giấy, Hà Nội",
      "items": [
        {
          "productName": "iPhone 15 Pro Max",
          "sku": "IP15PM-256-BLK",
          "quantity": 1
        }
      ],
      "total": 30000000,
      "shippingFee": 30000,
      "status": "CONFIRMED",
      "createdAt": "2023-11-19T10:30:00"
    }
  ]
}
```

#### 2.2. Xem chi tiết đơn hàng
**API:** `GET /api/inventory/orders/{orderId}`

---

### Phase 3: Xuất kho và tạo đơn GHN

#### 3.1. Quản lý kho tạo phiếu xuất
**API:** `POST /api/inventory/export-for-sale`

```json
{
  "orderId": 123,
  "reason": "Xuất kho bán hàng - Giao cho khách",
  "note": "Kiểm tra kỹ trước khi giao",
  "items": [
    {
      "productSku": "IP15PM-256-BLK",
      "serialNumbers": [
        "IP15PM256BLK001"
      ]
    }
  ]
}
```

#### 3.2. Backend xử lý (InventoryServiceImpl.exportForSale)

**Bước 1: Kiểm tra và cập nhật tồn kho**
```java
// 1. Kiểm tra tồn kho
if (stock.getOnHand() < exportCount) {
    throw new RuntimeException("Không đủ tồn kho");
}

// 2. Kiểm tra serial
for (String serial : serialNumbers) {
    ProductDetail pd = findBySerialNumber(serial);
    
    if (pd.getStatus() != ProductStatus.IN_STOCK) {
        throw new RuntimeException("Serial không ở trạng thái IN_STOCK");
    }
    
    // 3. Cập nhật trạng thái serial
    pd.setStatus(ProductStatus.SOLD);
    pd.setSoldDate(LocalDateTime.now());
    save(pd);
}

// 4. Giảm tồn kho thực tế
stock.setOnHand(stock.getOnHand() - exportCount);
save(stock);
```

**Bước 2: Tạo phiếu xuất kho**
```java
ExportOrder exportOrder = ExportOrder.builder()
    .exportCode("EX-SALE-" + System.currentTimeMillis())
    .status(ExportStatus.COMPLETED)
    .reason("SALE")
    .orderId(request.getOrderId())
    .createdBy(request.getCreatedBy())
    .exportDate(LocalDateTime.now())
    .items(exportItems)
    .build();

save(exportOrder);
```

**Bước 3: Tự động gọi GHN API**
```java
// Lấy thông tin Order
Order order = orderRepository.findById(orderId);

// Kiểm tra điều kiện gọi GHN
if (order.getShippingFee() == 0 || isHanoiInnerCity()) {
    log.info("Free ship, skip GHN");
    return;
}

// Build GHN request
CreateGHNOrderRequest ghnRequest = CreateGHNOrderRequest.builder()
    .toName(order.getCustomer().getFullName())
    .toPhone(order.getCustomer().getPhone())
    .toAddress(order.getShippingAddress())
    .toWardCode(order.getWard())  // ✅ Ward code từ Order
    .toDistrictId(getDistrictId(order.getProvince(), order.getDistrict()))
    .note(order.getNote())
    .codAmount(order.getPaymentMethod().equals("COD") ? order.getTotal() : 0)
    .weight(1000)
    .serviceTypeId(2)
    .paymentTypeId(order.getPaymentMethod().equals("COD") ? 2 : 1)
    .items(buildGHNItems(order))
    .build();

// Gọi GHN API
CreateGHNOrderResponse ghnResponse = shippingService.createGHNOrder(ghnRequest);

// Cập nhật Order
order.setGhnOrderCode(ghnResponse.getOrderCode());
order.setGhnShippingStatus("created");
order.setGhnCreatedAt(LocalDateTime.now());
order.setGhnExpectedDeliveryTime(ghnResponse.getExpectedDeliveryTime());
save(order);
```

**Response:**
```json
{
  "success": true,
  "message": "Xuất kho bán hàng thành công",
  "data": "EX-SALE-1700123456789"
}
```

**Kết quả:**
- ✅ Serial đổi status: `IN_STOCK` → `SOLD`
- ✅ `onHand` giảm (tồn kho thực tế giảm)
- ✅ Phiếu xuất kho được tạo
- ✅ Đơn GHN được tạo tự động
- ✅ `ghnOrderCode` được cập nhật vào Order

---

### Phase 4: Tracking và theo dõi

#### 4.1. Xem thông tin GHN
**API:** `GET /api/orders/{orderId}`

```json
{
  "orderId": 123,
  "orderCode": "ORD20231119001",
  "ghnOrderCode": "GHNABCD123",  // ✅ Có mã GHN
  "ghnShippingStatus": "created",
  "ghnCreatedAt": "2023-11-19T11:00:00",
  "ghnExpectedDeliveryTime": "2023-11-21T17:00:00"
}
```

#### 4.2. Tracking GHN
**API:** `GET /api/orders/{orderId}/ghn-tracking`

```json
{
  "orderCode": "GHNABCD123",
  "status": "delivering",
  "statusText": "Đang giao hàng",
  "expectedDeliveryTime": "2023-11-21T17:00:00",
  "currentWarehouse": "Kho Hà Nội",
  "logs": [
    {
      "status": "picked",
      "statusText": "Đã lấy hàng",
      "time": "2023-11-19T14:00:00",
      "location": "Kho Cầu Giấy"
    },
    {
      "status": "transporting",
      "statusText": "Đang luân chuyển",
      "time": "2023-11-20T08:00:00",
      "location": "Trung tâm phân loại HN"
    }
  ]
}
```

---

## Sơ đồ luồng dữ liệu

```
┌─────────────────┐
│   KHÁCH HÀNG    │
└────────┬────────┘
         │
         │ 1. Chọn địa chỉ (Tỉnh/Quận/Phường)
         │    GET /api/shipping/provinces
         │    GET /api/shipping/districts/{id}
         │    GET /api/shipping/wards/{id}
         │
         │ 2. Tính phí ship
         │    POST /api/shipping/calculate-fee
         │
         │ 3. Đặt hàng
         │    POST /api/orders
         ▼
┌─────────────────┐
│  ORDER CREATED  │
│  - Status: CONFIRMED
│  - reservedQty ↓
│  - onHand: không đổi
│  - ghnOrderCode: null
└────────┬────────┘
         │
         │ 4. Quản lý kho xem đơn
         │    GET /api/inventory/orders/pending-export
         ▼
┌─────────────────┐
│  QUẢN LÝ KHO    │
└────────┬────────┘
         │
         │ 5. Tạo phiếu xuất + nhập serial
         │    POST /api/inventory/export-for-sale
         ▼
┌─────────────────────────────────┐
│  BACKEND XỬ LÝ TỰ ĐỘNG          │
│  1. Cập nhật serial: SOLD        │
│  2. Giảm onHand                  │
│  3. Tạo ExportOrder              │
│  4. ✅ GỌI GHN API               │
│  5. Cập nhật ghnOrderCode        │
└────────┬────────────────────────┘
         │
         │ 6. Tracking
         │    GET /api/orders/{id}/ghn-tracking
         ▼
┌─────────────────┐
│   GHN SYSTEM    │
│  - Lấy hàng     │
│  - Vận chuyển   │
│  - Giao hàng    │
└─────────────────┘
```

## Các trường hợp đặc biệt

### 1. Free ship (Nội thành HN)
- Không gọi GHN API
- Shipper nội bộ giao hàng
- `ghnOrderCode` = null

### 2. GHN API lỗi
- Phiếu xuất vẫn được tạo
- Tồn kho vẫn giảm
- Log lỗi để admin xử lý
- Admin có thể tạo đơn GHN thủ công sau

### 3. Thiếu ward code
- Hệ thống tự động lấy ward đầu tiên của district
- Hoặc báo lỗi nếu không tìm thấy

## Kiểm tra và Debug

### 1. Kiểm tra Order có ward code chưa
```sql
SELECT id, order_code, province, district, ward, address 
FROM orders 
WHERE id = 123;
```

### 2. Kiểm tra GHN order đã tạo chưa
```sql
SELECT id, order_code, ghn_order_code, ghn_shipping_status, ghn_created_at
FROM orders 
WHERE id = 123;
```

### 3. Kiểm tra phiếu xuất kho
```sql
SELECT * FROM export_orders 
WHERE order_id = 123;
```

### 4. Kiểm tra serial
```sql
SELECT serial_number, status, sold_date 
FROM product_details 
WHERE serial_number = 'IP15PM256BLK001';
```

### 5. Xem log backend
```bash
# Tìm log GHN
grep "GHN" logs/application.log

# Tìm log xuất kho
grep "export" logs/application.log
```

## Tổng kết

✅ **Đã hoàn thành:**
1. API chọn địa chỉ (provinces/districts/wards)
2. Lưu ward code vào Order
3. Tách biệt: Đặt hàng ≠ Tạo GHN
4. Tự động gọi GHN khi xuất kho
5. Cập nhật tồn kho đúng thời điểm
6. Tracking GHN

✅ **Luồng nghiệp vụ đúng:**
- Đặt hàng → Giữ hàng (reserved)
- Xuất kho → Giảm tồn + Gọi GHN
- Tracking → Theo dõi vận chuyển
