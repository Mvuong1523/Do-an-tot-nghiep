# SƠ ĐỒ KIẾN TRÚC HỆ THỐNG - PHIÊN BẢN ĐƠN GIẢN

## 1. KIẾN TRÚC TỔNG QUAN (3 TẦNG)

```
[CLIENT - Web Browser]
         |
         | HTTPS/REST API
         v
[FRONTEND - Next.js 14]
    - Pages & Components
    - State Management (Zustand)
    - Axios HTTP Client
         |
         | HTTP/JSON
         v
[BACKEND - Spring Boot 3.5.6]
    - Security Layer (JWT + Spring Security)
    - Controller Layer (REST API)
    - Service Layer (Business Logic)
    - Repository Layer (Data Access)
         |
         | JDBC
         v
[DATABASE - MySQL 8.0]
    - 22 Tables
    - Relationships

[EXTERNAL SERVICES]
    - SePay (Payment)
    - GHTK (Shipping)
    - Cloudinary (Image CDN)
    - SMTP (Email)
```

---

## 2. BACKEND - 8 MODULES

```
1. AUTH MODULE
   - Login, Register, Logout
   - JWT Token
   - Employee Registration
   - OTP Verification

2. PRODUCT MODULE
   - Product CRUD
   - Category Management
   - Search & Filter

3. CART MODULE
   - Add/Update/Remove Items
   - Cart Management

4. ORDER MODULE
   - Create Order
   - Track Order
   - Cancel Order

5. PAYMENT MODULE
   - SePay Integration
   - Payment Webhook
   - Verify Payment

6. INVENTORY MODULE
   - Stock Management
   - Purchase Orders
   - Export Orders
   - Supplier Management

7. SHIPPING MODULE
   - GHTK Integration
   - Calculate Shipping Fee

8. FILE MODULE
   - Cloudinary Upload
   - Image Management
```

---

## 3. FRONTEND STRUCTURE

```
app/
  (customer)/
    - Home Page
    - Products
    - Cart
    - Checkout
    - Orders
  
  (employee)/
    - Warehouse Management
    - Product Management
  
  (admin)/
    - Admin Dashboard
  
  login/register/
    - Authentication

components/
  - Layout Components
  - Product Components
  - Category Components

store/
  - authStore (Authentication)
  - cartStore (Shopping Cart)
  - languageStore (i18n)
```

---

## 4. DATABASE - 22 TABLES

```
AUTHENTICATION (5 tables)
- users
- customers
- employees
- employee_registration
- otp_verification

PRODUCT (2 tables)
- categories
- products

WAREHOUSE (6 tables)
- warehouse_products
- product_details
- warehouse_product_images
- product_specifications
- inventory_stock
- (linked to suppliers)

PURCHASE (3 tables)
- suppliers
- purchase_orders
- purchase_order_items

EXPORT (2 tables)
- export_orders
- export_order_items

SHOPPING (2 tables)
- carts
- cart_items

ORDERS (3 tables)
- orders
- order_items
- payments
```

---

## 5. LUỒNG XỬ LÝ CHÍNH

### A. ĐĂNG NHẬP
```
User nhập email/password
  -> Frontend gửi POST /api/auth/login
  -> Backend xác thực
  -> Tạo JWT token
  -> Trả về token + user info
  -> Frontend lưu vào Zustand + LocalStorage
```

### B. MUA HÀNG
```
User duyệt sản phẩm
  -> Thêm vào giỏ hàng (CartStore)
  -> POST /api/cart/add
  -> Backend lưu vào database
  -> Cập nhật UI
```

### C. ĐẶT HÀNG
```
User checkout
  -> Tạo đơn hàng POST /api/orders
  -> Backend kiểm tra tồn kho
  -> Tính tổng tiền
  -> Tạo payment với SePay
  -> SePay gửi webhook khi thanh toán
  -> Cập nhật trạng thái đơn hàng
  -> Gửi email xác nhận
```

---

## 6. BẢO MẬT (5 LỚP)

```
1. CORS Configuration
   - Chỉ cho phép origins cụ thể

2. JWT Authentication Filter
   - Kiểm tra token trong header
   - Validate token

3. Spring Security
   - Phân quyền theo role (RBAC)
   - Bảo vệ endpoints
   - Mã hóa password (BCrypt)

4. Input Validation
   - @Valid annotations
   - Chống SQL injection

5. Exception Handling
   - Global exception handler
   - Custom error responses
```

