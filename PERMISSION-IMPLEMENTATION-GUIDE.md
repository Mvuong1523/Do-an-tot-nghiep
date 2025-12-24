# Hướng dẫn triển khai phân quyền cho Employee

## Mục tiêu
- Tất cả nhân viên có thể **XEM** tất cả các trang/menu
- Nhưng chỉ có thể **SỬ DỤNG** các chức năng nếu đúng phân quyền

## Các component đã tạo

### 1. `usePermissionCheck` Hook
```typescript
// src/frontend/hooks/usePermissionCheck.ts
const { checkPermission, getEmployeePosition } = usePermissionCheck()
```

### 2. `PermissionButton` Component
```typescript
// src/frontend/components/PermissionGuard.tsx
import { PermissionButton } from '@/components/PermissionGuard'

<PermissionButton
  requiredPosition="ACCOUNTANT"  // hoặc ["ACCOUNTANT", "ADMIN"]
  onClick={() => handleAction()}
  className="..."
>
  Button Text
</PermissionButton>
```

## Cách áp dụng cho từng module

### Module Kế toán (ACCOUNTANT)

#### 1. Trang Tổng quan (`/employee/accounting/page.tsx`) ✅ ĐÃ CẬP NHẬT
- Các button truy cập nhanh đã được bọc trong `PermissionButton`

#### 2. Trang Giao dịch (`/employee/accounting/transactions/page.tsx`)
Cần cập nhật các button sau:

```typescript
// Import
import { PermissionButton } from '@/components/PermissionGuard'

// Button Tạo mới
<PermissionButton
  requiredPosition="ACCOUNTANT"
  onClick={() => setShowCreateModal(true)}
  className="w-full bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 flex items-center justify-center"
>
  <FiPlus className="mr-2" />
  Tạo giao dịch
</PermissionButton>

// Button Sửa
<PermissionButton
  requiredPosition="ACCOUNTANT"
  onClick={() => setEditingTransaction(transaction)}
  className="text-blue-600 hover:text-blue-800"
>
  <FiEdit size={16} />
</PermissionButton>

// Button Xóa
<PermissionButton
  requiredPosition="ACCOUNTANT"
  onClick={() => deleteTransaction(transaction.id)}
  disabled={loading}
  className="text-red-600 hover:text-red-800"
>
  <FiTrash2 size={16} />
</PermissionButton>
```

#### 3. Trang Kỳ kế toán (`/employee/accounting/periods/page.tsx`)
```typescript
// Button Tạo kỳ mới
<PermissionButton
  requiredPosition="ACCOUNTANT"
  onClick={() => setShowCreateModal(true)}
  className="..."
>
  Tạo kỳ mới
</PermissionButton>

// Button Chốt sổ
<PermissionButton
  requiredPosition="ACCOUNTANT"
  onClick={() => closePeriod(period.id)}
  className="..."
>
  Chốt sổ
</PermissionButton>

// Button Mở khóa
<PermissionButton
  requiredPosition="ACCOUNTANT"
  onClick={() => reopenPeriod(period.id)}
  className="..."
>
  Mở khóa
</PermissionButton>
```

#### 4. Trang Công nợ NCC (`/employee/accounting/payables/page.tsx`)
```typescript
// Button Thanh toán
<PermissionButton
  requiredPosition="ACCOUNTANT"
  onClick={() => handlePayment(payable)}
  className="..."
>
  Thanh toán
</PermissionButton>
```

### Module Kho hàng (WAREHOUSE)

#### 1. Trang Nhập kho (`/employee/warehouse/import/page.tsx`)
```typescript
<PermissionButton
  requiredPosition={["WAREHOUSE", "PRODUCT_MANAGER"]}
  onClick={() => setShowImportModal(true)}
  className="..."
>
  Tạo phiếu nhập
</PermissionButton>
```

#### 2. Trang Xuất kho (`/employee/warehouse/export/page.tsx`)
```typescript
<PermissionButton
  requiredPosition={["WAREHOUSE", "PRODUCT_MANAGER"]}
  onClick={() => handleExport(order)}
  className="..."
>
  Xuất kho
</PermissionButton>
```

### Module Đơn hàng (SALES)

#### 1. Trang Đơn hàng (`/employee/orders/page.tsx`)
```typescript
// Button Tạo đơn
<PermissionButton
  requiredPosition="SALES"
  onClick={() => router.push('/employee/orders/create')}
  className="..."
>
  Tạo đơn hàng
</PermissionButton>

// Button Cập nhật trạng thái
<PermissionButton
  requiredPosition="SALES"
  onClick={() => updateOrderStatus(order)}
  className="..."
>
  Cập nhật
</PermissionButton>
```

### Module Sản phẩm (PRODUCT_MANAGER)

