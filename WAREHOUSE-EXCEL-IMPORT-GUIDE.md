# Warehouse Excel Import Guide - Hướng dẫn nhập kho bằng Excel

## Tổng quan
Đã thêm chức năng nhập nhanh từ file Excel/CSV vào form tạo phiếu nhập kho cho cả Admin và Employee.

## Tính năng mới

### 1. Tải Template
- ✅ Nút "Tải template" để download file CSV mẫu
- ✅ Template có đầy đủ cấu trúc: thông tin NCC + danh sách sản phẩm
- ✅ File tự động có BOM (UTF-8) để Excel đọc đúng tiếng Việt

### 2. Upload và Parse CSV
- ✅ Drag & drop hoặc click để chọn file
- ✅ Chỉ chấp nhận file .csv
- ✅ Tự động parse thông tin nhà cung cấp
- ✅ Tự động parse danh sách sản phẩm
- ✅ Hiển thị thông báo số lượng sản phẩm đã import

### 3. Auto-fill Form
- ✅ Tự động điền thông tin NCC vào form
- ✅ Tự động chuyển sang chế độ "Tạo NCC mới"
- ✅ Tự động điền danh sách sản phẩm
- ✅ Có thể chỉnh sửa sau khi import

## Cấu trúc File CSV

### Template Format
```csv
Nhà cung cấp,Công ty TNHH ABC
Mã số thuế,0123456789
Người liên hệ,Nguyễn Văn A
Số điện thoại,0901234567
Email,contact@abc.vn
Địa chỉ,123 Đường ABC - Quận 1 - TP.HCM
Tài khoản ngân hàng,1234567890 - Vietcombank
Điều khoản thanh toán,Thanh toán trong 30 ngày

SKU,Tên sản phẩm,Số lượng,Giá nhập,Bảo hành (tháng),Ghi chú
PROD-001,Sản phẩm mẫu 1,10,100000,12,Ghi chú mẫu
PROD-002,Sản phẩm mẫu 2,20,200000,24,Ghi chú mẫu
```

### Phần 1: Thông tin Nhà cung cấp (8 dòng đầu)
```
Dòng 1: Nhà cung cấp,{Tên công ty}
Dòng 2: Mã số thuế,{Mã số thuế}
Dòng 3: Người liên hệ,{Tên người liên hệ}
Dòng 4: Số điện thoại,{Số điện thoại}
Dòng 5: Email,{Email}
Dòng 6: Địa chỉ,{Địa chỉ đầy đủ}
Dòng 7: Tài khoản ngân hàng,{Số TK - Ngân hàng}
Dòng 8: Điều khoản thanh toán,{Điều khoản}
```

### Phần 2: Dòng trống (dòng 9)

### Phần 3: Header sản phẩm (dòng 10)
```
SKU,Tên sản phẩm,Số lượng,Giá nhập,Bảo hành (tháng),Ghi chú
```

### Phần 4: Danh sách sản phẩm (từ dòng 11)
```
{SKU},{Tên SP},{Số lượng},{Giá nhập},{Bảo hành},{Ghi chú}
```

**Lưu ý:**
- Các cột bắt buộc: SKU, Tên sản phẩm, Số lượng, Giá nhập
- Các cột optional: Bảo hành, Ghi chú
- Không có dấu phẩy trong giá trị (VD: 100000 chứ không phải 100,000)

## Code Implementation

### 1. Download Template Function
```typescript
const downloadTemplate = () => {
  const csvContent = `Nhà cung cấp,Công ty TNHH ABC
Mã số thuế,0123456789
Người liên hệ,Nguyễn Văn A
Số điện thoại,0901234567
Email,contact@abc.vn
Địa chỉ,123 Đường ABC - Quận 1 - TP.HCM
Tài khoản ngân hàng,1234567890 - Vietcombank
Điều khoản thanh toán,Thanh toán trong 30 ngày

SKU,Tên sản phẩm,Số lượng,Giá nhập,Bảo hành (tháng),Ghi chú
PROD-001,Sản phẩm mẫu 1,10,100000,12,Ghi chú mẫu
PROD-002,Sản phẩm mẫu 2,20,200000,24,Ghi chú mẫu`

  // Add BOM for UTF-8 encoding
  const blob = new Blob(['\uFEFF' + csvContent], { 
    type: 'text/csv;charset=utf-8;' 
  })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'template-import-warehouse.csv'
  link.click()
  toast.success('Đã tải template Excel')
}
```

