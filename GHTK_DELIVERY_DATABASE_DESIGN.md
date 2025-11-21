# THIẾT KẾ DATABASE CHO MODULE DELIVERY (GHTK)

## TỔNG QUAN

Module Delivery tích hợp với GHTK API để:
- Tạo đơn giao hàng
- Tính phí vận chuyển
- Theo dõi trạng thái đơn hàng
- Hủy đơn hàng
- In nhãn vận đơn

API Documentation: https://api.ghtk.vn/docs/submit-order/logistic-overview

---

## 1. BẢNG: shipments

Lưu thông tin đơn giao hàng

```sql
CREATE TABLE shipments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Liên kết
    order_id BIGINT NOT NULL,
    shipment_number INT NOT NULL DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Mã vận đơn
    tracking_code VARCHAR(50) UNIQUE,
    label_id VARCHAR(100),
    
    -- Thông tin người gửi (Shop)
    pick_name VARCHAR(255) NOT NULL,
    pick_address VARCHAR(500) NOT NULL,
    pick_province VARCHAR(100) NOT NULL,
    pick_district VARCHAR(100) NOT NULL,
    pick_ward VARCHAR(100),
    pick_tel VARCHAR(20) NOT NULL,
    
    -- Thông tin người nhận
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    province VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    ward VARCHAR(100),
    hamlet VARCHAR(100),
    tel VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    
    -- Thông tin hàng hóa
    pick_money DECIMAL(15,2) DEFAULT 0,
    value DECIMAL(15,2) NOT NULL,
    weight_option ENUM('gram', 'kilogram') DEFAULT 'gram',
    total_weight INT NOT NULL,
    
    -- Phí vận chuyển
    transport VARCHAR(50),
    deliver_option ENUM('none', 'xteam') DEFAULT 'none',
    shipping_fee DECIMAL(15,2),
    insurance_fee DECIMAL(15,2) DEFAULT 0,
    pick_money_fee DECIMAL(15,2) DEFAULT 0,
    coupon_code VARCHAR(50),
    
    -- Ghi chú
    note VARCHAR(500),
    
    -- Trạng thái
    status ENUM(
        'PENDING',      -- Chờ xử lý
        'PICKING',      -- Đang lấy hàng
        'PICKED',       -- Đã lấy hàng
        'STORING',      -- Đang lưu kho
        'DELIVERING',   -- Đang giao
        'DELIVERED',    -- Đã giao
        'CANCELLED',    -- Đã hủy
        'RETURNED'      -- Hoàn trả
    ) NOT NULL DEFAULT 'PENDING',
    
    -- GHTK Response
    ghtk_order_id VARCHAR(100),
    ghtk_label VARCHAR(100),
    ghtk_partner_id VARCHAR(100),
    ghtk_status_id INT,
    ghtk_status_text VARCHAR(255),
    ghtk_response TEXT,
    
    -- Thời gian
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    picked_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    -- Lý do hủy/hoàn
    cancel_reason VARCHAR(500),
    return_reason VARCHAR(500),
    
    FOREIGN KEY (order_id) REFERENCES orders(id),
    UNIQUE KEY uk_order_shipment (order_id, shipment_number)
);
```

---

## 2. BẢNG: shipment_items

Chi tiết sản phẩm trong đơn giao hàng

```sql
CREATE TABLE shipment_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shipment_id BIGINT NOT NULL,
    
    -- Thông tin sản phẩm
    product_name VARCHAR(255) NOT NULL,
    product_code VARCHAR(100),
    quantity INT NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    weight INT NOT NULL,
    
    -- Kích thước (cm)
    length INT,
    width INT,
    height INT,
    
    FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE
);
```

---

## 3. BẢNG: shipment_tracking

Lịch sử theo dõi đơn hàng

```sql
CREATE TABLE shipment_tracking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shipment_id BIGINT NOT NULL,
    
    -- Trạng thái
    status_id INT NOT NULL,
    status_text VARCHAR(255) NOT NULL,
    
    -- Thông tin
    location VARCHAR(255),
    reason VARCHAR(500),
    
    -- Thời gian
    action_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE
);
```

---

## 4. BẢNG: shipping_fees

Lưu cache phí vận chuyển

