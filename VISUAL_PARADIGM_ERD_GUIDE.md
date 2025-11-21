# Hướng dẫn vẽ ERD trên Visual Paradigm

## Bước 1: Chuẩn bị

1. Mở Visual Paradigm
2. Tạo project mới: `File` → `New` → `Project`
3. Chọn `Diagram` → `New` → `Entity Relationship Diagram`

## Bước 2: Tạo các Entity (Bảng)

### Cách tạo Entity:
1. Từ Toolbar bên trái, chọn `Entity`
2. Click vào canvas để tạo entity
3. Đặt tên cho entity (tên bảng)
4. Double-click vào entity để thêm các columns (cột)

### Cách thêm Columns:
1. Double-click vào entity
2. Click nút `+` để thêm column mới
3. Nhập tên column, chọn data type
4. Đánh dấu `PK` (Primary Key) nếu là khóa chính
5. Đánh dấu `FK` (Foreign Key) nếu là khóa ngoại
6. Đánh dấu `U` (Unique) nếu là unique constraint
7. Đánh dấu `NN` (Not Null) nếu không cho phép null

## Bước 3: Tạo Relationships (Mối quan hệ)

### Các loại Relationship:
- **One-to-One (1:1)**: Chọn `One-to-One Relationship`
- **One-to-Many (1:N)**: Chọn `One-to-Many Relationship`
- **Many-to-Many (M:N)**: Chọn `Many-to-Many Relationship`

### Cách tạo Relationship:
1. Từ Toolbar, chọn loại relationship
2. Click vào entity nguồn (parent)
3. Kéo đến entity đích (child)
4. Visual Paradigm sẽ tự động tạo đường thẳng nối

### Cách chỉnh đường nối thẳng:
1. Click vào đường nối
2. Chọn `Format` → `Line Style` → `Rectilinear` (đường thẳng góc vuông)
3. Hoặc chọn `Oblique` (đường thẳng chéo)

## Bước 4: Sắp xếp Layout

### Tự động sắp xếp:
1. Chọn tất cả entities: `Ctrl + A`
2. Click chuột phải → `Layout` → `Hierarchical Layout`
3. Hoặc chọn `Orthogonal Layout` để có đường nối vuông góc

### Thủ công sắp xếp:
1. Kéo thả các entity để sắp xếp theo ý muốn
2. Nhóm các entity liên quan gần nhau
3. Giữ khoảng cách đều giữa các entity

## Bước 5: Tùy chỉnh giao diện

### Thay đổi màu sắc:
1. Click vào entity
2. Chọn `Format` → `Fill Color`
3. Chọn màu phù hợp cho từng module

### Thay đổi font chữ:
1. Click vào entity
2. Chọn `Format` → `Font`
3. Chọn font size và style

### Thêm ghi chú:
1. Chọn `Note` từ Toolbar
2. Click vào canvas
3. Nhập nội dung ghi chú
4. Kéo đường nối từ note đến entity

## Bước 6: Export

1. `File` → `Export` → `Active Diagram as Image`
2. Chọn định dạng: PNG, JPG, SVG, PDF
3. Chọn độ phân giải cao để rõ nét
4. Click `Export`

---

# DANH SÁCH ĐẦY ĐỦ CÁC BẢNG VÀ MỐI QUAN HỆ

## MODULE 1: AUTHENTICATION & AUTHORIZATION

### Bảng: users
**Columns:**
- id (BIGINT, PK, Auto Increment)
- email (VARCHAR(255), UNIQUE, NOT NULL)
- password (VARCHAR(255), NOT NULL)
- role (ENUM: CUSTOMER, EMPLOYEE, ADMIN, NOT NULL)
- status (ENUM: ACTIVE, INACTIVE, BLOCKED, NOT NULL)

**Relationships:**
- 1:1 với `customers` (user_id)
- 1:1 với `employees` (user_id)
- 1:1 với `carts` (user_id)
- 1:N với `orders` (user_id)
- 1:N với `payments` (user_id)

---

