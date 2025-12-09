# 📊 Import Excel với thông tin Nhà cung cấp

## ✨ Tính năng mới

Giờ đây bạn có thể import **cả thông tin nhà cung cấp** cùng với danh sách sản phẩm trong 1 file Excel duy nhất!

## 🎯 2 Cách sử dụng

### Cách 1: Import chỉ sản phẩm
- File Excel chỉ có danh sách sản phẩm
- Thông tin NCC nhập thủ công hoặc chọn từ dropdown

### Cách 2: Import đầy đủ (MỚI) ⭐
- File Excel có cả thông tin NCC + danh sách sản phẩm
- Hệ thống tự động điền tất cả thông tin
- Tiết kiệm thời gian nhập liệu

---

## 📋 Format Excel đầy đủ

### Cấu trúc file:

```
┌─────────────────────────────────────────────┐
│ PHẦN 1: THÔNG TIN NHÀ CUNG CẤP (Dòng 1-8)  │
├─────────────────────────────────────────────┤
│ Dòng 1:  Nhà cung cấp | [Tên công ty]       │
│ Dòng 2:  Mã số thuế | [MST]                 │
│ Dòng 3:  Người liên hệ | [Tên người]        │
│ Dòng 4:  Số điện thoại | [SĐT]              │
│ Dòng 5:  Email | [Email]                    │
│ Dòng 6:  Địa chỉ | [Địa chỉ đầy đủ]         │
│ Dòng 7:  Tài khoản ngân hàng | [STK - NH]   │
│ Dòng 8:  Điều khoản thanh toán | [30 ngày]  │
├─────────────────────────────────────────────┤
│ DÒNG 9: [ĐỂ TRỐNG]                          │
├─────────────────────────────────────────────┤
│ PHẦN 2: DANH SÁCH SẢN PHẨM (Từ dòng 10)    │
├─────────────────────────────────────────────┤
│ Dòng 10: SKU | Tên SP | SL | Giá | BH | GC  │
│ Dòng 11: [Sản phẩm 1]                       │
│ Dòng 12: [Sản phẩm 2]                       │
│ ...                                          │
└─────────────────────────────────────────────┘
```

### Ví dụ cụ thể:

| Cột A | Cột B | Cột C | Cột D | Cột E | Cột F |
|-------|-------|-------|-------|-------|-------|
| **Nhà cung cấp** | Công ty TNHH ABC | | | | |
| **Mã số thuế** | 0123456789 | | | | |
| **Người liên hệ** | Nguyễn Văn A | | | | |
| **Số điện thoại** | 0901234567 | | | | |
| **Email** | contact@abc.com | | | | |
| **Địa chỉ** | 123 Đường ABC, Q1, HCM | | | | |
| **Tài khoản ngân hàng** | 1234567890 - VCB | | | | |
| **Điều khoản thanh toán** | 30 ngày | | | | |
| | | | | | |
| **SKU** | **Tên sản phẩm** | **Số lượng** | **Giá nhập** | **Bảo hành** | **Ghi chú** |
| IP15-128-BLK | iPhone 15 128GB Đen | 10 | 20000000 | 12 | Hàng mới |
| SS-S24-256 | Samsung S24 256GB | 5 | 18000000 | 12 | |

---

## 🚀 Cách sử dụng

### Bước 1: Tạo file Excel

**Option A: Tạo từ đầu**
1. Mở Excel/Google Sheets
2. Copy cấu trúc ở trên
3. Điền thông tin NCC vào dòng 1-8
4. Để trống dòng 9
5. Điền header sản phẩm vào dòng 10
6. Điền danh sách sản phẩm từ dòng 11
7. Lưu file `.xlsx`

**Option B: Xuất từ hệ thống NCC**
1. Lấy thông tin NCC từ email/website
2. Copy vào Excel theo format
3. Thêm danh sách sản phẩm

### Bước 2: Import vào hệ thống

1. Vào trang **Tạo phiếu nhập kho**
2. Click nút **📥 Import từ Excel** (màu xanh lá)
3. Chọn file Excel đã chuẩn bị
4. Hệ thống tự động:
   - ✅ Điền thông tin nhà cung cấp
   - ✅ Thêm danh sách sản phẩm
   - ✅ Validate dữ liệu
5. Kiểm tra lại thông tin
6. Click **Tạo phiếu** để hoàn tất

---

## 🎯 Kết quả

### Khi import thành công:

**Thông báo:**
```
✅ Đã import thông tin NCC và 10 sản phẩm
```

