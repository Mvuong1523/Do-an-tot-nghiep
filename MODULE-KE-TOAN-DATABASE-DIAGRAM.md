# 📊 SƠ ĐỒ DATABASE - MODULE KẾ TOÁN

## 📋 TỔNG QUAN

Module Kế Toán (Accounting) quản lý toàn bộ các giao dịch tài chính, công nợ nhà cung cấp, đối soát thanh toán và báo cáo thuế trong hệ thống TMDT.

## 🗂️ CÁC BẢNG CHÍNH

### 1. **financial_transactions** - Giao Dịch Tài Chính
### 2. **accounting_periods** - Kỳ Kế Toán  
### 3. **supplier_payables** - Công Nợ Phải Trả NCC
### 4. **supplier_payments** - Thanh Toán Cho NCC
### 5. **payment_reconciliation** - Đối Soát Thanh Toán
### 6. **tax_reports** - Báo Cáo Thuế

---

## 🎨 CLASS DIAGRAM (Mermaid)

```mermaid
classDiagram
    %% ========================================
    %% MODULE KẾ TOÁN - ACCOUNTING
    %% ========================================
    
    %% Entity: FinancialTransaction
    class FinancialTransaction {
        -id: Long
        -transactionCode: String
        -type: TransactionType
        -category: TransactionCategory
        -amount: Double
        -orderId: Long
        -supplierId: Long
        -description: String
        -transactionDate: LocalDateTime
        -createdAt: LocalDateTime
        -createdBy: String
    }

    
    %% Entity: AccountingPeriod
    class AccountingPeriod {
        -id: Long
        -name: String
        -startDate: LocalDate
        -endDate: LocalDate
        -status: PeriodStatus
        -totalRevenue: Double
        -totalExpense: Double
        -netProfit: Double
        -discrepancyRate: Double
        -closedAt: LocalDateTime
        -closedBy: String
        -createdAt: LocalDateTime
    }
    
    %% Entity: SupplierPayable
    class SupplierPayable {
        -id: Long
        -payableCode: String
        -supplier: Supplier
        -purchaseOrder: PurchaseOrder
        -totalAmount: BigDecimal
        -paidAmount: BigDecimal
        -remainingAmount: BigDecimal
        -status: PayableStatus
        -invoiceDate: LocalDate
        -dueDate: LocalDate
        -paymentTermDays: Integer
        -note: String
        -createdAt: LocalDateTime
        -updatedAt: LocalDateTime
        -createdBy: String
        +updateStatus(): void
    }

    
    %% Entity: SupplierPayment
    class SupplierPayment {
        -id: Long
        -paymentCode: String
        -payable: SupplierPayable
        -amount: BigDecimal
        -paymentDate: LocalDate
        -paymentMethod: PaymentMethod
        -referenceNumber: String
        -note: String
        -createdAt: LocalDateTime
        -createdBy: String
    }
    
    %% Entity: PaymentReconciliation
    class PaymentReconciliation {
        -id: Long
        -orderId: String
        -transactionId: String
        -gateway: String
        -systemAmount: BigDecimal
        -discrepancy: BigDecimal
        -status: ReconciliationStatus
        -transactionDate: LocalDateTime
        -reconciledAt: LocalDateTime
        -reconciledBy: String
        -note: String
        -createdAt: LocalDateTime
    }

    
    %% Entity: TaxReport
    class TaxReport {
        -id: Long
        -reportCode: String
        -taxType: TaxType
        -periodStart: LocalDate
        -periodEnd: LocalDate
        -taxableRevenue: Double
        -taxRate: Double
        -taxAmount: Double
        -paidAmount: Double
        -remainingTax: Double
        -status: TaxStatus
        -submittedAt: LocalDateTime
        -paidAt: LocalDateTime
        -createdAt: LocalDateTime
        -createdBy: String
    }
    
    %% ========================================
    %% ENUMS - KẾ TOÁN
    %% ========================================
    
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
        OTHER_REVENUE
        OTHER_EXPENSE
    }

    
    class PayableStatus {
        <<enumeration>>
        UNPAID
        PARTIAL
        PAID
        OVERDUE
    }
    
    class PaymentMethod {
        <<enumeration>>
        CASH
        BANK_TRANSFER
        CHECK
    }
    
    class PeriodStatus {
        <<enumeration>>
        OPEN
        CLOSED
    }
    
    class ReconciliationStatus {
        <<enumeration>>
        MATCHED
        MISMATCHED
        MISSING_IN_SYSTEM
        MISSING_IN_GATEWAY
    }
    
    class TaxType {
        <<enumeration>>
        VAT
        CORPORATE_TAX
    }
    
    class TaxStatus {
        <<enumeration>>
        DRAFT
        SUBMITTED
        PAID
    }
    
    class PaymentStatus {
        <<enumeration>>
        PENDING
        PAID
        FAILED
        REFUNDED
    }
    
    class OrderStatus {
        <<enumeration>>
        PENDING
        CONFIRMED
        READY_TO_PICK
        READY_TO_SHIP
        SHIPPING
        DELIVERED
        CANCELLED
    }
    
    class POStatus {
        <<enumeration>>
        PENDING
        RECEIVED
        CANCELLED
    }

    
    %% ========================================
    %% MODULE LIÊN QUAN - INVENTORY
    %% ========================================
    
    class Supplier {
        -id: Long
        -autoCreated: Boolean
        -name: String
        -contactName: String
        -phone: String
        -email: String
        -address: String
        -taxCode: String
        -bankAccount: String
        -paymentTerm: String
        -paymentTermDays: Integer
        -active: Boolean
    }
    
    class PurchaseOrder {
        -id: Long
        -poCode: String
        -supplier: Supplier
        -orderDate: LocalDateTime
        -receivedDate: LocalDateTime
        -status: POStatus
        -createdBy: String
        -note: String
        -items: List~PurchaseOrderItem~
    }

    
    %% ========================================
    %% MODULE LIÊN QUAN - ORDER
    %% ========================================
    
    class Order {
        -id: Long
        -orderCode: String
        -customer: Customer
        -items: List~OrderItem~
        -shippingAddress: String
        -province: String
        -district: String
        -ward: String
        -wardName: String
        -address: String
        -note: String
        -subtotal: Double
        -shippingFee: Double
        -discount: Double
        -total: Double
        -paymentStatus: PaymentStatus
        -paymentMethod: String
        -paymentId: Long
        -status: OrderStatus
        -createdAt: LocalDateTime
        -confirmedAt: LocalDateTime
        -shippedAt: LocalDateTime
        -deliveredAt: LocalDateTime
        -cancelledAt: LocalDateTime
        -cancelReason: String
        -ghnOrderCode: String
        -ghnShippingStatus: String
    }
    
    class Customer {
        -id: Long
        -user: User
        -fullName: String
        -phone: String
        -gender: String
        -birthDate: LocalDate
        -address: String
    }
    
    class OrderItem {
        -id: Long
        -order: Order
        -product: Product
        -productName: String
        -price: Double
        -quantity: Integer
        -subtotal: Double
        -serialNumber: String
        -reserved: Boolean
        -exported: Boolean
    }
    
    class PurchaseOrderItem {
        -id: Long
        -purchaseOrder: PurchaseOrder
        -sku: String
        -warehouseProduct: WarehouseProduct
        -quantity: Long
        -unitCost: Double
        -warrantyMonths: Integer
        -note: String
        -productDetails: List~ProductDetail~
    }

    
    %% ========================================
    %% MỐI QUAN HỆ - RELATIONSHIPS
    %% ========================================
    
    %% Quan hệ trong module Accounting
    FinancialTransaction --> TransactionType : type
    FinancialTransaction --> TransactionCategory : category
    FinancialTransaction ..> Order : orderId (optional)
    FinancialTransaction ..> Supplier : supplierId (optional)
    
    AccountingPeriod --> PeriodStatus : status
    
    SupplierPayable --> Supplier : supplier (ManyToOne)
    SupplierPayable --> PurchaseOrder : purchaseOrder (ManyToOne)
    SupplierPayable --> PayableStatus : status
    
    SupplierPayment --> SupplierPayable : payable (ManyToOne)
    SupplierPayment --> PaymentMethod : paymentMethod
    
    PaymentReconciliation ..> Order : orderId (reference)
    PaymentReconciliation --> ReconciliationStatus : status
    
    TaxReport --> TaxType : taxType
    TaxReport --> TaxStatus : status
    
    %% Quan hệ giữa các module
    PurchaseOrder --> Supplier : supplier (ManyToOne)
    PurchaseOrder *-- PurchaseOrderItem : items (OneToMany - Composition)
    
    PurchaseOrderItem --> PurchaseOrder : purchaseOrder (ManyToOne)
    PurchaseOrderItem --> WarehouseProduct : warehouseProduct (ManyToOne)
    PurchaseOrderItem *-- ProductDetail : productDetails (OneToMany - Composition)
    
    %% Quan hệ từ Order
    Order --> Customer : customer (ManyToOne)
    Order --> PaymentStatus : paymentStatus
    Order --> OrderStatus : status
    Order *-- OrderItem : items (OneToMany - Composition)
    
    OrderItem --> Order : order (ManyToOne)
    OrderItem --> Product : product (ManyToOne)
    
    Customer --> User : user (OneToOne)
```

