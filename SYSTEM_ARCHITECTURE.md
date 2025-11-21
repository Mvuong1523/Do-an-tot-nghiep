# Sơ Đồ Kiến Trúc Hệ Thống - WEB_TMDT

## 1. Tổng Quan Kiến Trúc Hệ Thống

```mermaid
graph TB
    subgraph "Client Layer"
        WEB[Next.js Frontend<br/>Port: 3000]
    end
    
    subgraph "API Gateway"
        API[Spring Boot Backend<br/>Port: 8080]
        JWT[JWT Authentication]
        SWAGGER[Swagger API Docs]
    end
    
    subgraph "Business Logic Layer"
        AUTH[Auth Module]
        PRODUCT[Product Module]
        CART[Cart Module]
        ORDER[Order Module]
        PAYMENT[Payment Module]
        INVENTORY[Inventory Module]
        SHIPPING[Shipping Module]
    end
    
    subgraph "Data Layer"
        DB[(MySQL Database)]
        CACHE[Session Cache]
    end
    
    subgraph "External Services"
        SEPAY[SePay Payment Gateway]
        GHTK[GHTK Shipping Service]
        CLOUDINARY[Cloudinary Image Storage]
    end
    
    WEB -->|REST API| API
    API --> JWT
    API --> SWAGGER
    
    JWT --> AUTH
    API --> PRODUCT
    API --> CART
    API --> ORDER
    API --> PAYMENT
    API --> INVENTORY
    API --> SHIPPING
    
    AUTH --> DB
    PRODUCT --> DB
    CART --> DB
    ORDER --> DB
    PAYMENT --> DB
    INVENTORY --> DB
    
    AUTH --> CACHE
    
    PAYMENT -->|Webhook| SEPAY
    SHIPPING -->|API| GHTK
    PRODUCT -->|Upload| CLOUDINARY
    INVENTORY -->|Upload| CLOUDINARY
```

## 2. Kiến Trúc Module (Layered Architecture)

```mermaid
graph LR
    subgraph "Presentation Layer"
        CTRL[Controllers]
    end
    
    subgraph "Business Layer"
        SVC[Services]
        DTO[DTOs]
    end
    
    subgraph "Data Access Layer"
        REPO[Repositories]
        ENTITY[Entities]
    end
    
    subgraph "Cross-Cutting"
        SEC[Security<br/>JWT Filter]
        EXC[Exception Handler]
        CONFIG[Configuration]
    end
    
    CTRL --> SVC
    SVC --> DTO
    SVC --> REPO
    REPO --> ENTITY
    
    SEC -.-> CTRL
    EXC -.-> CTRL
    CONFIG -.-> SVC
```

## 3. Chi Tiết Các Module

### 3.1 Auth Module
- **Chức năng**: Xác thực, phân quyền, quản lý người dùng
- **Components**:
  - User Management (Customer, Employee)
  - Role-Based Access Control (ADMIN, PRODUCT_MANAGER, WAREHOUSE_MANAGER, CUSTOMER)
  - JWT Token Generation & Validation
  - OTP Verification
  - Employee Registration Approval

### 3.2 Product Module
- **Chức năng**: Quản lý sản phẩm và danh mục
- **Components**:
  - Category Management (Hierarchical)
  - Product CRUD
  - Product Publishing from Warehouse
  - Technical Specifications
  - Product Images

### 3.3 Cart Module
- **Chức năng**: Giỏ hàng
- **Components**:
  - Add/Remove/Update Cart Items
  - Cart Calculation
  - User-specific Cart

### 3.4 Order Module
- **Chức năng**: Quản lý đơn hàng
- **Components**:
  - Order Creation
  - Order Status Tracking
  - Order History
  - Order Cancellation

### 3.5 Payment Module
- **Chức năng**: Xử lý thanh toán
- **Components**:
  - SePay Integration
  - Payment QR Code Generation
  - Webhook Handler
  - Payment Status Tracking

### 3.6 Inventory Module
- **Chức năng**: Quản lý kho hàng
- **Components**:
  - Warehouse Product Management
  - Purchase Orders (PO)
  - Export Orders
  - Stock Management
  - Supplier Management
  - Product Serial Tracking

### 3.7 Shipping Module
- **Chức năng**: Tính phí vận chuyển
- **Components**:
  - GHTK Integration
  - Shipping Fee Calculation

## 4. Luồng Xử Lý Chính

### 4.1 Luồng Đăng Ký & Đăng Nhập

