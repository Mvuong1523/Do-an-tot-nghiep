# Sequence Diagram - Module Quản Lý Đơn Hàng (Order Management Module)

## Tổng quan
Tài liệu này mô tả chi tiết các luồng nghiệp vụ quản lý đơn hàng từ góc nhìn người dùng (Frontend) tương tác với các layer Backend theo đúng code thực tế.

## Kiến trúc Backend
```
Frontend → Controller → Service → Repository → Entity (JPA/Hibernate) → Database
```

## Các Entity trong hệ thống
1. **Order** - Đơn hàng
2. **OrderItem** - Chi tiết sản phẩm trong đơn hàng
3. **Customer** - Khách hàng (từ module auth)
4. **Product** - Sản phẩm (từ module product)

## Các Enum
- **OrderStatus**: PENDING_PAYMENT, CONFIRMED, READY_TO_SHIP, PICKED_UP, SHIPPING, DELIVERY_FAILED, DELIVERED, COMPLETED, CANCELLED, RETURNED
- **PaymentStatus**: UNPAID, PENDING, PAID, FAILED, REFUNDED

## Các chức năng chính
1. Xem danh sách đơn hàng (Admin/Employee)
2. Xem chi tiết đơn hàng
3. Tạo đơn hàng (Customer)
4. Xác nhận đơn hàng (Admin/Sales)
5. Cập nhật trạng thái đơn hàng
6. Hủy đơn hàng
7. Theo dõi vận chuyển
8. Thống kê đơn hàng

---

## 1. XEM DANH SÁCH ĐỐN HÀNG (Admin/Employee)

### Mô tả
Người dùng (Admin/Employee) truy cập trang quản lý đơn hàng để xem danh sách tất cả đơn hàng với các bộ lọc.

### Frontend: `/admin/orders/page.tsx` hoặc `/employee/orders/page.tsx`

### Sequence Diagram

```mermaid
sequenceDiagram
    actor User as 👤 User<br/>(Admin/Employee)
    participant FE as 🖥️ Frontend<br/>page.tsx
    participant Ctrl as 🔌 OrderManagement<br/>Controller
    participant Svc as ⚙️ Order<br/>Service
    participant Repo as 💾 Order<br/>Repository
    participant Entity as 📦 Order<br/>Entity
    
    User->>FE: Truy cập /admin/orders hoặc /employee/orders
    FE->>FE: Kiểm tra auth từ localStorage
    FE->>FE: Kiểm tra quyền (Admin/Employee)
    
    alt Không có quyền
        FE->>User: Redirect đến /login hoặc /
    else Có quyền
        
        Note over FE,Entity: Load danh sách đơn hàng
        FE->>Ctrl: GET /api/admin/orders?status=ALL&page=0&size=20
        Ctrl->>Svc: getAllOrders(status, page, size)
        
        alt status == "ALL"
            Svc->>Repo: findAll(PageRequest.of(page, size))
            Repo->>Entity: Load Order entities<br/>ORDER BY createdAt DESC<br/>LIMIT 20 OFFSET 0
        else status != "ALL"
            Svc->>Repo: findByStatus(OrderStatus.valueOf(status), pageable)
            Repo->>Entity: Load Order entities<br/>WHERE status = ?<br/>ORDER BY createdAt DESC
        end
        
        Entity-->>Repo: Page<Order>
        Repo-->>Svc: Page<Order>
        
        Svc->>Svc: Convert to OrderResponse DTO:<br/>- Map Order fields<br/>- Get customer info<br/>- Count items
        
        Svc-->>Ctrl: ApiResponse.success(orders)
        Ctrl-->>FE: {success: true, data: [...]}
        
        Note over FE,Entity: Load thống kê
        FE->>Ctrl: GET /api/admin/orders/statistics
        Ctrl->>Svc: getOrderStatistics()
        
        Svc->>Repo: countByStatus(PENDING)
        Repo->>Entity: COUNT Order WHERE status='PENDING'
        Entity-->>Repo: count
        Repo-->>Svc: pendingCount
        
        Svc->>Repo: countByStatus(CONFIRMED)
        Repo->>Entity: COUNT Order WHERE status='CONFIRMED'
        Entity-->>Repo: count
        Repo-->>Svc: confirmedCount
        
        Svc->>Repo: countByStatus(SHIPPING)
        Repo->>Entity: COUNT Order WHERE status='SHIPPING'
        Entity-->>Repo: count
        Repo-->>Svc: shippingCount
        
        Svc->>Repo: countByStatus(DELIVERED)
        Repo->>Entity: COUNT Order WHERE status='DELIVERED'
        Entity-->>Repo: count
        Repo-->>Svc: deliveredCount
        
        Svc->>Repo: sumTotalByDateRange(startDate, endDate)
        Repo->>Entity: SUM(total) FROM Order<br/>WHERE createdAt BETWEEN ? AND ?<br/>AND paymentStatus='PAID'
        Entity-->>Repo: totalRevenue
        Repo-->>Svc: totalRevenue
        
        Svc-->>Ctrl: ApiResponse.success(stats)
        Ctrl-->>FE: {success: true, data: {...}}
        
        FE->>User: ✅ Hiển thị:<br/>📊 Statistics cards<br/>🔍 Filter tabs<br/>📋 Bảng đơn hàng
    end
```

---

## 2. XEM CHI TIẾT ĐƠN HÀNG

### Mô tả
Người dùng click vào một đơn hàng để xem thông tin chi tiết bao gồm thông tin khách hàng, sản phẩm, thanh toán và lịch sử.

### Frontend: `/admin/orders/[id]/page.tsx` hoặc `/employee/orders/[id]/page.tsx`

