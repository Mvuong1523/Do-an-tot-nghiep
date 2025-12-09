# Hướng dẫn quản lý nhiều tài khoản ngân hàng với SePay

## 🎯 Tổng quan

Hệ thống đã được cập nhật để hỗ trợ quản lý nhiều tài khoản ngân hàng và lưu trữ thông tin SePay riêng cho từng tài khoản.

## ⚠️ Lưu ý quan trọng về SePay

**SePay KHÔNG TỰ ĐỘNG đăng ký tài khoản ngân hàng!**

- Mỗi tài khoản ngân hàng cần được đăng ký **THỦ CÔNG** trên website SePay
- Sau khi đăng ký, bạn sẽ nhận được:
  - **API Token**: Dùng để xác thực webhook
  - **Merchant ID**: ID định danh merchant (tùy chọn)
- Chỉ các tài khoản đã đăng ký trên SePay mới nhận được webhook khi có giao dịch

## 📋 Quy trình sử dụng

### Bước 1: Đăng ký tài khoản trên SePay

1. Truy cập website SePay: https://sepay.vn
2. Đăng ký tài khoản merchant
3. Thêm tài khoản ngân hàng của bạn vào SePay
4. Lấy **API Token** và **Merchant ID** từ dashboard

### Bước 2: Thêm tài khoản vào hệ thống

1. Đăng nhập với quyền Admin
2. Vào menu **Kế toán** → **Quản lý tài khoản ngân hàng**
3. Click **Thêm tài khoản**
4. Điền thông tin:
   - **Ngân hàng**: Chọn ngân hàng (MBBank, VCB, ACB...)
   - **Số tài khoản**: Số tài khoản ngân hàng
   - **Tên tài khoản**: Tên chủ tài khoản
   - **Ghi chú**: Mô tả (tùy chọn)
   - **SePay API Token**: Token từ SePay (quan trọng!)
   - **SePay Merchant ID**: Merchant ID từ SePay (tùy chọn)
   - **Kích hoạt**: Bật để sử dụng
   - **Đặt làm mặc định**: Tài khoản mặc định sẽ được dùng cho thanh toán

### Bước 3: Đặt tài khoản làm mặc định

- Chỉ có **1 tài khoản mặc định** tại một thời điểm
- Tài khoản mặc định sẽ được dùng để:
  - Tạo mã QR thanh toán
  - Nhận webhook từ SePay
  - Xác thực giao dịch

### Bước 4: Chuyển đổi giữa các tài khoản

Khi muốn đổi sang tài khoản khác:
1. Click icon ⭐ (ngôi sao) bên cạnh tài khoản muốn dùng
2. Tài khoản đó sẽ trở thành mặc định
3. Tất cả thanh toán mới sẽ dùng tài khoản này

## 🔧 Tính năng

### Quản lý tài khoản

- ✅ **Thêm/Sửa/Xóa** tài khoản ngân hàng
- ✅ **Kích hoạt/Tạm dừng** tài khoản
- ✅ **Đặt làm mặc định** - chỉ 1 tài khoản mặc định
- ✅ **Lưu SePay Token** riêng cho từng tài khoản
- ✅ **Badge hiển thị**:
  - 🟡 **Mặc định**: Tài khoản đang dùng
  - 🟢 **Kích hoạt**: Tài khoản sẵn sàng (không phải mặc định)
  - ⚫ **Tạm dừng**: Tài khoản không dùng

### Thanh toán

- Khi khách hàng tạo đơn hàng:
  - Hệ thống lấy tài khoản **mặc định**
  - Tạo mã QR với thông tin tài khoản đó
  - Lưu thông tin tài khoản vào payment

- Khi nhận webhook từ SePay:
  - Hệ thống lấy **API Token** từ tài khoản mặc định
  - Xác thực webhook (nếu có token)
  - Cập nhật trạng thái thanh toán

## 📁 Files đã cập nhật

### Backend
- `BankAccount.java`: Thêm `sepayApiToken` và `sepayMerchantId`
- `BankAccountRequest.java`: Thêm fields cho SePay
- `BankAccountResponse.java`: Thêm fields cho SePay
- `PaymentServiceImpl.java`: Sử dụng token từ bank account thay vì config

### Frontend
- `app/admin/bank-accounts/page.tsx`: Thêm input fields cho SePay token

## 🔐 Bảo mật

- API Token được lưu trong database (nên mã hóa trong production)
- Chỉ Admin mới có quyền xem/sửa tài khoản ngân hàng
- Token được dùng để xác thực webhook từ SePay

## 💡 Tips

1. **Luôn có tài khoản mặc định**: Đảm bảo có ít nhất 1 tài khoản được đặt làm mặc định
2. **Nhập đúng token**: Token sai sẽ khiến webhook không hoạt động
3. **Test trước khi dùng**: Thử thanh toán nhỏ để kiểm tra webhook
4. **Backup token**: Lưu token ở nơi an toàn phòng trường hợp mất

## 🐛 Troubleshooting

### Webhook không hoạt động?
- ✅ Kiểm tra tài khoản đã đăng ký trên SePay chưa
- ✅ Kiểm tra API Token đã nhập đúng chưa
- ✅ Kiểm tra tài khoản đã được đặt làm mặc định chưa
- ✅ Kiểm tra tài khoản đang ở trạng thái "Kích hoạt"

### Không tạo được QR code?
- ✅ Kiểm tra có tài khoản mặc định chưa
- ✅ Kiểm tra thông tin tài khoản (số TK, tên TK) đã đúng chưa

### Muốn đổi tài khoản nhận tiền?
- ✅ Click icon ⭐ bên cạnh tài khoản muốn dùng
- ✅ Tài khoản đó sẽ trở thành mặc định ngay lập tức
