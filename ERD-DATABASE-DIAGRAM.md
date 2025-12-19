# 🗄️ ERD - ENTITY RELATIONSHIP DIAGRAM (DỰA TRÊN CODE THỰC TẾ)

## 📊 TỔNG QUAN HỆ THỐNG

Hệ thống quản lý thương mại điện tử với **30 bảng** được chia thành 7 module chính:

```
┌─────────────────────────────────────────────────────────────────┐
│                    HỆ THỐNG QUẢN LÝ TMĐT                        │
│                                                                 │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    │
│  │   AUTH (5)   │    │ PRODUCT (3)  │    │  ORDER (2)   │    │
│  └──────────────┘    └──────────────┘    └──────────────┘    │
│                                                                 │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    │
│  │ INVENTORY(9) │    │ACCOUNTING(6) │    │ PAYMENT (2)  │    │
│  └──────────────┘    └──────────────┘    └──────────────┘    │
│                                                                 │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    │
│  │  CART (2)    │    │ SHIPPING (0) │    │ WEBHOOK (0)  │    │
│  └──────────────┘    └──────────────┘    └──────────────┘    │
└─────────────────────────────────────────────────────────────────┘

Tổng: 30 tables (29 entity + 1 join table)
```

---

## 👤 MODULE 1: AUTH (Xác thực & Phân quyền) - 5 Tables

### **1.1. users**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'ADMIN', 'EMPLOYEE') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') NOT NULL DEFAULT 'ACTIVE',
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB;
```

**Relationships:**
- 1-1 → customers
- 1-1 → employees
- 1-N → payments

### **1.2. customers**
```sql
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    gender VARCHAR(10),
    birth_date DATE,
    address TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

**Relationships:**
- N-1 → users
- 1-1 → carts
- 1-N → orders


### **1.3. employees**
```sql
CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    position ENUM('SALE', 'CSKH', 'PRODUCT_MANAGER', 'WAREHOUSE', 'ACCOUNTANT', 'SHIPPER'),
    full_name VARCHAR(255),
    phone VARCHAR(20),
    address TEXT,
    first_login BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

**Relationships:**
- N-1 → users

### **1.4. employee_registration**
```sql
CREATE TABLE employee_registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    address TEXT,
    position ENUM('SALE', 'CSKH', 'PRODUCT_MANAGER', 'WAREHOUSE', 'ACCOUNTANT', 'SHIPPER') NOT NULL,
    note TEXT,
    approved BOOLEAN DEFAULT FALSE,
    created_at DATETIME,
    approved_at DATETIME
) ENGINE=InnoDB;
```

### **1.5. otp_verification**
```sql
CREATE TABLE otp_verification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255),
    encoded_password VARCHAR(255),
    full_name VARCHAR(255),
    phone VARCHAR(20),
    address TEXT,
    otp_code VARCHAR(6),
    created_at DATETIME,
    expires_at DATETIME,
    verified BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB;
```

---

## 🛍️ MODULE 2: PRODUCT (Sản phẩm) - 3 Tables

### **2.1. categories**
```sql
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    description TEXT,
    image_url VARCHAR(500),
    display_order INT,
    active BOOLEAN,
    parent_id BIGINT,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_parent (parent_id),
    INDEX idx_active (active)
) ENGINE=InnoDB;
```

**Relationships:**
- Self-join (parent-child hierarchy)
- 1-N → products

### **2.2. products**
```sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT,
    name VARCHAR(255) NOT NULL,
    price DOUBLE,
    sku VARCHAR(100) UNIQUE,
    description TEXT,
    stock_quantity BIGINT,
    reserved_quantity BIGINT,
    tech_specs_json TEXT,
    product_detail_id BIGINT,
    warehouse_product_id BIGINT,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (product_detail_id) REFERENCES product_details(id),
    FOREIGN KEY (warehouse_product_id) REFERENCES warehouse_products(id),
    INDEX idx_category (category_id),
    INDEX idx_sku (sku)
) ENGINE=InnoDB;
```

**Relationships:**
- N-1 → categories
- 1-N → product_images
- 1-N → cart_items
- 1-N → order_items
- 1-1 → product_details
- 1-1 → warehouse_products


### **2.3. product_images**
```sql
CREATE TABLE product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    display_order INT DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    alt_text VARCHAR(255),
    created_at DATETIME,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product (product_id)
) ENGINE=InnoDB;
```

---

## 🛒 MODULE 3: CART (Giỏ hàng) - 2 Tables

### **3.1. carts**
```sql
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