```mermaid
sequenceDiagram
    actor User as 👤 User
    participant FE as 🖥️ Frontend
    participant Ctrl as 🔌 OrderManagement<br/>Controller
    participant Svc as ⚙️ Order<br/>Service
    participant Repo as 💾 Order<br/>Repository
    participant Entity as 📦 Order<br/>Entity
    participant ItemEntity as 📦 OrderItem<br/>Entity
    
    User->>FE: Click "Chi tiết" hoặc truy cập /orders/{id}
    FE->>FE: Kiểm tra quyền
    
    FE->>Ctrl: GET /api/admin/orders/{orderId}
    Ctrl->>Svc: getOrderById(orderId)
    Svc->>Repo: findById(orderId)
    Repo->>Entity: Load Order entity by ID<br/>JOIN FETCH customer<br/>JOIN FETCH items
    Entity->>ItemEntity: Load OrderItem entities
    ItemEntity-->>Entity: List<OrderItem>
    Entity-->>Repo: Optional<Order>
    Repo-->>Svc: Optional<Order>
    
    alt Order not found
        Svc-->>Ctrl: ApiResponse.error("Không tìm thấy đơn hàng")
        Ctrl-->>FE: {success: false}
        FE->>User: ❌ Toast error + Redirect
    else Order found
        Svc->>Svc: Build OrderResponse:<br/>- orderId, orderCode<br/>- customerName, customerPhone, customerEmail<br/>- shippingAddress, province, district, ward<br/>- subtotal, shippingFee, discount, total<br/>- status, paymentStatus, paymentMethod<br/>- createdAt, confirmedAt, shippedAt, deliveredAt<br/>- ghnOrderCode, ghnShippingStatus<br/>- items: List<OrderItemResponse>
        
        Svc-->>Ctrl: ApiResponse.success(orderResponse)
        Ctrl-->>FE: {success: true, data: {...}}
        
        FE->>User: ✅ Hiển thị:<br/>📋 Status card<br/>👤 Thông tin khách hàng<br/>📦 Danh sách sản phẩm<br/>💰 Tổng quan thanh toán<br/>📅 Lịch sử đơn hàng<br/>🚚 Thông tin vận chuyển (nếu có)
    end
```

---

## 3. TẠO ĐƠN HÀNG (Customer)

### Mô tả
Khách hàng tạo đơn hàng từ giỏ hàng, nhập thông tin giao hàng và chọn phương thức thanh toán.

### Frontend: Customer checkout page

```mermaid
sequenceDiagram
    actor Customer as 👤 Customer
    participant FE as 🖥️ Frontend
    participant Ctrl as 🔌 Order<br/>Controller
    participant Svc as ⚙️ Order<br/>Service
    participant Repo as 💾 Order<br/>Repository
    participant CartRepo as 💾 Cart<br/>Repository
    participant Entity as 📦 Order<br/>Entity
    participant ItemEntity as 📦 OrderItem<br/>Entity
    
    Customer->>FE: Nhập thông tin giao hàng
    Customer->>FE: Chọn phương thức thanh toán
    Customer->>FE: Click "Đặt hàng"
    
    FE->>FE: Validate form data
    FE->>Ctrl: POST /api/orders<br/>Body: CreateOrderRequest {<br/>  shippingAddress, province, district, ward,<br/>  paymentMethod, note<br/>}
    Ctrl->>Ctrl: @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    Ctrl->>Ctrl: Authentication.getName() → email
    Ctrl->>Svc: getCustomerIdByEmail(email)
    Svc-->>Ctrl: customerId
    
    Ctrl->>Svc: createOrderFromCart(customerId, request)
    
    Note over Svc,CartRepo: 1. Load giỏ hàng
    Svc->>CartRepo: findByCustomerId(customerId)
    CartRepo-->>Svc: List<CartItem>
    
    alt Giỏ hàng trống
        Svc-->>Ctrl: ApiResponse.error("Giỏ hàng trống")
        Ctrl-->>FE: {success: false}
        FE->>Customer: ❌ Toast error
    else Giỏ hàng có sản phẩm
        
        Note over Svc,Entity: 2. Tạo Order entity
        Svc->>Svc: Generate orderCode = "ORD" + timestamp
        Svc->>Svc: Calculate:<br/>- subtotal = sum(item.price * item.quantity)<br/>- shippingFee = calculateShippingFee()<br/>- discount = 0<br/>- total = subtotal + shippingFee - discount
        
        Svc->>Entity: Order.builder()<br/>.orderCode(orderCode)<br/>.customer(customer)<br/>.shippingAddress(request.getShippingAddress())<br/>.province(request.getProvince())<br/>.district(request.getDistrict())<br/>.ward(request.getWard())<br/>.note(request.getNote())<br/>.subtotal(subtotal)<br/>.shippingFee(shippingFee)<br/>.discount(discount)<br/>.total(total)<br/>.paymentMethod(request.getPaymentMethod())<br/>.paymentStatus(PaymentStatus.UNPAID)<br/>.status(OrderStatus.PENDING_PAYMENT)<br/>.build()
        
        Entity->>Entity: @PrePersist:<br/>createdAt = LocalDateTime.now()<br/>if (status == null) status = PENDING_PAYMENT<br/>if (paymentStatus == null) paymentStatus = UNPAID
        
        Note over Svc,ItemEntity: 3. Tạo OrderItem entities
        loop For each CartItem
            Svc->>ItemEntity: OrderItem.builder()<br/>.order(order)<br/>.product(cartItem.getProduct())<br/>.productName(product.getName())<br/>.price(product.getPrice())<br/>.quantity(cartItem.getQuantity())<br/>.subtotal(price * quantity)<br/>.reserved(false)<br/>.exported(false)<br/>.build()
        end
        
        Svc->>Repo: save(order)
        Repo->>Entity: Persist Order entity with OrderItem entities
        Entity-->>Repo: Order (with ID)
        Repo-->>Svc: Order
        
        Note over Svc,CartRepo: 4. Xóa giỏ hàng
        Svc->>CartRepo: deleteByCustomerId(customerId)
        CartRepo-->>Svc: void
        
        Svc-->>Ctrl: ApiResponse.success(order)
        Ctrl-->>FE: {success: true, data: {...}}
        
        FE->>Customer: ✅ Toast: "Đặt hàng thành công"<br/>Redirect đến trang thanh toán hoặc đơn hàng
    end
```