### Bảng: customers
**Columns:**
- id (BIGINT, PK, Auto Increment)
- user_id (BIGINT, FK → users.id, UNIQUE, NOT NULL)
- full_name (VARCHAR(255), NOT NULL)
- phone (VARCHAR(20), UNIQUE, NOT NULL)
- gender (VARCHAR(10))
- birth_date (DATE)
- address (VARCHAR(500))

**Relationships:**
- N:1 với `users` (user_id)

---

### Bảng: employees
**Columns:**
- id (BIGINT, PK, Auto Increment)
- user_id (BIGINT, FK → users.id, UNIQUE)
- position (ENUM: WAREHOUSE_MANAGER, PRODUCT_MANAGER, ADMIN)
- full_name (VARCHAR(255))
- phone (VARCHAR(20))
- address (VARCHAR(500))
- first_login (BOOLEAN, NOT NULL, DEFAULT true)

**Relationships:**
- N:1 với `users` (user_id)

---

### Bảng: employee_registration
**Columns:**
- id (BIGINT, PK, Auto Increment)
- full_name (VARCHAR(255), NOT NULL)
- email (VARCHAR(255), UNIQUE, NOT NULL)
- phone (VARCHAR(20), UNIQUE, NOT NULL)
- address (VARCHAR(500))
- position (ENUM, NOT NULL)
- note (TEXT)
- approved (BOOLEAN, DEFAULT false)
- created_at (TIMESTAMP)
- approved_at (TIMESTAMP)

**Relationships:**
- Không có (bảng độc lập)

---

### Bảng: otp_verification
**Columns:**
- id (BIGINT, PK, Auto Increment)
- email (VARCHAR(255))
- encoded_password (VARCHAR(255))
- full_name (VARCHAR(255))
- phone (VARCHAR(20))
- address (VARCHAR(500))
- otp_code (VARCHAR(10))
- created_at (TIMESTAMP)
- expires_at (TIMESTAMP)
- verified (BOOLEAN)

**Relationships:**
- Không có (bảng độc lập)

---

## MODULE 2: PRODUCT MANAGEMENT

### Bảng: categories
**Columns:**
- id (BIGINT, PK, Auto Increment)
- name (VARCHAR(255), NOT NULL)
- slug (VARCHAR(255), UNIQUE)
- description (TEXT)
- image_url (VARCHAR(500))
- display_order (INT)
- active (BOOLEAN)
- parent_id (BIGINT, FK → categories.id)

**Relationships:**
- N:1 với `categories` (parent_id) - Self-referencing
- 1:N với `categories` (parent_id) - Children
- 1:N với `products` (category_id)

---

### Bảng: products
**Columns:**
- id (BIGINT, PK, Auto Increment)
- category_id (BIGINT, FK → categories.id)
- name (VARCHAR(255), NOT NULL)
- price (DECIMAL(15,2))
- sku (VARCHAR(100), UNIQUE)
- description (TEXT)
- image_url (VARCHAR(500))
- stock_quantity (BIGINT)
- tech_specs_json (TEXT)
- product_detail_id (BIGINT, FK → product_details.id)
- warehouse_product_id (BIGINT, FK → warehouse_products.id)

**Relationships:**
- N:1 với `categories` (category_id)
- 1:1 với `product_details` (product_detail_id)
- 1:1 với `warehouse_products` (warehouse_product_id)
- 1:N với `cart_items` (product_id)
- 1:N với `order_items` (product_id)

---

## MODULE 3: WAREHOUSE MANAGEMENT

### Bảng: warehouse_products
**Columns:**
- id (BIGINT, PK, Auto Increment)
- sku (VARCHAR(64), UNIQUE, NOT NULL)
- internal_name (VARCHAR(255), NOT NULL)
- tech_specs_json (TEXT)
- description (TEXT)
- supplier_id (BIGINT, FK → suppliers.id)
- last_import_date (TIMESTAMP)

**Relationships:**
- N:1 với `suppliers` (supplier_id)
- 1:1 với `products` (warehouse_product_id)
- 1:N với `product_details` (warehouse_product_id)
- 1:N với `warehouse_product_images` (warehouse_product_id)
- 1:N với `product_specifications` (warehouse_product_id)
- 1:N với `inventory_stock` (warehouse_product_id)
- 1:N với `purchase_order_items` (warehouse_product_id)
- 1:N với `export_order_items` (warehouse_product_id)

