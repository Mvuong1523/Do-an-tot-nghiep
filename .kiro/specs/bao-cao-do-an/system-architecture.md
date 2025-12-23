# Sơ Đồ Kiến Trúc Hệ Thống - E-Commerce System

## Tổng Quan

Tài liệu này mô tả chi tiết kiến trúc hệ thống thương mại điện tử, bao gồm:
- Kiến trúc phân tầng (Layered Architecture)
- Sơ đồ thành phần và phụ thuộc (Component Diagram)
- Sơ đồ triển khai (Deployment Diagram)
- Trực quan hóa công nghệ (Technology Stack Visualization)

---

## 1. Kiến Trúc Phân Tầng (Layered Architecture)

### 1.1. Tổng Quan Kiến Trúc

Hệ thống được thiết kế theo mô hình kiến trúc phân tầng (Layered Architecture) với 4 tầng chính:

```mermaid
graph TB
    subgraph "Presentation Layer"
        UI[Next.js Frontend<br/>React Components<br/>TypeScript]
        Store[State Management<br/>Zustand Store]
    end
    
    subgraph "Application Layer"
        Controller[Controllers<br/>REST API Endpoints<br/>Request/Response Handling]
        Security[Security Layer<br/>JWT Authentication<br/>Authorization]
    end
    
    subgraph "Business Logic Layer"
        Service[Services<br/>Business Rules<br/>Transaction Management]
        EventListener[Event Listeners<br/>Async Processing]
    end
    
    subgraph "Data Access Layer"
        Repository[Repositories<br/>JPA/Hibernate<br/>Query Methods]
        Entity[Entities<br/>Domain Models]
    end
    
    subgraph "External Services"
        GHN[GHN API<br/>Shipping Service]
        SePay[SePay API<br/>Payment Gateway]
        Cloudinary[Cloudinary<br/>Image Storage]
        SMTP[SMTP Server<br/>Email Service]
    end
    
    subgraph "Data Storage"
        MySQL[(MySQL Database<br/>Persistent Storage)]
    end

    
    UI --> Store
    Store --> Controller
    Controller --> Security
    Security --> Service
    Service --> EventListener
    Service --> Repository
    Repository --> Entity
    Entity --> MySQL
    
    Service --> GHN
    Service --> SePay
    Service --> Cloudinary
    Service --> SMTP
    
    style UI fill:#e1f5ff
    style Controller fill:#fff4e1
    style Service fill:#e8f5e9
    style Repository fill:#f3e5f5
    style MySQL fill:#ffebee
```

### 1.2. Chi Tiết Từng Tầng

#### Tầng 1: Presentation Layer (Tầng Giao Diện)
**Công nghệ**: Next.js 14, React 18, TypeScript, Tailwind CSS

**Trách nhiệm**:
- Hiển thị giao diện người dùng
- Xử lý tương tác người dùng
- Quản lý state phía client (Zustand)
- Gọi API đến backend
- Routing và navigation

**Thành phần chính**:
- Pages: `/admin`, `/employee`, `/warehouse`, `/customer`
- Components: Reusable UI components
- Store: `authStore`, `cartStore`, `languageStore`
- API Client: Axios với interceptors

#### Tầng 2: Application Layer (Tầng Ứng Dụng)
**Công nghệ**: Spring Boot 3.5.6, Spring MVC, Spring Security

**Trách nhiệm**:
- Xử lý HTTP requests/responses
- Xác thực và phân quyền (JWT)
- Validation dữ liệu đầu vào
- Exception handling
- API documentation (Swagger)

**Thành phần chính**:
- Controllers: REST API endpoints
- Security: JWT filter, authentication
- DTOs: Request/Response objects
- Exception Handlers: Global error handling


#### Tầng 3: Business Logic Layer (Tầng Nghiệp Vụ)
**Công nghệ**: Spring Boot Services, Spring Events

**Trách nhiệm**:
- Xử lý logic nghiệp vụ
- Quản lý transactions
- Tích hợp với external services
- Event-driven processing
- Business rules validation

**Thành phần chính**:
- Services: Business logic implementation
- Event Listeners: Async event processing
- Validators: Business rules validation
- Mappers: Entity ↔ DTO conversion

#### Tầng 4: Data Access Layer (Tầng Truy Cập Dữ Liệu)
**Công nghệ**: Spring Data JPA, Hibernate, MySQL Connector

