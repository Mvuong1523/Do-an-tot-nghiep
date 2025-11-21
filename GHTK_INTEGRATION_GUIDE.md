# 📦 Hướng dẫn Tích hợp Giao Hàng Tiết Kiệm (GHTK)

## 🔑 Bước 1: Đăng ký tài khoản GHTK

### 1. Truy cập trang đăng ký:
- **Production:** https://khachhang.giaohangtietkiem.vn/dang-ky
- **Sandbox (Test):** https://khachhang.giaohangtietkiem.vn/dang-ky

### 2. Điền thông tin:
- Tên shop
- Số điện thoại
- Email
- Địa chỉ lấy hàng (kho của bạn)
- CMND/CCCD
- Giấy phép kinh doanh (nếu có)

### 3. Chờ duyệt:
- GHTK sẽ liên hệ xác minh (1-2 ngày)
- Sau khi duyệt → Nhận tài khoản

---

## 🔐 Bước 2: Lấy API Token

### Cách 1: Từ Dashboard (Khuyến nghị)

1. **Đăng nhập:** https://khachhang.giaohangtietkiem.vn/
2. **Vào menu:** Cài đặt → Thông tin tài khoản
3. **Tìm mục:** API Token / Token
4. **Copy token:** Dạng `xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

### Cách 2: Liên hệ GHTK

- **Hotline:** 1900 2035
- **Email:** hotro@giaohangtietkiem.vn
- Yêu cầu: "Cho tôi API token để tích hợp"

---

## 🧪 Bước 3: Test API với Postman

### API Tính phí ship:

```http
POST https://services.giaohangtietkiem.vn/services/shipment/fee
Content-Type: application/json
Token: YOUR_API_TOKEN_HERE

{
  "pick_province": "Hà Nội",
  "pick_district": "Cầu Giấy",
  "province": "Hồ Chí Minh",
  "district": "Quận 1",
  "address": "123 Nguyễn Huệ",
  "weight": 1000,
  "value": 3000000,
  "transport": "road",
  "deliver_option": "xteam"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Thành công",
  "fee": {
    "name": "Giao hàng tiết kiệm",
    "fee": 30000,
    "insurance_fee": 15000,
    "delivery_type": "Giao hàng tiết kiệm"
  }
}
```

---

## 💻 Bước 4: Implement Backend

### 1. Tạo GHTKService:

```java
@Service
public class GHTKService {
    
    @Value("${ghtk.api.url:https://services.giaohangtietkiem.vn}")
    private String apiUrl;
    
    @Value("${ghtk.api.token}")
    private String apiToken;
    
    private final RestTemplate restTemplate;
    
    public GHTKService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Tính phí ship
     */
    public GHTKFeeResponse calculateShippingFee(GHTKFeeRequest request) {
        String url = apiUrl + "/services/shipment/fee";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", apiToken);
        
        HttpEntity<GHTKFeeRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<GHTKFeeResponse> response = restTemplate.postForEntity(
                url, 
                entity, 
                GHTKFeeResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tính phí ship: " + e.getMessage());
        }
    }
    
    /**
     * Tạo đơn hàng GHTK
     */
    public GHTKOrderResponse createOrder(GHTKOrderRequest request) {
        String url = apiUrl + "/services/shipment/order";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", apiToken);
        
        HttpEntity<GHTKOrderRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<GHTKOrderResponse> response = restTemplate.postForEntity(
                url, 
                entity, 
                GHTKOrderResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo đơn GHTK: " + e.getMessage());
        }
    }
}
```

### 2. Tạo DTOs:

```java
@Data
public class GHTKFeeRequest {
    private String pick_province;    // Tỉnh lấy hàng
    private String pick_district;    // Quận lấy hàng
    private String province;         // Tỉnh giao hàng
    private String district;         // Quận giao hàng
    private String address;          // Địa chỉ giao hàng
    private Integer weight;          // Khối lượng (gram)
    private Integer value;           // Giá trị đơn hàng
    private String transport = "road"; // Loại vận chuyển
    private String deliver_option = "xteam"; // Phương thức giao
}

@Data
public class GHTKFeeResponse {
    private Boolean success;
    private String message;
    private FeeDetail fee;
    