---

## 7. PHÂN QUYỀN

```
CUSTOMER (Khách hàng)
- Xem sản phẩm
- Quản lý giỏ hàng
- Đặt hàng
- Xem lịch sử đơn hàng

EMPLOYEE (Nhân viên)
- Tất cả quyền của Customer
- Quản lý sản phẩm (Product Manager)
- Quản lý kho (Warehouse Manager)

ADMIN (Quản trị viên)
- Tất cả quyền
- Quản lý users
- Duyệt đăng ký nhân viên
- Cấu hình hệ thống
```

---

## 8. CÔNG NGHỆ SỬ DỤNG

### BACKEND
- Spring Boot 3.5.6
- Java 21
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.0
- Swagger/OpenAPI

### FRONTEND
- Next.js 14
- TypeScript
- React 18
- Tailwind CSS
- Zustand
- Axios

### EXTERNAL
- SePay (Thanh toán)
- GHTK (Vận chuyển)
- Cloudinary (Lưu ảnh)
- SMTP (Email)

---

## 9. API ENDPOINTS CHÍNH

```
AUTHENTICATION
POST /api/auth/login
POST /api/auth/register
POST /api/auth/employee/register

PRODUCTS
GET  /api/products
GET  /api/products/{id}
POST /api/products (EMPLOYEE)
PUT  /api/products/{id} (EMPLOYEE)

CART
GET  /api/cart
POST /api/cart/add
PUT  /api/cart/update/{itemId}
DELETE /api/cart/remove/{itemId}

ORDERS
GET  /api/orders
POST /api/orders
GET  /api/orders/{id}
PUT  /api/orders/{id}/cancel

PAYMENTS
POST /api/payments/create
POST /api/payments/webhook

INVENTORY
GET  /api/inventory/stock (EMPLOYEE)
POST /api/inventory/purchase (EMPLOYEE)
POST /api/inventory/export (EMPLOYEE)
```

---

## 10. TRIỂN KHAI

```
PRODUCTION ENVIRONMENT

Load Balancer (Nginx)
    |
    +-- Frontend Server (Next.js:3000)
    |
    +-- Backend Server (Spring Boot:8080)
            |
            +-- Database Server (MySQL:3306)

External Services:
- SePay API
- GHTK API
- Cloudinary CDN
- SMTP Server
```

---

## 11. HƯỚNG DẪN VẼ TRÊN VISUAL PARADIGM

### Bước 1: Tạo Component Diagram
1. Mở Visual Paradigm
2. New -> Component Diagram
3. Tạo 4 components chính:
   - Frontend Component
   - Backend Component
   - Database Component
   - External Services Component
4. Kết nối bằng dependencies

### Bước 2: Tạo Deployment Diagram
1. New -> Deployment Diagram
2. Tạo nodes:
   - Web Server Node
   - Application Server Node
   - Database Server Node
3. Đặt artifacts vào nodes
4. Kết nối các nodes

### Bước 3: Tạo Package Diagram cho Backend
1. New -> Package Diagram
2. Tạo 8 packages:
   - auth
   - product
   - cart
   - order
   - payment
   - inventory
   - shipping
   - file
3. Thể hiện dependencies giữa packages

### Bước 4: Tùy chỉnh
- Màu Frontend: Xanh dương (#E3F2FD)
- Màu Backend: Cam nhạt (#FFE0B2)
- Màu Database: Vàng nhạt (#FFF9C4)
- Màu External: Xanh lá (#E8F5E9)
- Font: Arial 10pt
- Line Style: Rectilinear (đường thẳng góc vuông)

### Bước 5: Export
- File -> Export -> Active Diagram as Image
- Chọn PNG hoặc SVG
- Resolution: 300 DPI

---

## KẾT LUẬN

Hệ thống được thiết kế với các đặc điểm:

✓ MODULAR - 8 modules độc lập, dễ bảo trì
✓ SCALABLE - Có thể mở rộng từng module
✓ SECURE - 5 lớp bảo mật
✓ MAINTAINABLE - Code có cấu trúc rõ ràng
✓ TESTABLE - Dễ viết test
