# Hướng dẫn Test Module Công nợ Nhà cung cấp

## 📋 Tổng quan
Module công nợ tự động tạo khi nhập hàng và theo dõi thanh toán cho nhà cung cấp.

## 🔄 Luồng test đầy đủ

### Bước 1: Khởi động hệ thống

1. **Khởi động MySQL**
   - Mở XAMPP hoặc MySQL Workbench
   - Start MySQL Server

2. **Khởi động Backend**
   ```bash
   ./mvnw.cmd spring-boot:run
   ```
   - Đợi đến khi thấy: "Started WebTMDTApplication"

3. **Khởi động Frontend**
   ```bash
   cd src/frontend
   npm run dev
   ```
   - Truy cập: http://localhost:3000

---

### Bước 2: Đăng nhập

1. Truy cập: http://localhost:3000/login
2. Đăng nhập với tài khoản:
   - **Admin**: admin@example.com / password
   - **Warehouse Manager**: warehouse@example.com / password

---

### Bước 3: Nhập hàng (Tạo Purchase Order)

1. **Vào trang Warehouse**
   - Menu: Warehouse → Import/Export

2. **Tạo phiếu nhập hàng mới**
   - Click "Tạo phiếu nhập"
   - Điền thông tin nhà cung cấp:
     ```
     Tên NCC: Công ty TNHH ABC
     Mã số thuế: 0123456789
     Email: abc@company.com
     SĐT: 0901234567
     Địa chỉ: 123 Đường ABC, Hà Nội
     Tài khoản NH: 1234567890 - Vietcombank
     Điều khoản TT: Thanh toán trong 30 ngày
     Số ngày nợ: 30
     ```

3. **Thêm sản phẩm vào phiếu nhập**
   - Chọn sản phẩm: iPhone 15 Pro Max
   - Số lượng: 10
   - Giá nhập: 25,000,000 VNĐ
   - Bảo hành: 12 tháng
   
   - Thêm sản phẩm khác: Samsung Galaxy S24 Ultra
   - Số lượng: 5
   - Giá nhập: 22,000,000 VNĐ
   - Bảo hành: 12 tháng

4. **Tạo phiếu nhập**
   - Click "Tạo phiếu nhập"
   - Ghi nhớ mã PO (VD: PO-20231216-001)

---

### Bước 4: Hoàn tất nhập hàng (Tạo công nợ)

1. **Vào danh sách phiếu nhập**
   - Tìm phiếu vừa tạo (status: CREATED)

2. **Nhập serial cho từng sản phẩm**
   - Click "Hoàn tất nhập"
   - Nhập serial cho iPhone:
     ```
     IP15PM001
     IP15PM002
     IP15PM003
     IP15PM004
     IP15PM005
     IP15PM006
     IP15PM007
     IP15PM008
     IP15PM009
     IP15PM010
     ```
   
   - Nhập serial cho Samsung:
     ```
     SS24U001
     SS24U002
     SS24U003
     SS24U004
     SS24U005
     ```

3. **Xác nhận hoàn tất**
   - Click "Hoàn tất nhập hàng"
   - ✅ Hệ thống tự động tạo công nợ!

**Kết quả:**
- Tổng tiền: 10 × 25,000,000 + 5 × 22,000,000 = 360,000,000 VNĐ
- Ngày hạn thanh toán: Ngày nhập + 30 ngày
- Trạng thái: UNPAID (Chưa trả)

---

### Bước 5: Kiểm tra công nợ

1. **Vào trang Kế toán**
   - Menu: Admin → Accounting → Công nợ NCC

2. **Xem thống kê**
   - Tổng công nợ: 360,000,000 VNĐ
   - Số công nợ: 1
   - Quá hạn: 0
   - Sắp đến hạn: 0 (nếu còn xa)

3. **Xem chi tiết công nợ**
   - Mã công nợ: AP-20231216-XXXX
   - Nhà cung cấp: Công ty TNHH ABC
   - Mã PO: PO-20231216-001
   - Tổng tiền: 360,000,000 VNĐ
   - Đã trả: 0 VNĐ
   - Còn nợ: 360,000,000 VNĐ
   - Ngày hạn: (ngày nhập + 30 ngày)
   - Trạng thái: Chưa trả

