package com.doan.WEB_TMDT.module.shipping.service.impl;

import com.doan.WEB_TMDT.module.shipping.dto.CalculateShippingFeeRequest;
import com.doan.WEB_TMDT.module.shipping.dto.ShippingFeeResponse;
import com.doan.WEB_TMDT.module.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    @Value("${ghtk.api.url:https://services.giaohangtietkiem.vn}")
    private String ghtkApiUrl;

    @Value("${ghtk.api.token:demo_token}")
    private String ghtkApiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    // Danh sách quận nội thành Hà Nội (miễn phí ship)
    private static final List<String> HANOI_INNER_DISTRICTS = Arrays.asList(
            "Ba Đình", "Hoàn Kiếm", "Hai Bà Trưng", "Đống Đa",
            "Tây Hồ", "Cầu Giấy", "Thanh Xuân", "Hoàng Mai",
            "Long Biên", "Nam Từ Liêm", "Bắc Từ Liêm", "Hà Đông"
    );

    @Override
    public ShippingFeeResponse calculateShippingFee(CalculateShippingFeeRequest request) {
        // 1. Check if Hanoi inner city (free ship)
        if (isHanoiInnerCity(request.getProvince(), request.getDistrict())) {
            return ShippingFeeResponse.builder()
                    .fee(0.0)
                    .shipMethod("INTERNAL")
                    .estimatedTime("1-2 ngày")
                    .isFreeShip(true)
                    .build();
        }

        // 2. Call GHTK API to calculate fee
        try {
            Double ghtkFee = callGHTKApi(request);
            return ShippingFeeResponse.builder()
                    .fee(ghtkFee)
                    .shipMethod("GHTK")
                    .estimatedTime("3-5 ngày")
                    .isFreeShip(false)
                    .build();
        } catch (Exception e) {
            log.error("Error calling GHTK API", e);
            // Fallback: Tính phí cố định theo khu vực
            return ShippingFeeResponse.builder()
                    .fee(calculateFallbackFee(request))
                    .shipMethod("GHTK")
                    .estimatedTime("3-5 ngày")
                    .isFreeShip(false)
                    .build();
        }
    }

    @Override
    public boolean isHanoiInnerCity(String province, String district) {
        if (province == null || district == null) {
            return false;
        }

        // Normalize strings
        String normalizedProvince = province.trim().toLowerCase();
        String normalizedDistrict = district.trim();

        // Check if Hanoi
        boolean isHanoi = normalizedProvince.contains("hà nội") || 
                         normalizedProvince.contains("ha noi") ||
                         normalizedProvince.equals("hanoi");

        if (!isHanoi) {
            return false;
        }

        // Check if inner district
        return HANOI_INNER_DISTRICTS.stream()
                .anyMatch(innerDistrict -> normalizedDistrict.contains(innerDistrict));
    }

    private Double callGHTKApi(CalculateShippingFeeRequest request) {
        // TODO: Implement real GHTK API call
        // POST https://services.giaohangtietkiem.vn/services/shipment/fee
        // Headers: Token: {ghtkApiToken}
        // Body: {
        //   "pick_province": "Hà Nội",
        //   "pick_district": "Cầu Giấy",
        //   "province": request.getProvince(),
        //   "district": request.getDistrict(),
        //   "address": request.getAddress(),
        //   "weight": request.getWeight(),
        //   "value": request.getValue()
        // }

        // For demo, return mock fee
        return calculateFallbackFee(request);
    }

    private Double calculateFallbackFee(CalculateShippingFeeRequest request) {
        // Tính phí dự phòng khi API GHTK lỗi
        String province = request.getProvince().toLowerCase();

        // Hà Nội ngoại thành
        if (province.contains("hà nội") || province.contains("ha noi")) {
            return 25000.0;
        }

        // Các tỉnh lân cận (Bắc Ninh, Bắc Giang, Hưng Yên, Hải Dương, Hải Phòng)
        if (province.contains("bắc ninh") || province.contains("bắc giang") ||
            province.contains("hưng yên") || province.contains("hải dương") ||
            province.contains("hải phòng")) {
            return 30000.0;
        }

        // Miền Bắc
        if (province.contains("quảng ninh") || province.contains("thái nguyên") ||
            province.contains("vĩnh phúc") || province.contains("phú thọ")) {
            return 35000.0;
        }

        // Miền Trung
        if (province.contains("nghệ an") || province.contains("hà tĩnh") ||
            province.contains("quảng bình") || province.contains("đà nẵng") ||
            province.contains("huế")) {
            return 45000.0;
        }

        // Miền Nam
        if (province.contains("tp hồ chí minh") || province.contains("hồ chí minh") ||
            province.contains("sài gòn") || province.contains("đồng nai") ||
            province.contains("bình dương")) {
            return 40000.0;
        }

        // Các tỉnh xa khác
        return 50000.0;
    }
}
