# Sơ Đồ Database Đơn Giản - WEB_TMDT

## Tổng Quan
- **Tổng số bảng**: 22 bảng
- **Modules**: 6 modules (Auth, Product, Inventory, Cart, Order, Payment)

---

## 📊 Sơ Đồ Quan Hệ Theo Module

### 🔐 Module 1: AUTH (Xác thực & Phân quyền)

```
USERS (Người dùng)
├── id (PK)
├── email (UK)
├── password
├── role (ADMIN, PRODUCT_MANAGER, WAREHOUSE_MANAGER, CUSTOMER)
└── status (ACTIVE, INACTIVE, BANNED)
    │
    ├─── 1:1 ──→ CUSTOMERS (Khách hàng)
    │             ├── id (PK)
    │             ├── user_id (FK, UK)
    │             ├── full_name
    │             ├── phone (UK)
    │             └── address
    │
    ├─── 1:1 ──→ EMPLOYEES (Nhân viên)
    │             ├── id (PK)
    │             ├── user_id (FK, UK)
    │             ├── position (ADMIN, PRODUCT_MANAGER, WAREHOUSE_MANAGER)
    │             ├── full_name
    │             └── first_login
    │
    └─── 1:N ──→ OTP_VERIFICATIONS (Xác thực OTP)
                  ├── id (PK)
                  ├── user_id (FK)
                  ├── otp_code
                  └── expires_at

EMPLOYEE_REGISTRATIONS (Đăng ký nhân viên chờ duyệt)
├── id (PK)
├── email (UK)
├── position
├── status (PENDING, APPROVED, REJECTED)
└── employee_id (FK) → EMPLOYEES
```

**Quan hệ chính**:
- 1 USER = 1 CUSTOMER hoặc 1 EMPLOYEE
- 1 USER có nhiều OTP_VERIFICATIONS
- 1 EMPLOYEE có nhiều EMPLOYEE_REGISTRATIONS

---

### 🛍️ Module 2: PRODUCT (Sản phẩm)

```
CATEGORIES (Danh mục - Hỗ trợ phân cấp)
├── id (PK)
├── name
├── slug (UK)
├── parent_id (FK) → CATEGORIES (Tự tham chiếu)
├── display_order
└── active
    │
    └─── 1:N ──→ PRODUCTS (Sản phẩm hiển thị web)
                  ├── id (PK)
                  ├── category_id (FK)
                  ├── name
                  ├── price
                  ├── sku (UK)
                  ├── stock_quantity
                  └── warehouse_product_id (FK, UK) → WAREHOUSE_PRODUCTS
```

**Quan hệ chính**:
- CATEGORIES có cấu trúc cây (parent-child)
- 1 CATEGORY có nhiều PRODUCTS
- 1 PRODUCT link với 1 WAREHOUSE_PRODUCT

---

### 📦 Module 3: INVENTORY (Quản lý kho)

```
SUPPLIERS (Nhà cung cấp)
├── id (PK)
├── name
├── tax_code (UK)
├── phone
└── active
    │
    ├─── 1:N ──→ WAREHOUSE_PRODUCTS (Sản phẩm trong kho)
    │             ├── id (PK)
    │             ├── sku (UK)
    │             ├── internal_name
    │             ├── supplier_id (FK)
    │             └── tech_specs_json
    │                 │
    │                 ├─── 1:1 ──→ INVENTORY_STOCK (Tồn kho)
    │                 │             ├── id (PK)
    │                 │             ├── warehouse_product_id (FK, UK)
    │                 │             ├── on_hand (Tồn thực tế)
    │                 │             ├── reserved (Đã giữ chỗ)
    │                 │             └── damaged (Lỗi)
    │                 │
    │                 ├─── 1:N ──→ PRODUCT_DETAILS (Serial tracking)
    │                 │             ├── id (PK)
    │                 │             ├── warehouse_product_id (FK)
    │                 │             ├── serial_number (UK)
    │                 │             ├── status (IN_STOCK, RESERVED, SOLD, DAMAGED)
    │                 │             └── import_price
    │                 │
    │                 ├─── 1:N ──→ PRODUCT_SPECIFICATIONS (Thông số kỹ thuật)
    │                 │             ├── id (PK)
    │                 │             ├── warehouse_product_id (FK)
    │                 │             ├── spec_key (CPU, RAM, Storage)
    │                 │             └── spec_value
    │                 │
    │                 └─── 1:N ──→ WAREHOUSE_PRODUCT_IMAGES (Ảnh)
    │                               ├── id (PK)
    │                               ├── warehouse_product_id (FK)
    │                               ├── image_url
    │                               └── display_order
    │
    └─── 1:N ──→ PURCHASE_ORDERS (Đơn nhập kho)
                  ├── id (PK)
                  ├── po_code (UK)
                  ├── supplier_tax_code (FK)
                  ├── status (CREATED, RECEIVED, CANCELED)
                  └── received_date
                      │
                      └─── 1:N ──→ PURCHASE_ORDER_ITEMS
                                    ├── id (PK)
                                    ├── purchase_order_id (FK)
                                    ├── warehouse_product_id (FK)
                                    ├── quantity
                                    └── unit_price

EXPORT_ORDERS (Phiếu xuất kho)
├── id (PK)
├── export_code (UK)
├── reason (Bán hàng, Hủy, Đổi trả, Bảo hành)
└── status (PENDING, COMPLETED, CANCELED)
    │
    └─── 1:N ──→ EXPORT_ORDER_ITEMS
                  ├── id (PK)
                  ├── export_order_id (FK)
                  └── product_detail_id (FK) → PRODUCT_DETAILS
```