---

### Bước 6: Thanh toán công nợ (Trả một phần)

1. **Click "Thanh toán" trên công nợ**

2. **Điền thông tin thanh toán**
   ```
   Số tiền: 200,000,000 VNĐ
   Ngày thanh toán: (hôm nay)
   Phương thức: Chuyển khoản
   Số tham chiếu: CK123456789
   Ghi chú: Thanh toán đợt 1
   ```

3. **Xác nhận thanh toán**
   - Click "Thanh toán"

**Kết quả:**
- Đã trả: 200,000,000 VNĐ
- Còn nợ: 160,000,000 VNĐ
- Trạng thái: PARTIAL (Trả một phần)

---

### Bước 7: Thanh toán tiếp (Trả hết)

1. **Click "Thanh toán" lần nữa**

2. **Điền thông tin**
   ```
   Số tiền: 160,000,000 VNĐ
   Ngày thanh toán: (hôm nay)
   Phương thức: Chuyển khoản
   Số tham chiếu: CK987654321
   Ghi chú: Thanh toán đợt 2 - Hoàn tất
   ```

3. **Xác nhận thanh toán**

**Kết quả:**
- Đã trả: 360,000,000 VNĐ
- Còn nợ: 0 VNĐ
- Trạng thái: PAID (Đã trả hết)

---

### Bước 8: Xuất hàng bán (Publish sản phẩm)

1. **Vào Product Manager**
   - Menu: Product Manager → Products

2. **Publish sản phẩm từ kho**
   - Click "Publish từ kho"
   - Chọn sản phẩm: iPhone 15 Pro Max
   - Giá bán: 30,000,000 VNĐ
   - Mô tả, hình ảnh...
   - Click "Publish"

3. **Lặp lại cho Samsung Galaxy S24 Ultra**
   - Giá bán: 27,000,000 VNĐ

---

### Bước 9: Khách hàng mua hàng

1. **Đăng xuất và đăng ký tài khoản khách**
   - Email: customer@test.com
   - Password: password123
   - Họ tên: Nguyễn Văn A
   - SĐT: 0912345678

2. **Thêm sản phẩm vào giỏ**
   - Vào trang Products
   - Chọn iPhone 15 Pro Max
   - Click "Thêm vào giỏ hàng"

3. **Thanh toán**
   - Vào giỏ hàng
   - Click "Thanh toán"
   - Điền địa chỉ giao hàng
   - Chọn phương thức: COD hoặc Chuyển khoản
   - Xác nhận đơn hàng

---

### Bước 10: Xử lý đơn hàng (Admin/Sales)

1. **Đăng nhập Admin**

2. **Vào Orders**
   - Menu: Admin → Orders

3. **Xử lý đơn hàng**
   - Tìm đơn vừa tạo
   - Cập nhật trạng thái:
     - PENDING → CONFIRMED
     - CONFIRMED → PROCESSING
     - PROCESSING → SHIPPED
     - SHIPPED → DELIVERED

4. **Xác nhận thanh toán** (nếu COD)
   - Sau khi DELIVERED
   - Cập nhật trạng thái thanh toán

---

## ✅ Checklist Test

### Backend
- [ ] Tạo PO thành công
- [ ] Hoàn tất nhập hàng thành công
- [ ] Công nợ tự động được tạo
- [ ] Tính toán tổng tiền đúng
- [ ] Ngày hạn thanh toán đúng (ngày nhập + số ngày nợ)
- [ ] Thanh toán một phần thành công
- [ ] Trạng thái chuyển sang PARTIAL
- [ ] Thanh toán hết thành công
- [ ] Trạng thái chuyển sang PAID
- [ ] Lịch sử thanh toán đầy đủ

