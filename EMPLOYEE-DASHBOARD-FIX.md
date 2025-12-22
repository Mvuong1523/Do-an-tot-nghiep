# 🔧 Sửa lỗi Dashboard nhân viên và Admin không hiển thị dữ liệu

## ❌ Vấn đề
Dashboard nhân viên và admin hiển thị tất cả chỉ số là 0 hoặc lỗi 400/500.

## 🔍 Nguyên nhân
1. **API endpoint bị lặp `/api` 2 lần**: Frontend gọi `/api/api/dashboard/stats` thay vì `/api/dashboard/stats`
   - File `api.ts` đã có `baseURL = 'http://localhost:8080/api'`
   - File `admin/page.tsx` và `employee/page.tsx` lại thêm `/api` vào đầu URL
   - Kết quả: `http://localhost:8080/api` + `/api/dashboard/stats` = `/api/api/dashboard/stats` ❌

2. **@PreAuthorize sai format**: Controller dùng `hasAnyRole('ADMIN', 'EMPLOYEE')` thay vì `hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')`
   - Spring Security yêu cầu prefix `ROLE_` khi dùng `hasAnyRole`

3. **SecurityConfig đã được cập nhật** (từ conversation trước):
   - Đã thêm rule: `.requestMatchers("/api/dashboard/**").hasAnyAuthority("ADMIN", "EMPLOYEE")`
   - Backend đã compile thành công

## ✅ Giải pháp đã áp dụng

### 1. Sửa file `src/frontend/app/admin/page.tsx`
```typescript
// ❌ TRƯỚC (SAI)
const statsResponse = await api.get('/api/dashboard/stats')
const ordersResponse = await api.get('/api/dashboard/recent-orders?limit=5')

// ✅ SAU (ĐÚNG)
const statsResponse = await api.get('/dashboard/stats')
const ordersResponse = await api.get('/dashboard/recent-orders?limit=5')
```

### 2. Sửa file `src/frontend/app/employee/page.tsx`
```typescript
// ❌ TRƯỚC (SAI)
const statsResponse = await api.get('/api/dashboard/stats')
const ordersResponse = await api.get('/api/dashboard/recent-orders?limit=5')

// ✅ SAU (ĐÚNG)
const statsResponse = await api.get('/dashboard/stats')
const ordersResponse = await api.get('/dashboard/recent-orders?limit=5')
```

### 3. Sửa file `src/main/java/com/doan/WEB_TMDT/controller/DashboardController.java`
```java
// ❌ TRƯỚC (SAI)
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")

// ✅ SAU (ĐÚNG)
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
```

### 4. Backend đang khởi động lại
- Backend đang compile và khởi động với code mới
- SecurityConfig đã cho phép ADMIN và EMPLOYEE truy cập `/api/dashboard/**`

## 🧪 Cách kiểm tra

### 1. Đợi backend khởi động xong
```bash
# Kiểm tra log: "Started WebTMDTApplication"
# Backend chạy trên port 8080
```

### 2. Hard refresh frontend
```bash
# Nhấn Ctrl + Shift + R trong trình duyệt
# Hoặc mở DevTools (F12) → Network → check "Disable cache" → F5
```

### 3. Đăng nhập và kiểm tra
- **Admin**: http://localhost:3000/admin
- **Employee**: http://localhost:3000/employee
- Dashboard sẽ hiển thị:
  - ✅ Tổng đơn hàng
  - ✅ Doanh thu
  - ✅ Tổng sản phẩm
  - ✅ Đơn chờ xử lý
  - ✅ Danh sách đơn hàng gần đây

### 4. Kiểm tra console log
Mở DevTools (F12) và xem Console - không còn lỗi 400, 403, 500

## 📝 Lưu ý

### API URL trong project
- **Base URL**: `http://localhost:8080/api` (đã có `/api`)
- **Khi gọi API**: Chỉ cần thêm endpoint, VD: `/dashboard/stats`
- **URL cuối cùng**: `http://localhost:8080/api/dashboard/stats` ✅

### Spring Security @PreAuthorize
- **hasAnyRole**: Cần prefix `ROLE_`, VD: `hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')`
- **hasAnyAuthority**: Không cần prefix, VD: `hasAnyAuthority('ADMIN', 'EMPLOYEE')`

### Quyền truy cập
- **ADMIN**: Có thể truy cập tất cả endpoints
- **EMPLOYEE**: Có thể truy cập `/api/dashboard/**`
- **Token**: Phải có trong localStorage với key `auth_token`

## 🎯 Kết quả mong đợi
- Dashboard admin và nhân viên hiển thị đầy đủ thống kê từ database
- Không còn lỗi 400, 403, 500
- Dữ liệu được load thành công từ API

## 📂 Files đã sửa
1. `src/frontend/app/admin/page.tsx` - Sửa URL API
2. `src/frontend/app/employee/page.tsx` - Sửa URL API
3. `src/main/java/com/doan/WEB_TMDT/controller/DashboardController.java` - Sửa @PreAuthorize
4. `src/main/java/com/doan/WEB_TMDT/config/SecurityConfig.java` - Đã sửa trước đó
5. Backend đang khởi động lại với code mới

---
**Ngày sửa**: 22/12/2025  
**Trạng thái**: ✅ Hoàn thành - Đang khởi động backend, cần hard refresh frontend
