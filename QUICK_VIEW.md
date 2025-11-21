# Xem Nhanh Sơ Đồ

## 🚀 Cách Nhanh Nhất - Xem Online

### 1. Sơ Đồ Kiến Trúc Hệ Thống
Click link này để xem ngay:
```
http://www.plantuml.com/plantuml/uml/
```
Sau đó copy toàn bộ nội dung file `ARCHITECTURE_DIAGRAM.puml` và paste vào.

### 2. Sơ Đồ ERD Database  
Click link này để xem ngay:
```
http://www.plantuml.com/plantuml/uml/
```
Sau đó copy toàn bộ nội dung file `DATABASE_ERD.puml` và paste vào.

---

## 💻 Generate Ảnh PNG/SVG (Windows)

### Bước 1: Cài Java (nếu chưa có)
Download và cài Java JDK từ: https://www.oracle.com/java/technologies/downloads/

### Bước 2: Chạy Script
Double-click file `generate-diagrams.bat`

Script sẽ tự động:
1. Download PlantUML (lần đầu tiên)
2. Generate file PNG:
   - `ARCHITECTURE_DIAGRAM.png`
   - `DATABASE_ERD.png`
3. Generate file SVG (chất lượng cao):
   - `ARCHITECTURE_DIAGRAM.svg`
   - `DATABASE_ERD.svg`

### Bước 3: Xem Ảnh
Mở file PNG/SVG vừa tạo bằng trình xem ảnh hoặc browser.

---

## 📱 Xem Trên GitHub

Sau khi push code lên GitHub, sơ đồ sẽ tự động hiển thị trong file `DIAGRAMS.md`.

**Lưu ý**: Cần cập nhật URL trong `DIAGRAMS.md`:
```markdown
Thay: YOUR_USERNAME/YOUR_REPO
Bằng: username-github-của-bạn/tên-repo-của-bạn
```

---

## 🎨 Xem Trong IDE

### Visual Studio Code
1. Cài extension: **PlantUML** (jebbs.plantuml)
2. Mở file `.puml`
3. Press `Alt + D`

### IntelliJ IDEA
1. Cài plugin: **PlantUML Integration**
2. Mở file `.puml`
3. Sơ đồ hiển thị tự động

---

## 📊 Tóm Tắt Sơ Đồ

### Sơ Đồ Kiến Trúc
- 7 layers: Client → Load Balancer → Frontend → Backend → Database → Cache → External
- 2 Frontend servers (Next.js)
- 2 Backend servers (Spring Boot)
- MySQL Master-Slave
- Redis Cache
- 4 External services (SePay, GHTK, Cloudinary, SMTP)

### Sơ Đồ ERD
- **22 bảng** chia thành 6 modules:
  - Auth: 5 bảng
  - Product: 2 bảng
  - Inventory: 10 bảng
  - Cart: 2 bảng
  - Order: 2 bảng
  - Payment: 1 bảng

---

## ❓ Troubleshooting

### Lỗi: "Java is not installed"
→ Cài Java JDK từ Oracle hoặc OpenJDK

### Lỗi: "Failed to download PlantUML"
→ Download thủ công từ: https://plantuml.com/download
→ Đặt file `plantuml.jar` vào thư mục gốc project

### Sơ đồ không hiển thị trên GitHub
→ Kiểm tra URL trong `DIAGRAMS.md` đã đúng chưa
→ File `.puml` phải ở branch `main` hoặc `master`
→ Repository phải là public

### Muốn chỉnh sửa sơ đồ
→ Mở file `.puml` bằng text editor
→ Chỉnh sửa theo PlantUML syntax
→ Chạy lại `generate-diagrams.bat`
