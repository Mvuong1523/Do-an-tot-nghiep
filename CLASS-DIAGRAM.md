# SƠ ĐỒ LỚP (CLASS DIAGRAM) - HỆ THỐNG TMĐT

## 📊 Tổng quan

Sơ đồ lớp UML mô tả cấu trúc các class Entity và mối quan hệ giữa chúng trong hệ thống thương mại điện tử.

---

## 🎨 SƠ ĐỒ TỔNG QUAN (MERMAID)

```mermaid
classDiagram
    %% ========================================
    %% MODULE 1: AUTHENTICATION & AUTHORIZATION
    %% ========================================
    
    class User {
        -Long id
        -String email
        -String password
        -Role role
        -Status status
        -Customer customer
        -Employee employee
        +getId() Long
        +getEmail() String
        +getRole() Role
        +getStatus() Status
    }
    
    class Customer {
        -Long id
        -User user
        -String fullName
        -String phone
        -String gender
        -LocalDate birthDate
        -String address
        +getId() Long
        +getFullName() String
        +getPhone() String
    }
    
    class Employee {
        -Long id
        -User user
        -Position position
        -String fullName
        -String phone
        -String address
        -boolean firstLogin
        +getId() Long
        +getPosition() Position
        +isFirstLogin() boolean
    }
    
    class Role {
        <<enumeration>>
        CUSTOMER
        ADMIN
        WAREHOUSE_MANAGER
        SALES
        ACCOUNTANT
        SHIPPER
    }
    
    class Status {
        <<enumeration>>
        ACTIVE
        INACTIVE
        BANNED
    }
    
    class Position {
        <<enumeration>>
        ADMIN
        WAREHOUSE_MANAGER
        SALES
        ACCOUNTANT
        SHIPPER
    }
    
    %% Relationships - Authentication
    User "1" -- "1" Customer : has >
    User "1" -- "1" Employee : has >
    User --> Role : uses
    User --> Status : has
    Employee --> Position : has
    
    %% ========================================
    %% MODULE 2: PRODUCT MANAGEMENT
    %% ========================================
    
    class Category {
        -Long id
        -String name
        -String slug
        -String description
        -String imageUrl
        -Integer displayOrder
        -Boolean active
        -Category parent
        -List~Category~ children
        -List~Product~ products
        +getId() Long
        +getName() String
        +getProductCount() int
    }
    
    class Product {
        -Long id
        -Category category
        -String name
        -Double price
        -String sku
        -String description
        -List~ProductImage~ images
        -Long stockQuantity
        -Long reservedQuantity
        -String techSpecsJson
        -ProductDetail productDetail
        -WarehouseProduct warehouseProduct
        -Boolean active
        +getId() Long
        +getName() String
        +getPrice() Double
        +getAvailableQuantity() Long
    }
    
    class ProductImage {
        -Long id
        -Product product
        -String imageUrl
        -Integer displayOrder
        -Boolean isPrimary
        -String altText
        -LocalDateTime createdAt
        +getId() Long
        +getImageUrl() String
        +onCreate() void
    }
    
    %% Relationships - Product
    Category "1" -- "0..*" Category : parent-child
    Category "1" -- "0..*" Product : contains >
    Product "1" -- "0..*" ProductImage : has >
    
    %% ========================================
    %% MODULE 3: CART & ORDER
    %% ========================================
    
    class Cart {
        -Long id
        -Customer customer
        -List~CartItem~ items
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +getId() Long
        +getTotalItems() int
        +getSubtotal() Double
        +addItem(CartItem) void
        +removeItem(CartItem) void
        +clearItems() void
        +onCreate() void
        +onUpdate() void
    }
    
    class CartItem {
        -Long id
        -Cart cart
        -Product product
        -Integer quantity
        -Double price
        -LocalDateTime addedAt
        +getId() Long
        +getSubtotal() Double
        +onCreate() void
    }
    
    class Order {
        -Long id
        -String orderCode
        -Customer customer
        -List~OrderItem~ items
        -String shippingAddress
        -String province
        -String district
        -String ward
        -String wardName
        -String address
        -String note
        -Double subtotal
        -Double shippingFee
        -Double discount
        -Double total
        -PaymentStatus paymentStatus
        -String paymentMethod
        -Long paymentId
        -OrderStatus status
        -LocalDateTime createdAt
        -LocalDateTime confirmedAt
        -LocalDateTime shippedAt
        -LocalDateTime deliveredAt
        -LocalDateTime cancelledAt
        -String cancelReason
        -String ghnOrderCode
        -String ghnShippingStatus
        -LocalDateTime ghnCreatedAt
        -LocalDateTime ghnExpectedDeliveryTime
        +getId() Long
        +getOrderCode() String
        +getTotal() Double
        +onCreate() void
    }
    
    class OrderItem {
        -Long id
        -Order order
        -Product product
        -String productName
        -Double price
        -Integer quantity
        -Double subtotal
        -String serialNumber
        -Boolean reserved
        -Boolean exported
        +getId() Long
        +getSubtotal() Double
    }
    
    class OrderStatus {
        <<enumeration>>
        PENDING_PAYMENT
        CONFIRMED
        READY_TO_SHIP
        SHIPPING
        DELIVERED
        CANCELLED
    }
    
    class PaymentStatus {
        <<enumeration>>
        UNPAID
        PAID
        REFUNDED
    }
    
    %% Relationships - Cart & Order
    Customer "1" -- "1" Cart : owns >
    Cart "1" -- "0..*" CartItem : contains >
    CartItem "*" -- "1" Product : references >
    
    Customer "1" -- "0..*" Order : places >
    Order "1" -- "1..*" OrderItem : contains >
    OrderItem "*" -- "1" Product : references >
    Order --> OrderStatus : has
    Order --> PaymentStatus : has
    
    %% ========================================
    %% MODULE 4: INVENTORY MANAGEMENT
    %% ========================================
    
    class Supplier {
        -Long id
        -Boolean autoCreated
        -String name
        -String contactName
        -String phone
        -String email
        -String address
        -String taxCode
        -String bankAccount
        -String paymentTerm
        -Integer paymentTermDays
        -Boolean active
        +getId() Long
        +getName() String
        +getTaxCode() String
    }
    
    class WarehouseProduct {
        -Long id
        -String sku
        -String internalName
        -String techSpecsJson
        -String description
        -Supplier supplier
        -LocalDateTime lastImportDate
        -Product product
        -List~ProductDetail~ serials
        -List~WarehouseProductImage~ images
        -List~ProductSpecification~ specifications
        +getId() Long
        +getSku() String
        +getQuantityInStock() long
    }
    
    class ProductDetail {
        -Long id
        -String serialNumber
        -Double importPrice
        -Double salePrice
        -LocalDateTime importDate
        -ProductStatus status
        -WarehouseProduct warehouseProduct
        -PurchaseOrderItem purchaseOrderItem
        -Product product
        -Integer warrantyMonths
        -Long soldOrderId
        -LocalDateTime soldDate
        -String note
        +getId() Long
        +getSerialNumber() String
        +getStatus() ProductStatus
    }
    
    class ProductStatus {
        <<enumeration>>
        IN_STOCK
        RESERVED
        SOLD
        DAMAGED
        RETURNED
    }
    
    class PurchaseOrder {
        -Long id
        -String poCode
        -Supplier supplier
        -LocalDateTime orderDate
        -LocalDateTime receivedDate
        -POStatus status
        -String createdBy
        -String note
        -List~PurchaseOrderItem~ items
        +getId() Long
        +getPoCode() String
        +getStatus() POStatus
    }
    
    class PurchaseOrderItem {
        -Long id
        -PurchaseOrder purchaseOrder
        -String sku
        -WarehouseProduct warehouseProduct
        -Long quantity
        -Double unitCost
        -Integer warrantyMonths
        -String note
        -List~ProductDetail~ productDetails
        +getId() Long
        +getQuantity() Long
        +getUnitCost() Double
    }
    
    class POStatus {
        <<enumeration>>
        CREATED
        RECEIVED
        CANCELED
    }
    
    class ExportOrder {
        -Long id
        -String exportCode
        -LocalDateTime exportDate
        -String createdBy
        -String reason
        -String note
        -ExportStatus status
        -Long orderId
        -List~ExportOrderItem~ items
        +getId() Long
        +getExportCode() String
    }
    
    class ExportOrderItem {
        -Long id
        -ExportOrder exportOrder
        -String serialNumber
        -Integer quantity
        -String note
        +getId() Long
    }
    
    class ExportStatus {
        <<enumeration>>
        PENDING
        COMPLETED
        CANCELLED
    }
    
    class InventoryStock {
        -Long id
        -WarehouseProduct warehouseProduct
        -Long onHand
        -Long reserved
        -Long damaged
        -LocalDate lastAuditDate
        +getId() Long
        +getSellable() Long
        +getAvailable() Long
    }
    
    class ProductSpecification {
        -Long id
        -WarehouseProduct warehouseProduct
        -String specKey
        -String specValue
        +getId() Long
    }
    
    class WarehouseProductImage {
        -Long id
        -WarehouseProduct warehouseProduct
        -String imageUrl
        -Integer displayOrder
        +getId() Long
    }
    
    %% Relationships - Inventory
    Supplier "1" -- "0..*" PurchaseOrder : supplies >
    Supplier "1" -- "0..*" WarehouseProduct : provides >
    
    PurchaseOrder "1" -- "1..*" PurchaseOrderItem : contains >
    PurchaseOrder --> POStatus : has
    
    PurchaseOrderItem "*" -- "1" WarehouseProduct : orders >
    PurchaseOrderItem "1" -- "0..*" ProductDetail : receives >
    
    WarehouseProduct "1" -- "1" Product : publishes >
    WarehouseProduct "1" -- "0..*" ProductDetail : has >
    WarehouseProduct "1" -- "0..*" ProductSpecification : has >
    WarehouseProduct "1" -- "0..*" WarehouseProductImage : has >
    WarehouseProduct "1" -- "0..1" InventoryStock : tracks >
    
    ProductDetail "*" -- "1" WarehouseProduct : belongs to >
    ProductDetail "1" -- "0..1" Product : links >
    ProductDetail --> ProductStatus : has
    
    ExportOrder "1" -- "1..*" ExportOrderItem : contains >
    ExportOrder --> ExportStatus : has
    
    %% ========================================
    %% MODULE 5: PAYMENT
    %% ========================================
    
    class Payment {
        -Long id
        -String paymentCode
        -Order order
        -User user
        -Double amount
        -PaymentMethod method
        -PaymentStatusEnum status
        -String sepayTransactionId
        -String sepayBankCode
        -String sepayAccountNumber
        -String sepayAccountName
        -String sepayContent
        -String sepayQrCode
        -String sepayResponse
        -LocalDateTime createdAt
        -LocalDateTime paidAt
        -LocalDateTime expiredAt
        -String failureReason
        +getId() Long
        +getPaymentCode() String
        +getAmount() Double
        +onCreate() void
    }
    
    class PaymentMethod {
        <<enumeration>>
        SEPAY
        VNPAY
        COD
    }
    
    class PaymentStatusEnum {
        <<enumeration>>
        PENDING
        SUCCESS
        FAILED
        EXPIRED
    }
    
    class BankAccount {
        -Long id
        -String bankCode
        -String bankName
        -String accountNumber
        -String accountName
        -String description
        -String sepayApiToken
        -String sepayMerchantId
        -Boolean isActive
        -Boolean isDefault
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +getId() Long
        +getAccountNumber() String
        +onCreate() void
        +onUpdate() void
    }
    
    %% Relationships - Payment
    Order "1" -- "0..1" Payment : has >
    User "1" -- "0..*" Payment : makes >
    Payment --> PaymentMethod : uses
    Payment --> PaymentStatusEnum : has
    
    %% ========================================
    %% MODULE 6: ACCOUNTING
    %% ========================================
    
    class FinancialTransaction {
        -Long id
        -String transactionCode
        -TransactionType type
        -TransactionCategory category
        -Double amount
        -Long orderId
        -Long supplierId
        -String description
        -LocalDateTime transactionDate
        -LocalDateTime createdAt
        -String createdBy
        +getId() Long
        +getTransactionCode() String
        +getAmount() Double
        +onCreate() void
    }
    
    class TransactionType {
        <<enumeration>>
        REVENUE
        EXPENSE
        REFUND
    }
    
    class TransactionCategory {
        <<enumeration>>
        SALES
        SHIPPING
        PAYMENT_FEE
        TAX
        SUPPLIER_PAYMENT
        REFUND
        OTHER
    }
    
    class AccountingPeriod {
        -Long id
        -String name
        -LocalDate startDate
        -LocalDate endDate
        -PeriodStatus status
        -Double totalRevenue
        -Double totalExpense
        -Double netProfit
        -Double discrepancyRate
        -LocalDateTime closedAt
        -String closedBy
        -LocalDateTime createdAt
        +getId() Long
        +getName() String
        +getNetProfit() Double
        +onCreate() void
    }
    
    class PeriodStatus {
        <<enumeration>>
        OPEN
        CLOSED
    }
    
    class SupplierPayable {
        -Long id
        -String payableCode
        -Supplier supplier
        -PurchaseOrder purchaseOrder
        -BigDecimal totalAmount
        -BigDecimal paidAmount
        -BigDecimal remainingAmount
        -PayableStatus status
        -LocalDate invoiceDate
        -LocalDate dueDate
        -Integer paymentTermDays
        -String note
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -String createdBy
        +getId() Long
        +getPayableCode() String
        +getRemainingAmount() BigDecimal
        +updateStatus() void
        +onCreate() void
        +onUpdate() void
    }
    
    class PayableStatus {
        <<enumeration>>
        UNPAID
        PARTIAL
        PAID
        OVERDUE
    }
    
    class SupplierPayment {
        -Long id
        -String paymentCode
        -SupplierPayable payable
        -BigDecimal amount
        -LocalDate paymentDate
        -SupplierPaymentMethod paymentMethod
        -String referenceNumber
        -String note
        -LocalDateTime createdAt
        -String createdBy
        +getId() Long
        +getPaymentCode() String
        +getAmount() BigDecimal
        +onCreate() void
    }
    
    class SupplierPaymentMethod {
        <<enumeration>>
        CASH
        BANK_TRANSFER
        CHECK
    }
    
    class PaymentReconciliation {
        -Long id
        -Long orderId
        -Long paymentId
        -String bankTransactionId
        -Double amount
        -ReconciliationStatus status
        -LocalDateTime reconciliationDate
        -String note
        -LocalDateTime createdAt
        +getId() Long
    }
    
    class ReconciliationStatus {
        <<enumeration>>
        MATCHED
        UNMATCHED
        PENDING
    }
    
    class TaxReport {
        -Long id
        -Long periodId
        -TaxReportType reportType
        -Double totalRevenue
        -Double taxableAmount
        -Double taxAmount
        -LocalDateTime reportDate
        -TaxReportStatus status
        -LocalDateTime createdAt
        -String createdBy
        +getId() Long
    }
    
    class TaxReportType {
        <<enumeration>>
        VAT
        INCOME_TAX
    }
    
    class TaxReportStatus {
        <<enumeration>>
        DRAFT
        SUBMITTED
    }
    
    %% Relationships - Accounting
    FinancialTransaction --> TransactionType : has
    FinancialTransaction --> TransactionCategory : has
    
    AccountingPeriod --> PeriodStatus : has
    
    Supplier "1" -- "0..*" SupplierPayable : owes >
    PurchaseOrder "1" -- "0..*" SupplierPayable : creates >
    SupplierPayable "1" -- "0..*" SupplierPayment : receives >
    SupplierPayable --> PayableStatus : has
    SupplierPayment --> SupplierPaymentMethod : uses
    
    PaymentReconciliation --> ReconciliationStatus : has
    
    TaxReport --> TaxReportType : has
    TaxReport --> TaxReportStatus : has
```

