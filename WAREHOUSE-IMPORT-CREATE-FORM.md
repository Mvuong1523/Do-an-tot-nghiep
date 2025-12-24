# Warehouse Import Create Form - Form tạo phiếu nhập kho

## Tổng quan
Đã tạo form đầy đủ để tạo phiếu nhập kho cho cả Admin và Employee, thay thế các trang redirect cũ.

## Files đã tạo

### 1. Admin Import Create Form
**File:** `src/frontend/app/admin/warehouse/import/create/page.tsx`

**Chức năng:**
- ✅ Form tạo phiếu nhập kho đầy đủ
- ✅ Tự động generate mã phiếu (PO + ngày + timestamp)
- ✅ Chọn nhà cung cấp có sẵn hoặc tạo mới
- ✅ Thêm/xóa nhiều sản phẩm
- ✅ Tính tổng tiền tự động
- ✅ Validation đầy đủ
- ✅ Gọi API `/api/inventory/create_pchaseOrder`

### 2. Employee Import Create Form
**File:** `src/frontend/app/employee/warehouse/import/create/page.tsx`

**Chức năng:**
- ✅ Giống Admin form
- ✅ Thêm permission check (chỉ WAREHOUSE position)
- ✅ Hiển thị thông báo nếu không có quyền
- ✅ Tự động redirect nếu không có quyền

## Cấu trúc Form

### 1. Thông tin cơ bản
```typescript
{
  poCode: string,        // Mã phiếu (auto-generated)
  createdBy: string,     // Người tạo (từ user/employee)
  note: string           // Ghi chú
}
```

### 2. Nhà cung cấp
**Option 1: Chọn NCC có sẵn**
```typescript
{
  supplier: {
    taxCode: string  // Chỉ cần taxCode để link với NCC có sẵn
  }
}
```

**Option 2: Tạo NCC mới**
```typescript
{
  supplier: {
    name: string,           // * Bắt buộc
    taxCode: string,        // * Bắt buộc
    contactName: string,
    phone: string,
    email: string,
    address: string,
    bankAccount: string,
    paymentTerm: string,
    paymentTermDays: number
  }
}
```

### 3. Danh sách sản phẩm
```typescript
{
  items: [
    {
      sku: string,              // * Bắt buộc
      internalName: string,     // * Bắt buộc
      quantity: number,         // * Bắt buộc, > 0
      unitCost: number,         // * Bắt buộc, > 0
      warrantyMonths: number,   // Optional, >= 0
      techSpecsJson: string,    // Optional, JSON format
      note: string              // Optional
    }
  ]
}
```

## API Request Format

### Endpoint
```
POST /api/inventory/create_pchaseOrder
Authorization: Bearer {token}
```

### Request Body
```json
{
  "createdBy": "Nguyen Van A",
  "poCode": "PO20240124_123456",
  "supplier": {
    "taxCode": "0123456789"
  },
  "items": [
    {
      "sku": "SKU001",
      "internalName": "Laptop Dell XPS 15",
      "quantity": 10,
      "unitCost": 25000000,
      "warrantyMonths": 24,
      "techSpecsJson": "{\"cpu\":\"i7\",\"ram\":\"16GB\"}",
      "note": "Hàng mới 100%"
    }
  ],
  "note": "Đơn nhập hàng tháng 1"
}
```

## Tính năng chính

### 1. Auto-generate PO Code
```typescript
const generatePOCode = () => {
  const date = new Date()
  const code = `PO${date.getFullYear()}${String(date.getMonth() + 1).padStart(2, '0')}${String(date.getDate()).padStart(2, '0')}_${Date.now().toString().slice(-6)}`
  setPoCode(code)
}
// Ví dụ: PO20240124_123456
```

### 2. Dynamic Items Management
```typescript
// Thêm sản phẩm
const addItem = () => {
  setItems([...items, { sku: '', internalName: '', ... }])
}

// Xóa sản phẩm
const removeItem = (index: number) => {
  if (items.length > 1) {
    setItems(items.filter((_, i) => i !== index))
  }
}

// Cập nhật sản phẩm
const updateItem = (index: number, field: keyof POItem, value: any) => {
  const newItems = [...items]
  newItems[index] = { ...newItems[index], [field]: value }
  setItems(newItems)
}
```

### 3. Auto-calculate Total
```typescript
const calculateTotal = () => {
  return items.reduce((sum, item) => sum + (item.quantity * item.unitCost), 0)
}
```

### 4. Toggle Supplier Form
```typescript
// Chuyển đổi giữa "Chọn NCC có sẵn" và "Tạo NCC mới"
<button onClick={() => setShowNewSupplierForm(!showNewSupplierForm)}>
  {showNewSupplierForm ? 'Chọn NCC có sẵn' : '+ Thêm NCC mới'}
</button>
```

### 5. Validation
```typescript
// Kiểm tra trước khi submit
- Mã phiếu không được trống
- Phải chọn hoặc tạo nhà cung cấp
- Nếu tạo NCC mới: phải có name và taxCode
- Phải có ít nhất 1 sản phẩm
- Mỗi sản phẩm phải có: sku, internalName, quantity > 0, unitCost > 0
```

