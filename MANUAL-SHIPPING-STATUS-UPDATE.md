# Tính năng: Cập nhật thủ công trạng thái đơn hàng sang "Đang giao"

## Vấn đề
GHN (Giao Hàng Nhanh) đôi khi cập nhật trạng thái đơn hàng chậm. Khi đơn hàng ở trạng thái **READY_TO_SHIP** (Đã chuẩn bị hàng - Đợi tài xế đến lấy), nhân viên bán hàng phải đợi GHN tự động cập nhật sang **SHIPPING** (Đang giao hàng).

## Giải pháp
Cho phép nhân viên bán hàng **chủ động cập nhật** trạng thái đơn hàng từ READY_TO_SHIP sang SHIPPING thay vì phải đợi GHN.

## ⚠️ Quy tắc quan trọng
- **CHỈ** cho phép cập nhật từ `READY_TO_SHIP` → `SHIPPING`
- **KHÔNG** cho phép cập nhật từ `CONFIRMED` hoặc các trạng thái khác
- Endpoint cũ `/shipping` đã bị vô hiệu hóa
- Chỉ sử dụng endpoint mới `/mark-shipping-from-ready`

## Luồng hoạt động

### 1. Trạng thái đơn hàng
```
CONFIRMED (Đã xác nhận)
    ↓
    [Kho xuất hàng]
    ↓
READY_TO_SHIP (Đã chuẩn bị hàng - Đợi tài xế)
    ↓
    [GHN tự động cập nhật HOẶC Nhân viên bán hàng cập nhật thủ công]
    ↓
SHIPPING (Đang giao hàng)
    ↓
DELIVERED (Đã giao hàng)
```

### 2. Khi nào sử dụng?
- Đơn hàng đã ở trạng thái **READY_TO_SHIP**
- Tài xế đã đến lấy hàng nhưng GHN chưa cập nhật
- Muốn thông báo cho khách hàng sớm hơn

## Cách sử dụng

### Trang danh sách đơn hàng (`/orders`)
1. Nhấn vào tab **"🚚 Đợi tài xế lấy hàng"** (màu tím) để lọc đơn hàng READY_TO_SHIP
2. Tìm đơn hàng cần cập nhật
3. Nhấn nút **"🚚 Chuyển sang Đang giao"** (màu tím)
4. Xác nhận trong hộp thoại
5. Trạng thái đơn hàng sẽ được cập nhật ngay lập tức

### Trang chi tiết đơn hàng (`/orders/[id]`)
1. Mở chi tiết đơn hàng có trạng thái **READY_TO_SHIP**
2. Nhấn nút **"🚚 Chuyển sang Đang giao"** ở góc trên bên phải (màu tím, nổi bật)
3. Đọc kỹ cảnh báo trong hộp thoại xác nhận
4. Chỉ nhấn OK nếu tài xế đã lấy hàng
5. Trang sẽ tự động tải lại với trạng thái mới

### Tab mới: "Đợi tài xế lấy hàng"
- Tab màu **tím** nổi bật trong danh sách đơn hàng
- Hiển thị tất cả đơn hàng ở trạng thái READY_TO_SHIP
- Giúp nhân viên dễ dàng theo dõi đơn hàng cần cập nhật

## Thay đổi kỹ thuật

### Backend

#### 1. Controller
**File:** `OrderManagementController.java`

**Endpoint mới (CHỈ endpoint này được dùng):**
```java
@PutMapping("/{orderId}/mark-shipping-from-ready")
public ApiResponse markShippingFromReady(@PathVariable Long orderId)
```

**Endpoint cũ (ĐÃ VÔ HIỆU HÓA):**
```java
// DEPRECATED: Không cho phép cập nhật từ CONFIRMED nữa
// @PutMapping("/{orderId}/shipping")
// public ApiResponse markAsShipping(@PathVariable Long orderId)
```

#### 2. Service
**File:** `OrderService.java` & `OrderServiceImpl.java`
```java
ApiResponse markShippingFromReady(Long orderId);
```

**Logic:**
- Kiểm tra đơn hàng phải ở trạng thái `READY_TO_SHIP`
- Cập nhật sang `SHIPPING`
- Ghi nhận thời gian `shippedAt`
- Publish event cho module kế toán
- Log hoạt động

#### 3. Validation
- Chỉ cho phép cập nhật từ `READY_TO_SHIP` → `SHIPPING`
- Không cho phép cập nhật từ các trạng thái khác
- Trả về lỗi rõ ràng nếu trạng thái không hợp lệ

### Frontend

#### 1. API Client
**File:** `lib/api.ts`
```typescript
markShippingFromReady: async (orderId: number): Promise<ApiResponse<any>>
```

#### 2. UI Components

**Tab mới: "Đợi tài xế lấy hàng"**
- Tab màu **tím** (purple) nổi bật
- Filter chỉ hiển thị đơn hàng READY_TO_SHIP
- Icon 🚚 để dễ nhận biết
- Highlight khi được chọn

**Trang danh sách đơn hàng:**
- Hiển thị nút **"🚚 Chuyển sang Đang giao"** CHỈ cho đơn READY_TO_SHIP
- Nút màu tím (purple-600) với shadow để nổi bật
- Xác nhận với cảnh báo rõ ràng trước khi cập nhật
- Tự động reload danh sách sau khi cập nhật thành công
- Badge trạng thái READY_TO_SHIP có border tím đậm

