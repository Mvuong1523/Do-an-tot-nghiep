# Hướng dẫn Hoàn thành Migration Employee Interface

## ✅ Đã hoàn thành (44%)

### Warehouse Module - 11/11 trang
- ✅ `/employee/warehouse/import/page.tsx`
- ✅ `/employee/warehouse/import/create/page.tsx`
- ✅ `/employee/warehouse/import/list/page.tsx`
- ✅ `/employee/warehouse/import/[id]/page.tsx`
- ✅ `/employee/warehouse/export/page.tsx`
- ✅ `/employee/warehouse/export/create/page.tsx`
- ✅ `/employee/warehouse/export/list/page.tsx`
- ✅ `/employee/warehouse/export/[id]/page.tsx`
- ✅ `/employee/warehouse/inventory/page.tsx`
- ✅ `/employee/warehouse/reports/page.tsx`
- ✅ `/employee/suppliers/page.tsx`

## ⏳ Cần hoàn thành (56%)

### 1. Product Manager Module - 4 trang

#### `/employee/products/page.tsx`
**Copy từ**: `/product-manager/products/page.tsx`

**Thay đổi cần thiết**:
```typescript
// 1. Thêm permission check
import { hasPermission, type Position } from '@/lib/permissions'
import { useAuthStore } from '@/store/authStore'

const { employee } = useAuthStore()
const canCreate = hasPermission(employee?.position as Position, 'products.create')
const canEdit = hasPermission(employee?.position as Position, 'products.edit')
const canDelete = hasPermission(employee?.position as Position, 'products.delete')

// 2. Conditional rendering cho nút "Đăng bán sản phẩm mới"
{canCreate && (
  <Link href="/employee/products/publish">
    Đăng bán sản phẩm mới
  </Link>
)}

// 3. Conditional rendering cho nút "Sửa"
{canEdit && (
  <button onClick={() => handleEdit(product)}>
    <FiEdit /> Sửa
  </button>
)}

// 4. Thêm permission notice
{!canCreate && !canEdit && (
  <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
    <div className="flex items-start">
      <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
      <div>
        <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
        <p className="text-sm text-blue-700 mt-1">
          Bạn chỉ có quyền xem danh sách sản phẩm, không thể thêm hoặc chỉnh sửa.
        </p>
      </div>
    </div>
  </div>
)}

// 5. Cập nhật routing
// OLD: /product-manager/products/publish
// NEW: /employee/products/publish

// 6. Xóa check role cũ
// OLD: const isProductManager = user?.role === 'ADMIN' || (user?.role === 'EMPLOYEE' && user?.position === 'PRODUCT_MANAGER')
// NEW: Không cần check, tất cả employee đều vào được
```

#### `/employee/products/publish/page.tsx`
**Copy từ**: `/product-manager/products/publish/page.tsx`

**Thay đổi cần thiết**:
```typescript
// 1. Thêm permission check ở đầu component
const { employee } = useAuthStore()
const canCreate = hasPermission(employee?.position as Position, 'products.create')

useEffect(() => {
  if (!canCreate) {
    toast.error('Bạn không có quyền đăng bán sản phẩm')
    router.push('/employee/products')
  }
}, [canCreate, router])

// 2. Cập nhật routing
// OLD: /product-manager/products
// NEW: /employee/products
```

#### `/employee/categories/page.tsx`
**Copy từ**: `/product-manager/categories/page.tsx`