---

## 📋 CHI TIẾT CÁC CLASS CHÍNH

### 🔐 Module Authentication

#### Class: User
**Mô tả:** Tài khoản đăng nhập chung

**Thuộc tính:**
- `id: Long` - Primary key
- `email: String` - Email đăng nhập (unique)
- `password: String` - Mật khẩu đã mã hóa
- `role: Role` - Vai trò người dùng
- `status: Status` - Trạng thái tài khoản
- `customer: Customer` - Thông tin khách hàng (1-1)
- `employee: Employee` - Thông tin nhân viên (1-1)

**Phương thức:**
- `getId(): Long`
- `getEmail(): String`
- `getRole(): Role`
- `getStatus(): Status`

**Quan hệ:**
- 1-1 với Customer
- 1-1 với Employee
- Sử dụng enum Role và Status

---

#### Class: Customer
**Mô tả:** Thông tin chi tiết khách hàng

**Thuộc tính:**
- `id: Long` - Primary key
- `user: User` - Tài khoản liên kết
- `fullName: String` - Họ tên
- `phone: String` - Số điện thoại (unique)
- `gender: String` - Giới tính
- `birthDate: LocalDate` - Ngày sinh
- `address: String` - Địa chỉ

**Phương thức:**
- `getId(): Long`
- `getFullName(): String`
- `getPhone(): String`

