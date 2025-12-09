# 🚀 Quick Start - Excel Import

## ✅ Đã sửa xong!

Lỗi syntax đã được khắc phục. Frontend có thể chạy bình thường.

## 📝 Cách sử dụng ngay

### Bước 1: Khởi động frontend
```bash
cd src/frontend
npm run dev
```

### Bước 2: Tạo file Excel

**Option A: Chỉ sản phẩm (Đơn giản)**

| SKU | Tên sản phẩm | Số lượng | Giá nhập | Bảo hành (tháng) | Ghi chú |
|-----|--------------|----------|----------|------------------|---------|
| IP15-128-BLK | iPhone 15 128GB Đen | 10 | 20000000 | 12 | Hàng mới |
| SS-S24-256-WHT | Samsung S24 256GB | 5 | 18000000 | 12 | |

**Option B: Đầy đủ NCC + Sản phẩm (Khuyến nghị)** ⭐

```
Dòng 1:  Nhà cung cấp | Công ty TNHH ABC
Dòng 2:  Mã số thuế | 0123456789
Dòng 3:  Người liên hệ | Nguyễn Văn A
Dòng 4:  Số điện thoại | 0901234567
Dòng 5:  Email | contact@abc.com
Dòng 6:  Địa chỉ | 123 Đường ABC, Q1, HCM
Dòng 7:  Tài khoản ngân hàng | 1234567890 - VCB
Dòng 8:  Điều khoản thanh toán | 30 ngày
Dòng 9:  [Để trống]
Dòng 10: SKU | Tên sản phẩm | Số lượng | Giá nhập | Bảo hành | Ghi chú
Dòng 11: IP15-128-BLK | iPhone 15 128GB Đen | 10 | 20000000 | 12 | Hàng mới
```

**Lưu ý:** 
- Giá không có dấu phẩy (20000000 không phải 20,000,000)
- Format B tự động điền thông tin NCC

### Bước 3: Import vào hệ thống
1. Mở trình duyệt: `http://localhost:3000`
2. Đăng nhập với tài khoản Admin
3. Vào: **Quản trị** → **Quản lý kho** → **Tạo phiếu nhập**
4. Click nút **📥 Import từ Excel** (màu xanh lá)
5. Chọn file `products.xlsx`
6. Kiểm tra danh sách sản phẩm
7. Click **Tạo phiếu**

## 🎯 Kết quả

### Format A (Chỉ sản phẩm):
- ✅ Sản phẩm được thêm tự động
- ❌ Phải nhập thủ công thông tin NCC

### Format B (Đầy đủ):
- ✅ Thông tin NCC được điền tự động
- ✅ Sản phẩm được thêm tự động
- ✅ Tiết kiệm 80% thời gian

**Cả 2 format đều:**
- ✅ Validate dữ liệu
- ✅ Hiển thị lỗi nếu có
- ✅ Có thể sửa sau khi import

## 📚 Tài liệu chi tiết

- **EXCEL-IMPORT-WITH-SUPPLIER.md** - ⭐ Hướng dẫn import cả NCC (MỚI)
- **EXCEL-EXAMPLE-LAYOUT.md** - Layout Excel chi tiết với ví dụ
- **EXCEL-TEMPLATE-GUIDE.md** - Hướng dẫn tạo file Excel chi tiết
- **EXCEL-IMPORT-GUIDE.md** - Hướng dẫn sử dụng chức năng
- **EXCEL-IMPORT-FIX-SUMMARY.md** - Chi tiết lỗi đã sửa

## ❓ Gặp vấn đề?

### Lỗi: "Không có dữ liệu hợp lệ"
→ Kiểm tra file có ít nhất 2 dòng (header + data)

### Lỗi: "SKU không được trống"
→ Kiểm tra cột A có giá trị

### Lỗi: "Số lượng phải > 0"
→ Kiểm tra cột C là số dương

### Lỗi: "Giá nhập phải > 0"
→ Kiểm tra cột D là số dương, không có dấu phẩy

---

**Chúc bạn sử dụng thành công!** 🎉