---

## 4. XÁC NHẬN ĐƠN HÀNG (Admin/Sales)

### Mô tả
Nhân viên bán hàng hoặc Admin xác nhận đơn hàng sau khi kiểm tra thông tin và thanh toán.

### Frontend: Admin/Employee orders page

```mermaid
sequenceDiagram
    actor User as 👤 User<br/>(Admin/Sales)
    participant FE as 🖥️ Frontend
    participant Ctrl as 🔌 OrderManagement<br/>Controller
    participant Svc as ⚙️ Order<br/>Service
    participant Repo as 💾 Order<br/>Repository
    participant Entity as 📦 Order<br/>Entity
    
    User->>FE: Click nút "Xác nhận" trên đơn PENDING
    FE->>User: Confirm dialog
    User->>FE: Xác nhận
    
    FE->>Ctrl: PUT /api/admin/orders/{orderId}/status?status=CONFIRMED
    Ctrl->>Svc: updateOrderStatus(orderId, "CONFIRMED")
    
    Svc->>Repo: findById(orderId)
    Repo->>Entity: Load Order entity
    Entity-->>Repo: Optional<Order>
    Repo-->>Svc: Optional<Order>
    
    alt Order not found
        Svc-->>Ctrl: ApiResponse.error("Không tìm thấy đơn hàng")
        Ctrl-->>FE: {success: false}
        FE->>User: ❌ Toast error
    else Order found
        Svc->>Svc: Validate current status<br/>(Chỉ cho phép từ PENDING_PAYMENT)
        
        alt Invalid status transition
            Svc-->>Ctrl: ApiResponse.error("Không thể cập nhật trạng thái")
            Ctrl-->>FE: {success: false}
            FE->>User: ❌ Toast error
        else Valid transition
            Svc->>Entity: order.setStatus(OrderStatus.CONFIRMED)<br/>order.setConfirmedAt(LocalDateTime.now())
            
            Svc->>Repo: save(order)
            Repo->>Entity: Update Order entity
            Entity-->>Repo: Order
            Repo-->>Svc: Order
            
            Svc-->>Ctrl: ApiResponse.success(order)
            Ctrl-->>FE: {success: true, data: {...}}
            
            FE->>User: ✅ Toast: "Đã xác nhận đơn hàng"
            FE->>FE: Reload danh sách đơn hàng
        end
    end
```

---

## 5. XUẤT KHO VÀ CHUYỂN SANG READY_TO_SHIP

### Mô tả
Sau khi đơn hàng được xác nhận (CONFIRMED), nhân viên kho xuất hàng. Khi xuất kho thành công, đơn hàng tự động chuyển sang READY_TO_SHIP.

### Frontend: Warehouse export page

```mermaid
sequenceDiagram
    actor User as 👤 User<br/>(Warehouse Staff)
    participant FE as 🖥️ Frontend<br/>Warehouse
    participant WCtrl as 🔌 ExportOrder<br/>Controller
    participant WSvc as ⚙️ ExportOrder<br/>Service
    participant ORepo as 💾 Order<br/>Repository
    participant OEntity as 📦 Order<br/>Entity
    participant ERepo as 💾 ExportOrder<br/>Repository
    participant EEntity as 📦 ExportOrder<br/>Entity
    
    User->>FE: Truy cập trang xuất kho
    FE->>WCtrl: GET /api/warehouse/orders/pending-export
    WCtrl->>WSvc: getOrdersPendingExport()
    WSvc->>ORepo: findByStatusAndNotExported(CONFIRMED)
    ORepo->>OEntity: Load Order entities<br/>WHERE status='CONFIRMED'<br/>AND NOT EXISTS (SELECT 1 FROM ExportOrder WHERE orderId = Order.id)
    OEntity-->>ORepo: List<Order>
    ORepo-->>WSvc: List<Order>
    WSvc-->>WCtrl: ApiResponse.success(orders)
    WCtrl-->>FE: {success: true, data: [...]}
    
    FE->>User: Hiển thị danh sách đơn chờ xuất kho
    
    User->>FE: Click "Xuất kho" cho đơn hàng
    FE->>User: Hiển thị modal xác nhận
    User->>FE: Xác nhận xuất kho
    
    FE->>WCtrl: POST /api/warehouse/export<br/>Body: {orderId, items: [...]}
    WCtrl->>WSvc: createExportOrder(request)
    
    Note over WSvc,OEntity: 1. Kiểm tra Order
    WSvc->>ORepo: findById(orderId)
    ORepo->>OEntity: Load Order entity
    OEntity-->>ORepo: Optional<Order>
    ORepo-->>WSvc: Optional<Order>
    
    WSvc->>WSvc: Validate status == CONFIRMED
    
    Note over WSvc,EEntity: 2. Tạo ExportOrder
    WSvc->>EEntity: ExportOrder.builder()<br/>.orderId(orderId)<br/>.exportCode("EXP" + timestamp)<br/>.status("COMPLETED")<br/>.createdBy(username)<br/>.build()
    
    WSvc->>ERepo: save(exportOrder)
    ERepo->>EEntity: Persist ExportOrder entity
    EEntity-->>ERepo: ExportOrder
    ERepo-->>WSvc: ExportOrder
    
    Note over WSvc,OEntity: 3. Cập nhật Order status
    WSvc->>OEntity: order.setStatus(OrderStatus.READY_TO_SHIP)
    WSvc->>ORepo: save(order)
    ORepo->>OEntity: Update Order entity
    OEntity-->>ORepo: Order
    ORepo-->>WSvc: Order
    
    WSvc-->>WCtrl: ApiResponse.success(exportOrder)
    WCtrl-->>FE: {success: true}
    
    FE->>User: ✅ Toast: "Xuất kho thành công"<br/>Đơn hàng chuyển sang READY_TO_SHIP
```

