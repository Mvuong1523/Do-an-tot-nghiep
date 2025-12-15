package com.doan.WEB_TMDT.module.shipping.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GHNDistrictMapper {
    
    @Value("${ghn.api.url}")
    private String ghnApiUrl;

    @Value("${ghn.api.token}")
    private String ghnApiToken;
    
    private final RestTemplate restTemplate;
    
    private static final Map<String, Map<String, Integer>> DISTRICT_MAP = initDistrictMap();
    
    private static Map<String, Map<String, Integer>> initDistrictMap() {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        
        // Hà Nội
        Map<String, Integer> hanoi = new HashMap<>();
        hanoi.put("Ba Đình", 1454);
        hanoi.put("Hoàn Kiếm", 1452);
        hanoi.put("Hai Bà Trưng", 1451);
        hanoi.put("Đống Đa", 1450);
        hanoi.put("Tây Hồ", 1453);
        hanoi.put("Cầu Giấy", 1449);
        hanoi.put("Thanh Xuân", 1455);
        hanoi.put("Hoàng Mai", 1448);
        hanoi.put("Long Biên", 1447);
        hanoi.put("Nam Từ Liêm", 3440);
        hanoi.put("Bắc Từ Liêm", 3439);
        hanoi.put("Hà Đông", 1485);
        map.put("Hà Nội", hanoi);
        
        // TP. Hồ Chí Minh
        Map<String, Integer> hcm = new HashMap<>();
        hcm.put("Quận 1", 1442);
        hcm.put("Quận 2", 1443);
        hcm.put("Quận 3", 1444);
        hcm.put("Quận 4", 1445);
        hcm.put("Quận 5", 1446);
        hcm.put("Quận 6", 1447);
        hcm.put("Quận 7", 1448);
        hcm.put("Quận 8", 1449);
        hcm.put("Quận 9", 1450);
        hcm.put("Quận 10", 1451);
        hcm.put("Quận 11", 1453);
        hcm.put("Quận 12", 1454);
        hcm.put("Bình Thạnh", 1462);
        hcm.put("Tân Bình", 1458);
        hcm.put("Tân Phú", 1459);
        hcm.put("Phú Nhuận", 1457);
        hcm.put("Gò Vấp", 1461);
        hcm.put("Bình Tân", 1463);
        hcm.put("Thủ Đức", 3695);
        map.put("TP. Hồ Chí Minh", hcm);
        map.put("Hồ Chí Minh", hcm);
        
        return map;
    }

    public Integer getDistrictId(String provinceName, String districtName) {
        log.info("Looking for district: {} in province: {}", districtName, provinceName);
        
        Integer districtId = findInHardcodedMap(provinceName, districtName);
        if (districtId != null) {
            return districtId;
        }
        
        log.info("District not in map, calling GHN API...");
        return findViaGHNApi(provinceName, districtName);
    }

    private Integer findInHardcodedMap(String provinceName, String districtName) {
        for (Map.Entry<String, Map<String, Integer>> entry : DISTRICT_MAP.entrySet()) {
            if (matchLocation(entry.getKey(), provinceName)) {
                log.info("Found province in map: {}", entry.getKey());
                
                for (Map.Entry<String, Integer> district : entry.getValue().entrySet()) {
                    if (matchLocation(district.getKey(), districtName)) {
                        log.info("Found district in map: {} → ID: {}", district.getKey(), district.getValue());
                        return district.getValue();
                    }
                }
                break;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Integer findViaGHNApi(String provinceName, String districtName) {
        try {
            Integer provinceId = getProvinceId(provinceName);
            String url = ghnApiUrl + "/master-data/district";
            
            Map<String, Object> body = new HashMap<>();
            body.put("province_id", provinceId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Token", ghnApiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), Map.class);
            
            if (response != null && Integer.valueOf(200).equals(response.get("code"))) {
                List<Map<String, Object>> districts = (List<Map<String, Object>>) response.get("data");
                
                for (Map<String, Object> district : districts) {
                    if (matchLocation((String) district.get("DistrictName"), districtName)) {
                        Integer id = (Integer) district.get("DistrictID");
                        log.info("Found district via API: {} → ID: {}", district.get("DistrictName"), id);
                        return id;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error calling GHN API: {}", e.getMessage());
        }
        
        return 1485; // Default Hà Đông
    }

    @SuppressWarnings("unchecked")
    private Integer getProvinceId(String provinceName) {
        try {
            String url = ghnApiUrl + "/master-data/province";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Token", ghnApiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> response = restTemplate.postForObject(url, new HttpEntity<>(headers), Map.class);
            
            if (response != null && Integer.valueOf(200).equals(response.get("code"))) {
                List<Map<String, Object>> provinces = (List<Map<String, Object>>) response.get("data");
                
                for (Map<String, Object> province : provinces) {
                    if (matchLocation((String) province.get("ProvinceName"), provinceName)) {
                        return (Integer) province.get("ProvinceID");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting province ID: {}", e.getMessage());
        }
        
        return 201; // Default Hanoi
    }

    private boolean matchLocation(String ghnName, String userInput) {
        String normalized1 = normalize(ghnName);
        String normalized2 = normalize(userInput);
        
        if (normalized1.equals(normalized2)) return true;
        if (normalized2.matches(".*\\d$")) {
            return normalized1.equals(normalized2) || normalized1.endsWith(" " + normalized2);
        }
        return normalized1.contains(normalized2) || normalized2.contains(normalized1);
    }

    private String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase().trim()
                .replaceAll("^(tp\\.|tp |thành phố |tỉnh |quận |huyện |thị xã )", "")
                .replaceAll("\\s+", " ").trim();
    }
}
