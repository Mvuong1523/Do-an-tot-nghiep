package com.doan.WEB_TMDT.module.shipping.helper;

import org.springframework.stereotype.Component;

@Component
public class GHNStatusMapper {
    
    public String getStatusText(String status) {
        if (status == null) return "Không xác định";
        
        switch (status) {
            case "ready_to_pick": return "Chờ lấy hàng";
            case "picking": return "Đang lấy hàng";
            case "cancel": return "Đã hủy";
            case "money_collect_picking": return "Đang thu tiền người gửi";
            case "picked": return "Đã lấy hàng";
            case "storing": return "Hàng đang nằm ở kho";
            case "transporting": return "Đang luân chuyển";
            case "sorting": return "Đang phân loại";
            case "delivering": return "Đang giao hàng";
            case "money_collect_delivering": return "Đang thu tiền người nhận";
            case "delivered": return "Đã giao hàng";
            case "delivery_fail": return "Giao hàng thất bại";
            case "waiting_to_return": return "Chờ trả hàng";
            case "return": return "Trả hàng";
            case "return_transporting": return "Đang luân chuyển hàng trả";
            case "return_sorting": return "Đang phân loại hàng trả";
            case "returning": return "Đang trả hàng";
            case "return_fail": return "Trả hàng thất bại";
            case "returned": return "Đã trả hàng";
            case "exception": return "Đơn hàng ngoại lệ";
            case "damage": return "Hàng bị hư hỏng";
            case "lost": return "Hàng bị thất lạc";
            default: return status;
        }
    }
}
