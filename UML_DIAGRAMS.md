# Sơ Đồ UML Chuẩn - WEB_TMDT

## Hướng Dẫn Sử Dụng

Các sơ đồ dưới đây sử dụng **PlantUML syntax**. Để render:
1. Sử dụng PlantUML plugin trong IDE (IntelliJ, VSCode)
2. Truy cập [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/)
3. Sử dụng PlantUML CLI tool

---

## 1. Class Diagram - Auth Module

```plantuml
@startuml Auth_Module_Class_Diagram

!define ENTITY class
!define ENUM enum

package "com.doan.WEB_TMDT.module.auth.entity" {
    
    ENTITY User {
        - Long id
        - String email
        - String password
        - Role role
        - Status status
        --
        + getId(): Long
        + getEmail(): String
        + getRole(): Role
    }
    
    ENTITY Customer {
        - Long id
        - String fullName
        - String phone
        - String gender
        - LocalDate birthDate
        - String address
        --
        + getFullName(): String
        + getPhone(): String
    }
    
    ENTITY Employee {
        - Long id
        - Position position
        - String fullName
        - String phone
        - String address
        - boolean firstLogin
        --
        + getPosition(): Position
        + isFirstLogin(): boolean
    }
    
    ENUM Role {
        ADMIN
        PRODUCT_MANAGER
        WAREHOUSE_MANAGER
        CUSTOMER
    }
    
    ENUM Status {
        ACTIVE
        INACTIVE
        BANNED
    }
    
    ENUM Position {
        ADMIN
        PRODUCT_MANAGER
        WAREHOUSE_MANAGER
    }
    
    ENTITY OtpVerification {
        - Long id
        - String email
        - String otpCode
        - LocalDateTime createdAt
        - LocalDateTime expiresAt
        - boolean verified
    }
    
    ENTITY EmployeeRegistration {
        - Long id
        - String email
        - String fullName
        - String phone
        - Position position
        - RegistrationStatus status
        - LocalDateTime createdAt
        - Long approvedBy
        - LocalDateTime approvedAt
    }
}

User "1" -- "1" Customer : has >
User "1" -- "1" Employee : has >
User --> Role : uses
User --> Status : has
Employee --> Position : has
User "1" -- "0..*" OtpVerification : verifies >
Employee "1" -- "0..*" EmployeeRegistration : approved from >

@enduml
```

## 2. Class Diagram - Product & Inventory Module

```plantuml
@startuml Product_Inventory_Class_Diagram

package "com.doan.WEB_TMDT.module.product.entity" {
    
    class Category {
        - Long id
        - String name
        - String slug
        - String description
        - String imageUrl
        - Integer displayOrder
        - Boolean active
        --
        + getProductCount(): int
        + getChildren(): List<Category>
    }
    
    class Product {
        - Long id
        - String name
        - Double price
        - String sku
        - String description
        - String imageUrl
        - Long stockQuantity
        - String techSpecsJson
        --
        + getPrice(): Double
        + getStockQuantity(): Long
    }
}

package "com.doan.WEB_TMDT.module.inventory.entity" {
    
    class WarehouseProduct {
        - Long id
        - String sku
        - String internalName
        - String techSpecsJson
        - String description
        - LocalDateTime lastImportDate
        --
        + getQuantityInStock(): long
    }
    
    class ProductDetail {
        - Long id
        - String serialNumber
        - ProductStatus status
        - LocalDateTime importDate
        - Double importPrice
        - String notes
        --
        + getStatus(): ProductStatus
        + getSerialNumber(): String
    }
    
    class InventoryStock {
        - Long id
        - Long onHand
        - Long reserved
        - Long damaged
        - LocalDate lastAuditDate
        --
        + getSellable(): Long
        + getAvailable(): Long
    }
    
    class Supplier {
        - Long id
        - String name
        - String contactName
        - String phone
        - String email
        - String address
        - String taxCode
        - String bankAccount
        - Boolean active
    }
    
    class ProductSpecification {
        - Long id
        - String specKey
        - String specValue
    }
    
    class WarehouseProductImage {
        - Long id
        - String imageUrl
        - Integer displayOrder
    }
    
    enum ProductStatus {
        IN_STOCK
        RESERVED
        SOLD
        DAMAGED
        RETURNED
    }
}

Category "1" -- "0..*" Category : parent-child
Category "1" -- "0..*" Product : contains >
Product "1" -- "1" WarehouseProduct : published from >
WarehouseProduct "1" -- "0..*" ProductDetail : has serials >
WarehouseProduct "1" -- "1" InventoryStock : has stock >
WarehouseProduct "1" -- "0..*" ProductSpecification : has specs >
WarehouseProduct "1" -- "0..*" WarehouseProductImage : has images >
WarehouseProduct "0..*" -- "1" Supplier : supplied by >
ProductDetail --> ProductStatus : has

@enduml
```

