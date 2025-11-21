# Sơ Đồ Database - WEB_TMDT

## Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    %% ==================== AUTH MODULE ====================
    USERS ||--o| CUSTOMERS : "1:1"
    USERS ||--o| EMPLOYEES : "1:1"
    USERS ||--o{ OTP_VERIFICATIONS : "1:N"
    EMPLOYEES ||--o{ EMPLOYEE_REGISTRATIONS : "1:N"
    
    USERS {
        bigint id PK
        varchar email UK
        varchar password
        enum role
        enum status
        timestamp created_at
    }
    
    CUSTOMERS {
        bigint id PK
        bigint user_id FK_UK
        varchar full_name
        varchar phone UK
        varchar gender
        date birth_date
        text address
    }
    
    EMPLOYEES {
        bigint id PK
        bigint user_id FK_UK
        enum position
        varchar full_name
        varchar phone
        text address
        boolean first_login
    }
    
    EMPLOYEE_REGISTRATIONS {
        bigint id PK
        varchar email UK
        varchar full_name
        varchar phone
        enum position
        enum status
        timestamp created_at
        bigint approved_by FK
        timestamp approved_at
        bigint employee_id FK
    }
    
    OTP_VERIFICATIONS {
        bigint id PK
        bigint user_id FK
        varchar email
        varchar otp_code
        timestamp created_at
        timestamp expires_at
        boolean verified
    }
    
    %% ==================== PRODUCT MODULE ====================
    CATEGORIES ||--o{ CATEGORIES : "parent-child"
    CATEGORIES ||--o{ PRODUCTS : "1:N"
    
    CATEGORIES {
        bigint id PK
        varchar name
        varchar slug UK
        text description
        varchar image_url
        int display_order
        boolean active
        bigint parent_id FK
    }
    
    PRODUCTS {
        bigint id PK
        bigint category_id FK
        varchar name
        double price
        varchar sku UK
        text description
        varchar image_url
        bigint stock_quantity
        text tech_specs_json
        bigint warehouse_product_id FK_UK
    }
    
    %% ==================== INVENTORY MODULE ====================
    PRODUCTS }o--|| WAREHOUSE_PRODUCTS : "N:1"
    WAREHOUSE_PRODUCTS ||--o{ PRODUCT_DETAILS : "1:N"
    WAREHOUSE_PRODUCTS ||--|| INVENTORY_STOCK : "1:1"
    WAREHOUSE_PRODUCTS ||--o{ PRODUCT_SPECIFICATIONS : "1:N"
    WAREHOUSE_PRODUCTS ||--o{ WAREHOUSE_PRODUCT_IMAGES : "1:N"
    WAREHOUSE_PRODUCTS }o--|| SUPPLIERS : "N:1"
    SUPPLIERS ||--o{ PURCHASE_ORDERS : "1:N"
    PURCHASE_ORDERS ||--o{ PURCHASE_ORDER_ITEMS : "1:N"
    PURCHASE_ORDER_ITEMS }o--|| WAREHOUSE_PRODUCTS : "N:1"
    PRODUCT_DETAILS }o--o| PURCHASE_ORDERS : "N:1"
    EXPORT_ORDERS ||--o{ EXPORT_ORDER_ITEMS : "1:N"
    EXPORT_ORDER_ITEMS }o--|| PRODUCT_DETAILS : "N:1"
    
    WAREHOUSE_PRODUCTS {
        bigint id PK
        varchar sku UK
        varchar internal_name
        text tech_specs_json
        text description
        bigint supplier_id FK
        timestamp last_import_date
    }
    
    PRODUCT_DETAILS {
        bigint id PK
        bigint warehouse_product_id FK
        varchar serial_number UK
        enum status
        timestamp import_date
        double import_price
        bigint purchase_order_id FK
        bigint export_order_id FK
        text notes
    }
    
    INVENTORY_STOCK {
        bigint id PK
        bigint warehouse_product_id FK_UK
        bigint on_hand
        bigint reserved
        bigint damaged
        date last_audit_date
    }
    
    PRODUCT_SPECIFICATIONS {
        bigint id PK
        bigint warehouse_product_id FK
        varchar spec_key
        varchar spec_value
    }
    
    WAREHOUSE_PRODUCT_IMAGES {
        bigint id PK
        bigint warehouse_product_id FK
        varchar image_url
        int display_order
    }
    
    SUPPLIERS {
        bigint id PK
        boolean auto_created
        varchar name
        varchar contact_name
        varchar phone
        varchar email
        text address
        varchar tax_code UK
        varchar bank_account
        text payment_term
        boolean active
    }
    
    PURCHASE_ORDERS {
        bigint id PK
        varchar po_code UK
        varchar supplier_tax_code FK
        timestamp order_date
        timestamp received_date
        enum status
        varchar created_by
        text note
    }
    
    PURCHASE_ORDER_ITEMS {
        bigint id PK
        bigint purchase_order_id FK
        bigint warehouse_product_id FK
        int quantity
        double unit_price
    }
    
    EXPORT_ORDERS {
        bigint id PK
        varchar export_code UK
        timestamp export_date
        varchar created_by
        varchar reason
        text note
        enum status
    }
    
    EXPORT_ORDER_ITEMS {
        bigint id PK
        bigint export_order_id FK
        bigint product_detail_id FK
    }
    
    %% ==================== CART MODULE ====================
    USERS ||--|| CARTS : "1:1"
    CARTS ||--o{ CART_ITEMS : "1:N"
    CART_ITEMS }o--|| PRODUCTS : "N:1"
    
    CARTS {
        bigint id PK
        bigint user_id FK_UK
        timestamp created_at
        timestamp updated_at
    }
    
    CART_ITEMS {
        bigint id PK
        bigint cart_id FK
        bigint product_id FK
        int quantity
        double price
        timestamp added_at
    }
    
    %% ==================== ORDER MODULE ====================
    USERS ||--o{ ORDERS : "1:N"
    ORDERS ||--o{ ORDER_ITEMS : "1:N"
    ORDER_ITEMS }o--|| PRODUCTS : "N:1"
    
    ORDERS {
        bigint id PK
        varchar order_code UK
        bigint user_id FK
        varchar customer_name
        varchar customer_phone
        varchar customer_email
        text shipping_address
        text note
        double subtotal
        double shipping_fee
        double discount
        double total
        enum payment_status
        bigint payment_id FK
        timestamp paid_at
        enum status
        timestamp created_at
        timestamp confirmed_at
        timestamp shipped_at
        timestamp delivered_at
        timestamp cancelled_at
        text cancel_reason
    }
    
    ORDER_ITEMS {
        bigint id PK
        bigint order_id FK
        bigint product_id FK
        varchar product_name
        varchar product_sku
        int quantity
        double price
        double subtotal
    }
    
    %% ==================== PAYMENT MODULE ====================
    ORDERS ||--o| PAYMENTS : "1:1"
    USERS ||--o{ PAYMENTS : "1:N"
    
    PAYMENTS {
        bigint id PK
        varchar payment_code UK
        bigint order_id FK_UK
        bigint user_id FK
        double amount
        enum method
        enum status
        varchar sepay_transaction_id
        varchar sepay_bank_code
        varchar sepay_account_number
        varchar sepay_account_name
        varchar sepay_content
        varchar sepay_qr_code
        text sepay_response
        timestamp created_at
        timestamp paid_at
        timestamp expired_at
        text failure_reason
    }
```

## Tổng Quan Database

### Thống Kê
- **Tổng số bảng**: 22 bảng
- **Modules**: 6 modules chính
- **Relationships**: 30+ quan hệ

### Phân Chia Theo Module

| Module | Số Bảng | Mô Tả |
|--------|---------|-------|
| **Auth** | 5 | Quản lý người dùng, phân quyền |
| **Product** | 2 | Sản phẩm hiển thị trên web |
| **Inventory** | 10 | Quản lý kho, serial tracking |
| **Cart** | 2 | Giỏ hàng |
| **Order** | 2 | Đơn hàng |
| **Payment** | 1 | Thanh toán |

---

## Chi Tiết Các Module

### 🔐 Auth Module (5 bảng)

#### USERS
Bảng trung tâm quản lý tất cả người dùng
- **Role**: ADMIN, PRODUCT_MANAGER, WAREHOUSE_MANAGER, CUSTOMER
- **Status**: ACTIVE, INACTIVE, BANNED
- **Relationships**: 1-1 với CUSTOMERS hoặc EMPLOYEES

#### CUSTOMERS
Thông tin khách hàng
- Liên kết 1-1 với USERS
- Lưu thông tin cá nhân: họ tên, SĐT, địa chỉ

#### EMPLOYEES
Thông tin nhân viên
- Liên kết 1-1 với USERS
- **Position**: ADMIN, PRODUCT_MANAGER, WAREHOUSE_MANAGER
- **first_login**: Bắt buộc đổi mật khẩu lần đầu

#### EMPLOYEE_REGISTRATIONS
Đăng ký nhân viên chờ duyệt
- **Status**: PENDING, APPROVED, REJECTED
- Admin phê duyệt → Tạo EMPLOYEE

#### OTP_VERIFICATIONS
Xác thực OTP qua email
- Expires sau 5 phút
- Dùng cho đăng ký, quên mật khẩu

---

### 🛍️ Product Module (2 bảng)

#### CATEGORIES
Danh mục sản phẩm (hỗ trợ phân cấp)
- **parent_id**: Tạo cây danh mục
- **slug**: URL-friendly (dien-thoai, laptop-gaming)
- **display_order**: Thứ tự hiển thị
- **active**: Ẩn/hiện danh mục

#### PRODUCTS
Sản phẩm hiển thị trên website
- Link với WAREHOUSE_PRODUCTS (sản phẩm được publish từ kho)
- **stock_quantity**: Sync từ INVENTORY_STOCK
- **tech_specs_json**: Thông số kỹ thuật dạng JSON

---

### 📦 Inventory Module (10 bảng)

#### WAREHOUSE_PRODUCTS
Sản phẩm trong kho (chưa publish)
- **sku**: Mã SKU duy nhất
- **internal_name**: Tên kỹ thuật nội bộ
- Có thể có nhiều ảnh, nhiều specs

#### PRODUCT_DETAILS
Theo dõi từng serial number
- **serial_number**: Unique cho mỗi sản phẩm
- **status**: IN_STOCK, RESERVED, SOLD, DAMAGED, RETURNED
- **import_price**: Giá nhập
- Link với PURCHASE_ORDER và EXPORT_ORDER

#### INVENTORY_STOCK
Quản lý tồn kho
- **on_hand**: Tồn thực tế
- **reserved**: Đã giữ chỗ cho đơn hàng
- **damaged**: Sản phẩm lỗi
- **Công thức**: `sellable = on_hand - reserved - damaged`

#### PRODUCT_SPECIFICATIONS
Thông số kỹ thuật (dạng bảng)
- **spec_key**: Tên thông số (CPU, RAM, Storage)
- **spec_value**: Giá trị (Intel i7, 16GB, 512GB SSD)
- Dễ search hơn JSON

#### WAREHOUSE_PRODUCT_IMAGES
Ảnh sản phẩm kho
- Nhiều ảnh cho 1 sản phẩm
- **display_order**: Thứ tự hiển thị

#### SUPPLIERS
Nhà cung cấp
- **tax_code**: Mã số thuế (unique)
- **auto_created**: Tự động tạo khi nhập PO
- **active**: Ngừng hợp tác

#### PURCHASE_ORDERS
Đơn đặt hàng nhập kho
- **po_code**: Mã PO duy nhất (PO20231119001)
- **status**: CREATED, RECEIVED, CANCELED
- **received_date**: Ngày nhập thực tế

#### PURCHASE_ORDER_ITEMS
Chi tiết đơn nhập
- Sản phẩm nào, số lượng, giá nhập

#### EXPORT_ORDERS
Phiếu xuất kho
- **export_code**: Mã phiếu xuất (PX20231119001)
- **reason**: Bán hàng, hủy hàng, đổi trả, bảo hành
- **status**: PENDING, COMPLETED, CANCELED

#### EXPORT_ORDER_ITEMS
Chi tiết phiếu xuất
- Link với PRODUCT_DETAILS (xuất theo serial)

---

### 🛒 Cart Module (2 bảng)

#### CARTS
Giỏ hàng
- 1 USER có 1 CART
- **updated_at**: Tự động cập nhật khi thay đổi

#### CART_ITEMS
Sản phẩm trong giỏ
- **quantity**: Số lượng
- **price**: Giá tại thời điểm thêm vào giỏ

---

### 📋 Order Module (2 bảng)

#### ORDERS
Đơn hàng
- **order_code**: Mã đơn hàng (ORD20231119001)
- **status**: PENDING → CONFIRMED → PROCESSING → SHIPPING → DELIVERED
- **payment_status**: UNPAID, PENDING, PAID, FAILED, EXPIRED, REFUNDED
- Lưu thông tin giao hàng, giá tiền, timestamps

#### ORDER_ITEMS
Chi tiết đơn hàng
- Snapshot sản phẩm tại thời điểm đặt hàng
- **product_name**, **product_sku**: Lưu lại để tránh mất data khi sản phẩm bị xóa

---

### 💳 Payment Module (1 bảng)

#### PAYMENTS
Thanh toán (tích hợp SePay)
- **payment_code**: Mã thanh toán (PAY20231119001)
- **method**: SEPAY, COD, BANK_TRANSFER
- **status**: PENDING, PAID, FAILED, EXPIRED, REFUNDED
- **sepay_qr_code**: URL QR Code
- **expired_at**: Hết hạn sau 15 phút
- **sepay_response**: Lưu full response từ SePay (JSON)

---

## Quan Hệ Chính

### 1:1 Relationships
- USERS ↔ CUSTOMERS
- USERS ↔ EMPLOYEES
- USERS ↔ CARTS
- WAREHOUSE_PRODUCTS ↔ INVENTORY_STOCK
- ORDERS ↔ PAYMENTS

### 1:N Relationships
- USERS → ORDERS
- USERS → PAYMENTS
- CATEGORIES → PRODUCTS
- WAREHOUSE_PRODUCTS → PRODUCT_DETAILS
- SUPPLIERS → PURCHASE_ORDERS
- PURCHASE_ORDERS → PURCHASE_ORDER_ITEMS
- ORDERS → ORDER_ITEMS
- CARTS → CART_ITEMS

### N:1 Relationships
- PRODUCTS → WAREHOUSE_PRODUCTS
- WAREHOUSE_PRODUCTS → SUPPLIERS
- PURCHASE_ORDER_ITEMS → WAREHOUSE_PRODUCTS

---

## Indexes Quan Trọng

```sql
-- Users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- Products
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_warehouse ON products(warehouse_product_id);

-- Warehouse Products
CREATE INDEX idx_warehouse_products_sku ON warehouse_products(sku);
CREATE INDEX idx_warehouse_products_supplier ON warehouse_products(supplier_id);

-- Product Details
CREATE INDEX idx_product_details_serial ON product_details(serial_number);
CREATE INDEX idx_product_details_status ON product_details(status);
CREATE INDEX idx_product_details_warehouse ON product_details(warehouse_product_id);

-- Orders
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_code ON orders(order_code);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- Payments
CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
```

---

## Business Rules

### Inventory Rules
```
on_hand >= reserved + damaged
sellable = on_hand - reserved - damaged >= 0
Không thể xuất kho nếu sellable < quantity
```

### Order Rules
```
Order CONFIRMED ← payment_status = PAID
Không thể CANCEL khi status = DELIVERED
Order CANCELLED phải có cancel_reason
```

### Product Rules
```
Product.stock_quantity = INVENTORY_STOCK.sellable
Product chỉ publish nếu sellable > 0
SKU unique trong cả PRODUCTS và WAREHOUSE_PRODUCTS
```

### Payment Rules
```
Payment expires sau 15 phút
1 ORDER = 1 PAYMENT active
Payment.amount = Order.total
```

---

## Luồng Dữ Liệu

### Luồng Nhập Hàng
```
1. Tạo PURCHASE_ORDER (CREATED)
2. Thêm PURCHASE_ORDER_ITEMS
3. Complete PO:
   - Tạo PRODUCT_DETAILS (serial)
   - Cập nhật INVENTORY_STOCK (on_hand += quantity)
   - Cập nhật PURCHASE_ORDER (RECEIVED)
```

### Luồng Publish Sản Phẩm
```
1. Chọn WAREHOUSE_PRODUCT
2. Tạo PRODUCT:
   - Link warehouse_product_id
   - Copy thông tin
   - Set stock_quantity từ INVENTORY_STOCK.sellable
3. Sản phẩm hiển thị trên web
```

### Luồng Đặt Hàng
```
1. Tạo ORDER (PENDING, UNPAID)
2. Tạo ORDER_ITEMS từ CART_ITEMS
3. Tạo PAYMENT (PENDING)
4. SePay Webhook:
   - PAYMENT (PAID)
   - ORDER (CONFIRMED, PAID)
   - INVENTORY_STOCK (reserved += quantity)
5. Xuất kho:
   - Tạo EXPORT_ORDER
   - PRODUCT_DETAILS (SOLD)
   - INVENTORY_STOCK (on_hand -= quantity, reserved -= quantity)
```
