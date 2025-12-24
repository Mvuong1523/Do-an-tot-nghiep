# Warehouse Backend API Complete - Tổng hợp đầy đủ API Backend Kho

## Tổng quan
Document này tổng hợp TẤT CẢ các API endpoints của module Inventory (Kho) từ backend để đảm bảo frontend implement đúng 100%.

## 1. Purchase Orders (Phiếu Nhập Kho)

### 1.1. Tạo Phiếu Nhập Kho
```
POST /api/inventory/create_pchaseOrder
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Request Body (CreatePORequest):**
```json
{
  "createdBy": "string (required)",
  "poCode": "string (required)",
  "supplier": {
    "name": "string",
    "taxCode": "string",
    "contactName": "string",
    "phone": "string",
    "email": "string",
    "address": "string",
    "bankAccount": "string",
    "paymentTerm": "string",
    "paymentTermDays": "integer",
    "active": "boolean"
  },
  "items": [
    {
      "sku": "string (required)",
      "quantity": "long (required, > 0)",
      "internalName": "string",
      "techSpecsJson": "string (JSON format)",
      "unitCost": "double (required, > 0)",
      "warrantyMonths": "integer (>= 0)",
      "note": "string"
    }
  ],
  "note": "string"
}
```

**Frontend Status:**
- ✅ Admin: `/admin/warehouse/import/create/page.tsx`
- ✅ Employee: `/employee/warehouse/import/create/page.tsx`
- ✅ Có đầy đủ 9 trường cho supplier
- ✅ Có đầy đủ 7 trường cho items
- ✅ Có validation JSON cho techSpecsJson
- ✅ Có Excel import

### 1.2. Hoàn Tất Phiếu Nhập (Import Stock)
```
POST /api/inventory/import
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Request Body (CompletePORequest):**
```json
{
  "purchaseOrderId": "long (required)",
  "receivedDate": "string (ISO date)",
  "note": "string"
}
```

**Frontend Status:**
- ⚠️ Cần kiểm tra: `/admin/warehouse/import/complete/page.tsx`
- ⚠️ Cần kiểm tra: `/employee/warehouse/import/complete/page.tsx`

### 1.3. Lấy Danh Sách Phiếu Nhập
```
GET /api/inventory/purchase-orders?status={status}
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Query Params:**
- `status` (optional): CREATED, RECEIVED, CANCELLED

**Frontend Status:**
- ✅ Admin: `/admin/warehouse/import/page.tsx`
- ✅ Employee: `/employee/warehouse/import/page.tsx`

### 1.4. Lấy Chi Tiết Phiếu Nhập
```
GET /api/inventory/purchase-orders/{id}
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Frontend Status:**
- ✅ Admin: `/admin/warehouse/import/[id]/page.tsx`
- ✅ Employee: `/employee/warehouse/import/[id]/page.tsx`

### 1.5. Hủy Phiếu Nhập
```
PUT /api/inventory/purchase-orders/{id}/cancel
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Frontend Status:**
- ⚠️ Cần kiểm tra có button cancel trong detail page

## 2. Export Orders (Phiếu Xuất Kho)

### 2.1. Tạo Phiếu Xuất Kho (Generic)
```
POST /api/inventory/create
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Request Body (CreateExportOrderRequest):**
```json
{
  "createdBy": "string (required)",
  "reason": "string (required)",
  "note": "string",
  "items": [
    {
      "productSku": "string (required)",
      "serialNumbers": ["string (required, not empty)"]
    }
  ]
}
```

**Frontend Status:**
- ✅ Admin: `/admin/warehouse/export/create/page.tsx`
- ✅ Employee: `/employee/warehouse/export/create/page.tsx`
- ✅ Có dropdown chọn reason
- ✅ Có textarea nhập serial numbers
- ✅ Parse serial numbers (comma or newline separated)

### 2.2. Xuất Kho Cho Bán Hàng
```
POST /api/inventory/export-for-sale
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Request Body (SaleExportRequest):**
```json
{
  "orderId": "long (required)",
  "reason": "string (required)",
  "items": [
    {
      "productSku": "string (required)",
      "serialNumbers": ["string"]
    }
  ]
}
```

**Note:** `createdBy` được set tự động từ Authentication

**Frontend Status:**
- ⚠️ Cần tạo: Form xuất kho cho đơn hàng cụ thể
- ⚠️ Có thể integrate vào `/admin/warehouse/orders/page.tsx`

### 2.3. Xuất Kho Cho Bảo Hành
```
POST /api/inventory/export-for-warranty
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Request Body (WarrantyExportRequest):**
```json
{
  "customerName": "string (required)",
  "customerPhone": "string (required)",
  "warrantyReason": "string (required)",
  "items": [
    {
      "productSku": "string (required)",
      "serialNumbers": ["string"]
    }
  ]
}
```

