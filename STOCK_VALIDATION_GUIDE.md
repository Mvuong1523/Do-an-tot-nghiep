# 📦 Hướng dẫn Kiểm tra Tồn kho khi Thanh toán

## 🎯 Chiến lược: Kiểm tra khi thanh toán

### Lý do:
- ✅ Thêm vào giỏ thoải mái (không chặn khách hàng)
- ✅ Chỉ kiểm tra khi thực sự mua (thanh toán)
- ✅ Tránh overselling (bán quá số lượng tồn kho)
- ✅ UX tốt (giống Shopee, Lazada)

---

## 🔧 Implementation

### 1. Backend - OrderService

Thêm logic kiểm tra tồn kho khi tạo đơn hàng:

```java
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    
    @Override
    public ApiResponse createOrder(CreateOrderRequest request) {
        // 1. Validate stock TRƯỚC KHI tạo order
        List<String> outOfStockItems = new ArrayList<>();
        
        for (OrderItemRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
            
            // Lấy số lượng có thể bán từ InventoryStock
            Long availableStock = inventoryStockRepository
                .findByWarehouseProduct_Id(product.getWarehouseProduct().getId())
                .map(InventoryStock::getSellable)
                .orElse(0L);
            
            // Kiểm tra đủ hàng không
            if (availableStock < item.getQuantity()) {
                outOfStockItems.add(String.format(
                    "%s (Còn %d, yêu cầu %d)", 
                    product.getName(), 
                    availableStock, 
                    item.getQuantity()
                ));
            }
        }
        
        // 2. Nếu có sản phẩm hết hàng → Trả về lỗi
        if (!outOfStockItems.isEmpty()) {
            return ApiResponse.error(
                "Một số sản phẩm không đủ hàng: " + String.join(", ", outOfStockItems)
            );
        }
        
        // 3. Tạo order (với synchronized hoặc pessimistic lock)
        synchronized (this) {
            // Kiểm tra lại lần nữa (double-check)
            for (OrderItemRequest item : request.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                    .orElseThrow();
                
                Long availableStock = inventoryStockRepository
                    .findByWarehouseProduct_Id(product.getWarehouseProduct().getId())
                    .map(InventoryStock::getSellable)
                    .orElse(0L);
                
                if (availableStock < item.getQuantity()) {
                    return ApiResponse.error("Sản phẩm " + product.getName() + " vừa hết hàng");
                }
            }
            
            // 4. Tạo order và trừ tồn kho
            Order order = createOrderEntity(request);
            orderRepository.save(order);
            
            // 5. Trừ tồn kho
            for (OrderItem item : order.getItems()) {
                inventoryService.decreaseStock(
                    item.getProduct().getWarehouseProduct().getId(), 
                    item.getQuantity()
                );
            }
            
            return ApiResponse.success("Đặt hàng thành công", order);
        }
    }
}
```

### 2. InventoryService - Trừ tồn kho

```java
@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {
    
    @Override
    public void decreaseStock(Long warehouseProductId, Long quantity) {
        InventoryStock stock = inventoryStockRepository
            .findByWarehouseProduct_Id(warehouseProductId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho"));
        
        // Kiểm tra đủ hàng
        if (stock.getSellable() < quantity) {
            throw new RuntimeException("Không đủ hàng để bán");
        }
        
        // Trừ sellable
        stock.setSellable(stock.getSellable() - quantity);
        
        // Cập nhật
        inventoryStockRepository.save(stock);
    }
}
```

### 3. Frontend - Xử lý lỗi hết hàng

```typescript
// checkout/page.tsx
const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault()
  
  setSubmitting(true)
  try {
    const response = await orderApi.create(orderData)
    
    if (response.success) {
      toast.success('Đặt hàng thành công!')
      router.push(`/orders/${response.data.id}`)
    } else {
      // Hiển thị lỗi hết hàng
      toast.error(response.message || 'Đặt hàng thất bại')
      
      // Nếu hết hàng, reload giỏ hàng để cập nhật số lượng
      if (response.message?.includes('không đủ hàng')) {
        setTimeout(() => {
          window.location.reload()
        }, 2000)
      }
    }
  } catch (error: any) {
    toast.error(error.message || 'Lỗi khi đặt hàng')
  } finally {
    setSubmitting(false)
  }
}
```