---


## 📊 CHI TIẾT CÁC BẢNG

### 1. **financial_transactions** - Giao Dịch Tài Chính

Bảng này lưu trữ tất cả các giao dịch tài chính trong hệ thống.

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Primary Key, Auto Increment |
| `transaction_code` | VARCHAR(255) | Mã giao dịch duy nhất (TXN...) |
| `type` | ENUM | Loại giao dịch: REVENUE, EXPENSE, REFUND |
| `category` | ENUM | Danh mục: SALES, SHIPPING, PAYMENT_FEE, TAX, SUPPLIER_PAYMENT, REFUND, OTHER_REVENUE, OTHER_EXPENSE |
| `amount` | DOUBLE | Số tiền giao dịch |
| `order_id` | BIGINT | ID đơn hàng (nếu có) |
| `supplier_id` | BIGINT | ID nhà cung cấp (nếu có) |
| `description` | VARCHAR(1000) | Mô tả giao dịch |
| `transaction_date` | DATETIME | Ngày giao dịch |
| `created_at` | DATETIME | Ngày tạo record |
| `created_by` | VARCHAR(255) | Người tạo |

**Indexes:**
- `idx_transaction_code` ON `transaction_code`
- `idx_type` ON `type`
- `idx_category` ON `category`
- `idx_order_id` ON `order_id`
- `idx_supplier_id` ON `supplier_id`
- `idx_transaction_date` ON `transaction_date`

