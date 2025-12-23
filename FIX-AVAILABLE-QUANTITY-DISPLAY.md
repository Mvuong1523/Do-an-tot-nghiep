# Fix: Hiển thị số lượng có thể mua (Available Quantity)

## Vấn đề

Sau khi khách hàng đặt hàng, số tồn kho hiển thị trên trang sản phẩm (client-side) **không giảm**.

## Nguyên nhân

### Logic đúng (theo FIX-STOCK-LOGIC.md):
```
1. Khi khách đặt hàng:
   - stockQuantity KHÔNG thay đổi (chỉ thay đổi khi xuất kho)
   - reservedQuantity TĂNG (giữ hàng cho đơn hàng)

2. Khi xuất kho:
   - stockQuantity GIẢM (trừ tồn kho thực tế)
   - reservedQuantity GIẢM (bỏ giữ hàng)

3. Số lượng có thể mua:
   availableQuantity = stockQuantity - reservedQuantity
```

### Vấn đề cũ:
- Frontend chỉ hiển thị `stockQuantity`
- Không tính đến `reservedQuantity` (số lượng đang giữ)
- Khách hàng vẫn thấy số lượng cũ sau khi đặt hàng

### Ví dụ:
```
Ban đầu:
- stockQuantity = 100
- reservedQuantity = 0
- Hiển thị: "Còn 100 sản phẩm" ✅

Khách A đặt 10 sản phẩm:
- stockQuantity = 100 (không đổi)
- reservedQuantity = 10 (tăng)
- Hiển thị cũ: "Còn 100 sản phẩm" ❌ SAI!
- Hiển thị đúng: "Còn 90 sản phẩm" ✅ (100 - 10)
```

## Giải pháp

### 1. Backend: Thêm field `availableQuantity`

**File:** `ProductWithSpecsDTO.java`
```java
private Long stockQuantity;        // Tồn kho thực tế
private Long reservedQuantity;     // Số lượng đang giữ cho đơn hàng
private Long availableQuantity;    // Số lượng có thể mua = stockQuantity - reservedQuantity
```

**File:** `ProductServiceImpl.java` - Method `toProductWithSpecs()`
```java
// Tính availableQuantity = stockQuantity - reservedQuantity
Long stockQty = product.getStockQuantity() != null ? product.getStockQuantity() : 0L;
Long reservedQty = product.getReservedQuantity() != null ? product.getReservedQuantity() : 0L;
Long availableQty = Math.max(0, stockQty - reservedQty);

var dto = ProductWithSpecsDTO.builder()
    .stockQuantity(stockQty)
    .reservedQuantity(reservedQty)
    .availableQuantity(availableQty)  // ← Thêm field mới
    // ... other fields
    .build();
```

### 2. Frontend: Hiển thị `availableQuantity`

#### Trang chi tiết sản phẩm (`products/[id]/page.tsx`)

**Hiển thị trạng thái:**
```tsx
{(product.availableQuantity || 0) > 0 ? (
  <>
    <span className="px-3 py-1 bg-green-100 text-green-600 rounded-full">
      Còn hàng
    </span>
    <span className="text-gray-600">
      Còn {product.availableQuantity || 0} sản phẩm
    </span>
  </>
) : (
  <span className="px-3 py-1 bg-red-100 text-red-600 rounded-full">
    Hết hàng
  </span>
)}
```

**Giới hạn số lượng mua:**
```tsx
<input
  type="number"
  value={quantity}
  onChange={(e) => {
    const val = Math.max(1, parseInt(e.target.value) || 1)
    const maxQty = product.availableQuantity || 0
    setQuantity(Math.min(val, maxQty))  // ← Giới hạn theo availableQuantity
  }}
  max={product.availableQuantity || 0}
  disabled={(product.availableQuantity || 0) === 0}
/>
```

**Disable nút khi hết hàng:**
```tsx
<button
  onClick={handleAddToCart}
  disabled={(product.availableQuantity || 0) === 0}
  className="... disabled:bg-gray-400 disabled:cursor-not-allowed"
>
  {(product.availableQuantity || 0) === 0 ? 'Hết hàng' : 'Thêm vào giỏ'}
</button>
```

#### Trang danh sách sản phẩm (`page.tsx`)

```tsx
{(product.availableQuantity || 0) > 0 ? (
  <span className="text-xs text-green-600">
    Còn {product.availableQuantity} sản phẩm
  </span>
) : (
  <span className="text-xs text-red-600">Hết hàng</span>
)}
```

## Luồng hoạt động mới

### Scenario 1: Khách hàng đặt hàng

