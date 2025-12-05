# ✅ GHN Integration - Bước 2: API xem trạng thái vận chuyển

## 📋 Tổng quan

Đã hoàn thành API để xem trạng thái vận chuyển real-time từ GHN, bao gồm lịch sử di chuyển của đơn hàng.

## 🎯 Tính năng đã triển khai

### 1. DTO Class

**GHNOrderDetailResponse.java**
- Chứa thông tin chi tiết đơn hàng từ GHN
- Bao gồm:
  - `orderCode`: Mã vận đơn GHN
  - `status`: Trạng thái hiện tại (mã)
  - `statusText`: Trạng thái hiện tại (tiếng Việt)
  - `expectedDeliveryTime`: Thời gian giao dự kiến
  - `updatedDate`: Thời gian cập nhật cuối
  - `currentWarehouse`: Kho hiện tại
  - `codAmount`: Số tiền COD
  - `shippingFee`: Phí vận chuyển
  - `logs`: Lịch sử di chuyển (StatusLog[])

**StatusLog (nested class)**
- `status`: Mã trạng thái
- `statusText`: Trạng thái (tiếng Việt)
- `time`: Thời gian
- `location`: Địa điểm

### 2. ShippingService

**Method mới: `getGHNOrderDetail(String ghnOrderCode)`**
- Gọi API GHN: `/v2/shipping-order/detail`
- Parse response và convert timestamps
- Dịch status codes sang tiếng Việt
- Trả về GHNOrderDetailResponse với đầy đủ thông tin

**Helper method: `getStatusText(String status)`**
Mapping các status code GHN sang tiếng Việt:
- `ready_to_pick` → "Chờ lấy hàng"
- `picking` → "Đang lấy hàng"
- `picked` → "Đã lấy hàng"
- `storing` → "Hàng đang nằm ở kho"
- `transporting` → "Đang luân chuyển"
- `sorting` → "Đang phân loại"
- `delivering` → "Đang giao hàng"
- `delivered` → "Đã giao hàng"
- `delivery_fail` → "Giao hàng thất bại"
- `waiting_to_return` → "Chờ trả hàng"
- `return` → "Trả hàng"
- `returned` → "Đã trả hàng"
- `cancel` → "Đã hủy"
- `exception` → "Đơn hàng ngoại lệ"
- `damage` → "Hàng bị hư hỏng"
- `lost` → "Hàng bị thất lạc"

### 3. OrderService

**Method mới: `getShippingStatus(Long orderId, Long customerId)`**
- Verify ownership (khách hàng chỉ xem được đơn của mình)
- Kiểm tra đơn có mã GHN không
- Gọi ShippingService để lấy thông tin từ GHN
- Tự động cập nhật `ghnShippingStatus` vào database
- Trả về thông tin chi tiết

**Method mới: `getShippingStatusAdmin(Long orderId)`**
- Tương tự nhưng dành cho Admin/Staff
- Không cần verify ownership

### 4. API Endpoints

#### Customer Endpoint
```
GET /api/orders/{orderId}/shipping-status
Authorization: Bearer <customer_token>
```

#### Admin Endpoint
```
GET /api/admin/orders/{orderId}/shipping-status
Authorization: Bearer <admin_token>
```

## 📝 Logic hoạt động

1. **Khách/Admin gọi API** → `GET /api/orders/{orderId}/shipping-status`
2. **Verify quyền truy cập** → Kiểm tra ownership (nếu là customer)
3. **Kiểm tra mã GHN** → Nếu không có → Trả lỗi
4. **Gọi GHN API** → `/v2/shipping-order/detail`
5. **Parse response:**
   - Convert timestamps (Unix epoch → LocalDateTime)
   - Dịch status codes sang tiếng Việt
   - Parse logs history
6. **Cập nhật database** → Lưu `ghnShippingStatus` mới nhất
7. **Trả về response** → Thông tin chi tiết + lịch sử

## 🧪 Test