**Trách nhiệm**:
- Truy vấn và lưu trữ dữ liệu
- ORM mapping
- Transaction management
- Query optimization
- Database connection pooling

**Thành phần chính**:
- Repositories: JPA repositories
- Entities: Domain models
- Custom Queries: JPQL/Native SQL
- Database Configuration

---

## 2. Sơ Đồ Thành Phần và Phụ Thuộc (Component Diagram)

### 2.1. Module Dependencies - Backend

```mermaid
graph LR
    subgraph "Frontend"
        NextJS[Next.js Application]
    end
    
    subgraph "Backend Modules"
        Auth[Auth Module<br/>Authentication<br/>Authorization]
        Product[Product Module<br/>Catalog Management]
        Cart[Cart Module<br/>Shopping Cart]
        Order[Order Module<br/>Order Processing]
        Payment[Payment Module<br/>Payment Processing]
        Inventory[Inventory Module<br/>Warehouse Management]
        Shipping[Shipping Module<br/>GHN Integration]
        Accounting[Accounting Module<br/>Financial Records]
        Webhook[Webhook Module<br/>External Callbacks]
    end
    
    subgraph "Common Components"
        Security[Security<br/>JWT Service]
        Config[Configuration<br/>App Settings]
        Utils[Common Utils<br/>Helpers]
    end

    
    NextJS -->|REST API| Auth
    NextJS -->|REST API| Product
    NextJS -->|REST API| Cart
    NextJS -->|REST API| Order
    NextJS -->|REST API| Payment
    NextJS -->|REST API| Inventory
    NextJS -->|REST API| Shipping
    NextJS -->|REST API| Accounting
    
    Auth --> Security
    Auth --> Config
    
    Order --> Product
    Order --> Cart
    Order --> Payment
    Order --> Inventory
    Order --> Accounting
    
    Payment --> Order
    Payment --> Accounting
    Payment --> Webhook
    
    Shipping --> Order
    Shipping --> Webhook
    
    Inventory --> Product
    Inventory --> Accounting
    
    Accounting --> Order
    Accounting --> Payment
    Accounting --> Inventory
    
    Webhook --> Payment
    Webhook --> Shipping
    
    style Auth fill:#ffcdd2
    style Order fill:#c8e6c9
    style Payment fill:#fff9c4
    style Inventory fill:#b3e5fc
    style Accounting fill:#f8bbd0
```

### 2.2. Module Structure Detail

#### Auth Module
**Package**: `com.doan.WEB_TMDT.module.auth`

**Components**:
- `AuthController`: Login, register, logout
- `AuthService`: Authentication logic
- `UserService`: User management
- `EmployeeRegistrationService`: Employee approval
- Entities: `User`, `Customer`, `Employee`, `Position`, `Role`

**Dependencies**:
- Security (JWT)
- Email Service
- Database

#### Product Module
**Package**: `com.doan.WEB_TMDT.module.product`

**Components**:
- `ProductController`: CRUD operations
- `ProductService`: Business logic
- `CategoryService`: Category management
- Entities: `Product`, `Category`, `ProductImage`

**Dependencies**:
- Cloudinary (image upload)
- Inventory Module
- Database


#### Order Module
**Package**: `com.doan.WEB_TMDT.module.order`

**Components**:
- `OrderController`: Order management
- `OrderService`: Order processing
- Entities: `Order`, `OrderItem`, `OrderStatus`

**Dependencies**:
- Cart Module (checkout)
- Product Module (validation)
- Inventory Module (stock reservation)
- Payment Module (payment processing)
- Accounting Module (revenue recording)
- Database

#### Payment Module
**Package**: `com.doan.WEB_TMDT.module.payment`

**Components**:
- `PaymentController`: Payment operations
- `PaymentService`: Payment processing
- `BankAccountService`: Multi-account management
- Entities: `Payment`, `BankAccount`

**Dependencies**:
- SePay API
- Order Module
- Accounting Module
- Webhook Module
- Database

#### Inventory Module
**Package**: `com.doan.WEB_TMDT.module.inventory`

**Components**:
- `InventoryController`: Warehouse operations
- `InventoryService`: Stock management
- `ExportOrderService`: Export processing
- `PurchaseOrderService`: Import processing
- Entities: `WarehouseProduct`, `InventoryStock`, `ExportOrder`, `PurchaseOrder`, `Supplier`