## 3. Class Diagram - Order & Payment Module

```plantuml
@startuml Order_Payment_Class_Diagram

package "com.doan.WEB_TMDT.module.order.entity" {
    
    class Order {
        - Long id
        - String orderCode
        - String customerName
        - String customerPhone
        - String customerEmail
        - String shippingAddress
        - String note
        - Double subtotal
        - Double shippingFee
        - Double discount
        - Double total
        - PaymentStatus paymentStatus
        - OrderStatus status
        - LocalDateTime createdAt
        - LocalDateTime confirmedAt
        - LocalDateTime shippedAt
        - LocalDateTime deliveredAt
        - LocalDateTime cancelledAt
        - String cancelReason
        --
        + getTotal(): Double
        + getStatus(): OrderStatus
    }
    
    class OrderItem {
        - Long id
        - String productName
        - String productSku
        - Integer quantity
        - Double price
        - Double subtotal
        --
        + getSubtotal(): Double
    }
    
    enum OrderStatus {
        PENDING
        CONFIRMED
        PROCESSING
        SHIPPING
        DELIVERED
        CANCELLED
    }
    
    enum PaymentStatus {
        UNPAID
        PENDING
        PAID
        FAILED
        EXPIRED
        REFUNDED
    }
}

package "com.doan.WEB_TMDT.module.payment.entity" {
    
    class Payment {
        - Long id
        - String paymentCode
        - Double amount
        - PaymentMethod method
        - PaymentStatus status
        - String sepayTransactionId
        - String sepayBankCode
        - String sepayAccountNumber
        - String sepayAccountName
        - String sepayContent
        - String sepayQrCode
        - String sepayResponse
        - LocalDateTime createdAt
        - LocalDateTime paidAt
        - LocalDateTime expiredAt
        - String failureReason
        --
        + getAmount(): Double
        + getStatus(): PaymentStatus
    }
    
    enum PaymentMethod {
        SEPAY
        COD
        BANK_TRANSFER
    }
}

package "com.doan.WEB_TMDT.module.cart.entity" {
    
    class Cart {
        - Long id
        - LocalDateTime createdAt
        - LocalDateTime updatedAt
        --
        + getTotalItems(): int
        + getSubtotal(): Double
        + addItem(CartItem): void
        + removeItem(CartItem): void
        + clearItems(): void
    }
    
    class CartItem {
        - Long id
        - Integer quantity
        - Double price
        - LocalDateTime addedAt
        --
        + getQuantity(): Integer
        + getPrice(): Double
    }
}

Order "1" -- "0..*" OrderItem : contains >
Order --> OrderStatus : has
Order --> PaymentStatus : has
Order "1" -- "0..1" Payment : paid by >
Payment --> PaymentMethod : uses
Payment --> PaymentStatus : has
Cart "1" -- "0..*" CartItem : contains >

@enduml
```


## 4. Sequence Diagram - User Login Flow