```sql
CREATE TABLE shipping_fees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Địa chỉ
    pick_province VARCHAR(100) NOT NULL,
    pick_district VARCHAR(100) NOT NULL,
    province VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    
    -- Thông tin hàng
    weight INT NOT NULL,
    value DECIMAL(15,2) NOT NULL,
    transport VARCHAR(50),
    deliver_option VARCHAR(50),
    
    -- Phí
    fee DECIMAL(15,2) NOT NULL,
    insurance_fee DECIMAL(15,2),
    
    -- Cache
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    
    INDEX idx_location (pick_province, pick_district, province, district),
    INDEX idx_expires (expires_at)
);
```

---

## 5. BẢNG: ghtk_webhooks

Lưu webhook từ GHTK

```sql
CREATE TABLE ghtk_webhooks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Thông tin webhook
    label_id VARCHAR(100) NOT NULL,
    partner_id VARCHAR(100),
    action_time TIMESTAMP NOT NULL,
    reason_code VARCHAR(50),
    reason VARCHAR(500),
    
    -- Trạng thái
    status_id INT NOT NULL,
    weight INT,
    fee DECIMAL(15,2),
    
    -- Raw data
    raw_data TEXT NOT NULL,
    
    -- Xử lý
    processed BOOLEAN DEFAULT FALSE,
    processed_at TIMESTAMP,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_label (label_id),
    INDEX idx_processed (processed)
);
```

---

## 6. ENUM DEFINITIONS

### Trạng thái GHTK (status_id)

```
-1: Hủy đơn hàng
1: Chưa tiếp nhận
2: Đã tiếp nhận
3: Đã lấy hàng/Đã nhập kho
4: Đã điều phối giao hàng/Đang giao hàng
5: Đã giao hàng/Chưa đối soát
6: Đã đối soát
7: Không lấy được hàng
8: Hoãn lấy hàng
9: Không giao được hàng
10: Delay giao hàng
11: Đã đối soát công nợ trả hàng
12: Đã điều phối lấy hàng/Đang lấy hàng
13: Đơn hàng bồi hoàn
20: Đang trả hàng (COD cầm hàng đi trả)
21: Đã trả hàng (COD đã trả xong hàng)
123: Shipper báo đã lấy hàng
127: Shipper (nhân viên lấy/giao hàng) báo không lấy được hàng
128: Shipper báo delay lấy hàng
45: Shipper báo đã giao hàng
49: Shipper báo không giao được giao hàng
410: Shipper báo delay giao hàng
```

---

## 7. ENTITY CLASSES

### Shipment.java

```java
@Entity
@Table(name = "shipments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "shipment_number"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "shipment_number", nullable = false)
    private Integer shipmentNumber = 1;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "tracking_code", unique = true, length = 50)
    private String trackingCode;
    
    @Column(name = "label_id", length = 100)
    private String labelId;
    
    // Người gửi
    @Column(name = "pick_name", nullable = false)
    private String pickName;
    
    @Column(name = "pick_address", nullable = false, length = 500)
    private String pickAddress;
    
    @Column(name = "pick_province", nullable = false, length = 100)
    private String pickProvince;
    
    @Column(name = "pick_district", nullable = false, length = 100)
    private String pickDistrict;
    
    @Column(name = "pick_ward", length = 100)
    private String pickWard;
    
    @Column(name = "pick_tel", nullable = false, length = 20)
    private String pickTel;
    
    // Người nhận
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 500)
    private String address;
    
    @Column(nullable = false, length = 100)
    private String province;
    
    @Column(nullable = false, length = 100)
    private String district;
    
    private String ward;
    private String hamlet;
    
    @Column(nullable = false, length = 20)
    private String tel;
    
    private String email;
    
    // Hàng hóa
    @Column(name = "pick_money")
    private Double pickMoney = 0.0;
    
    @Column(nullable = false)
    private Double value;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "weight_option")
    private WeightOption weightOption = WeightOption.GRAM;
    
    @Column(name = "total_weight", nullable = false)
    private Integer totalWeight;
    
    // Phí
    private String transport;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "deliver_option")
    private DeliverOption deliverOption = DeliverOption.NONE;
    
    @Column(name = "shipping_fee")
    private Double shippingFee;
    
    @Column(name = "insurance_fee")
    private Double insuranceFee = 0.0;
    
    @Column(name = "pick_money_fee")
    private Double pickMoneyFee = 0.0;
    
    @Column(name = "coupon_code", length = 50)
    private String couponCode;
    
    private String note;
    
    // Trạng thái
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.PENDING;
    
    // GHTK
    @Column(name = "ghtk_order_id", length = 100)
    private String ghtkOrderId;
    
    @Column(name = "ghtk_label", length = 100)
    private String ghtkLabel;
    
    @Column(name = "ghtk_partner_id", length = 100)
    private String ghtkPartnerId;
    
    @Column(name = "ghtk_status_id")
    private Integer ghtkStatusId;
    
    @Column(name = "ghtk_status_text")
    private String ghtkStatusText;
    
    @Column(name = "ghtk_response", columnDefinition = "TEXT")
    private String ghtkResponse;
    
    // Thời gian
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "picked_at")
    private LocalDateTime pickedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;
    
    @Column(name = "return_reason", length = 500)
    private String returnReason;
    
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL)
    private List<ShipmentItem> items;
    
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL)
    private List<ShipmentTracking> trackingHistory;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### ShipmentItem.java

```java
@Entity
@Table(name = "shipment_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShipmentItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "product_code", length = 100)
    private String productCode;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Double price;
    
    @Column(nullable = false)
    private Integer weight;
    
    private Integer length;
    private Integer width;
    private Integer height;
}
```

### ShipmentTracking.java

```java
@Entity
@Table(name = "shipment_tracking")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShipmentTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;
    
    @Column(name = "status_id", nullable = false)
    private Integer statusId;
    
    @Column(name = "status_text", nullable = false)
    private String statusText;
    
    private String location;
    private String reason;
    
    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