**Dependencies**:
- Product Module
- Accounting Module (supplier payables)
- Excel Processing (Apache POI)
- Database

#### Shipping Module
**Package**: `com.doan.WEB_TMDT.module.shipping`

**Components**:
- `ShippingController`: Shipping operations
- `ShippingService`: GHN integration
- No entities (uses Order entity)

**Dependencies**:
- GHN API
- Order Module
- Webhook Module
- Database


#### Accounting Module
**Package**: `com.doan.WEB_TMDT.module.accounting`

**Components**:
- `AccountingController`: Financial operations
- `AccountingService`: Transaction recording
- `OrderEventListener`: Event-driven accounting
- Entities: `FinancialTransaction`, `SupplierPayable`, `SupplierPayment`, `PaymentReconciliation`, `AccountingPeriod`, `TaxReport`

**Dependencies**:
- Order Module (revenue)
- Payment Module (cash receipts)
- Inventory Module (supplier payables)
- Spring Events
- Database

#### Webhook Module
**Package**: `com.doan.WEB_TMDT.module.webhook`

**Components**:
- `WebhookController`: Webhook endpoints
- `WebhookService`: Webhook processing
- No entities

**Dependencies**:
- Payment Module (SePay webhooks)
- Shipping Module (GHN webhooks)
- Signature verification
- Database

---

## 3. Sơ Đồ Triển Khai (Deployment Diagram)

### 3.1. Production Deployment Architecture

```mermaid
graph TB
    subgraph "Client Devices"
        Browser[Web Browser<br/>Chrome, Firefox, Safari]
        Mobile[Mobile Browser<br/>iOS, Android]
    end
    
    subgraph "Internet"
        DNS[DNS Server<br/>Domain Resolution]
    end
    
    subgraph "VPS/Cloud Server - 103.90.227.154"
        subgraph "Nginx - Port 80/443"
            Nginx[Nginx Reverse Proxy<br/>SSL Termination<br/>Load Balancing]
        end
        
        subgraph "Frontend - Port 3000"
            NextApp[Next.js Application<br/>Node.js Runtime<br/>PM2 Process Manager]
        end
        
        subgraph "Backend - Port 8080"
            SpringApp[Spring Boot Application<br/>Java 21 Runtime<br/>Embedded Tomcat]
        end
        
        subgraph "Database - Port 3306"
            MySQL[(MySQL 8.x<br/>InnoDB Engine<br/>Persistent Storage)]
        end
    end

    
    subgraph "External Services"
        GHN[GHN API<br/>api.ghn.vn<br/>Shipping Service]
        SePay[SePay API<br/>my.sepay.vn<br/>Payment Monitoring]
        Cloudinary[Cloudinary CDN<br/>res.cloudinary.com<br/>Image Storage]
        SMTP[SMTP Server<br/>smtp.gmail.com<br/>Email Service]
    end
    
    Browser -->|HTTPS| DNS
    Mobile -->|HTTPS| DNS
    DNS -->|Resolve| Nginx
    
    Nginx -->|Proxy Pass| NextApp
    Nginx -->|Proxy Pass /api| SpringApp
    
    NextApp -->|HTTP| SpringApp
    SpringApp -->|JDBC| MySQL
    
    SpringApp -->|REST API| GHN
    SpringApp -->|REST API| SePay
    SpringApp -->|SDK| Cloudinary
    SpringApp -->|SMTP| SMTP
    
    GHN -.Webhook.-> Nginx
    SePay -.Webhook.-> Nginx
    Nginx -.Webhook.-> SpringApp
    
    style Nginx fill:#90caf9
    style NextApp fill:#a5d6a7
    style SpringApp fill:#ffcc80
    style MySQL fill:#ef9a9a
    style GHN fill:#ce93d8
    style SePay fill:#fff59d
    style Cloudinary fill:#80deea
```

### 3.2. Deployment Details

#### Server Configuration
- **Server**: VPS/Cloud Server
- **IP**: 103.90.227.154
- **OS**: Linux (Ubuntu/CentOS)
- **RAM**: 4GB+ recommended
- **Storage**: 50GB+ SSD

#### Port Mapping
| Service | Internal Port | External Port | Protocol |
|---------|--------------|---------------|----------|
| Nginx | - | 80, 443 | HTTP/HTTPS |
| Next.js | 3000 | - | HTTP |
| Spring Boot | 8080 | - | HTTP |
| MySQL | 3306 | - | TCP |

