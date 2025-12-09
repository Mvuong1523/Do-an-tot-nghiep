# 🎨 Excel Template Đẹp - Phiếu Nhập Kho

## 📊 Template với màu sắc và format đẹp

### Cách tạo file Excel đẹp:

---

## 🎨 PHẦN 1: THÔNG TIN NHÀ CUNG CẤP

### Màu sắc và format:

**Background:** Màu xanh dương nhạt `#E3F2FD`
**Font:** Arial 11pt, Bold cho cột A
**Border:** Viền đậm xung quanh

### Cấu trúc:

```
┌─────────────────────────────┬──────────────────────────────────────┐
│ Nhà cung cấp                │ Công ty TNHH Phân phối Di Động ABC   │
├─────────────────────────────┼──────────────────────────────────────┤
│ Mã số thuế                  │ 0123456789                           │
├─────────────────────────────┼──────────────────────────────────────┤
│ Người liên hệ               │ Nguyễn Văn A                         │
├─────────────────────────────┼──────────────────────────────────────┤
│ Số điện thoại               │ 0901234567                           │
├─────────────────────────────┼──────────────────────────────────────┤
│ Email                       │ contact@abc.com                      │
├─────────────────────────────┼──────────────────────────────────────┤
│ Địa chỉ                     │ 123 Đường Nguyễn Huệ, Q1, TP.HCM    │
├─────────────────────────────┼──────────────────────────────────────┤
│ Tài khoản ngân hàng         │ 1234567890 - Vietcombank             │
├─────────────────────────────┼──────────────────────────────────────┤
│ Điều khoản thanh toán       │ 30 ngày                              │
└─────────────────────────────┴──────────────────────────────────────┘
```

---

## 🎨 PHẦN 2: DÒNG PHÂN CÁCH

**Background:** Màu xám nhạt `#F5F5F5`
**Height:** 20px
**Để trống hoàn toàn**

---

## 🎨 PHẦN 3: DANH SÁCH SẢN PHẨM

### Header (Dòng 10):

**Background:** Màu cam nhạt `#FFF3E0`
**Font:** Arial 11pt, Bold
**Text align:** Center
**Border:** Viền đậm

```
┌──────────────┬─────────────────────────┬──────────┬──────────────┬──────────────┬────────────┐
│     SKU      │     Tên sản phẩm        │ Số lượng │   Giá nhập   │ Bảo hành (T) │  Ghi chú   │
└──────────────┴─────────────────────────┴──────────┴──────────────┴──────────────┴────────────┘
```

### Dữ liệu sản phẩm (Dòng 11+):

**Background:** Xen kẽ trắng và xám nhạt `#FAFAFA`
**Font:** Arial 10pt
**Border:** Viền mỏng
**Number format:** 
- Số lượng: `#,##0`
- Giá: `#,##0` (hoặc `#,##0 ₫`)

```
┌──────────────┬─────────────────────────┬──────────┬──────────────┬──────────────┬────────────┐
│ IP15-128-BLK │ iPhone 15 128GB Đen     │    10    │  20,000,000  │      12      │ Hàng mới   │
├──────────────┼─────────────────────────┼──────────┼──────────────┼──────────────┼────────────┤
│ SS-S24-256   │ Samsung S24 256GB Trắng │     5    │  18,000,000  │      12      │            │
├──────────────┼─────────────────────────┼──────────┼──────────────┼──────────────┼────────────┤
│ IP14-256-BLU │ iPhone 14 256GB Xanh    │     8    │  17,500,000  │      24      │ BH 2 năm   │
└──────────────┴─────────────────────────┴──────────┴──────────────┴──────────────┴────────────┘
```

---

## 🎯 Hướng dẫn tạo trong Excel

### Bước 1: Tạo phần Thông tin NCC

1. **Merge cells A1:A1** (không merge, giữ nguyên)
2. **Chọn A1:B8**
3. **Format:**
   - Fill color: `#E3F2FD` (Xanh dương nhạt)
   - Font: Arial 11pt
   - Bold cho cột A
   - Border: All borders (Thick)
4. **Nhập dữ liệu:**
   - A1: `Nhà cung cấp`, B1: `Công ty TNHH ABC`
   - A2: `Mã số thuế`, B2: `0123456789`
   - ... (tiếp tục)

### Bước 2: Tạo dòng phân cách

1. **Chọn dòng 9**
2. **Format:**
   - Fill color: `#F5F5F5` (Xám nhạt)
   - Row height: 20px
3. **Để trống**

### Bước 3: Tạo Header sản phẩm

1. **Chọn A10:F10**
2. **Format:**
   - Fill color: `#FFF3E0` (Cam nhạt)
   - Font: Arial 11pt, Bold
   - Text align: Center
   - Border: All borders (Thick)
3. **Nhập header:**
   - A10: `SKU`
   - B10: `Tên sản phẩm`
   - C10: `Số lượng`
   - D10: `Giá nhập`
   - E10: `Bảo hành (tháng)`
   - F10: `Ghi chú`

### Bước 4: Tạo dữ liệu sản phẩm

1. **Chọn A11:F100** (hoặc nhiều hơn)
2. **Format:**
   - Border: All borders (Thin)
   - Font: Arial 10pt
3. **Alternate row colors:**
   - Dòng lẻ: Trắng
   - Dòng chẵn: `#FAFAFA` (Xám rất nhạt)
4. **Number format:**
   - Cột C (Số lượng): `#,##0`
   - Cột D (Giá): `#,##0`
   - Cột E (Bảo hành): `#,##0`

### Bước 5: Điều chỉnh độ rộng cột

