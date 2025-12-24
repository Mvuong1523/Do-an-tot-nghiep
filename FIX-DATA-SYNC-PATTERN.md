# Fix Data Synchronization Pattern

## Problem
When switching between different employee accounts, data from previous user is still displayed (cached).

## Solution
Add employee dependency to useEffect and cleanup on unmount.

## Pattern to Apply

### 1. Add useAuthStore Import
```typescript
import { useAuthStore } from '@/store/authStore'
```

### 2. Get employee from store
```typescript
const { employee } = useAuthStore()
// or for pages that use user
const { user } = useAuthStore()
```

### 3. Update useEffect with employee dependency
**BEFORE:**
```typescript
useEffect(() => {
  fetchData()
}, [])
```

**AFTER:**
```typescript
useEffect(() => {
  if (employee) {  // or if (user) for non-employee pages
    fetchData()
  }
  
  // Cleanup on unmount
  return () => {
    setData([])  // Clear data state
    setStats(null)  // Clear stats if any
  }
}, [employee])  // Re-run when employee changes
```

## Files to Update

### Warehouse Module - Employee
- [x] `/employee/warehouse/page.tsx` - Dashboard - DONE
- [ ] `/employee/warehouse/import/page.tsx` - Import list
- [ ] `/employee/warehouse/export/page.tsx` - Export list
- [ ] `/employee/warehouse/products/page.tsx` - Products list
- [ ] `/employee/warehouse/inventory/page.tsx` - Inventory list
- [ ] `/employee/warehouse/suppliers/page.tsx` - Suppliers list
- [ ] `/employee/warehouse/orders/page.tsx` - Orders list

### Detail Pages
- [ ] `/employee/warehouse/import/[id]/page.tsx`
- [ ] `/employee/warehouse/export/[id]/page.tsx`
- [ ] `/employee/warehouse/products/[id]/page.tsx`
- [ ] `/employee/warehouse/orders/[id]/page.tsx`

### Create/Edit Pages
- [ ] `/employee/warehouse/import/create/page.tsx`
- [ ] `/employee/warehouse/export/create/page.tsx`
- [ ] `/employee/warehouse/products/create/page.tsx`
- [ ] `/employee/warehouse/products/[id]/edit/page.tsx`

## Complete Example

```typescript
'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'

export default function SomeListPage() {
  const router = useRouter()
  const { employee } = useAuthStore()  // ✅ Add this
  const [data, setData] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // ✅ Check if employee exists
    if (employee) {
      fetchData()
    }
    
    // ✅ Cleanup on unmount
    return () => {
      setData([])
    }
  }, [employee])  // ✅ Add employee dependency

  const fetchData = async () => {
    try {
      setLoading(true)
      const response = await api.getData()
      if (response.success) {
        setData(response.data || [])
      }
    } catch (error) {
      console.error('Error:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      {/* Page content */}
    </div>
  )
}
```

## Additional Fix: Force Reload on Logout

Update `src/frontend/store/authStore.ts`:

```typescript
logout: () => {
  if (typeof window !== 'undefined') {
    localStorage.removeItem('auth_token')
    localStorage.removeItem('token')
    localStorage.removeItem('auth-storage')
    localStorage.removeItem('cart-storage')
    
    // ✅ Force reload to clear all cached data
    window.location.href = '/login'
  }
  set({ user: null, employee: null, isAuthenticated: false, token: null })
}
```

## Testing Checklist

For each page:
1. Login as User A (e.g., WAREHOUSE position)
2. Navigate to page and verify data loads
3. Logout
4. Login as User B (e.g., SALE position)
5. Navigate to same page
6. ✅ Verify data is refreshed (not showing User A's data)
7. ✅ Verify correct permissions are applied

## Quick Fix Script

Run this pattern on all warehouse pages:

1. Add import: `import { useAuthStore } from '@/store/authStore'`
2. Add hook: `const { employee } = useAuthStore()`
3. Update useEffect:
   ```typescript
   useEffect(() => {
     if (employee) {
       fetchData()
     }
     return () => {
       setData([])
     }
   }, [employee])
   ```

## Summary

This fix ensures:
- ✅ Data is refreshed when switching users
- ✅ No stale data from previous user
- ✅ Proper cleanup on component unmount
- ✅ Re-fetch when employee changes
