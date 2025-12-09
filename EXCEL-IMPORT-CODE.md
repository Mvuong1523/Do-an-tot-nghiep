# 📝 Code Import Excel - Chi tiết implementation

## Bước 1: Cài đặt thư viện

```bash
cd src/frontend
npm install xlsx
npm install @types/xlsx --save-dev
```

## Bước 2: Tạo component ExcelImport

Tạo file: `src/frontend/components/ExcelImport.tsx`

```tsx
'use client'

import { useRef } from 'react'
import * as XLSX from 'xlsx'
import { FiUpload } from 'react-icons/fi'
import toast from 'react-hot-toast'

interface ExcelImportProps {
  onImport: (items: any[]) => void
}

export default function ExcelImport({ onImport }: ExcelImportProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    // Validate file type
    if (!file.name.endsWith('.xlsx') && !file.name.endsWith('.xls')) {
      toast.error('Vui lòng chọn file Excel (.xlsx hoặc .xls)')
      return
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      toast.error('File không được vượt quá 5MB')
      return
    }

    const reader = new FileReader()

    reader.onload = (event) => {
      try {
        const data = event.target?.result
        const workbook = XLSX.read(data, { type: 'binary' })
        
        // Get first sheet
        const sheetName = workbook.SheetNames[0]
        const worksheet = workbook.Sheets[sheetName]
        
        // Convert to JSON
        const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
        
        // Parse data
        const items = parseExcelData(jsonData as any[][])
        
        if (items.length === 0) {
          toast.error('Không có dữ liệu hợp lệ trong file')
          return
        }

        onImport(items)
        toast.success(`Đã import ${items.length} sản phẩm từ Excel`)
        
        // Reset input
        if (fileInputRef.current) {
          fileInputRef.current.value = ''
        }
      } catch (error) {
        console.error('Error reading Excel:', error)
        toast.error('Lỗi khi đọc file Excel')
      }
    }

    reader.onerror = () => {
      toast.error('Lỗi khi đọc file')
    }

    reader.readAsBinaryString(file)
  }

  const parseExcelData = (data: any[][]): any[] => {
    if (data.length < 2) {
      toast.error('File Excel phải có ít nhất 2 dòng (header + data)')
      return []
    }

    // Skip header row (row 0)
    const items: any[] = []
    const errors: string[] = []

    for (let i = 1; i < data.length; i++) {
      const row = data[i]
      
      // Skip empty rows
      if (!row || row.length === 0 || !row[0]) continue

      try {
        // Expected columns: SKU | Tên SP | Số lượng | Giá nhập | Bảo hành | Ghi chú
        const sku = row[0]?.toString().trim()
        const productName = row[1]?.toString().trim()
        const quantity = parseInt(row[2]?.toString() || '0')
        const price = parseFloat(row[3]?.toString() || '0')
        const warrantyMonths = parseInt(row[4]?.toString() || '12')
        const note = row[5]?.toString().trim() || ''

        // Validate
        if (!sku) {
          errors.push(`Dòng ${i + 1}: SKU không được trống`)
          continue
        }
        if (!productName) {
          errors.push(`Dòng ${i + 1}: Tên sản phẩm không được trống`)
          continue
        }
        if (quantity <= 0 || isNaN(quantity)) {
          errors.push(`Dòng ${i + 1}: Số lượng phải > 0`)
          continue
        }
        if (price <= 0 || isNaN(price)) {
          errors.push(`Dòng ${i + 1}: Giá nhập phải > 0`)
          continue
        }

        items.push({
          sku,
          productName,
          quantity,
          price,
          warrantyMonths: isNaN(warrantyMonths) ? 12 : warrantyMonths,
          note
        })
      } catch (error) {
        errors.push(`Dòng ${i + 1}: Lỗi xử lý dữ liệu`)
      }
    }

    // Show errors if any
    if (errors.length > 0) {
      console.warn('Import errors:', errors)
      toast.error(`Có ${errors.length} dòng lỗi. Kiểm tra console để xem chi tiết.`)
    }

    return items
  }

  return (
    <div>
      <input
        ref={fileInputRef}
        type="file"
        accept=".xlsx,.xls"
        onChange={handleFileUpload}
        className="hidden"
      />
      <button
        type="button"
        onClick={() => fileInputRef.current?.click()}
        className="flex items-center space-x-2 px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
      >
        <FiUpload />
        <span>📥 Import từ Excel</span>
      </button>
    </div>
  )
}
```

