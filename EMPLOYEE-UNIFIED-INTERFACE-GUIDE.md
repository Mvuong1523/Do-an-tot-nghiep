# Hướng dẫn Giao diện Nhân viên Thống nhất

## Tổng quan

Hệ thống đã được cập nhật để gộp tất cả nhân viên về **1 giao diện chung** tại `/employee`, thay vì mỗi vị trí có giao diện riêng.

### Nguyên tắc phân quyền:

1. **Tất cả nhân viên có thể XEM tất cả các trang**
2. **Các nút chức năng (thêm, sửa, xóa) được ẩn/hiện dựa trên Position**
3. **Admin vẫn có quyền cao nhất** - xem và làm tất cả

## Cấu trúc Position (Vị trí)

Các vị trí nhân viên vẫn giữ nguyên:

```typescript
enum Position {
  SALE              // Nhân viên bán hàng
  CSKH              // Chăm sóc khách hàng
  PRODUCT_MANAGER   // Quản lý sản phẩm
  WAREHOUSE         // Nhân viên kho
  ACCOUNTANT        // Kế toán
  SHIPPER           // Tài xế giao hàng
}
```

## Hệ thống Permission

### File: `src/frontend/lib/permissions.ts`

Permission được chia thành 2 loại:
- **VIEW**: Tất cả nhân viên đều có (không cần khai báo)
- **ACTION**: Chỉ một số position mới có (cần khai báo)

### Ví dụ Permission theo Position:

#### 1. PRODUCT_MANAGER (Quản lý sản phẩm)
```typescript
PRODUCT_MANAGER: [
  'products.create',
  'products.edit',
  'products.delete',
  'categories.create',
  'categories.edit',
  'categories.delete',
  // KHÔNG có warehouse.import/export
]
```

**Nghĩa là:**
- ✅ Xem được trang Nhập kho, Xuất kho
- ❌ KHÔNG thấy nút "Tạo phiếu nhập", "Tạo phiếu xuất"
- ✅ Có nút "Thêm sản phẩm", "Sửa sản phẩm"

#### 2. WAREHOUSE (Nhân viên kho)
```typescript
WAREHOUSE: [
  'warehouse.import.create',
  'warehouse.import.approve',
  'warehouse.export.create',
  'warehouse.export.approve',
  'suppliers.create',
  'suppliers.edit',
]
```

**Nghĩa là:**
- ✅ Xem được trang Nhập kho, Xuất kho
- ✅ CÓ nút "Tạo phiếu nhập", "Tạo phiếu xuất"
- ✅ Có thể duyệt phiếu nhập/xuất
- ❌ KHÔNG có nút "Thêm sản phẩm", "Sửa sản phẩm"

#### 3. SALE (Nhân viên bán hàng)
```typescript
SALE: [
  'orders.create',
  'orders.edit',
  'orders.confirm',
  'orders.cancel',
  'customers.edit',
]
```

#### 4. ACCOUNTANT (Kế toán)
```typescript
ACCOUNTANT: [
  'accounting.reconciliation.edit',
  'accounting.payables.create',
  'accounting.payables.edit',
  'accounting.payables.delete',
  'bank_accounts.create',
  'bank_accounts.edit',
  'bank_accounts.delete',
]
```

## Cách sử dụng trong Component

### 1. Import permission helper
```typescript
import { hasPermission, type Position } from '@/lib/permissions'
import { useAuthStore } from '@/store/authStore'
```

### 2. Check permission
```typescript
const { employee } = useAuthStore()
const canCreate = hasPermission(employee?.position as Position, 'warehouse.import.create')
```

### 3. Hiển thị có điều kiện
```typescript
{canCreate && (
  <button>Tạo phiếu nhập</button>
)}

{!canCreate && (
  <div className="text-gray-500">Bạn chỉ có quyền xem</div>
)}
```

## Ví dụ thực tế: Trang Nhập kho

### File: `src/frontend/app/employee/warehouse/import/page.tsx`

```typescript
export default function WarehouseImportPage() {
  const { employee } = useAuthStore()
  
  // Check permissions
  const canCreate = hasPermission(employee?.position as Position, 'warehouse.import.create')
  const canApprove = hasPermission(employee?.position as Position, 'warehouse.import.approve')

  return (
    <div>
      {/* Header với nút có điều kiện */}
      <div className="flex justify-between">
        <h1>Phiếu nhập kho</h1>
        
        {/* Chỉ hiện nút nếu có quyền */}
        {canCreate && (
          <Link href="/employee/warehouse/import/create">
            Tạo phiếu nhập
          </Link>
        )}
        
        {/* Hiện thông báo nếu không có quyền */}
        {!canCreate && (
          <div>Bạn chỉ có quyền xem</div>
        )}
      </div>

      {/* Thông báo quyền hạn */}
      {!canCreate && (
        <div className="alert">
          Bạn có thể xem danh sách và chi tiết phiếu nhập kho, 
          nhưng không thể tạo hoặc chỉnh sửa.
        </div>
      )}

      {/* Danh sách - tất cả đều xem được */}
      <table>
        {/* ... */}
      </table>
    </div>
  )
}
```

## Cấu trúc Folder mới