### **3.2. cart_items**
```sql
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    added_at DATETIME NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

---

## 📦 MODULE 4: ORDER (Đơn hàng) - 2 Tables

### **4.1. orders**
```sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    shipping_address TEXT NOT NULL,
    note TEXT,
    subtotal DOUBLE NOT NULL,
    shipping_fee DOUBLE NOT NULL,
    discount DOUBLE NOT NULL,
    total DOUBLE NOT NULL,
    payment_status ENUM('UNPAID', 'PAID', 'REFUNDED', 'FAILED') NOT NULL,
    payment_method VARCHAR(20),
    payment_id BIGINT,
    status ENUM('PENDING_PAYMENT', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'CANCELLED') NOT NULL,
    created_at DATETIME NOT NULL,
    confirmed_at DATETIME,
    shipped_at DATETIME,
    delivered_at DATETIME,
    cancelled_at DATETIME,
    cancel_reason TEXT,
    ghn_order_code VARCHAR(100),
    ghn_shipping_status VARCHAR(50),
    ghn_created_at DATETIME,
    ghn_expected_delivery_time DATETIME,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    INDEX idx_customer (customer_id),
    INDEX idx_code (order_code),
    INDEX idx_status (status)
) ENGINE=InnoDB;
```

### **4.2. order_items**
```sql
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    subtotal DOUBLE NOT NULL,
    serial_number VARCHAR(100),
    reserved BOOLEAN DEFAULT FALSE,
    exported BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;
```

---

## 💳 MODULE 5: PAYMENT (Thanh toán) - 2 Tables

### **5.1. payments**
```sql
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_code VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    method ENUM('SEPAY', 'COD', 'VNPAY') NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'EXPIRED') NOT NULL,
    sepay_transaction_id VARCHAR(100),
    sepay_bank_code VARCHAR(50),
    sepay_account_number VARCHAR(50),
    sepay_account_name VARCHAR(255),
    sepay_content TEXT,
    sepay_qr_code VARCHAR(500),
    sepay_response TEXT,
    created_at DATETIME NOT NULL,
    paid_at DATETIME,
    expired_at DATETIME,
    failure_reason TEXT,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;
```

### **5.2. bank_accounts**
```sql
CREATE TABLE bank_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_code VARCHAR(50) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    sepay_api_token VARCHAR(255),
    sepay_merchant_id VARCHAR(100),
    is_active BOOLEAN DEFAULT FALSE,
    is_default BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
) ENGINE=InnoDB;
```

---


## 📦 MODULE 6: INVENTORY (Quản lý kho) - 9 Tables

### **6.1. warehouse_products**
```sql
CREATE TABLE warehouse_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(64) NOT NULL UNIQUE,
    internal_name VARCHAR(255) NOT NULL,
    tech_specs_json TEXT,
    description TEXT,
    supplier_id BIGINT,
    last_import_date DATETIME,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
) ENGINE=InnoDB;
```

### **6.2. inventory_stock**
```sql
CREATE TABLE inventory_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_product_id BIGINT NOT NULL UNIQUE,
    on_hand BIGINT NOT NULL DEFAULT 0,
    reserved BIGINT NOT NULL DEFAULT 0,
    damaged BIGINT NOT NULL DEFAULT 0,
    last_audit_date DATE,
    FOREIGN KEY (warehouse_product_id) REFERENCES warehouse_products(id)
) ENGINE=InnoDB;
```

### **6.3. suppliers**
```sql
CREATE TABLE suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    auto_created BOOLEAN DEFAULT FALSE,
    name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    tax_code VARCHAR(50) NOT NULL UNIQUE,
    bank_account VARCHAR(100),
    payment_term TEXT,
    payment_term_days INT,
    active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;