**Business Rules:**
- Tự động tạo `transaction_code` nếu không có
- `type` = REVENUE: Ghi nhận doanh thu (khi đơn hàng DELIVERED)
- `type` = EXPENSE: Ghi nhận chi phí (thanh toán NCC, phí vận chuyển)
- `type` = REFUND: Hoàn tiền khách hàng


---

### 2. **accounting_periods** - Kỳ Kế Toán

Bảng quản lý các kỳ kế toán (tháng, quý, năm).

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Primary Key, Auto Increment |
| `name` | VARCHAR(255) | Tên kỳ (VD: "Tháng 12/2024") |
| `start_date` | DATE | Ngày bắt đầu kỳ |
| `end_date` | DATE | Ngày kết thúc kỳ |
| `status` | ENUM | Trạng thái: OPEN, CLOSED |
| `total_revenue` | DOUBLE | Tổng doanh thu trong kỳ |
| `total_expense` | DOUBLE | Tổng chi phí trong kỳ |
| `net_profit` | DOUBLE | Lợi nhuận ròng (revenue - expense) |
| `discrepancy_rate` | DOUBLE | Tỷ lệ sai lệch (%) |
| `closed_at` | DATETIME | Thời gian chốt kỳ |
| `closed_by` | VARCHAR(255) | Người chốt kỳ |
| `created_at` | DATETIME | Ngày tạo |