#### URL Structure
- **Frontend**: `https://hoanghamobile.com`
- **Backend API**: `https://hoanghamobile.com/api`
- **Swagger UI**: `https://hoanghamobile.com/api/swagger-ui.html`
- **Admin Panel**: `https://hoanghamobile.com/admin`
- **Employee Portal**: `https://hoanghamobile.com/employee`


#### Nginx Configuration
```nginx
server {
    listen 80;
    server_name hoanghamobile.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name hoanghamobile.com;
    
    ssl_certificate /etc/ssl/certs/cert.pem;
    ssl_certificate_key /etc/ssl/private/key.pem;
    
    # Frontend
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
    
    # Backend API
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### Process Management (PM2)
```bash
# Frontend
pm2 start npm --name "frontend" -- start
pm2 startup
pm2 save

# Backend (alternative to systemd)
pm2 start "java -jar target/WEB_TMDT-0.0.1-SNAPSHOT.jar" --name "backend"
```

---

## 4. Trực Quan Hóa Công Nghệ (Technology Stack Visualization)

### 4.1. Full Stack Overview

```mermaid
graph TB
    subgraph "Frontend Stack"
        FE1[Next.js 14.2.33<br/>React Framework]
        FE2[React 18.2.0<br/>UI Library]
        FE3[TypeScript 5.2.2<br/>Type Safety]
        FE4[Tailwind CSS 3.3.5<br/>Styling]
        FE5[Zustand 4.4.7<br/>State Management]
        FE6[Axios 1.6.0<br/>HTTP Client]
    end
    
    subgraph "Backend Stack"
        BE1[Spring Boot 3.5.6<br/>Framework]
        BE2[Java 21 LTS<br/>Language]
        BE3[Spring Security 6.5.5<br/>Authentication]
        BE4[Spring Data JPA<br/>ORM]
        BE5[JWT 0.11.5<br/>Token Auth]
        BE6[Lombok 1.18.32<br/>Code Generation]
    end

    
    subgraph "Database"
        DB1[(MySQL 8.x<br/>Relational DB)]
        DB2[Hibernate<br/>ORM Engine]
        DB3[HikariCP<br/>Connection Pool]
    end
    
    subgraph "External Services"
        EXT1[GHN API<br/>Shipping]
        EXT2[SePay API<br/>Payment]
        EXT3[Cloudinary<br/>Images]
        EXT4[SMTP<br/>Email]
    end
    
    subgraph "Build Tools"
        BUILD1[Maven 3.x<br/>Backend Build]
        BUILD2[npm/Node.js<br/>Frontend Build]
        BUILD3[Git<br/>Version Control]
    end
    
    subgraph "Development Tools"
        DEV1[Swagger/OpenAPI<br/>API Docs]
        DEV2[Postman<br/>API Testing]
        DEV3[MySQL Workbench<br/>DB Management]
    end
    
    FE1 --> FE2
    FE2 --> FE3
    FE3 --> FE4
    FE2 --> FE5
    FE2 --> FE6
    
    BE1 --> BE2
    BE1 --> BE3
    BE1 --> BE4
    BE3 --> BE5
    BE2 --> BE6
    
    BE4 --> DB2
    DB2 --> DB1
    DB2 --> DB3
    
    BE1 --> EXT1
    BE1 --> EXT2
    BE1 --> EXT3
    BE1 --> EXT4
    
    style FE1 fill:#61dafb
    style BE1 fill:#6db33f
    style DB1 fill:#00758f
    style EXT1 fill:#ff6b6b