**Quan hệ:**
- 1-1 với User (owner side)
- 1-1 với Cart
- 1-N với Order

---

#### Class: Employee
**Mô tả:** Thông tin nhân viên

**Thuộc tính:**
- `id: Long` - Primary key
- `user: User` - Tài khoản liên kết
- `position: Position` - Vị trí công việc
- `fullName: String` - Họ tên
- `phone: String` - Số điện thoại
- `address: String` - Địa chỉ
- `firstLogin: boolean` - Lần đầu đăng nhập

**Phương thức:**
- `getId(): Long`
- `getPosition(): Position`
- `isFirstLogin(): boolean`

**Quan hệ:**
- 1-1 với User (owner side)
- Sử dụng enum Position

---

### 📦 Module Product

#### Class: Category
**Mô tả:** Danh mục sản phẩm (có phân cấp)

**Thuộc tính:**
- `id: Long` - Primary key
- `name: String` - Tên danh mục
- `slug: String` - URL-friendly name (unique)
- `description: String` - Mô tả
- `imageUrl: String` - Ảnh đại diện
- `displayOrder: Integer` - Thứ tự hiển thị
- `active: Boolean` - Hiển thị/ẩn
- `parent: Category` - Danh mục cha (self-reference)
- `children: List<Category>` - Danh mục con
- `products: List<Product>` - Danh sách sản phẩm

