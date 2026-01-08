# KIẾN TRÚC TỔNG QUAN HỆ THỐNG BÁN ĐỒ CÔNG NGHỆ

## 1. KIẾN TRÚC TỔNG QUAN

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Browser    │  │    Mobile    │  │   Tablet     │          │
│  │  (Desktop)   │  │   (Phone)    │  │              │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↓ HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                            │
│                     (Next.js 14 - React)                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Pages & Components                                       │  │
│  │  • Customer UI (Trang chủ, Sản phẩm, Giỏ hàng, ...)     │  │
│  │  • Admin Dashboard (Quản lý toàn bộ hệ thống)           │  │
│  │  • Employee Portal (Kho, Bán hàng, Giao hàng, ...)      │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  State Management: Zustand                                │  │
│  │  Styling: TailwindCSS                                     │  │
│  │  API Client: Axios                                        │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ REST API
┌─────────────────────────────────────────────────────────────────┐
│                      API GATEWAY LAYER                           │
│                    (Spring Security + JWT)                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  • Authentication & Authorization                         │  │
│  │  • JWT Token Validation                                   │  │
│  │  • CORS Configuration                                     │  │
│  │  • Rate Limiting                                          │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                             │
│                  (Spring Boot 3.x - Java 17)                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    CONTROLLERS                            │  │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐           │  │
│  │  │   Auth     │ │  Product   │ │   Order    │           │  │
│  │  │ Controller │ │ Controller │ │ Controller │  ...      │  │
│  │  └────────────┘ └────────────┘ └────────────┘           │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ↓                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                     SERVICES                              │  │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐           │  │
│  │  │   Auth     │ │  Product   │ │   Order    │           │  │
│  │  │  Service   │ │  Service   │ │  Service   │  ...      │  │
│  │  └────────────┘ └────────────┘ └────────────┘           │  │
│  │  • Business Logic                                         │  │
│  │  • Data Validation                                        │  │
│  │  • Transaction Management                                 │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ↓                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                   REPOSITORIES                            │  │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐           │  │
│  │  │   User     │ │  Product   │ │   Order    │           │  │
│  │  │ Repository │ │ Repository │ │ Repository │  ...      │  │
│  │  └────────────┘ └────────────┘ └────────────┘           │  │
│  │  • Spring Data JPA                                        │  │
│  │  • Custom Queries                                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      DATA LAYER                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              MySQL Database (Primary)                     │  │
│  │  • Users, Products, Orders, Inventory                     │  │
│  │  • Payments, Reviews, Categories                          │  │
│  │  • Shipping Assignments                                   │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────────┐
│                   EXTERNAL SERVICES                              │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐               │
│  │   SePay    │  │    GHN     │  │   Email    │               │
│  │  Payment   │  │  Shipping  │  │  Service   │               │
│  └────────────┘  └────────────┘  └────────────┘               │
└─────────────────────────────────────────────────────────────────┘
```

## 2. KIẾN TRÚC CHI TIẾT CÁC MODULE

### 2.1. Module Authentication & Authorization
```
┌─────────────────────────────────────────┐
│         Auth Module                      │
├─────────────────────────────────────────┤
│ Controllers:                             │
│  • AuthController                        │
│  • CustomerController                    │
│  • EmployeeRegistrationController        │
├─────────────────────────────────────────┤
│ Services:                                │
│  • AuthService                           │
│  • JwtService                            │
│  • EmployeeRegistrationService           │
├─────────────────────────────────────────┤
│ Entities:                                │
│  • User (CUSTOMER, ADMIN, EMPLOYEE)      │
│  • Customer                              │
│  • Employee                              │
│  • EmployeeRegistration                  │
├─────────────────────────────────────────┤
│ Security:                                │
│  • JWT Token (Access + Refresh)         │
│  • BCrypt Password Encoding              │
│  • Role-based Access Control             │
└─────────────────────────────────────────┘
```

### 2.2. Module Product Management
```
┌─────────────────────────────────────────┐
│       Product Module                     │
├─────────────────────────────────────────┤
│ Controllers:                             │
│  • ProductController                     │
│  • CategoryController                    │
│  • ProductImageController                │
├─────────────────────────────────────────┤
│ Services:                                │
│  • ProductService                        │
│  • CategoryService                       │
│  • ProductImageService                   │
├─────────────────────────────────────────┤
│ Entities:                                │
│  • Product                               │
│  • Category (Tree Structure)            │
│  • ProductImage                          │
│  • TechSpecs (JSON)                      │
├─────────────────────────────────────────┤
│ Features:                                │
│  • CRUD Operations                       │
│  • Search & Filter by Specs             │
│  • Multi-image Upload                    │
│  • Category Hierarchy                    │
│  • Publish/Unpublish                     │
└─────────────────────────────────────────┘
```

### 2.3. Module Inventory Management
```
┌─────────────────────────────────────────┐
│      Inventory Module                    │
├─────────────────────────────────────────┤
│ Controllers:                             │
│  • InventoryController                   │
│  • SupplierController                    │
├─────────────────────────────────────────┤
│ Services:                                │
│  • InventoryService                      │
│  • StockService                          │
├─────────────────────────────────────────┤
│ Entities:                                │
│  • Stock (Tồn kho)                       │
│  • PurchaseOrder (Phiếu nhập)            │
│  • ExportOrder (Phiếu xuất)              │
│  • Supplier (Nhà cung cấp)               │
│  • WarrantyCard (Phiếu bảo hành)         │
├─────────────────────────────────────────┤
│ Features:                                │
│  • Import/Export Management              │
│  • Stock Tracking                        │
│  • Serial Number Management              │
│  • Warranty Card Generation              │
│  • Supplier Management                   │
└─────────────────────────────────────────┘
```

### 2.4. Module Order Management
```
┌─────────────────────────────────────────┐
│        Order Module                      │
├─────────────────────────────────────────┤
│ Controllers:                             │
│  • OrderController                       │
│  • OrderManagementController             │
│  • ShipperAssignmentController           │
├─────────────────────────────────────────┤
│ Services:                                │
│  • OrderService                          │
│  • ShipperAssignmentService              │
│  • ShippingService (GHN API)             │
├─────────────────────────────────────────┤
│ Entities:                                │
│  • Order                                 │
│  • OrderItem                             │
│  • ShipperAssignment                     │
│  • OrderStatus (Enum)                    │
├─────────────────────────────────────────┤
│ Features:                                │
│  • Order Creation & Tracking             │
│  • Status Management                     │
│  • Shipper Assignment                    │
│  • GHN Integration                       │
│  • Internal Delivery                     │
└─────────────────────────────────────────┘
```

### 2.5. Module Payment
```
┌─────────────────────────────────────────┐
│       Payment Module                     │
├─────────────────────────────────────────┤
│ Controllers:                             │
│  • PaymentController                     │
│  • WebhookController (SePay)             │
├─────────────────────────────────────────┤
│ Services:                                │
│  • PaymentService                        │
│  • SePayService                          │
├─────────────────────────────────────────┤
│ Entities:                                │
│  • Payment                               │
│  • PaymentMethod (Enum)                  │
│  • PaymentStatus (Enum)                  │
├─────────────────────────────────────────┤
│ Features:                                │
│  • COD (Cash on Delivery)                │
│  • Bank Transfer (SePay)                 │
│  • Payment Verification                  │
│  • Auto-cancel Unpaid Orders             │
│  • Webhook Handling                      │
└─────────────────────────────────────────┘
```

### 2.6. Module Cart & Wishlist
```
┌─────────────────────────────────────────┐
│      Cart & Wishlist Module              │
├─────────────────────────────────────────┤
│ Controllers:                             │
│  • CartController                        │
│  • WishlistController                    │
├─────────────────────────────────────────┤
│ Services:                                │
│  • CartService                           │
│  • WishlistService                       │
├─────────────────────────────────────────┤
│ Entities:                                │
│  • Cart                                  │
│  • CartItem                              │
│  • Wishlist                              │
├─────────────────────────────────────────┤
│ Features:                                │
│  • Add/Remove Items                      │
│  • Update Quantity                       │
│  • Stock Validation                      │
│  • Selective Checkout                    │
└─────────────────────────────────────────┘
```

### 2.7. Module Review & Rating
```
┌─────────────────────────────────────────┐
│       Review Module                      │
├─────────────────────────────────────────┤
│ Controllers:                             │
│  • ProductReviewController               │
├─────────────────────────────────────────┤
│ Services:                                │
│  • ProductReviewService                  │
├─────────────────────────────────────────┤
│ Entities:                                │
│  • ProductReview                         │
│  • isVerifiedPurchase (Boolean)          │
├─────────────────────────────────────────┤
│ Features:                                │
│  • Rating (1-5 stars)                    │
│  • Comment (with/without purchase)       │
│  • Verified Purchase Badge               │
│  • Admin Delete                          │
│  • Rating Statistics                     │
└─────────────────────────────────────────┘
```

## 3. LUỒNG DỮ LIỆU CHÍNH

### 3.1. Luồng Đặt Hàng
```
Customer → Browse Products → Add to Cart → Select Items → Checkout
    ↓
