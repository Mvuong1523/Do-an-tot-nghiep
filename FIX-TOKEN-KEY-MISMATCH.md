# Fix Token Key Mismatch - Employee vs Admin

## Vấn đề

Employee pages không hiển thị dữ liệu dù admin pages có dữ liệu từ cùng database.

## Nguyên nhân

**Token key không khớp**:
- **Admin pages** dùng `inventoryApi` → lấy token từ `localStorage.getItem('auth_token')`
- **Employee pages** dùng fetch trực tiếp → lấy token từ `localStorage.getItem('token')`

Đây là 2 key khác nhau trong localStorage!

## Giải pháp

Sửa tất cả employee pages để lấy token từ cả 2 keys (fallback):

```typescript
// BEFORE (Wrong)
const token = localStorage.getItem('token')

// AFTER (Correct)
const token = localStorage.getItem('auth_token') || localStorage.getItem('token')
```

## Files đã sửa

### Employee Warehouse Pages
- ✅ `src/frontend/app/employee/warehouse/page.tsx` - Dashboard
- ✅ `src/frontend/app/employee/warehouse/import/page.tsx` - Import list
- ✅ `src/frontend/app/employee/warehouse/export/page.tsx` - Export list
- ✅ `src/frontend/app/employee/warehouse/products/page.tsx` - Products list
- ✅ `src/frontend/app/employee/warehouse/inventory/page.tsx` - Inventory
- ✅ `src/frontend/app/employee/warehouse/reports/page.tsx` - Reports (2 places)

### Employee Warehouse Detail Pages
- ✅ `src/frontend/app/employee/warehouse/products/[id]/page.tsx` - Product detail

### Employee Warehouse Edit Pages
- ✅ `src/frontend/app/employee/warehouse/products/[id]/edit/page.tsx` - Product edit (3 places)

### Employee Warehouse Create Pages
- ✅ `src/frontend/app/employee/warehouse/products/create/page.tsx` - Product create

## Kiểm tra token trong localStorage

Mở browser console và chạy:

```javascript
// Check which token keys exist
console.log('auth_token:', localStorage.getItem('auth_token'))
console.log('token:', localStorage.getItem('token'))
```

## Testing

### 1. Login as Admin
```
1. Login vào admin account
2. Mở console: localStorage.getItem('auth_token') → should have value
3. Navigate to /admin/warehouse/import
4. Verify data displays correctly
```

### 2. Login as Employee
```
1. Login vào employee account (WAREHOUSE position)
2. Mở console: localStorage.getItem('auth_token') → should have value
3. Navigate to /employee/warehouse/import
4. Verify data displays correctly (same data as admin)
```

### 3. Verify Token is Used
```
1. Open Network tab in DevTools
2. Navigate to any warehouse page
3. Check API request headers
4. Verify: Authorization: Bearer <token>
5. Token should match localStorage.getItem('auth_token')
```

## Root Cause Analysis

### Why 2 different keys?

Có thể do:
1. **Legacy code** - Code cũ dùng `token`, code mới dùng `auth_token`
2. **Different auth flows** - Admin và employee có auth flow khác nhau
3. **Inconsistent implementation** - Không có standard về token key

### Recommended Solution

**Option 1: Standardize on `auth_token`** (Current approach)
- ✅ Admin already uses this
- ✅ apiClient uses this
- ✅ Just need to update employee pages

**Option 2: Create a helper function**
```typescript
// lib/auth.ts
export const getAuthToken = () => {
  return localStorage.getItem('auth_token') || localStorage.getItem('token')
}

// Usage
const token = getAuthToken()
```

**Option 3: Migrate all to use `inventoryApi`**
```typescript
// Instead of fetch
const response = await inventoryApi.getPurchaseOrders()
// inventoryApi handles token automatically
```

## Next Steps

### Immediate (Done)
- ✅ Fix all employee pages to use fallback token

### Short-term (Recommended)
- [ ] Create `getAuthToken()` helper function
- [ ] Update all pages to use helper
- [ ] Remove `token` key, only use `auth_token`

### Long-term (Best practice)
- [ ] Migrate all pages to use `inventoryApi` instead of fetch
- [ ] Centralize API calls in `lib/api.ts`
- [ ] Remove direct fetch calls from components

## Summary

Vấn đề đã được sửa bằng cách:
1. ✅ Thêm fallback: `localStorage.getItem('auth_token') || localStorage.getItem('token')`
2. ✅ Áp dụng cho tất cả employee warehouse pages
3. ✅ Đảm bảo employee và admin dùng cùng token key

Kết quả:
- ✅ Employee pages giờ hiển thị dữ liệu đúng
- ✅ Dữ liệu đồng bộ giữa admin và employee
- ✅ Không còn 403 Forbidden errors
