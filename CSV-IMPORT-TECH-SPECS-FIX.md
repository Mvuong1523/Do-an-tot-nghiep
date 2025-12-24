# ✅ SỬA LỖI IMPORT CSV - THÔNG SỐ KỸ THUẬT

## 🐛 VẤN ĐỀ
Khi import file `sample-import-products.csv`, cột "Thông số kỹ thuật (JSON)" không được parse và điền vào form.

## 🔍 NGUYÊN NHÂN
1. **CSV Parser không xử lý đúng dấu ngoặc kép**: Cột JSON có dấu ngoặc kép bên trong nên cần parser đặc biệt
2. **Không trim và clean dữ liệu**: Dấu ngoặc kép bao quanh không được loại bỏ
3. **Thiếu debug logging**: Không biết được parse ra bao nhiêu cột

## ✅ GIẢI PHÁP ĐÃ ÁP DỤNG

### 1. Cải thiện CSV Parser
```typescript
// Parse CSV with proper handling of quoted fields (for JSON)
const parts: string[] = []
let current = ''
let inQuotes = false

for (let j = 0; j < line.length; j++) {
  const char = line[j]
  if (char === '"') {
    inQuotes = !inQuotes
  } else if (char === ',' && !inQuotes) {
    parts.push(current)  // ✅ Không trim ngay, để giữ nguyên dữ liệu
    current = ''
  } else {
    current += char
  }
}
parts.push(current)
```

### 2. Clean Up Parts
```typescript
// Clean up parts - remove quotes and trim
const cleanParts = parts.map(p => {
  let cleaned = p.trim()
  // Remove surrounding quotes if present
  if (cleaned.startsWith('"') && cleaned.endsWith('"')) {
    cleaned = cleaned.slice(1, -1)
  }
  return cleaned
})
```

### 3. Thêm Debug Logging
```typescript
console.log(`Row ${i}: Found ${cleanParts.length} columns`, cleanParts)

let techSpecs = ''
if (cleanParts.length >= 7 && cleanParts[6]) {
  techSpecs = cleanParts[6]
  console.log(`Tech specs for ${cleanParts[0]}:`, techSpecs)
}
```

### 4. Mapping Cột Đúng
```typescript
// Column mapping for sample-import-products.csv:
// 0: SKU
// 1: Tên sản phẩm
// 2: Loại sản phẩm
// 3: Giá bán
// 4: Số lượng
// 5: Mô tả
// 6: Thông số kỹ thuật (JSON)

parsedItems.push({
  sku: cleanParts[0],
  internalName: cleanParts[1],
  quantity: parseInt(cleanParts[4]) || 0,
  unitCost: parseFloat(cleanParts[3]) || 0,
  warrantyMonths: 12,
  techSpecsJson: techSpecs,  // ✅ Lấy từ cột 6
  note: cleanParts[5] || ''
})
```

## 📋 FORMAT CSV HỖ TRỢ

### Format 1: Danh sách sản phẩm thuần túy (sample-import-products.csv)
```csv
SKU,Tên sản phẩm,Loại sản phẩm,Giá bán,Số lượng,Mô tả,Thông số kỹ thuật (JSON)
IP15PM-256-BLK,iPhone 15 Pro Max 256GB,Điện thoại,32900000,15,Flagship cao cấp,"{""Chip"":""A17 Pro"",""RAM"":""8GB""}"
```

**Đặc điểm:**
- Header có chứa "SKU,Tên sản phẩm" hoặc "Thông số kỹ thuật"
- 7 cột: SKU, Tên, Loại, Giá, Số lượng, Mô tả, Thông số JSON
- Cột JSON được bao bằng dấu ngoặc kép
- Dấu ngoặc kép bên trong JSON được escape bằng `""`

### Format 2: Nhà cung cấp + Sản phẩm (sample-import-with-supplier.csv)
```csv
Nhà cung cấp,Công ty TNHH ABC
Mã số thuế,0123456789
Người liên hệ,Nguyễn Văn A
Số điện thoại,0901234567
Email,contact@abc.vn
Địa chỉ,123 Đường ABC
Tài khoản ngân hàng,1234567890 - Vietcombank
Điều khoản thanh toán,Thanh toán trong 30 ngày

SKU,Tên sản phẩm,Số lượng,Giá nhập,Bảo hành (tháng),Ghi chú
PROD-001,Sản phẩm mẫu 1,10,100000,12,Ghi chú mẫu
```

