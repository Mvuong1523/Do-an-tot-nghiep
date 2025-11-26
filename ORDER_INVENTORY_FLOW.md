# Quy trình Đặt hàng và Xuất kho

## Flow tổng quan

```
KHÁCH HÀNG                    HỆ THỐNG                    QUẢN LÝ KHO              NHÂN VIÊN BÁN
    |                             |                            |                         |
    | 1. Đặt hàng                 |                            |                         |
    |--------------------------->|                            |                         |
    |                             |                            |                         |
    |                             | 2. Tạo Order (CONFIRMED)   |                         |
    |                             | Reserve stock              |                         |
    |                             |--------------------------->|                         |
    |                             |                            |                         |
    |                             |                            | 3. Xem đơn cần xuất     |
    |                             |                            | (GET /api/inventory/    |
    |                             |                            |  orders/pending-export) |
    |                             |                            |                         |
    |                             |                            | 4. Tạo phiếu xuất       |
    |                             |                            | (Sau này implement)     |
    |                             |                            |                         |
    |                             |                            | 5. Quét/nhập serial     |
    |                             |                            | Chuẩn bị hàng           |
    |                             |                            |                         |
    |                             |                            | 6. Hoàn tất xuất kho    |
    |                             |                            | - Trừ tồn kho           |
    |                             |                            | - Trừ reserved          |
    |                             |                            | - Set exported = true   |
    |                             | 7. Update status SHIPPING  |                         |
    |                             |<---------------------------|                         |
    |                             |                            |                         |
    |                             |                            |                         | 8. Lấy hàng
    |                             |                            |                         | giao khách
    |                             |                            |                         |
    | 9. Nhận hàng                |                            |                         |
    |<--------------------------------------------------------------------|
    |                             |                            |                         |
    |                             | 10. Update DELIVERED       |                         |
```

## Chi tiết từng bước

### 1. Khách đặt hàng
- **API**: `POST /api/orders`
- **Request**: CreateOrderRequest (chỉ có địa chỉ, phí ship, note)
- **Action**: 
  - Tạo Order với status = CONFIRMED
  - Reserve stock: `product.reservedQuantity += quantity`
  - Set `orderItem.reserved = true`
  - Clear cart

### 2. Quản lý kho xem đơn cần xuất
- **API**: `GET /api/inventory/orders/pending-export`
- **Response**: Danh sách orders với status = CONFIRMED
- **Hiển thị**:
  - Order code, customer info
  - Danh sách sản phẩm cần xuất (SKU, tên, số lượng)
  - Trạng thái reserved/exported của từng item

### 3. Tạo phiếu xuất kho (Sau này)
- Quản lý kho tạo ExportOrder trong inventory module
- Link với Order qua `orderId`
- Tạo ExportOrderItem cho từng OrderItem

### 4. Quét/nhập serial (Sau này)
- Quét QR code hoặc nhập tay serial
- Gán serial vào OrderItem
- Validate serial tồn tại và available

### 5. Hoàn tất xuất kho (Sau này)
- **Action**:
  - Trừ tồn kho: `product.stockQuantity -= quantity`
  - Trừ reserved: `product.reservedQuantity -= quantity`
  - Set `orderItem.exported = true`
  - Update Order status = SHIPPING
  - Set `order.shippedAt = now()`

## Database Schema Changes

### Table: products
```sql
ALTER TABLE products ADD COLUMN reserved_quantity BIGINT DEFAULT 0;
```

### Table: order_items
```sql
ALTER TABLE order_items ADD COLUMN reserved BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE order_items ADD COLUMN exported BOOLEAN DEFAULT FALSE NOT NULL;
```

### Table: export_orders
```sql
ALTER TABLE export_orders ADD COLUMN order_id BIGINT;
ALTER TABLE export_orders ADD CONSTRAINT fk_export_orders_order 
    FOREIGN KEY (order_id) REFERENCES orders(id);
```

## API Endpoints

### Cho Khách hàng
- `POST /api/orders` - Đặt hàng
- `GET /api/orders` - Xem đơn hàng của mình
- `GET /api/orders/{orderId}` - Chi tiết đơn hàng
- `PUT /api/orders/{orderId}/cancel` - Hủy đơn

### Cho Quản lý kho
- `GET /api/inventory/orders/pending-export` - Đơn cần xuất kho
- `GET /api/inventory/orders/{orderId}` - Chi tiết đơn để chuẩn bị
- `GET /api/inventory/orders/statistics` - Thống kê

### Cho Admin/Sales Staff
- `GET /api/admin/orders` - Tất cả đơn hàng
- `PUT /api/admin/orders/{orderId}/confirm` - Xác nhận đơn
- `PUT /api/admin/orders/{orderId}/shipping` - Đánh dấu đang giao
- `PUT /api/admin/orders/{orderId}/delivered` - Đã giao
- `PUT /api/admin/orders/{orderId}/cancel` - Hủy đơn

## Tính năng hiện tại

✅ Reserve stock khi đặt hàng
✅ API cho quản lý kho xem đơn cần xuất
✅ Hiển thị thông tin reserved/exported
✅ Migration SQL

## Tính năng cần implement sau

⏳ Tạo phiếu xuất kho (ExportOrder)
⏳ Quét/nhập serial
⏳ Hoàn tất xuất kho (trừ tồn, update status)
⏳ Hủy phiếu xuất (release reserved stock)
⏳ Báo cáo xuất kho

## Lưu ý

1. **Reserved vs Stock**:
   - `stockQuantity`: Tồn kho thực tế
   - `reservedQuantity`: Đang giữ cho đơn hàng
   - Available = stockQuantity - reservedQuantity

2. **Khi hủy đơn**:
   - Phải release reserved stock
   - Set `orderItem.reserved = false`
   - Trừ `product.reservedQuantity`

3. **Validation**:
   - Khi đặt hàng: Check `stockQuantity >= quantity`
   - Khi xuất kho: Check `reservedQuantity >= quantity`

4. **Phân quyền**:
   - CUSTOMER: Đặt hàng, xem đơn của mình
   - WAREHOUSE_STAFF: Xem đơn cần xuất, tạo phiếu xuất
   - SALES_STAFF: Quản lý đơn hàng
   - ADMIN: Full quyền
