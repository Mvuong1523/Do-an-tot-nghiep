# SƠ ĐỒ KIẾN TRÚC HỆ THỐNG E-COMMERCE

## TỔNG QUAN

Hệ thống sử dụng kiến trúc **3-tier Modular Monolith**:
- **Frontend**: Next.js 14 (TypeScript)
- **Backend**: Spring Boot 3.5.6 (Java 21)
- **Database**: MySQL 8.0
- **External Services**: SePay, GHTK, Cloudinary

---

## SƠ ĐỒ TỔNG QUAN

```
┌─────────────────────────────────────────────────────┐
│              CLIENT (Web Browser)                    │
│  - Customer Interface                                │
│  - Employee Dashboard                                │
│  - Admin Panel                                       │
└──────────────────┬──────────────────────────────────┘
                   │ HTTPS/REST API
┌──────────────────▼──────────────────────────────────┐
│         FRONTEND (Next.js 14)                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐          │
│  │  Pages   │  │Components│  │  Store   │          │
│  │  Router  │  │  Layout  │  │ Zustand  │          │
│  └──────────┘  └──────────┘  └──────────┘          │
│  Tech: TypeScript, Tailwind CSS, Axios              │
└──────────────────┬──────────────────────────────────┘
                   │ HTTP/JSON
┌──────────────────▼──────────────────────────────────┐
│         BACKEND (Spring Boot 3.5.6)                  │
│  ┌─────────────────────────────────────────────┐    │
│  │  Security: JWT + Spring Security + CORS     │    │
│  └─────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────┐    │
│  │  Controllers (REST API Endpoints)           │    │
│  │  Auth│Product│Cart│Order│Payment│Inventory  │    │
│  └─────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────┐    │
│  │  Services (Business Logic)                  │    │
│  │  8 Modules: Auth, Product, Cart, Order...   │    │
│  └─────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────┐    │
│  │  Repositories (Spring Data JPA)             │    │
│  └─────────────────────────────────────────────┘    │
└──────────────────┬──────────────────────────────────┘
                   │ JDBC
┌──────────────────▼──────────────────────────────────┐
│         DATABASE (MySQL 8.0)                         │
│  22 Tables: users, products, orders, inventory...   │
└──────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│         EXTERNAL SERVICES                            │
│  SePay │ GHTK │ Cloudinary │ SMTP                    │
└──────────────────────────────────────────────────────┘
```

---

## KIẾN TRÚC BACKEND (8 MODULES)

```
1. AUTH MODULE
   - Login/Register/Logout
   - JWT Token Management
   - Employee Registration
   - OTP Verification

2. PRODUCT MODULE
   - Product CRUD
   - Category Management
   - Product Search
   - Tech Specifications

3. CART MODULE
   - Add/Update/Remove Items
   - Cart Management
   - Price Calculation

4. ORDER MODULE
   - Create Order
   - Order Tracking
   - Order Status Management
   - Cancel Order

5. PAYMENT MODULE
   - SePay Integration
   - Payment Webhook
   - Payment Verification

6. INVENTORY MODULE
   - Stock Management
   - Purchase Orders
   - Export Orders
   - Supplier Management
   - Serial Number Tracking

7. SHIPPING MODULE
   - GHTK API Integration
   - Shipping Fee Calculation
   - Order Tracking

8. FILE MODULE
   - Cloudinary Integration
   - Image Upload/Delete
```

---

## KIẾN TRÚC FRONTEND

```
app/
├── (customer)/      # Customer Routes
│   ├── page.tsx     # Home
│   ├── products/    # Product Listing
│   ├── cart/        # Shopping Cart
│   ├── checkout/    # Checkout
│   └── orders/      # Order History
│
├── (employee)/      # Employee Routes
│   ├── warehouse/   # Warehouse Management
│   └── product-manager/  # Product Management
│
├── (admin)/         # Admin Routes
│   └── admin/       # Admin Dashboard
│
└── login/register/  # Authentication

components/
├── layout/          # Header, Footer, Sidebar
├── product/         # Product Card, List
└── category/        # Category Components

store/ (Zustand)
├── authStore.ts     # Authentication State
├── cartStore.ts     # Cart State
└── languageStore.ts # i18n State
```

---

## LUỒNG DỮ LIỆU