**Phương thức:**
- `getId(): Long`
- `getName(): String`
- `getProductCount(): int` - Đếm số sản phẩm

**Quan hệ:**
- N-1 với Category (parent)
- 1-N với Category (children)
- 1-N với Product

---

#### Class: Product
**Mô tả:** Sản phẩm hiển thị trên website

**Thuộc tính:**
- `id: Long` - Primary key
- `category: Category` - Danh mục
- `name: String` - Tên sản phẩm
- `price: Double` - Giá bán
- `sku: String` - Mã SKU (unique)
- `description: String` - Mô tả chi tiết
- `images: List<ProductImage>` - Danh sách ảnh
- `stockQuantity: Long` - Tồn kho thực tế
- `reservedQuantity: Long` - Số lượng đang giữ
- `techSpecsJson: String` - Thông số kỹ thuật (JSON)
- `productDetail: ProductDetail` - Chi tiết serial
- `warehouseProduct: WarehouseProduct` - Sản phẩm kho
- `active: Boolean` - Đang bán/ngừng bán

**Phương thức:**
- `getId(): Long`
- `getName(): String`
- `getPrice(): Double`
- `getAvailableQuantity(): Long` - Số lượng có thể bán

**Quan hệ:**
- N-1 với Category
- 1-N với ProductImage
- 1-1 với ProductDetail
- 1-1 với WarehouseProduct