```
Trước khi đặt:
- stockQuantity = 100
- reservedQuantity = 0
- availableQuantity = 100
- Hiển thị: "Còn 100 sản phẩm"

Khách A đặt 10 sản phẩm:
- stockQuantity = 100 (không đổi)
- reservedQuantity = 10 (tăng)
- availableQuantity = 90 (100 - 10)
- Hiển thị: "Còn 90 sản phẩm" ✅

Khách B đặt 20 sản phẩm:
- stockQuantity = 100 (không đổi)
- reservedQuantity = 30 (10 + 20)
- availableQuantity = 70 (100 - 30)
- Hiển thị: "Còn 70 sản phẩm" ✅
```

### Scenario 2: Khách hủy đơn

```
Trước khi hủy:
- stockQuantity = 100
- reservedQuantity = 30
- availableQuantity = 70
- Hiển thị: "Còn 70 sản phẩm"

Khách A hủy đơn (10 sản phẩm):
- stockQuantity = 100 (không đổi)
- reservedQuantity = 20 (30 - 10)
- availableQuantity = 80 (100 - 20)
- Hiển thị: "Còn 80 sản phẩm" ✅
```

### Scenario 3: Xuất kho

```
Trước khi xuất:
- stockQuantity = 100
- reservedQuantity = 30
- availableQuantity = 70

Xuất kho 10 sản phẩm cho đơn A:
- stockQuantity = 90 (100 - 10)
- reservedQuantity = 20 (30 - 10)
- availableQuantity = 70 (90 - 20)
- Hiển thị: "Còn 70 sản phẩm" ✅
```

## Lợi ích

1. ✅ **Chính xác:** Hiển thị đúng số lượng có thể mua
2. ✅ **Real-time:** Cập nhật ngay khi có đơn hàng mới
3. ✅ **Tránh overselling:** Không bán quá số lượng có sẵn
4. ✅ **UX tốt hơn:** Khách hàng biết chính xác còn bao nhiêu
5. ✅ **Giới hạn số lượng:** Không cho mua quá availableQuantity

## Files đã thay đổi

### Backend
- ✅ `ProductWithSpecsDTO.java` - Thêm 3 fields: stockQuantity, reservedQuantity, availableQuantity
- ✅ `ProductServiceImpl.java` - Tính availableQuantity trong toProductWithSpecs()

### Frontend
- ✅ `app/products/[id]/page.tsx` - Hiển thị availableQuantity, giới hạn số lượng, disable khi hết hàng
- ✅ `app/page.tsx` - Hiển thị availableQuantity trong danh sách sản phẩm

## Testing

### Test Case 1: Hiển thị số lượng đúng
1. Vào trang sản phẩm có stockQuantity = 100, reservedQuantity = 0
2. ✅ Hiển thị: "Còn 100 sản phẩm"

### Test Case 2: Sau khi đặt hàng
1. Đặt hàng 10 sản phẩm
2. Reload trang sản phẩm
3. ✅ Hiển thị: "Còn 90 sản phẩm" (không phải 100)

### Test Case 3: Giới hạn số lượng mua
1. Vào sản phẩm còn 5 sản phẩm
2. Thử nhập số lượng 10
3. ✅ Tự động giới hạn về 5
4. ✅ Nút + bị disable khi đạt max

### Test Case 4: Hết hàng
1. Vào sản phẩm có availableQuantity = 0
2. ✅ Hiển thị: "Hết hàng"
3. ✅ Nút "Thêm vào giỏ" và "Mua ngay" bị disable
4. ✅ Input số lượng bị disable

### Test Case 5: Sau khi hủy đơn
1. Hủy đơn hàng 10 sản phẩm
2. Reload trang sản phẩm
3. ✅ Số lượng tăng lên 10

## Lưu ý

### Không thay đổi logic backend
- Logic reserve/release stock vẫn giữ nguyên
- Chỉ thêm field `availableQuantity` để frontend hiển thị
- Backend vẫn validate đúng khi đặt hàng

### Tương thích ngược
- Nếu `availableQuantity` null → fallback về 0
- Nếu `reservedQuantity` null → coi như 0
- Không ảnh hưởng đến code cũ

### Performance
- Tính toán `availableQuantity` rất nhanh (chỉ là phép trừ)
- Không cần query thêm database
- Không ảnh hưởng đến performance

## Kết luận

Sau khi fix, khách hàng sẽ thấy số lượng sản phẩm **giảm ngay lập tức** sau khi đặt hàng, phản ánh đúng số lượng thực tế có thể mua. Điều này cải thiện đáng kể trải nghiệm người dùng và tránh tình trạng overselling.

**Công thức quan trọng:**
```
availableQuantity = stockQuantity - reservedQuantity
```

Đây là số lượng thực sự mà khách hàng có thể mua tại thời điểm hiện tại!