```

### **6.4. purchase_orders**
```sql
CREATE TABLE purchase_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    po_code VARCHAR(50) NOT NULL UNIQUE,
    supplier_tax_code VARCHAR(50) NOT NULL,
    order_date DATETIME,
    received_date DATETIME,
    status ENUM('CREATED', 'RECEIVED', 'CANCELED'),
    created_by VARCHAR(255),
    note TEXT,
    FOREIGN KEY (supplier_tax_code) REFERENCES suppliers(tax_code)
) ENGINE=InnoDB;
```

### **6.5. purchase_order_items**
```sql
CREATE TABLE purchase_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    sku VARCHAR(64) NOT NULL,
    warehouse_product_id BIGINT,
    quantity BIGINT,
    unit_cost DOUBLE,
    warranty_months INT,
    note TEXT,
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
    FOREIGN KEY (warehouse_product_id) REFERENCES warehouse_products(id)
) ENGINE=InnoDB;
```

### **6.6. product_details** (Serial/IMEI tracking)
```sql
CREATE TABLE product_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(64) NOT NULL UNIQUE,
    import_price DOUBLE NOT NULL,
    sale_price DOUBLE,
    import_date DATETIME,
    status ENUM('IN_STOCK', 'SOLD', 'DEFECTIVE', 'RETURNED') NOT NULL DEFAULT 'IN_STOCK',
    warehouse_product_id BIGINT NOT NULL,
    purchase_order_item_id BIGINT,
    warranty_months INT,
    sold_order_id BIGINT,
    sold_date DATETIME,
    note TEXT,
    FOREIGN KEY (warehouse_product_id) REFERENCES warehouse_products(id),
    FOREIGN KEY (purchase_order_item_id) REFERENCES purchase_order_items(id)
) ENGINE=InnoDB;
```

### **6.7. export_orders**
```sql
CREATE TABLE export_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    export_code VARCHAR(50) NOT NULL UNIQUE,
    export_date DATETIME,
    created_by VARCHAR(255),
    reason TEXT,
    note TEXT,
    status ENUM('PENDING', 'COMPLETED', 'CANCELLED'),
    order_id BIGINT
) ENGINE=InnoDB;
```

### **6.8. export_order_items**
```sql
CREATE TABLE export_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    export_order_id BIGINT NOT NULL,
    warehouse_product_id BIGINT,
    sku VARCHAR(64) NOT NULL,
    quantity BIGINT NOT NULL,
    serial_numbers TEXT,
    total_cost DOUBLE,
    FOREIGN KEY (export_order_id) REFERENCES export_orders(id),
    FOREIGN KEY (warehouse_product_id) REFERENCES warehouse_products(id)
) ENGINE=InnoDB;
```

### **6.9. product_specifications**
```sql
CREATE TABLE product_specifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_product_id BIGINT NOT NULL,
    spec_key VARCHAR(100) NOT NULL,
    spec_value VARCHAR(255) NOT NULL,
    FOREIGN KEY (warehouse_product_id) REFERENCES warehouse_products(id),
    INDEX idx_spec_key (spec_key),
    INDEX idx_spec_value (spec_value)
) ENGINE=InnoDB;
```

### **6.10. warehouse_product_images**
```sql
CREATE TABLE warehouse_product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(500),
    warehouse_product_id BIGINT,
    FOREIGN KEY (warehouse_product_id) REFERENCES warehouse_products(id)
) ENGINE=InnoDB;
```

---


## 💰 MODULE 7: ACCOUNTING (Kế toán) - 6 Tables

### **7.1. financial_transaction**
```sql
CREATE TABLE financial_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_code VARCHAR(50) NOT NULL,
    order_id VARCHAR(50) NOT NULL,
    type ENUM('REVENUE', 'EXPENSE', 'REFUND') NOT NULL,
    category ENUM('SALES', 'SHIPPING', 'PAYMENT_FEE', 'TAX', 'COST_OF_GOODS') NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    description TEXT,
    transaction_date DATETIME NOT NULL,
    created_by VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME
) ENGINE=InnoDB;
```

### **7.2. supplier_payables**
```sql
CREATE TABLE supplier_payables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payable_code VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    purchase_order_id BIGINT NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    paid_amount DECIMAL(15,2) NOT NULL,
    remaining_amount DECIMAL(15,2) NOT NULL,
    status ENUM('UNPAID', 'PARTIAL', 'PAID', 'OVERDUE') NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    payment_term_days INT,
    note TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id)
) ENGINE=InnoDB;
```

### **7.3. supplier_payments**
```sql
CREATE TABLE supplier_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_code VARCHAR(50) NOT NULL UNIQUE,
    payable_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method ENUM('CASH', 'BANK_TRANSFER', 'CHECK') NOT NULL,
    reference_number VARCHAR(100),
    note TEXT,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(255),
    FOREIGN KEY (payable_id) REFERENCES supplier_payables(id)
) ENGINE=InnoDB;
```

### **7.4. payment_reconciliation**
```sql
CREATE TABLE payment_reconciliation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(100) NOT NULL,
    gateway VARCHAR(50) NOT NULL,
    system_amount DECIMAL(15,2) NOT NULL,
    gateway_amount DECIMAL(15,2) NOT NULL,
    discrepancy DECIMAL(15,2) NOT NULL,
    status ENUM('MATCHED', 'MISMATCHED', 'MISSING_IN_SYSTEM', 'MISSING_IN_GATEWAY') NOT NULL,
    transaction_date DATETIME NOT NULL,
    reconciled_at DATETIME,
    reconciled_by VARCHAR(255),
    note TEXT,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB;
