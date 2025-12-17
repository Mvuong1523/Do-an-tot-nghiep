# SHIPPER Position Integration Guide

## Problem
When trying to register an employee with SHIPPER position, getting error:
```
Transaction silently rolled back because it has been marked as rollback-only
```

## Root Cause
The database column `position` might be using MySQL ENUM type instead of VARCHAR, which restricts the values to a predefined list. When SHIPPER was added to the Java enum but not to the database ENUM, the database rejected the insert.

## Solution

### 1. Update Database Schema
Run the SQL script to change the column type from ENUM to VARCHAR:

```sql
-- Run this in MySQL
source add-shipper-position.sql
```

Or manually execute:
```sql
ALTER TABLE employee_registration 
MODIFY COLUMN position VARCHAR(50) NOT NULL;

ALTER TABLE employees 
MODIFY COLUMN position VARCHAR(50) NOT NULL;
```

**Why VARCHAR instead of ENUM?**
- More flexible - no need to alter database when adding new positions
- Java enum already provides type safety
- `@Enumerated(EnumType.STRING)` works perfectly with VARCHAR

### 2. Backend Changes

#### Position Enum (Already Done)
```java
public enum Position {
    SALE,
    CSKH,
    PRODUCT_MANAGER,
    WAREHOUSE,
    ACCOUNTANT,
    SHIPPER  // ✅ Added
}
```

#### SecurityConfig.java
Added SHIPPER to authorized positions:
```java
// Orders access
.requestMatchers("/api/orders/**")
    .hasAnyAuthority("CUSTOMER", "ADMIN", "EMPLOYEE", "SALE", "SALES", "SHIPPER")

// Admin order management
.requestMatchers("/api/admin/orders/**")
    .hasAnyAuthority("ADMIN", "SALE", "SALES", "EMPLOYEE", "SHIPPER")
```

### 3. Frontend Changes

#### authStore.ts
Added SHIPPER to Position type:
```typescript
position?: 'WAREHOUSE' | 'PRODUCT_MANAGER' | 'ACCOUNTANT' | 'SALE' | 'SALES' | 'CSKH' | 'SHIPPER'
```

#### RoleBasedRedirect.tsx
Added routing for SHIPPER:
```typescript
case 'SHIPPER':
  router.push('/shipper')
  break
```

#### employee-register/page.tsx (Already Done)
SHIPPER option already exists in the registration form:
```typescript
{ value: 'SHIPPER', label: 'Nhân viên giao hàng' }
```

#### shipper/page.tsx (New)
Created dedicated dashboard for shippers with:
- View orders ready for delivery (CONFIRMED status)
- View orders being delivered (SHIPPING status)
- View delivered orders (DELIVERED status)
- Update order status (CONFIRMED → SHIPPING → DELIVERED)
- Statistics dashboard

## Testing Steps

### 1. Update Database
```bash
# Connect to MySQL
mysql -u root -p

# Select database
use your_database_name;

# Run the alter commands
source add-shipper-position.sql
```

### 2. Restart Backend
```bash
# Stop the Spring Boot application
# Then restart it
mvn spring-boot:run
```

### 3. Test Registration
1. Go to `http://localhost:3000/employee-register`
2. Fill in the form
3. Select "Nhân viên giao hàng" (SHIPPER) as position
4. Submit the form
5. Should see success message: "Đăng ký thành công"

### 4. Test Admin Approval
1. Login as ADMIN
2. Go to employee registration management
3. Approve the SHIPPER registration
4. Check that email is sent with credentials

### 5. Test SHIPPER Login
1. Login with the SHIPPER credentials from email
2. Should be redirected to `/shipper` dashboard
3. Should see orders ready for delivery
4. Test updating order status:
   - Click "Bắt đầu giao" on CONFIRMED order → becomes SHIPPING
   - Click "Đã giao" on SHIPPING order → becomes DELIVERED

## SHIPPER Permissions

SHIPPER position has access to:
- ✅ View all orders (`/api/orders/**`)
- ✅ View order management (`/api/admin/orders/**`)
- ✅ Update order status
- ✅ View order details
- ❌ Cannot access accounting
- ❌ Cannot access inventory
- ❌ Cannot manage products
- ❌ Cannot manage categories

## Files Modified

### Backend
1. `src/main/java/com/doan/WEB_TMDT/module/auth/entity/Position.java` - Added SHIPPER enum
2. `src/main/java/com/doan/WEB_TMDT/config/SecurityConfig.java` - Added SHIPPER permissions
3. `add-shipper-position.sql` - Database migration script

### Frontend
1. `src/frontend/store/authStore.ts` - Added SHIPPER to type
2. `src/frontend/components/RoleBasedRedirect.tsx` - Added SHIPPER routing
3. `src/frontend/app/employee-register/page.tsx` - Already had SHIPPER option
4. `src/frontend/app/shipper/page.tsx` - New shipper dashboard

## Troubleshooting

### Still getting transaction rollback error?
1. Verify database column type:
   ```sql
   DESCRIBE employee_registration;
   DESCRIBE employees;
   ```
   Both `position` columns should be VARCHAR(50), not ENUM

2. Check backend logs for detailed error message

3. Verify the Position enum value matches exactly (case-sensitive):
   - Frontend sends: `"SHIPPER"`
   - Backend expects: `Position.SHIPPER`
   - Database stores: `"SHIPPER"`

### SHIPPER can't access pages?
1. Check JWT token includes position claim
2. Verify SecurityConfig allows SHIPPER for the endpoint
3. Check browser console for 403 errors
4. Verify user.position === 'SHIPPER' in frontend

### Shipper dashboard not showing orders?
1. Check backend API returns orders
2. Verify token is being sent in Authorization header
3. Check order status filter logic
4. Verify SHIPPER has permission to access `/api/admin/orders`

## Next Steps

Consider adding these features for SHIPPER:
1. **Route optimization** - Show optimal delivery route
2. **GPS tracking** - Track shipper location in real-time
3. **Proof of delivery** - Upload photo/signature on delivery
4. **Delivery notes** - Add notes about delivery attempts
5. **Customer contact** - Quick call/message customer
6. **Delivery history** - View past deliveries and performance stats
7. **Push notifications** - Alert when new orders assigned

## Summary

The SHIPPER position is now fully integrated:
- ✅ Database schema updated to support SHIPPER
- ✅ Backend authorization configured
- ✅ Frontend routing and UI created
- ✅ Registration form includes SHIPPER option
- ✅ Dedicated shipper dashboard with order management

The transaction rollback error should be resolved after running the database migration script.