```plantuml
@startuml User_Login_Sequence

actor User
participant "Frontend" as FE
participant "AuthController" as Ctrl
participant "AuthService" as Svc
participant "JwtService" as JWT
database "Database" as DB

User -> FE: Nhập email & password
activate FE

FE -> Ctrl: POST /api/auth/login\n{email, password}
activate Ctrl

Ctrl -> Svc: login(email, password)
activate Svc

Svc -> DB: SELECT * FROM users\nWHERE email = ?
activate DB
DB --> Svc: User data
deactivate DB

Svc -> Svc: Verify password\n(BCrypt)

alt Password correct
    Svc -> JWT: generateToken(user)
    activate JWT
    JWT --> Svc: accessToken, refreshToken
    deactivate JWT
    
    Svc -> DB: Save refresh token
    activate DB
    DB --> Svc: OK
    deactivate DB
    
    Svc --> Ctrl: LoginResponse\n{token, user}
    deactivate Svc
    
    Ctrl --> FE: 200 OK\n{token, user}
    deactivate Ctrl
    
    FE -> FE: Store token in\nlocalStorage
    FE --> User: Redirect to dashboard
    deactivate FE
    
else Password incorrect
    Svc --> Ctrl: AuthenticationException
    deactivate Svc
    Ctrl --> FE: 401 Unauthorized
    deactivate Ctrl
    FE --> User: Show error message
    deactivate FE
end

@enduml
```

## 5. Sequence Diagram - Order & Payment Flow

```plantuml
@startuml Order_Payment_Sequence

actor Customer
participant "Frontend" as FE
participant "OrderController" as OCtrl
participant "OrderService" as OSvc
participant "PaymentService" as PSvc
participant "SePay API" as SePay
participant "InventoryService" as ISvc
database "Database" as DB

Customer -> FE: Click "Đặt hàng"
activate FE

FE -> OCtrl: POST /api/orders\n{cartItems, shippingInfo}
activate OCtrl

OCtrl -> OSvc: createOrder(request)
activate OSvc

OSvc -> DB: INSERT INTO orders\nstatus='PENDING'
activate DB
DB --> OSvc: Order created
deactivate DB

OSvc -> PSvc: createPayment(order)
activate PSvc

PSvc -> DB: INSERT INTO payments\nstatus='PENDING'
activate DB
DB --> PSvc: Payment created
deactivate DB

PSvc -> SePay: POST /create-payment\n{amount, orderCode}
activate SePay
SePay --> PSvc: {qrCode, transactionId}
deactivate SePay

PSvc -> DB: UPDATE payments\nSET sepay_qr_code = ?
activate DB
DB --> PSvc: OK
deactivate DB

PSvc --> OSvc: PaymentResponse\n{qrCode}
deactivate PSvc

OSvc --> OCtrl: OrderResponse\n{order, payment}
deactivate OSvc

OCtrl --> FE: 200 OK\n{order, qrCode}
deactivate OCtrl

FE --> Customer: Hiển thị QR Code
deactivate FE

... Khách hàng quét QR và chuyển khoản ...

SePay -> PSvc: POST /webhook\n{transactionId, status}
activate PSvc

PSvc -> DB: UPDATE payments\nSET status='PAID'
activate DB
DB --> PSvc: OK
deactivate DB

PSvc -> OSvc: confirmOrder(orderId)
activate OSvc

OSvc -> DB: UPDATE orders\nSET status='CONFIRMED'
activate DB
DB --> OSvc: OK
deactivate DB

OSvc -> ISvc: reserveStock(orderItems)
activate ISvc

loop For each item
    ISvc -> DB: UPDATE inventory_stock\nSET reserved = reserved + ?
    activate DB
    DB --> ISvc: OK
    deactivate DB
end

ISvc --> OSvc: Stock reserved
deactivate ISvc

OSvc --> PSvc: Order confirmed
deactivate OSvc

PSvc --> SePay: 200 OK
deactivate PSvc

@enduml
```

