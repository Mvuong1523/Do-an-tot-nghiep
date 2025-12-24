# Permission View-Only Pattern

## Concept
All employees can **VIEW** all pages, but only employees with proper permissions can **PERFORM ACTIONS** (create/edit/delete).

## Implementation Pattern

### 1. Remove Page-Level Blocking
**BEFORE (Wrong):**
```typescript
useEffect(() => {
  if (!canCreate) {
    toast.error('Bạn không có quyền...')
    router.push('/employee/warehouse')  // ❌ Don't redirect!
    return
  }
  loadData()
}, [canCreate, router])
```

**AFTER (Correct):**
```typescript
useEffect(() => {
  loadData()  // ✅ Always load data
}, [])
```

### 2. Add Warning Banner
```typescript
return (
  <div className="p-6">
    {!canCreate && (
      <div className="mb-6 bg-yellow-50 border border-yellow-200 rounded-lg p-4 flex items-start space-x-3">
        <FiAlertCircle className="text-yellow-600 w-5 h-5 mt-0.5" />
        <div className="flex-1">
          <h3 className="text-sm font-semibold text-yellow-900">Chế độ xem</h3>
          <p className="text-sm text-yellow-700 mt-1">
            Bạn chỉ có quyền xem. Chỉ [position] mới có thể [action].
          </p>
        </div>
      </div>
    )}
    
    {/* Rest of page content */}
  </div>
)
```

### 3. Disable Action Buttons
```typescript
<button
  type="submit"
  disabled={loading || !canCreate}  // ✅ Disable if no permission
  className="... disabled:bg-gray-400 disabled:cursor-not-allowed"
  title={!canCreate ? 'Bạn không có quyền...' : ''}
>
  Tạo/Lưu
</button>
```

### 4. Check Permission in Submit Handler
```typescript
const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault()

  // Check permission before submit
  if (!canCreate) {
    toast.error('Bạn không có quyền...')
    return  // ✅ Stop here
  }

  // Continue with submit logic...
}
```

### 5. Hide Action Buttons in List Pages
```typescript
<td className="px-6 py-4 whitespace-nowrap">
  {canEdit && (
    <button onClick={() => handleEdit(item.id)}>
      <FiEdit />
    </button>
  )}
  {canDelete && (
    <button onClick={() => handleDelete(item.id)}>
      <FiTrash2 />
    </button>
  )}
  {/* Always show view button */}
  <button onClick={() => handleView(item.id)}>
    <FiEye />
  </button>
</td>
```

## Pages to Update

### Warehouse Module
- [x] `/employee/warehouse/export/create` - DONE
- [ ] `/employee/warehouse/import/create`
- [ ] `/employee/warehouse/products/create`
- [ ] `/employee/warehouse/products/[id]/edit`
- [ ] `/employee/warehouse/suppliers` (create/edit/delete buttons)

### Products Module
- [ ] `/employee/products/create`
- [ ] `/employee/products/[id]/edit`
- [ ] `/employee/products/publish`

### Orders Module
- [ ] `/employee/orders` (action buttons)
- [ ] `/employee/orders/[id]` (action buttons)

### Accounting Module
- [ ] `/employee/accounting/payables` (create/edit/delete buttons)
- [ ] `/employee/accounting/reconciliation` (edit button)

## Testing Checklist

For each page, test with different positions:

### WAREHOUSE Position
- ✅ Can view all warehouse pages
- ✅ Can create/edit warehouse items
- ❌ Cannot create/edit products
- ❌ Cannot create/edit orders

### PRODUCT_MANAGER Position
- ✅ Can view all pages
- ✅ Can create/edit products
- ❌ Cannot create/edit warehouse items
- ❌ Cannot create/edit orders

### SALE Position
- ✅ Can view all pages
- ✅ Can create/edit orders
- ❌ Cannot create/edit products
- ❌ Cannot create/edit warehouse items

### ACCOUNTANT Position
- ✅ Can view all pages
- ✅ Can edit accounting data
- ❌ Cannot create/edit products
- ❌ Cannot create/edit warehouse items
- ❌ Cannot create/edit orders

## Benefits

1. **Better UX**: Users can see what's available even if they can't edit
2. **Transparency**: Clear indication of permissions
3. **Collaboration**: Different roles can view same data
4. **Security**: Actions still protected by permission checks

## Example: Complete Implementation

```typescript
'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { FiSave, FiAlertCircle } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import { hasPermission, type Position } from '@/lib/permissions'

export default function CreateSomethingPage() {
  const router = useRouter()
  const { employee } = useAuthStore()
  const [loading, setLoading] = useState(false)
  
  // Permission check - for ACTIONS only
  const canCreate = hasPermission(employee?.position as Position, 'something.create')

  useEffect(() => {
    // Always load data - no permission check here
    loadData()
  }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    // Check permission before submit
    if (!canCreate) {
      toast.error('Bạn không có quyền tạo...')
      return
    }

    // Submit logic...
  }

  return (
    <div className="p-6">
      {/* Warning banner for view-only users */}
      {!canCreate && (
        <div className="mb-6 bg-yellow-50 border border-yellow-200 rounded-lg p-4 flex items-start space-x-3">
          <FiAlertCircle className="text-yellow-600 w-5 h-5 mt-0.5" />
          <div className="flex-1">
            <h3 className="text-sm font-semibold text-yellow-900">Chế độ xem</h3>
            <p className="text-sm text-yellow-700 mt-1">
              Bạn chỉ có quyền xem. Chỉ [position] mới có thể tạo...
            </p>
          </div>
        </div>
      )}

      <h1>Tạo...</h1>

      <form onSubmit={handleSubmit}>
        {/* Form fields */}
        
        <button
          type="submit"
          disabled={loading || !canCreate}
          className="... disabled:bg-gray-400 disabled:cursor-not-allowed"
          title={!canCreate ? 'Bạn không có quyền...' : ''}
        >
          <FiSave />
          <span>{loading ? 'Đang lưu...' : 'Tạo mới'}</span>
        </button>
      </form>
    </div>
  )
}
```

## Summary

This pattern ensures:
- ✅ All employees can VIEW all data
- ✅ Only authorized employees can PERFORM ACTIONS
- ✅ Clear visual feedback about permissions
- ✅ Better collaboration between teams
- ✅ Maintained security through permission checks
