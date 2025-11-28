# Danh sách các trang cần fix hydration

## Đã fix ✅
- [x] warehouse/page.tsx
- [x] product-manager/page.tsx
- [x] product-manager/products/page.tsx
- [x] product-manager/products/publish/page.tsx
- [x] product-manager/categories/page.tsx

## Cần fix (có role check)
### Warehouse pages
- [ ] warehouse/suppliers/page.tsx
- [ ] warehouse/reports/page.tsx
- [ ] warehouse/inventory/page.tsx
- [ ] warehouse/import/[id]/page.tsx
- [ ] warehouse/import/list/page.tsx
- [ ] warehouse/import/complete/page.tsx
- [ ] warehouse/export/[id]/page.tsx
- [ ] warehouse/export/list/page.tsx

### Product Manager pages
- [ ] product-manager/inventory/page.tsx

### Admin pages
- [ ] admin/page.tsx
- [ ] admin/suppliers/page.tsx
- [ ] admin/products/page.tsx
- [ ] admin/products/publish/page.tsx
- [ ] admin/products/create/page.tsx
- [ ] admin/inventory/page.tsx
- [ ] admin/inventory/import/page.tsx
- [ ] admin/inventory/transactions/page.tsx
- [ ] admin/inventory/transactions/create/page.tsx
- [ ] admin/categories/page.tsx
- [ ] admin/employee-approval/page.tsx
- [ ] admin/accounting/page.tsx (nếu có)

### Customer pages (ít quan trọng hơn)
- [ ] profile/page.tsx
- [ ] orders/page.tsx
- [ ] orders/[id]/page.tsx
- [ ] checkout/page.tsx
- [ ] cart/page.tsx
- [ ] payment/[orderCode]/page_new.tsx

## Pattern để fix:

```typescript
// 1. Thêm state
const [isHydrated, setIsHydrated] = useState(false)

// 2. Thêm useEffect để set hydrated
useEffect(() => {
  setIsHydrated(true)
}, [])

// 3. Sửa useEffect auth check
useEffect(() => {
  if (!isHydrated) return
  
  // ... existing auth check code
}, [isHydrated, isAuthenticated, user, router])

// 4. Nếu check role, sửa thành check position:
// OLD: user?.role !== 'WAREHOUSE'
// NEW: user?.role === 'ADMIN' || (user?.role === 'EMPLOYEE' && user?.position === 'WAREHOUSE')
```
