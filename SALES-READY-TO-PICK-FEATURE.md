# 📦 Tính năng "Sẵn sàng lấy hàng" cho Nhân viên Bán hàng

## 🎯 Tổng quan

Đã thêm tab **"Sẵn sàng lấy hàng"** (READY_TO_SHIP) vào trang quản lý đơn hàng của nhân viên bán hàng, cho phép xem và xử lý các đơn hàng đã được xuất kho và sẵn sàng để giao cho shipper.

## ✨ Tính năng mới

### 1. Tab "Sẵn sàng lấy hàng"

**Vị trí:** `/sales/orders`

**Mô tả:** Tab mới hiển thị các đơn hàng có trạng thái `READY_TO_SHIP` - đơn hàng đã được kho xuất xong và sẵn sàng để giao hàng.

**Đặc điểm:**
- Icon: 📦 (Package)
- Màu sắc: Indigo (xanh tím)
- Hiển thị đầy đủ thông tin đơn hàng
- Nút hành động: "Chuyển sang Đang giao"

### 2. Nút "Chuyển sang Đang giao"

**Chức năng:** 
- Chuyển trạng thái đơn hàng từ `READY_TO_PICK` → `SHIPPING`
- Đánh dấu đơn hàng đã được giao cho shipper/đơn vị vận chuyển

**Giao diện:**
- Màu: Indigo (xanh tím)
- Icon: 🚚 (Truck)
- Text: "Chuyển sang Đang giao"
- Có loading state khi đang xử lý

## 🔄 Luồng xử lý đơn hàng

```
1. PENDING (Chờ xác nhận)
   ↓ [Nhân viên bán hàng xác nhận]
   
2. CONFIRMED (Đã xác nhận)
   ↓ [Nhân viên kho xuất hàng]
   
3. READY_TO_SHIP (Sẵn sàng lấy hàng) ⭐ MỚI
   ↓ [Nhân viên bán hàng chuyển giao]
   
4. SHIPPING (Đang giao hàng)
   ↓ [Shipper giao hàng]
   
5. DELIVERED (Đã giao)
```

## 📋 Danh sách Tab

Trang `/sales/orders` hiện có các tab:

1. **Tất cả** - Hiển thị tất cả đơn hàng
2. **Chờ thanh toán** - Đơn chưa thanh toán
3. **Đã xác nhận** - Đơn đã xác nhận, chờ xuất kho
4. **Sẵn sàng lấy hàng** ⭐ - Đơn đã xuất kho, sẵn sàng giao
5. **Đang giao** - Đơn đang được vận chuyển
6. **Đã giao** - Đơn đã giao thành công
7. **Đã hủy** - Đơn đã bị hủy

## 🎨 Màu sắc trạng thái

| Trạng thái | Màu nền | Màu chữ | Icon |
|------------|---------|---------|------|
| PENDING_PAYMENT | Orange | Orange | 🕐 |
| PENDING | Yellow | Yellow | 🕐 |
| CONFIRMED | Blue | Blue | ✅ |
| **READY_TO_SHIP** | **Indigo** | **Indigo** | **📦** |
| SHIPPING | Purple | Purple | 🚚 |
| DELIVERED | Green | Green | ✅ |
| CANCELLED | Red | Red | ❌ |

## 💻 Code Changes

### File: `src/frontend/app/sales/orders/page.tsx`

**Thay đổi:**

1. **Thêm tab READY_TO_SHIP:**
```typescript
{ key: 'READY_TO_SHIP', label: 'Sẵn sàng lấy hàng', icon: '📦' }
```

2. **Thêm màu sắc:**
```typescript
case 'READY_TO_SHIP': return 'bg-indigo-100 text-indigo-800'
```

3. **Thêm icon:**
```typescript
case 'READY_TO_SHIP': return <FiPackage className="text-indigo-600" size={20} />
```

4. **Thêm text hiển thị:**
```typescript
case 'READY_TO_SHIP': return 'Sẵn sàng lấy hàng'
```

5. **Thêm nút hành động:**
```typescript
case 'READY_TO_SHIP':
  return (
    <button
      onClick={() => handleMarkAsShipping(order.orderId)}
      disabled={isProcessing}
      className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:bg-gray-400 transition-colors text-sm font-medium inline-flex items-center"
    >
      <FiTruck className="mr-2" />
      {isProcessing ? 'Đang xử lý...' : 'Chuyển sang Đang giao'}
    </button>
  )
```

## 🔧 API Endpoint

**Endpoint:** `PUT /api/admin/orders/{orderId}/mark-shipping-from-ready`

**Chức năng:** Chuyển đơn hàng từ `READY_TO_SHIP` sang `SHIPPING`

**Lưu ý:** API này CHỈ chấp nhận đơn hàng có trạng thái `READY_TO_SHIP`. Không thể chuyển từ các trạng thái khác.

## 📱 Hướng dẫn sử dụng

### Cho Nhân viên Bán hàng:

1. **Truy cập trang quản lý đơn hàng:**
   - Đăng nhập với tài khoản nhân viên bán hàng
   - Vào menu "Quản lý đơn hàng" hoặc truy cập `/sales/orders`

2. **Xem đơn sẵn sàng lấy hàng:**
   - Click vào tab "Sẵn sàng lấy hàng" (📦)
   - Xem danh sách các đơn đã được kho xuất xong

3. **Chuyển giao đơn hàng:**
   - Kiểm tra thông tin đơn hàng
   - Click nút "Chuyển sang Đang giao"
   - Xác nhận trong popup
   - Đơn hàng sẽ chuyển sang tab "Đang giao"

4. **Theo dõi đơn hàng:**
   - Vào tab "Đang giao" để xem các đơn đang vận chuyển
   - Click "Chi tiết" để xem thông tin tracking (nếu có)

## ✅ Checklist kiểm tra

- [x] Tab "Sẵn sàng lấy hàng" hiển thị đúng
- [x] Màu sắc indigo cho trạng thái READY_TO_SHIP
- [x] Icon package hiển thị
- [x] Nút "Chuyển sang Đang giao" hoạt động
- [x] Loading state khi đang xử lý
- [x] Toast notification khi thành công/lỗi
- [x] Tự động reload danh sách sau khi cập nhật
- [x] Responsive trên mobile

## 🐛 Xử lý lỗi

### Lỗi: "Không thể cập nhật trạng thái"

**Nguyên nhân:**
- Đơn hàng không ở trạng thái READY_TO_SHIP
- Không có quyền xử lý đơn hàng
- Lỗi kết nối API

**Giải pháp:**
1. Kiểm tra trạng thái đơn hàng hiện tại
2. Đảm bảo đăng nhập với tài khoản nhân viên bán hàng
3. Refresh trang và thử lại
4. Liên hệ admin nếu vẫn lỗi

## 🔮 Tính năng tương lai

- [ ] Thông báo real-time khi có đơn mới READY_TO_SHIP
- [ ] Tích hợp in phiếu giao hàng
- [ ] Gán shipper trực tiếp từ trang này
- [ ] Quét QR code để xác nhận lấy hàng
- [ ] Thống kê thời gian xử lý trung bình

## 📞 Hỗ trợ

Nếu gặp vấn đề, liên hệ:
- Team Dev: dev@example.com
- Hotline: 1900-xxxx