---

### Bảng: product_details
**Columns:**
- id (BIGINT, PK, Auto Increment)
- serial_number (VARCHAR(64), UNIQUE, NOT NULL)
- import_price (DECIMAL(15,2), NOT NULL)
- sale_price (DECIMAL(15,2))
- import_date (TIMESTAMP)
- status (ENUM: IN_STOCK, SOLD, DAMAGED, RETURNED, NOT NULL)
- warehouse_product_id (BIGINT, FK → warehouse_products.id, NOT NULL)
- purchase_order_item_id (BIGINT, FK → purchase_order_items.id)
- warranty_months (INT)
- sold_order_id (BIGINT)
- sold_date (TIMESTAMP)
- note (TEXT)

**Relationships:**
- N:1 với `warehouse_products` (warehouse_product_id)
- N:1 với `purchase_order_items` (purchase_order_item_id)
- 1:1 với `products` (product_detail_id)

---

### Bảng: warehouse_product_images
**Columns:**
- id (BIGINT, PK, Auto Increment)
- url (VARCHAR(500))
- warehouse_product_id (BIGINT, FK → warehouse_products.id)

**Relationships:**
- N:1 với `warehouse_products` (warehouse_product_id)

---

### Bảng: product_specifications
**Columns:**
- id (BIGINT, PK, Auto Increment)
- warehouse_product_id (BIGINT, FK → warehouse_products.id, NOT NULL)
- spec_key (VARCHAR(100), NOT NULL)
- spec_value (VARCHAR(255), NOT NULL)

**Relationships:**
- N:1 với `warehouse_products` (warehouse_product_id)

---

### Bảng: inventory_stock
**Columns:**
- id (BIGINT, PK, Auto Increment)
- warehouse_product_id (BIGINT, FK → warehouse_products.id, UNIQUE, NOT NULL)
- on_hand (BIGINT, NOT NULL, DEFAULT 0)
- reserved (BIGINT, NOT NULL, DEFAULT 0)
- damaged (BIGINT, NOT NULL, DEFAULT 0)
- last_audit_date (DATE)

**Relationships:**
- N:1 với `warehouse_products` (warehouse_product_id)

---

## MODULE 4: SUPPLIERS & PURCHASE ORDERS

### Bảng: suppliers
**Columns:**
- id (BIGINT, PK, Auto Increment)
- name (VARCHAR(255), NOT NULL)
- contact_name (VARCHAR(255))
- phone (VARCHAR(20))
- email (VARCHAR(255))
- address (VARCHAR(500))
- tax_code (VARCHAR(50), UNIQUE, NOT NULL)
- bank_account (VARCHAR(100))
- payment_term (VARCHAR(255))
- active (BOOLEAN, NOT NULL, DEFAULT true)
- auto_created (BOOLEAN, NOT NULL, DEFAULT false)

**Relationships:**
- 1:N với `warehouse_products` (supplier_id)
- 1:N với `purchase_orders` (supplier_tax_code)

---

### Bảng: purchase_orders
**Columns:**
- id (BIGINT, PK, Auto Increment)
- po_code (VARCHAR(50), UNIQUE, NOT NULL)
- supplier_tax_code (VARCHAR(50), FK → suppliers.tax_code, NOT NULL)
- order_date (TIMESTAMP)
- received_date (TIMESTAMP)
- status (ENUM: CREATED, RECEIVED, CANCELED)
- created_by (VARCHAR(255))
- note (TEXT)

**Relationships:**
- N:1 với `suppliers` (supplier_tax_code)
- 1:N với `purchase_order_items` (purchase_order_id)

---

### Bảng: purchase_order_items
**Columns:**
- id (BIGINT, PK, Auto Increment)
- purchase_order_id (BIGINT, FK → purchase_orders.id, NOT NULL)
- sku (VARCHAR(64), UNIQUE, NOT NULL)
- warehouse_product_id (BIGINT, FK → warehouse_products.id)
- quantity (BIGINT)
- unit_cost (DECIMAL(15,2))
- warranty_months (INT)
- note (TEXT)

