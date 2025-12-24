# ✅ Hoàn thành tính năng Publish Sản phẩm

## 🎯 Tổng quan

Đã tạo đầy đủ các trang để PRODUCT_MANAGER đăng bán sản phẩm từ kho lên trang bán hàng.

## 📁 Files đã tạo

### **1. Admin Publish Page**
```
src/frontend/app/admin/products/publish/page.tsx
```
- Dành cho ADMIN
- Hiển thị danh sách warehouse_products
- Form đăng bán với đầy đủ thông tin
- Upload nhiều ảnh (tối đa 9)

### **2. Employee Publish Page**
```
src/frontend/app/employee/products/publish/page.tsx
```
- Dành cho EMPLOYEE (có permission check)
- Chỉ PRODUCT_MANAGER mới truy cập được
- Tương tự Admin nhưng có kiểm tra quyền

### **3. Product Manager Publish Page**
```
src/frontend/app/product-manager/products/publish/page.tsx
```
- Dành cho PRODUCT_MANAGER (legacy route)
- Tương tự Employee nhưng check position trực tiếp

---

## 🔄 Luồng hoạt động

### **Bước 1: Vào trang Publish**
```
/admin/products/publish
/employee/products/publish
/product-manager/products/publish
```

### **Bước 2: Xem danh sách sản phẩm kho**
- API: `GET /api/products/warehouse/list`
- Hiển thị:
  - SKU
  - Tên sản phẩm (internalName)
  - Nhà cung cấp
  - Tồn kho (sellableQuantity)
  - Trạng thái (đã/chưa đăng bán)

### **Bước 3: Chọn sản phẩm chưa đăng bán**
- Click nút "Đăng bán"
- Mở modal với form

### **Bước 4: Điền thông tin**
**Form fields:**
- ✅ Tên hiển thị (required) - Tên cho khách hàng
- ✅ Mô tả (optional) - Mô tả chi tiết
- ✅ Giá bán (required) - Phải > 0
- ✅ Danh mục (required) - Chọn từ dropdown
- ✅ Hình ảnh (optional) - Upload tối đa 9 ảnh

### **Bước 5: Submit**
- API: `POST /api/products/warehouse/publish`
- Request body:
```json
{
  "warehouseProductId": 123,
  "name": "iPhone 15 Pro Max 256GB",
  "description": "Mô tả...",
  "price": 30000000,
  "categoryId": 5
}
```

### **Bước 6: Upload ảnh (nếu có)**
- API: `POST /api/products/{productId}/images`
- Ảnh đầu tiên tự động là primary

---

## ✨ Tính năng

### **1. Thống kê**
- Tổng sản phẩm kho
- Chưa đăng bán (màu vàng)
- Đã đăng bán (màu xanh)

### **2. Tìm kiếm**
- Tìm theo tên sản phẩm
- Tìm theo SKU
- Real-time search

### **3. Trạng thái sản phẩm**
- **Chưa đăng bán**: Hiện nút "Đăng bán"
- **Đã đăng bán**: Hiện link "Xem sản phẩm"

### **4. Validation**
- Tên hiển thị: Bắt buộc
- Giá bán: Bắt buộc, phải > 0
- Danh mục: Bắt buộc
- Hiển thị giá format VND khi nhập

### **5. Upload ảnh**
- Component: `MultiImageUpload`
- Tối đa 9 ảnh
- Ảnh đầu tiên là primary
- Preview trước khi upload
- Drag & drop để sắp xếp

---

## 🔐 Phân quyền

### **Admin**
```
✅ Truy cập: /admin/products/publish
✅ Quyền: Không cần check (ADMIN có tất cả quyền)
```

### **Employee**
```
✅ Truy cập: /employee/products/publish
✅ Quyền: hasPermission(position, 'products.create')
✅ Position: PRODUCT_MANAGER
```

### **Product Manager (Legacy)**
```
✅ Truy cập: /product-manager/products/publish
✅ Quyền: position === 'PRODUCT_MANAGER'
```

---

## 📊 API Endpoints sử dụng

### **1. Lấy danh sách sản phẩm kho**
```typescript
productApi.getWarehouseProductsForPublish()
// GET /api/products/warehouse/list
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "sku": "IP15-256-BLK",
      "internalName": "iPhone 15 256GB Black",
      "description": "...",
      "supplierName": "Apple Vietnam",
      "stockQuantity": 50,
      "sellableQuantity": 45,
      "isPublished": false,
      "publishedProductId": null
    }
  ]
}
```

### **2. Đăng bán sản phẩm**
```typescript
productApi.createProductFromWarehouse(data)
// POST /api/products/warehouse/publish
```

**Request:**
```json
{
  "warehouseProductId": 1,
  "name": "iPhone 15 256GB",
  "description": "Mô tả sản phẩm",
  "price": 25000000,
  "categoryId": 5
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng bán sản phẩm thành công",
  "data": {
    "id": 100,
    "name": "iPhone 15 256GB",
    "sku": "IP15-256-BLK",
    "price": 25000000,
    "warehouseProduct": { "id": 1 }
  }
}
```