---

### 🛒 Module Cart & Order

#### Class: Cart
**Mô tả:** Giỏ hàng của khách hàng

**Thuộc tính:**
- `id: Long` - Primary key
- `customer: Customer` - Khách hàng (1-1)
- `items: List<CartItem>` - Danh sách sản phẩm
- `createdAt: LocalDateTime` - Ngày tạo
- `updatedAt: LocalDateTime` - Ngày cập nhật

**Phương thức:**
- `getId(): Long`
- `getTotalItems(): int` - Tổng số sản phẩm
- `getSubtotal(): Double` - Tổng tiền
- `addItem(CartItem): void` - Thêm sản phẩm
- `removeItem(CartItem): void` - Xóa sản phẩm
- `clearItems(): void` - Xóa tất cả
- `onCreate(): void` - Lifecycle hook
- `onUpdate(): void` - Lifecycle hook

**Quan hệ:**
- 1-1 với Customer
- 1-N với CartItem

---

#### Class: Order
**Mô tả:** Đơn hàng

**Thuộc tính:**
- `id: Long` - Primary key
- `orderCode: String` - Mã đơn hàng (unique)
- `customer: Customer` - Khách hàng
- `items: List<OrderItem>` - Danh sách sản phẩm
- `shippingAddress: String` - Địa chỉ giao hàng
- `province, district, ward, wardName, address: String` - Địa chỉ chi tiết
- `note: String` - Ghi chú
- `subtotal, shippingFee, discount, total: Double` - Giá tiền
- `paymentStatus: PaymentStatus` - Trạng thái thanh toán
- `paymentMethod: String` - Phương thức thanh toán
- `paymentId: Long` - Reference Payment
- `status: OrderStatus` - Trạng thái đơn hàng
- `createdAt, confirmedAt, shippedAt, deliveredAt, cancelledAt: LocalDateTime` - Thời gian
- `cancelReason: String` - Lý do hủy
- `ghnOrderCode, ghnShippingStatus: String` - Thông tin GHN
- `ghnCreatedAt, ghnExpectedDeliveryTime: LocalDateTime` - Thời gian GHN