---

## 6. CHUYỂN SANG ĐANG GIAO HÀNG (SHIPPING)

### Mô tả
Sau khi đơn hàng ở trạng thái READY_TO_SHIP, nhân viên bán hàng cập nhật sang SHIPPING khi tài xế đã lấy hàng.

### Frontend: Admin/Employee orders page

```mermaid
sequenceDiagram
    actor User as 👤 User<br/>(Sales/Admin)
    participant FE as 🖥️ Frontend
    participant Ctrl as 🔌 OrderManagement<br/>Controller
    participant Svc as ⚙️ Order<br/>Service
    participant Repo as 💾 Order<br/>Repository
    participant Entity as 📦 Order<br/>Entity
    
    User->>FE: Click "Đang giao" trên đơn READY_TO_SHIP
    FE->>User: Confirm dialog
    User->>FE: Xác nhận
    
    FE->>Ctrl: PUT /api/admin/orders/{orderId}/mark-shipping-from-ready
    Ctrl->>Svc: markShippingFromReady(orderId)
    
    Svc->>Repo: findById(orderId)
    Repo->>Entity: Load Order entity
    Entity-->>Repo: Optional<Order>
    Repo-->>Svc: Optional<Order>
    
    alt Order not found
        Svc-->>Ctrl: ApiResponse.error("Không tìm thấy đơn hàng")
        Ctrl-->>FE: {success: false}
        FE->>User: ❌ Toast error
    else Order found
        Svc->>Svc: Validate status == READY_TO_SHIP
        
        alt Status != READY_TO_SHIP
            Svc-->>Ctrl: ApiResponse.error("Chỉ cho phép từ READY_TO_SHIP")
            Ctrl-->>FE: {success: false}
            FE->>User: ❌ Toast error
        else Status == READY_TO_SHIP
            Svc->>Entity: order.setStatus(OrderStatus.SHIPPING)<br/>order.setShippedAt(LocalDateTime.now())
            
            Svc->>Repo: save(order)
            Repo->>Entity: Update Order entity
            Entity-->>Repo: Order
            Repo-->>Svc: Order
            
            Svc-->>Ctrl: ApiResponse.success(order)
            Ctrl-->>FE: {success: true}
            
            FE->>User: ✅ Toast: "Đã chuyển sang đang giao hàng"
            FE->>FE: Reload danh sách
        end
    end
```

---

## 7. XÁC NHẬN ĐÃ GIAO HÀNG (DELIVERED)

### Mô tả
Khi đơn hàng đã được giao thành công, nhân viên cập nhật trạng thái sang DELIVERED.

### Frontend: Admin/Employee orders page

```mermaid
sequenceDiagram
    actor User as 👤 User<br/>(Sales/Admin)
    participant FE as 🖥️ Frontend
    participant Ctrl as 🔌 OrderManagement<br/>Controller
    participant Svc as ⚙️ Order<br/>Service
    participant Repo as 💾 Order<br/>Repository
    participant Entity as 📦 Order<br/>Entity
    
    User->>FE: Click "Đã giao" trên đơn SHIPPING
    FE->>User: Confirm dialog
    User->>FE: Xác nhận
    
    FE->>Ctrl: PUT /api/admin/orders/{orderId}/delivered
    Ctrl->>Svc: markAsDelivered(orderId)
    
    Svc->>Repo: findById(orderId)
    Repo->>Entity: Load Order entity
    Entity-->>Repo: Optional<Order>
    Repo-->>Svc: Optional<Order>
    
    alt Order not found
        Svc-->>Ctrl: ApiResponse.error("Không tìm thấy đơn hàng")
        Ctrl-->>FE: {success: false}
        FE->>User: ❌ Toast error
    else Order found
        Svc->>Svc: Validate status == SHIPPING
        
        alt Status != SHIPPING
            Svc-->>Ctrl: ApiResponse.error("Chỉ cho phép từ SHIPPING")
            Ctrl-->>FE: {success: false}
            FE->>User: ❌ Toast error
        else Status == SHIPPING
            Svc->>Entity: order.setStatus(OrderStatus.DELIVERED)<br/>order.setDeliveredAt(LocalDateTime.now())
            
            Svc->>Repo: save(order)
            Repo->>Entity: Update Order entity
            Entity-->>Repo: Order
            Repo-->>Svc: Order
            
            Svc-->>Ctrl: ApiResponse.success(order)
            Ctrl-->>FE: {success: true}
            
            FE->>User: ✅ Toast: "Đã xác nhận giao hàng thành công"
            FE->>FE: Reload danh sách
        end
    end
```

---

## 8. HỦY ĐƠN HÀNG

### 8.1 Hủy bởi Customer

