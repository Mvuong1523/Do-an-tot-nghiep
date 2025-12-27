# Test After Backend Restart

## Bước 1: Đợi backend khởi động

Chờ đến khi thấy log:
```
Started WebTmdtApplication in X.XXX seconds
```

Hoặc chạy command này để test:
```bash
curl http://localhost:8080/actuator/health
```

## Bước 2: Clear browser cache

Mở Console (F12) và chạy:
```javascript
localStorage.clear()
sessionStorage.clear()
location.reload()
```

## Bước 3: Login lại

1. Login với employee account (bất kỳ position nào)
2. Sau khi login, check token:
```javascript
console.log('Token:', localStorage.getItem('auth_token'))
```

## Bước 4: Test API trực tiếp

Chạy trong console để test API:
```javascript
const token = localStorage.getItem('auth_token')
fetch('http://localhost:8080/api/inventory/purchase-orders', {
  headers: { 'Authorization': `Bearer ${token}` }
})
.then(r => {
  console.log('Status:', r.status)
  return r.json()
})
.then(d => console.log('Data:', d))
```

**Expected result**:
- Status: 200 (not 403!)
- Data: { success: true, data: [...] }

## Bước 5: Navigate và test

1. Go to `/employee/warehouse/import`
2. Check console - should see:
   ```
   Fetching purchase orders: /api/inventory/purchase-orders
   Purchase orders response: {success: true, data: Array(X)}
   Orders data: Array(X)
   ```
3. Check Network tab:
   - Status: 200 OK
   - Response has data

## Bước 6: Test với các positions khác nhau

### Test với WAREHOUSE position
```
✅ Can view all data
✅ Can create/edit
✅ No warning banner
✅ Submit buttons enabled
```

### Test với SALE position
```
✅ Can view all data
✅ Cannot create/edit
✅ Yellow warning banner visible
✅ Submit buttons disabled
```

### Test với PRODUCT_MANAGER position
```
✅ Can view all data
✅ Cannot create/edit warehouse items
✅ Yellow warning banner on warehouse pages
```

## Debug nếu vẫn 403

### 1. Check backend logs
```
# Look for this in backend console
DEBUG ... o.s.security.web.FilterChainProxy : Securing GET /api/inventory/purchase-orders
DEBUG ... JwtAuthenticationFilter : Token validated successfully
DEBUG ... JwtAuthenticationFilter : Authorities: [EMPLOYEE, ...]
```

Nếu thấy "anonymous" → Token không hợp lệ

### 2. Check InventoryController.java

Verify file có đúng code này:
```java
@GetMapping("/purchase-orders")
@PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN', 'EMPLOYEE')")
public ApiResponse getPurchaseOrders(@RequestParam(required = false) String status) {
    // ...
}
```

### 3. Verify compile

Check file đã được compile:
```bash
# Check if class file exists and is recent
ls -la target/classes/com/doan/WEB_TMDT/module/inventory/controller/InventoryController.class
```

### 4. Force clean rebuild

```bash
mvn clean
mvn compile
mvn spring-boot:run
```

## Quick Test Script

Paste this in browser console after login:
```javascript
// Quick Test Script
console.log('=== QUICK TEST ===')

const token = localStorage.getItem('auth_token') || localStorage.getItem('token')
console.log('Token exists:', !!token)

if (!token) {
  console.error('❌ No token found! Please login first.')
} else {
  console.log('✅ Token found')
  
  // Test API
  console.log('Testing API...')
  fetch('http://localhost:8080/api/inventory/purchase-orders', {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  .then(r => {
    console.log('Response status:', r.status)
    if (r.status === 200) {
      console.log('✅ API call successful!')
    } else if (r.status === 403) {
      console.error('❌ Still getting 403 Forbidden')
      console.error('Possible issues:')
      console.error('1. Backend not restarted with new code')
      console.error('2. Token invalid or expired')
      console.error('3. User does not have EMPLOYEE authority')
    }
    return r.json()
  })
  .then(d => {
    console.log('Response data:', d)
    if (d.success && d.data) {
      console.log(`✅ Got ${d.data.length} items`)
    }
  })
  .catch(e => console.error('Error:', e))
}
```

## Expected Timeline

1. **T+0s**: Backend starts
2. **T+30s**: Compiling...
3. **T+45s**: Initializing Spring...
4. **T+60s**: Database connection...
5. **T+75s**: Application ready! ✅

After "Application ready", wait 5 more seconds then test.

## Success Criteria

✅ No 403 errors in console
✅ Data displays in all warehouse pages
✅ WAREHOUSE position can create/edit
✅ Other positions see warning banner
✅ Other positions have disabled submit buttons

## If Still Failing

1. Check if you edited the RIGHT file:
   - `src/main/java/com/doan/WEB_TMDT/module/inventory/controller/InventoryController.java`
   
2. Check if Maven compiled the changes:
   - Look for "Compiling 253 source files" in mvn output
   - Check timestamp of target/classes files

3. Check if Spring loaded the new class:
   - Restart backend completely
   - Check logs for any errors during startup

4. Check user authorities in database:
   ```sql
   SELECT * FROM employees WHERE id = YOUR_EMPLOYEE_ID;
   ```
   
5. Check JWT token payload:
   ```javascript
   const token = localStorage.getItem('auth_token')
   const parts = token.split('.')
   const payload = JSON.parse(atob(parts[1]))
   console.log('Token payload:', payload)
   console.log('Authorities:', payload.authorities)
   ```

## Contact Points

If all else fails, check:
1. Is MySQL running?
2. Is port 8080 free?
3. Are there any compilation errors?
4. Is the token actually being sent in request headers?
