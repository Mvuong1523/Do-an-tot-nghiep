# Debug Token Issue - 403 Forbidden

## Quan sát từ logs

Từ console logs, tôi thấy:
```
✅ Orders data: Array(3)  <- Thành công!
❌ 403 Forbidden          <- Thất bại!
```

Điều này cho thấy:
1. ✅ Backend đã restart và code mới đã được load
2. ✅ Có request thành công → Authorization đang hoạt động
3. ❌ Có request thất bại → Có vấn đề với token

## Nguyên nhân có thể

### 1. Token không tồn tại hoặc hết hạn
```javascript
// Check trong console
console.log('auth_token:', localStorage.getItem('auth_token'))
console.log('token:', localStorage.getItem('token'))
```

### 2. Đang có nhiều tabs với users khác nhau
- Tab 1: Login as User A
- Tab 2: Login as User B
- Khi chuyển tab → Token bị override

### 3. Token không match với user trong database
- Token đã bị revoke
- User đã bị disable
- Token format không đúng

## Giải pháp

### Bước 1: Clear tất cả và login lại

```javascript
// Chạy trong console
localStorage.clear()
sessionStorage.clear()
// Sau đó refresh và login lại
```

### Bước 2: Kiểm tra token sau khi login

```javascript
// Sau khi login thành công
const token = localStorage.getItem('auth_token')
console.log('Token:', token)
console.log('Token length:', token?.length)
console.log('Token starts with:', token?.substring(0, 20))
```

### Bước 3: Test API với token

```javascript
// Test trực tiếp trong console
const token = localStorage.getItem('auth_token')
fetch('http://localhost:8080/api/inventory/purchase-orders', {
  headers: { 'Authorization': `Bearer ${token}` }
})
.then(r => r.json())
.then(d => console.log('Response:', d))
.catch(e => console.error('Error:', e))
```

### Bước 4: Kiểm tra user role

```javascript
// Decode JWT token (nếu là JWT)
const token = localStorage.getItem('auth_token')
if (token) {
  const parts = token.split('.')
  if (parts.length === 3) {
    const payload = JSON.parse(atob(parts[1]))
    console.log('Token payload:', payload)
    console.log('Authorities:', payload.authorities)
    console.log('Username:', payload.sub)
  }
}
```

## Testing Steps

### Test 1: Login as WAREHOUSE employee

```
1. Clear localStorage
2. Login với account có position = WAREHOUSE
3. Check console: localStorage.getItem('auth_token')
4. Navigate to /employee/warehouse/import
5. Check Network tab:
   - Request URL: http://localhost:8080/api/inventory/purchase-orders
   - Request Headers: Authorization: Bearer <token>
   - Response Status: Should be 200 OK
   - Response Body: Should have data array
```

### Test 2: Login as SALE employee

```
1. Clear localStorage (hoặc dùng incognito window)
2. Login với account có position = SALE
3. Check console: localStorage.getItem('auth_token')
4. Navigate to /employee/warehouse/import
5. Check Network tab:
   - Response Status: Should be 200 OK (vì đã thêm EMPLOYEE authority)
   - Response Body: Should have data array
   - UI: Should show yellow warning banner
   - UI: Create button should be disabled
```

### Test 3: Check backend logs

Khi gặp 403, check backend logs để xem:
```
DEBUG ... o.s.security.web.FilterChainProxy : Securing GET /api/inventory/purchase-orders
DEBUG ... o.s.s.w.a.AnonymousAuthenticationFilter : Set SecurityContextHolder to anonymous
DEBUG ... o.s.s.w.a.Http403ForbiddenEntryPoint : Pre-authenticated entry point called. Rejecting access
```

Nếu thấy "anonymous" → Token không được gửi hoặc không hợp lệ

## Common Issues & Solutions

### Issue 1: Token bị null
```javascript
localStorage.getItem('auth_token') // null
```

**Solution**: Login lại

### Issue 2: Token format sai
```javascript
localStorage.getItem('auth_token') // "undefined" hoặc "null" (string)
```

**Solution**: 
```javascript
localStorage.removeItem('auth_token')
// Login lại
```

### Issue 3: Multiple tabs conflict
**Solution**: Chỉ dùng 1 tab, hoặc dùng incognito cho user thứ 2

### Issue 4: Token expired
**Solution**: Login lại để lấy token mới

## Debug Script

Chạy script này trong console để debug:

```javascript
// Debug Token Script
console.log('=== TOKEN DEBUG ===')
console.log('auth_token:', localStorage.getItem('auth_token'))
console.log('token:', localStorage.getItem('token'))
console.log('auth-storage:', localStorage.getItem('auth-storage'))

// Try to parse auth-storage
try {
  const authStorage = JSON.parse(localStorage.getItem('auth-storage') || '{}')
  console.log('Parsed auth-storage:', authStorage)
  console.log('User:', authStorage.state?.user)
  console.log('Employee:', authStorage.state?.employee)
  console.log('Token from storage:', authStorage.state?.token)
} catch (e) {
  console.error('Failed to parse auth-storage:', e)
}

// Test API call
const token = localStorage.getItem('auth_token') || localStorage.getItem('token')
if (token) {
  console.log('Testing API with token...')
  fetch('http://localhost:8080/api/inventory/purchase-orders', {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  .then(r => {
    console.log('Response status:', r.status)
    return r.json()
  })
  .then(d => console.log('Response data:', d))
  .catch(e => console.error('API Error:', e))
} else {
  console.error('No token found!')
}
```

## Expected Results

### Khi thành công:
```
✅ Token exists in localStorage
✅ API returns 200 OK
✅ Response has data array
✅ Data displays in UI
✅ No 403 errors in console
```

### Khi thất bại:
```
❌ Token is null or undefined
❌ API returns 403 Forbidden
❌ Response has error message
❌ No data in UI
❌ Console shows "API returned error: Forbidden"
```

## Next Steps

1. **Clear localStorage và login lại**
2. **Chỉ dùng 1 tab/window**
3. **Run debug script trong console**
4. **Check Network tab để xem request/response**
5. **Nếu vẫn 403, check backend logs**

Nếu sau khi làm các bước trên vẫn 403, có thể cần kiểm tra:
- User có tồn tại trong database không?
- User có bị disable không?
- Token generation có đúng không?
- Spring Security configuration có đúng không?