```
A (SKU):              15
B (Tên sản phẩm):     35
C (Số lượng):         12
D (Giá nhập):         15
E (Bảo hành):         15
F (Ghi chú):          20
```

---

## 🎨 Color Palette

### Màu sử dụng:

| Phần | Màu | Hex Code |
|------|-----|----------|
| Thông tin NCC | Xanh dương nhạt | `#E3F2FD` |
| Dòng phân cách | Xám nhạt | `#F5F5F5` |
| Header sản phẩm | Cam nhạt | `#FFF3E0` |
| Dòng chẵn | Xám rất nhạt | `#FAFAFA` |
| Dòng lẻ | Trắng | `#FFFFFF` |
| Border | Xám đậm | `#BDBDBD` |

---

## 📋 Copy-Paste Data (Với Tab)

Copy đoạn này vào Excel (dữ liệu đã có tab):

```
Nhà cung cấp	Công ty TNHH Phân phối Di Động ABC
Mã số thuế	0123456789
Người liên hệ	Nguyễn Văn A
Số điện thoại	0901234567
Email	contact@abc.com
Địa chỉ	123 Đường Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh
Tài khoản ngân hàng	1234567890 - Vietcombank Chi nhánh TP.HCM
Điều khoản thanh toán	30 ngày kể từ ngày nhận hàng
	
SKU	Tên sản phẩm	Số lượng	Giá nhập	Bảo hành (tháng)	Ghi chú
IP15-128-BLK	iPhone 15 128GB Đen	10	20000000	12	Hàng mới, nguyên seal
SS-S24-256-WHT	Samsung Galaxy S24 256GB Trắng	5	18000000	12	Hàng chính hãng
IP14-256-BLU	iPhone 14 256GB Xanh Dương	8	17500000	24	Bảo hành 2 năm
OPPO-R11-128-BLK	OPPO Reno 11 128GB Đen	15	8500000	12	
XIAOMI-13T-256-BLU	Xiaomi 13T 256GB Xanh	20	9500000	18	Bảo hành 18 tháng
```

Sau khi paste, áp dụng format màu sắc như hướng dẫn ở trên.

---

## 🎯 Conditional Formatting (Tùy chọn)

### Highlight giá cao:

1. Chọn cột D (Giá nhập)
2. Conditional Formatting > Color Scales
3. Chọn: Red - Yellow - Green
   - Đỏ: Giá cao
   - Vàng: Giá trung bình
   - Xanh: Giá thấp

### Highlight số lượng thấp:

1. Chọn cột C (Số lượng)
2. Conditional Formatting > Highlight Cell Rules > Less Than
3. Value: 5
4. Format: Light Red Fill

---

## 💡 Tips Format đẹp

### 1. Freeze Panes (Đóng băng)
- Chọn A11 (dòng đầu tiên của data)
- View > Freeze Panes > Freeze Panes
- Khi scroll xuống, header vẫn hiển thị

### 2. Auto Filter
- Chọn A10:F10 (header)
- Data > Filter
- Có thể filter/sort sản phẩm

### 3. Data Validation
- Chọn C11:C100 (Số lượng)
- Data > Data Validation
- Allow: Whole number, Minimum: 1
- Ngăn nhập số âm

### 4. Print Settings
- Page Layout > Orientation: Landscape
- Page Layout > Print Titles: Rows to repeat at top: $10:$10
- Margins: Narrow

---

## 📸 Preview

```
╔═══════════════════════════════════════════════════════════════════════════╗
║                    PHIẾU NHẬP KHO - IMPORT TEMPLATE                       ║
╠═══════════════════════════════════════════════════════════════════════════╣
║                                                                           ║
║  ┌─────────────────────────┬──────────────────────────────────────┐     ║
║  │ 📋 THÔNG TIN NHÀ CUNG CẤP (Màu xanh dương nhạt)                │     ║
║  ├─────────────────────────┼──────────────────────────────────────┤     ║
║  │ Nhà cung cấp            │ Công ty TNHH ABC                     │     ║
║  │ Mã số thuế              │ 0123456789                           │     ║
║  │ ...                     │ ...                                  │     ║
║  └─────────────────────────┴──────────────────────────────────────┘     ║
║                                                                           ║
║  ┌────────────────────────────────────────────────────────────────┐     ║
║  │ [Dòng trống - Màu xám nhạt]                                    │     ║
║  └────────────────────────────────────────────────────────────────┘     ║
║                                                                           ║
║  ┌──────┬─────────────┬────────┬──────────┬─────────┬──────────┐       ║
║  │ SKU  │ Tên SP      │ SL     │ Giá      │ BH      │ Ghi chú  │       ║
║  ├──────┼─────────────┼────────┼──────────┼─────────┼──────────┤       ║
║  │ IP15 │ iPhone 15   │   10   │ 20,000K  │   12    │ Hàng mới │       ║
║  │ SS24 │ Samsung S24 │    5   │ 18,000K  │   12    │          │       ║
║  └──────┴─────────────┴────────┴──────────┴─────────┴──────────┘       ║
║                                                                           ║
╚═══════════════════════════════════════════════════════════════════════════╝
```

---

## 🚀 Quick Start

### Cách nhanh nhất:

1. **Download template có sẵn** (nếu có)
2. **Hoặc copy đoạn text ở trên** vào Excel
3. **Áp dụng format màu sắc:**
   - A1:B8 → Xanh dương nhạt
   - Dòng 9 → Xám nhạt
   - A10:F10 → Cam nhạt, Bold, Center
   - A11:F100 → Border, alternate colors
4. **Điều chỉnh độ rộng cột**
5. **Lưu file `.xlsx`**
6. **Import vào hệ thống**

---

**File Excel đẹp và chuyên nghiệp!** 🎨✨