```mermaid
sequenceDiagram
    participant U as User
    participant FE as Frontend
    participant API as Backend API
    participant DB as Database
    
    U->>FE: Nhập thông tin đăng ký
    FE->>API: POST /api/auth/register
    API->>DB: Lưu User & Customer
    DB-->>API: Success
    API->>API: Gửi OTP qua email
    API-->>FE: Yêu cầu xác thực OTP
    
    U->>FE: Nhập OTP
    FE->>API: POST /api/auth/verify-otp
    API->>DB: Xác thực OTP
    DB-->>API: Valid
    API->>API: Generate JWT Token
    API-->>FE: Return Token
    FE-->>U: Đăng nhập thành công
```

### 4.2 Luồng Mua Hàng

```mermaid
sequenceDiagram
    participant C as Customer
    participant FE as Frontend
    participant API as Backend
    participant DB as Database
    participant PAY as SePay
    
    C->>FE: Thêm sản phẩm vào giỏ
    FE->>API: POST /api/cart/add
    API->>DB: Lưu CartItem
    
    C->>FE: Thanh toán
    FE->>API: POST /api/orders
    API->>DB: Tạo Order (PENDING)
    API->>API: Tạo Payment
    API->>PAY: Tạo QR thanh toán
    PAY-->>API: QR Code URL
    API-->>FE: Order + QR Code
    
    C->>C: Quét QR & chuyển khoản
    PAY->>API: Webhook notification
    API->>DB: Cập nhật Payment (PAID)
    API->>DB: Cập nhật Order (CONFIRMED)
    API-->>FE: Thông báo thành công
```

### 4.3 Luồng Quản Lý Kho

```mermaid
sequenceDiagram
    participant WM as Warehouse Manager
    participant FE as Frontend
    participant API as Backend
    participant DB as Database
    
    WM->>FE: Tạo Purchase Order
    FE->>API: POST /api/inventory/purchase-orders
    API->>DB: Lưu PO (CREATED)
    
    WM->>FE: Nhập hàng (Complete PO)
    FE->>API: POST /api/inventory/purchase-orders/{id}/complete
    API->>DB: Tạo ProductDetail (Serial)
    API->>DB: Cập nhật InventoryStock
    API->>DB: Cập nhật PO (RECEIVED)
    
    WM->>FE: Publish sản phẩm
    FE->>API: POST /api/products/publish
    API->>DB: Tạo Product từ WarehouseProduct
    API-->>FE: Sản phẩm đã publish
```

## 5. Bảo Mật

### 5.1 Authentication Flow
```mermaid
graph LR
    REQ[HTTP Request] --> FILTER[JWT Filter]
    FILTER --> VALIDATE{Valid Token?}
    VALIDATE -->|Yes| EXTRACT[Extract User Info]
    VALIDATE -->|No| REJECT[401 Unauthorized]
    EXTRACT --> CONTEXT[Security Context]
    CONTEXT --> CONTROLLER[Controller]
```

### 5.2 Authorization
- **Role-based Access Control (RBAC)**
  - ADMIN: Toàn quyền
  - PRODUCT_MANAGER: Quản lý sản phẩm, danh mục
  - WAREHOUSE_MANAGER: Quản lý kho, nhập/xuất hàng
  - CUSTOMER: Mua hàng, xem đơn hàng

## 6. Công Nghệ Sử Dụng

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: MySQL
- **ORM**: JPA/Hibernate
- **Security**: Spring Security + JWT
- **API Documentation**: Swagger/OpenAPI
- **Build Tool**: Maven

### Frontend
- **Framework**: Next.js 14 (React)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **State Management**: Zustand
- **HTTP Client**: Axios

### External Services
- **Payment**: SePay
- **Shipping**: GHTK
- **Image Storage**: Cloudinary

## 7. Deployment Architecture

```mermaid
graph TB
    subgraph "Production Environment"
        LB[Load Balancer]
        
        subgraph "Application Servers"
            APP1[Spring Boot Instance 1]
            APP2[Spring Boot Instance 2]
        end
        
        subgraph "Frontend Servers"
            FE1[Next.js Instance 1]
            FE2[Next.js Instance 2]
        end
        
        subgraph "Database Cluster"
            MASTER[(MySQL Master)]
            SLAVE[(MySQL Slave)]
        end
        
        CACHE_CLUSTER[Redis Cluster]
    end
    
    LB --> FE1
    LB --> FE2
    FE1 --> APP1
    FE2 --> APP2
    
    APP1 --> MASTER
    APP2 --> MASTER
    APP1 --> SLAVE
    APP2 --> SLAVE
    
    APP1 --> CACHE_CLUSTER
    APP2 --> CACHE_CLUSTER
    
    MASTER -.Replication.-> SLAVE
```

## 8. Monitoring & Logging

- **Application Logs**: SLF4J + Logback
- **Performance Monitoring**: Spring Boot Actuator
- **Error Tracking**: Exception Handler với logging
- **API Monitoring**: Swagger UI + Request/Response logging