**Quan hệ chính**:
- 1 SUPPLIER có nhiều WAREHOUSE_PRODUCTS và PURCHASE_ORDERS
- 1 WAREHOUSE_PRODUCT có 1 INVENTORY_STOCK (tồn kho)
- 1 WAREHOUSE_PRODUCT có nhiều PRODUCT_DETAILS (serial)
- 1 PURCHASE_ORDER có nhiều PURCHASE_ORDER_ITEMS
- 1 EXPORT_ORDER có nhiều EXPORT_ORDER_ITEMS

---

### 🛒 Module 4: CART (Giỏ hàng)

```
USERS
  │
  └─── 1:1 ──→ CARTS (Giỏ hàng)
                ├── id (PK)
                ├── user_id (FK, UK)
                ├── created_at
                └── updated_at
                    │
                    └─── 1:N ──→ CART_ITEMS
                                  ├── id (PK)
                                  ├── cart_id (FK)
                                  ├── product_id (FK) → PRODUCTS
                                  ├── quantity
                                  └── price
```

**Quan hệ chính**:
- 1 USER có 1 CART
- 1 CART có nhiều CART_ITEMS
- 1 CART_ITEM tham chiếu 1 PRODUCT

---

### 📋 Module 5: ORDER (Đơn hàng)

```
USERS
  │
  └─── 1:N ──→ ORDERS (Đơn hàng)
                ├── id (PK)
                ├── order_code (UK)
                ├── user_id (FK)
                ├── customer_name
                ├── shipping_address
                ├── subtotal
                ├── shipping_fee
                ├── total
                ├── status (PENDING → CONFIRMED → PROCESSING → SHIPPING → DELIVERED)
                └── payment_status (UNPAID, PENDING, PAID, FAILED)
                    │
                    └─── 1:N ──→ ORDER_ITEMS
                                  ├── id (PK)
                                  ├── order_id (FK)
                                  ├── product_id (FK) → PRODUCTS
                                  ├── product_name (Snapshot)
                                  ├── quantity
                                  └── price
```

**Quan hệ chính**:
- 1 USER có nhiều ORDERS
- 1 ORDER có nhiều ORDER_ITEMS
- 1 ORDER_ITEM tham chiếu 1 PRODUCT

---

### 💳 Module 6: PAYMENT (Thanh toán)

```
ORDERS
  │
  └─── 1:1 ──→ PAYMENTS (Thanh toán SePay)
                ├── id (PK)
                ├── payment_code (UK)
                ├── order_id (FK, UK)
                ├── user_id (FK)
                ├── amount
                ├── method (SEPAY, COD, BANK_TRANSFER)
                ├── status (PENDING, PAID, FAILED, EXPIRED)
                ├── sepay_qr_code
                ├── sepay_transaction_id
                ├── created_at
                └── expired_at (15 phút)
```

**Quan hệ chính**:
- 1 ORDER có 1 PAYMENT
- 1 USER có nhiều PAYMENTS

---

## 🔗 Quan Hệ Giữa Các Module

