package com.doan.WEB_TMDT.module.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "export_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExportOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String exportCode;  // Mã phiếu xuất, ví dụ: PX20251101-001

    private LocalDateTime exportDate;        // Ngày xuất kho (tạo phiếu là xuất ngay)
    private String createdBy;                // Người thực hiện xuất
    private String reason;                   // Lý do xuất: bán hàng / hủy hàng / đổi trả / bảo hành
    private String note;                     // Ghi chú thêm

    @OneToMany(mappedBy = "exportOrder", cascade = CascadeType.ALL)
    private List<ExportOrderItem> items;     // Danh sách sản phẩm xuất
}
