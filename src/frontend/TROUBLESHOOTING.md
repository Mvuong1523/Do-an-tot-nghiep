# Hướng dẫn giải quyết lỗi npm PowerShell

## Vấn đề gặp phải
```
npm : File C:\Program Files\nodejs\npm.ps1 cannot be loaded because running scripts is disabled on this system.
```

## Nguyên nhân
1. **Node.js chưa được cài đặt** hoặc chưa được thêm vào PATH
2. **PowerShell Execution Policy** bị hạn chế
3. **npm.ps1** không thể chạy do policy bảo mật

## Giải pháp

### Bước 1: Kiểm tra Node.js đã cài đặt chưa
Mở Command Prompt (cmd) và chạy:
```cmd
node --version
npm --version
```

Nếu không có output hoặc báo lỗi "not recognized", thì Node.js chưa được cài đặt.

### Bước 2: Cài đặt Node.js
1. Truy cập: https://nodejs.org/
2. Tải phiên bản **LTS** (Long Term Support)
3. Chạy file installer (.msi)
4. **Quan trọng**: Đảm bảo tick vào "Add to PATH" trong quá trình cài đặt

### Bước 3: Restart Terminal
Sau khi cài đặt xong:
1. Đóng tất cả terminal/command prompt
2. Mở lại Command Prompt hoặc PowerShell
3. Chạy lại: `node --version` và `npm --version`

### Bước 4: Giải quyết PowerShell Execution Policy (nếu cần)

#### Cách 1: Sử dụng Command Prompt thay vì PowerShell
```cmd
cd "C:\Users\Quang\Desktop\Đồ án TN\Do-an-tot-nghiep\src\frontend"
npm install
npm run dev
```

#### Cách 2: Chạy PowerShell với quyền Administrator
1. Click chuột phải vào PowerShell
2. Chọn "Run as Administrator"
3. Chạy lệnh:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```
4. Nhập `Y` để confirm
5. Chạy lại npm commands

#### Cách 3: Bypass execution policy cho session hiện tại
```powershell
PowerShell -ExecutionPolicy Bypass -Command "npm install"
```

### Bước 5: Cài đặt và chạy Frontend
Sau khi Node.js đã hoạt động:

```cmd
# Di chuyển vào thư mục frontend
cd "C:\Users\Quang\Desktop\Đồ án TN\Do-an-tot-nghiep\src\frontend"

# Cài đặt dependencies
npm install

# Chạy development server
npm run dev
```

### Bước 6: Truy cập website
Mở trình duyệt và truy cập: **http://localhost:3000**

## Troubleshooting

### Nếu vẫn gặp lỗi PowerShell:
1. **Sử dụng Command Prompt** thay vì PowerShell
2. **Chạy PowerShell với quyền Administrator**
3. **Tạm thời disable antivirus** (có thể block npm)

### Nếu Node.js không được nhận diện:
1. **Restart máy tính** sau khi cài đặt Node.js
2. **Kiểm tra PATH environment variable**:
   - Windows + R → `sysdm.cpl` → Advanced → Environment Variables
   - Thêm `C:\Program Files\nodejs\` vào PATH nếu chưa có

### Alternative: Sử dụng Node Version Manager
Nếu gặp vấn đề với cài đặt Node.js trực tiếp:
1. Cài đặt **nvm-windows**: https://github.com/coreybutler/nvm-windows
2. Chạy:
```cmd
nvm install latest
nvm use latest
```

## Kiểm tra thành công
Sau khi cài đặt thành công, bạn sẽ thấy:
```cmd
C:\> node --version
v18.17.0

C:\> npm --version
9.6.7
```

## Lưu ý quan trọng
- **Luôn sử dụng Command Prompt** nếu PowerShell gặp vấn đề
- **Cài đặt Node.js LTS** (phiên bản ổn định)
- **Restart terminal** sau khi cài đặt
- **Kiểm tra PATH** nếu command không được nhận diện

## Liên hệ hỗ trợ
Nếu vẫn gặp vấn đề, hãy:
1. Chụp screenshot lỗi
2. Kiểm tra phiên bản Windows
3. Thử cài đặt Node.js portable version
4. Sử dụng WSL (Windows Subsystem for Linux) nếu có