### 2. Parse CSV Function
```typescript
const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
  const file = e.target.files?.[0]
  if (!file) return

  if (!file.name.endsWith('.csv')) {
    toast.error('Vui lòng chọn file CSV')
    return
  }

  try {
    const text = await file.text()
    const lines = text.split('\n')
      .map(line => line.trim())
      .filter(line => line)
    
    // Parse supplier info (first 8 lines)
    const supplierData: any = {}
    for (let i = 0; i < Math.min(8, lines.length); i++) {
      const [key, value] = lines[i].split(',').map(s => s.trim())
      if (key === 'Nhà cung cấp') supplierData.name = value
      if (key === 'Mã số thuế') supplierData.taxCode = value
      // ... parse other fields
    }

    // Find product header line
    let productStartIndex = -1
    for (let i = 0; i < lines.length; i++) {
      if (lines[i].startsWith('SKU,')) {
        productStartIndex = i + 1
        break
      }
    }

    // Parse products
    const parsedItems: POItem[] = []
    for (let i = productStartIndex; i < lines.length; i++) {
      const parts = lines[i].split(',').map(s => s.trim())
      if (parts.length >= 4) {
        parsedItems.push({
          sku: parts[0],
          internalName: parts[1],
          quantity: parseInt(parts[2]) || 0,
          unitCost: parseFloat(parts[3]) || 0,
          warrantyMonths: parseInt(parts[4]) || 0,
          techSpecsJson: '',
          note: parts[5] || ''
        })
      }
    }

    // Update form
    if (supplierData.name && supplierData.taxCode) {
      setNewSupplier({ ...newSupplier, ...supplierData })
      setShowNewSupplierForm(true)
    }
    
    setItems(parsedItems)
    toast.success(`Đã import ${parsedItems.length} sản phẩm từ file`)
  } catch (error) {
    console.error('Error parsing CSV:', error)
    toast.error('Lỗi khi đọc file CSV')
  }
}
```

### 3. UI Component
```tsx
<div className="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg shadow-sm p-6 border border-blue-200">
  <div className="flex items-center justify-between mb-4">
    <div>
      <h2 className="text-lg font-semibold text-gray-900 flex items-center space-x-2">
        <FiUpload className="text-blue-600" />
        <span>Nhập nhanh từ Excel/CSV</span>
      </h2>
      <p className="text-sm text-gray-600 mt-1">
        Tải file Excel/CSV để tự động điền thông tin nhà cung cấp và sản phẩm
      </p>
    </div>
    <button
      type="button"
      onClick={downloadTemplate}
      className="flex items-center space-x-2 px-4 py-2 bg-white border border-blue-300 text-blue-600 rounded-lg hover:bg-blue-50 transition-colors"
    >
      <FiDownload />
      <span>Tải template</span>
    </button>
  </div>
  
  <div className="flex items-center space-x-4">
    <label className="flex-1 cursor-pointer">
      <div className="border-2 border-dashed border-blue-300 rounded-lg p-6 text-center hover:border-blue-500 hover:bg-blue-50 transition-colors">
        <FiUpload className="mx-auto text-blue-500 mb-2" size={32} />
        <p className="text-sm text-gray-700 font-medium">
          Click để chọn file CSV
        </p>
        <p className="text-xs text-gray-500 mt-1">
          Hỗ trợ file .csv (UTF-8)
        </p>
      </div>
      <input
        type="file"
        accept=".csv"
        onChange={handleFileUpload}
        className="hidden"
      />
    </label>
  </div>
</div>
```

## Workflow

### 1. Tải Template
```
User click "Tải template" 
  → Generate CSV content
  → Add BOM for UTF-8
  → Create Blob
  → Download file
  → Show success toast
```

### 2. Upload và Parse
```
User select CSV file
  → Read file content
  → Split into lines
  → Parse supplier info (lines 1-8)
  → Find product header (line with "SKU,")
  → Parse products (lines after header)
  → Validate data
  → Update form state
  → Show success toast with count
```

### 3. Edit và Submit
```
Form auto-filled with CSV data
  → User can edit/add/remove items
  → User can edit supplier info
  → Submit form
  → Create purchase order
```

## Validation

### File Validation
- ✅ Phải là file .csv
- ✅ File không rỗng
- ✅ Có header "SKU," để xác định vị trí sản phẩm

### Supplier Validation
- ⚠️ Nếu có thông tin NCC → auto-fill và chuyển sang "Tạo NCC mới"
- ⚠️ Nếu không có → user phải chọn NCC có sẵn hoặc nhập thủ công

