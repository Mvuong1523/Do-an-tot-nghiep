# Hướng dẫn cài đặt ngrok để nhận webhook từ SePay

## Bước 1: Tải và cài đặt ngrok

1. Truy cập: https://ngrok.com/download
2. Tải bản Windows
3. Giải nén file ngrok.exe vào thư mục dự án hoặc C:\ngrok

## Bước 2: Đăng ký tài khoản ngrok (miễn phí)

1. Truy cập: https://dashboard.ngrok.com/signup
2. Đăng ký tài khoản miễn phí
3. Lấy authtoken tại: https://dashboard.ngrok.com/get-started/your-authtoken

## Bước 3: Kết nối authtoken

```bash
ngrok config add-authtoken YOUR_AUTHTOKEN_HERE
```

## Bước 4: Chạy ngrok

```bash
ngrok http 8080
```

Bạn sẽ thấy output như:
```
Forwarding  https://abc123.ngrok-free.app -> http://localhost:8080
```

## Bước 5: Cấu hình webhook URL trên SePay

1. Đăng nhập vào SePay dashboard
2. Vào phần Webhook Settings
3. Nhập URL: `https://abc123.ngrok-free.app/api/payment/sepay/webhook`
4. Lưu lại

## Bước 6: Test thanh toán

Bây giờ khi bạn chuyển khoản thật, SePay sẽ gọi webhook và tự động cập nhật trạng thái!

## Lưu ý:

- Mỗi lần chạy ngrok, URL sẽ thay đổi (bản free)
- Cần cập nhật lại webhook URL trên SePay mỗi lần restart ngrok
- Nếu muốn URL cố định, nâng cấp lên ngrok Pro