**Indexes:**
- `idx_start_date` ON `start_date`
- `idx_end_date` ON `end_date`
- `idx_status` ON `status`

**Business Rules:**
- Khi `status` = CLOSED: Không thể sửa các giao dịch trong kỳ
- Tự động tính `net_profit` = `total_revenue` - `total_expense`
- `discrepancy_rate` = (Chênh lệch / Tổng doanh thu) * 100


---

### 3. **supplier_payables** - Công Nợ Phải Trả NCC

Bảng quản lý công nợ phải trả cho nhà cung cấp.

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Primary Key, Auto Increment |
| `payable_code` | VARCHAR(255) | Mã công nợ (AP-YYYYMMDD-XXXX) |
| `supplier_id` | BIGINT | Foreign Key → suppliers.id |
| `purchase_order_id` | BIGINT | Foreign Key → purchase_orders.id |
| `total_amount` | DECIMAL(15,2) | Tổng tiền phải trả |
| `paid_amount` | DECIMAL(15,2) | Số tiền đã trả |
| `remaining_amount` | DECIMAL(15,2) | Số tiền còn nợ |
| `status` | ENUM | Trạng thái: UNPAID, PARTIAL, PAID, OVERDUE |
| `invoice_date` | DATE | Ngày hóa đơn (ngày nhập hàng) |
| `due_date` | DATE | Ngày hạn thanh toán |
| `payment_term_days` | INT | Số ngày nợ (từ supplier) |
| `note` | TEXT | Ghi chú |
| `created_at` | DATETIME | Ngày tạo |
| `updated_at` | DATETIME | Ngày cập nhật |
| `created_by` | VARCHAR(255) | Người tạo |

**Indexes:**
- `idx_payable_code` ON `payable_code` (UNIQUE)
- `idx_supplier_id` ON `supplier_id`
- `idx_purchase_order_id` ON `purchase_order_id`
- `idx_status` ON `status`
- `idx_due_date` ON `due_date`

**Business Rules:**
- `remaining_amount` = `total_amount` - `paid_amount`
- Tự động cập nhật `status`:
  - `remaining_amount` = 0 → PAID
  - `paid_amount` > 0 AND `remaining_amount` > 0 → PARTIAL
  - `due_date` < NOW() AND `remaining_amount` > 0 → OVERDUE
  - Còn lại → UNPAID


---

### 4. **supplier_payments** - Thanh Toán Cho NCC

Bảng ghi nhận các lần thanh toán cho nhà cung cấp.

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Primary Key, Auto Increment |
| `payment_code` | VARCHAR(255) | Mã thanh toán (PAY-YYYYMMDD-XXXX) |
| `payable_id` | BIGINT | Foreign Key → supplier_payables.id |
| `amount` | DECIMAL(15,2) | Số tiền thanh toán |
| `payment_date` | DATE | Ngày thanh toán |
| `payment_method` | ENUM | Phương thức: CASH, BANK_TRANSFER, CHECK |
| `reference_number` | VARCHAR(255) | Số tham chiếu (số CK, số séc) |
| `note` | TEXT | Ghi chú |
| `created_at` | DATETIME | Ngày tạo |
| `created_by` | VARCHAR(255) | Người tạo |

**Indexes:**
- `idx_payment_code` ON `payment_code` (UNIQUE)
- `idx_payable_id` ON `payable_id`
- `idx_payment_date` ON `payment_date`

**Business Rules:**
- Khi tạo payment mới:
  1. Cập nhật `paid_amount` trong `supplier_payables`
  2. Tính lại `remaining_amount`
  3. Cập nhật `status` của payable
  4. Tạo `financial_transaction` với type=EXPENSE, category=SUPPLIER_PAYMENT


---

### 5. **payment_reconciliation** - Đối Soát Thanh Toán