```

### 4.2. Technology Stack Summary

#### Frontend Technologies
| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Framework** | Next.js | 14.2.33 | React framework with SSR |
| **UI Library** | React | 18.2.0 | Component-based UI |
| **Language** | TypeScript | 5.2.2 | Type-safe JavaScript |
| **Styling** | Tailwind CSS | 3.3.5 | Utility-first CSS |
| **State** | Zustand | 4.4.7 | Lightweight state management |
| **HTTP** | Axios | 1.6.0 | Promise-based HTTP client |
| **Icons** | React Icons | 4.12.0 | Icon library |
| **Carousel** | Swiper | 11.0.0 | Touch slider |
| **Toast** | Sonner | 2.0.7 | Notifications |
| **Excel** | XLSX | 0.18.5 | Excel file handling |
| **QR Code** | html5-qrcode | 2.3.8 | QR scanning |
| **Print** | react-to-print | 3.2.0 | Print functionality |


#### Backend Technologies
| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Framework** | Spring Boot | 3.5.6 | Java application framework |
| **Language** | Java | 21 LTS | Programming language |
| **Security** | Spring Security | 6.5.5 | Authentication & authorization |
| **JWT** | JJWT | 0.11.5 | JSON Web Token |
| **ORM** | Spring Data JPA | 3.5.6 | Database abstraction |
| **ORM Engine** | Hibernate | 6.x | JPA implementation |
| **Validation** | Bean Validation | 3.0 | Input validation |
| **Code Gen** | Lombok | 1.18.32 | Boilerplate reduction |
| **API Docs** | SpringDoc OpenAPI | 2.5.0 | Swagger UI |
| **Excel** | Apache POI | 5.2.5 | Excel processing |
| **JSON** | org.json | 20240303 | JSON manipulation |
| **Images** | Cloudinary SDK | 1.36.0 | Image upload |
| **Email** | Spring Mail | 3.5.6 | Email sending |
| **Build** | Maven | 3.x | Dependency management |

#### Database Technologies
| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **RDBMS** | MySQL | 8.x | Primary database |
| **Engine** | InnoDB | - | Storage engine |
| **Connector** | MySQL Connector/J | Runtime | JDBC driver |
| **Pool** | HikariCP | - | Connection pooling |
| **Charset** | utf8mb4 | - | Unicode support |

#### External Services
| Service | Purpose | Integration |
|---------|---------|-------------|
| **GHN** | Shipping & logistics | REST API + Webhook |
| **SePay** | Payment monitoring | REST API + Webhook |
| **Cloudinary** | Image storage & CDN | SDK |
| **SMTP** | Email notifications | JavaMailSender |

---

## 5. Data Flow Architecture

### 5.1. Request-Response Flow

```mermaid
sequenceDiagram
    participant User as User/Browser
    participant Nginx as Nginx
    participant Next as Next.js
    participant Spring as Spring Boot
    participant DB as MySQL
    participant Ext as External API
    
    User->>Nginx: HTTPS Request
    activate Nginx
    
    alt Static Content
        Nginx->>Next: Forward to :3000
        activate Next
        Next-->>Nginx: HTML/CSS/JS
        deactivate Next
        Nginx-->>User: Response
    else API Request
        Nginx->>Spring: Forward to :8080/api
        activate Spring
        
        Spring->>Spring: JWT Validation
        Spring->>Spring: Authorization Check
        
        alt Database Operation
            Spring->>DB: SQL Query
            activate DB
            DB-->>Spring: Result Set
            deactivate DB
        end
        
        alt External Service Call
            Spring->>Ext: REST API Call
            activate Ext
            Ext-->>Spring: API Response
            deactivate Ext
        end
        
        Spring-->>Nginx: JSON Response
        deactivate Spring
        Nginx-->>User: Response
    end
    
    deactivate Nginx
```


### 5.2. Authentication Flow

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Controller
    participant AuthService
    participant JwtService
    participant UserRepo
    participant DB
    
    User->>Frontend: Enter credentials
    Frontend->>Controller: POST /api/auth/login
    activate Controller
    
    Controller->>AuthService: authenticate(email, password)
    activate AuthService
    
    AuthService->>UserRepo: findByEmail(email)
    activate UserRepo
    UserRepo->>DB: SELECT * FROM users
    DB-->>UserRepo: User data
    UserRepo-->>AuthService: User entity
    deactivate UserRepo
    
    AuthService->>AuthService: BCrypt.matches(password)
    
    alt Invalid Credentials
        AuthService-->>Controller: AuthenticationException
        Controller-->>Frontend: 401 Unauthorized
        Frontend-->>User: "Invalid credentials"
    else Valid Credentials
        AuthService->>JwtService: generateToken(user)
        activate JwtService
        JwtService-->>AuthService: JWT token
        deactivate JwtService
        
        AuthService-->>Controller: LoginResponse(token, user)
        deactivate AuthService
        Controller-->>Frontend: 200 OK + JWT
        deactivate Controller
        
        Frontend->>Frontend: Store token in localStorage
        Frontend-->>User: Redirect to dashboard
    end
```