**Thay đổi cần thiết**:
```typescript
// 1. Thêm permission check
const canCreate = hasPermission(employee?.position as Position, 'categories.create')
const canEdit = hasPermission(employee?.position as Position, 'categories.edit')
const canDelete = hasPermission(employee?.position as Position, 'categories.delete')

// 2. Conditional rendering cho nút "Thêm danh mục"
{canCreate && (
  <button onClick={handleCreate}>
    <FiPlus /> Thêm danh mục
  </button>
)}

// 3. Conditional rendering cho nút "Sửa"
{canEdit && (
  <button onClick={() => handleEdit(category)}>
    <FiEdit />
  </button>
)}

// 4. Conditional rendering cho nút "Xóa"
{canDelete && (
  <button onClick={() => handleDelete(category.id)}>
    <FiTrash2 />
  </button>
)}

// 5. Thêm permission notice
{!canCreate && !canEdit && (
  <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
    <div className="flex items-start">
      <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
      <div>
        <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
        <p className="text-sm text-blue-700 mt-1">
          Bạn chỉ có quyền xem danh sách danh mục, không thể thêm hoặc chỉnh sửa.
        </p>
      </div>
    </div>
  </div>
)}

// 6. Cập nhật routing
// OLD: /product-manager/products?category=
// NEW: /employee/products?category=
```

#### `/employee/inventory/page.tsx`
**Copy từ**: `/product-manager/inventory/page.tsx`

**Thay đổi cần thiết**:
- Tương tự như `/employee/warehouse/inventory/page.tsx` đã tạo
- Không cần permission check vì chỉ xem

### 2. Sales Module - 2 trang

#### `/employee/orders/page.tsx`
**Copy từ**: `/sales/orders/page.tsx`

**Thay đổi cần thiết**:
```typescript
// 1. Thêm permission check
const canCreate = hasPermission(employee?.position as Position, 'orders.create')
const canEdit = hasPermission(employee?.position as Position, 'orders.edit')
const canConfirm = hasPermission(employee?.position as Position, 'orders.confirm')
const canCancel = hasPermission(employee?.position as Position, 'orders.cancel')

// 2. Conditional rendering cho nút "Tạo đơn hàng"
{canCreate && (
  <button onClick={handleCreateOrder}>
    Tạo đơn hàng
  </button>
)}

// 3. Conditional rendering cho nút "Xác nhận"
{canConfirm && order.status === 'PENDING_PAYMENT' && (
  <button onClick={() => handleConfirm(order.id)}>
    Xác nhận
  </button>
)}

// 4. Conditional rendering cho nút "Hủy"
{canCancel && (
  <button onClick={() => handleCancel(order.id)}>
    Hủy đơn
  </button>
)}

// 5. Thêm permission notice
{!canCreate && !canEdit && (
  <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
    <div className="flex items-start">
      <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
      <div>
        <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
        <p className="text-sm text-blue-700 mt-1">
          Bạn chỉ có quyền xem đơn hàng, không thể tạo hoặc chỉnh sửa.
        </p>
      </div>
    </div>
  </div>
)}

// 6. Cập nhật routing
// OLD: /sales/orders/...
// NEW: /employee/orders/...
```

#### `/employee/export/page.tsx`
**Copy từ**: `/sales/export/page.tsx`

**Thay đổi cần thiết**:
- Tương tự như orders page
- Thêm permission check cho export actions

### 3. Accounting Module - 4 trang

#### `/employee/accounting/reconciliation/page.tsx`
**Copy từ**: `/admin/accounting/reconciliation/page.tsx`

**Thay đổi cần thiết**:
```typescript
// 1. Thêm permission check
const canEdit = hasPermission(employee?.position as Position, 'accounting.reconciliation.edit')

// 2. Conditional rendering cho nút "Đối soát"
{canEdit && (
  <button onClick={handleReconcile}>
    Đối soát
  </button>
)}

// 3. Thêm permission notice
{!canEdit && (
  <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
    <div className="flex items-start">
      <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
      <div>
        <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
        <p className="text-sm text-blue-700 mt-1">
          Bạn chỉ có quyền xem báo cáo đối soát, không thể thực hiện đối soát.
        </p>
      </div>
    </div>
  </div>
)}

// 4. Cập nhật routing
// OLD: /admin/accounting/reconciliation
// NEW: /employee/accounting/reconciliation
```

#### `/employee/accounting/payables/page.tsx`
**Copy từ**: `/admin/accounting/payables/page.tsx`

