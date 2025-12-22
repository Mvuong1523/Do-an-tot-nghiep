# 🚀 Hướng dẫn Setup Ngrok trên Windows

## Cách 1: Cài đặt bằng Chocolatey (Khuyến nghị)

### Bước 1: Cài Chocolatey (nếu chưa có)
Mở PowerShell **với quyền Administrator** và chạy:

```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```

### Bước 2: Cài ngrok
```powershell
choco install ngrok -y
```

### Bước 3: Thêm authtoken
```powershell
ngrok config add-authtoken 36SM7cdNlEF0Kke4NPdWZcd2vK6_53ayYQ1hMcwd42JTV1Yw2
```

---

## Cách 2: Tải file .zip (Nhanh nhất)

### Bước 1: Tải ngrok
1. Vào: https://ngrok.com/download
2. Chọn **Windows (64-bit)**
3. Tải file `ngrok-v3-stable-windows-amd64.zip`

### Bước 2: Giải nén
1. Giải nén file zip
2. Copy file `ngrok.exe` vào thư mục dự án của bạn
   - Hoặc copy vào `C:\Windows\System32` để dùng global

### Bước 3: Thêm authtoken
Mở Command Prompt hoặc PowerShell trong thư mục chứa `ngrok.exe`:

```cmd
ngrok config add-authtoken 36SM7cdNlEF0Kke4NPdWZcd2vK6_53ayYQ1hMcwd42JTV1Yw2
```

---

## Cách 3: Cài bằng Scoop

### Bước 1: Cài Scoop (nếu chưa có)
```powershell
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
irm get.scoop.sh | iex
```

### Bước 2: Cài ngrok
```powershell
scoop install ngrok
```

### Bước 3: Thêm authtoken
```powershell
ngrok config add-authtoken 36SM7cdNlEF0Kke4NPdWZcd2vK6_53ayYQ1hMcwd42JTV1Yw2
```

---

## Kiểm tra cài đặt thành công

Chạy lệnh:
```cmd
ngrok version
```

Kết quả mong đợi:
```
ngrok version 3.x.x
```

---

## Sử dụng ngrok

### 1. Expose Backend (Spring Boot - port 8080)
```cmd
ngrok http 8080
```

Kết quả:
```
Session Status                online
Account                       [your-email]
Version                       3.x.x
Region                        United States (us)
Forwarding                    https://xxxx-xxxx-xxxx.ngrok-free.app -> http://localhost:8080
```

→ Copy URL `https://xxxx-xxxx-xxxx.ngrok-free.app` để dùng

### 2. Expose Frontend (Next.js - port 3000)
```cmd
ngrok http 3000
```

### 3. Expose cả 2 cùng lúc (2 terminal)

**Terminal 1 - Backend:**
```cmd
ngrok http 8080 --region=us
```

**Terminal 2 - Frontend:**
```cmd
ngrok http 3000 --region=us
```

---

## Cấu hình nâng cao

### Tạo file config ngrok
Tạo file `ngrok.yml` trong thư mục dự án:

```yaml
version: "2"
authtoken: 36SM7cdNlEF0Kke4NPdWZcd2vK6_53ayYQ1hMcwd42JTV1Yw2
tunnels:
  backend:
    proto: http
    addr: 8080
    subdomain: my-backend
  frontend:
    proto: http
    addr: 3000
    subdomain: my-frontend
```

Chạy cả 2 tunnel:
```cmd
ngrok start --all
```

---

## Script tự động (Khuyến nghị)

### Tạo file `start-ngrok.bat`

```batch
@echo off
echo Starting ngrok for Backend (port 8080)...
start "Ngrok Backend" ngrok http 8080 --region=us

timeout /t 3

echo Starting ngrok for Frontend (port 3000)...
start "Ngrok Frontend" ngrok http 3000 --region=us

echo.
echo Ngrok tunnels started!
echo Check the ngrok windows for URLs
pause
```

Chạy file này để start cả 2 tunnel cùng lúc!

---

## Lưu ý quan trọng

### 1. Free plan limitations
- ✅ 1 authtoken
- ✅ Unlimited tunnels (nhưng chỉ 1 agent)
- ⚠️ URL thay đổi mỗi lần restart
- ⚠️ Session timeout sau 2 giờ

### 2. Cập nhật URL trong code

Sau khi có ngrok URL, cập nhật trong frontend:

**File: `src/frontend/lib/api.ts`**
```typescript
// Thay đổi từ:
const API_BASE_URL = 'http://localhost:8080/api';

// Thành:
const API_BASE_URL = 'https://xxxx-xxxx-xxxx.ngrok-free.app/api';
```

### 3. CORS Configuration

Đảm bảo backend cho phép ngrok domain:

**File: `SecurityConfig.java`**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",
        "https://*.ngrok-free.app"  // Thêm dòng này
    ));
    // ...
}
```

---

## Troubleshooting

### Lỗi: "ngrok not found"
**Giải pháp:**
- Restart terminal sau khi cài
- Hoặc chạy từ thư mục chứa `ngrok.exe`

### Lỗi: "authtoken not found"
**Giải pháp:**
```cmd
ngrok config add-authtoken 36SM7cdNlEF0Kke4NPdWZcd2vK6_53ayYQ1hMcwd42JTV1Yw2
```

### Lỗi: "tunnel not found"
**Giải pháp:**
- Đảm bảo backend/frontend đang chạy trước khi start ngrok
- Kiểm tra port đúng (8080 cho backend, 3000 cho frontend)

### Lỗi: "ERR_NGROK_108"
**Giải pháp:**
- Authtoken không hợp lệ
- Tạo authtoken mới tại: https://dashboard.ngrok.com/get-started/your-authtoken

---

## Các lệnh hữu ích

```cmd
# Xem version
ngrok version

# Xem config
ngrok config check

# Xem authtoken
ngrok config edit

# Start với region cụ thể
ngrok http 8080 --region=us

# Start với subdomain (cần paid plan)
ngrok http 8080 --subdomain=my-app

# Xem log chi tiết
ngrok http 8080 --log=stdout

# Inspect traffic
# Mở browser: http://localhost:4040
```

---

## Kết luận

**Cách nhanh nhất:**
1. Tải ngrok.exe từ https://ngrok.com/download
2. Copy vào thư mục dự án
3. Chạy: `ngrok config add-authtoken 36SM7cdNlEF0Kke4NPdWZcd2vK6_53ayYQ1hMcwd42JTV1Yw2`
4. Chạy: `ngrok http 8080`

**Done!** 🎉

---

## Tài liệu tham khảo

- Official docs: https://ngrok.com/docs
- Dashboard: https://dashboard.ngrok.com
- Download: https://ngrok.com/download