### 5.3. Order Processing Flow

```mermaid
graph TB
    Start([Customer Checkout]) --> ValidateCart[Validate Cart Items]
    ValidateCart --> CheckStock{Stock Available?}
    
    CheckStock -->|No| StockError[Return Error:<br/>Insufficient Stock]
    CheckStock -->|Yes| ReserveStock[Reserve Stock<br/>in Inventory]
    
    ReserveStock --> CreateOrder[Create Order<br/>Status: PENDING_PAYMENT]
    
    CreateOrder --> PaymentMethod{Payment Method?}
    
    PaymentMethod -->|COD| ConfirmOrder[Update Status:<br/>CONFIRMED]
    PaymentMethod -->|Online| GenerateQR[Generate QR Code<br/>via SePay]
    
    GenerateQR --> WaitPayment[Wait for Payment<br/>15 min timeout]
    
    WaitPayment --> PaymentReceived{Payment Received?}
    PaymentReceived -->|Yes| ConfirmOrder
    PaymentReceived -->|No| CancelOrder[Cancel Order<br/>Release Stock]
    
    ConfirmOrder --> WarehouseExport[Warehouse Creates<br/>Export Order]
    WarehouseExport --> ReadyToShip[Status:<br/>READY_TO_SHIP]
    
    ReadyToShip --> CreateGHN[Create GHN<br/>Shipping Order]
    CreateGHN --> Shipping[Status:<br/>SHIPPING]
    
    Shipping --> GHNWebhook[GHN Webhook:<br/>Delivery Status]
    GHNWebhook --> Delivered[Status:<br/>DELIVERED]
    
    Delivered --> RecordRevenue[Accounting:<br/>Record Revenue]
    RecordRevenue --> End([Order Complete])
    
    CancelOrder --> End
    StockError --> End
    
    style Start fill:#c8e6c9
    style End fill:#ffcdd2
    style ConfirmOrder fill:#fff9c4
    style Delivered fill:#b3e5fc
```


---

## 6. Security Architecture

### 6.1. Security Layers

```mermaid
graph TB
    subgraph "Network Security"
        HTTPS[HTTPS/TLS<br/>SSL Certificate]
        Firewall[Firewall Rules<br/>Port Restrictions]
    end
    
    subgraph "Application Security"
        JWT[JWT Authentication<br/>Token-based Auth]
        RBAC[Role-Based Access Control<br/>ADMIN/EMPLOYEE/CUSTOMER]
        PBAC[Position-Based Access<br/>SALES/WAREHOUSE/ACCOUNTANT]
        CORS[CORS Configuration<br/>Origin Restrictions]
    end
    
    subgraph "Data Security"
        BCrypt[BCrypt Password Hashing<br/>Salt + Hash]
        Validation[Input Validation<br/>Bean Validation]
        SQLInjection[SQL Injection Prevention<br/>Prepared Statements]
        XSS[XSS Protection<br/>Output Encoding]
    end
    
    subgraph "API Security"
        RateLimit[Rate Limiting<br/>Request Throttling]
        Signature[Webhook Signature<br/>Verification]
        APIKey[API Key Management<br/>External Services]
    end
    
    HTTPS --> JWT
    Firewall --> JWT
    JWT --> RBAC
    RBAC --> PBAC
    JWT --> CORS
    
    PBAC --> BCrypt
    PBAC --> Validation
    Validation --> SQLInjection
    Validation --> XSS
    
    CORS --> RateLimit
    RateLimit --> Signature
    Signature --> APIKey
    
    style HTTPS fill:#ffcdd2
    style JWT fill:#c8e6c9
    style BCrypt fill:#fff9c4
    style RateLimit fill:#b3e5fc
```

### 6.2. Authentication & Authorization Matrix

| Role | Position | Access Level | Permissions |
|------|----------|--------------|-------------|
| **CUSTOMER** | - | Public | Browse products, Place orders, View own orders |
| **EMPLOYEE** | SALES | Staff | Confirm orders, View customer info |
| **EMPLOYEE** | WAREHOUSE | Staff | Manage inventory, Create export/import orders |
| **EMPLOYEE** | SHIPPER | Staff | View shipping orders, Update delivery status |
| **EMPLOYEE** | ACCOUNTANT | Staff | View financial reports, Manage payables |
| **EMPLOYEE** | PRODUCT_MANAGER | Staff | Manage products, categories |
| **ADMIN** | - | Full | All permissions, User management, System config |