**Note:** `createdBy` được set tự động từ Authentication

**Frontend Status:**
- ❌ Chưa có: Cần tạo form xuất kho bảo hành

### 2.4. Lấy Danh Sách Phiếu Xuất
```
GET /api/inventory/export-orders?status={status}
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Query Params:**
- `status` (optional): PENDING, COMPLETED, CANCELLED

**Frontend Status:**
- ✅ Admin: `/admin/warehouse/export/page.tsx`
- ✅ Employee: `/employee/warehouse/export/page.tsx`

### 2.5. Lấy Chi Tiết Phiếu Xuất
```
GET /api/inventory/export-orders/{id}
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Frontend Status:**
- ✅ Admin: `/admin/warehouse/export/[id]/page.tsx`
- ✅ Employee: `/employee/warehouse/export/[id]/page.tsx`

### 2.6. Hủy Phiếu Xuất
```
PUT /api/inventory/export-orders/{id}/cancel
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Frontend Status:**
- ⚠️ Cần kiểm tra có button cancel trong detail page

## 3. Suppliers (Nhà Cung Cấp)

### 3.1. Lấy Danh Sách NCC
```
GET /api/inventory/suppliers
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Frontend Status:**
- ✅ Admin: `/admin/warehouse/suppliers/page.tsx`
- ✅ Employee: `/employee/warehouse/suppliers/page.tsx`
- ✅ Được dùng trong form tạo phiếu nhập

### 3.2. Tạo/Lấy NCC
```
POST /api/inventory/suppliers
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Request Body (CreateSupplierRequest):**
```json
{
  "name": "string",
  "taxCode": "string",
  "contactName": "string",
  "phone": "string",
  "email": "string",
  "address": "string",
  "bankAccount": "string",
  "paymentTerm": "string",
  "paymentTermDays": "integer",
  "active": "boolean"
}
```

**Note:** Nếu taxCode đã tồn tại, trả về NCC có sẵn. Nếu chưa, tạo mới.

**Frontend Status:**
- ✅ Được dùng trong form tạo phiếu nhập (option "Tạo NCC mới")
- ✅ Có đầy đủ 10 trường

### 3.3. Lấy Sản Phẩm Theo NCC
```
GET /api/inventory/supplier/{supplierId}/products
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Frontend Status:**
- ❌ Chưa dùng: Có thể thêm vào trang suppliers để xem sản phẩm của NCC

## 4. Stock (Tồn Kho)

### 4.1. Lấy Tồn Kho
```
GET /api/inventory/stock
Authorization: Bearer {token}
Permission: WAREHOUSE, PRODUCT_MANAGER, ADMIN
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "long",
      "warehouseProduct": {
        "id": "long",
        "sku": "string",
        "internalName": "string",
        "description": "string",
        "supplier": {
          "id": "long",
          "name": "string",
          "taxCode": "string"
        }
      },
      "onHand": "long",
      "reserved": "long",
      "damaged": "long",
      "sellable": "long"
    }
  ]
}
```

**Frontend Status:**
- ✅ Admin: `/admin/warehouse/inventory/page.tsx`
- ✅ Employee: `/employee/warehouse/inventory/page.tsx`
- ✅ Được dùng trong form xuất kho (dropdown chọn sản phẩm)
- ✅ Được dùng trong trang overview

## 5. Search & Filter