#### 1. Trang Sản phẩm (`/employee/products/page.tsx`)
```typescript
<PermissionButton
  requiredPosition="PRODUCT_MANAGER"
  onClick={() => router.push('/employee/products/create')}
  className="..."
>
  Thêm sản phẩm
</PermissionButton>

<PermissionButton
  requiredPosition="PRODUCT_MANAGER"
  onClick={() => handleEdit(product)}
  className="..."
>
  Sửa
</PermissionButton>

<PermissionButton
  requiredPosition="PRODUCT_MANAGER"
  onClick={() => handleDelete(product)}
  className="..."
>
  Xóa
</PermissionButton>
```

### Module Khách hàng (CSKH)

#### 1. Trang Khách hàng (`/employee/customers/page.tsx`)
```typescript
<PermissionButton
  requiredPosition={["CSKH", "SALES"]}
  onClick={() => handleContact(customer)}
  className="..."
>
  Liên hệ
</PermissionButton>
```

### Module Giao hàng (SHIPPER)

#### 1. Trang Giao hàng (`/employee/shipping/page.tsx`)
```typescript
<PermissionButton
  requiredPosition="SHIPPER"
  onClick={() => updateShippingStatus(order)}
  className="..."
>
  Cập nhật trạng thái
</PermissionButton>
```

## Danh sách vị trí (Position)

```typescript
type Position = 
  | 'SALES'           // Nhân viên bán hàng
  | 'WAREHOUSE'       // Nhân viên kho
  | 'PRODUCT_MANAGER' // Quản lý sản phẩm
  | 'ACCOUNTANT'      // Kế toán
  | 'CSKH'            // Chăm sóc khách hàng
  | 'SHIPPER'         // Nhân viên giao hàng
```

## Lưu ý quan trọng

1. **Không xóa check permission ở backend** - Backend vẫn phải kiểm tra quyền để bảo mật
2. **Không redirect** - Cho phép nhân viên vào trang nhưng disable các action
3. **Hiển thị thông báo rõ ràng** - Khi click vào button không có quyền, hiện toast thông báo
4. **Icon khóa** - Button không có quyền sẽ hiển thị icon khóa và bị mờ đi

## Ví dụ hoàn chỉnh

```typescript
'use client'

import { PermissionButton } from '@/components/PermissionGuard'
import { FiPlus, FiEdit, FiTrash2 } from 'react-icons/fi'

export default function MyPage() {
  return (
    <div>
      {/* Button cho phép nhiều vị trí */}
      <PermissionButton
        requiredPosition={["ACCOUNTANT", "ADMIN"]}
        onClick={() => handleCreate()}
        className="bg-blue-600 text-white px-4 py-2 rounded-lg"
      >
        <FiPlus className="mr-2" />
        Tạo mới
      </PermissionButton>

      {/* Button chỉ cho 1 vị trí */}
      <PermissionButton
        requiredPosition="ACCOUNTANT"
        onClick={() => handleEdit()}
        className="text-blue-600"
      >
        <FiEdit />
      </PermissionButton>

      {/* Button với disabled riêng */}
      <PermissionButton
        requiredPosition="ACCOUNTANT"
        onClick={() => handleDelete()}
        disabled={loading}
        className="text-red-600"
      >
        <FiTrash2 />
      </PermissionButton>
    </div>
  )
}
```

## Checklist triển khai

### Kế toán
- [x] `/employee/accounting` - Trang tổng quan
- [ ] `/employee/accounting/transactions` - Giao dịch tài chính
- [ ] `/employee/accounting/periods` - Kỳ kế toán
- [ ] `/employee/accounting/tax` - Quản lý thuế
- [ ] `/employee/accounting/advanced-reports` - Báo cáo nâng cao
- [ ] `/employee/accounting/shipping` - Đối soát vận chuyển
- [ ] `/employee/accounting/payables` - Công nợ NCC

### Kho hàng
- [ ] `/employee/warehouse/import` - Nhập kho
- [ ] `/employee/warehouse/export` - Xuất kho
- [ ] `/employee/warehouse/inventory` - Tồn kho

### Đơn hàng
- [ ] `/employee/orders` - Danh sách đơn hàng
- [ ] `/employee/orders/create` - Tạo đơn hàng
- [ ] `/employee/orders/[id]` - Chi tiết đơn hàng

### Sản phẩm
- [ ] `/employee/products` - Danh sách sản phẩm
- [ ] `/employee/products/create` - Thêm sản phẩm
- [ ] `/employee/categories` - Danh mục

### Khác
- [ ] `/employee/customers` - Khách hàng
- [ ] `/employee/suppliers` - Nhà cung cấp
- [ ] `/employee/shipping` - Giao hàng