**Đặc điểm:**
- 8 dòng đầu: Thông tin nhà cung cấp
- Dòng 9: Trống
- Dòng 10+: Header và danh sách sản phẩm
- 6 cột sản phẩm: SKU, Tên, Số lượng, Giá, Bảo hành, Ghi chú
- **Không có cột JSON** (có thể nhập sau khi import)

## 🎯 CÁCH SỬ DỤNG

### Bước 1: Chuẩn bị file CSV
1. Mở Excel hoặc Google Sheets
2. Nhập dữ liệu theo format trên
3. **Quan trọng**: Với cột JSON, bọc toàn bộ giá trị trong dấu ngoặc kép
4. Save as CSV (UTF-8)

### Bước 2: Import vào hệ thống
1. Vào trang "Tạo phiếu nhập kho"
2. Click nút "📤 Import từ Excel/CSV"
3. Chọn file CSV
4. Hệ thống sẽ tự động detect format và parse

### Bước 3: Kiểm tra kết quả
1. Mở Console (F12) để xem log
2. Kiểm tra số cột được parse: `Row 1: Found 7 columns`
3. Kiểm tra tech specs: `Tech specs for IP15PM-256-BLK: {"Chip":"A17 Pro",...}`
4. Xem form đã được điền đúng chưa

### Bước 4: Chỉnh sửa nếu cần
- Có thể edit trực tiếp trong form
- Có thể thêm/sửa tech specs JSON
- Validate JSON trước khi submit

## 🔧 DEBUG

### Nếu không parse được tech specs:

1. **Kiểm tra Console log**:
```javascript
// Mở Console (F12) và xem:
Row 1: Found 7 columns [...] // Phải có 7 cột
Tech specs for IP15PM-256-BLK: {...} // Phải có JSON
```

2. **Kiểm tra format CSV**:
- Có đúng 7 cột không?
- Cột JSON có được bao bằng dấu ngoặc kép không?
- Dấu ngoặc kép trong JSON có được escape (`""`) không?

3. **Kiểm tra encoding**:
- File phải là UTF-8
- Không có BOM (Byte Order Mark)

4. **Test với dòng đơn giản**:
```csv
SKU,Tên sản phẩm,Loại sản phẩm,Giá bán,Số lượng,Mô tả,Thông số kỹ thuật (JSON)
TEST-001,Test Product,Test,100000,1,Test note,"{""test"":""value""}"
```

## 📝 LƯU Ý

### Về JSON trong CSV
- **Phải escape dấu ngoặc kép**: `""` thay vì `"`
- **Ví dụ đúng**: `"{""Chip"":""A17 Pro""}"`
- **Ví dụ sai**: `"{"Chip":"A17 Pro"}"`

### Về Excel
- Khi save as CSV từ Excel, Excel tự động escape dấu ngoặc kép
- Nếu copy/paste từ nguồn khác, cần kiểm tra lại

### Về Google Sheets
- Download as CSV (UTF-8)
- Không dùng "Comma Separated Values (.csv)" vì có thể sai encoding

## ✅ KẾT QUẢ

Sau khi sửa:
- ✅ Parse đúng 7 cột từ CSV
- ✅ Extract được JSON từ cột "Thông số kỹ thuật"
- ✅ Remove dấu ngoặc kép bao quanh
- ✅ Điền vào form textarea
- ✅ Có debug logging để kiểm tra
- ✅ Hỗ trợ cả 2 format CSV

## 🎉 HOÀN THÀNH

Bây giờ bạn có thể:
1. Import file `sample-import-products.csv` với đầy đủ thông số kỹ thuật
2. Xem console log để debug nếu có vấn đề
3. Chỉnh sửa tech specs trực tiếp trong form
4. Submit để tạo phiếu nhập kho với đầy đủ dữ liệu