---

## 7. Scalability & Performance

### 7.1. Current Architecture Characteristics

**Scalability Type**: Vertical Scaling (Single Server)

**Performance Optimizations**:
- Database connection pooling (HikariCP)
- JPA lazy loading
- Database indexing on frequently queried columns
- CDN for static assets (Cloudinary)
- Next.js SSR for faster initial page load
- Nginx caching for static content

### 7.2. Future Scaling Options

```mermaid
graph TB
    subgraph "Phase 1: Current (Monolithic)"
        LB1[Single Server<br/>All Services]
    end
    
    subgraph "Phase 2: Horizontal Scaling"
        LB2[Load Balancer<br/>Nginx]
        App1[App Server 1]
        App2[App Server 2]
        App3[App Server 3]
        Redis[Redis<br/>Session Store]
        
        LB2 --> App1
        LB2 --> App2
        LB2 --> App3
        App1 --> Redis
        App2 --> Redis
        App3 --> Redis
    end

    
    subgraph "Phase 3: Microservices"
        Gateway[API Gateway]
        OrderSvc[Order Service]
        PaymentSvc[Payment Service]
        InventorySvc[Inventory Service]
        ShippingSvc[Shipping Service]
        
        Gateway --> OrderSvc
        Gateway --> PaymentSvc
        Gateway --> InventorySvc
        Gateway --> ShippingSvc
    end
    
    subgraph "Phase 4: Database Scaling"
        Master[(Master DB<br/>Write)]
        Slave1[(Slave DB 1<br/>Read)]
        Slave2[(Slave DB 2<br/>Read)]
        
        Master -.Replication.-> Slave1
        Master -.Replication.-> Slave2
    end
    
    style LB1 fill:#ffcdd2
    style LB2 fill:#c8e6c9
    style Gateway fill:#fff9c4
    style Master fill:#b3e5fc
```

---

## 8. Monitoring & Logging

### 8.1. Logging Strategy

```mermaid
graph LR
    subgraph "Application Logs"
        SpringLog[Spring Boot Logs<br/>Logback]
        NextLog[Next.js Logs<br/>Console]
    end
    
    subgraph "Server Logs"
        NginxLog[Nginx Access/Error Logs]
        SystemLog[System Logs<br/>syslog]
    end
    
    subgraph "Database Logs"
        MySQLLog[MySQL Query Logs<br/>Slow Query Log]
    end
    
    subgraph "External Service Logs"
        GHNLog[GHN API Logs]
        SePayLog[SePay Webhook Logs]
    end
    
    SpringLog --> LogFile[Log Files<br/>/var/log/app]
    NextLog --> LogFile
    NginxLog --> LogFile
    SystemLog --> LogFile
    MySQLLog --> LogFile
    
    GHNLog --> SpringLog
    SePayLog --> SpringLog
    
    LogFile --> Analysis[Log Analysis<br/>grep, awk, tail]
    
    style LogFile fill:#fff9c4
```

### 8.2. Monitoring Metrics

**Application Metrics**:
- Request count and response time
- Error rate and exception tracking
- Database query performance
- Memory and CPU usage
- Thread pool utilization

**Business Metrics**:
- Order creation rate
- Payment success rate
- Inventory stock levels
- Revenue and profit
- Customer registration rate

---

## 9. Backup & Disaster Recovery

### 9.1. Backup Strategy

```mermaid
graph TB
    subgraph "Database Backup"
        Daily[Daily Full Backup<br/>mysqldump]
        Hourly[Hourly Incremental<br/>Binary Logs]
    end
    
    subgraph "Application Backup"
        Code[Code Repository<br/>Git]
        Config[Configuration Files<br/>application.properties]
    end
    
    subgraph "Storage"
        Local[Local Storage<br/>/backup]
        Cloud[Cloud Storage<br/>S3/Google Drive]
    end
    
    Daily --> Local
    Hourly --> Local
    Code --> Cloud
    Config --> Local
    
    Local -.Sync.-> Cloud
    
    style Daily fill:#c8e6c9
    style Cloud fill:#b3e5fc
```

### 9.2. Recovery Procedures

**Database Recovery**:
1. Stop application services
2. Restore from latest full backup
3. Apply incremental backups
4. Verify data integrity
5. Restart services