```
src/frontend/app/
├── admin/                    # Admin (quyền cao nhất)
│   ├── page.tsx
│   └── ...
├── employee/                 # Giao diện chung cho tất cả nhân viên
│   ├── layout.tsx           # Sidebar menu chung
│   ├── page.tsx             # Dashboard chung
│   ├── products/            # Tất cả xem được
│   ├── categories/          # Tất cả xem được
│   ├── warehouse/           # Tất cả xem được
│   │   ├── import/          # Nhưng chỉ WAREHOUSE có nút thêm
│   │   ├── export/          # Nhưng chỉ WAREHOUSE có nút thêm
│   │   └── inventory/
│   ├── orders/              # Tất cả xem được
│   ├── customers/           # Tất cả xem được
│   ├── suppliers/           # Tất cả xem được
│   ├── accounting/          # Tất cả xem được
│   │   ├── reconciliation/  # Nhưng chỉ ACCOUNTANT có nút sửa
│   │   ├── payables/        # Nhưng chỉ ACCOUNTANT có nút thêm/sửa
│   │   └── bank-accounts/   # Nhưng chỉ ACCOUNTANT có nút thêm/sửa
│   └── shipping/            # Tất cả xem được
└── ...
```

## Migration từ giao diện cũ

### Trước đây:
- `/product-manager` - Riêng cho Product Manager
- `/warehouse` - Riêng cho Warehouse
- `/sales` - Riêng cho Sales
- `/shipper` - Riêng cho Shipper

### Bây giờ:
- `/employee` - Chung cho TẤT CẢ nhân viên
- Phân quyền bằng cách ẩn/hiện nút chức năng

## Lợi ích

1. **Dễ quản lý**: Chỉ 1 layout, 1 sidebar, 1 routing
2. **Linh hoạt**: Nhân viên có thể xem công việc của nhau
3. **Minh bạch**: Mọi người thấy được quy trình làm việc
4. **Dễ mở rộng**: Thêm permission mới không cần tạo route mới

## Checklist Implementation

### Backend (Không cần thay đổi)
- ✅ Position enum vẫn giữ nguyên
- ✅ Employee entity vẫn giữ nguyên
- ✅ API vẫn hoạt động bình thường

### Frontend
- ✅ Tạo `src/frontend/lib/permissions.ts`
- ✅ Tạo `src/frontend/app/employee/layout.tsx`
- ✅ Tạo `src/frontend/app/employee/page.tsx`
- ✅ Cập nhật `src/frontend/store/authStore.ts` (thêm employee field)
- ⏳ Di chuyển các trang từ `/product-manager`, `/warehouse`, `/sales` sang `/employee`
- ⏳ Thêm permission check vào từng trang
- ⏳ Cập nhật routing và redirect

## Các trang cần implement

### 1. Products
- [ ] `/employee/products` - Danh sách sản phẩm
- [ ] `/employee/products/create` - Thêm sản phẩm (chỉ PRODUCT_MANAGER)
- [ ] `/employee/products/[id]` - Chi tiết sản phẩm
- [ ] `/employee/products/[id]/edit` - Sửa sản phẩm (chỉ PRODUCT_MANAGER)

### 2. Categories
- [ ] `/employee/categories` - Danh sách danh mục
- [ ] Nút thêm/sửa/xóa (chỉ PRODUCT_MANAGER)

### 3. Warehouse
- [x] `/employee/warehouse/import` - Phiếu nhập kho (ví dụ đã tạo)
- [ ] `/employee/warehouse/import/create` - Tạo phiếu nhập (chỉ WAREHOUSE)
- [ ] `/employee/warehouse/export` - Phiếu xuất kho
- [ ] `/employee/warehouse/export/create` - Tạo phiếu xuất (chỉ WAREHOUSE)
- [ ] `/employee/warehouse/inventory` - Tồn kho
- [ ] `/employee/warehouse/reports` - Báo cáo

### 4. Orders
- [ ] `/employee/orders` - Danh sách đơn hàng
- [ ] `/employee/orders/[id]` - Chi tiết đơn hàng
- [ ] Nút xác nhận/hủy (SALE, CSKH)

### 5. Accounting
- [ ] `/employee/accounting/reconciliation` - Đối soát
- [ ] `/employee/accounting/payables` - Công nợ NCC
- [ ] `/employee/accounting/statements` - Báo cáo tài chính
- [ ] `/employee/accounting/bank-accounts` - Tài khoản ngân hàng

### 6. Shipping
- [ ] `/employee/shipping` - Danh sách đơn giao hàng
- [ ] Nút cập nhật trạng thái (chỉ SHIPPER)

## Testing

### Test case 1: Product Manager
1. Login với account PRODUCT_MANAGER
2. Vào `/employee/warehouse/import`
3. ✅ Xem được danh sách phiếu nhập
4. ❌ KHÔNG thấy nút "Tạo phiếu nhập"
5. ✅ Thấy thông báo "Bạn chỉ có quyền xem"

### Test case 2: Warehouse Staff
1. Login với account WAREHOUSE
2. Vào `/employee/warehouse/import`
3. ✅ Xem được danh sách phiếu nhập
4. ✅ CÓ nút "Tạo phiếu nhập"
5. ✅ Click được vào tạo phiếu mới

### Test case 3: Admin
1. Login với account ADMIN
2. Vào `/admin` (vẫn giữ giao diện riêng)
3. ✅ Có tất cả quyền
4. ✅ Xem được tất cả

## Notes

- Admin vẫn giữ giao diện riêng tại `/admin`
- Employee dùng giao diện chung tại `/employee`
- Tất cả employee đều xem được tất cả trang
- Chỉ các nút chức năng mới bị ẩn theo permission