## 6. Sequence Diagram - Complete Purchase Order

```plantuml
@startuml Complete_PO_Sequence

actor "Warehouse Manager" as WM
participant "Frontend" as FE
participant "InventoryController" as Ctrl
participant "InventoryService" as Svc
database "Database" as DB

WM -> FE: Click "Complete PO"
activate FE

FE -> FE: Nhập serial numbers

FE -> Ctrl: POST /api/inventory/purchase-orders/{id}/complete\n{serialNumbers}
activate Ctrl

Ctrl -> Svc: completePurchaseOrder(poId, serials)
activate Svc

Svc -> DB: SELECT * FROM purchase_orders\nWHERE id = ?
activate DB
DB --> Svc: PO with items
deactivate DB

alt PO status = CREATED
    
    loop For each PO item
        Svc -> DB: Get warehouse_product_id
        activate DB
        DB --> Svc: warehouseProductId
        deactivate DB
        
        loop For each serial number
            Svc -> DB: INSERT INTO product_details\n(serial, status='IN_STOCK')
            activate DB
            DB --> Svc: Product detail created
            deactivate DB
        end
        
        Svc -> DB: SELECT * FROM inventory_stock\nWHERE warehouse_product_id = ?
        activate DB
        DB --> Svc: Stock or null
        deactivate DB
        
        alt Stock exists
            Svc -> DB: UPDATE inventory_stock\nSET on_hand = on_hand + quantity
            activate DB
            DB --> Svc: Updated
            deactivate DB
        else Stock not exists
            Svc -> DB: INSERT INTO inventory_stock\n(on_hand = quantity)
            activate DB
            DB --> Svc: Created
            deactivate DB
        end
    end
    
    Svc -> DB: UPDATE purchase_orders\nSET status='RECEIVED'
    activate DB
    DB --> Svc: Updated
    deactivate DB
    
    Svc --> Ctrl: PurchaseOrderResponse
    deactivate Svc
    
    Ctrl --> FE: 200 OK\n{purchaseOrder}
    deactivate Ctrl
    
    FE --> WM: Thông báo thành công
    deactivate FE
    
else PO status != CREATED
    Svc --> Ctrl: InvalidStatusException
    deactivate Svc
    Ctrl --> FE: 400 Bad Request
    deactivate Ctrl
    FE --> WM: Không thể complete
    deactivate FE
end

@enduml
```

## 7. State Machine Diagram - Order Status

```plantuml
@startuml Order_State_Machine

[*] --> PENDING : Tạo đơn hàng

PENDING --> CONFIRMED : Thanh toán thành công
PENDING --> CANCELLED : Hủy đơn/Hết hạn

CONFIRMED --> PROCESSING : Admin xác nhận
CONFIRMED --> CANCELLED : Khách hủy/Admin hủy

PROCESSING --> SHIPPING : Giao cho ĐVVC
PROCESSING --> CANCELLED : Admin hủy

SHIPPING --> DELIVERED : Giao hàng thành công
SHIPPING --> CANCELLED : Giao hàng thất bại

DELIVERED --> [*]
CANCELLED --> [*]

note right of PENDING
  Payment Status: UNPAID
  Chờ thanh toán
end note

note right of CONFIRMED
  Payment Status: PAID
  Reserve stock
end note

note right of PROCESSING
  Chuẩn bị hàng
  Tạo Export Order
end note

note right of SHIPPING
  Đang vận chuyển
  Cập nhật tracking
end note

@enduml
```

## 8. State Machine Diagram - Payment Status

```plantuml
@startuml Payment_State_Machine

[*] --> PENDING : Tạo payment

PENDING --> PAID : Webhook từ SePay
PENDING --> EXPIRED : Timeout 15 phút
PENDING --> FAILED : Lỗi thanh toán

PAID --> REFUNDED : Hoàn tiền (Order cancelled)

EXPIRED --> [*]
FAILED --> [*]
REFUNDED --> [*]
PAID --> [*]

note right of PENDING
  Hiển thị QR Code
  Chờ khách chuyển khoản
  Expires in 15 minutes
end note

note right of PAID
  Cập nhật Order status
  Gửi email xác nhận
  Reserve inventory
end note

note right of REFUNDED
  Order bị hủy
  Hoàn tiền cho khách
  Release inventory
end note

@enduml
```