**Form được điền tự động:**
- ✅ Tên nhà cung cấp
- ✅ Mã số thuế
- ✅ Người liên hệ
- ✅ Số điện thoại
- ✅ Email
- ✅ Địa chỉ
- ✅ Tài khoản ngân hàng
- ✅ Điều khoản thanh toán
- ✅ Danh sách sản phẩm (SKU, tên, số lượng, giá, bảo hành, ghi chú)

---

## 📝 Lưu ý quan trọng

### ✅ Đúng:

1. **Thông tin NCC phải ở đầu file (dòng 1-8)**
   - Dòng 1 bắt đầu bằng "Nhà cung cấp" hoặc "Supplier"
   - Thông tin ở cột B (cột A là label)

2. **Dòng 9 phải để trống**
   - Để phân cách giữa NCC và sản phẩm

3. **Header sản phẩm ở dòng 10**
   - SKU | Tên sản phẩm | Số lượng | Giá nhập | Bảo hành | Ghi chú

4. **Dữ liệu sản phẩm từ dòng 11 trở đi**

### ❌ Sai:

1. ❌ Không có dòng "Nhà cung cấp" ở đầu
2. ❌ Thông tin NCC không đúng thứ tự
3. ❌ Không có dòng trống giữa NCC và sản phẩm
4. ❌ Header sản phẩm không đúng vị trí

---

## 🔄 So sánh 2 format

### Format 1: Chỉ sản phẩm

```
Dòng 1: SKU | Tên SP | Số lượng | Giá | BH | Ghi chú
Dòng 2: IP15-128 | iPhone 15 | 10 | 20000000 | 12 | 
```

**Kết quả:**
- ✅ Import 1 sản phẩm
- ❌ Phải nhập thủ công thông tin NCC

### Format 2: Đầy đủ (Khuyến nghị)

```
Dòng 1-8: Thông tin NCC
Dòng 9: [Trống]
Dòng 10: Header sản phẩm
Dòng 11+: Danh sách sản phẩm
```

**Kết quả:**
- ✅ Import thông tin NCC tự động
- ✅ Import danh sách sản phẩm
- ✅ Tiết kiệm thời gian

---

## 💡 Tips

### 1. Tạo template cho từng NCC
- Lưu file Excel mẫu cho mỗi NCC
- Chỉ cần cập nhật danh sách sản phẩm
- Import nhanh chóng

### 2. Copy từ email NCC
- NCC gửi báo giá qua email
- Copy thông tin vào Excel
- Import trực tiếp

### 3. Sử dụng công thức Excel
- Tính giá tự động: `=D11*1.1` (giá + 10%)
- Tạo SKU: `=CONCATENATE("SKU-", A11)`

### 4. Kiểm tra trước khi import
- Dùng Find & Replace để chuẩn hóa
- Dùng Filter để tìm dòng trống
- Dùng Conditional Formatting để highlight lỗi

---

## ❓ Xử lý lỗi

### "Không có dữ liệu hợp lệ"
→ Kiểm tra file có ít nhất 11 dòng (8 NCC + 1 trống + 1 header + 1 data)

### "SKU không được trống"
→ Kiểm tra cột A từ dòng 11 trở đi có giá trị

### "Mã số thuế không được trống"
→ Kiểm tra dòng 2, cột B có giá trị

### Thông tin NCC không được điền
→ Kiểm tra dòng 1, cột A có chữ "Nhà cung cấp"

---

## 🎉 Ví dụ thực tế

### Tình huống: Nhập hàng từ NCC mới

**Trước đây:**
1. Mở trang tạo phiếu
2. Nhập thủ công 8 trường thông tin NCC
3. Thêm từng sản phẩm một (hoặc import riêng)
4. Tổng thời gian: ~10 phút

**Bây giờ:**
1. Tạo file Excel với thông tin NCC + sản phẩm
2. Click "Import từ Excel"
3. Chọn file
4. Kiểm tra và tạo phiếu
5. Tổng thời gian: ~2 phút

**Tiết kiệm: 80% thời gian!** ⚡

---

## 📚 Tài liệu liên quan

- **EXCEL-TEMPLATE-GUIDE.md** - Hướng dẫn chi tiết format Excel
- **EXCEL-IMPORT-GUIDE.md** - Hướng dẫn sử dụng chức năng import
- **QUICK-START-EXCEL-IMPORT.md** - Quick start guide

---

**Chúc bạn import thành công!** 🎊