Bảng đối soát thanh toán giữa hệ thống và cổng thanh toán (VNPAY, MOMO, ZALOPAY).

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Primary Key, Auto Increment |
| `order_id` | VARCHAR(255) | Mã đơn hàng |
| `transaction_id` | VARCHAR(255) | Mã giao dịch từ gateway |
| `gateway` | VARCHAR(50) | Tên cổng: VNPAY, MOMO, ZALOPAY |
| `system_amount` | DECIMAL(15,2) | Số tiền trong hệ thống |
| `gateway_amount` | DECIMAL(15,2) | Số tiền từ gateway |
| `discrepancy` | DECIMAL(15,2) | Chênh lệch (system - gateway) |
| `status` | ENUM | Trạng thái: MATCHED, MISMATCHED, MISSING_IN_SYSTEM, MISSING_IN_GATEWAY |
| `transaction_date` | DATETIME | Ngày giao dịch |
| `reconciled_at` | DATETIME | Ngày đối soát |
| `reconciled_by` | VARCHAR(255) | Người đối soát |
| `note` | TEXT | Ghi chú |
| `created_at` | DATETIME | Ngày tạo |

**Indexes:**
- `idx_order_id` ON `order_id`
- `idx_transaction_id` ON `transaction_id`
- `idx_status` ON `status`
- `idx_transaction_date` ON `transaction_date`

**Business Rules:**
- `discrepancy` = `system_amount` - `gateway_amount`
- `status` = MATCHED nếu `discrepancy` = 0
- `status` = MISMATCHED nếu `discrepancy` ≠ 0


---

### 6. **tax_reports** - Báo Cáo Thuế

Bảng quản lý báo cáo thuế (VAT, thuế TNDN).

| Cột | Kiểu Dữ Liệu | Mô Tả |
|-----|--------------|-------|
| `id` | BIGINT | Primary Key, Auto Increment |
| `report_code` | VARCHAR(255) | Mã báo cáo (TAX...) |
| `tax_type` | ENUM | Loại thuế: VAT, CORPORATE_TAX |
| `period_start` | DATE | Ngày bắt đầu kỳ |
| `period_end` | DATE | Ngày kết thúc kỳ |
| `taxable_revenue` | DOUBLE | Doanh thu chịu thuế |
| `tax_rate` | DOUBLE | Thuế suất (%) |
| `tax_amount` | DOUBLE | Số thuế phải nộp |
| `paid_amount` | DOUBLE | Số thuế đã nộp |
| `remaining_tax` | DOUBLE | Số thuế còn nợ |
| `status` | ENUM | Trạng thái: DRAFT, SUBMITTED, PAID |
| `submitted_at` | DATETIME | Ngày gửi báo cáo |
| `paid_at` | DATETIME | Ngày nộp thuế |
| `created_at` | DATETIME | Ngày tạo |
| `created_by` | VARCHAR(255) | Người tạo |

**Indexes:**
- `idx_report_code` ON `report_code` (UNIQUE)
- `idx_tax_type` ON `tax_type`
- `idx_status` ON `status`
- `idx_period_start` ON `period_start`
- `idx_period_end` ON `period_end`

**Business Rules:**
- `tax_amount` = `taxable_revenue` * `tax_rate` / 100
- `remaining_tax` = `tax_amount` - `paid_amount`
- VAT: Thuế suất 10%
- CORPORATE_TAX: Thuế suất 20%


---

## 🔗 MỐI QUAN HỆ VỚI CÁC MODULE KHÁC

### 1. **Với Module ORDER (Đơn Hàng)**

```
financial_transactions.order_id → orders.id
payment_reconciliation.order_id → orders.order_code
```

**Luồng nghiệp vụ:**
1. Khi đơn hàng chuyển sang trạng thái `DELIVERED`:
   - Tự động tạo `FinancialTransaction` với:
     - `type` = REVENUE
     - `category` = SALES
     - `amount` = order.total
     - `order_id` = order.id
     - `description` = "Doanh thu từ đơn hàng {orderCode}"

