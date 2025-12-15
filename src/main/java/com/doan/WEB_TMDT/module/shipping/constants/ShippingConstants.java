package com.doan.WEB_TMDT.module.shipping.constants;

import java.util.Arrays;
import java.util.List;

public class ShippingConstants {
    
    // Danh sách quận nội thành Hà Nội (miễn phí ship)
    public static final List<String> HANOI_INNER_DISTRICTS = Arrays.asList(
        "Ba Đình", "Hoàn Kiếm", "Đống Đa", "Hai Bà Trưng",
        "Cầu Giấy", "Thanh Xuân", "Tây Hồ", "Long Biên",
        "Hoàng Mai", "Hà Đông", "Nam Từ Liêm", "Bắc Từ Liêm"
    );
    
    // GHN Service Type IDs
    public static final int GHN_SERVICE_TYPE_EXPRESS = 2; // Hỏa tốc
    public static final int GHN_SERVICE_TYPE_STANDARD = 1; // Tiêu chuẩn
    
    // Default shipping settings
    public static final int DEFAULT_WEIGHT = 500; // gram
    public static final int DEFAULT_LENGTH = 20; // cm
    public static final int DEFAULT_WIDTH = 15; // cm
    public static final int DEFAULT_HEIGHT = 10; // cm
    
    // Shop info (from address)
    public static final int SHOP_DISTRICT_ID = 1542; // Hà Đông, Hà Nội
    public static final String SHOP_WARD_CODE = "21211"; // Phường Văn Quán
    
    // Shipping methods
    public static final String SHIP_METHOD_INTERNAL = "INTERNAL"; // Shipper riêng
    public static final String SHIP_METHOD_GHN = "GHN"; // Giao Hàng Nhanh
    
    // Estimated delivery time
    public static final String DELIVERY_TIME_HANOI_INNER = "1-2 ngày";
    public static final String DELIVERY_TIME_DEFAULT = "3-5 ngày";
    
    private ShippingConstants() {
        // Private constructor to prevent instantiation
    }
}
