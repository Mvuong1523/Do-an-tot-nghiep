# 📊 Hướng dẫn tạo file Excel Import

## 🎯 2 Định dạng hỗ trợ

### Format 1: Chỉ sản phẩm (Đơn giản)
File Excel chỉ có danh sách sản phẩm, thông tin NCC nhập thủ công.

### Format 2: Đầy đủ (Khuyến nghị)
File Excel có cả thông tin nhà cung cấp + danh sách sản phẩm.

---

## 📋 Format 1: Chỉ sản phẩm

File Excel cần có **6 cột** theo thứ tự sau:

| Cột | Tên cột | Bắt buộc | Kiểu dữ liệu | Ví dụ |
|-----|---------|----------|--------------|-------|
| A | SKU | ✅ Có | Text | IP15-128-BLK |
| B | Tên sản phẩm | ✅ Có | Text | iPhone 15 128GB Đen |
| C | Số lượng | ✅ Có | Số nguyên > 0 | 10 |
| D | Giá nhập | ✅ Có | Số > 0 | 20000000 |
| E | Bảo hành (tháng) | ❌ Không | Số nguyên | 12 |
| F | Ghi chú | ❌ Không | Text | Hàng mới |

### Ví dụ Format 1:

```
Dòng 1: SKU | Tên sản phẩm | Số lượng | Giá nhập | Bảo hành (tháng) | Ghi chú
Dòng 2: IP15-128-BLK | iPhone 15 128GB Đen | 10 | 20000000 | 12 | Hàng mới
Dòng 3: SS-S24-256-WHT | Samsung S24 256GB | 5 | 18000000 | 12 | 
```

---

## 📋 Format 2: Đầy đủ (Thông tin NCC + Sản phẩm)

### Phần 1: Thông tin nhà cung cấp (Dòng 1-8)

| Dòng | Cột A | Cột B |
|------|-------|-------|
| 1 | Nhà cung cấp | Công ty TNHH ABC |
| 2 | Mã số thuế | 0123456789 |
| 3 | Người liên hệ | Nguyễn Văn A |
| 4 | Số điện thoại | 0901234567 |
| 5 | Email | contact@abc.com |
| 6 | Địa chỉ | 123 Đường ABC, Quận 1, TP.HCM |
| 7 | Tài khoản ngân hàng | 1234567890 - Vietcombank |
| 8 | Điều khoản thanh toán | 30 ngày |

### Phần 2: Dòng trống (Dòng 9)
Để trống để phân cách

### Phần 3: Danh sách sản phẩm (Từ dòng 10)

| Cột | Tên cột | Bắt buộc | Kiểu dữ liệu | Ví dụ |
|-----|---------|----------|--------------|-------|
| A | SKU | ✅ Có | Text | IP15-128-BLK |
| B | Tên sản phẩm | ✅ Có | Text | iPhone 15 128GB Đen |
| C | Số lượng | ✅ Có | Số nguyên > 0 | 10 |
| D | Giá nhập | ✅ Có | Số > 0 | 20000000 |
| E | Bảo hành (tháng) | ❌ Không | Số nguyên | 12 |
| F | Ghi chú | ❌ Không | Text | Hàng mới |

### Ví dụ Format 2 (File hoàn chỉnh):

```
Dòng 1:  Nhà cung cấp | Công ty TNHH ABC
Dòng 2:  Mã số thuế | 0123456789
Dòng 3:  Người liên hệ | Nguyễn Văn A
Dòng 4:  Số điện thoại | 0901234567
Dòng 5:  Email | contact@abc.com
Dòng 6:  Địa chỉ | 123 Đường ABC, Quận 1, TP.HCM
Dòng 7:  Tài khoản ngân hàng | 1234567890 - Vietcombank
Dòng 8:  Điều khoản thanh toán | 30 ngày
Dòng 9:  [Để trống]
Dòng 10: SKU | Tên sản phẩm | Số lượng | Giá nhập | Bảo hành (tháng) | Ghi chú
Dòng 11: IP15-128-BLK | iPhone 15 128GB Đen | 10 | 20000000 | 12 | Hàng mới
Dòng 12: SS-S24-256-WHT | Samsung S24 256GB | 5 | 18000000 | 12 |
```

---

## 💡 Ưu điểm mỗi format

### Format 1 (Chỉ sản phẩm):
- ✅ Đơn giản, nhanh
- ✅ Dùng khi đã có NCC trong hệ thống
- ✅ Chỉ cần import sản phẩm

### Format 2 (Đầy đủ):
- ✅ Import một lần, đầy đủ thông tin
- ✅ Không cần nhập thủ công thông tin NCC
- ✅ Phù hợp với NCC mới
- ✅ Tiết kiệm thời gian nhập liệu

## Lưu ý quan trọng