### Frontend
- [ ] Hiển thị danh sách công nợ
- [ ] Thống kê chính xác
- [ ] Filter theo trạng thái hoạt động
- [ ] Search hoạt động
- [ ] Modal thanh toán hiển thị đúng
- [ ] Validation form thanh toán
- [ ] Toast notification hiển thị
- [ ] Refresh data sau thanh toán

### Luồng bán hàng
- [ ] Publish sản phẩm từ kho thành công
- [ ] Khách hàng thêm vào giỏ thành công
- [ ] Checkout thành công
- [ ] Tạo đơn hàng thành công
- [ ] Cập nhật trạng thái đơn hàng
- [ ] Xuất kho tự động

---

## 🐛 Các lỗi thường gặp

### 1. MySQL không kết nối
**Lỗi:** `Communications link failure`
**Giải pháp:** 
- Kiểm tra MySQL đã chạy chưa
- Kiểm tra port 3306
- Kiểm tra username/password trong application.properties

### 2. Không tạo được công nợ
**Lỗi:** Hoàn tất nhập hàng nhưng không thấy công nợ
**Giải pháp:**
- Kiểm tra log backend
- Đảm bảo Supplier có paymentTermDays
- Kiểm tra SupplierPayableService đã được inject

### 3. Frontend không load dữ liệu
**Lỗi:** Trang trắng hoặc loading mãi
**Giải pháp:**
- Kiểm tra console browser (F12)
- Kiểm tra backend đã chạy chưa
- Kiểm tra CORS
- Kiểm tra token authentication

### 4. Không thêm được vào giỏ hàng
**Lỗi:** "Vui lòng đăng nhập"
**Giải pháp:**
- Đăng nhập lại
- Kiểm tra token trong localStorage
- Clear cache browser

---

## 📊 Dữ liệu test mẫu

### Nhà cung cấp 1
```
Tên: Công ty TNHH Điện tử ABC
MST: 0123456789
Email: abc@electronics.com
SĐT: 0901234567
Địa chỉ: 123 Trần Hưng Đạo, Hà Nội
TK NH: 1234567890 - Vietcombank
Số ngày nợ: 30
```

### Nhà cung cấp 2
```
Tên: Công ty CP Công nghệ XYZ
MST: 9876543210
Email: xyz@tech.com
SĐT: 0987654321
Địa chỉ: 456 Nguyễn Trãi, TP.HCM
TK NH: 0987654321 - Techcombank
Số ngày nợ: 60
```

### Sản phẩm test
```
1. iPhone 15 Pro Max 256GB
   - Giá nhập: 25,000,000 VNĐ
   - Giá bán: 30,000,000 VNĐ
   - Số lượng: 10

2. Samsung Galaxy S24 Ultra
   - Giá nhập: 22,000,000 VNĐ
   - Giá bán: 27,000,000 VNĐ
   - Số lượng: 5

3. MacBook Pro M3
   - Giá nhập: 45,000,000 VNĐ
   - Giá bán: 52,000,000 VNĐ
   - Số lượng: 3
```

---

## 🎯 Kết quả mong đợi

Sau khi test xong, bạn sẽ có:

1. ✅ **Công nợ được tạo tự động** khi nhập hàng
2. ✅ **Theo dõi công nợ** theo nhà cung cấp
3. ✅ **Thanh toán công nợ** từng phần hoặc toàn bộ
4. ✅ **Lịch sử thanh toán** đầy đủ
5. ✅ **Thống kê công nợ** chính xác
6. ✅ **Cảnh báo quá hạn** (nếu có)
7. ✅ **Báo cáo công nợ** theo thời gian

---

## 📝 Ghi chú

- Module công nợ hoạt động độc lập, không ảnh hưởng đến luồng nhập/xuất hàng
- Nếu tạo công nợ thất bại, nhập hàng vẫn thành công (có log warning)
- Có thể thanh toán nhiều lần cho một công nợ
- Không thể thanh toán vượt quá số tiền còn nợ
- Trạng thái tự động cập nhật sau mỗi lần thanh toán

---

**Chúc bạn test thành công! 🎉**