**Thay đổi cần thiết**:
```typescript
// 1. Thêm permission check
const canCreate = hasPermission(employee?.position as Position, 'accounting.payables.create')
const canEdit = hasPermission(employee?.position as Position, 'accounting.payables.edit')
const canDelete = hasPermission(employee?.position as Position, 'accounting.payables.delete')

// 2. Conditional rendering cho các nút
{canCreate && <button>Thêm công nợ</button>}
{canEdit && <button>Sửa</button>}
{canDelete && <button>Xóa</button>}

// 3. Thêm permission notice
{!canCreate && !canEdit && (
  <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
    <div className="flex items-start">
      <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
      <div>
        <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
        <p className="text-sm text-blue-700 mt-1">
          Bạn chỉ có quyền xem công nợ nhà cung cấp, không thể thêm hoặc chỉnh sửa.
        </p>
      </div>
    </div>
  </div>
)}
```

#### `/employee/accounting/statements/page.tsx`
**Tạo mới** - Báo cáo tài chính (chỉ xem)

```typescript
'use client'

import { useState, useEffect } from 'react'
import { FiFileText, FiDownload } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'

export default function FinancialStatementsPage() {
  const { employee } = useAuthStore()
  const [statements, setStatements] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadStatements()
  }, [])

  const loadStatements = async () => {
    try {
      // TODO: Call API
      setStatements([])
    } catch (error) {
      console.error('Error loading statements:', error)
      toast.error('Lỗi khi tải báo cáo')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Báo cáo tài chính</h1>
        <p className="text-gray-600 mt-1">Xem các báo cáo tài chính</p>
      </div>

      {/* Permission notice - Chỉ xem */}
      <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-start">
          <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
          <div>
            <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
            <p className="text-sm text-blue-700 mt-1">
              Bạn có thể xem và tải xuống báo cáo tài chính.
            </p>
          </div>
        </div>
      </div>

      {/* Statements list */}
      <div className="bg-white rounded-lg shadow-sm p-6">
        <p className="text-gray-500 text-center">Chưa có báo cáo nào</p>
      </div>
    </div>
  )
}
```

#### `/employee/accounting/bank-accounts/page.tsx`
**Copy từ**: `/admin/bank-accounts/page.tsx`

**Thay đổi cần thiết**:
```typescript
// 1. Thêm permission check
const canCreate = hasPermission(employee?.position as Position, 'bank_accounts.create')
const canEdit = hasPermission(employee?.position as Position, 'bank_accounts.edit')
const canDelete = hasPermission(employee?.position as Position, 'bank_accounts.delete')

// 2. Conditional rendering
{canCreate && <button>Thêm tài khoản</button>}
{canEdit && <button>Sửa</button>}
{canDelete && <button>Xóa</button>}

// 3. Permission notice
{!canCreate && !canEdit && (
  <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
    <div className="flex items-start">
      <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
      <div>
        <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
        <p className="text-sm text-blue-700 mt-1">
          Bạn chỉ có quyền xem danh sách tài khoản ngân hàng, không thể thêm hoặc chỉnh sửa.
        </p>
      </div>
    </div>
  </div>
)}
```

### 4. Shipping Module - 1 trang

#### `/employee/shipping/page.tsx`
**Copy từ**: `/shipper/page.tsx`

**Thay đổi cần thiết**:
```typescript
// 1. Thêm permission check
const canPickup = hasPermission(employee?.position as Position, 'shipping.pickup')
const canDeliver = hasPermission(employee?.position as Position, 'shipping.deliver')
const canUpdateStatus = hasPermission(employee?.position as Position, 'shipping.update_status')

// 2. Conditional rendering
{canPickup && <button>Lấy hàng</button>}
{canDeliver && <button>Giao hàng</button>}
{canUpdateStatus && <button>Cập nhật trạng thái</button>}

// 3. Permission notice
{!canPickup && !canDeliver && (
  <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
    <div className="flex items-start">
      <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
      <div>
        <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
        <p className="text-sm text-blue-700 mt-1">
          Bạn chỉ có quyền xem danh sách đơn giao hàng, không thể thực hiện giao hàng.
        </p>
      </div>
    </div>
  </div>
)}
```