```mermaid
sequenceDiagram
    actor Customer as 👤 Customer
    participant FE as 🖥️ Frontend
    participant Ctrl as 🔌 Order<br/>Controller
    participant Svc as ⚙️ Order<br/>Service
    participant Repo as 💾 Order<br/>Repository
    participant Entity as 📦 Order<br/>Entity
    
    Customer->>FE: Click "Hủy đơn" trên đơn của mình
    FE->>Customer: Hiển thị modal nhập lý do
    Customer->>FE: Nhập lý do và xác nhận
    
    FE->>Ctrl: PUT /api/orders/{orderId}/cancel?reason=...
    Ctrl->>Ctrl: @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    Ctrl->>Ctrl: Authentication.getName() → email
    Ctrl->>Svc: getCustomerIdByEmail(email)
    Svc-->>Ctrl: customerId
    
    Ctrl->>Svc: cancelOrderByCustomer(orderId, customerId, reason)
    
    Svc->>Repo: findById(orderId)
    Repo->>Entity: Load Order entity
    Entity-->>Repo: Optional<Order>
    Repo-->>Svc: Optional<Order>
    
    alt Order not found
        Svc-->>Ctrl: ApiResponse.error("Không tìm thấy đơn hàng")
        Ctrl-->>FE: {success: false}
        FE->>Customer: ❌ Toast error
    else Order found
        Svc->>Svc: Validate:<br/>- order.customer.id == customerId<br/>- status in [PENDING_PAYMENT, CONFIRMED]
        
        alt Không có quyền hoặc không thể hủy
            Svc-->>Ctrl: ApiResponse.error("Không thể hủy đơn hàng")
            Ctrl-->>FE: {success: false}
            FE->>Customer: ❌ Toast error
        else Có thể hủy
            Svc->>Entity: order.setStatus(OrderStatus.CANCELLED)<br/>order.setCancelledAt(LocalDateTime.now())<br/>order.setCancelReason(reason)
            
            Svc->>Repo: save(order)
            Repo->>Entity: Update Order entity
            Entity-->>Repo: Order
            Repo-->>Svc: Order
            
            Svc-->>Ctrl: ApiResponse.success(order)
            Ctrl-->>FE: {success: true}
            
            FE->>Customer: ✅ Toast: "Đã hủy đơn hàng"
        end
    end
```

### 8.2 Hủy bởi Admin/Staff

```mermaid
sequenceDiagram
    actor User as 👤 User<br/>(Admin/Staff)
    participant FE as 🖥️ Frontend
    participant Ctrl as 🔌 OrderManagement<br/>Controller
    participant Svc as ⚙️ Order<br/>Service
    participant Repo as 💾 Order<br/>Repository
    participant Entity as 📦 Order<br/>Entity
    
    User->>FE: Click "Hủy đơn"
    FE->>User: Hiển thị modal nhập lý do
    User->>FE: Nhập lý do và xác nhận
    
    FE->>Ctrl: PUT /api/admin/orders/{orderId}/cancel?reason=...
    Ctrl->>Svc: cancelOrder(orderId, reason)
    
    Svc->>Repo: findById(orderId)
    Repo->>Entity: Load Order entity
    Entity-->>Repo: Optional<Order>
    Repo-->>Svc: Optional<Order>
    
    alt Order not found
        Svc-->>Ctrl: ApiResponse.error("Không tìm thấy đơn hàng")
        Ctrl-->>FE: {success: false}
        FE->>User: ❌ Toast error
    else Order found
        Svc->>Svc: Validate status != DELIVERED
        
        alt Status == DELIVERED
            Svc-->>Ctrl: ApiResponse.error("Không thể hủy đơn đã giao")
            Ctrl-->>FE: {success: false}
            FE->>User: ❌ Toast error
        else Có thể hủy
            Svc->>Entity: order.setStatus(OrderStatus.CANCELLED)<br/>order.setCancelledAt(LocalDateTime.now())<br/>order.setCancelReason(reason)
            
            Svc->>Repo: save(order)
            Repo->>Entity: Update Order entity
            Entity-->>Repo: Order
            Repo-->>Svc: Order
            
            Svc-->>Ctrl: ApiResponse.success(order)
            Ctrl-->>FE: {success: true}
            
            FE->>User: ✅ Toast: "Đã hủy đơn hàng"
        end
    end
```

---

## 9. THEO DÕI VẬN CHUYỂN GHN

### Mô tả
Xem trạng thái vận chuyển từ GHN (Giao Hàng Nhanh) cho đơn hàng đã tạo vận đơn.

### Frontend: Order detail page

```mermaid
sequenceDiagram
    actor User as 👤 User
    participant FE as 🖥️ Frontend
    participant Ctrl as 🔌 OrderManagement<br/>Controller
    participant Svc as ⚙️ Order<br/>Service
    participant Repo as 💾 Order<br/>Repository
    participant Entity as 📦 Order<br/>Entity
    participant GHN as 🚚 GHN API
    
    User->>FE: Click "Cập nhật" trạng thái vận chuyển
    
    FE->>Ctrl: GET /api/admin/orders/{orderId}/shipping-status
    Ctrl->>Svc: getShippingStatusAdmin(orderId)
    
    Svc->>Repo: findById(orderId)
    Repo->>Entity: Load Order entity
    Entity-->>Repo: Optional<Order>
    Repo-->>Svc: Optional<Order>
    
    alt Order not found
        Svc-->>Ctrl: ApiResponse.error("Không tìm thấy đơn hàng")
        Ctrl-->>FE: {success: false}
        FE->>User: ❌ Toast error
    else Order found
        alt Không có mã vận đơn GHN
            Svc-->>Ctrl: ApiResponse.error("Đơn hàng chưa có mã vận đơn")
            Ctrl-->>FE: {success: false}
            FE->>User: ❌ Toast: "Chưa tạo vận đơn GHN"
        else Có mã vận đơn
            Note over Svc,GHN: Gọi GHN API
            Svc->>GHN: GET /v2/shipping-order/detail<br/>order_code={ghnOrderCode}
            GHN-->>Svc: {<br/>  status: "delivering",<br/>  expected_delivery_time: "...",<br/>  log: [...]<br/>}
            
            Svc->>Entity: order.setGhnShippingStatus(status)<br/>order.setGhnExpectedDeliveryTime(expectedTime)
            Svc->>Repo: save(order)
            Repo->>Entity: Update Order entity
            Entity-->>Repo: Order
            Repo-->>Svc: Order
            
            Svc->>Svc: Build ShippingStatusResponse:<br/>- ghnOrderCode<br/>- status<br/>- expectedDeliveryTime<br/>- log: List<StatusLog>
            
            Svc-->>Ctrl: ApiResponse.success(shippingStatus)
            Ctrl-->>FE: {success: true, data: {...}}
            
            FE->>User: ✅ Hiển thị:<br/>📦 Mã vận đơn<br/>🚚 Trạng thái hiện tại<br/>📅 Thời gian dự kiến<br/>📋 Lịch sử vận chuyển
        end
    end
```