### **3. Upload ảnh sản phẩm**
```typescript
productApi.addProductImage(productId, imageUrl, isPrimary)
// POST /api/products/{productId}/images
```

### **4. Lấy danh mục**
```typescript
categoryApi.getAll()
// GET /api/categories
```

---

## 🎨 UI Components

### **1. Stats Cards**
- Tổng sản phẩm kho (màu xanh dương)
- Chưa đăng bán (màu vàng)
- Đã đăng bán (màu xanh lá)

### **2. Search Bar**
- Icon search
- Placeholder: "Tìm kiếm theo tên, SKU..."
- Real-time filter

### **3. Products Table**
- Columns: SKU, Tên, NCC, Tồn kho, Trạng thái, Thao tác
- Hover effect
- Responsive

### **4. Publish Modal**
- Full screen overlay
- Scrollable content
- Form validation
- Loading state
- Success/Error toast

### **5. MultiImageUpload**
- Drag & drop
- Preview thumbnails
- Delete image
- Reorder images
- Max 9 images

---

## 🧪 Testing

### **Test Case 1: Truy cập trang**
1. Đăng nhập với PRODUCT_MANAGER
2. Vào `/employee/products/publish`
3. ✅ Thấy danh sách sản phẩm kho

### **Test Case 2: Đăng bán sản phẩm**
1. Chọn sản phẩm chưa đăng bán
2. Click "Đăng bán"
3. Điền form đầy đủ
4. Upload 3 ảnh
5. Click "Đăng bán"
6. ✅ Thành công, sản phẩm chuyển sang "Đã đăng bán"

### **Test Case 3: Validation**
1. Mở form đăng bán
2. Để trống tên → ❌ Lỗi
3. Nhập giá = 0 → ❌ Lỗi
4. Không chọn danh mục → ❌ Lỗi
5. Điền đầy đủ → ✅ Submit thành công

### **Test Case 4: Permission**
1. Đăng nhập với WAREHOUSE
2. Vào `/employee/products/publish`
3. ❌ Redirect về `/employee/products`
4. Toast: "Bạn không có quyền đăng bán sản phẩm"

---

## 🔗 Navigation

### **Thêm link vào các trang list**

**File: `/admin/products/page.tsx`**
```tsx
<Link href="/admin/products/publish">
  Đăng bán sản phẩm từ kho
</Link>
```

**File: `/employee/products/page.tsx`**
```tsx
{canCreate && (
  <Link href="/employee/products/publish">
    Đăng bán sản phẩm mới
  </Link>
)}
```

**File: `/product-manager/products/page.tsx`**
```tsx
<Link href="/product-manager/products/publish">
  Đăng bán sản phẩm mới
</Link>
```

---

## ✅ Checklist hoàn thành

### **Backend (Đã có sẵn)**
- ✅ API `/api/products/warehouse/list`
- ✅ API `/api/products/warehouse/publish`
- ✅ API `/api/products/{id}/images`
- ✅ API `/api/categories`

### **Frontend (Vừa tạo)**
- ✅ Trang `/admin/products/publish/page.tsx`
- ✅ Trang `/employee/products/publish/page.tsx`
- ✅ Trang `/product-manager/products/publish/page.tsx`
- ✅ Permission check cho Employee
- ✅ Form validation
- ✅ Upload nhiều ảnh
- ✅ Loading states
- ✅ Error handling

### **Cần làm tiếp (Optional)**
- ⏳ Thêm link "Đăng bán" vào các trang list
- ⏳ Thêm chức năng "Gỡ sản phẩm" (unpublish)
- ⏳ Thêm chức năng "Ẩn/Hiện" sản phẩm (toggle active)

---

## 🎯 Kết quả

**Trước khi có trang Publish:**
- ❌ Không có cách nào để đăng bán sản phẩm từ kho
- ❌ PRODUCT_MANAGER không thể làm việc
- ❌ Trang khách hàng không có sản phẩm

**Sau khi có trang Publish:**
- ✅ PRODUCT_MANAGER chọn sản phẩm từ kho
- ✅ Điền thông tin bán hàng (tên, giá, mô tả, danh mục)
- ✅ Upload ảnh sản phẩm
- ✅ Sản phẩm xuất hiện trên trang khách hàng
- ✅ Tồn kho tự động sync từ InventoryStock

---

## 📝 Lưu ý quan trọng

### **1. Tồn kho**
- Lấy từ `sellableQuantity` (không phải `stockQuantity`)
- `sellableQuantity` = `onHand` - `reserved` - `damaged`
- Tự động sync khi publish

### **2. SKU**
- Copy từ `warehouse_products.sku`
- Không cho phép sửa
- Unique trong cả 2 bảng

### **3. Thông số kỹ thuật**
- Copy từ `warehouse_products.techSpecsJson`
- Format JSON
- Có thể search/filter

### **4. Ảnh sản phẩm**
- Lưu riêng trong bảng `product_images`
- Ảnh đầu tiên là primary
- Tối đa 9 ảnh

---

**Ngày hoàn thành:** 24/12/2024
**Trạng thái:** ✅ Sẵn sàng sử dụng
