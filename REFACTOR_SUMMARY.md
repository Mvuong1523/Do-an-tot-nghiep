# Tóm tắt Refactoring: Cart & Order

## Vấn đề ban đầu
Order entity đang lưu thừa thông tin:
- Đã có `user_id` (reference đến User)
- Lại còn lưu thêm `customerName`, `customerPhone`, `customerEmail`
- Dữ liệu bị duplicate và không đồng bộ

## Giải pháp
Thay đổi từ **User-based** sang **Customer-based**:
- `Order` và `Cart` giờ reference đến `Customer` thay vì `User`
- Xóa các trường thừa trong `Order`: `customerName`, `customerPhone`, `customerEmail`
- Thông tin customer được lấy từ relationship: `Order -> Customer -> User`

## Thay đổi Database Schema

### Bảng `orders`
```sql
-- Trước
user_id BIGINT NOT NULL
customer_name VARCHAR(255) NOT NULL
customer_phone VARCHAR(255) NOT NULL
customer_email VARCHAR(255) NOT NULL

-- Sau
customer_id BIGINT NOT NULL
-- (đã xóa customer_name, customer_phone, customer_email)
```

### Bảng `carts`
```sql
-- Trước
user_id BIGINT NOT NULL UNIQUE

-- Sau
customer_id BIGINT NOT NULL UNIQUE
```

## Thay đổi Code

### Entities
- `Order.java`: `User user` → `Customer customer`
- `Cart.java`: `User user` → `Customer customer`

### Repositories
- `OrderRepository`: `findByUserId()` → `findByCustomerId()`
- `CartRepository`: `findByUserId()` → `findByCustomerId()`

### Services
- `OrderService`: Tất cả method nhận `customerId` thay vì `userId`
- `CartService`: Tất cả method nhận `customerId` thay vì `userId`

### Controllers
- `OrderController`: `getCustomerIdFromAuth()` thay vì `getUserIdFromAuth()`
- `CartController`: `getCustomerIdFromAuth()` thay vì `getUserIdFromAuth()`

### DTOs
- `CreateOrderRequest`: Xóa `customerName`, `customerPhone`, `customerEmail`
- `OrderResponse`: Thêm `customerId`, giữ lại display fields

## Cách lấy thông tin Customer

```java
// Trong OrderServiceImpl.toOrderResponse()
Customer customer = order.getCustomer();

OrderResponse.builder()
    .customerId(customer.getId())
    .customerName(customer.getFullName())      // Từ Customer
    .customerPhone(customer.getPhone())        // Từ Customer
    .customerEmail(customer.getUser().getEmail()) // Từ User qua Customer
    .build();
```

## Migration Steps

1. ✅ Backup database
2. ✅ Chạy migration SQL script
3. ✅ Update code (entities, repositories, services, controllers)
4. ✅ Test application
5. ✅ Deploy

## Files Created
- `migration_cart_order_to_customer.sql` - SQL migration script
- `MIGRATION_GUIDE.md` - Hướng dẫn chi tiết
- `REFACTOR_SUMMARY.md` - File này

## Testing Checklist
- [ ] Login thành công
- [ ] Add to cart hoạt động
- [ ] View cart hiển thị đúng
- [ ] Create order thành công
- [ ] View orders hiển thị đúng thông tin customer
- [ ] Cancel order hoạt động
- [ ] Admin view orders hiển thị đúng

## Lưu ý quan trọng
1. Phải chạy migration SQL trước khi start application
2. Đảm bảo mọi User đều có Customer tương ứng
3. Backup database trước khi migration
4. Test kỹ trước khi deploy production