**Phương thức:**
- `getId(): Long`
- `getOrderCode(): String`
- `getTotal(): Double`
- `onCreate(): void` - Khởi tạo giá trị mặc định

**Quan hệ:**
- N-1 với Customer
- 1-N với OrderItem
- 1-1 với Payment
- Sử dụng enum OrderStatus, PaymentStatus

---

### 🏭 Module Inventory

#### Class: WarehouseProduct
**Mô tả:** Sản phẩm trong kho (chưa publish)

**Thuộc tính:**
- `id: Long` - Primary key
- `sku: String` - Mã SKU (unique)
- `internalName: String` - Tên kỹ thuật
- `techSpecsJson: String` - Thông số kỹ thuật (JSON)
- `description: String` - Mô tả
- `supplier: Supplier` - Nhà cung cấp
- `lastImportDate: LocalDateTime` - Ngày nhập gần nhất
- `product: Product` - Sản phẩm đã publish (1-1)
- `serials: List<ProductDetail>` - Danh sách serial
- `images: List<WarehouseProductImage>` - Danh sách ảnh
- `specifications: List<ProductSpecification>` - Thông số kỹ thuật

**Phương thức:**
- `getId(): Long`
- `getSku(): String`
- `getQuantityInStock(): long` - Đếm số serial IN_STOCK

**Quan hệ:**
- N-1 với Supplier
- 1-1 với Product
- 1-N với ProductDetail
- 1-N với ProductSpecification
- 1-N với WarehouseProductImage
- 1-1 với InventoryStock

---

#### Class: ProductDetail
**Mô tả:** Chi tiết sản phẩm theo serial/IMEI

**Thuộc tính:**
- `id: Long` - Primary key
- `serialNumber: String` - Serial/IMEI (unique)
- `importPrice: Double` - Giá nhập
- `salePrice: Double` - Giá bán thực tế
- `importDate: LocalDateTime` - Ngày nhập kho
- `status: ProductStatus` - Trạng thái
- `warehouseProduct: WarehouseProduct` - Sản phẩm kho
- `purchaseOrderItem: PurchaseOrderItem` - Phiếu nhập
- `product: Product` - Sản phẩm đã publish (1-1)
- `warrantyMonths: Integer` - Bảo hành (tháng)
- `soldOrderId: Long` - Đơn hàng đã bán
- `soldDate: LocalDateTime` - Ngày bán
- `note: String` - Ghi chú

**Phương thức:**
- `getId(): Long`
- `getSerialNumber(): String`
- `getStatus(): ProductStatus`

**Quan hệ:**
- N-1 với WarehouseProduct
- N-1 với PurchaseOrderItem
- 1-1 với Product
- Sử dụng enum ProductStatus

---

#### Class: PurchaseOrder
**Mô tả:** Đơn đặt hàng nhà cung cấp

**Thuộc tính:**
- `id: Long` - Primary key
- `poCode: String` - Mã PO (unique)
- `supplier: Supplier` - Nhà cung cấp
- `orderDate: LocalDateTime` - Ngày đặt hàng
- `receivedDate: LocalDateTime` - Ngày nhập thực tế
- `status: POStatus` - Trạng thái
- `createdBy: String` - Người tạo
- `note: String` - Ghi chú
- `items: List<PurchaseOrderItem>` - Danh sách sản phẩm

**Phương thức:**
- `getId(): Long`
- `getPoCode(): String`
- `getStatus(): POStatus`

**Quan hệ:**
- N-1 với Supplier
- 1-N với PurchaseOrderItem
- 1-N với SupplierPayable
- Sử dụng enum POStatus

---

### 💳 Module Payment

#### Class: Payment
**Mô tả:** Thanh toán đơn hàng

**Thuộc tính:**
- `id: Long` - Primary key
- `paymentCode: String` - Mã thanh toán (unique)
- `order: Order` - Đơn hàng (1-1)
- `user: User` - Người dùng
- `amount: Double` - Số tiền
- `method: PaymentMethod` - Phương thức
- `status: PaymentStatusEnum` - Trạng thái
- `sepayTransactionId, sepayBankCode, sepayAccountNumber, sepayAccountName, sepayContent, sepayQrCode, sepayResponse: String` - Thông tin SePay
- `createdAt, paidAt, expiredAt: LocalDateTime` - Thời gian
- `failureReason: String` - Lý do thất bại