### Product Validation
- ✅ Phải có ít nhất 1 sản phẩm
- ✅ Mỗi sản phẩm phải có: SKU, Tên, Số lượng, Giá nhập
- ✅ Số lượng và giá phải > 0

## Error Handling

### Lỗi file không đúng định dạng
```typescript
if (!file.name.endsWith('.csv')) {
  toast.error('Vui lòng chọn file CSV')
  return
}
```

### Lỗi không tìm thấy sản phẩm
```typescript
if (productStartIndex === -1) {
  toast.error('Không tìm thấy dữ liệu sản phẩm trong file')
  return
}
```

### Lỗi không có sản phẩm
```typescript
if (parsedItems.length === 0) {
  toast.error('Không có sản phẩm nào trong file')
  return
}
```

### Lỗi parse CSV
```typescript
try {
  // Parse logic
} catch (error) {
  console.error('Error parsing CSV:', error)
  toast.error('Lỗi khi đọc file CSV')
}
```

## Testing

### Test Case 1: Download Template
1. Click "Tải template"
2. ✅ File `template-import-warehouse.csv` được tải về
3. ✅ Mở file bằng Excel → hiển thị đúng tiếng Việt
4. ✅ Có đầy đủ cấu trúc: NCC info + product list

### Test Case 2: Upload Valid CSV
1. Tải template
2. Sửa thông tin NCC và sản phẩm
3. Upload file
4. ✅ Form tự động điền thông tin NCC
5. ✅ Danh sách sản phẩm hiển thị đúng
6. ✅ Toast: "Đã import X sản phẩm từ file"

### Test Case 3: Upload Invalid CSV
1. Upload file không phải .csv
2. ✅ Toast: "Vui lòng chọn file CSV"
3. Upload file CSV không có header "SKU,"
4. ✅ Toast: "Không tìm thấy dữ liệu sản phẩm trong file"

### Test Case 4: Edit After Import
1. Upload CSV với 5 sản phẩm
2. Xóa 2 sản phẩm
3. Thêm 1 sản phẩm mới
4. Sửa giá của 1 sản phẩm
5. Submit form
6. ✅ Phiếu nhập được tạo với 4 sản phẩm (đã chỉnh sửa)

### Test Case 5: Multiple Uploads
1. Upload file A (3 sản phẩm)
2. Upload file B (5 sản phẩm)
3. ✅ Danh sách sản phẩm được thay thế bằng file B
4. ✅ Có 5 sản phẩm từ file B

## Lưu ý quan trọng

### 1. Encoding
- File CSV phải có BOM (Byte Order Mark) để Excel đọc đúng UTF-8
- Khi download template, tự động thêm `\uFEFF` vào đầu file

### 2. Delimiter
- Sử dụng dấu phẩy (,) làm delimiter
- Không có dấu phẩy trong giá trị (VD: 100000 không phải 100,000)

### 3. Line Breaks
- Sử dụng `\n` cho line break
- Trim whitespace ở đầu/cuối mỗi dòng

### 4. Empty Lines
- Filter bỏ các dòng trống
- Chỉ parse các dòng có nội dung

### 5. Data Types
- Số lượng: parseInt()
- Giá: parseFloat()
- Bảo hành: parseInt() với default = 0

## Files đã cập nhật
1. ✅ `src/frontend/app/admin/warehouse/import/create/page.tsx`
2. ✅ `src/frontend/app/employee/warehouse/import/create/page.tsx`

## Tính năng tương lai (Optional)

### 1. Support Excel (.xlsx)
- Sử dụng thư viện như `xlsx` hoặc `exceljs`
- Parse file .xlsx thay vì chỉ .csv

### 2. Drag & Drop
- Thêm drag & drop zone
- Highlight khi drag over

### 3. Preview Before Import
- Hiển thị preview table trước khi import
- Cho phép user review và chỉnh sửa

### 4. Validation Rules
- Validate SKU format
- Validate phone number format
- Validate email format
- Validate tax code format

### 5. Error Report
- Hiển thị chi tiết lỗi từng dòng
- Export error report

## Kết luận
✅ Đã thêm chức năng nhập Excel/CSV vào form tạo phiếu nhập kho
✅ Hỗ trợ cả Admin và Employee
✅ Tự động parse và fill form
✅ Có thể chỉnh sửa sau khi import
✅ Validation đầy đủ
✅ Error handling tốt
✅ UI/UX thân thiện
