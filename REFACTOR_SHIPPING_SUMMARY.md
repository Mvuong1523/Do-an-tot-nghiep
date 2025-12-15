# Refactor ShippingServiceImpl - Tóm tắt

## ✅ Đã tạo:

### 1. ShippingConstants.java (40 dòng)
- Danh sách quận nội thành Hà Nội
- GHN service type IDs
- Default shipping settings
- Shop info

### 2. AddressService.java (65 dòng)
- `isHanoiInnerCity()` - Check miễn phí ship
- `isValidAddress()` - Validate địa chỉ
- `normalizeProvinceName()` - Chuẩn hóa tên tỉnh
- `normalizeDistrictName()` - Chuẩn hóa tên quận

### 3. GHNApiClient.java (200 dòng)
- `calculateShippingFee()` - Tính phí qua GHN
- `getAvailableServiceType()` - Lấy service type
- `getDistrictId()` - Lấy district ID
- `createHeaders()` - Tạo headers cho API

## 📝 Cần làm tiếp:

### 4. Refactor ShippingServiceImpl
Thay đổi từ 661 dòng → ~100 dòng:

**Inject dependencies:**
```java
private final GHNApiClient ghnApiClient;
private final AddressService addressService;
```

**Method calculateShippingFee():**
```java
public ShippingFeeResponse calculateShippingFee(CalculateShippingFeeRequest request) {
    // Check free ship
    if (addressService.isHanoiInnerCity(request.getProvince(), request.getDistrict())) {
        return ShippingFeeResponse.builder()
                .fee(0.0)
                .shipMethod(ShippingConstants.SHIP_METHOD_INTERNAL)
                .estimatedTime(ShippingConstants.DELIVERY_TIME_HANOI_INNER)
                .isFreeShip(true)
                .build();
    }

    // Call GHN
    Integer districtId = ghnApiClient.getDistrictId(request.getProvince(), request.getDistrict());
    Map<String, Object> result = ghnApiClient.calculateShippingFee(
            districtId,
            request.getWardCode(),
            request.getWeight() != null ? request.getWeight() : ShippingConstants.DEFAULT_WEIGHT,
            ShippingConstants.DEFAULT_LENGTH,
            ShippingConstants.DEFAULT_WIDTH,
            ShippingConstants.DEFAULT_HEIGHT,
            request.getInsuranceValue()
    );
    
    return ShippingFeeResponse.builder()
            .fee((Double) result.get("fee"))
            .shipMethod(ShippingConstants.SHIP_METHOD_GHN)
            .estimatedTime((String) result.get("expectedTime"))
            .isFreeShip(false)
            .build();
}
```

**Xóa các method đã move:**
- ❌ `isHanoiInnerCity()` → AddressService
- ❌ `callGHNApi()` → GHNApiClient
- ❌ `getDistrictId()` → GHNApiClient
- ❌ `getAvailableServiceType()` → GHNApiClient
- ❌ Constants (HANOI_INNER_DISTRICTS, etc.) → ShippingConstants

**Giữ lại:**
- ✅ `createGHNOrder()` - Tạo đơn GHN
- ✅ `getGHNOrderDetail()` - Lấy chi tiết đơn
- ✅ `cancelGHNOrder()` - Hủy đơn GHN
- ✅ `getProvinces()`, `getDistricts()`, `getWards()` - Lấy địa chỉ

## 📊 Kết quả:

**Trước:**
- ShippingServiceImpl: 661 dòng

**Sau:**
- ShippingServiceImpl: ~100 dòng
- ShippingConstants: 40 dòng
- AddressService: 65 dòng
- GHNApiClient: 200 dòng
- **Tổng: 405 dòng** (giảm 256 dòng)

**Lợi ích:**
- ✅ Dễ đọc, dễ hiểu
- ✅ Dễ test (mock từng service)
- ✅ Dễ maintain
- ✅ Tách biệt concerns
- ✅ Reusable (AddressService, GHNApiClient có thể dùng ở nơi khác)

## 🚀 Bước tiếp theo:

1. Update ShippingServiceImpl để inject và sử dụng các service mới
2. Xóa code duplicate
3. Test lại các chức năng
4. (Optional) Tạo GHNOrderService riêng cho create/cancel/track order