## 9. State Machine Diagram - Product Detail Status

```plantuml
@startuml Product_Detail_State_Machine

[*] --> IN_STOCK : Nhập kho\n(PO Complete)

IN_STOCK --> RESERVED : Order được tạo\n(Payment confirmed)
IN_STOCK --> DAMAGED : Phát hiện lỗi\n(Quality check)

RESERVED --> SOLD : Export Order\n(Order shipped)
RESERVED --> IN_STOCK : Order bị hủy\n(Release stock)

DAMAGED --> RETURNED : Trả nhà cung cấp
DAMAGED --> [*] : Thanh lý

SOLD --> RETURNED : Khách trả hàng\n(Return request)
SOLD --> [*]

RETURNED --> IN_STOCK : Kiểm tra OK\n(Quality check passed)
RETURNED --> DAMAGED : Kiểm tra lỗi\n(Quality check failed)
RETURNED --> [*] : Thanh lý

note right of IN_STOCK
  Available for sale
  Counted in sellable stock
end note

note right of RESERVED
  Held for order
  Not available for sale
  Counted in reserved stock
end note

note right of SOLD
  Shipped to customer
  No longer in warehouse
end note

note right of DAMAGED
  Cannot be sold
  Counted in damaged stock
end note

@enduml
```


## 10. Use Case Diagram - Customer

```plantuml
@startuml Customer_Use_Case

left to right direction
skinparam packageStyle rectangle

actor Customer

rectangle "WEB_TMDT System" {
    
    package "Authentication" {
        usecase "Đăng ký tài khoản" as UC1
        usecase "Đăng nhập" as UC2
        usecase "Xác thực OTP" as UC3
        usecase "Cập nhật thông tin" as UC4
    }
    
    package "Product Browsing" {
        usecase "Xem danh sách sản phẩm" as UC5
        usecase "Tìm kiếm sản phẩm" as UC6
        usecase "Xem chi tiết sản phẩm" as UC7
        usecase "Lọc theo danh mục" as UC8
    }
    
    package "Shopping Cart" {
        usecase "Thêm vào giỏ hàng" as UC9
        usecase "Xem giỏ hàng" as UC10
        usecase "Cập nhật số lượng" as UC11
        usecase "Xóa khỏi giỏ" as UC12
    }
    
    package "Order Management" {
        usecase "Đặt hàng" as UC13
        usecase "Thanh toán SePay" as UC14
        usecase "Xem lịch sử đơn hàng" as UC15
        usecase "Xem chi tiết đơn hàng" as UC16
        usecase "Hủy đơn hàng" as UC17
    }
}

actor "SePay System" as SePay
actor "GHTK System" as GHTK

Customer --> UC1
Customer --> UC2
Customer --> UC4
Customer --> UC5
Customer --> UC6
Customer --> UC7
Customer --> UC8
Customer --> UC9
Customer --> UC10
Customer --> UC11
Customer --> UC12
Customer --> UC13
Customer --> UC14
Customer --> UC15
Customer --> UC16
Customer --> UC17

UC1 ..> UC3 : <<include>>
UC13 ..> UC14 : <<include>>
UC14 ..> SePay : <<communicate>>
UC13 ..> GHTK : <<communicate>>

@enduml
```

## 11. Use Case Diagram - Warehouse Manager