---

## 🔒 Xử lý Race Condition

### Vấn đề:
2 khách hàng thanh toán cùng lúc → Cả 2 đều pass validation → Overselling

### Giải pháp 1: Synchronized Block (Đơn giản)
```java
synchronized (this) {
    // Kiểm tra và tạo order
}
```
**Nhược điểm:** Chỉ work với 1 server

### Giải pháp 2: Pessimistic Lock (Tốt hơn)
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM InventoryStock s WHERE s.warehouseProduct.id = :id")
Optional<InventoryStock> findByWarehouseProductIdWithLock(@Param("id") Long id);
```

### Giải pháp 3: Optimistic Lock (Chuẩn nhất)
```java
@Entity
public class InventoryStock {
    @Version
    private Long version;
    
    // Khi update, Hibernate tự động check version
    // Nếu version khác → Throw OptimisticLockException
}
```

---

## 📊 Flow hoàn chỉnh

```
1. Khách A: Thêm 2 SP vào giỏ
   → Không kiểm tra tồn kho
   → Thành công

2. Khách B: Thêm 1 SP vào giỏ  
   → Không kiểm tra tồn kho
   → Thành công

3. Khách A: Thanh toán
   → Kiểm tra tồn kho: OK (còn 2)
   → Lock stock
   → Tạo order
   → Trừ tồn kho: 2 - 2 = 0
   → Unlock
   → Thành công ✅

4. Khách B: Thanh toán
   → Kiểm tra tồn kho: FAIL (còn 0, cần 1)
   → Trả về lỗi: "Sản phẩm không đủ hàng"
   → Yêu cầu giảm số lượng hoặc xóa khỏi giỏ
   → Thất bại ❌
```

---

## 🎨 UX Improvements

### 1. Hiển thị cảnh báo trong giỏ hàng
```typescript
// cart/page.tsx
{item.product.stockQuantity < item.quantity && (
  <div className="text-red-500 text-sm mt-1">
    ⚠️ Chỉ còn {item.product.stockQuantity} sản phẩm
  </div>
)}
```

### 2. Tự động điều chỉnh số lượng
```typescript
const handleCheckout = async () => {
  // Kiểm tra trước khi chuyển checkout
  const hasOutOfStock = cart.items.some(
    item => item.quantity > item.product.stockQuantity
  )
  
  if (hasOutOfStock) {
    toast.error('Vui lòng điều chỉnh số lượng sản phẩm')
    return
  }
  
  router.push('/checkout')
}
```

### 3. Real-time stock update
```typescript
// Polling mỗi 30s để cập nhật tồn kho
useEffect(() => {
  const interval = setInterval(() => {
    loadCart() // Refresh cart
  }, 30000)
  
  return () => clearInterval(interval)
}, [])
```

---

## ✅ Checklist Implementation

- [ ] Thêm validation tồn kho trong OrderService
- [ ] Implement decreaseStock trong InventoryService
- [ ] Thêm pessimistic lock hoặc optimistic lock
- [ ] Xử lý lỗi hết hàng ở frontend
- [ ] Hiển thị cảnh báo trong giỏ hàng
- [ ] Test race condition (2 user cùng mua)
- [ ] Test với số lượng lớn
- [ ] Monitor overselling trong production

---

## 🧪 Test Cases

### Test 1: Đủ hàng
- Kho: 10
- Khách A mua: 5
- Kết quả: ✅ Thành công, kho còn 5

### Test 2: Không đủ hàng
- Kho: 2
- Khách A mua: 5
- Kết quả: ❌ Lỗi "Không đủ hàng"

### Test 3: Race condition
- Kho: 2
- Khách A mua: 2 (cùng lúc)
- Khách B mua: 1 (cùng lúc)
- Kết quả: 
  - Khách A: ✅ Thành công
  - Khách B: ❌ Lỗi "Hết hàng"

### Test 4: Multiple items
- Kho: SP1=5, SP2=2
- Khách mua: SP1=3, SP2=3
- Kết quả: ❌ Lỗi "SP2 không đủ hàng (còn 2, yêu cầu 3)"
