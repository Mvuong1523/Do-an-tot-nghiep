# Database Cleanup Guide

## 🎯 Vấn đề

Database có nhiều đơn hàng `PENDING` cũ từ lúc test, gây khó khăn trong việc:
- Phân biệt đơn thật vs đơn test
- Thống kê không chính xác
- UI hiển thị nhiều đơn "rác"

## 🔧 Giải pháp

### Bước 1: Backup Database (BẮT BUỘC!)

```bash
# Backup toàn bộ database
mysqldump -u root -p web2 > backup_web2_20251127.sql

# Hoặc chỉ backup bảng orders
mysqldump -u root -p web2 orders > backup_orders_20251127.sql
```

### Bước 2: Chạy Migration chính

```bash
# Thêm PENDING_PAYMENT status và payment_method column
mysql -u root -p web2 < migration_add_pending_payment_status.sql
```

### Bước 3: Cập nhật payment_method cho đơn cũ

```bash
# Phân loại đơn cũ: COD hay Online
mysql -u root -p web2 < migrate_old_orders_payment_method.sql
```

### Bước 4: Cleanup đơn test (Chọn 1 trong 4 options)

```bash
# Xem trước và cleanup
mysql -u root -p web2 < cleanup_old_pending_orders.sql
```

## 📋 Chi tiết các Options

### Option 1: XÓA tất cả đơn PENDING (Nhanh nhất)

**Ưu điểm:**
- ✅ Database sạch ngay
- ✅ Không có dữ liệu rác

**Nhược điểm:**
- ❌ Mất hết lịch sử
- ❌ Không thể rollback

**Khi nào dùng:**
- Database test/development
- Chắc chắn không cần lịch sử

**SQL:**
```sql
DELETE FROM orders WHERE status = 'PENDING';
```

### Option 2: Chuyển sang CANCELLED (Giữ lịch sử)

**Ưu điểm:**
- ✅ Giữ lại lịch sử
- ✅ Có thể xem lại sau
- ✅ Thống kê đầy đủ

**Nhược điểm:**
- ❌ Database vẫn có nhiều dữ liệu
- ❌ Cần filter khi query

**Khi nào dùng:**
- Production database
- Cần audit trail

**SQL:**
```sql
UPDATE orders 
SET 
    status = 'CANCELLED',
    cancelled_at = NOW(),
    cancel_reason = 'Tự động hủy - Đơn test cũ'
WHERE status = 'PENDING'
  AND created_at < DATE_SUB(NOW(), INTERVAL 1 DAY);
```

### Option 3: Xóa chọn lọc (An toàn)

**Ưu điểm:**
- ✅ Chỉ xóa đơn test rõ ràng
- ✅ Giữ đơn có thể còn dùng
- ✅ An toàn hơn

**Nhược điểm:**
- ❌ Phức tạp hơn
- ❌ Cần hiểu logic

**Khi nào dùng:**
- Không chắc đơn nào là test
- Muốn cẩn thận

**SQL:**
```sql
-- Xóa đơn COD test (không có payment)
DELETE FROM orders 
WHERE status = 'PENDING'
  AND payment_id IS NULL
  AND created_at < DATE_SUB(NOW(), INTERVAL 1 DAY);

-- Xóa đơn Online expired
DELETE o FROM orders o
LEFT JOIN payments p ON o.payment_id = p.id
WHERE o.status = 'PENDING'
  AND p.status = 'EXPIRED';
```

### Option 4: Backup trước khi xóa (An toàn nhất)

**Ưu điểm:**
- ✅ Có thể rollback
- ✅ Không mất dữ liệu
- ✅ An toàn tuyệt đối

**Nhược điểm:**
- ❌ Tốn storage
- ❌ Nhiều bước hơn

**Khi nào dùng:**
- Production database
- Không chắc chắn 100%

**SQL:**
```sql
-- Backup
CREATE TABLE orders_backup_20251127 AS
SELECT * FROM orders WHERE status = 'PENDING';

-- Xóa
DELETE FROM orders WHERE status = 'PENDING';

-- Rollback (nếu cần)
INSERT INTO orders SELECT * FROM orders_backup_20251127;
```

## 🎯 Khuyến nghị

### Cho Development/Test:
```bash
# 1. Backup
mysqldump -u root -p web2 > backup_web2.sql

# 2. Xóa sạch
mysql -u root -p web2 -e "DELETE FROM orders WHERE status = 'PENDING';"

# 3. Restart app và test lại
```

### Cho Production:
```bash
# 1. Backup
mysqldump -u root -p web2 > backup_web2_$(date +%Y%m%d).sql

# 2. Chuyển sang CANCELLED
mysql -u root -p web2 -e "
UPDATE orders 
SET status = 'CANCELLED', 
    cancelled_at = NOW(),
    cancel_reason = 'Cleanup - Old pending orders'
WHERE status = 'PENDING' 
  AND created_at < DATE_SUB(NOW(), INTERVAL 7 DAY);
"

# 3. Verify
mysql -u root -p web2 -e "
SELECT status, COUNT(*) 
FROM orders 
GROUP BY status;
"
```

## 📊 Kiểm tra sau khi cleanup

```sql
-- 1. Thống kê tổng quan
SELECT 
    status,
    payment_method,
    COUNT(*) as total,
    SUM(total) as revenue
FROM orders
GROUP BY status, payment_method
ORDER BY status, payment_method;

-- 2. Xem đơn mới nhất
SELECT * FROM orders 
ORDER BY created_at DESC 
LIMIT 10;

-- 3. Kiểm tra còn đơn PENDING không
SELECT COUNT(*) as remaining_pending
FROM orders 
WHERE status = 'PENDING';
```

## ⚠️ Lưu ý

1. **LUÔN BACKUP** trước khi xóa dữ liệu
2. **Test trên local** trước khi chạy production
3. **Verify kết quả** sau mỗi bước
4. **Có kế hoạch rollback** nếu có vấn đề
5. **Thông báo team** trước khi cleanup production

## 🔄 Rollback

Nếu có vấn đề:

```bash
# Restore từ backup
mysql -u root -p web2 < backup_web2_20251127.sql

# Hoặc chỉ restore bảng orders
mysql -u root -p web2 -e "DROP TABLE orders;"
mysql -u root -p web2 < backup_orders_20251127.sql
```

## 📝 Checklist

- [ ] Backup database
- [ ] Chạy migration chính
- [ ] Cập nhật payment_method
- [ ] Cleanup đơn test
- [ ] Verify kết quả
- [ ] Test app hoạt động bình thường
- [ ] Xóa backup cũ (sau 1 tuần)

Happy cleaning! 🧹