---

## 10. THỐNG KÊ ĐƠN HÀNG

### Mô tả
Xem thống kê tổng quan về đơn hàng theo trạng thái và doanh thu.

### Frontend: Dashboard hoặc Orders page

```mermaid
sequenceDiagram
    actor User as 👤 User<br/>(Admin/Employee)
    participant FE as 🖥️ Frontend
    participant Ctrl as 🔌 OrderManagement<br/>Controller
    participant Svc as ⚙️ Order<br/>Service
    participant Repo as 💾 Order<br/>Repository
    participant Entity as 📦 Order<br/>Entity
    
    User->>FE: Truy cập trang thống kê
    
    FE->>Ctrl: GET /api/admin/orders/statistics
    Ctrl->>Svc: getOrderStatistics()
    
    Note over Svc,Entity: Đếm theo trạng thái
    Svc->>Repo: countByStatus(PENDING)
    Repo->>Entity: COUNT(*) FROM Order WHERE status='PENDING'
    Entity-->>Repo: count
    Repo-->>Svc: pendingCount
    
    Svc->>Repo: countByStatus(CONFIRMED)
    Repo->>Entity: COUNT(*) FROM Order WHERE status='CONFIRMED'
    Entity-->>Repo: count
    Repo-->>Svc: confirmedCount
    
    Svc->>Repo: countByStatus(READY_TO_SHIP)
    Repo->>Entity: COUNT(*) FROM Order WHERE status='READY_TO_SHIP'
    Entity-->>Repo: count
    Repo-->>Svc: readyToShipCount
    
    Svc->>Repo: countByStatus(SHIPPING)
    Repo->>Entity: COUNT(*) FROM Order WHERE status='SHIPPING'
    Entity-->>Repo: count
    Repo-->>Svc: shippingCount
    
    Svc->>Repo: countByStatus(DELIVERED)
    Repo->>Entity: COUNT(*) FROM Order WHERE status='DELIVERED'
    Entity-->>Repo: count
    Repo-->>Svc: deliveredCount
    
    Svc->>Repo: countByStatus(CANCELLED)
    Repo->>Entity: COUNT(*) FROM Order WHERE status='CANCELLED'
    Entity-->>Repo: count
    Repo-->>Svc: cancelledCount
    
    Note over Svc,Entity: Tính doanh thu
    Svc->>Svc: Calculate date range (30 days)
    Svc->>Repo: sumTotalByDateRange(startDate, endDate)
    Repo->>Entity: SUM(total) FROM Order<br/>WHERE createdAt BETWEEN ? AND ?<br/>AND paymentStatus='PAID'
    Entity-->>Repo: totalRevenue
    Repo-->>Svc: totalRevenue
    
    Svc->>Repo: countPaidOrdersBetween(startDate, endDate)
    Repo->>Entity: COUNT(*) FROM Order<br/>WHERE createdAt BETWEEN ? AND ?<br/>AND paymentStatus='PAID'
    Entity-->>Repo: count
    Repo-->>Svc: totalOrders
    
    Svc->>Svc: Build OrderStatisticsResponse:<br/>- total: totalOrders<br/>- pending: pendingCount<br/>- confirmed: confirmedCount<br/>- readyToShip: readyToShipCount<br/>- shipping: shippingCount<br/>- delivered: deliveredCount<br/>- cancelled: cancelledCount<br/>- totalRevenue: totalRevenue<br/>- averageOrderValue: totalRevenue / totalOrders
    
    Svc-->>Ctrl: ApiResponse.success(statistics)
    Ctrl-->>FE: {success: true, data: {...}}
    
    FE->>User: ✅ Hiển thị:<br/>📊 Statistics cards<br/>📈 Biểu đồ doanh thu<br/>📋 Bảng thống kê theo trạng thái
```

---

## 11. LUỒNG TRẠNG THÁI ĐƠN HÀNG (Order Status Flow)

### Sơ đồ luồng trạng thái

```mermaid
stateDiagram-v2
    [*] --> PENDING_PAYMENT: Customer tạo đơn
    
    PENDING_PAYMENT --> CONFIRMED: Admin/Sales xác nhận<br/>+ Thanh toán thành công
    PENDING_PAYMENT --> CANCELLED: Customer/Admin hủy
    
    CONFIRMED --> READY_TO_SHIP: Warehouse xuất kho
    CONFIRMED --> CANCELLED: Admin hủy
    
    READY_TO_SHIP --> SHIPPING: Sales cập nhật<br/>Tài xế đã lấy hàng
    READY_TO_SHIP --> CANCELLED: Admin hủy
    
    SHIPPING --> DELIVERED: Giao hàng thành công
    SHIPPING --> DELIVERY_FAILED: Giao hàng thất bại
    SHIPPING --> CANCELLED: Admin hủy
    
    DELIVERY_FAILED --> SHIPPING: Giao lại
    DELIVERY_FAILED --> RETURNED: Trả hàng
    
    DELIVERED --> COMPLETED: Hoàn thành
    DELIVERED --> RETURNED: Khách trả hàng
    
    COMPLETED --> [*]
    CANCELLED --> [*]
    RETURNED --> [*]
```

### Quy tắc chuyển trạng thái