    @Data
    public static class FeeDetail {
        private String name;
        private Integer fee;           // Phí ship
        private Integer insurance_fee; // Phí bảo hiểm
        private String delivery_type;
    }
}
```

### 3. Tạo Controller:

```java
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {
    
    private final GHTKService ghtkService;
    
    @PostMapping("/calculate-fee")
    public ApiResponse calculateFee(@RequestBody CalculateFeeRequest request) {
        // Chuyển đổi sang GHTK format
        GHTKFeeRequest ghtkRequest = GHTKFeeRequest.builder()
            .pick_province("Hà Nội")
            .pick_district("Cầu Giấy")
            .province(request.getProvince())
            .district(request.getDistrict())
            .address(request.getAddress())
            .weight(request.getWeight())
            .value(request.getValue())
            .build();
        
        try {
            GHTKFeeResponse response = ghtkService.calculateShippingFee(ghtkRequest);
            
            if (response.getSuccess()) {
                return ApiResponse.success("Tính phí thành công", 
                    Map.of(
                        "fee", response.getFee().getFee(),
                        "insurance_fee", response.getFee().getInsurance_fee(),
                        "total", response.getFee().getFee() + response.getFee().getInsurance_fee()
                    )
                );
            } else {
                return ApiResponse.error(response.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("Lỗi khi tính phí: " + e.getMessage());
        }
    }
}
```

### 4. Cấu hình application.properties:

```properties
# GHTK Configuration
ghtk.api.url=https://services.giaohangtietkiem.vn
ghtk.api.token=YOUR_TOKEN_HERE

# Địa chỉ kho (lấy hàng)
ghtk.warehouse.province=Hà Nội
ghtk.warehouse.district=Cầu Giấy
ghtk.warehouse.ward=Dịch Vọng
ghtk.warehouse.address=123 Đường ABC
```

---

## 🎨 Bước 5: Implement Frontend

### 1. Tạo API helper:

```typescript
// lib/shipping.ts
export const shippingApi = {
  calculateFee: async (data: {
    province: string
    district: string
    address: string
    weight: number
    value: number
  }) => {
    const response = await fetch('http://localhost:8080/api/shipping/calculate-fee', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: JSON.stringify(data)
    })
    return response.json()
  }
}
```

### 2. Cập nhật checkout page:

```typescript
// Tính phí ship khi thay đổi địa chỉ
useEffect(() => {
  const calculateShipping = async () => {
    if (form.province && form.district && form.address) {
      // Kiểm tra nội thành HN
      const isHanoi = form.province.toLowerCase().includes('hà nội')
      
      if (isHanoi) {
        // Miễn phí nội thành
        setForm(prev => ({ ...prev, shippingFee: 0 }))
        setShippingMethod('internal')
      } else {
        // Gọi API GHTK
        try {
          const totalWeight = items.reduce((sum, item) => 
            sum + (item.weight || 500) * item.quantity, 0
          )
          
          const response = await shippingApi.calculateFee({
            province: form.province,
            district: form.district,
            address: form.address,
            weight: totalWeight,
            value: calculateSubtotal()
          })
          
          if (response.success) {
            setForm(prev => ({ 
              ...prev, 
              shippingFee: response.data.total 
            }))
            setShippingMethod('ghtk')
          }
        } catch (error) {
          console.error('Error calculating shipping:', error)
          // Fallback: Phí cố định
          setForm(prev => ({ ...prev, shippingFee: 30000 }))
        }
      }
    }
  }
  
  // Debounce để tránh gọi API liên tục
  const timer = setTimeout(calculateShipping, 500)
  return () => clearTimeout(timer)
}, [form.province, form.district, form.address])
```

---

## 📝 Bước 6: Test

### Test Case 1: Nội thành HN
```
Tỉnh: Hà Nội
Quận: Cầu Giấy
→ Phí ship: 0đ (Shipper riêng)
```

### Test Case 2: Ngoại thành HN
```
Tỉnh: Hà Nội
Quận: Sóc Sơn
→ Phí ship: ~25,000đ (GHTK)
```

### Test Case 3: Ngoài HN
```
Tỉnh: Hồ Chí Minh
Quận: Quận 1
→ Phí ship: ~30,000đ (GHTK)
```

---

## 🚀 Bước 7: Tạo đơn hàng GHTK (Sau khi khách đặt hàng)

```java
@Service
public class OrderServiceImpl {
    
    @Autowired
    private GHTKService ghtkService;
    
    @Transactional
    public ApiResponse createOrder(CreateOrderRequest request) {
        // 1. Tạo order trong database
        Order order = createOrderEntity(request);
        orderRepository.save(order);
        
        // 2. Nếu ship ngoài HN → Tạo đơn GHTK
        if (!isHanoiInternal(order.getProvince())) {
            GHTKOrderRequest ghtkRequest = buildGHTKOrderRequest(order);
            GHTKOrderResponse ghtkResponse = ghtkService.createOrder(ghtkRequest);
            
            if (ghtkResponse.getSuccess()) {
                // Lưu tracking code
                order.setGhtkTrackingCode(ghtkResponse.getOrder().getLabel());
                orderRepository.save(order);
            }
        }
        
        return ApiResponse.success("Đặt hàng thành công", order);
    }
}
```

---

## 📚 Tài liệu API GHTK

- **Docs:** https://docs.giaohangtietkiem.vn/
- **API Reference:** https://docs.giaohangtietkiem.vn/#api-reference
- **Postman Collection:** https://www.postman.com/ghtk-api

---

## ⚠️ Lưu ý

1. **Token bảo mật:** Không commit token vào Git
2. **Rate limit:** GHTK giới hạn số request/phút
3. **Địa chỉ chuẩn:** Dùng tên tỉnh/quận chính xác
4. **Khối lượng:** Tính đúng để phí chính xác
5. **Test trước:** Dùng sandbox trước khi lên production

---

## 💰 Bảng giá tham khảo

| Khoảng cách | Khối lượng | Phí ship |
|-------------|------------|----------|
| Nội thành HN | < 3kg | 0đ (Shipper riêng) |
| Ngoại thành HN | < 3kg | 20,000 - 30,000đ |
| HN → HCM | < 3kg | 30,000 - 40,000đ |
| HN → Miền Trung | < 3kg | 35,000 - 45,000đ |

*Giá thực tế tùy thuộc vào hợp đồng với GHTK*