**Phương thức:**
- `getId(): Long`
- `getPaymentCode(): String`
- `getAmount(): Double`
- `onCreate(): void` - Khởi tạo giá trị mặc định

**Quan hệ:**
- 1-1 với Order
- N-1 với User
- Sử dụng enum PaymentMethod, PaymentStatusEnum

---

### 📊 Module Accounting

#### Class: FinancialTransaction
**Mô tả:** Giao dịch tài chính

**Thuộc tính:**
- `id: Long` - Primary key
- `transactionCode: String` - Mã giao dịch (unique)
- `type: TransactionType` - Loại giao dịch
- `category: TransactionCategory` - Danh mục
- `amount: Double` - Số tiền
- `orderId: Long` - Reference Order
- `supplierId: Long` - Reference Supplier
- `description: String` - Mô tả
- `transactionDate: LocalDateTime` - Ngày giao dịch
- `createdAt: LocalDateTime` - Ngày tạo
- `createdBy: String` - Người tạo

**Phương thức:**
- `getId(): Long`
- `getTransactionCode(): String`
- `getAmount(): Double`
- `onCreate(): void` - Tự động tạo mã

**Quan hệ:**
- Sử dụng enum TransactionType, TransactionCategory
- Reference với Order và Supplier (không dùng FK)

---

#### Class: SupplierPayable
**Mô tả:** Công nợ phải trả nhà cung cấp

**Thuộc tính:**
- `id: Long` - Primary key
- `payableCode: String` - Mã công nợ (unique)
- `supplier: Supplier` - Nhà cung cấp
- `purchaseOrder: PurchaseOrder` - Đơn đặt hàng
- `totalAmount: BigDecimal` - Tổng tiền phải trả
- `paidAmount: BigDecimal` - Đã trả
- `remainingAmount: BigDecimal` - Còn nợ
- `status: PayableStatus` - Trạng thái
- `invoiceDate: LocalDate` - Ngày hóa đơn
- `dueDate: LocalDate` - Ngày hạn thanh toán
- `paymentTermDays: Integer` - Số ngày nợ
- `note: String` - Ghi chú
- `createdAt, updatedAt: LocalDateTime` - Thời gian
- `createdBy: String` - Người tạo

**Phương thức:**
- `getId(): Long`
- `getPayableCode(): String`
- `getRemainingAmount(): BigDecimal`
- `updateStatus(): void` - Tự động cập nhật trạng thái
- `onCreate(): void` - Khởi tạo giá trị
- `onUpdate(): void` - Cập nhật trạng thái

**Quan hệ:**
- N-1 với Supplier
- N-1 với PurchaseOrder
- 1-N với SupplierPayment
- Sử dụng enum PayableStatus

---

#### Class: SupplierPayment
**Mô tả:** Thanh toán cho nhà cung cấp

**Thuộc tính:**
- `id: Long` - Primary key
- `paymentCode: String` - Mã thanh toán (unique)
- `payable: SupplierPayable` - Công nợ
- `amount: BigDecimal` - Số tiền thanh toán
- `paymentDate: LocalDate` - Ngày thanh toán
- `paymentMethod: SupplierPaymentMethod` - Phương thức
- `referenceNumber: String` - Số tham chiếu
- `note: String` - Ghi chú
- `createdAt: LocalDateTime` - Ngày tạo
- `createdBy: String` - Người tạo

**Phương thức:**
- `getId(): Long`
- `getPaymentCode(): String`
- `getAmount(): BigDecimal`
- `onCreate(): void`

**Quan hệ:**
- N-1 với SupplierPayable
- Sử dụng enum SupplierPaymentMethod

---

## 🔗 TỔNG HỢP QUAN HỆ

### Composition (◆—)
Quan hệ mạnh, object con không tồn tại độc lập khi object cha bị xóa:

1. **Cart ◆— CartItem**: Xóa Cart → Xóa tất cả CartItem
2. **Order ◆— OrderItem**: Xóa Order → Xóa tất cả OrderItem
3. **PurchaseOrder ◆— PurchaseOrderItem**: Xóa PO → Xóa tất cả item
4. **ExportOrder ◆— ExportOrderItem**: Xóa phiếu xuất → Xóa tất cả item
5. **WarehouseProduct ◆— ProductDetail**: Xóa sản phẩm kho → Xóa tất cả serial
6. **SupplierPayable ◆— SupplierPayment**: Xóa công nợ → Xóa tất cả thanh toán

