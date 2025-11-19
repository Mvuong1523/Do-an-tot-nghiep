# Hệ thống quản lý Danh mục (Category)

## ✅ Đã hoàn thành Backend

### 1. Entity (Category.java)
- Hỗ trợ phân cấp danh mục (parent/children)
- Thêm các trường: slug, imageUrl, displayOrder, active
- Helper method: getProductCount()

### 2. DTO
- **CategoryDTO**: Hiển thị danh mục với children
- **CreateCategoryRequest**: Tạo/cập nhật danh mục

### 3. Repository
- `existsBySlug(String slug)`
- `findBySlug(String slug)`
- `findByParentIsNull()` - Lấy danh mục gốc
- `findByParentId(Long parentId)` - Lấy danh mục con
- `findByActiveTrue()` - Lấy danh mục đang hoạt động

### 4. Service
- `getAllCategoriesTree()` - Lấy danh mục dạng cây
- `getActiveCategories()` - Lấy danh mục active
- `getCategoryWithProducts(Long id)` - Chi tiết danh mục
- `createCategory(CreateCategoryRequest)` - Tạo danh mục
- `updateCategory(Long id, CreateCategoryRequest)` - Cập nhật
- `toCategoryDTO(Category)` - Convert sang DTO
- `generateSlug(String name)` - Tạo slug từ tên tiếng Việt

### 5. Controller
**Public endpoints:**
- `GET /api/categories` - Tất cả danh mục
- `GET /api/categories/tree` - Danh mục dạng cây
- `GET /api/categories/active` - Danh mục đang hoạt động
- `GET /api/categories/{id}` - Chi tiết danh mục

**Admin endpoints:**
- `POST /api/categories` - Tạo danh mục (ADMIN, EMPLOYEE)
- `PUT /api/categories/{id}` - Cập nhật (ADMIN, EMPLOYEE)
- `DELETE /api/categories/{id}` - Xóa (ADMIN only)

## 📋 Cần làm tiếp Frontend

### 1. Admin - Quản lý danh mục
- `/admin/categories` - Danh sách danh mục (dạng bảng hoặc cây)
- `/admin/categories/create` - Tạo danh mục mới
- `/admin/categories/edit/{id}` - Sửa danh mục
- Tính năng: Drag & drop để sắp xếp, phân cấp

### 2. Customer - Hiển thị sản phẩm
- `/products` - Trang danh sách sản phẩm
- `/products/category/{slug}` - Sản phẩm theo danh mục
- Sidebar: Danh mục dạng cây
- Filter: Giá, thương hiệu, đánh giá
- Sort: Mới nhất, giá tăng/giảm, bán chạy

### 3. Components
- `CategoryTree` - Hiển thị danh mục dạng cây
- `ProductCard` - Card sản phẩm
- `ProductFilter` - Bộ lọc sản phẩm
- `ProductSort` - Sắp xếp sản phẩm

## 🎨 Thiết kế UI

### Admin
```
┌─────────────────────────────────────────┐
│ Quản lý danh mục                [+ Thêm]│
├─────────────────────────────────────────┤
│ 📱 Điện thoại & Phụ kiện        [Sửa][Xóa]│
│   ├─ 📱 Điện thoại (150)        [Sửa][Xóa]│
│   │   ├─ iPhone (50)            [Sửa][Xóa]│
│   │   ├─ Samsung (60)           [Sửa][Xóa]│
│   │   └─ Xiaomi (40)            [Sửa][Xóa]│
│   └─ 🎧 Phụ kiện (80)           [Sửa][Xóa]│
│ 💻 Laptop & Máy tính            [Sửa][Xóa]│
│   ├─ 💻 Laptop (100)            [Sửa][Xóa]│
│   └─ 🖥️ PC & Linh kiện (50)    [Sửa][Xóa]│
└─────────────────────────────────────────┘
```

### Customer
```
┌─────────────┬───────────────────────────┐
│ Danh mục    │ Sản phẩm                  │
├─────────────┤                           │
│ 📱 Điện thoại│ [Sản phẩm 1] [Sản phẩm 2]│
│   iPhone    │ [Sản phẩm 3] [Sản phẩm 4]│
│   Samsung   │                           │
│   Xiaomi    │ Lọc: [Giá] [Thương hiệu]  │
│ 💻 Laptop    │ Sắp xếp: [Mới nhất ▼]    │
│   Gaming    │                           │
│   Văn phòng │                           │
└─────────────┴───────────────────────────┘
```

## 🔄 Tiếp theo
Tôi sẽ tạo:
1. Trang quản lý danh mục cho admin
2. Trang hiển thị sản phẩm cho khách hàng
3. Components tái sử dụng
