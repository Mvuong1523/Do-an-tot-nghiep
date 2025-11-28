# Logic Hủy Đơn Hàng

## ✅ Đã cập nhật

### Backend: `OrderServiceImpl.cancelOrderByCustomer()`

## 🎯 Logic mới

### 1. Hủy đơn PENDING_PAYMENT (Chờ thanh toán)

**Khi:** Khách hàng nhấn "Hủy đơn hàng" trong trang thanh toán

**Hành động:**
```java
if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
    // 1. Giải phóng stock đã reserve
    for (OrderItem item : order.getItems()) {
        product.setReservedQuantity(currentReserved - item.getQuantity());
    }
    
    // 2. XÓA đơn hàng khỏi database
    orderRepository.delete(order);
    
    return "Đã hủy đơn hàng";
}
```

**Kết quả:**
- ❌ Đơn hàng bị XÓA hoàn toàn
- ❌ KHÔNG xuất hiện trong danh sách "Đã hủy"
- ✅ Stock được giải phóng
- ✅ Không có dữ liệu rác trong DB

### 2. Hủy đơn CONFIRMED trở đi (Đã xác nhận)

**Khi:** Khách hàng hủy đơn đã được xác nhận

**Hành động:**
```java
if (order.getStatus() == OrderStatus.CONFIRMED || 
    order.getStatus() == OrderStatus.SHIPPING) {
    
    // 1. Chuyển status sang CANCELLED
    order.setStatus(OrderStatus.CANCELLED);
    order.setCancelledAt(LocalDateTime.now());
    order.setCancelReason(reason);
    
    // 2. Lưu lại trong DB
    orderRepository.save(order);
    
    return "Đã hủy đơn hàng";
}
```

**Kết quả:**
- ✅ Đơn hàng được LƯU LẠI với status = CANCELLED
- ✅ Xuất hiện trong danh sách "Đã hủy"
- ✅ Có thể xem lịch sử
- ✅ Nếu đã thanh toán → Thông báo hoàn tiền

## 📊 Bảng so sánh

| Trạng thái | Hành động khi hủy | Kết quả | Lý do |
|------------|-------------------|---------|-------|
| PENDING_PAYMENT | **XÓA** khỏi DB | Không còn trong danh sách | Chưa thanh toán, chưa xử lý |
| CONFIRMED | **CANCELLED** | Vào danh sách "Đã hủy" | Đã xác nhận, cần lưu lịch sử |
| SHIPPING | **CANCELLED** | Vào danh sách "Đã hủy" | Đang giao, cần lưu lịch sử |
| DELIVERED | ❌ Không cho hủy | - | Đã giao thành công |

## 🔄 Flow hoàn chỉnh

### Flow 1: Hủy khi chờ thanh toán

```
1. Đặt hàng Online
   ↓
2. Status: PENDING_PAYMENT
   ↓
3. Vào trang thanh toán
   ↓
4. Nhấn "Hủy đơn hàng"
   ↓
5. Confirm
   ↓
6. Backend XÓA đơn khỏi DB
   ↓
7. Giải phóng stock
   ↓
8. Toast: "Đã hủy đơn hàng"
   ↓
9. Redirect về /orders
   ↓
10. Đơn KHÔNG còn trong danh sách
```

### Flow 2: Hủy khi đã xác nhận

```
1. Đơn hàng đã CONFIRMED
   ↓
2. Khách vào chi tiết đơn
   ↓
3. Nhấn "Hủy đơn hàng"
   ↓
4. Confirm
   ↓
5. Backend chuyển status → CANCELLED
   ↓
6. Lưu lại trong DB
   ↓
7. Toast: "Đã hủy đơn hàng"
   ↓
8. Đơn vào danh sách "Đã hủy"
```

## 💡 Lợi ích

### 1. Database sạch hơn
- Không lưu đơn hàng "rác" (chưa thanh toán)
- Chỉ lưu đơn có ý nghĩa (đã xác nhận)

### 2. UX tốt hơn
- Khách không thấy đơn "Đã hủy" khi chỉ thử thanh toán
- Danh sách đơn hàng gọn gàng hơn

### 3. Quản lý tốt hơn
- Admin chỉ thấy đơn thực sự cần xử lý
- Thống kê chính xác hơn

### 4. Performance tốt hơn
- Ít dữ liệu trong DB
- Query nhanh hơn

## 🧪 Test Cases

### Test 1: Hủy đơn PENDING_PAYMENT
1. Đặt hàng Online
2. ✅ Status = PENDING_PAYMENT
3. Nhấn "Hủy đơn hàng"
4. ✅ Toast: "Đã hủy đơn hàng"
5. ✅ Redirect về /orders
6. ✅ Đơn KHÔNG còn trong danh sách
7. ✅ Check DB: Đơn đã bị XÓA
8. ✅ Stock được giải phóng

### Test 2: Hủy đơn CONFIRMED
1. Đơn hàng COD (tự động CONFIRMED)
2. ✅ Status = CONFIRMED
3. Nhấn "Hủy đơn hàng"
4. ✅ Toast: "Đã hủy đơn hàng"
5. ✅ Đơn vào danh sách "Đã hủy"
6. ✅ Check DB: Status = CANCELLED
7. ✅ Có thể xem lịch sử

### Test 3: Hủy đơn đã thanh toán
1. Đơn Online đã thanh toán
2. ✅ Status = CONFIRMED, PaymentStatus = PAID
3. Nhấn "Hủy đơn hàng"
4. ✅ Toast: "Đã hủy đơn hàng. Tiền sẽ được hoàn lại..."
5. ✅ Đơn vào danh sách "Đã hủy"
6. ✅ Check DB: Status = CANCELLED

### Test 4: Không cho hủy đơn đã giao
1. Đơn hàng DELIVERED
2. ❌ Không có nút "Hủy đơn hàng"
3. Hoặc nếu có: "Không thể hủy đơn hàng đã giao thành công"

## 📝 Notes

- **PENDING_PAYMENT**: Đơn tạm thời, chưa có giá trị → XÓA
- **CONFIRMED trở đi**: Đơn thực sự, đã xử lý → CANCELLED (lưu lại)
- Stock luôn được giải phóng khi hủy
- Nếu đã thanh toán → Cần xử lý hoàn tiền (TODO)

## 🔐 Security

- ✅ Verify ownership: Chỉ khách hàng sở hữu mới hủy được
- ✅ Check status: Không cho hủy đơn đã giao
- ✅ Transaction: Đảm bảo atomic (xóa đơn + giải phóng stock)

Happy coding! 🎉