**Application Recovery**:
1. Pull latest code from Git
2. Restore configuration files
3. Rebuild application (Maven/npm)
4. Deploy to server
5. Verify functionality

---

## 10. Development Workflow

### 10.1. Development Process

```mermaid
graph LR
    Dev[Developer] -->|Code| Git[Git Repository]
    Git -->|Pull| Local[Local Development]
    Local -->|Test| LocalTest[Local Testing]
    LocalTest -->|Push| Git
    Git -->|Deploy| Server[Production Server]
    Server -->|Monitor| Logs[Logs & Metrics]
    Logs -->|Feedback| Dev
    
    style Dev fill:#c8e6c9
    style Git fill:#fff9c4
    style Server fill:#ffcdd2
```

### 10.2. Build & Deploy Process

**Backend Build**:
```bash
# Clean and build
mvn clean package -DskipTests

# Run locally
mvn spring-boot:run

# Deploy to server
scp target/WEB_TMDT-0.0.1-SNAPSHOT.jar user@server:/app/
ssh user@server "systemctl restart backend"
```

**Frontend Build**:
```bash
# Install dependencies
npm install

# Build for production
npm run build

# Deploy to server
rsync -avz .next/ user@server:/app/frontend/.next/
ssh user@server "pm2 restart frontend"
```

---

## 11. API Documentation

### 11.1. Swagger/OpenAPI Integration

**Access URL**: `http://localhost:8080/swagger-ui.html`

**Features**:
- Interactive API documentation
- Try-it-out functionality
- Request/response examples
- Authentication testing
- Schema definitions

### 11.2. API Endpoint Categories

| Category | Base Path | Description |
|----------|-----------|-------------|
| **Auth** | `/api/auth` | Authentication & registration |
| **Products** | `/api/products` | Product catalog |
| **Cart** | `/api/cart` | Shopping cart operations |
| **Orders** | `/api/orders` | Order management |
| **Payments** | `/api/payments` | Payment processing |
| **Inventory** | `/api/inventory` | Warehouse operations |
| **Shipping** | `/api/shipping` | Shipping & GHN |
| **Accounting** | `/api/accounting` | Financial operations |
| **Webhooks** | `/api/webhook` | External callbacks |

---

## 12. Kết Luận

### 12.1. Điểm Mạnh Của Kiến Trúc

✅ **Phân tầng rõ ràng**: Dễ bảo trì và mở rộng
✅ **Công nghệ hiện đại**: Spring Boot 3.5.6, Next.js 14, Java 21
✅ **Bảo mật tốt**: JWT, BCrypt, RBAC, HTTPS
✅ **Tích hợp linh hoạt**: GHN, SePay, Cloudinary
✅ **Event-driven**: Kế toán tự động qua Spring Events
✅ **RESTful API**: Chuẩn REST, dễ tích hợp
✅ **Documentation**: Swagger UI tự động

### 12.2. Hạn Chế Hiện Tại

⚠️ **Single server**: Chưa có load balancing
⚠️ **No caching**: Chưa sử dụng Redis
⚠️ **Monolithic**: Chưa tách microservices
⚠️ **Manual deployment**: Chưa có CI/CD pipeline
⚠️ **Limited monitoring**: Chưa có monitoring tool chuyên dụng

### 12.3. Đề Xuất Cải Tiến

🔄 **Short-term** (1-3 tháng):
- Implement Redis caching
- Add monitoring tools (Prometheus, Grafana)
- Setup CI/CD pipeline (Jenkins, GitHub Actions)
- Implement rate limiting
- Add comprehensive logging

🔄 **Mid-term** (3-6 tháng):
- Horizontal scaling with load balancer
- Database read replicas
- CDN for frontend assets
- Automated backup system
- Performance optimization

🔄 **Long-term** (6-12 tháng):
- Migrate to microservices architecture
- Implement message queue (RabbitMQ, Kafka)
- Container orchestration (Docker, Kubernetes)
- Multi-region deployment
- Advanced analytics and reporting

---

**Tài liệu này mô tả đầy đủ kiến trúc hệ thống thương mại điện tử, từ tầng giao diện đến tầng dữ liệu, bao gồm cả tích hợp với các dịch vụ bên ngoài và chiến lược triển khai.**

**Ngày tạo**: 2024
**Phiên bản**: 1.0
**Trạng thái**: Production Ready ✅