**Relationships:**
- N:1 với `purchase_orders` (purchase_order_id)
- N:1 với `warehouse_products` (warehouse_product_id)
- 1:N với `product_details` (purchase_order_item_id)

---

## MODULE 5: EXPORT ORDERS

### Bảng: export_orders
**Columns:**
- id (BIGINT, PK, Auto Increment)
- export_code (VARCHAR(50), UNIQUE, NOT NULL)
- export_date (TIMESTAMP)
- created_by (VARCHAR(255))
- reason (VARCHAR(255))
- note (TEXT)
- status (ENUM: PENDING, COMPLETED, CANCELED)

**Relationships:**
- 1:N với `export_order_items` (export_order_id)

---

### Bảng: export_order_items
**Columns:**
- id (BIGINT, PK, Auto Increment)
- export_order_id (BIGINT, FK → export_orders.id, NOT NULL)
- warehouse_product_id (BIGINT, FK → warehouse_products.id)
- sku (VARCHAR(64), NOT NULL)
- quantity (BIGINT, NOT NULL)
- serial_numbers (TEXT)
- total_cost (DECIMAL(15,2))

**Relationships:**
- N:1 với `export_orders` (export_order_id)
- N:1 với `warehouse_products` (warehouse_product_id)

---

## MODULE 6: SHOPPING CART

### Bảng: carts
**Columns:**
- id (BIGINT, PK, Auto Increment)
- user_id (BIGINT, FK → users.id, UNIQUE, NOT NULL)
- created_at (TIMESTAMP, NOT NULL)
- updated_at (TIMESTAMP)

**Relationships:**
- 1:1 với `users` (user_id)
- 1:N với `cart_items` (cart_id)

---

### Bảng: cart_items
**Columns:**
- id (BIGINT, PK, Auto Increment)
- cart_id (BIGINT, FK → carts.id, NOT NULL)
- product_id (BIGINT, FK → products.id, NOT NULL)
- quantity (INT, NOT NULL)
- price (DECIMAL(15,2), NOT NULL)
- added_at (TIMESTAMP, NOT NULL)

**Relationships:**
- N:1 với `carts` (cart_id)
- N:1 với `products` (product_id)

---

## MODULE 7: ORDERS

### Bảng: orders
**Columns:**
- id (BIGINT, PK, Auto Increment)
- order_code (VARCHAR(50), UNIQUE, NOT NULL)
- user_id (BIGINT, FK → users.id, NOT NULL)
- customer_name (VARCHAR(255), NOT NULL)
- customer_phone (VARCHAR(20), NOT NULL)
- customer_email (VARCHAR(255), NOT NULL)
- shipping_address (TEXT, NOT NULL)
- note (TEXT)
- subtotal (DECIMAL(15,2), NOT NULL)
- shipping_fee (DECIMAL(15,2), NOT NULL)
- discount (DECIMAL(15,2), NOT NULL)
- total (DECIMAL(15,2), NOT NULL)
- payment_status (ENUM: UNPAID, PAID, REFUNDED, NOT NULL)
- payment_id (BIGINT)
- paid_at (TIMESTAMP)
- status (ENUM: PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELED, NOT NULL)
- created_at (TIMESTAMP, NOT NULL)
- confirmed_at (TIMESTAMP)
- shipped_at (TIMESTAMP)
- delivered_at (TIMESTAMP)
- cancelled_at (TIMESTAMP)
- cancel_reason (TEXT)

**Relationships:**
- N:1 với `users` (user_id)
- 1:N với `order_items` (order_id)
- 1:1 với `payments` (payment_id)

---

### Bảng: order_items
**Columns:**
- id (BIGINT, PK, Auto Increment)
- order_id (BIGINT, FK → orders.id, NOT NULL)
- product_id (BIGINT, FK → products.id, NOT NULL)
- product_name (VARCHAR(255), NOT NULL)
- price (DECIMAL(15,2), NOT NULL)
- quantity (INT, NOT NULL)
- subtotal (DECIMAL(15,2), NOT NULL)
- serial_number (VARCHAR(64))