```plantuml
@startuml Warehouse_Manager_Use_Case

left to right direction
skinparam packageStyle rectangle

actor "Warehouse Manager" as WM

rectangle "Inventory Management System" {
    
    package "Supplier Management" {
        usecase "Quản lý nhà cung cấp" as UC1
        usecase "Tạo nhà cung cấp mới" as UC2
        usecase "Cập nhật thông tin NCC" as UC3
    }
    
    package "Purchase Order" {
        usecase "Tạo Purchase Order" as UC4
        usecase "Xem danh sách PO" as UC5
        usecase "Nhập hàng vào kho" as UC6
        usecase "Complete PO" as UC7
        usecase "Hủy PO" as UC8
    }
    
    package "Warehouse Product" {
        usecase "Tạo Warehouse Product" as UC9
        usecase "Cập nhật thông tin sản phẩm" as UC10
        usecase "Upload ảnh sản phẩm" as UC11
        usecase "Quản lý thông số kỹ thuật" as UC12
    }
    
    package "Inventory Management" {
        usecase "Quản lý serial sản phẩm" as UC13
        usecase "Tạo phiếu xuất kho" as UC14
        usecase "Kiểm kê tồn kho" as UC15
        usecase "Xem báo cáo tồn kho" as UC16
        usecase "Quản lý sản phẩm lỗi" as UC17
    }
}

actor "Cloudinary" as Cloud

WM --> UC1
WM --> UC2
WM --> UC3
WM --> UC4
WM --> UC5
WM --> UC6
WM --> UC7
WM --> UC8
WM --> UC9
WM --> UC10
WM --> UC11
WM --> UC12
WM --> UC13
WM --> UC14
WM --> UC15
WM --> UC16
WM --> UC17

UC6 ..> UC7 : <<include>>
UC7 ..> UC13 : <<include>>
UC11 ..> Cloud : <<communicate>>

@enduml
```

## 12. Use Case Diagram - Product Manager

```plantuml
@startuml Product_Manager_Use_Case

left to right direction
skinparam packageStyle rectangle

actor "Product Manager" as PM

rectangle "Product Management System" {
    
    package "Category Management" {
        usecase "Quản lý danh mục" as UC1
        usecase "Tạo danh mục mới" as UC2
        usecase "Sửa danh mục" as UC3
        usecase "Xóa danh mục" as UC4
        usecase "Sắp xếp danh mục" as UC5
    }
    
    package "Product Publishing" {
        usecase "Xem kho hàng" as UC6
        usecase "Publish sản phẩm lên web" as UC7
        usecase "Chọn Warehouse Product" as UC8
        usecase "Chọn Category" as UC9
        usecase "Đặt giá bán" as UC10
    }
    
    package "Product Management" {
        usecase "Cập nhật thông tin sản phẩm" as UC11
        usecase "Cập nhật giá sản phẩm" as UC12
        usecase "Ẩn/Hiện sản phẩm" as UC13
        usecase "Upload ảnh sản phẩm" as UC14
        usecase "Quản lý thông số kỹ thuật" as UC15
    }
    
    package "Reporting" {
        usecase "Xem báo cáo sản phẩm" as UC16
        usecase "Xem sản phẩm bán chạy" as UC17
    }
}

actor "Cloudinary" as Cloud

PM --> UC1
PM --> UC2
PM --> UC3
PM --> UC4
PM --> UC5
PM --> UC6
PM --> UC7
PM --> UC11
PM --> UC12
PM --> UC13
PM --> UC14
PM --> UC15
PM --> UC16
PM --> UC17

UC7 ..> UC8 : <<include>>
UC7 ..> UC9 : <<include>>
UC7 ..> UC10 : <<include>>
UC14 ..> Cloud : <<communicate>>

@enduml
```

## 13. Component Diagram - Backend Architecture

