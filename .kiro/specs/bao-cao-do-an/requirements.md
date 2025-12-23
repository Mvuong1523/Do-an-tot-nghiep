# Requirements Document - Phân Tích Thiết Kế Hệ Thống TMDT

## Introduction

Tài liệu này mô tả các yêu cầu cho việc phân tích và thiết kế chi tiết hệ thống thương mại điện tử (TMDT) bao gồm quản lý đơn hàng, kho, vận chuyển, kế toán và thanh toán. Hệ thống tích hợp với GHN (Giao Hàng Nhanh) cho vận chuyển và SePay cho thanh toán online.

## Glossary

- **System**: Hệ thống thương mại điện tử (E-commerce System)
- **Order**: Đơn hàng từ khách hàng
- **Warehouse**: Kho hàng, quản lý tồn kho
- **GHN**: Giao Hàng Nhanh - dịch vụ vận chuyển
- **SePay**: Dịch vụ thanh toán online
- **Inventory Transaction**: Phiếu nhập/xuất kho
- **Serial Number**: Mã serial sản phẩm (QR code)
- **Webhook**: Cơ chế callback tự động từ dịch vụ bên ngoài
- **COD**: Cash On Delivery - thanh toán khi nhận hàng
- **Accounting Entry**: Bút toán kế toán
- **Supplier Payable**: Công nợ nhà cung cấp

## Requirements

### Requirement 1: Quản Lý Đơn Hàng Khách Hàng

**User Story:** As a customer, I want to place orders for products, so that I can purchase items and have them delivered to my address.

#### Acceptance Criteria

1. WHEN a customer adds products to cart and proceeds to checkout, THE System SHALL create an order with status PENDING
2. WHEN creating an order, THE System SHALL validate shipping address information including province, district, ward, and detailed address
3. WHEN a customer selects payment method (COD or Online), THE System SHALL store the payment method with the order
4. WHEN an order is created, THE System SHALL generate a unique order code for tracking
5. WHEN order creation fails due to validation errors, THE System SHALL return specific error messages to the customer

### Requirement 2: Xử Lý Trạng Thái Đơn Hàng

**User Story:** As a sales staff member, I want to process orders through different statuses, so that orders move through the fulfillment workflow.

#### Acceptance Criteria

1. WHEN a sales staff confirms an order, THE System SHALL update order status from PENDING to CONFIRMED
2. WHEN a warehouse staff creates an export transaction for an order, THE System SHALL update order status from CONFIRMED to READY_TO_PICK
3. WHEN an order reaches READY_TO_SHIP status, THE System SHALL allow creation of GHN shipping order
4. WHEN an order is cancelled, THE System SHALL update status to CANCELLED and restore inventory if applicable
5. WHEN status transition is invalid (e.g., PENDING to SHIPPING), THE System SHALL reject the update and return an error

### Requirement 3: Quản Lý Nhập Kho

**User Story:** As a warehouse staff member, I want to import products into inventory, so that stock levels are accurately tracked.

#### Acceptance Criteria

1. WHEN warehouse staff imports products via Excel file, THE System SHALL parse product data including name, quantity, price, and optional supplier information
2. WHEN import data is valid, THE System SHALL create an inventory import transaction with status PENDING
3. WHEN an import transaction is completed, THE System SHALL increase product quantity and available quantity
4. WHEN import includes supplier information, THE System SHALL create accounting entries for supplier payables
5. WHEN import data contains errors (missing required fields, invalid format), THE System SHALL reject the import and provide detailed error messages

### Requirement 4: Quản Lý Xuất Kho

**User Story:** As a warehouse staff member, I want to export products for orders, so that items can be prepared for shipping.

#### Acceptance Criteria

1. WHEN warehouse staff creates an export transaction for an order, THE System SHALL validate that sufficient available quantity exists for all order items
2. WHEN creating export transaction, THE System SHALL generate or scan serial numbers (QR codes) for each product unit
3. WHEN an export transaction is completed, THE System SHALL decrease available quantity and update order status to READY_TO_SHIP
4. WHEN insufficient inventory exists, THE System SHALL reject the export and display specific products that are out of stock
5. WHEN export transaction is cancelled, THE System SHALL restore available quantity

### Requirement 5: Tích Hợp GHN Vận Chuyển

**User Story:** As a shipping coordinator, I want to create GHN shipping orders, so that products can be delivered to customers.

#### Acceptance Criteria

1. WHEN an order has status READY_TO_SHIP, THE System SHALL allow creation of GHN shipping order via API
2. WHEN creating GHN order, THE System SHALL send shipping address (province code, district code, ward code), product information, COD amount, and service type
3. WHEN GHN order is created successfully, THE System SHALL store ghnOrderCode and update order status to SHIPPING
4. WHEN GHN API returns error (invalid address, service unavailable), THE System SHALL display error message and keep order in READY_TO_SHIP status
5. WHEN GHN order creation fails due to network timeout, THE System SHALL allow retry without creating duplicate orders

### Requirement 6: Webhook GHN Tracking

**User Story:** As a system administrator, I want to receive GHN status updates automatically, so that order statuses are synchronized in real-time.

#### Acceptance Criteria

1. WHEN GHN sends webhook notification, THE System SHALL authenticate the webhook request using signature verification
2. WHEN webhook indicates delivery success, THE System SHALL update order status to DELIVERED
3. WHEN webhook indicates order return or cancellation, THE System SHALL update order status accordingly and trigger inventory restoration if needed
4. WHEN webhook contains invalid order code, THE System SHALL log the error and return appropriate HTTP status
5. WHEN webhook processing fails, THE System SHALL return HTTP 500 to trigger GHN retry mechanism

### Requirement 7: Kế Toán Tự Động

