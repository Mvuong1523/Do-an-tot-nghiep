# 🔄 Restart Frontend & Clear Cache

## Vấn Đề

Frontend vẫn gọi endpoint cũ `/api/accounting/financial-statement` mặc dù code đã sửa thành `/api/accounting/financial-statement/dashboard`.

## Nguyên Nhân

1. **Next.js dev server cache** - Chưa rebuild
2. **Browser cache** - Vẫn dùng JS cũ
3. **Service Worker** - Có thể cache request

## ✅ Giải Pháp

### 1. Stop Next.js Dev Server

```bash
# Trong terminal đang chạy Next.js, nhấn:
Ctrl + C
```

### 2. Clear Next.js Cache

```bash
cd src/frontend

# Xóa cache Next.js
rm -rf .next
# Hoặc trên Windows:
rmdir /s /q .next

# Xóa node_modules cache (optional, nếu vẫn lỗi)
rm -rf node_modules/.cache
```

### 3. Restart Next.js

```bash
npm run dev
```

### 4. Clear Browser Cache

**Chrome/Edge**:
1. Mở DevTools (F12)
2. Right-click vào nút Refresh
3. Chọn "Empty Cache and Hard Reload"

**Hoặc**:
- Nhấn `Ctrl + Shift + R` (Windows)
- Nhấn `Cmd + Shift + R` (Mac)

### 5. Clear Service Worker (Nếu có)

1. Mở DevTools (F12)
2. Tab "Application"
3. Sidebar "Service Workers"
4. Click "Unregister" nếu có

### 6. Test Lại

1. Vào: http://localhost:3000/employee/accounting
2. Mở DevTools → Tab "Network"
3. Refresh trang
4. Check xem có gọi đúng endpoint `/dashboard` không

## 🧪 Test API Trực Tiếp

Mở file `test-accounting-dashboard.html` trong browser:

1. **Lấy token**:
   - Login vào http://localhost:3000/login
   - Mở DevTools → Tab "Application" → "Local Storage"
   - Copy giá trị của `auth_token`

2. **Paste token** vào input box

3. **Click các button test**:
   - "Test /dashboard" → Should work ✅
   - "Test /financial-statement" → Should fail (missing params) ❌
   - "Test /financial-statement/dashboard" → Should work ✅

## 📝 Expected Results

### ✅ Success Response

```json
{
  "success": true,
  "message": "Báo cáo tài chính",
  "data": {
    "startDate": "2024-12-01",
    "endDate": "2024-12-31",
    "revenue": {
      "totalRevenue": 1000000,
      "productRevenue": 1000000,
      "shippingRevenue": 0,
      "otherRevenue": 0,
      "orderCount": 5
    },
    "expenses": {
      "totalExpense": 200000,
      ...
    },
    "profit": {
      "netProfit": 800000,
      ...
    }
  }
}
```

### ❌ Error Response (Missing Params)

```json
{
  "success": false,
  "message": "Required request parameter 'startDate' for method parameter type LocalDate is not present"
}
```

## 🔍 Debug Network Requests

Trong DevTools → Network tab, check:

1. **Request URL**: Phải là `/api/accounting/financial-statement/dashboard`
2. **Status**: Phải là 200 (không phải 500)
3. **Response**: Phải có data

Nếu vẫn thấy request đến `/api/accounting/financial-statement` (không có `/dashboard`):
- Frontend chưa rebuild → Restart Next.js
- Browser cache → Hard refresh (Ctrl + Shift + R)

## 🚨 Nếu Vẫn Lỗi 500

Check backend console để xem error message chi tiết:

```bash
# Trong terminal backend, tìm dòng:
ERROR ... Exception ...
```

Hoặc check file log (nếu có):
```bash
tail -f logs/spring.log
```

---

**Lưu ý**: Sau khi restart, trang có thể mất vài giây để rebuild.