### 5.1. Tìm Kiếm Theo Thông Số
```
GET /api/inventory/search?keyword={keyword}
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Frontend Status:**
- ❌ Chưa có: Có thể thêm vào trang products

### 5.2. Lọc Theo Thông Số
```
GET /api/inventory/filter?key={key}&value={value}
Authorization: Bearer {token}
Permission: WAREHOUSE, ADMIN
```

**Frontend Status:**
- ❌ Chưa có: Có thể thêm vào trang products

## 6. Warehouse Products

### 6.1. Lấy Danh Sách Sản Phẩm Kho
```
GET /api/inventory/warehouse-products
Authorization: Bearer {token}
Permission: WAREHOUSE, PRODUCT_MANAGER, ADMIN
```

**Frontend Status:**
- ✅ Admin: `/admin/warehouse/products/page.tsx`
- ✅ Employee: `/employee/warehouse/products/page.tsx`

## Checklist Frontend vs Backend

### ✅ Đã Implement Đầy Đủ:
1. ✅ Tạo phiếu nhập kho (với Excel import)
2. ✅ Danh sách phiếu nhập
3. ✅ Chi tiết phiếu nhập
4. ✅ Tạo phiếu xuất kho (generic)
5. ✅ Danh sách phiếu xuất
6. ✅ Chi tiết phiếu xuất
7. ✅ Danh sách NCC
8. ✅ Tạo NCC (trong form nhập kho)
9. ✅ Xem tồn kho
10. ✅ Danh sách sản phẩm kho

### ⚠️ Cần Kiểm Tra:
1. ⚠️ Form hoàn tất phiếu nhập (`/import/complete`)
2. ⚠️ Button hủy phiếu nhập/xuất trong detail page
3. ⚠️ Trang đơn hàng cần xuất (`/warehouse/orders`)

### ❌ Chưa Implement:
1. ❌ Form xuất kho cho bán hàng (export-for-sale)
2. ❌ Form xuất kho cho bảo hành (export-for-warranty)
3. ❌ Tìm kiếm theo thông số kỹ thuật
4. ❌ Lọc theo thông số kỹ thuật
5. ❌ Xem sản phẩm theo NCC

## Các DTO Quan Trọng

### CreatePORequest
```typescript
interface CreatePORequest {
  createdBy: string;          // Required
  poCode: string;             // Required
  supplier: {
    name?: string;
    taxCode?: string;
    contactName?: string;
    phone?: string;
    email?: string;
    address?: string;
    bankAccount?: string;
    paymentTerm?: string;
    paymentTermDays?: number;
    active?: boolean;
  };
  items: POItemRequest[];     // Required, not empty
  note?: string;
}

interface POItemRequest {
  sku: string;                // Required
  quantity: number;           // Required, > 0
  internalName?: string;
  techSpecsJson?: string;     // JSON format
  unitCost: number;           // Required, > 0
  warrantyMonths?: number;    // >= 0
  note?: string;
}
```

### CreateExportOrderRequest
```typescript
interface CreateExportOrderRequest {
  createdBy: string;          // Required
  reason: string;             // Required
  note?: string;
  items: ExportItemRequest[]; // Required, not empty
}

interface ExportItemRequest {
  productSku: string;         // Required
  serialNumbers: string[];    // Required, not empty
}
```

### CompletePORequest
```typescript
interface CompletePORequest {
  purchaseOrderId: number;    // Required
  receivedDate?: string;      // ISO date
  note?: string;
}
```

## Permissions

### WAREHOUSE (Nhân viên kho):
- ✅ Tạo phiếu nhập
- ✅ Hoàn tất phiếu nhập
- ✅ Tạo phiếu xuất
- ✅ Xem/Quản lý NCC
- ✅ Xem tồn kho
- ✅ Xem sản phẩm kho
- ❌ KHÔNG tạo/sửa sản phẩm (chỉ PRODUCT_MANAGER)

### PRODUCT_MANAGER (Quản lý sản phẩm):
- ✅ Xem tồn kho (read-only)
- ✅ Xem sản phẩm kho
- ✅ Tạo/Sửa/Xóa sản phẩm
- ❌ KHÔNG nhập/xuất kho

### ADMIN:
- ✅ Tất cả quyền

## Kết luận

**Frontend đã implement:**
- ✅ 10/15 endpoints chính (67%)
- ✅ Tất cả CRUD cơ bản cho nhập/xuất kho
- ✅ Form đầy đủ các trường theo DTO
- ✅ Validation đúng
- ✅ Permission check đúng

**Cần bổ sung:**
- ❌ Form xuất kho cho bán hàng (có orderId)
- ❌ Form xuất kho cho bảo hành
- ❌ Tìm kiếm/lọc theo thông số kỹ thuật
- ⚠️ Kiểm tra form hoàn tất phiếu nhập
- ⚠️ Thêm button hủy phiếu