```
┌─────────────┐
│    USERS    │
└──────┬──────┘
       │
       ├──→ CUSTOMERS (1:1)
       ├──→ EMPLOYEES (1:1)
       ├──→ CARTS (1:1)
       ├──→ ORDERS (1:N)
       └──→ PAYMENTS (1:N)

┌──────────────────┐
│ WAREHOUSE_PRODUCTS│
└────────┬──────────┘
         │
         ├──→ PRODUCTS (1:N) ← CATEGORIES (1:N)
         ├──→ INVENTORY_STOCK (1:1)
         ├──→ PRODUCT_DETAILS (1:N)
         └──→ SUPPLIERS (N:1)

┌─────────────┐
│   ORDERS    │
└──────┬──────┘
       │
       ├──→ ORDER_ITEMS (1:N) → PRODUCTS
       └──→ PAYMENTS (1:1)

┌──────────────────┐
│ PURCHASE_ORDERS  │
└────────┬─────────┘
         │
         ├──→ PURCHASE_ORDER_ITEMS (1:N) → WAREHOUSE_PRODUCTS
         └──→ SUPPLIERS (N:1)
```

---

## 📈 Luồng Dữ Liệu Chính

### 1️⃣ Luồng Nhập Hàng
```
SUPPLIER → PURCHASE_ORDER → PURCHASE_ORDER_ITEMS
                ↓
         WAREHOUSE_PRODUCTS
                ↓
    ┌───────────┴───────────┐
    ↓                       ↓
PRODUCT_DETAILS      INVENTORY_STOCK
(Serial tracking)    (on_hand += quantity)
```

### 2️⃣ Luồng Publish Sản Phẩm
```
WAREHOUSE_PRODUCTS → PRODUCTS
         ↓                ↓
  INVENTORY_STOCK → stock_quantity
         ↓
    CATEGORIES
```

### 3️⃣ Luồng Đặt Hàng
```
CART → CART_ITEMS
         ↓
      ORDER → ORDER_ITEMS
         ↓
     PAYMENT (SePay QR)
         ↓
   [Customer Pay]
         ↓
     Webhook
         ↓
  ORDER (CONFIRMED)
         ↓
  INVENTORY_STOCK (reserved += quantity)
         ↓
   EXPORT_ORDER
         ↓
  PRODUCT_DETAILS (status = SOLD)
         ↓
  INVENTORY_STOCK (on_hand -= quantity)
```

---

## 🎯 Key Points

### Primary Keys (PK)
Tất cả bảng đều có `id BIGINT AUTO_INCREMENT` làm PK

### Foreign Keys (FK)
- `user_id` → USERS
- `category_id` → CATEGORIES
- `product_id` → PRODUCTS
- `warehouse_product_id` → WAREHOUSE_PRODUCTS
- `order_id` → ORDERS
- `supplier_id` / `supplier_tax_code` → SUPPLIERS

### Unique Keys (UK)
- `email` (USERS)
- `phone` (CUSTOMERS)
- `sku` (PRODUCTS, WAREHOUSE_PRODUCTS)
- `serial_number` (PRODUCT_DETAILS)
- `order_code` (ORDERS)
- `payment_code` (PAYMENTS)
- `po_code` (PURCHASE_ORDERS)
- `export_code` (EXPORT_ORDERS)

### Enums
- **Role**: ADMIN, PRODUCT_MANAGER, WAREHOUSE_MANAGER, CUSTOMER
- **Status**: ACTIVE, INACTIVE, BANNED
- **Position**: ADMIN, PRODUCT_MANAGER, WAREHOUSE_MANAGER
- **OrderStatus**: PENDING, CONFIRMED, PROCESSING, SHIPPING, DELIVERED, CANCELLED
- **PaymentStatus**: UNPAID, PENDING, PAID, FAILED, EXPIRED, REFUNDED
- **ProductStatus**: IN_STOCK, RESERVED, SOLD, DAMAGED, RETURNED
- **POStatus**: CREATED, RECEIVED, CANCELED
- **ExportStatus**: PENDING, COMPLETED, CANCELED

---

## 💡 Business Rules

### Inventory
```
sellable = on_hand - reserved - damaged
available = on_hand - reserved
```

### Order
- Order CONFIRMED ← payment_status = PAID
- Không CANCEL khi status = DELIVERED

### Payment
- Expires sau 15 phút
- 1 ORDER = 1 PAYMENT

### Product
- Product.stock_quantity sync với INVENTORY_STOCK.sellable
- SKU unique trong cả PRODUCTS và WAREHOUSE_PRODUCTS
