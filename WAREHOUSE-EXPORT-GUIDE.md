# Hướng dẫn Xuất kho và Tạo đơn GHN

## Luồng nghiệp vụ

### 1. Khách đặt hàng
- Khách chọn sản phẩm → Thanh toán
- Hệ thống tạo Order với status:
  - `PENDING_PAYMENT` (nếu thanh toán online)
  - `CONFIRMED` (nếu COD)
- **Giảm `reservedQuantity`** (giữ hàng)
- **KHÔNG gọi GHN** ở bước này

### 2. Quản lý kho xem đơn cần xuất
**Endpoint:** `GET /api/inventory/orders/pending-export`

Response:
```json
{
  "success": true,
  "data": [
    {
      "orderId": 123,
      "orderCode": "ORD20231119001",
      "customerName": "Nguyễn Văn A",
      "customerPhone": "0123456789",
      "shippingAddress": "123 Đường ABC, Phường XYZ, Quận 1, TP.HCM",
      "items": [
        {
          "productId": 1,
          "productName": "iPhone 15 Pro Max",
          "sku": "IP15PM-256-BLK",
          "quantity": 1,
          "price": 29990000
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

### 3. Tạo phiếu xuất kho
**Endpoint:** `POST /api/inventory/export-for-sale`

Request body:
```json
{
  "orderId": 123,
  "reason": "Xuất kho bán hàng - Giao cho khách",
  "note": "Kiểm tra kỹ trước khi giao",
  "items": [
    {
      "productSku": "IP15PM-256-BLK",
      "serialNumbers": [
        "IP15PM256BLK001",
        "IP15PM256BLK002"
      ]
    }
  ]
}
```

**Lưu ý:**
- `createdBy` sẽ tự động lấy từ user đang đăng nhập
- Mỗi sản phẩm cần nhập đủ serial numbers theo số lượng
- Serial phải ở trạng thái `IN_STOCK`

### 4. Hệ thống xử lý tự động

Khi gọi API xuất kho, hệ thống sẽ:

1. **Kiểm tra tồn kho**
   - Đủ số lượng không?
   - Serial có hợp lệ không?
   - Serial có đang IN_STOCK không?

2. **Cập nhật trạng thái sản phẩm**
   - Đổi status serial từ `IN_STOCK` → `SOLD`
   - Ghi nhận `soldDate`

3. **Giảm tồn kho**
   - Giảm `onHand` (tồn kho thực tế)

4. **Tạo phiếu xuất**
   - Tạo `ExportOrder` với status `COMPLETED`
   - Ghi nhận thông tin xuất kho

5. **Gọi GHN API** (TỰ ĐỘNG)
   - Lấy thông tin địa chỉ từ Order
   - Tạo đơn vận chuyển trên GHN
   - Cập nhật `ghnOrderCode` vào Order
   - Cập nhật `ghnShippingStatus` = "created"

Response:
```json
{
  "success": true,
  "message": "Xuất kho bán hàng thành công",
  "data": "EX-SALE-1700123456789"
}
```

### 5. Kiểm tra kết quả

**Xem chi tiết phiếu xuất:**
```
GET /api/inventory/export-orders/{id}
```

**Xem thông tin GHN của đơn hàng:**
```
GET /api/orders/{orderId}
```

Response sẽ có thêm:
```json
{
  "ghnOrderCode": "GHNABCD123",
  "ghnShippingStatus": "created",
  "ghnCreatedAt": "2023-11-19T11:00:00",
  "ghnExpectedDeliveryTime": "2023-11-21T17:00:00"
}
```

## API Endpoints

### 1. Lấy danh sách đơn cần xuất kho
```
GET /api/inventory/orders/pending-export?page=0&size=20
```

### 2. Xem chi tiết đơn hàng
```
GET /api/inventory/orders/{orderId}
```

### 3. Xuất kho bán hàng
```
POST /api/inventory/export-for-sale
Content-Type: application/json
Authorization: Bearer {token}

{
  "orderId": 123,
  "reason": "Xuất kho bán hàng",
  "note": "Ghi chú",
  "items": [
    {
      "productSku": "SKU001",
      "serialNumbers": ["SN001", "SN002"]
    }
  ]
}
```

### 4. Xuất kho bảo hành
```
POST /api/inventory/export-for-warranty
Content-Type: application/json
Authorization: Bearer {token}

{
  "orderId": 123,
  "reason": "Xuất kho bảo hành",
  "note": "Sản phẩm lỗi màn hình",
  "productSku": "SKU001",
  "serialNumber": "SN001"
}
```

## Lưu ý quan trọng

### Về Serial Numbers
- Mỗi sản phẩm có serial phải nhập đúng số lượng serial
- Serial phải tồn tại trong hệ thống
- Serial phải ở trạng thái `IN_STOCK`
- Không được trùng serial

### Về GHN Integration
- GHN chỉ được gọi khi:
  - `shippingFee > 0`
  - Không phải nội thành Hà Nội (free ship)
- Nếu GHN API lỗi:
  - Phiếu xuất vẫn được tạo
  - Admin có thể tạo đơn GHN thủ công sau
  - Kiểm tra log để biết lỗi

### Về Tồn kho
- `reservedQuantity`: Giảm khi khách đặt hàng
- `onHand`: Giảm khi xuất kho thực tế
- `availableQuantity` = `onHand` - `reservedQuantity`

## Troubleshooting

### Lỗi: "Serial không ở trạng thái IN_STOCK"
- Kiểm tra serial đã được xuất kho chưa
- Kiểm tra serial có đúng không

### Lỗi: "Không đủ tồn kho"
- Kiểm tra số lượng serial nhập vào
- Kiểm tra tồn kho thực tế

### Lỗi: "Failed to create GHN order"
- Kiểm tra thông tin địa chỉ đầy đủ chưa
- Kiểm tra ward code có hợp lệ không
- Kiểm tra GHN token còn hiệu lực không
- Xem log backend để biết chi tiết

### GHN order không được tạo
- Kiểm tra `shippingFee` có > 0 không
- Kiểm tra có phải nội thành HN không (free ship)
- Kiểm tra log backend

## Test Flow

1. **Tạo đơn hàng test:**
```bash
POST /api/orders
{
  "province": "Hà Nội",
  "district": "Cầu Giấy",
  "ward": "20308",
  "address": "123 Đường ABC",
  "shippingFee": 30000,
  "paymentMethod": "COD"
}
```

2. **Kiểm tra đơn cần xuất:**
```bash
GET /api/inventory/orders/pending-export
```

3. **Xuất kho:**
```bash
POST /api/inventory/export-for-sale
{
  "orderId": 123,
  "reason": "Test xuất kho",
  "items": [...]
}
```

4. **Kiểm tra GHN order:**
```bash
GET /api/orders/123
# Xem field ghnOrderCode
```

5. **Tracking GHN:**
```bash
GET /api/orders/123/ghn-tracking
```