### 1. Authentication Flow
```
User Input → Frontend → POST /api/auth/login
→ AuthController → AuthService → UserRepository
→ JWT Generation → Response → Store in Frontend
```

### 2. Product Purchase Flow
```
Browse → Add to Cart → CartStore (Zustand)
→ POST /api/cart/add → CartController
→ CartService → CartRepository → MySQL
```

### 3. Order Processing Flow
```
Checkout → Create Order → POST /api/orders
→ OrderService (Validate Stock + Calculate)
→ PaymentService → SePay API
→ Webhook → Update Status → Email Notification
```

---

## SECURITY LAYERS

```
1. CORS Configuration
   - Allow specific origins
   - Restrict methods/headers

2. JWT Authentication Filter
   - Extract & validate token
   - Load user details

3. Spring Security
   - Role-based access (RBAC)
   - Endpoint protection
   - BCrypt password encryption

4. Input Validation
   - @Valid annotations
   - SQL injection prevention

5. Exception Handling
   - Global exception handler
   - Custom error responses
```

---

## ROLE-BASED ACCESS CONTROL

```
CUSTOMER:
- Browse products
- Manage cart
- Place orders
- View order history

EMPLOYEE:
- Customer permissions +
- Manage products (Product Manager)
- Manage inventory (Warehouse Manager)

ADMIN:
- All permissions +
- Manage users
- Approve employee registration
- System configuration
```

---

## DATABASE SCHEMA (22 TABLES)

```
Authentication (5):
- users, customers, employees
- employee_registration, otp_verification

Product (2):
- categories, products

Warehouse (6):
- warehouse_products, product_details
- warehouse_product_images, product_specifications
- inventory_stock

Purchase (3):
- suppliers, purchase_orders, purchase_order_items

Export (2):
- export_orders, export_order_items

Shopping (2):
- carts, cart_items

Orders (3):
- orders, order_items, payments
```

---

## API ENDPOINTS

```
Authentication:
POST /api/auth/login
POST /api/auth/register
POST /api/auth/employee/register

Products:
GET  /api/products
GET  /api/products/{id}
POST /api/products (EMPLOYEE)
PUT  /api/products/{id} (EMPLOYEE)

Cart:
GET  /api/cart
POST /api/cart/add
PUT  /api/cart/update/{itemId}

Orders:
GET  /api/orders
POST /api/orders
GET  /api/orders/{id}/track

Payments:
POST /api/payments/create
POST /api/payments/webhook

Inventory:
GET  /api/inventory/stock (EMPLOYEE)
POST /api/inventory/purchase (EMPLOYEE)
```

---

## TECHNOLOGY STACK

### Backend
- Spring Boot 3.5.6
- Java 21
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.0
- Swagger/OpenAPI

### Frontend
- Next.js 14
- TypeScript
- React 18
- Tailwind CSS
- Zustand (State)
- Axios (HTTP)

### External
- SePay (Payment)
- GHTK (Shipping)
- Cloudinary (CDN)
- SMTP (Email)

---

## DEPLOYMENT

```
Production:
- Load Balancer (Nginx)
- Frontend Server (Next.js:3000)
- Backend Server (Spring Boot:8080)
- Database Server (MySQL:3306)
- CDN (Cloudinary)
```

---

## HƯỚNG DẪN VẼ TRÊN VISUAL PARADIGM

### 1. Component Diagram
- Tạo components: Frontend, Backend, Database, External Services
- Kết nối bằng interfaces

### 2. Deployment Diagram
- Tạo nodes: Web Server, App Server, DB Server
- Thể hiện artifacts và connections

### 3. Package Diagram
- Tạo packages cho 8 modules
- Thể hiện dependencies

### 4. Tùy chỉnh
- Màu sắc: Xanh (Frontend), Cam (Backend), Vàng (Database)
- Sử dụng stereotypes
- Thêm notes giải thích
- Export PNG/SVG

---

## KẾT LUẬN

Hệ thống được thiết kế:
- **Modular**: 8 modules độc lập, dễ bảo trì
- **Scalable**: Có thể scale từng module
- **Secure**: Nhiều lớp bảo mật (JWT, RBAC, Validation)
- **Maintainable**: Code rõ ràng, có cấu trúc
- **Testable**: Dễ viết unit test và integration test