**User Story:** As an accountant, I want automatic accounting entries for business transactions, so that financial records are accurate and up-to-date.

#### Acceptance Criteria

1. WHEN an order reaches DELIVERED status, THE System SHALL create accounting entry for revenue recognition
2. WHEN a customer completes online payment, THE System SHALL create accounting entry for cash receipt
3. WHEN warehouse imports products with supplier, THE System SHALL create accounting entry for supplier payable
4. WHEN supplier payment is made, THE System SHALL create accounting entry to reduce payable balance
5. WHEN accounting entry creation fails, THE System SHALL log the error and allow manual correction without blocking the business transaction

### Requirement 8: Thanh Toán Online SePay

**User Story:** As a customer, I want to pay online via bank transfer, so that I can complete my order without cash.

#### Acceptance Criteria

1. WHEN a customer selects online payment, THE System SHALL generate QR code with payment information
2. WHEN customer completes bank transfer, SePay SHALL send webhook notification to the System
3. WHEN SePay webhook is received, THE System SHALL verify transaction details and match to order
4. WHEN payment is confirmed, THE System SHALL update order payment status and create accounting entry
5. WHEN payment amount does not match order total, THE System SHALL flag the order for manual review

### Requirement 9: Quản Lý Tồn Kho

**User Story:** As a warehouse manager, I want to track inventory levels, so that I can monitor stock availability and prevent overselling.

#### Acceptance Criteria

1. WHEN products are imported, THE System SHALL increase both quantity and available quantity
2. WHEN export transaction is created, THE System SHALL decrease available quantity but maintain total quantity
3. WHEN export transaction is completed, THE System SHALL decrease total quantity to match available quantity
4. WHEN viewing product inventory, THE System SHALL display both total quantity and available quantity separately
5. WHEN available quantity reaches zero, THE System SHALL prevent new export transactions for that product

### Requirement 10: Phân Quyền Người Dùng

**User Story:** As a system administrator, I want role-based access control, so that users can only access features appropriate to their role.

#### Acceptance Criteria

1. WHEN a user logs in, THE System SHALL authenticate credentials and return JWT token with role information
2. WHEN a user accesses a protected endpoint, THE System SHALL verify JWT token and check role permissions
3. WHEN a user attempts to access unauthorized feature, THE System SHALL return HTTP 403 Forbidden
4. WHEN an admin creates employee account, THE System SHALL assign position (SALES, WAREHOUSE, SHIPPER, ACCOUNTANT) and set initial password
5. WHEN an employee logs in for the first time, THE System SHALL require password change before accessing other features

### Requirement 11: Báo Cáo và Dashboard

**User Story:** As a manager, I want to view reports and dashboards, so that I can monitor business performance.

#### Acceptance Criteria

1. WHEN a user accesses dashboard, THE System SHALL display statistics appropriate to their role (admin sees all, employees see relevant metrics)
2. WHEN generating revenue report, THE System SHALL calculate total revenue from DELIVERED orders within selected date range
3. WHEN generating inventory report, THE System SHALL show current stock levels, import/export history, and low stock alerts
4. WHEN generating accounting report, THE System SHALL display profit/loss, cash flow, and supplier payables
5. WHEN report generation takes longer than 5 seconds, THE System SHALL display loading indicator and allow cancellation

### Requirement 12: Quản Lý Sản Phẩm

**User Story:** As a product manager, I want to manage product catalog, so that customers can browse and purchase items.

#### Acceptance Criteria

1. WHEN creating a product, THE System SHALL require name, category, price, and at least one image
2. WHEN uploading product images, THE System SHALL store images in Cloudinary and save URLs in database
3. WHEN a product supports multiple images, THE System SHALL maintain image order and designate primary image
4. WHEN importing products via Excel, THE System SHALL create products with specifications and link to categories
5. WHEN a product is deleted, THE System SHALL soft-delete (mark as inactive) rather than permanently remove from database

### Requirement 13: Đối Soát Công Nợ

**User Story:** As an accountant, I want to reconcile supplier payables, so that payment obligations are accurately tracked.

#### Acceptance Criteria

1. WHEN viewing supplier payables, THE System SHALL display total outstanding balance per supplier
2. WHEN recording supplier payment, THE System SHALL reduce payable balance and create accounting entry
3. WHEN generating payable report, THE System SHALL show aging analysis (current, 30 days, 60 days, 90+ days)
4. WHEN supplier has both payables and receivables, THE System SHALL display net balance
5. WHEN payment amount exceeds payable balance, THE System SHALL reject the transaction and display error message

### Requirement 14: Multi-Account Banking

**User Story:** As an accountant, I want to manage multiple bank accounts, so that payments can be tracked across different accounts.

#### Acceptance Criteria

1. WHEN configuring SePay integration, THE System SHALL support multiple bank account credentials
2. WHEN receiving payment webhook, THE System SHALL identify which bank account received the payment
3. WHEN viewing bank account list, THE System SHALL display current balance for each account
4. WHEN reconciling transactions, THE System SHALL match payments to specific bank accounts
5. WHEN a bank account is deactivated, THE System SHALL prevent new transactions but maintain historical records

### Requirement 15: Serial Number Tracking

**User Story:** As a warehouse staff member, I want to track products by serial numbers, so that individual items can be identified throughout the supply chain.

#### Acceptance Criteria

1. WHEN creating export transaction, THE System SHALL generate unique serial numbers for each product unit
2. WHEN scanning QR code, THE System SHALL decode serial number and validate against database
3. WHEN a serial number is used in export, THE System SHALL mark it as assigned and prevent reuse
4. WHEN viewing export transaction details, THE System SHALL display all serial numbers included in the shipment
5. WHEN a serial number format is invalid, THE System SHALL reject the scan and display error message