**Trang chi tiết đơn hàng:**
- Hiển thị nút lớn, nổi bật ở header
- Thông báo xác nhận chi tiết với emoji và hướng dẫn
- Text nhỏ phía dưới nhắc nhở "Chỉ cập nhật khi tài xế đã lấy hàng"
- Tự động reload chi tiết đơn hàng sau khi cập nhật
- Badge trạng thái có icon 🚚

#### 3. Toast Notifications
- ✅ Thành công: "Đã cập nhật trạng thái đơn hàng sang 'Đang giao hàng'"
- ❌ Lỗi: Hiển thị message từ backend

## Quyền truy cập
- Endpoint: `/api/admin/orders/{orderId}/mark-shipping-from-ready`
- Yêu cầu: Đăng nhập với vai trò **SALES_STAFF** hoặc **ADMIN**
- Hiện tại: Security đang tạm tắt để debug (sẽ bật lại sau)

## Lợi ích
1. ✅ **Tăng tốc độ xử lý**: Không phải đợi GHN cập nhật
2. ✅ **Cải thiện trải nghiệm khách hàng**: Thông báo nhanh hơn
3. ✅ **Linh hoạt**: Nhân viên có quyền kiểm soát
4. ✅ **An toàn**: Chỉ cho phép cập nhật từ READY_TO_SHIP, không cho phép từ CONFIRMED
5. ✅ **Tích hợp kế toán**: Tự động publish event cho module kế toán
6. ✅ **UI rõ ràng**: Tab riêng và nút nổi bật giúp dễ sử dụng
7. ✅ **Ngăn chặn lỗi**: Endpoint cũ đã bị vô hiệu hóa

## Lưu ý
- Tính năng này **không thay thế** webhook GHN
- GHN vẫn có thể cập nhật trạng thái sau đó
- Chỉ sử dụng khi thực sự cần thiết (tài xế đã lấy hàng)
- Không nên lạm dụng để tránh sai lệch với thực tế

## Testing

### Test Case 1: Cập nhật thành công
1. Tạo đơn hàng và xuất kho → trạng thái READY_TO_SHIP
2. Nhấn nút "Chuyển sang Đang giao"
3. Xác nhận
4. ✅ Trạng thái chuyển sang SHIPPING
5. ✅ Hiển thị toast thành công
6. ✅ Danh sách/chi tiết được reload

### Test Case 2: Trạng thái không hợp lệ
1. Thử cập nhật đơn hàng ở trạng thái CONFIRMED
2. ❌ Backend trả về lỗi: "Chỉ có thể chuyển sang đang giao hàng từ trạng thái 'Đã chuẩn bị hàng - Đợi tài xế'"
3. ❌ Hiển thị toast lỗi

### Test Case 3: Đơn hàng không tồn tại
1. Thử cập nhật đơn hàng với ID không tồn tại
2. ❌ Backend trả về lỗi: "Không tìm thấy đơn hàng"
3. ❌ Hiển thị toast lỗi

## API Endpoint

### Request
```http
PUT /api/admin/orders/{orderId}/mark-shipping-from-ready
Authorization: Bearer {token}
```

### Response - Success
```json
{
  "success": true,
  "message": "Đã chuyển đơn hàng sang đang giao hàng",
  "data": {
    "orderId": 123,
    "orderCode": "ORD20231219001",
    "status": "SHIPPING",
    "shippedAt": "2023-12-19T10:30:00",
    ...
  }
}
```

### Response - Error
```json
{
  "success": false,
  "message": "Chỉ có thể chuyển sang đang giao hàng từ trạng thái 'Đã chuẩn bị hàng - Đợi tài xế'"
}
```

## Files Changed

### Backend
- ✅ `OrderManagementController.java` - Thêm endpoint mới, vô hiệu hóa endpoint cũ
- ✅ `OrderService.java` - Thêm method signature
- ✅ `OrderServiceImpl.java` - Implement logic với validation chặt chẽ

### Frontend
- ✅ `lib/api.ts` - Thêm API client method mới, comment method cũ
- ✅ `app/orders/page.tsx` - Thêm tab "Đợi tài xế lấy hàng", nút cập nhật, styling
- ✅ `app/orders/[id]/page.tsx` - Thêm nút cập nhật nổi bật, cảnh báo chi tiết

### Documentation
- ✅ `MANUAL-SHIPPING-STATUS-UPDATE.md` - File này

## UI/UX Improvements

### Visual Indicators
- **Tab màu tím**: Dễ nhận biết tab "Đợi tài xế lấy hàng"
- **Border tím đậm**: Badge READY_TO_SHIP có border để nổi bật
- **Shadow effect**: Nút cập nhật có shadow để thu hút sự chú ý
- **Icon 🚚**: Sử dụng emoji truck để dễ hiểu

### User Guidance
- **Cảnh báo rõ ràng**: Hộp thoại xác nhận có emoji và hướng dẫn chi tiết
- **Text nhắc nhở**: "Chỉ cập nhật khi tài xế đã lấy hàng"
- **Toast notification**: Thông báo thành công/lỗi rõ ràng với emoji

### Safety Features
- **Validation backend**: Chỉ cho phép từ READY_TO_SHIP
- **Confirmation dialog**: Yêu cầu xác nhận trước khi cập nhật
- **Endpoint deprecated**: Vô hiệu hóa endpoint cũ để tránh nhầm lẫn

## Kết luận
Tính năng này giúp nhân viên bán hàng chủ động hơn trong việc quản lý trạng thái đơn hàng, không phải phụ thuộc hoàn toàn vào tốc độ cập nhật của GHN. Điều này cải thiện đáng kể trải nghiệm của cả nhân viên và khách hàng.
