# Hướng dẫn Migration: Cart & Order từ User sang Customer

## Tổng quan thay đổi

### Trước đây:
- `Order` và `Cart` lưu reference đến `User` (user_id)
- `Order` lưu thừa thông tin: `customerName`, `customerPhone`, `customerEmail`

### Sau khi refactor:
- `Order` và `Cart` lưu reference đến `Customer` (customer_id)
- `Order` chỉ lưu `shippingAddress`, các thông tin khác lấy từ `Customer`
- Thông tin customer được truy xuất qua relationship: `Order -> Customer -> User`

## Lợi ích

1. **Giảm redundancy**: Không lưu trùng lặp thông tin
2. **Data consistency**: Thông tin customer luôn đồng bộ
3. **Dễ maintain**: Chỉ cần update ở một chỗ (Customer entity)
4. **Chuẩn database design**: Normalize data properly

## Các bước thực hiện

### 1. Backup Database (BẮT BUỘC)

```bash
# MySQL/MariaDB
mysqldump -u root -p your_database > backup_before_migration.sql

# PostgreSQL
pg_dump -U postgres your_database > backup_before_migration.sql
```

### 2. Chạy Migration Script

```bash
mysql -u root -p your_database < migration_cart_order_to_customer.sql
```

Hoặc chạy từng bước trong script để kiểm soát tốt hơn.

### 3. Rebuild và Test Application

```bash
# Backend
mvn clean install
mvn spring-boot:run

# Frontend
cd src/frontend
npm install
npm run dev
```

### 4. Test các chức năng

- [ ] Đăng nhập
- [ ] Thêm sản phẩm vào giỏ hàng
- [ ] Xem giỏ hàng
- [ ] Tạo đơn hàng
- [ ] Xem danh sách đơn hàng
- [ ] Xem chi tiết đơn hàng
- [ ] Hủy đơn hàng

## Các file đã thay đổi

### Backend Entities
- ✅ `Order.java` - Đổi từ `User user` → `Customer customer`, xóa `customerName/Phone/Email`
- ✅ `Cart.java` - Đổi từ `User user` → `Customer customer`

### Backend Repositories
- ✅ `OrderRepository.java` - Đổi `findByUserId` → `findByCustomerId`
- ✅ `CartRepository.java` - Đổi `findByUserId` → `findByCustomerId`

### Backend Services
- ✅ `OrderService.java` - Đổi tất cả `userId` → `customerId`
- ✅ `OrderServiceImpl.java` - Cập nhật logic, lấy thông tin từ Customer
- ✅ `CartService.java` - Đổi tất cả `userId` → `customerId`
- ✅ `CartServiceImpl.java` - Cập nhật logic

### Backend Controllers
- ✅ `OrderController.java` - Đổi helper method `getUserIdFromAuth` → `getCustomerIdFromAuth`
- ✅ `CartController.java` - Đổi helper method `getUserIdFromAuth` → `getCustomerIdFromAuth`

### Backend DTOs
- ✅ `CreateOrderRequest.java` - Xóa `customerName`, `customerPhone`, `customerEmail`
- ✅ `OrderResponse.java` - Thêm `customerId`, giữ lại các field để hiển thị

### Database
- ✅ Migration script SQL

## Rollback (nếu cần)

Nếu có vấn đề, restore từ backup:

```bash
# MySQL/MariaDB
mysql -u root -p your_database < backup_before_migration.sql

# PostgreSQL
psql -U postgres your_database < backup_before_migration.sql
```

Sau đó revert code về commit trước:

```bash
git revert HEAD
# hoặc
git reset --hard <commit_hash_before_migration>
```

## Lưu ý

1. **Không có User thì không có Customer**: Đảm bảo mọi User đều có Customer tương ứng
2. **Guest checkout**: Nếu có tính năng này trong tương lai, cần xem xét lại design
3. **Admin/Staff orders**: Hiện tại chỉ Customer mới có thể đặt hàng
4. **Historical data**: Nếu cần giữ thông tin customer tại thời điểm đặt hàng, cân nhắc snapshot approach

## Kiểm tra sau migration

```sql
-- Kiểm tra orders có customer_id hợp lệ
SELECT o.id, o.order_code, o.customer_id, c.full_name, u.email
FROM orders o
JOIN customers c ON o.customer_id = c.id
JOIN users u ON c.user_id = u.id
LIMIT 10;

-- Kiểm tra carts có customer_id hợp lệ
SELECT ca.id, ca.customer_id, c.full_name, u.email
FROM carts ca
JOIN customers c ON ca.customer_id = c.id
JOIN users u ON c.user_id = u.id
LIMIT 10;
```

## Hỗ trợ

Nếu gặp vấn đề, kiểm tra:
1. Log của Spring Boot application
2. Database constraints và foreign keys
3. Đảm bảo mọi User đều có Customer