```plantuml
@startuml Backend_Component_Diagram

package "Presentation Layer" {
    [AuthController] as AuthCtrl
    [ProductController] as ProdCtrl
    [CartController] as CartCtrl
    [OrderController] as OrderCtrl
    [PaymentController] as PayCtrl
    [InventoryController] as InvCtrl
}

package "Security Layer" {
    [JwtAuthenticationFilter] as JWTFilter
    [SecurityConfig] as SecConfig
}

package "Business Logic Layer" {
    [AuthService] as AuthSvc
    [UserService] as UserSvc
    [ProductService] as ProdSvc
    [CategoryService] as CatSvc
    [CartService] as CartSvc
    [OrderService] as OrderSvc
    [PaymentService] as PaySvc
    [InventoryService] as InvSvc
    [SupplierService] as SupSvc
}

package "Data Access Layer" {
    [UserRepository] as UserRepo
    [CustomerRepository] as CustRepo
    [EmployeeRepository] as EmpRepo
    [ProductRepository] as ProdRepo
    [CategoryRepository] as CatRepo
    [CartRepository] as CartRepo
    [OrderRepository] as OrderRepo
    [PaymentRepository] as PayRepo
    [InventoryRepository] as InvRepo
    [SupplierRepository] as SupRepo
}

package "External Integration" {
    [SePay Client] as SePay
    [GHTK Client] as GHTK
    [Cloudinary Client] as Cloud
    [Email Service] as Email
}

database "MySQL Database" as DB

JWTFilter --> AuthCtrl
JWTFilter --> ProdCtrl
JWTFilter --> CartCtrl
JWTFilter --> OrderCtrl
JWTFilter --> PayCtrl
JWTFilter --> InvCtrl

AuthCtrl --> AuthSvc
AuthCtrl --> UserSvc
ProdCtrl --> ProdSvc
ProdCtrl --> CatSvc
CartCtrl --> CartSvc
OrderCtrl --> OrderSvc
PayCtrl --> PaySvc
InvCtrl --> InvSvc
InvCtrl --> SupSvc

AuthSvc --> UserRepo
AuthSvc --> CustRepo
AuthSvc --> EmpRepo
UserSvc --> UserRepo
ProdSvc --> ProdRepo
CatSvc --> CatRepo
CartSvc --> CartRepo
OrderSvc --> OrderRepo
PaySvc --> PayRepo
InvSvc --> InvRepo
SupSvc --> SupRepo

UserRepo --> DB
CustRepo --> DB
EmpRepo --> DB
ProdRepo --> DB
CatRepo --> DB
CartRepo --> DB
OrderRepo --> DB
PayRepo --> DB
InvRepo --> DB
SupRepo --> DB

PaySvc --> SePay
OrderSvc --> GHTK
ProdSvc --> Cloud
InvSvc --> Cloud
AuthSvc --> Email
OrderSvc --> Email

@enduml
```

## 14. Deployment Diagram - Production Environment

```plantuml
@startuml Deployment_Diagram

node "Client Devices" {
    artifact "Web Browser" as Browser
    artifact "Mobile Browser" as Mobile
}

node "Load Balancer" {
    component "Nginx" as LB
}

node "Frontend Server 1" {
    component "Next.js App" as FE1
    note right of FE1
        Node.js 18
        Port 3000
    end note
}

node "Frontend Server 2" {
    component "Next.js App" as FE2
    note right of FE2
        Node.js 18
        Port 3000
    end note
}

node "Backend Server 1" {
    component "Spring Boot App" as BE1
    note right of BE1
        Java 17
        Port 8080
    end note
}

node "Backend Server 2" {
    component "Spring Boot App" as BE2
    note right of BE2
        Java 17
        Port 8080
    end note
}

database "MySQL Master" as DBM {
    storage "Database" as DB1
}

database "MySQL Slave" as DBS {
    storage "Database" as DB2
}

node "Cache Server" {
    component "Redis Cluster" as Redis
}

cloud "External Services" {
    component "SePay API" as SePay
    component "GHTK API" as GHTK
    component "Cloudinary" as Cloud
    component "SMTP Server" as SMTP
}

Browser --> LB : HTTPS
Mobile --> LB : HTTPS

LB --> FE1 : HTTP
LB --> FE2 : HTTP

FE1 --> BE1 : REST API
FE1 --> BE2 : REST API
FE2 --> BE1 : REST API
FE2 --> BE2 : REST API

BE1 --> DBM : Read/Write
BE2 --> DBM : Read/Write
BE1 --> DBS : Read Only
BE2 --> DBS : Read Only

DBM --> DBS : Replication

BE1 --> Redis : Cache
BE2 --> Redis : Cache

BE1 --> SePay : API Call
BE2 --> SePay : API Call
BE1 --> GHTK : API Call
BE2 --> GHTK : API Call
BE1 --> Cloud : Upload
BE2 --> Cloud : Upload
BE1 --> SMTP : Send Email
BE2 --> SMTP : Send Email

@enduml
```