## Permission System (Employee)

### Check Permission
```typescript
const canCreate = hasPermission(employee?.position as Position, 'warehouse.import.create')

useEffect(() => {
  if (!canCreate) {
    toast.error('Bạn không có quyền tạo phiếu nhập kho')
    router.push('/employee/warehouse')
    return
  }
}, [canCreate, router])
```

### Permission Notice
```typescript
if (!canCreate) {
  return (
    <div className="bg-red-50 border border-red-200 rounded-lg p-6">
      <FiAlertCircle className="text-red-600" />
      <h3>Không có quyền truy cập</h3>
      <p>Bạn không có quyền tạo phiếu nhập kho. Chỉ nhân viên kho mới có quyền này.</p>
    </div>
  )
}
```

## UI Components

### Form Sections
1. **Thông tin cơ bản** - Mã phiếu, Người tạo
2. **Nhà cung cấp** - Chọn/Tạo NCC
3. **Danh sách sản phẩm** - Dynamic items list
4. **Ghi chú** - Note textarea
5. **Actions** - Hủy / Tạo phiếu

### Item Card
```
┌─────────────────────────────────────────┐
│ Sản phẩm 1                         [X]  │
├─────────────────────────────────────────┤
│ SKU*        | Tên SP*      | Số lượng* │
│ Đơn giá*    | Bảo hành     | Thành tiền│
│ Thông số kỹ thuật (JSON)               │
│ Ghi chú                                 │
└─────────────────────────────────────────┘
```

### Total Display
```
┌─────────────────────────────────────────┐
│ Tổng cộng:              25,000,000 đ    │
└─────────────────────────────────────────┘
```

## Workflow

### 1. Tạo phiếu nhập
```
User → Form → Validation → API Call → Success → Redirect to list
                    ↓
                  Error → Show toast
```

### 2. Với NCC mới
```
Form → supplier: { name, taxCode, ... } → Backend tạo NCC mới → Tạo PO
```

### 3. Với NCC có sẵn
```
Form → supplier: { taxCode } → Backend link với NCC có sẵn → Tạo PO
```

## Testing

### Test Case 1: Tạo phiếu với NCC có sẵn
1. Truy cập `/admin/warehouse/import/create`
2. Mã phiếu tự động generate
3. Chọn NCC từ dropdown
4. Thêm sản phẩm: SKU, tên, số lượng, đơn giá
5. Click "Tạo phiếu nhập"
6. ✅ Phiếu được tạo, redirect về list

### Test Case 2: Tạo phiếu với NCC mới
1. Click "+ Thêm NCC mới"
2. Nhập: Tên NCC, Mã số thuế
3. Nhập thông tin liên hệ (optional)
4. Thêm sản phẩm
5. Click "Tạo phiếu nhập"
6. ✅ NCC mới được tạo, phiếu được tạo

### Test Case 3: Validation
1. Bỏ trống mã phiếu → ❌ "Vui lòng nhập mã phiếu nhập"
2. Không chọn NCC → ❌ "Vui lòng chọn nhà cung cấp"
3. Sản phẩm thiếu SKU → ❌ "Sản phẩm 1: Vui lòng nhập đầy đủ thông tin"
4. Số lượng = 0 → ❌ "Sản phẩm 1: Vui lòng nhập đầy đủ thông tin"

### Test Case 4: Employee Permission
1. Login với employee không phải WAREHOUSE
2. Truy cập `/employee/warehouse/import/create`
3. ✅ Hiển thị thông báo "Không có quyền truy cập"
4. ✅ Tự động redirect về `/employee/warehouse`

### Test Case 5: Multiple Items
1. Click "Thêm sản phẩm" 3 lần
2. Nhập thông tin cho 3 sản phẩm
3. Xóa sản phẩm thứ 2
4. ✅ Còn 2 sản phẩm
5. ✅ Tổng tiền tính đúng

## Lưu ý

### Backend Requirements
- Endpoint: `POST /api/inventory/create_pchaseOrder`
- Authorization: Bearer token required
- Permission: WAREHOUSE hoặc ADMIN

### Frontend Dependencies
```typescript
import { inventoryApi } from '@/lib/api'
import { useAuthStore } from '@/store/authStore'
import { hasPermission, type Position } from '@/lib/permissions'
```

### API Method
```typescript
// lib/api.ts
createPurchaseOrder: async (data: any): Promise<ApiResponse<any>> => {
  const response = await apiClient.post('/inventory/create_pchaseOrder', data)
  return response.data
}
```

## Kết luận
✅ Form tạo phiếu nhập kho đầy đủ chức năng
✅ Hỗ trợ cả Admin và Employee
✅ Permission system hoàn chỉnh
✅ Validation đầy đủ
✅ UI/UX thân thiện
✅ Không còn redirect đến trang không tồn tại