Enter Shipping Info → Choose Payment Method → Create Order
    ↓
[COD] → Order Created → Assign Shipper → Delivery
[Bank Transfer] → Pending Payment → Verify Payment → Assign Shipper → Delivery
    ↓
Update Stock → Generate Warranty Card → Complete Order
```

### 3.2. Luồng Quản Lý Kho
```
Supplier → Create Purchase Order → Import Products → Generate Serial Numbers
    ↓
Update Stock → Assign to Warehouse → Ready for Sale
    ↓
Customer Order → Export Order → Reduce Stock → Delivery
```

### 3.3. Luồng Giao Hàng
```
Order Created → [Internal Delivery] → Shipper Claims → Pickup → Deliver
                [GHN Shipping] → Create GHN Order → Track Status → Deliver
```

## 4. CÔNG NGHỆ SỬ DỤNG

### Frontend
- **Framework**: Next.js 14 (React 18)
- **Language**: TypeScript
- **Styling**: TailwindCSS
- **State Management**: Zustand
- **HTTP Client**: Axios
- **Icons**: React Icons
- **Notifications**: React Hot Toast

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Security**: Spring Security + JWT
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA (Hibernate)
- **Build Tool**: Maven
- **Scheduler**: Spring Scheduler

### External Services
- **Payment**: SePay API
- **Shipping**: Giao Hàng Nhanh (GHN) API
- **Email**: SMTP (Gmail)

## 5. BẢO MẬT

### Authentication
- JWT Token (Access Token + Refresh Token)
- BCrypt Password Hashing
- Token Expiration & Refresh

### Authorization
- Role-based Access Control (RBAC)
- Roles: CUSTOMER, ADMIN, EMPLOYEE
- Employee Positions: SALE, WAREHOUSE, SHIPPER, PRODUCT_MANAGER, ACCOUNTANT, CSKH

### API Security
- CORS Configuration
- CSRF Protection
- Rate Limiting
- Input Validation
- SQL Injection Prevention (JPA)

## 6. TÍNH NĂNG NỔI BẬT

### Cho Khách Hàng
✅ Tìm kiếm & lọc sản phẩm theo thông số kỹ thuật
✅ Giỏ hàng với chọn sản phẩm trước khi thanh toán
✅ Nhiều phương thức thanh toán (COD, Chuyển khoản)
✅ Theo dõi đơn hàng real-time
✅ Đánh giá & bình luận sản phẩm
✅ Kiểm tra bảo hành bằng serial

### Cho Admin
✅ Dashboard tổng quan
✅ Quản lý toàn bộ hệ thống
✅ Phê duyệt nhân viên
✅ Báo cáo & thống kê

### Cho Nhân Viên
✅ Quản lý kho (nhập/xuất hàng)
✅ Quản lý đơn hàng
✅ Giao hàng nội bộ
✅ Quản lý sản phẩm
✅ Xem danh sách khách hàng

## 7. TRIỂN KHAI

### Development
- Frontend: `npm run dev` (Port 3000)
- Backend: `mvn spring-boot:run` (Port 8080)
- Database: MySQL (Port 3306)

### Production
- Frontend: Vercel / Netlify
- Backend: AWS / Heroku / VPS
- Database: AWS RDS / Cloud SQL
- CDN: Cloudflare

---

**Ghi chú**: Hệ thống được thiết kế theo kiến trúc Monolithic với module hóa rõ ràng, dễ bảo trì và mở rộng.