### 5. Customers Module - 1 trang

#### `/employee/customers/page.tsx`
**Tạo mới** - Danh sách khách hàng

```typescript
'use client'

import { useState, useEffect } from 'react'
import { FiUsers, FiSearch, FiEdit, FiFileText } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import { hasPermission, type Position } from '@/lib/permissions'

export default function CustomersPage() {
  const { employee } = useAuthStore()
  const [customers, setCustomers] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')

  // Check permissions
  const canEdit = hasPermission(employee?.position as Position, 'customers.edit')

  useEffect(() => {
    loadCustomers()
  }, [])

  const loadCustomers = async () => {
    try {
      // TODO: Call API
      setCustomers([])
    } catch (error) {
      console.error('Error loading customers:', error)
      toast.error('Lỗi khi tải khách hàng')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Danh sách khách hàng</h1>
        <p className="text-gray-600 mt-1">Quản lý thông tin khách hàng</p>
      </div>

      {/* Permission notice */}
      {!canEdit && (
        <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-start">
            <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
            <div>
              <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
              <p className="text-sm text-blue-700 mt-1">
                Bạn chỉ có quyền xem thông tin khách hàng, không thể chỉnh sửa.
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Search */}
      <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div className="relative">
          <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Tìm kiếm khách hàng..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      {/* Customers list */}
      <div className="bg-white rounded-lg shadow-sm p-6">
        <p className="text-gray-500 text-center">Chưa có khách hàng nào</p>
      </div>
    </div>
  )
}
```

### 6. Warehouse Orders - 2 trang

#### `/employee/warehouse/orders/page.tsx`
**Copy từ**: `/warehouse/orders/page.tsx`

**Thay đổi cần thiết**:
- Tương tự như các trang warehouse khác
- Không cần permission check đặc biệt vì chỉ xem

#### `/employee/warehouse/orders/[id]/page.tsx`
**Copy từ**: `/warehouse/orders/[id]/page.tsx`

**Thay đổi cần thiết**:
- Tương tự như các trang warehouse khác
- Không cần permission check đặc biệt vì chỉ xem

## 📝 Checklist Tổng hợp

### Warehouse (11/11) ✅
- [x] Import list
- [x] Import detail
- [x] Import create
- [x] Export list
- [x] Export detail
- [x] Export create
- [x] Inventory
- [x] Reports
- [x] Suppliers
- [x] Orders list
- [x] Orders detail

### Products (0/4) ⏳
- [ ] Products list
- [ ] Products publish
- [ ] Categories
- [ ] Inventory

### Sales (0/2) ⏳
- [ ] Orders
- [ ] Export

### Accounting (0/4) ⏳
- [ ] Reconciliation
- [ ] Payables
- [ ] Statements
- [ ] Bank accounts

### Shipping (0/1) ⏳
- [ ] Shipping list

### Customers (0/1) ⏳
- [ ] Customers list

## 🎯 Tổng kết

- **Đã hoàn thành**: 11/25 trang (44%)
- **Còn lại**: 14/25 trang (56%)

## 🚀 Cách thực hiện nhanh

1. **Copy file gốc** từ folder cũ
2. **Find & Replace** routing paths
3. **Thêm permission imports** ở đầu file
4. **Thêm permission checks** trong component
5. **Thêm conditional rendering** cho buttons
6. **Thêm permission notice** box
7. **Test** từng trang

## ⚡ Script tự động (Optional)

Có thể tạo script Node.js để tự động:
1. Copy files
2. Replace imports
3. Replace routing
4. Inject permission code

Nhưng vì mỗi trang có logic khác nhau, nên manual migration an toàn hơn.