## Bước 3: Tích hợp vào trang create

Mở file: `src/frontend/app/admin/inventory/transactions/create/page.tsx`

### 3.1. Import component

Thêm vào đầu file:

```tsx
import ExcelImport from '@/components/ExcelImport'
```

### 3.2. Thêm handler

Thêm function xử lý import:

```tsx
const handleExcelImport = (importedItems: any[]) => {
  // Convert imported items to TransactionItem format
  const newItems: TransactionItem[] = importedItems.map(item => ({
    sku: item.sku,
    productName: item.productName,
    quantity: item.quantity,
    price: item.price,
    warrantyMonths: item.warrantyMonths,
    note: item.note,
    techSpecs: '' // Empty for now, can be added later
  }))

  // Add to existing items
  setItems(prevItems => [...prevItems, ...newItems])
  
  toast.success(`Đã thêm ${newItems.length} sản phẩm từ Excel`)
}
```

### 3.3. Thêm button vào UI

Tìm phần hiển thị danh sách sản phẩm và thêm button:

```tsx
<div className="flex justify-between items-center mb-4">
  <h3 className="text-lg font-semibold">Danh sách sản phẩm</h3>
  <div className="flex space-x-2">
    <ExcelImport onImport={handleExcelImport} />
    <button
      type="button"
      onClick={() => setShowProductModal(true)}
      className="flex items-center space-x-2 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
    >
      <FiPlus />
      <span>Thêm sản phẩm</span>
    </button>
  </div>
</div>
```

## Bước 4: Tạo file Excel mẫu

Tạo file `import-template.xlsx` với cấu trúc:

| SKU | Tên sản phẩm | Số lượng | Giá nhập | Bảo hành (tháng) | Ghi chú |
|-----|--------------|----------|----------|------------------|---------|
| IP15-128-BLK | iPhone 15 128GB Đen | 10 | 20000000 | 12 | Hàng mới |
| SS-S24-256-WHT | Samsung S24 256GB | 5 | 18000000 | 12 | |

## Bước 5: Test

1. Tạo file Excel theo template
2. Vào trang tạo phiếu nhập kho
3. Click "Import từ Excel"
4. Chọn file
5. Kiểm tra danh sách sản phẩm đã được điền tự động

## 🎯 Kết quả

- ✅ Upload file Excel
- ✅ Parse dữ liệu tự động
- ✅ Validate dữ liệu
- ✅ Điền vào form
- ✅ Hiển thị lỗi nếu có
- ✅ Có thể sửa sau khi import

## 📊 Format Excel chi tiết

### Cột 1: SKU (Bắt buộc)
- Mã sản phẩm unique
- Ví dụ: `IP15-128-BLK`, `SS-S24-256-WHT`

### Cột 2: Tên sản phẩm (Bắt buộc)
- Tên đầy đủ của sản phẩm
- Ví dụ: `iPhone 15 128GB Đen`

### Cột 3: Số lượng (Bắt buộc)
- Số nguyên > 0
- Ví dụ: `10`, `5`, `100`

### Cột 4: Giá nhập (Bắt buộc)
- Số > 0, đơn vị VNĐ
- Ví dụ: `20000000`, `18000000`

### Cột 5: Bảo hành (Tùy chọn)
- Số tháng bảo hành
- Mặc định: 12 tháng
- Ví dụ: `12`, `24`, `36`

### Cột 6: Ghi chú (Tùy chọn)
- Ghi chú thêm về sản phẩm
- Ví dụ: `Hàng mới`, `Màu đen`

## 🔧 Customize

Nếu muốn thêm cột khác (ví dụ: Thông số kỹ thuật), sửa trong `parseExcelData`:

```tsx
const techSpecs = row[6]?.toString().trim() || ''

items.push({
  // ... existing fields
  techSpecs
})
```

## 💡 Tips

1. **Tạo template Excel** để user download và điền
2. **Validate kỹ** trước khi import
3. **Hiển thị preview** trước khi submit
4. **Cho phép sửa** sau khi import
5. **Log errors** để debug

Chức năng import Excel đã sẵn sàng! 🎉
