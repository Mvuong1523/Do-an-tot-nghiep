# Hướng dẫn Fix Ward Name cho đơn hàng cũ

## Vấn đề
Đơn hàng cũ chỉ có `ward` (mã code) mà không có `wardName` (tên phường/xã), nên hiển thị mã thay vì tên.

## Giải pháp

### Option 1: Update tất cả đơn hàng cũ với tên tạm thời

```sql
-- Update wardName = ward code với prefix
UPDATE orders 
SET ward_name = CONCAT('Phường/Xã mã ', ward)
WHERE ward IS NOT NULL 
  AND (ward_name IS NULL OR ward_name = '');
```

### Option 2: Update từng ward code cụ thể (Chính xác nhất)

Tra cứu tên phường từ GHN API hoặc tra Google, rồi update:

```sql
-- Ví dụ: Ward code 190404 ở Bắc Ninh
UPDATE orders 
SET ward_name = 'Xã Liên Bão'  -- Tên thực tế
WHERE ward = '190404';

-- Ví dụ: Ward code 20308 ở Hà Nội
UPDATE orders 
SET ward_name = 'Phường Dịch Vọng'
WHERE ward = '20308';
```

### Option 3: Tạo script tự động tra cứu từ GHN

Tạo một Java service để:
1. Lấy tất cả orders có ward nhưng chưa có wardName
2. Gọi GHN API để lấy tên phường
3. Update vào database

```java
@Service
public class WardNameFixService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ShippingService shippingService;
    
    public void fixAllWardNames() {
        // Lấy tất cả orders cần fix
        List<Order> orders = orderRepository.findByWardIsNotNullAndWardNameIsNull();
        
        for (Order order : orders) {
            try {
                // Lấy district ID từ province và district name
                Integer districtId = getDistrictId(order.getProvince(), order.getDistrict());
                
                // Lấy danh sách wards
                List<Map<String, Object>> wards = shippingService.getWards(districtId);
                
                // Tìm ward name
                Optional<Map<String, Object>> ward = wards.stream()
                    .filter(w -> order.getWard().equals(w.get("code")))
                    .findFirst();
                
                if (ward.isPresent()) {
                    String wardName = (String) ward.get().get("name");
                    order.setWardName(wardName);
                    orderRepository.save(order);
                    System.out.println("Updated order " + order.getOrderCode() + " with ward name: " + wardName);
                }
            } catch (Exception e) {
                System.err.println("Error fixing ward name for order " + order.getOrderCode() + ": " + e.getMessage());
            }
        }
    }
}
```

### Option 4: Fix thủ công từng đơn

1. Vào database xem ward code
2. Tra Google: "mã phường 190404 bắc ninh"
3. Update thủ công:

```sql
UPDATE orders 
SET ward_name = 'Tên phường tra được'
WHERE id = 123;
```

## Kiểm tra sau khi fix

```sql
-- Xem các đơn đã có wardName
SELECT id, order_code, ward, ward_name, district, province 
FROM orders 
WHERE ward IS NOT NULL
ORDER BY created_at DESC
LIMIT 20;

-- Đếm số đơn chưa có wardName
SELECT COUNT(*) as missing_ward_name
FROM orders 
WHERE ward IS NOT NULL 
  AND (ward_name IS NULL OR ward_name = '');
```

## Đơn hàng MỚI

Từ bây giờ, tất cả đơn hàng mới sẽ TỰ ĐỘNG có wardName vì:
1. Frontend gửi cả `ward` (code) và `wardName` (tên)
2. Backend lưu cả 2 vào database
3. API trả về đầy đủ cả 2

## Test

1. Tạo đơn hàng mới
2. Kiểm tra database:
```sql
SELECT ward, ward_name FROM orders ORDER BY id DESC LIMIT 1;
```
3. Kiểm tra API response có wardName không
4. Kiểm tra giao diện warehouse hiển thị tên phường đúng không

## Lưu ý

- Ward code dùng để gửi cho GHN API
- Ward name dùng để hiển thị cho người dùng
- Cần có CẢ HAI để hệ thống hoạt động tốt