### Test case 1: Xem trạng thái đơn hàng (Customer)
```bash
GET /api/orders/123/shipping-status
Authorization: Bearer <customer_token>

Expected Response:
{
  "success": true,
  "message": "Trạng thái vận chuyển",
  "data": {
    "orderCode": "GHNABCD1234",
    "status": "delivering",
    "statusText": "Đang giao hàng",
    "expectedDeliveryTime": "2023-12-07T18:00:00",
    "updatedDate": "2023-12-06T14:30:00",
    "currentWarehouse": "Kho Hà Nội",
    "codAmount": 500000,
    "shippingFee": 30000,
    "logs": [
      {
        "status": "picked",
        "statusText": "Đã lấy hàng",
        "time": "2023-12-05T10:00:00",
        "location": "Hà Đông, Hà Nội"
      },
      {
        "status": "transporting",
        "statusText": "Đang luân chuyển",
        "time": "2023-12-05T15:00:00",
        "location": "Trung tâm phân loại HN"
      },
      {
        "status": "delivering",
        "statusText": "Đang giao hàng",
        "time": "2023-12-06T08:00:00",
        "location": "Bưu cục Bắc Ninh"
      }
    ]
  }
}
```

### Test case 2: Đơn không có mã GHN
```bash
GET /api/orders/456/shipping-status

Expected Response:
{
  "success": false,
  "message": "Đơn hàng này không có mã vận đơn GHN"
}
```

### Test case 3: Xem đơn của người khác (Customer)
```bash
GET /api/orders/789/shipping-status
Authorization: Bearer <customer_token>

Expected Response:
{
  "success": false,
  "message": "Bạn không có quyền xem đơn hàng này"
}
```

### Test case 4: Admin xem bất kỳ đơn nào
```bash
GET /api/admin/orders/123/shipping-status
Authorization: Bearer <admin_token>

Expected Response:
{
  "success": true,
  "message": "Trạng thái vận chuyển",
  "data": { ... }
}
```

## 📊 GHN Status Codes

| Status Code | Tiếng Việt | Ý nghĩa |
|------------|-----------|---------|
| `ready_to_pick` | Chờ lấy hàng | Đơn đã tạo, chờ shipper đến lấy |
| `picking` | Đang lấy hàng | Shipper đang đến lấy hàng |
| `picked` | Đã lấy hàng | Đã lấy hàng thành công |
| `storing` | Hàng đang nằm ở kho | Hàng đang ở kho GHN |
| `transporting` | Đang luân chuyển | Đang vận chuyển giữa các kho |
| `sorting` | Đang phân loại | Đang phân loại tại trung tâm |
| `delivering` | Đang giao hàng | Shipper đang giao cho người nhận |
| `delivered` | Đã giao hàng | Giao thành công |
| `delivery_fail` | Giao hàng thất bại | Không giao được (khách không nhận, sai địa chỉ...) |
| `waiting_to_return` | Chờ trả hàng | Chờ trả hàng về shop |
| `return` | Trả hàng | Đang trong quá trình trả hàng |
| `returned` | Đã trả hàng | Đã trả hàng về shop |
| `cancel` | Đã hủy | Đơn bị hủy |
| `exception` | Đơn hàng ngoại lệ | Có vấn đề bất thường |
| `damage` | Hàng bị hư hỏng | Hàng bị hư trong quá trình vận chuyển |
| `lost` | Hàng bị thất lạc | Hàng bị mất |

## 🔄 Auto-update

Mỗi lần gọi API xem trạng thái, hệ thống tự động:
1. Lấy status mới nhất từ GHN
2. Cập nhật `ghnShippingStatus` trong database
3. Giữ lịch sử đồng bộ với GHN

## ⚠️ Lưu ý

1. **Rate limiting**: GHN có thể giới hạn số lần gọi API, nên cache kết quả nếu cần
2. **Error handling**: Nếu GHN API fail, trả về thông tin từ database
3. **Timestamp parsing**: GHN trả về Unix timestamp (seconds), cần convert sang LocalDateTime
4. **Logs order**: Logs được sắp xếp theo thời gian (cũ → mới)

## 🔜 Bước tiếp theo

**Bước 3: Webhook nhận callback từ GHN**
- Endpoint: `POST /api/webhooks/ghn`
- Tự động cập nhật khi GHN push status changes
- Không cần polling, real-time updates
- Cập nhật cả `ghnShippingStatus` và `status` của Order
