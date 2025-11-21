# Sơ Đồ Hệ Thống WEB_TMDT

## 📊 Danh Sách Sơ Đồ

### 1. **ARCHITECTURE_DIAGRAM.puml** - Sơ Đồ Kiến Trúc Hệ Thống
Sơ đồ tổng thể kiến trúc hệ thống bao gồm:
- **Client Layer**: Web Browser, Mobile Browser
- **Load Balancer**: Nginx
- **Frontend Layer**: Next.js Servers (Node.js 18)
- **Backend Layer**: Spring Boot (Java 17)
  - Controllers (Auth, Product, Cart, Order, Payment, Inventory)
  - Services
  - Repositories
  - Security (JWT Filter, Security Config)
- **Database Layer**: MySQL Master-Slave, Redis Cache
- **External Services**: SePay, GHTK, Cloudinary, SMTP

### 2. **DATABASE_ERD.puml** - Sơ Đồ ERD Đầy Đủ Database
Sơ đồ quan hệ thực thể đầy đủ với 20+ bảng:
- **Auth Module**: users, customers, employees, employee_registrations, otp_verifications
- **Product Module**: categories, products
- **Inventory Module**: warehouse_products, product_details, inventory_stock, product_specifications, warehouse_product_images, suppliers, purchase_orders, purchase_order_items, export_orders, export_order_items
- **Cart Module**: carts, cart_items
- **Order Module**: orders, order_items
- **Payment Module**: payments

## 🎨 Cách Render Sơ Đồ

### Option 1: PlantUML Online Server (Nhanh nhất)
1. Truy cập: http://www.plantuml.com/plantuml/uml/
2. Copy nội dung file `.puml` vào
3. Click "Submit" để xem sơ đồ
4. Download PNG/SVG nếu cần

### Option 2: Visual Studio Code
1. Cài đặt extension: **PlantUML** (jebbs.plantuml)
2. Mở file `.puml`
3. Press `Alt + D` để preview
4. Right-click → "Export Current Diagram" để export

### Option 3: IntelliJ IDEA
1. Cài đặt plugin: **PlantUML Integration**
2. Mở file `.puml`
3. Sơ đồ sẽ tự động hiển thị bên phải
4. Right-click → "Copy/Export Diagram" để export

### Option 4: PlantUML CLI (Cho automation)
```bash
# Cài đặt
npm install -g node-plantuml

# Render sang PNG
puml generate ARCHITECTURE_DIAGRAM.puml -o output.png

# Render sang SVG
puml generate DATABASE_ERD.puml -o output.svg
```

## 📁 Cấu Trúc File

```
project-root/
├── ARCHITECTURE_DIAGRAM.puml    # Sơ đồ kiến trúc hệ thống
├── DATABASE_ERD.puml             # Sơ đồ ERD database
└── README_DIAGRAMS.md            # File hướng dẫn này
```

## 🎯 Mục Đích Sử Dụng

### ARCHITECTURE_DIAGRAM.puml
- Trình bày kiến trúc tổng thể cho stakeholders
- Tài liệu kỹ thuật cho team development
- Báo cáo đồ án, luận văn
- Onboarding cho developer mới

### DATABASE_ERD.puml
- Thiết kế database schema
- Tài liệu cho DBA
- Review database design
- Báo cáo kỹ thuật

## 💡 Tips

1. **Chỉnh sửa sơ đồ**: Mở file `.puml` bằng text editor và chỉnh sửa
2. **Thay đổi màu sắc**: Sửa `#LightBlue`, `#LightGreen`, etc.
3. **Thêm/bớt component**: Thêm/xóa các block `[Component Name]`
4. **Export chất lượng cao**: Sử dụng SVG format thay vì PNG

## 📝 Ghi Chú

- Sơ đồ sử dụng PlantUML syntax chuẩn UML 2.0
- Tất cả relationships đều được đánh dấu rõ ràng (1:1, 1:N, N:1)
- Primary Key (PK), Foreign Key (FK), Unique Key (UK) được highlight màu
- Có notes giải thích cho các phần quan trọng

## 🔗 Tài Liệu Tham Khảo

- PlantUML Official: https://plantuml.com/
- PlantUML Component Diagram: https://plantuml.com/component-diagram
- PlantUML Entity Relationship: https://plantuml.com/ie-diagram