## 15. Activity Diagram - Complete Order Flow

```plantuml
@startuml Complete_Order_Flow

start

:Khách hàng xem giỏ hàng;

if (Giỏ hàng có sản phẩm?) then (yes)
    :Kiểm tra tồn kho;
    
    if (Đủ hàng?) then (yes)
        :Nhập thông tin giao hàng;
        :Tính phí vận chuyển (GHTK);
        :Hiển thị tổng tiền;
        
        if (Xác nhận đặt hàng?) then (yes)
            :Tạo Order (PENDING);
            :Tạo Payment (PENDING);
            :Gọi SePay API;
            :Tạo QR Code;
            :Hiển thị QR cho khách;
            
            fork
                :Chờ thanh toán (15 phút);
            fork again
                :Khách quét QR & chuyển khoản;
            end fork
            
            if (Thanh toán thành công?) then (yes)
                :SePay gửi Webhook;
                :Cập nhật Payment (PAID);
                :Cập nhật Order (CONFIRMED);
                :Reserve Stock;
                :Gửi email xác nhận;
                
                :Admin xem xét đơn;
                
                if (Admin xác nhận?) then (yes)
                    :Order (PROCESSING);
                    :Tạo Export Order;
                    :Lấy hàng từ kho;
                    :Cập nhật Product Details (SOLD);
                    :Cập nhật Inventory Stock;
                    :Đóng gói;
                    
                    :Order (SHIPPING);
                    :Gọi GHTK API;
                    :Lấy tracking number;
                    :Thông báo đang giao;
                    
                    if (Giao hàng thành công?) then (yes)
                        :Order (DELIVERED);
                        :Gửi email cảm ơn;
                        stop
                    else (thất bại)
                        :Giao hàng thất bại;
                        :Trả hàng về kho;
                        :Order (CANCELLED);
                        :Hoàn tiền;
                        stop
                    endif
                else (hủy)
                    :Order (CANCELLED);
                    :Release Stock;
                    :Hoàn tiền;
                    stop
                endif
            else (timeout/failed)
                :Payment (EXPIRED/FAILED);
                :Thông báo hết hạn;
                stop
            endif
        else (no)
            stop
        endif
    else (hết hàng)
        :Thông báo hết hàng;
        stop
    endif
else (no)
    :Thông báo giỏ hàng trống;
    stop
endif

@enduml
```

---

## Tổng Kết

File này chứa **15 sơ đồ UML chuẩn** sử dụng PlantUML:

### Class Diagrams (3)
1. Auth Module
2. Product & Inventory Module  
3. Order & Payment Module

### Sequence Diagrams (3)
4. User Login Flow
5. Order & Payment Flow
6. Complete Purchase Order

### State Machine Diagrams (3)
7. Order Status
8. Payment Status
9. Product Detail Status

### Use Case Diagrams (3)
10. Customer Use Cases
11. Warehouse Manager Use Cases
12. Product Manager Use Cases

### Component & Deployment (2)
13. Backend Component Diagram
14. Deployment Diagram

### Activity Diagram (1)
15. Complete Order Flow

Tất cả sơ đồ đều theo chuẩn UML 2.0 và có thể render bằng PlantUML.
