package com.doan.WEB_TMDT.module.delivery.dto;
import lombok.Data;
import java.util.List;

@Data
public class DeliveryCreateOrderRequest {
    private List<DeliveryProduct> products;
    private Order order;

    @Data
    public static class Order {
        private String id;

        // pick (kho)
        private String pick_name, pick_address, pick_province, pick_district, pick_ward, pick_tel;
        private String pick_address_id, pick_street, pick_email, pick_ext_tel;

        // receiver
        private String name, address, province, district, ward, street, hamlet, tel, ext_tel, email, note;

        // phí/COD
        private Integer pick_money, is_freeship, value;

        // option
        private String transport;    // fly / road
        private String pick_option;  // cod / post
        private Double total_weight;

        // (optional khác: work_shift, return_address...) thêm sau nếu cần
    }
}
