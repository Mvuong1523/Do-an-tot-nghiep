# Sơ Đồ Hệ Thống WEB_TMDT

## 1. Sơ Đồ Kiến Trúc Hệ Thống

![System Architecture](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/YOUR_USERNAME/YOUR_REPO/main/ARCHITECTURE_DIAGRAM.puml)

**Mô tả:**
- **Client Layer**: Web Browser, Mobile Browser
- **Load Balancer**: Nginx phân tải
- **Frontend**: Next.js (Node.js 18, Port 3000) - 2 instances
- **Backend**: Spring Boot (Java 17, Port 8080) - 2 instances
  - Controllers: Auth, Product, Cart, Order, Payment, Inventory
  - Services: Business logic layer
  - Repositories: Data access layer
  - Security: JWT Authentication & Authorization
- **Database**: MySQL Master-Slave Replication + Redis Cache
- **External Services**: SePay (Payment), GHTK (Shipping), Cloudinary (Images), SMTP (Email)

---

## 2. Sơ Đồ ERD Database

![Database ERD](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/YOUR_USERNAME/YOUR_REPO/main/DATABASE_ERD.puml)

**Mô tả:**

### Auth Module (5 bảng)
- `users` - Người dùng hệ thống
- `customers` - Thông tin khách hàng
- `employees` - Thông tin nhân viên
- `employee_registrations` - Đăng ký nhân viên chờ duyệt
- `otp_verifications` - Xác thực OTP

### Product Module (2 bảng)
- `categories` - Danh mục sản phẩm (hỗ trợ phân cấp)
- `products` - Sản phẩm hiển thị trên web

### Inventory Module (10 bảng)
- `warehouse_products` - Sản phẩm trong kho
- `product_details` - Chi tiết serial từng sản phẩm
- `inventory_stock` - Tồn kho (on_hand, reserved, damaged)
- `product_specifications` - Thông số kỹ thuật
- `warehouse_product_images` - Ảnh sản phẩm kho
- `suppliers` - Nhà cung cấp
- `purchase_orders` - Đơn đặt hàng nhập kho
- `purchase_order_items` - Chi tiết đơn nhập
- `export_orders` - Phiếu xuất kho
- `export_order_items` - Chi tiết phiếu xuất

### Cart Module (2 bảng)
- `carts` - Giỏ hàng
- `cart_items` - Sản phẩm trong giỏ

### Order Module (2 bảng)
- `orders` - Đơn hàng
- `order_items` - Chi tiết đơn hàng

### Payment Module (1 bảng)
- `payments` - Thanh toán (tích hợp SePay)

**Tổng cộng: 22 bảng**

---

## Cách Xem Sơ Đồ Offline

### Option 1: Sử dụng VS Code
1. Cài extension: **PlantUML** (jebbs.plantuml)
2. Mở file `ARCHITECTURE_DIAGRAM.puml` hoặc `DATABASE_ERD.puml`
3. Press `Alt + D` để preview

### Option 2: Sử dụng IntelliJ IDEA
1. Cài plugin: **PlantUML Integration**
2. Mở file `.puml`
3. Sơ đồ hiển thị tự động bên phải

### Option 3: PlantUML Online
1. Truy cập: http://www.plantuml.com/plantuml/uml/
2. Copy nội dung file `.puml`
3. Paste và click Submit

### Option 4: Generate PNG/SVG
```bash
# Cài PlantUML
npm install -g node-plantuml

# Generate PNG
puml generate ARCHITECTURE_DIAGRAM.puml -o architecture.png
puml generate DATABASE_ERD.puml -o database_erd.png

# Generate SVG (chất lượng cao hơn)
puml generate ARCHITECTURE_DIAGRAM.puml -o architecture.svg
puml generate DATABASE_ERD.puml -o database_erd.svg
```

---

## Hướng Dẫn Cập Nhật Link GitHub

Để sơ đồ hiển thị trên GitHub, thay đổi URL trong file này:

```markdown
![System Architecture](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/YOUR_USERNAME/YOUR_REPO/main/ARCHITECTURE_DIAGRAM.puml)
```

Thay thế:
- `YOUR_USERNAME` → username GitHub của bạn
- `YOUR_REPO` → tên repository

Ví dụ:
```markdown
![System Architecture](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/john-doe/web-tmdt/main/ARCHITECTURE_DIAGRAM.puml)
```

---

## Công Nghệ Sử Dụng

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Database**: MySQL 8.0
- **Cache**: Redis
- **Security**: Spring Security + JWT
- **ORM**: JPA/Hibernate

### Frontend
- **Framework**: Next.js 14
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **State**: Zustand
- **HTTP**: Axios

### External Services
- **Payment**: SePay
- **Shipping**: GHTK
- **Storage**: Cloudinary
- **Email**: SMTP

---

## Phân Quyền (RBAC)

| Role | Quyền |
|------|-------|
| **ADMIN** | Toàn quyền hệ thống |
| **PRODUCT_MANAGER** | Quản lý sản phẩm, danh mục, publish sản phẩm |
| **WAREHOUSE_MANAGER** | Quản lý kho, nhập/xuất hàng, quản lý serial |
| **CUSTOMER** | Mua hàng, xem đơn hàng, quản lý giỏ hàng |

---

## Luồng Nghiệp Vụ Chính

### 1. Luồng Đặt Hàng
```
Customer → Add to Cart → Checkout → Create Order (PENDING)
→ Create Payment (PENDING) → Show QR Code → Customer Pay
→ SePay Webhook → Update Payment (PAID) → Update Order (CONFIRMED)
→ Reserve Stock → Admin Process → Create Export Order
→ Update Stock → Ship Order → Delivered
```

### 2. Luồng Nhập Kho
```
Warehouse Manager → Create Purchase Order → Add Items
→ Receive Goods → Input Serial Numbers → Create Product Details
→ Update Inventory Stock → Complete PO (RECEIVED)
```

### 3. Luồng Publish Sản Phẩm
```
Product Manager → View Warehouse Products → Select Product
→ Choose Category → Set Price → Upload Images
→ Create Product → Link to Warehouse Product → Sync Stock
→ Product Live on Website
```

---

## Liên Hệ & Đóng Góp

Nếu có câu hỏi hoặc đề xuất cải tiến, vui lòng tạo issue hoặc pull request.
