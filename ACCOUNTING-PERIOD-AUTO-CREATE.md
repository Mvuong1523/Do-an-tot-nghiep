# Tự động tạo kỳ kế toán theo tháng

## Tổng quan
Hệ thống đã được cấu hình để **tự động tạo kỳ kế toán theo tháng** mà không cần thao tác thủ công.

## Cách hoạt động

### 1. Tự động tạo khi khởi động
Khi backend khởi động, hệ thống sẽ tự động:
- Tạo kỳ kế toán cho **tháng hiện tại** (nếu chưa có)
- Tạo kỳ kế toán cho **tháng trước** (nếu chưa có) - để có dữ liệu lịch sử

**Thời gian chạy:** 5 giây sau khi ứng dụng khởi động

### 2. Tự động tạo hàng tháng
Vào **00:01 ngày đầu tiên** của mỗi tháng, hệ thống sẽ tự động:
- Kiểm tra xem kỳ kế toán cho tháng mới đã tồn tại chưa
- Nếu chưa có, tạo kỳ mới với:
  - **Tên:** "Tháng MM/YYYY" (VD: "Tháng 12/2024")
  - **Từ ngày:** Ngày đầu tiên của tháng
  - **Đến ngày:** Ngày cuối cùng của tháng
  - **Trạng thái:** OPEN (Đang mở)

**Cron expression:** `0 1 0 1 * ?`
- Phút: 1
- Giờ: 0 (00:01 sáng)
- Ngày: 1 (ngày đầu tháng)
- Tháng: * (mọi tháng)
- Năm: ? (mọi năm)

## Nút "Tạo kỳ mới"

Nút tạo kỳ thủ công vẫn được giữ lại để:
- Tạo các **kỳ ngắn hơn** (tuần, quý, 6 tháng)
- Tạo các **kỳ đặc biệt** (kỳ kiểm toán, kỳ đóng sổ năm)
- Tạo **kỳ tùy chỉnh** với ngày bắt đầu/kết thúc theo nhu cầu

## Ví dụ

### Kỳ tự động (theo tháng)
```
Tháng 11/2024: 01/11/2024 - 30/11/2024
Tháng 12/2024: 01/12/2024 - 31/12/2024
Tháng 01/2025: 01/01/2025 - 31/01/2025
```

### Kỳ thủ công (tùy chỉnh)
```
Quý 4/2024: 01/10/2024 - 31/12/2024
Tuần 1 tháng 12: 01/12/2024 - 07/12/2024
Kiểm toán cuối năm: 15/12/2024 - 15/01/2025
```

## Logs

Kiểm tra logs để xem quá trình tự động tạo kỳ:

```
[INFO] Đã tự động tạo kỳ kế toán: Tháng 12/2024
[INFO] Kỳ kế toán tháng 12/2024 đã tồn tại
[INFO] Đã tạo kỳ kế toán cho tháng hiện tại khi khởi động: Tháng 12/2024
```

## File liên quan

- **Scheduler:** `src/main/java/com/doan/WEB_TMDT/module/accounting/scheduler/AccountingPeriodScheduler.java`
- **Entity:** `src/main/java/com/doan/WEB_TMDT/module/accounting/entity/AccountingPeriod.java`
- **Repository:** `src/main/java/com/doan/WEB_TMDT/module/accounting/repository/AccountingPeriodRepository.java`

## Lưu ý

1. **Không trùng lặp:** Hệ thống kiểm tra trước khi tạo để tránh tạo trùng kỳ
2. **Tự động chạy:** Không cần cấu hình thêm, chỉ cần restart backend
3. **Logging:** Mọi hoạt động đều được ghi log để theo dõi
4. **Timezone:** Sử dụng timezone của server (mặc định là UTC+7 cho Việt Nam)

## Kiểm tra

Sau khi restart backend:
1. Vào trang **Quản lý kỳ báo cáo** (Admin hoặc Employee)
2. Bạn sẽ thấy kỳ tháng hiện tại và tháng trước đã được tạo tự động
3. Vào đầu tháng sau, kỳ mới sẽ tự động xuất hiện