| Từ trạng thái | Sang trạng thái | Người thực hiện | Điều kiện |
|---------------|-----------------|-----------------|-----------|
| PENDING_PAYMENT | CONFIRMED | Admin/Sales | Đã thanh toán hoặc COD |
| PENDING_PAYMENT | CANCELLED | Customer/Admin | Bất kỳ lúc nào |
| CONFIRMED | READY_TO_SHIP | System (auto) | Sau khi xuất kho thành công |
| CONFIRMED | CANCELLED | Admin | Trước khi xuất kho |
| READY_TO_SHIP | SHIPPING | Sales/Admin | Tài xế đã lấy hàng |
| READY_TO_SHIP | CANCELLED | Admin | Trước khi giao |
| SHIPPING | DELIVERED | Sales/Admin | Giao hàng thành công |
| SHIPPING | DELIVERY_FAILED | System/Admin | Giao hàng thất bại |
| SHIPPING | CANCELLED | Admin | Đặc biệt |
| DELIVERY_FAILED | SHIPPING | Admin | Giao lại |
| DELIVERY_FAILED | RETURNED | Admin | Trả hàng |
| DELIVERED | COMPLETED | System (auto) | Sau 7 ngày không khiếu nại |
| DELIVERED | RETURNED | Admin | Khách yêu cầu trả hàng |

---

## 12. ENTITY DEFINITIONS

### Entity: Order

```java
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String orderCode; // Mã đơn hàng: ORD20231119001
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;
    
    // Thông tin giao hàng
    @Column(nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;
    
    private String province;  // Tỉnh/Thành phố
    private String district;  // Quận/Huyện
    private String ward;      // Phường/Xã ward code (for GHN API)
    private String wardName;  // Tên phường/xã (for display)
    private String address;   // Địa chỉ cụ thể (số nhà, tên đường)
    
    private String note; // Ghi chú của khách hàng
    
    // Giá tiền
    @Column(nullable = false)
    private Double subtotal; // Tổng tiền hàng
    
    @Column(nullable = false)
    private Double shippingFee; // Phí vận chuyển
    
    @Column(nullable = false)
    private Double discount; // Giảm giá
    
    @Column(nullable = false)
    private Double total; // Tổng thanh toán
    
    // Thanh toán
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;
    
    @Column(length = 20)
    private String paymentMethod; // COD, SEPAY, VNPAY, etc.
    
    private Long paymentId; // Reference đến Payment entity
    
    // Trạng thái đơn hàng
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime confirmedAt; // Xác nhận đơn
    
    private LocalDateTime shippedAt; // Giao hàng
    
    private LocalDateTime deliveredAt; // Đã giao
    
    private LocalDateTime cancelledAt; // Hủy đơn
    
    private String cancelReason; // Lý do hủy
    
    // GHN Shipping Integration
    private String ghnOrderCode; // Mã vận đơn GHN
    
    private String ghnShippingStatus; // Trạng thái vận chuyển từ GHN
    
    private LocalDateTime ghnCreatedAt; // Thời gian tạo đơn GHN
    
    private LocalDateTime ghnExpectedDeliveryTime; // Thời gian giao hàng dự kiến
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING_PAYMENT;
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.UNPAID;
        }
    }
}
```

### Entity: OrderItem

```java
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private String productName; // Lưu tên sản phẩm tại thời điểm mua
    
    @Column(nullable = false)
    private Double price; // Giá tại thời điểm mua
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Double subtotal; // price * quantity
    
    private String serialNumber; // Serial nếu là sản phẩm có serial
    
    @Column(nullable = false)
    private Boolean reserved = false; // Đã giữ hàng chưa
    
    @Column(nullable = false)
    private Boolean exported = false; // Đã xuất kho chưa
}
```

### Enum: OrderStatus

```java
public enum OrderStatus {
    PENDING_PAYMENT,    // Chờ thanh toán (đơn online)
    CONFIRMED,          // Đã xác nhận - Chờ xuất kho
    READY_TO_SHIP,      // Đã xuất kho - Chờ tài xế lấy hàng
    PICKED_UP,          // Tài xế đã lấy hàng
    SHIPPING,           // Đang giao hàng
    DELIVERY_FAILED,    // Giao hàng thất bại
    DELIVERED,          // Đã giao hàng
    COMPLETED,          // Hoàn thành
    CANCELLED,          // Đã hủy
    RETURNED            // Đã trả hàng
}
```

### Enum: PaymentStatus

```java
public enum PaymentStatus {
    UNPAID,         // Chưa thanh toán
    PENDING,        // Đang chờ thanh toán
    PAID,           // Đã thanh toán
    FAILED,         // Thanh toán thất bại
    REFUNDED        // Đã hoàn tiền
}
```

---

## 13. API ENDPOINTS SUMMARY

### Customer Endpoints (`/api/orders`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/orders` | Tạo đơn hàng từ giỏ hàng | CUSTOMER, ADMIN |
| GET | `/api/orders` | Lấy danh sách đơn hàng của customer | CUSTOMER, ADMIN |
| GET | `/api/orders/{orderId}` | Lấy chi tiết đơn hàng | CUSTOMER, ADMIN |
| GET | `/api/orders/code/{orderCode}` | Lấy đơn hàng theo mã | CUSTOMER, ADMIN |
| PUT | `/api/orders/{orderId}/cancel` | Hủy đơn hàng (Customer) | CUSTOMER, ADMIN |
| GET | `/api/orders/{orderId}/shipping-status` | Xem trạng thái vận chuyển | CUSTOMER, ADMIN |