### Aggregation (◇—)
Quan hệ yếu, object con có thể tồn tại độc lập:

1. **Category ◇— Product**: Xóa Category → Product vẫn tồn tại (set null)
2. **Supplier ◇— WarehouseProduct**: Xóa Supplier → Sản phẩm vẫn tồn tại
3. **Customer ◇— Order**: Xóa Customer → Order vẫn lưu lại (soft delete)

### Association (—)
Quan hệ tham chiếu thông thường:

1. **User — Customer**: 1-1
2. **User — Employee**: 1-1
3. **Product — ProductImage**: 1-N
4. **Order — Payment**: 1-1
5. **Product — ProductDetail**: 1-1

---

## 📊 BIỂU ĐỒ PHÂN CẤP CLASS

```
Object
│
├── Entity Classes
│   ├── User
│   │   ├── Customer
│   │   └── Employee
│   │
│   ├── Product Domain
│   │   ├── Category
│   │   ├── Product
│   │   └── ProductImage
│   │
│   ├── Order Domain
│   │   ├── Cart
│   │   ├── CartItem
│   │   ├── Order
│   │   └── OrderItem
│   │
│   ├── Inventory Domain
│   │   ├── Supplier
│   │   ├── WarehouseProduct
│   │   ├── ProductDetail
│   │   ├── PurchaseOrder
│   │   ├── PurchaseOrderItem
│   │   ├── ExportOrder
│   │   ├── ExportOrderItem
│   │   ├── InventoryStock
│   │   ├── ProductSpecification
│   │   └── WarehouseProductImage
│   │
│   ├── Payment Domain
│   │   ├── Payment
│   │   └── BankAccount
│   │
│   └── Accounting Domain
│       ├── FinancialTransaction
│       ├── AccountingPeriod
│       ├── SupplierPayable
│       ├── SupplierPayment
│       ├── PaymentReconciliation
│       └── TaxReport
│
└── Enum Classes
    ├── Role
    ├── Status
    ├── Position
    ├── OrderStatus
    ├── PaymentStatus
    ├── PaymentStatusEnum
    ├── PaymentMethod
    ├── ProductStatus
    ├── POStatus
    ├── ExportStatus
    ├── TransactionType
    ├── TransactionCategory
    ├── PeriodStatus
    ├── PayableStatus
    ├── SupplierPaymentMethod
    ├── ReconciliationStatus
    ├── TaxReportType
    └── TaxReportStatus
```

---

## 🎯 DESIGN PATTERNS SỬ DỤNG

### 1. Builder Pattern
Tất cả entity sử dụng `@Builder` annotation của Lombok:
```java
Product product = Product.builder()
    .name("iPhone 15")
    .price(25000000.0)
    .sku("IP15-001")
    .build();
```

### 2. Strategy Pattern
Sử dụng enum để định nghĩa các chiến lược khác nhau:
- `PaymentMethod`: SEPAY, VNPAY, COD
- `TransactionType`: REVENUE, EXPENSE, REFUND

### 3. State Pattern
Quản lý trạng thái của entity:
- `OrderStatus`: PENDING_PAYMENT → CONFIRMED → SHIPPING → DELIVERED
- `PaymentStatus`: PENDING → SUCCESS/FAILED

### 4. Observer Pattern
Sử dụng JPA Lifecycle Callbacks:
- `@PrePersist`: onCreate()
- `@PreUpdate`: onUpdate()

---

## 📝 LƯU Ý THIẾT KẾ

### 1. Separation of Concerns
- **WarehouseProduct**: Quản lý kho (internal)
- **Product**: Hiển thị website (public)
- Liên kết 1-1 để tách biệt logic

### 2. Audit Trail
Các entity quan trọng có:
- `createdAt`: Ngày tạo
- `createdBy`: Người tạo
- `updatedAt`: Ngày cập nhật

### 3. Soft Delete
Sử dụng `active` flag thay vì xóa thật:
- `Product.active`
- `Supplier.active`
- `Category.active`

### 4. Immutable Snapshot
Lưu thông tin tại thời điểm giao dịch:
- `OrderItem.productName`: Tên sản phẩm khi mua
- `OrderItem.price`: Giá tại thời điểm mua
- `CartItem.price`: Giá khi thêm vào giỏ

---

**Tạo bởi:** Kiro AI Assistant  
**Ngày:** 25/12/2024  
**Phiên bản:** 1.0