**Relationships:**
- N:1 với `orders` (order_id)
- N:1 với `products` (product_id)

---

## MODULE 8: PAYMENTS

### Bảng: payments
**Columns:**
- id (BIGINT, PK, Auto Increment)
- payment_code (VARCHAR(50), UNIQUE, NOT NULL)
- order_id (BIGINT, FK → orders.id, NOT NULL)
- user_id (BIGINT, FK → users.id, NOT NULL)
- amount (DECIMAL(15,2), NOT NULL)
- method (ENUM: SEPAY, COD, NOT NULL)
- status (ENUM: PENDING, SUCCESS, FAILED, EXPIRED, NOT NULL)
- sepay_transaction_id (VARCHAR(255))
- sepay_bank_code (VARCHAR(50))
- sepay_account_number (VARCHAR(50))
- sepay_account_name (VARCHAR(255))
- sepay_content (VARCHAR(500))
- sepay_qr_code (VARCHAR(500))
- sepay_response (TEXT)
- created_at (TIMESTAMP, NOT NULL)
- paid_at (TIMESTAMP)
- expired_at (TIMESTAMP)
- failure_reason (TEXT)

**Relationships:**
- 1:1 với `orders` (order_id)
- N:1 với `users` (user_id)

---

# TỔNG KẾT MỐI QUAN HỆ

## Nhóm theo Module:

### Authentication (3 bảng chính + 2 bảng phụ):
- users ↔ customers (1:1)
- users ↔ employees (1:1)
- employee_registration (độc lập)
- otp_verification (độc lập)

### Product (2 bảng):
- categories ↔ categories (self-referencing)
- categories ↔ products (1:N)

### Warehouse (6 bảng):
- warehouse_products ↔ products (1:1)
- warehouse_products ↔ product_details (1:N)
- warehouse_products ↔ warehouse_product_images (1:N)
- warehouse_products ↔ product_specifications (1:N)
- warehouse_products ↔ inventory_stock (1:N)

### Purchase (3 bảng):
- suppliers ↔ warehouse_products (1:N)
- suppliers ↔ purchase_orders (1:N)
- purchase_orders ↔ purchase_order_items (1:N)
- purchase_order_items ↔ warehouse_products (N:1)
- purchase_order_items ↔ product_details (1:N)

### Export (2 bảng):
- export_orders ↔ export_order_items (1:N)
- export_order_items ↔ warehouse_products (N:1)

### Shopping (2 bảng):
- users ↔ carts (1:1)
- carts ↔ cart_items (1:N)
- cart_items ↔ products (N:1)

### Orders & Payments (3 bảng):
- users ↔ orders (1:N)
- orders ↔ order_items (1:N)
- order_items ↔ products (N:1)
- orders ↔ payments (1:1)
- users ↔ payments (1:N)

---

# GỢI Ý BỐ CỤC TRÊN VISUAL PARADIGM

## Layout theo chiều ngang (từ trái sang phải):

```
[Authentication]  →  [Shopping]  →  [Orders]  →  [Payments]
     users              carts         orders      payments
   customers          cart_items    order_items
   employees
   
[Product]  →  [Warehouse]  →  [Purchase]  →  [Export]
categories    warehouse_      suppliers      export_
products      products        purchase_      orders
              product_        orders         export_
              details         purchase_      order_
              inventory_      order_items    items
              stock
```

## Màu sắc gợi ý:
- Authentication: Xanh dương nhạt (#E3F2FD)
- Product: Xanh lá nhạt (#E8F5E9)
- Warehouse: Vàng nhạt (#FFF9C4)
- Purchase: Cam nhạt (#FFE0B2)
- Export: Đỏ nhạt (#FFCDD2)
- Shopping: Tím nhạt (#F3E5F5)
- Orders: Xanh ngọc nhạt (#E0F2F1)
- Payments: Hồng nhạt (#FCE4EC)