### Admin/Employee Endpoints (`/api/admin/orders`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/admin/orders` | Lấy tất cả đơn hàng (có filter) | ADMIN, EMPLOYEE |
| GET | `/api/admin/orders/{orderId}` | Lấy chi tiết đơn hàng | ADMIN, EMPLOYEE |
| GET | `/api/admin/orders/statistics` | Thống kê đơn hàng | ADMIN, EMPLOYEE |
| PUT | `/api/admin/orders/{orderId}/status` | Cập nhật trạng thái | ADMIN, EMPLOYEE |
| PUT | `/api/admin/orders/{orderId}/mark-shipping-from-ready` | Chuyển READY_TO_SHIP → SHIPPING | ADMIN, EMPLOYEE |
| PUT | `/api/admin/orders/{orderId}/delivered` | Đánh dấu đã giao | ADMIN, EMPLOYEE |
| PUT | `/api/admin/orders/{orderId}/cancel` | Hủy đơn hàng (Admin) | ADMIN, EMPLOYEE |
| GET | `/api/admin/orders/{orderId}/shipping-status` | Xem trạng thái vận chuyển | ADMIN, EMPLOYEE |

---

## 14. BUSINESS RULES

### Quy tắc tạo đơn hàng
1. Giỏ hàng phải có ít nhất 1 sản phẩm
2. Tất cả sản phẩm phải còn hàng (available quantity > 0)
3. Địa chỉ giao hàng phải đầy đủ: province, district, ward, address
4. Mã đơn hàng tự động: `ORD{timestamp}`
5. Trạng thái mặc định: `PENDING_PAYMENT`
6. PaymentStatus mặc định: `UNPAID`

### Quy tắc xác nhận đơn hàng
1. Chỉ xác nhận được đơn ở trạng thái `PENDING_PAYMENT`
2. Phải kiểm tra thanh toán (nếu không phải COD)
3. Sau khi xác nhận: status → `CONFIRMED`
4. Lưu thời gian xác nhận: `confirmedAt`

### Quy tắc xuất kho
1. Chỉ xuất kho đơn ở trạng thái `CONFIRMED`
2. Kiểm tra tồn kho trước khi xuất
3. Tạo phiếu xuất kho (ExportOrder)
4. Cập nhật số lượng tồn kho
5. Tự động chuyển Order sang `READY_TO_SHIP`

### Quy tắc giao hàng
1. Chỉ chuyển sang `SHIPPING` từ `READY_TO_SHIP`
2. Lưu thời gian bắt đầu giao: `shippedAt`
3. Có thể tích hợp với GHN để tạo vận đơn
4. Cập nhật trạng thái vận chuyển từ GHN webhook

### Quy tắc hoàn thành
1. Chỉ đánh dấu `DELIVERED` từ `SHIPPING`
2. Lưu thời gian giao hàng: `deliveredAt`
3. Tự động chuyển sang `COMPLETED` sau 7 ngày (nếu không có khiếu nại)

### Quy tắc hủy đơn
1. **Customer** chỉ hủy được đơn ở trạng thái: `PENDING_PAYMENT`, `CONFIRMED`
2. **Admin** có thể hủy đơn ở bất kỳ trạng thái nào (trừ `DELIVERED`, `COMPLETED`)
3. Phải nhập lý do hủy
4. Lưu thời gian hủy: `cancelledAt`
5. Nếu đã xuất kho: phải tạo phiếu nhập kho trả lại

---

## 15. INTEGRATION POINTS

### 1. Module Warehouse (Kho)
- **Xuất kho**: Khi Order chuyển từ `CONFIRMED` → `READY_TO_SHIP`
- **Nhập kho trả lại**: Khi Order bị hủy sau khi đã xuất kho
- **Kiểm tra tồn kho**: Trước khi tạo đơn hàng

### 2. Module Payment (Thanh toán)
- **Tạo Payment**: Khi Customer chọn phương thức thanh toán online
- **Cập nhật PaymentStatus**: Khi thanh toán thành công/thất bại
- **Webhook**: Nhận thông báo từ cổng thanh toán (SePay, VNPay)

### 3. Module Accounting (Kế toán)
- **Tạo FinancialTransaction**: Khi đơn hàng `DELIVERED`
  - Type: `REVENUE`
  - Category: `SALES`
  - Amount: `order.total`
- **Đối soát vận chuyển**: Tính toán chi phí và lợi nhuận vận chuyển

### 4. GHN Shipping API
- **Tạo vận đơn**: Khi Order ở trạng thái `READY_TO_SHIP`
- **Webhook**: Nhận cập nhật trạng thái vận chuyển
- **Tracking**: Lấy thông tin chi tiết vận đơn

### 5. Module Inventory (Tồn kho)
- **Giữ hàng (Reserve)**: Khi Order được xác nhận
- **Xuất hàng (Export)**: Khi tạo phiếu xuất kho
- **Trả hàng (Return)**: Khi đơn bị hủy hoặc trả hàng

---

## 16. NOTES

### Performance Considerations
1. **Pagination**: Luôn sử dụng phân trang cho danh sách đơn hàng
2. **Lazy Loading**: Sử dụng `@ManyToOne(fetch = FetchType.LAZY)` cho các quan hệ
3. **Indexing**: Đánh index cho các cột: `orderCode`, `status`, `createdAt`, `customerId`
4. **Caching**: Cache thống kê đơn hàng (statistics) với TTL 5 phút

### Security Considerations
1. **Authorization**: Kiểm tra quyền truy cập cho mỗi endpoint
2. **Customer Isolation**: Customer chỉ xem được đơn hàng của mình
3. **Audit Log**: Ghi log mọi thay đổi trạng thái đơn hàng
4. **Rate Limiting**: Giới hạn số lần tạo đơn hàng trong 1 phút

### Error Handling
1. **Order Not Found**: Trả về 404 với message rõ ràng
2. **Invalid Status Transition**: Trả về 400 với message giải thích
3. **Insufficient Stock**: Trả về 400 với thông tin sản phẩm hết hàng
4. **Payment Failed**: Trả về 400 và giữ đơn hàng ở `PENDING_PAYMENT`

---

**Tài liệu được tạo dựa trên code thực tế của hệ thống**  
**Ngày cập nhật**: 2024-12-25