2. Khi khách hàng thanh toán online:
   - Tạo `FinancialTransaction` với:
     - `type` = REVENUE
     - `category` = PAYMENT_FEE (nếu có phí)
     - `amount` = phí thanh toán
     - `order_id` = order.id

3. Đối soát thanh toán:
   - Tạo record trong `payment_reconciliation`
   - So sánh số tiền trong hệ thống vs cổng thanh toán


---

### 2. **Với Module INVENTORY (Kho Hàng)**

```
supplier_payables.supplier_id → suppliers.id
supplier_payables.purchase_order_id → purchase_orders.id
financial_transactions.supplier_id → suppliers.id
```

**Luồng nghiệp vụ:**
1. Khi tạo phiếu nhập hàng (`PurchaseOrder`) với supplier:
   - Tự động tạo `SupplierPayable`:
     - `supplier_id` = supplier.id
     - `purchase_order_id` = po.id
     - `total_amount` = tổng tiền nhập hàng
     - `due_date` = invoice_date + supplier.paymentTermDays
     - `status` = UNPAID

2. Khi thanh toán cho nhà cung cấp:
   - Tạo `SupplierPayment`
   - Cập nhật `paid_amount` và `remaining_amount` trong `SupplierPayable`
   - Tạo `FinancialTransaction` với:
     - `type` = EXPENSE
     - `category` = SUPPLIER_PAYMENT
     - `amount` = số tiền thanh toán
     - `supplier_id` = supplier.id

3. Theo dõi công nợ:
   - Tính tổng công nợ theo supplier
   - Phân tích aging (nợ quá hạn 30, 60, 90 ngày)


---

## 📈 LUỒNG DỮ LIỆU CHÍNH

### Luồng 1: Ghi Nhận Doanh Thu

```
Order (DELIVERED) 
    → Event: OrderStatusChangedEvent
    → OrderEventListener.handleOrderDelivered()
    → Tạo FinancialTransaction (REVENUE, SALES)
    → Cập nhật AccountingPeriod.totalRevenue
```

### Luồng 2: Quản Lý Công Nợ NCC

```
PurchaseOrder (CREATED)
    → Tạo SupplierPayable (UNPAID)
    → SupplierPayment (thanh toán)
    → Cập nhật SupplierPayable (PARTIAL/PAID)
    → Tạo FinancialTransaction (EXPENSE, SUPPLIER_PAYMENT)
    → Cập nhật AccountingPeriod.totalExpense
```

### Luồng 3: Đối Soát Thanh Toán

```
Payment Gateway Webhook
    → Lấy thông tin giao dịch
    → So sánh với Order.total
    → Tạo PaymentReconciliation
    → Cập nhật status (MATCHED/MISMATCHED)
```

### Luồng 4: Báo Cáo Thuế

```
Kết thúc kỳ kế toán
    → Tính tổng doanh thu chịu thuế
    → Tạo TaxReport (VAT 10%, CORPORATE_TAX 20%)
    → Tính tax_amount
    → Gửi báo cáo (SUBMITTED)
    → Nộp thuế (PAID)
```


---

## 🎯 CÁC CHỨC NĂNG CHÍNH

### 1. Quản Lý Giao Dịch Tài Chính
- ✅ Ghi nhận tự động doanh thu khi đơn hàng giao thành công
- ✅ Ghi nhận chi phí thanh toán nhà cung cấp
- ✅ Ghi nhận phí vận chuyển, phí thanh toán
- ✅ Phân loại giao dịch theo type và category
- ✅ Tra cứu giao dịch theo ngày, loại, danh mục

### 2. Quản Lý Công Nợ Nhà Cung Cấp
- ✅ Tự động tạo công nợ khi nhập hàng
- ✅ Theo dõi số tiền đã trả / còn nợ
- ✅ Cảnh báo công nợ quá hạn (OVERDUE)
- ✅ Thanh toán công nợ (toàn bộ hoặc từng phần)
- ✅ Báo cáo aging analysis (30, 60, 90 ngày)