### ✅ Đúng:
- Dòng đầu tiên là header (tên cột)
- SKU không được trùng lặp
- Số lượng và giá phải là số dương
- Giá nhập không có dấu phẩy, chấm (VD: 20000000 không phải 20,000,000)
- Bảo hành để trống sẽ mặc định là 12 tháng

### ❌ Sai:
- Không có header
- SKU để trống
- Số lượng = 0 hoặc âm
- Giá = 0 hoặc âm
- Giá có dấu phẩy (20,000,000)

## Cách tạo file Excel

### Cách 1: Microsoft Excel
1. Mở Excel
2. Tạo file mới
3. Nhập header vào dòng 1
4. Nhập dữ liệu từ dòng 2 trở đi
5. Lưu file với định dạng `.xlsx`

### Cách 2: Google Sheets
1. Mở Google Sheets
2. Tạo sheet mới
3. Nhập header vào dòng 1
4. Nhập dữ liệu từ dòng 2 trở đi
5. File > Download > Microsoft Excel (.xlsx)

### Cách 3: LibreOffice Calc
1. Mở LibreOffice Calc
2. Tạo file mới
3. Nhập header vào dòng 1
4. Nhập dữ liệu từ dòng 2 trở đi
5. Lưu file với định dạng `.xlsx`

## Cách sử dụng

1. Vào trang **Tạo phiếu nhập kho**
2. Click nút **📥 Import từ Excel**
3. Chọn file Excel đã chuẩn bị
4. Hệ thống sẽ tự động:
   - Đọc và validate dữ liệu
   - Hiển thị lỗi nếu có
   - Thêm sản phẩm vào danh sách
5. Kiểm tra lại danh sách sản phẩm
6. Có thể sửa hoặc xóa sản phẩm sau khi import
7. Click **Tạo phiếu** để hoàn tất

## Xử lý lỗi

### Lỗi thường gặp:

**"SKU không được trống"**
- Kiểm tra cột A có giá trị chưa

**"Số lượng phải > 0"**
- Kiểm tra cột C phải là số dương

**"Giá nhập phải > 0"**
- Kiểm tra cột D phải là số dương
- Không được có dấu phẩy hoặc ký tự đặc biệt

**"Không có dữ liệu hợp lệ trong file"**
- Kiểm tra file có ít nhất 2 dòng (header + data)
- Kiểm tra định dạng file là .xlsx hoặc .xls

**"File không được vượt quá 5MB"**
- Giảm số lượng dòng hoặc tối ưu file

## Tips

💡 **Copy từ website khác:**
- Copy bảng sản phẩm từ website nhà cung cấp
- Paste vào Excel
- Chỉnh sửa cho đúng format
- Import vào hệ thống

💡 **Sử dụng công thức Excel:**
- Tính giá tự động: `=C2*1.1` (giá gốc + 10%)
- Tạo SKU tự động: `=CONCATENATE("SKU-", A2)`

💡 **Kiểm tra trước khi import:**
- Sắp xếp theo SKU để tìm trùng lặp
- Dùng filter để kiểm tra giá trị rỗng
- Dùng conditional formatting để highlight lỗi

## 📥 File mẫu

### Mẫu Format 1 (Chỉ sản phẩm):

| SKU | Tên sản phẩm | Số lượng | Giá nhập | Bảo hành (tháng) | Ghi chú |
|-----|--------------|----------|----------|------------------|---------|
| IP15-128-BLK | iPhone 15 128GB Đen | 10 | 20000000 | 12 | Hàng mới |
| SS-S24-256-WHT | Samsung S24 256GB | 5 | 18000000 | 12 | |
| IP14-256-BLU | iPhone 14 256GB Xanh | 8 | 17500000 | 24 | Bảo hành 2 năm |

### Mẫu Format 2 (Đầy đủ):

**Thông tin NCC:**
| | |
|---|---|
| Nhà cung cấp | Công ty TNHH Phân phối ABC |
| Mã số thuế | 0123456789 |
| Người liên hệ | Nguyễn Văn A |
| Số điện thoại | 0901234567 |
| Email | contact@abc.com |
| Địa chỉ | 123 Đường ABC, Quận 1, TP.HCM |
| Tài khoản ngân hàng | 1234567890 - Vietcombank |
| Điều khoản thanh toán | 30 ngày |

**[Dòng trống]**

**Danh sách sản phẩm:**
| SKU | Tên sản phẩm | Số lượng | Giá nhập | Bảo hành (tháng) | Ghi chú |
|-----|--------------|----------|----------|------------------|---------|
| IP15-128-BLK | iPhone 15 128GB Đen | 10 | 20000000 | 12 | Hàng mới |
| SS-S24-256-WHT | Samsung S24 256GB | 5 | 18000000 | 12 | |
| IP14-256-BLU | iPhone 14 256GB Xanh | 8 | 17500000 | 24 | Bảo hành 2 năm |

---

Chúc bạn import thành công! 🎉
