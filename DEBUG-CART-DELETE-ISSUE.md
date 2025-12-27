# DEBUG: SẢN PHẨM KHÔNG BỊ XÓA KHỎI GIỎ HÀNG

## 🐛 VẤN ĐỀ
- Thông báo "Đã xóa sản phẩm" hiện ra
- Nhưng sản phẩm vẫn còn trong giỏ hàng
- UI không cập nhật

## 🔍 NGUYÊN NHÂN

### Vấn đề 1: JPA Relationship Cache
Backend đang dùng `cartItemRepository.delete(item)` trực tiếp, nhưng Cart entity có:
```java
@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
private List<CartItem> items = new ArrayList<>();
```

Khi xóa trực tiếp qua repository, JPA có thể không cập nhật collection `items` trong Cart entity → Khi query lại vẫn thấy item cũ.

### Vấn đề 2: Frontend không await loadCart()
```typescript
// ❌ SAI - Không đợi loadCart() hoàn thành
loadCart()
toast.success('Đã xóa sản phẩm')

// ✅ ĐÚNG - Đợi loadCart() xong mới hiện toast
await loadCart()
toast.success('Đã xóa sản phẩm')
```

## ✅ GIẢI PHÁP

### Bước 1: Fix Backend - Dùng Cart.removeItem()
📍 File: `CartServiceImpl.java`

**Thay đổi:**
```java
// ❌ CÁCH CŨ - Xóa trực tiếp
cartItemRepository.delete(item);

// ✅ CÁCH MỚI - Xóa qua Cart entity
cart.removeItem(item);
cartRepository.save(cart);  // Trigger orphanRemoval
cartRepository.flush();     // Đảm bảo DB được update ngay
```

**Lý do:**
- `cart.removeItem(item)` sẽ xóa item khỏi collection `items`
- `orphanRemoval = true` sẽ tự động xóa item khỏi database
- `flush()` đảm bảo thay đổi được commit ngay lập tức

### Bước 2: Fix Frontend - Thêm await và logging
📍 File: `src/frontend/app/cart/page.tsx`

**Thay đổi:**
```typescript
const handleRemoveItem = async (itemId: number) => {
  console.log('🗑️ Attempting to remove item:', itemId)
  if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return
  
  try {
    console.log('📤 Calling removeCartItem API...')
    const response = await cartApi.removeCartItem(itemId)
    console.log('📥 Remove response:', response)
    
    if (response.success) {
      console.log('✅ Remove successful, reloading cart...')
      await loadCart()  // ← THÊM AWAIT
      console.log('✅ Cart reloaded')
      toast.success('Đã xóa sản phẩm')
    } else {
      console.error('❌ Remove failed:', response.message)
      toast.error(response.message || 'Không thể xóa sản phẩm')
    }
  } catch (error: any) {
    console.error('❌ Error removing item:', error)
    toast.error(error.message || 'Lỗi khi xóa')
  }
}
```

## 🧪 CÁCH TEST

### Bước 1: Restart Backend
```bash
# Stop backend nếu đang chạy
# Restart lại để load code mới
```

### Bước 2: Mở Console và Test
1. Mở trang giỏ hàng: `http://localhost:3000/cart`
2. Mở DevTools Console (F12)
3. Click nút xóa một sản phẩm
4. Xem log trong console

### Bước 3: Kiểm tra Log

#### Frontend Console (Browser)
```
🗑️ Attempting to remove item: 123
📤 Calling removeCartItem API...
📥 Remove response: {success: true, message: "Đã xóa sản phẩm khỏi giỏ hàng", data: {...}}
✅ Remove successful, reloading cart...
Cart API response: {success: true, data: {...}}
Cart data: {cartId: 1, items: [...], totalItems: 2}  ← Kiểm tra items count giảm
✅ Cart reloaded
```

#### Backend Console (IntelliJ/Terminal)
```
🗑️ Removing cart item - customerId: 1, itemId: 123
📦 Found cart: id=1, items count=3
📦 Found item to delete: id=123, product=iPhone 15
🗑️ Removing item from cart...
✅ Item removed successfully
📦 Updated cart: items count=2  ← Kiểm tra count giảm
✅ Returning response with 2 items
```

### Bước 4: Kiểm tra Database
```sql
-- Kiểm tra item đã bị xóa chưa
SELECT * FROM cart_items WHERE id = 123;  -- Không có kết quả

-- Kiểm tra số lượng items còn lại
SELECT * FROM cart_items WHERE cart_id = 1;  -- Chỉ còn 2 items
```

## 🎯 KẾT QUẢ MONG ĐỢI

✅ Click xóa → Sản phẩm biến mất ngay lập tức  
✅ Reload trang → Sản phẩm vẫn không còn  
✅ Kiểm tra DB → Item đã bị xóa  
✅ Console không có lỗi  
✅ Toast "Đã xóa sản phẩm" hiện ra  

## ❌ NẾU VẪN KHÔNG HOẠT ĐỘNG

### Kiểm tra 1: Response có đúng không?
```typescript
// Trong console, xem response.data.items
console.log('Items after delete:', response.data.items)
// Nếu vẫn thấy item cũ → Backend chưa xóa đúng
```

### Kiểm tra 2: loadCart() có được gọi không?
```typescript
// Thêm log trong loadCart()
const loadCart = async () => {
  console.log('🔄 Loading cart...')
  const response = await cartApi.getCart()
  console.log('📦 Cart loaded:', response.data)
  // ...
}
```

### Kiểm tra 3: State có được update không?
```typescript
// Sau setCart(response.data)
console.log('State updated:', cart)
```

### Kiểm tra 4: Backend có lỗi không?
- Xem log backend có exception không
- Kiểm tra transaction có bị rollback không
- Thử query trực tiếp database

## 🔧 GIẢI PHÁP DỰ PHÒNG

Nếu vẫn không work, thử cách này:

```java
// CartServiceImpl.java
@Override
@Transactional
public ApiResponse removeCartItem(Long customerId, Long itemId) {
    Cart cart = getOrCreateCart(customerId);
    CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    
    if (!item.getCart().getId().equals(cart.getId())) {
        return ApiResponse.error("Không có quyền xóa");
    }
    
    // Xóa trực tiếp và clear cache
    cartItemRepository.deleteById(itemId);
    cartItemRepository.flush();
    
    // Clear entity manager cache
    entityManager.clear();
    
    // Query lại từ DB
    Cart updatedCart = cartRepository.findById(cart.getId()).orElseThrow();
    CartResponse response = toCartResponse(updatedCart);
    return ApiResponse.success("Đã xóa sản phẩm", response);
}
```

## 📝 TÓM TẮT THAY ĐỔI

### Backend
- ✅ Dùng `cart.removeItem(item)` thay vì `cartItemRepository.delete(item)`
- ✅ Thêm `cartRepository.flush()` để đảm bảo DB update
- ✅ Thêm logging chi tiết

### Frontend  
- ✅ Thêm `await` trước `loadCart()`
- ✅ Thêm logging để debug
- ✅ Xử lý error case tốt hơn