### 3. Đối Soát Thanh Toán
- ✅ So sánh số tiền hệ thống vs cổng thanh toán
- ✅ Phát hiện chênh lệch (MISMATCHED)
- ✅ Xử lý giao dịch thiếu (MISSING_IN_SYSTEM/GATEWAY)
- ✅ Ghi nhận người đối soát và thời gian

### 4. Quản Lý Kỳ Kế Toán
- ✅ Tạo kỳ kế toán (tháng, quý, năm)
- ✅ Tính tổng doanh thu, chi phí, lợi nhuận
- ✅ Chốt kỳ kế toán (CLOSED)
- ✅ Tính tỷ lệ sai lệch

### 5. Báo Cáo Thuế
- ✅ Tạo báo cáo thuế VAT (10%)
- ✅ Tạo báo cáo thuế TNDN (20%)
- ✅ Tính số thuế phải nộp
- ✅ Theo dõi số thuế đã nộp / còn nợ
- ✅ Quản lý trạng thái báo cáo (DRAFT, SUBMITTED, PAID)


---

## 📊 THỐNG KÊ DATABASE

| Thông Tin | Giá Trị |
|-----------|---------|
| **Tổng số bảng** | 6 bảng chính |
| **Tổng số enum** | 8 enums |
| **Foreign Keys** | 4 FKs |
| **Indexes** | ~25 indexes |
| **Triggers** | 0 (sử dụng @PrePersist, @PreUpdate) |

---

## 🔐 PHÂN QUYỀN TRUY CẬP

| Role | Quyền Truy Cập |
|------|----------------|
| **ADMIN** | Full access (tất cả chức năng) |
| **ACCOUNTANT** | - Xem/tạo/sửa giao dịch tài chính<br>- Quản lý công nợ NCC<br>- Đối soát thanh toán<br>- Quản lý kỳ kế toán<br>- Báo cáo thuế |
| **WAREHOUSE_MANAGER** | - Xem công nợ NCC<br>- Xem giao dịch liên quan đến nhập hàng |
| **SALES** | - Xem giao dịch liên quan đến đơn hàng |

---

## 📝 GHI CHÚ QUAN TRỌNG

### 1. Tự Động Hóa
- **Event-Driven**: Sử dụng Spring Events để tự động tạo giao dịch tài chính
- **@PrePersist**: Tự động tạo mã code, set giá trị mặc định
- **@PreUpdate**: Tự động cập nhật status, tính toán lại số liệu

### 2. Tính Toàn Vẹn Dữ Liệu
- Sử dụng `BigDecimal` cho các trường tiền tệ (độ chính xác cao)
- Foreign Key constraints đảm bảo tham chiếu hợp lệ
- Unique constraints trên các mã code

### 3. Performance
- Indexes trên các cột thường xuyên query
- Lazy loading cho các relationship
- Pagination cho danh sách lớn

### 4. Audit Trail
- Lưu `created_by`, `created_at` cho mọi record
- Lưu `updated_at` cho các bảng quan trọng
- Không xóa vật lý, chỉ đánh dấu inactive

---

## 🚀 HƯỚNG PHÁT TRIỂN

### Tính Năng Cần Bổ Sung
1. ❌ **Journal Entries** - Bút toán kế toán chi tiết
2. ❌ **Chart of Accounts** - Hệ thống tài khoản kế toán
3. ❌ **Budget Management** - Quản lý ngân sách
4. ❌ **Cash Flow Statement** - Báo cáo lưu chuyển tiền tệ
5. ❌ **Profit & Loss Statement** - Báo cáo lãi lỗ chi tiết
6. ❌ **Balance Sheet** - Bảng cân đối kế toán
7. ❌ **Multi-Currency Support** - Hỗ trợ đa tiền tệ
8. ❌ **Approval Workflow** - Quy trình phê duyệt thanh toán

---

**Tài liệu này được tạo tự động từ source code**  
**Ngày tạo:** 2024-12-25  
**Version:** 1.0
