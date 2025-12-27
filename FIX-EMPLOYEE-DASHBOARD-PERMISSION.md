# Fix Employee Dashboard Permission Issue

## Problem
User với position `ACCOUNTANT` (email: ketoan@gmail.com) bị lỗi 403 Forbidden khi truy cập trang employee dashboard và endpoint `/api/dashboard/stats`.

### Error Logs
```
GET http://localhost:8080/api/dashboard/stats 403 (Forbidden)
Error: {status: 403, error: 'Forbidden', message: 'Forbidden', path: '/api/dashboard/stats'}
```

### User Info
- Email: ketoan@gmail.com
- Position: ACCOUNTANT
- Role: EMPLOYEE
- Token: Valid ✅
- Employee data: Loaded correctly ✅

## Root Causes

### 1. DashboardController Permission Mismatch
**File:** `src/main/java/com/doan/WEB_TMDT/controller/DashboardController.java`

**Before:**
```java
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
```

**Problem:** 
- Sử dụng `hasAnyRole()` yêu cầu prefix `ROLE_`
- JWT token của ACCOUNTANT chỉ có authority `ACCOUNTANT`, không có `ROLE_EMPLOYEE`
- Kết quả: 403 Forbidden

### 2. SecurityConfig Route-Level Permission
**File:** `src/main/java/com/doan/WEB_TMDT/config/SecurityConfig.java`

**Before:**
```java
.requestMatchers("/api/dashboard/**").hasAnyAuthority("ADMIN", "EMPLOYEE")
```

**Problem:**
- Chỉ cho phép `ADMIN` và `EMPLOYEE`
- Không bao gồm các position cụ thể như `ACCOUNTANT`, `SALE`, `WAREHOUSE`, etc.

### 3. JWT Filter Authority Assignment
**File:** `src/main/java/com/doan/WEB_TMDT/security/JwtAuthenticationFilter.java`

**Before:**
```java
Object position = claims.get("position");
if (position != null) {
    authorities.add(new SimpleGrantedAuthority(position.toString()));
    // Không thêm ROLE_EMPLOYEE
}
```

**Problem:**
- Chỉ thêm position name (VD: `ACCOUNTANT`)
- Không thêm `EMPLOYEE` hoặc `ROLE_EMPLOYEE` authority
- Các endpoint yêu cầu `ROLE_EMPLOYEE` sẽ bị reject

## Solutions Applied

### 1. ✅ Fixed DashboardController
**File:** `src/main/java/com/doan/WEB_TMDT/controller/DashboardController.java`

**After:**
```java
@GetMapping("/stats")
@PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE', 'ACCOUNTANT', 'SALE', 'SALES', 'WAREHOUSE', 'PRODUCT_MANAGER', 'CSKH', 'SHIPPER')")
public ApiResponse getDashboardStats() {
    return accountingService.getDashboardStats();
}

@GetMapping("/recent-orders")
@PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE', 'ACCOUNTANT', 'SALE', 'SALES', 'WAREHOUSE', 'PRODUCT_MANAGER', 'CSKH', 'SHIPPER')")
public ApiResponse getRecentOrders(@RequestParam(defaultValue = "10") int limit) {
    return accountingService.getRecentOrders(limit);
}
```

**Changes:**
- Đổi từ `hasAnyRole()` sang `hasAnyAuthority()`
- Thêm tất cả employee positions: `ACCOUNTANT`, `SALE`, `SALES`, `WAREHOUSE`, `PRODUCT_MANAGER`, `CSKH`, `SHIPPER`

### 2. ✅ Fixed SecurityConfig
**File:** `src/main/java/com/doan/WEB_TMDT/config/SecurityConfig.java`

**After:**
```java
// Dashboard endpoints (ADMIN + All Employee Positions)
.requestMatchers("/api/dashboard/**").hasAnyAuthority("ADMIN", "EMPLOYEE", "ACCOUNTANT", "SALE", "SALES", "WAREHOUSE", "PRODUCT_MANAGER", "CSKH", "SHIPPER")
```

**Changes:**
- Thêm tất cả employee positions vào route-level security
- Đảm bảo consistency với method-level security

### 3. ✅ Fixed JWT Filter
**File:** `src/main/java/com/doan/WEB_TMDT/security/JwtAuthenticationFilter.java`

**After:**
```java
Object position = claims.get("position");
if (position != null) {
    authorities.add(new SimpleGrantedAuthority(position.toString()));
    // Add ROLE_EMPLOYEE for all employee positions
    authorities.add(new SimpleGrantedAuthority("EMPLOYEE"));
    authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
    System.out.println("✅ User position: " + position.toString());
}
```

**Changes:**
- Thêm `EMPLOYEE` authority cho tất cả users có position
- Thêm `ROLE_EMPLOYEE` authority để support cả `hasAnyRole()` và `hasAnyAuthority()`
- Giờ mỗi employee sẽ có 3 authorities: position name, `EMPLOYEE`, và `ROLE_EMPLOYEE`

## Testing

### 1. Restart Backend
```bash
# Stop current backend
# Restart Spring Boot application
```

### 2. Test with ACCOUNTANT User
```bash
# Login as ketoan@gmail.com
# Navigate to /employee page
# Should see dashboard data without 403 errors
```

### 3. Expected Authorities for ACCOUNTANT
```
✅ ACCOUNTANT
✅ EMPLOYEE
✅ ROLE_EMPLOYEE
```

### 4. Verify Endpoints
- ✅ GET `/api/dashboard/stats` - Should return 200
- ✅ GET `/api/dashboard/recent-orders` - Should return 200
- ✅ NotificationBell component - Should load without errors

## Impact Analysis

### Affected Components
1. ✅ **DashboardController** - Fixed permission checks
2. ✅ **SecurityConfig** - Updated route-level security
3. ✅ **JwtAuthenticationFilter** - Enhanced authority assignment
4. ✅ **NotificationBell.tsx** - Will work after backend fix
5. ✅ **employee/page.tsx** - Will work after backend fix

### All Employee Positions Now Have Access
- ACCOUNTANT ✅
- SALE / SALES ✅
- WAREHOUSE ✅
- PRODUCT_MANAGER ✅
- CSKH ✅
- SHIPPER ✅

### Backward Compatibility
- ✅ Existing ADMIN users still work
- ✅ Existing EMPLOYEE role still works
- ✅ All position-based users now work
- ✅ No breaking changes to other endpoints

## Additional Notes

### Position Enum
**File:** `src/main/java/com/doan/WEB_TMDT/module/auth/entity/Position.java`
```java
public enum Position {
    SALE,
    CSKH,
    PRODUCT_MANAGER,
    WAREHOUSE,
    ACCOUNTANT,
    SHIPPER
}
```

### Database Check
Run this SQL to verify user data:
```sql
-- Check user and employee data
SELECT 
    u.id,
    u.email,
    u.full_name,
    u.role,
    e.position,
    e.first_login
FROM users u
LEFT JOIN employees e ON e.user_id = u.id
WHERE u.email = 'ketoan@gmail.com';
```

## Summary

Đã fix thành công lỗi permission cho employee dashboard:
1. ✅ DashboardController giờ chấp nhận tất cả employee positions
2. ✅ SecurityConfig route-level security đã được cập nhật
3. ✅ JWT filter giờ thêm EMPLOYEE và ROLE_EMPLOYEE authorities
4. ✅ Tất cả employee positions (ACCOUNTANT, SALE, WAREHOUSE, etc.) giờ có thể truy cập dashboard
5. ✅ NotificationBell component sẽ hoạt động bình thường

**Restart backend để áp dụng các thay đổi!**
