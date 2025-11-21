# VISUAL PARADIGM ERD GUIDE - PHIÊN BẢN ĐẦY ĐỦ

## TỔNG QUAN HỆ THỐNG

Hệ thống gồm **10 MODULES** với **32 BẢNG**:
1. Authentication (5 bảng)
2. Product (2 bảng)
3. Warehouse (6 bảng)
4. Purchase (3 bảng)
5. Export (2 bảng)
6. Shopping (2 bảng)
7. Orders (3 bảng)
8. Payments (1 bảng)
9. **Delivery (5 bảng)** ← MỚI
10. **Customer Support (3 bảng)** ← MỚI

---

# HƯỚNG DẪN VẼ TRÊN VISUAL PARADIGM

## Bước 1: Chuẩn bị
1. Mở Visual Paradigm
2. New → Entity Relationship Diagram
3. Đặt tên: "E-Commerce Database ERD"

## Bước 2: Tạo Entity
- Toolbar → Entity
- Click canvas → Đặt tên bảng
- Double-click → Thêm columns

## Bước 3: Tạo Relationships
- Chọn loại: 1:1, 1:N, N:M
- Click entity nguồn → Kéo đến entity đích
- Format → Line Style → Rectilinear (đường thẳng)

## Bước 4: Màu sắc theo Module
- Authentication: Xanh dương (#E3F2FD)
- Product: Xanh lá (#E8F5E9)
- Warehouse: Vàng (#FFF9C4)
- Purchase: Cam (#FFE0B2)
- Export: Đỏ nhạt (#FFCDD2)
- Shopping: Tím (#F3E5F5)
- Orders: Xanh ngọc (#E0F2F1)
- Payments: Hồng (#FCE4EC)
- **Delivery: Xanh navy (#E8EAF6)** ← MỚI
- **Support: Nâu nhạt (#EFEBE9)** ← MỚI

---

# DANH SÁCH ĐẦY ĐỦ 32 BẢNG

## MODULE 1: AUTHENTICATION (5 bảng)

### 1. users
- id (BIGINT, PK)
- email (VARCHAR(255), UK, NN)
- password (VARCHAR(255), NN)
- role (ENUM, NN)
- status (ENUM, NN)

**Relations:**
- 1:1 → customers
- 1:1 → employees
- 1:1 → carts
- 1:N → orders
- 1:N → payments
- 1:N → support_tickets

### 2. customers
- id (BIGINT, PK)
- user_id (BIGINT, FK→users, UK, NN)
- full_name (VARCHAR(255), NN)
- phone (VARCHAR(20), UK, NN)
- gender (VARCHAR(10))
- birth_date (DATE)
- address (VARCHAR(500))

**Relations:**
- N:1 → users

### 3. employees
- id (BIGINT, PK)
- user_id (BIGINT, FK→users, UK)
- position (ENUM)
- full_name (VARCHAR(255))
- phone (VARCHAR(20))
- address (VARCHAR(500))
- first_login (BOOLEAN, NN)

**Relations:**
- N:1 → users
- 1:N → support_tickets (assigned_to)

### 4. employee_registration
- id (BIGINT, PK)
- full_name (VARCHAR(255), NN)
- email (VARCHAR(255), UK, NN)
- phone (VARCHAR(20), UK, NN)
- address (VARCHAR(500))
- position (ENUM, NN)
- note (TEXT)
- approved (BOOLEAN)
- created_at (TIMESTAMP)
- approved_at (TIMESTAMP)

**Relations:** Không có

### 5. otp_verification
- id (BIGINT, PK)
- email (VARCHAR(255))
- encoded_password (VARCHAR(255))
- full_name (VARCHAR(255))
- phone (VARCHAR(20))
- address (VARCHAR(500))
- otp_code (VARCHAR(10))
- created_at (TIMESTAMP)
- expires_at (TIMESTAMP)
- verified (BOOLEAN)

**Relations:** Không có

---

## MODULE 2: PRODUCT (2 bảng)

### 6. categories
- id (BIGINT, PK)
- name (VARCHAR(255), NN)
- slug (VARCHAR(255), UK)
- description (TEXT)
- image_url (VARCHAR(500))
- display_order (INT)
- active (BOOLEAN)
- parent_id (BIGINT, FK→categories)

**Relations:**
- N:1 → categories (self-ref)
- 1:N → categories (children)
- 1:N → products

### 7. products
- id (BIGINT, PK)
- category_id (BIGINT, FK→categories)
- name (VARCHAR(255), NN)
- price (DECIMAL(15,2))
- sku (VARCHAR(100), UK)
- description (TEXT)
- image_url (VARCHAR(500))
- stock_quantity (BIGINT)
- tech_specs_json (TEXT)
- product_detail_id (BIGINT, FK→product_details)
- warehouse_product_id (BIGINT, FK→warehouse_products)

**Relations:**
- N:1 → categories
- 1:1 → product_details
- 1:1 → warehouse_products
- 1:N → cart_items
- 1:N → order_items

---

## MODULE 3: WAREHOUSE (6 bảng)

### 8. warehouse_products
- id (BIGINT, PK)
- sku (VARCHAR(64), UK, NN)
- internal_name (VARCHAR(255), NN)
- tech_specs_json (TEXT)
- description (TEXT)
- supplier_id (BIGINT, FK→suppliers)
- last_import_date (TIMESTAMP)

**Relations:**
- N:1 → suppliers
- 1:1 → products
- 1:N → product_details
- 1:N → warehouse_product_images
- 1:N → product_specifications
- 1:N → inventory_stock
- 1:N → purchase_order_items
- 1:N → export_order_items

### 9. product_details
- id (BIGINT, PK)
- serial_number (VARCHAR(64), UK, NN)
- import_price (DECIMAL(15,2), NN)
- sale_price (DECIMAL(15,2))
- import_date (TIMESTAMP)
- status (ENUM, NN)
- warehouse_product_id (BIGINT, FK→warehouse_products, NN)
- purchase_order_item_id (BIGINT, FK→purchase_order_items)
- warranty_months (INT)
- sold_order_id (BIGINT)
- sold_date (TIMESTAMP)
- note (TEXT)

**Relations:**
- N:1 → warehouse_products
- N:1 → purchase_order_items
- 1:1 → products

### 10. warehouse_product_images
- id (BIGINT, PK)
- url (VARCHAR(500))
- warehouse_product_id (BIGINT, FK→warehouse_products)

**Relations:**
- N:1 → warehouse_products

### 11. product_specifications
- id (BIGINT, PK)
- warehouse_product_id (BIGINT, FK→warehouse_products, NN)
- spec_key (VARCHAR(100), NN)
- spec_value (VARCHAR(255), NN)

**Relations:**
- N:1 → warehouse_products

### 12. inventory_stock
- id (BIGINT, PK)
- warehouse_product_id (BIGINT, FK→warehouse_products, UK, NN)
- on_hand (BIGINT, NN)
- reserved (BIGINT, NN)
- damaged (BIGINT, NN)
- last_audit_date (DATE)

**Relations:**
- N:1 → warehouse_products

---

## MODULE 4: PURCHASE (3 bảng)

### 13. suppliers
- id (BIGINT, PK)
- name (VARCHAR(255), NN)
- contact_name (VARCHAR(255))
- phone (VARCHAR(20))
- email (VARCHAR(255))
- address (VARCHAR(500))
- tax_code (VARCHAR(50), UK, NN)
- bank_account (VARCHAR(100))
- payment_term (VARCHAR(255))
- active (BOOLEAN, NN)
- auto_created (BOOLEAN, NN)

**Relations:**
- 1:N → warehouse_products
- 1:N → purchase_orders

### 14. purchase_orders
- id (BIGINT, PK)
- po_code (VARCHAR(50), UK, NN)
- supplier_tax_code (VARCHAR(50), FK→suppliers.tax_code, NN)
- order_date (TIMESTAMP)
- received_date (TIMESTAMP)
- status (ENUM)
- created_by (VARCHAR(255))
- note (TEXT)

**Relations:**
- N:1 → suppliers
- 1:N → purchase_order_items

### 15. purchase_order_items
- id (BIGINT, PK)
- purchase_order_id (BIGINT, FK→purchase_orders, NN)
- sku (VARCHAR(64), UK, NN)
- warehouse_product_id (BIGINT, FK→warehouse_products)
- quantity (BIGINT)
- unit_cost (DECIMAL(15,2))
- warranty_months (INT)
- note (TEXT)

**Relations:**
- N:1 → purchase_orders
- N:1 → warehouse_products
- 1:N → product_details

---

## MODULE 5: EXPORT (2 bảng)

### 16. export_orders
- id (BIGINT, PK)
- export_code (VARCHAR(50), UK, NN)
- export_date (TIMESTAMP)
- created_by (VARCHAR(255))
- reason (VARCHAR(255))
- note (TEXT)
- status (ENUM)

**Relations:**
- 1:N → export_order_items

### 17. export_order_items
- id (BIGINT, PK)
- export_order_id (BIGINT, FK→export_orders, NN)
- warehouse_product_id (BIGINT, FK→warehouse_products)
- sku (VARCHAR(64), NN)
- quantity (BIGINT, NN)
- serial_numbers (TEXT)
- total_cost (DECIMAL(15,2))

**Relations:**
- N:1 → export_orders
- N:1 → warehouse_products

---

## MODULE 6: SHOPPING (2 bảng)

### 18. carts
- id (BIGINT, PK)
- user_id (BIGINT, FK→users, UK, NN)
- created_at (TIMESTAMP, NN)
- updated_at (TIMESTAMP)

**Relations:**
- 1:1 → users
- 1:N → cart_items

### 19. cart_items
- id (BIGINT, PK)
- cart_id (BIGINT, FK→carts, NN)
- product_id (BIGINT, FK→products, NN)
- quantity (INT, NN)
- price (DECIMAL(15,2), NN)
- added_at (TIMESTAMP, NN)

**Relations:**
- N:1 → carts
- N:1 → products

---

## MODULE 7: ORDERS (3 bảng)

### 20. orders
- id (BIGINT, PK)
- order_code (VARCHAR(50), UK, NN)
- user_id (BIGINT, FK→users, NN)
- customer_name (VARCHAR(255), NN)
- customer_phone (VARCHAR(20), NN)
- customer_email (VARCHAR(255), NN)
- shipping_address (TEXT, NN)
- note (TEXT)
- subtotal (DECIMAL(15,2), NN)
- shipping_fee (DECIMAL(15,2), NN)
- discount (DECIMAL(15,2), NN)
- total (DECIMAL(15,2), NN)
- payment_status (ENUM, NN)
- payment_id (BIGINT)
- paid_at (TIMESTAMP)
- status (ENUM, NN)
- created_at (TIMESTAMP, NN)
- confirmed_at (TIMESTAMP)
- shipped_at (TIMESTAMP)
- delivered_at (TIMESTAMP)
- cancelled_at (TIMESTAMP)
- cancel_reason (TEXT)

**Relations:**
- N:1 → users
- 1:N → order_items
- 1:1 → payments
- 1:N → shipments

### 21. order_items
- id (BIGINT, PK)
- order_id (BIGINT, FK→orders, NN)
- product_id (BIGINT, FK→products, NN)
- product_name (VARCHAR(255), NN)
- price (DECIMAL(15,2), NN)
- quantity (INT, NN)
- subtotal (DECIMAL(15,2), NN)
- serial_number (VARCHAR(64))

**Relations:**
- N:1 → orders
- N:1 → products

---

## MODULE 8: PAYMENTS (1 bảng)

### 22. payments
- id (BIGINT, PK)
- payment_code (VARCHAR(50), UK, NN)
- order_id (BIGINT, FK→orders, NN)
- user_id (BIGINT, FK→users, NN)
- amount (DECIMAL(15,2), NN)
- method (ENUM, NN)
- status (ENUM, NN)
- sepay_transaction_id (VARCHAR(255))
- sepay_bank_code (VARCHAR(50))
- sepay_account_number (VARCHAR(50))
- sepay_account_name (VARCHAR(255))
- sepay_content (VARCHAR(500))
- sepay_qr_code (VARCHAR(500))
- sepay_response (TEXT)
- created_at (TIMESTAMP, NN)
- paid_at (TIMESTAMP)
- expired_at (TIMESTAMP)
- failure_reason (TEXT)

**Relations:**
- 1:1 → orders
- N:1 → users

---

## MODULE 9: DELIVERY (5 bảng) ← MỚI

### 23. shipments (TỐI ƯU - 18 columns)
- id (BIGINT, PK)
- order_id (BIGINT, FK→orders, NN)
- shipment_number (INT, NN, DEFAULT 1)
- is_active (BOOLEAN, DEFAULT TRUE)
- tracking_code (VARCHAR(50), UK)
- ghtk_label (VARCHAR(100))
- total_weight (INT, NN)
- value (DECIMAL(15,2), NN)
- shipping_fee (DECIMAL(15,2))
- insurance_fee (DECIMAL(15,2), DEFAULT 0)
- status (ENUM, NN)
- ghtk_status_id (INT)
- ghtk_response (TEXT)
- note (VARCHAR(500))
- cancel_reason (VARCHAR(500))
- created_at (TIMESTAMP, NN)
- picked_at (TIMESTAMP)
- delivered_at (TIMESTAMP)
- cancelled_at (TIMESTAMP)

**Lưu ý:** Địa chỉ gửi/nhận lấy từ bảng `orders`, không duplicate

**Relations:**
- N:1 → orders
- 1:N → shipment_items
- 1:N → shipment_tracking

**Note:** Một order có thể có nhiều shipments (giao lại, giao từng phần)

### 24. shipment_items
- id (BIGINT, PK)
- shipment_id (BIGINT, FK→shipments, NN)
- product_name (VARCHAR(255), NN)
- quantity (INT, NN)
- weight (INT, NN)

**Relations:**
- N:1 → shipments

### 25. shipment_tracking
- id (BIGINT, PK)
- shipment_id (BIGINT, FK→shipments, NN)
- status_id (INT, NN)
- status_text (VARCHAR(255), NN)
- location (VARCHAR(255))
- reason (VARCHAR(500))
- action_time (TIMESTAMP, NN)
- created_at (TIMESTAMP, NN)

**Relations:**
- N:1 → shipments

### 26. shipping_fees (Cache phí vận chuyển)
- id (BIGINT, PK)
- from_province (VARCHAR(100), NN)
- from_district (VARCHAR(100), NN)
- to_province (VARCHAR(100), NN)
- to_district (VARCHAR(100), NN)
- weight (INT, NN)
- fee (DECIMAL(15,2), NN)
- created_at (TIMESTAMP, NN)
- expires_at (TIMESTAMP, NN)

**Relations:** Không có (Cache table)
**Lưu ý:** Cache 24h để tránh gọi API GHTK nhiều lần

### 27. ghtk_webhooks (Log webhook từ GHTK)
- id (BIGINT, PK)
- label_id (VARCHAR(100), NN)
- status_id (INT, NN)
- action_time (TIMESTAMP, NN)
- reason (VARCHAR(500))
- raw_data (TEXT, NN)
- processed (BOOLEAN, DEFAULT FALSE)
- processed_at (TIMESTAMP)
- created_at (TIMESTAMP, NN)

**Relations:** Không có (Webhook log)
**Lưu ý:** Nhận webhook tự động từ GHTK khi trạng thái đơn thay đổi

---

## MODULE 10: CUSTOMER SUPPORT (3 bảng) ← MỚI

### 28. support_tickets
- id (BIGINT, PK)
- ticket_code (VARCHAR(50), UK, NN)
- user_id (BIGINT, FK→users, NN)
- order_id (BIGINT, FK→orders)
- subject (VARCHAR(255), NN)
- description (TEXT, NN)
- category (ENUM, NN)
- priority (ENUM, NN)
- status (ENUM, NN)
- assigned_to (BIGINT, FK→employees)
- created_at (TIMESTAMP, NN)
- updated_at (TIMESTAMP)
- resolved_at (TIMESTAMP)
- resolution (TEXT)

**Relations:**
- N:1 → users
- N:1 → orders (optional)
- N:1 → employees (assigned_to)
- 1:N → ticket_messages
- 1:N → ticket_attachments

### 29. ticket_messages
- id (BIGINT, PK)
- ticket_id (BIGINT, FK→support_tickets, NN)
- sender_id (BIGINT, FK→users, NN)
- message (TEXT, NN)
- is_internal (BOOLEAN)
- created_at (TIMESTAMP, NN)

**Relations:**
- N:1 → support_tickets
- N:1 → users (sender)

### 30. ticket_attachments
- id (BIGINT, PK)
- ticket_id (BIGINT, FK→support_tickets, NN)
- file_name (VARCHAR(255), NN)
- file_url (VARCHAR(500), NN)
- file_size (BIGINT)
- file_type (VARCHAR(50))
- uploaded_by (BIGINT, FK→users, NN)
- uploaded_at (TIMESTAMP, NN)

**Relations:**
- N:1 → support_tickets
- N:1 → users (uploaded_by)

---

## BONUS: REVIEWS & RATINGS (2 bảng - TÙY CHỌN)

### 31. product_reviews
- id (BIGINT, PK)
- product_id (BIGINT, FK→products, NN)
- user_id (BIGINT, FK→users, NN)
- order_id (BIGINT, FK→orders)
- rating (INT, NN) -- 1-5 stars
- title (VARCHAR(255))
- comment (TEXT)
- verified_purchase (BOOLEAN)
- helpful_count (INT)
- created_at (TIMESTAMP, NN)
- updated_at (TIMESTAMP)

**Relations:**
- N:1 → products
- N:1 → users
- N:1 → orders (optional)
- 1:N → review_images

### 32. review_images
- id (BIGINT, PK)
- review_id (BIGINT, FK→product_reviews, NN)
- image_url (VARCHAR(500), NN)
- uploaded_at (TIMESTAMP, NN)

**Relations:**
- N:1 → product_reviews

---

# TỔNG KẾT MỐI QUAN HỆ

## Relationships Summary

```
users (1) ←→ (1) customers
users (1) ←→ (1) employees
users (1) ←→ (1) carts
users (1) ←→ (N) orders
users (1) ←→ (N) payments
users (1) ←→ (N) support_tickets
users (1) ←→ (N) ticket_messages
users (1) ←→ (N) product_reviews

categories (1) ←→ (N) categories (self-ref)
categories (1) ←→ (N) products

products (N) ←→ (1) categories
products (1) ←→ (1) warehouse_products
products (1) ←→ (1) product_details
products (1) ←→ (N) cart_items
products (1) ←→ (N) order_items
products (1) ←→ (N) product_reviews

warehouse_products (1) ←→ (N) product_details
warehouse_products (1) ←→ (N) inventory_stock
warehouse_products (N) ←→ (1) suppliers

purchase_orders (N) ←→ (1) suppliers
purchase_orders (1) ←→ (N) purchase_order_items
purchase_order_items (1) ←→ (N) product_details

export_orders (1) ←→ (N) export_order_items

carts (1) ←→ (N) cart_items

orders (1) ←→ (N) order_items
orders (1) ←→ (1) payments
orders (1) ←→ (N) shipments
orders (1) ←→ (N) support_tickets

shipments (N) ←→ (1) orders
shipments (1) ←→ (N) shipment_items
shipments (1) ←→ (N) shipment_tracking

support_tickets (1) ←→ (N) ticket_messages
support_tickets (1) ←→ (N) ticket_attachments

product_reviews (1) ←→ (N) review_images
```

---

# GỢI Ý BỐ CỤC TRÊN VISUAL PARADIGM

## Layout theo nhóm chức năng:

```
┌─────────────────────────────────────────────────────────┐
│                    TOP SECTION                          │
│  [Authentication Module - 5 tables]                     │
│  users, customers, employees, employee_registration,    │
│  otp_verification                                       │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                   MIDDLE LEFT                           │
│  [Product Module - 2 tables]                            │
│  categories, products                                   │
│                                                         │
│  [Warehouse Module - 6 tables]                          │
│  warehouse_products, product_details,                   │
│  warehouse_product_images, product_specifications,      │
│  inventory_stock                                        │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                   MIDDLE CENTER                         │
│  [Shopping Module - 2 tables]                           │
│  carts, cart_items                                      │
│                                                         │
│  [Orders Module - 3 tables]                             │
│  orders, order_items, payments                          │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                   MIDDLE RIGHT                          │
│  [Purchase Module - 3 tables]                           │
│  suppliers, purchase_orders, purchase_order_items       │
│                                                         │
│  [Export Module - 2 tables]                             │
│  export_orders, export_order_items                      │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                   BOTTOM LEFT                           │
│  [Delivery Module - 5 tables]                           │
│  shipments, shipment_items, shipment_tracking,          │
│  shipping_fees, ghtk_webhooks                           │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                   BOTTOM RIGHT                          │
│  [Customer Support Module - 3 tables]                   │
│  support_tickets, ticket_messages, ticket_attachments   │
│                                                         │
│  [Reviews Module - 2 tables] (Optional)                 │
│  product_reviews, review_images                         │
└─────────────────────────────────────────────────────────┘
```

---

# MÀU SẮC CHO TỪNG MODULE

```
Authentication:  #E3F2FD (Xanh dương nhạt)
Product:         #E8F5E9 (Xanh lá nhạt)
Warehouse:       #FFF9C4 (Vàng nhạt)
Purchase:        #FFE0B2 (Cam nhạt)
Export:          #FFCDD2 (Đỏ nhạt)
Shopping:        #F3E5F5 (Tím nhạt)
Orders:          #E0F2F1 (Xanh ngọc nhạt)
Payments:        #FCE4EC (Hồng nhạt)
Delivery:        #E8EAF6 (Xanh navy nhạt)
Support:         #EFEBE9 (Nâu nhạt)
Reviews:         #FFF3E0 (Cam sáng)
```

---

# CHECKLIST HOÀN THÀNH

- [ ] Tạo 32 entities
- [ ] Thêm tất cả columns với đúng data type
- [ ] Đánh dấu PK, FK, UK, NN
- [ ] Tạo tất cả relationships
- [ ] Chỉnh line style thành Rectilinear
- [ ] Áp dụng màu sắc theo module
- [ ] Sắp xếp layout hợp lý
- [ ] Thêm notes giải thích (nếu cần)
- [ ] Export PNG/SVG với resolution cao
- [ ] Kiểm tra lại tất cả foreign keys

---

# KẾT LUẬN

Hệ thống database hoàn chỉnh với:
- ✓ 32 bảng
- ✓ 10 modules chức năng
- ✓ Tích hợp GHTK Delivery
- ✓ Customer Support đầy đủ
- ✓ Optional Reviews & Ratings
- ✓ Relationships rõ ràng
- ✓ Dễ mở rộng trong tương lai
