# Fix Stock Management Logic

## Vấn đề hiện tại
Code đang trừ `stockQuantity` khi đặt hàng, nhưng theo yêu cầu:
- `stockQuantity` chỉ thay đổi khi **xuất kho** (warehouse export)
- Khi đặt hàng chỉ tăng `reservedQuantity` (giữ hàng)

## Cần sửa 2 chỗ trong OrderServiceImpl.java

### 1. Method cancelOrderByCustomer (dòng ~329)
```java
// THAY THẾ:
// Restore stock for cancelled order
for (OrderItem item : order.getItems()) {
    Product product = item.getProduct();
    Long currentStock = product.getStockQuantity();
    Long restoredStock = currentStock + item.getQuantity();
    product.setStockQuantity(restoredStock);  // ❌ BỎ DÒNG NÀY
    
    // Update reserved quantity
    Long currentReserved = product.getReservedQuantity() != null ? product.getReservedQuantity() : 0L;
    Long newReserved = Math.max(0, currentReserved - item.getQuantity());
    product.setReservedQuantity(newReserved);
    
    log.info("Restored stock for product {}: {} -> {} (returned: {})", 
        product.getName(), currentStock, restoredStock, item.getQuantity());
}

// BẰNG:
// Release reserved stock (bỏ giữ hàng)
for (OrderItem item : order.getItems()) {
    Product product = item.getProduct();
    
    // Release reserved quantity only
    Long currentReserved = product.getReservedQuantity() != null ? product.getReservedQuantity() : 0L;
    Long newReserved = Math.max(0, currentReserved - item.getQuantity());
    product.setReservedQuantity(newReserved);
    
    log.info("Released reserved for product {}: {} -> {}", 
        product.getName(), currentReserved, newReserved);
}
```

### 2. Method cancelOrder (dòng ~649)
Sửa tương tự như trên.

## Luồng đúng

```
1. Nhập kho (Inventory Import)
   → Tăng tồn kho thực (Inventory.quantity)
   
2. Xuất kho bán hàng (Warehouse Export)
   → Trừ tồn kho thực (Inventory.quantity)
   → Tăng stock có thể bán (Product.stockQuantity)
   
3. Khách đặt hàng
   → Tăng reserved (Product.reservedQuantity)
   → stockQuantity KHÔNG ĐỔI
   
4. Khách hủy đơn
   → Giảm reserved (Product.reservedQuantity)
   → stockQuantity KHÔNG ĐỔI
   
5. Giao hàng thành công
   → Giảm reserved (Product.reservedQuantity)
   → stockQuantity KHÔNG ĐỔI (đã trừ lúc xuất kho)
```

## Available Quantity (Số lượng có thể mua)

```java
availableQuantity = stockQuantity - reservedQuantity
```

Ví dụ:
- stockQuantity = 100 (đã xuất kho, sẵn sàng bán)
- reservedQuantity = 30 (đang giữ cho 30 đơn hàng)
- availableQuantity = 70 (khách còn có thể đặt 70 cái)