```

### **7.5. accounting_period**
```sql
CREATE TABLE accounting_period (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('OPEN', 'CLOSED') NOT NULL,
    total_revenue DECIMAL(15,2),
    total_orders INT,
    discrepancy_amount DECIMAL(15,2),
    discrepancy_rate DOUBLE,
    closed_by VARCHAR(255),
    closed_at DATETIME,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB;
```

### **7.6. tax_report**
```sql
CREATE TABLE tax_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_code VARCHAR(50) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    tax_type ENUM('VAT', 'CORPORATE_TAX') NOT NULL,
    taxable_revenue DECIMAL(15,2) NOT NULL,
    tax_rate DECIMAL(5,2) NOT NULL,
    tax_amount DECIMAL(15,2) NOT NULL,
    paid_tax DECIMAL(15,2),
    remaining_tax DECIMAL(15,2),
    status ENUM('DRAFT', 'SUBMITTED', 'PAID') NOT NULL,
    submitted_by VARCHAR(255),
    submitted_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
) ENGINE=InnoDB;
```

---

## 🔗 QUAN HỆ GIỮA CÁC BẢNG (RELATIONSHIPS)

```
users (1) ──────── (1) customers
users (1) ──────── (1) employees
users (1) ──────── (N) payments

customers (1) ───── (1) carts
customers (1) ───── (N) orders

carts (1) ────────── (N) cart_items
cart_items (N) ───── (1) products

orders (1) ─────────── (N) order_items
orders (1) ─────────── (1) payments
order_items (N) ────── (1) products

categories (1) ─────── (N) products
categories (1) ─────── (N) categories (Self-join)

products (1) ───────── (N) product_images
products (1) ───────── (1) product_details
products (1) ───────── (1) warehouse_products

warehouse_products (1) ─ (1) inventory_stock
warehouse_products (1) ─ (N) product_details
warehouse_products (1) ─ (N) product_specifications
warehouse_products (1) ─ (N) warehouse_product_images
warehouse_products (N) ─ (1) suppliers

suppliers (1) ──────── (N) purchase_orders
suppliers (1) ──────── (N) supplier_payables

purchase_orders (1) ─── (N) purchase_order_items
purchase_orders (1) ─── (N) supplier_payables

purchase_order_items (1) ─ (N) product_details

export_orders (1) ────── (N) export_order_items

supplier_payables (1) ── (N) supplier_payments
```

---

## 📊 THỐNG KÊ DATABASE

| Module | Số bảng | Bảng chính |
|--------|---------|-----------|
| **Auth** | 5 | users, customers, employees |
| **Product** | 3 | products, categories, product_images |
| **Cart** | 2 | carts, cart_items |
| **Order** | 2 | orders, order_items |
| **Payment** | 2 | payments, bank_accounts |
| **Inventory** | 10 | warehouse_products, inventory_stock, suppliers |
| **Accounting** | 6 | financial_transaction, supplier_payables |
| **TỔNG** | **30** | - |

---

## 🎯 ĐẶC ĐIỂM NỔI BẬT

### **1. Quản lý Serial/IMEI**
- Bảng `product_details` lưu từng serial riêng biệt
- Theo dõi trạng thái: IN_STOCK, SOLD, DEFECTIVE, RETURNED
- Liên kết với đơn hàng đã bán

### **2. Tách biệt Warehouse & Product**
- `warehouse_products`: Sản phẩm trong kho (nội bộ)
- `products`: Sản phẩm hiển thị cho khách (public)
- Linh hoạt quản lý giá nhập/bán

### **3. Quản lý công nợ NCC**
- `supplier_payables`: Công nợ phải trả
- `supplier_payments`: Lịch sử thanh toán
- Tự động tính toán còn nợ

### **4. Kế toán tự động**
- `financial_transaction`: Ghi nhận mọi giao dịch
- Tự động tạo khi có đơn hàng/thanh toán
- Hỗ trợ báo cáo tài chính

### **5. Tích hợp GHN**
- Lưu `ghn_order_code` trong orders
- Theo dõi trạng thái vận chuyển
- Webhook cập nhật tự động

---

**File này được tạo dựa trên code thực tế từ các Entity class trong dự án!** ✅
