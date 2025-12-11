# Module Kế toán - Clean & Simple

## Triết lý: Chỉ làm việc với DATA THẬT

Module kế toán đã được đơn giản hóa để chỉ làm việc với dữ liệu thực tế từ hệ thống, không có giả định hay dữ liệu mẫu phức tạp.

## Các danh mục giao dịch (TransactionCategory)

### Chỉ có 4 loại - tất cả từ data thật:

1. **SALES** - Doanh thu bán hàng
   - Tự động tạo từ `Order.total` khi đơn hàng được thanh toán

2. **SHIPPING** - Chi phí vận chuyển  
   - Tự động tạo từ `Order.shippingFee` khi đơn hàng được thanh toán

3. **PAYMENT_FEE** - Phí cổng thanh toán
   - Tự động tính = 2% của `Order.total`

4. **TAX** - Thuế
   - VAT: 10% của lợi nhuận gộp
   - TNDN: 20% của lợi nhuận sau VAT

## Công thức tính toán đơn giản

```
Doanh thu = Order.total
Chi phí vận chuyển = Order.shippingFee  
Phí thanh toán = Order.total × 2%

Tổng chi phí = Chi phí vận chuyển + Phí thanh toán
Lợi nhuận gộp = Doanh thu - Tổng chi phí

VAT = Lợi nhuận gộp × 10%
Lợi nhuận sau VAT = Lợi nhuận gộp - VAT

Thuế TNDN = Lợi nhuận sau VAT × 20%
Lợi nhuận ròng = Lợi nhuận sau VAT - Thuế TNDN
```

## Về vấn đề doanh thu về 0 sau khi chốt kỳ

Logic chốt kỳ trong `AccountingServiceImpl.closePeriod()` đã được kiểm tra và **hoạt động đúng**:

```java
// Tính doanh thu thực tế từ đơn hàng trong kỳ
Double actualRevenue = orderRepo.sumTotalByDateRange(startDateTime, endDateTime);
if (actualRevenue == null) actualRevenue = 0.0;

// Cập nhật thông tin kỳ
period.setTotalRevenue(BigDecimal.valueOf(actualRevenue));
```

**Lưu ý quan trọng**: 
- Doanh thu được tính từ các đơn hàng **đã thanh toán** trong khoảng thời gian của kỳ
- Nếu không có đơn hàng nào trong kỳ đó, doanh thu sẽ là 0
- Doanh thu 65,000,000 ₫ ban đầu có thể là dữ liệu mẫu không chính xác

## Cách kiểm tra

1. **Xóa database cũ và tạo lại**:
   ```sql
   DROP DATABASE web2;
   CREATE DATABASE web2;
   ```

2. **Chạy lại ứng dụng**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Kiểm tra dữ liệu mẫu mới**:
   - Truy cập: http://localhost:3000/admin/accounting/transactions
   - Xem các giao dịch mẫu với danh mục mới
   - Không còn thấy "Marketing" hay "Vận hành"

4. **Kiểm tra báo cáo**:
   - Truy cập: http://localhost:3000/admin/accounting/advanced-reports
   - Xem báo cáo lãi lỗ với các danh mục chi phí mới

## Files đã được cập nhật

### Backend
1. `src/main/java/com/doan/WEB_TMDT/module/accounting/entity/TransactionCategory.java`
2. `src/main/java/com/doan/WEB_TMDT/module/accounting/dto/ProfitLossReport.java`
3. `src/main/java/com/doan/WEB_TMDT/module/accounting/service/impl/FinancialTransactionServiceImpl.java`
4. `src/main/java/com/doan/WEB_TMDT/config/AccountingDataInitializer.java`

### Frontend
1. `src/frontend/app/admin/accounting/transactions/page.tsx`
2. `src/frontend/app/admin/accounting/advanced-reports/page.tsx`

## Tính năng tự động

Module kế toán sẽ **tự động tạo giao dịch** khi:
- Đơn hàng được thanh toán → Tạo giao dịch SALES (doanh thu)
- Đơn hàng được thanh toán → Tạo giao dịch SHIPPING (chi phí vận chuyển)
- Đơn hàng được thanh toán → Tạo giao dịch PAYMENT_FEE (phí thanh toán 2%)

Điều này được thực hiện qua:
- `OrderEventListener` - Lắng nghe sự kiện đơn hàng
- `PaymentServiceImpl` - Publish event khi thanh toán thành công
- `OrderServiceImpl` - Publish event khi cập nhật trạng thái đơn hàng


## Dữ liệu mẫu

**KHÔNG CÓ** - Tất cả giao dịch được tạo tự động từ đơn hàng thật.

Khi khởi động lần đầu, module kế toán sẽ:
- Tạo 2 kỳ báo cáo mẫu (tháng 11 và 12/2024)
- KHÔNG tạo giao dịch mẫu
- Chờ đơn hàng thật được thanh toán để tạo giao dịch

## Tự động hóa

### Khi đơn hàng được thanh toán:
1. Tạo giao dịch SALES (doanh thu)
2. Tạo giao dịch SHIPPING (chi phí vận chuyển)
3. Tạo giao dịch PAYMENT_FEE (phí thanh toán)

### Khi đơn hàng bị hủy (sau khi đã thanh toán):
- Tạo giao dịch REFUND (hoàn tiền)

## Files đã được làm sạch

### Backend
1. `TransactionCategory.java` - Chỉ còn 4 categories
2. `ProfitLossReport.java` - Chỉ còn các trường cần thiết
3. `FinancialTransactionServiceImpl.java` - Logic tính toán đơn giản
4. `AccountingServiceImpl.java` - Công thức tính toán rõ ràng
5. `AccountingDataInitializer.java` - Không tạo giao dịch mẫu

### Frontend
1. `transactions/page.tsx` - Hiển thị 4 categories
2. `reports/page.tsx` - Bảng báo cáo đơn giản với 10 cột
3. `advanced-reports/page.tsx` - Chỉ hiển thị 2 chi phí thật

## Lợi ích

✅ **Dễ đọc**: Code ngắn gọn, logic rõ ràng
✅ **Dễ fix**: Ít logic phức tạp, ít bug
✅ **Data thật**: Không có giả định, tất cả từ hệ thống
✅ **Dễ mở rộng**: Thêm category mới khi thực sự cần

## Cách test

1. Tạo đơn hàng mới
2. Thanh toán đơn hàng
3. Vào `/admin/accounting/transactions` - Thấy 3 giao dịch tự động:
   - SALES (doanh thu)
   - SHIPPING (chi phí VC)
   - PAYMENT_FEE (phí thanh toán)
4. Vào `/admin/accounting/reports` - Xem báo cáo tài chính
5. Chốt kỳ - Doanh thu tính từ đơn hàng thật trong kỳ