---

## 8. ENUMS

```java
public enum ShipmentStatus {
    PENDING,      // Chờ xử lý
    PICKING,      // Đang lấy hàng
    PICKED,       // Đã lấy hàng
    STORING,      // Đang lưu kho
    DELIVERING,   // Đang giao
    DELIVERED,    // Đã giao
    CANCELLED,    // Đã hủy
    RETURNED      // Hoàn trả
}

public enum WeightOption {
    GRAM,
    KILOGRAM
}

public enum DeliverOption {
    NONE,
    XTEAM
}
```

---

## 9. RELATIONSHIPS

```
orders (1) ←→ (1) shipments
shipments (1) ←→ (N) shipment_items
shipments (1) ←→ (N) shipment_tracking
```

---

## 10. INDEXES

```sql
-- shipments
CREATE INDEX idx_shipments_order ON shipments(order_id);
CREATE INDEX idx_shipments_tracking ON shipments(tracking_code);
CREATE INDEX idx_shipments_status ON shipments(status);
CREATE INDEX idx_shipments_created ON shipments(created_at);

-- shipment_tracking
CREATE INDEX idx_tracking_shipment ON shipment_tracking(shipment_id);
CREATE INDEX idx_tracking_time ON shipment_tracking(action_time);

-- shipping_fees
CREATE INDEX idx_fees_location ON shipping_fees(
    pick_province, pick_district, province, district
);
CREATE INDEX idx_fees_expires ON shipping_fees(expires_at);

-- ghtk_webhooks
CREATE INDEX idx_webhooks_label ON ghtk_webhooks(label_id);
CREATE INDEX idx_webhooks_processed ON ghtk_webhooks(processed);
```

---

## 11. SAMPLE DATA

```sql
-- Tạo shipment mẫu
INSERT INTO shipments (
    order_id, pick_name, pick_address, pick_province, pick_district, pick_tel,
    name, address, province, district, tel,
    value, total_weight, status
) VALUES (
    1, 'Shop ABC', '123 Nguyen Trai', 'Hà Nội', 'Thanh Xuân', '0901234567',
    'Nguyen Van A', '456 Le Loi', 'TP HCM', 'Quận 1', '0987654321',
    1500000, 2000, 'PENDING'
);
```

---

## KẾT LUẬN

Database được thiết kế để:
- ✓ Lưu trữ đầy đủ thông tin giao hàng
- ✓ Tích hợp với GHTK API
- ✓ Theo dõi trạng thái real-time
- ✓ Cache phí vận chuyển
- ✓ Xử lý webhook từ GHTK
- ✓ Lưu lịch sử tracking
