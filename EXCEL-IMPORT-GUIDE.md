# 📊 Hướng dẫn Import Excel - Phiếu nhập kho

## ✅ Đã chuẩn bị

Chức năng import Excel cho phiếu nhập kho - chỉ xử lý ở frontend, không cần backend.

📖 **Xem hướng dẫn chi tiết tạo file Excel:** [EXCEL-TEMPLATE-GUIDE.md](./EXCEL-TEMPLATE-GUIDE.md)

## 🔧 Cài đặt

```bash
cd src/frontend
npm install xlsx
```

## 📋 Format file Excel mẫu

File Excel cần có các cột sau:

| SKU | Tên sản phẩm | Số lượng | Giá nhập | Bảo hành (tháng) | Ghi chú |
|-----|--------------|----------|----------|------------------|---------|
| IP15-128-BLK | iPhone 15 128GB Đen | 10 | 20000000 | 12 | Hàng mới |
| SS-S24-256-WHT | Samsung S24 256GB Trắng | 5 | 18000000 | 12 | |

**Lưu ý:**
- Dòng đầu tiên là header (tên cột)
- SKU: Bắt buộc, unique
- Tên sản phẩm: Bắt buộc
- Số lượng: Bắt buộc, số nguyên > 0
- Giá nhập: Bắt buộc, số > 0
- Bảo hành: Tùy chọn, mặc định 12 tháng
- Ghi chú: Tùy chọn

## 🎯 Cách sử dụng

### Bước 1: Chuẩn bị file Excel
1. Tạo file Excel (.xlsx) theo format trên
2. Điền thông tin sản phẩm cần nhập kho

### Bước 2: Import vào hệ thống
1. Vào trang **Tạo phiếu nhập kho**
2. Click nút **"📥 Import từ Excel"**
3. Chọn file Excel đã chuẩn bị
4. Hệ thống tự động:
   - Đọc file Excel
   - Validate dữ liệu
   - Điền vào form

### Bước 3: Kiểm tra và submit
1. Kiểm tra danh sách sản phẩm đã import
2. Sửa nếu cần
3. Điền thông tin nhà cung cấp
4. Click **"Tạo phiếu nhập"**

## 💻 Code đã thêm

### 1. Component ExcelImport
File: `src/frontend/components/ExcelImport.tsx`

```tsx
import * as XLSX from 'xlsx'

// Component để upload và parse Excel
// Trả về array of items
```

### 2. Tích hợp vào trang create
File: `src/frontend/app/admin/inventory/transactions/create/page.tsx`

Thêm:
- Nút "Import từ Excel"
- Logic xử lý file Excel
- Auto-fill form với dữ liệu từ Excel

## 🔍 Validate dữ liệu

Hệ thống tự động validate:
- ✅ SKU không được trống
- ✅ Tên sản phẩm không được trống
- ✅ Số lượng phải > 0
- ✅ Giá nhập phải > 0
- ✅ Bảo hành phải là số (nếu có)
- ❌ Bỏ qua dòng có lỗi và hiện thông báo

## 📝 Ví dụ file Excel

Tạo file `import-template.xlsx` với nội dung:

```
SKU              | Tên sản phẩm           | Số lượng | Giá nhập  | Bảo hành | Ghi chú
IP15-128-BLK     | iPhone 15 128GB Đen    | 10       | 20000000  | 12       | Hàng mới
IP15-256-WHT     | iPhone 15 256GB Trắng  | 5        | 23000000  | 12       |
SS-S24-256-BLK   | Samsung S24 256GB      | 8        | 18000000  | 12       | Màu đen
```

## 🚀 Ưu điểm

✅ **Nhanh:** Import hàng trăm sản phẩm cùng lúc  
✅ **Dễ dùng:** Chỉ cần file Excel  
✅ **Không cần backend:** Xử lý hoàn toàn ở frontend  
✅ **Validate:** Tự động kiểm tra dữ liệu  
✅ **Preview:** Xem trước trước khi submit  

## ⚠️ Lưu ý

- File Excel phải có đúng format (header ở dòng 1)
- Dữ liệu bắt đầu từ dòng 2
- SKU phải unique trong file
- Số lượng và giá phải là số hợp lệ
- File không quá 5MB

## 🐛 Troubleshooting

**Lỗi: "Không đọc được file"**
→ Kiểm tra file có đúng format .xlsx không

**Lỗi: "Dữ liệu không hợp lệ"**
→ Kiểm tra các cột bắt buộc đã điền đủ chưa

**Lỗi: "SKU trùng lặp"**
→ Mỗi SKU chỉ xuất hiện 1 lần trong file
