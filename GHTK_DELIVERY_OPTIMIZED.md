# THIẾT KẾ DATABASE DELIVERY TỐI ƯU HƠN

## VẤN ĐỀ

Bảng `shipments` hiện tại quá lớn (40+ columns) vì:
- Lưu thông tin người gửi (pick_*)
- Lưu thông tin người nhận (name, address, province...)
- Lưu thông tin GHTK (ghtk_*)
- Lưu phí vận chuyển

## GIẢI PHÁP: TÁCH THÀNH 3 BẢNG

---

## 1. BẢNG: shipments (CORE - 15 columns)

Chỉ lưu thông tin cốt lõi

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
    
    -- Thông tin hàng hóa
    total_weight INT NOT NULL,
    weight_option ENUM('gram', 'kilogram') DEFAULT 'gram',
    value DECIMAL(15,2) NOT NULL,
    
    -- Phí
    shipping_fee DECIMAL(15,2),
    insurance_fee DECIMAL(15,2) DEFAULT 0,
    
    -- Trạng thái
    status ENUM(
        'PENDING', 'PICKING', 'PICKED', 
        'STORING', 'DELIVERING', 'DELIVERED', 
        'CANCELLED', 'RETURNED'
    ) NOT NULL DEFAULT 'PENDING',
    
    -- Ghi chú
    note VARCHAR(500),
    
    -- Thời gian
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    picked_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id),
    UNIQUE KEY uk_order_shipment (order_id, shipment_number)
);
```

**Giải thích:**
- Bỏ thông tin địa chỉ → Lấy từ bảng `orders`
- Bỏ thông tin GHTK chi tiết → Chuyển sang bảng riêng
- Chỉ giữ thông tin cần thiết

---

## 2. BẢNG: shipment_ghtk_data (GHTK INFO - 10 columns)

Lưu response từ GHTK

```sql
CREATE TABLE shipment_ghtk_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shipment_id BIGINT NOT NULL UNIQUE,
    
    -- GHTK Response
    ghtk_order_id VARCHAR(100),
    ghtk_label VARCHAR(100),
    ghtk_partner_id VARCHAR(100),
    ghtk_status_id INT,
    ghtk_status_text VARCHAR(255),
    
    -- Request/Response
    request_payload TEXT,
    response_payload TEXT,
    
    -- Thời gian
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE
);
```

**Tại sao tách:**
- Không phải shipment nào cũng dùng GHTK
- Dễ thêm provider khác (VNPost, J&T...)
- Giữ bảng shipments gọn

---

## 3. BẢNG: shipment_addresses (ĐỊA CHỈ - 12 columns)

Lưu địa chỉ gửi/nhận

```sql
CREATE TABLE shipment_addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shipment_id BIGINT NOT NULL UNIQUE,
    
    -- Người gửi
    pick_name VARCHAR(255) NOT NULL,
    pick_address VARCHAR(500) NOT NULL,
    pick_province VARCHAR(100) NOT NULL,
    pick_district VARCHAR(100) NOT NULL,
    pick_ward VARCHAR(100),
    pick_tel VARCHAR(20) NOT NULL,
    
    -- Người nhận (copy từ order)
    receiver_name VARCHAR(255) NOT NULL,
    receiver_address VARCHAR(500) NOT NULL,
    receiver_province VARCHAR(100) NOT NULL,
    receiver_district VARCHAR(100) NOT NULL,
    receiver_ward VARCHAR(100),
    receiver_hamlet VARCHAR(100),
    receiver_tel VARCHAR(20) NOT NULL,
    receiver_email VARCHAR(255),
    
    FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE
);
```

**Tại sao tách:**
- Địa chỉ có thể lấy từ `orders`
- Chỉ lưu khi cần (snapshot tại thời điểm giao)
- Có thể NULL nếu dùng địa chỉ từ order

---

## HOẶC GIẢI PHÁP ĐỠN GIẢN HƠN: KHÔNG CẦN BẢNG RIÊNG

### Lấy địa chỉ trực tiếp từ bảng `orders`

```sql
CREATE TABLE shipments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    shipment_number INT NOT NULL DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    
    tracking_code VARCHAR(50) UNIQUE,
    label_id VARCHAR(100),
    
    -- Chỉ lưu thông tin hàng hóa
    total_weight INT NOT NULL,
    value DECIMAL(15,2) NOT NULL,
    
    -- Phí
    shipping_fee DECIMAL(15,2),
    
    -- Trạng thái
    status ENUM(...) NOT NULL DEFAULT 'PENDING',
    
    note VARCHAR(500),
    
    -- GHTK (chỉ lưu mã quan trọng)
    ghtk_label VARCHAR(100),
    ghtk_status_id INT,
    
    -- Thời gian
    created_at TIMESTAMP NOT NULL,
    delivered_at TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

**Lấy địa chỉ từ Order:**
```java
@Service
public class ShipmentService {
    
    public void createShipment(Order order) {
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        
        // Lấy địa chỉ từ order
        String receiverName = order.getCustomerName();
        String receiverAddress = order.getShippingAddress();
        String receiverPhone = order.getCustomerPhone();
        
        // Gửi đến GHTK
        GHTKRequest request = GHTKRequest.builder()
            .name(receiverName)
            .address(receiverAddress)
            .tel(receiverPhone)
            .build();
    }
}
```

---

## SO SÁNH 3 PHƯƠNG ÁN

### Phương án 1: Bảng lớn (40+ columns) ❌
**Ưu điểm:**
- Đơn giản, tất cả ở 1 chỗ

**Nhược điểm:**
- Quá nhiều columns
- Khó maintain
- Lãng phí storage

---

### Phương án 2: Tách 3 bảng (15 + 10 + 12 columns) ⚠️
**Ưu điểm:**
- Tách biệt rõ ràng
- Dễ mở rộng provider khác

**Nhược điểm:**
- Phức tạp hơn
- Nhiều JOIN

---

### Phương án 3: Bảng nhỏ + Lấy từ Order (15 columns) ✅ KHUYẾN NGHỊ
**Ưu điểm:**
- Gọn nhẹ nhất
- Không duplicate data
- Dễ maintain

**Nhược điểm:**
- Phải JOIN với orders để lấy địa chỉ
- Không snapshot địa chỉ (nếu order thay đổi)

---

## KHUYẾN NGHỊ: PHƯƠNG ÁN 3

```sql
CREATE TABLE shipments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    shipment_number INT NOT NULL DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Mã tracking
    tracking_code VARCHAR(50) UNIQUE,
    ghtk_label VARCHAR(100),
    
    -- Hàng hóa
    total_weight INT NOT NULL,
    value DECIMAL(15,2) NOT NULL,
    
    -- Phí
    shipping_fee DECIMAL(15,2),
    insurance_fee DECIMAL(15,2) DEFAULT 0,
    
    -- Trạng thái
    status ENUM(
        'PENDING', 'PICKING', 'PICKED', 
        'DELIVERING', 'DELIVERED', 
        'CANCELLED', 'RETURNED'
    ) NOT NULL DEFAULT 'PENDING',
    
    -- GHTK
    ghtk_status_id INT,
    ghtk_response TEXT,
    
    -- Ghi chú
    note VARCHAR(500),
    cancel_reason VARCHAR(500),
    
    -- Thời gian
    created_at TIMESTAMP NOT NULL,
    picked_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id),
    UNIQUE KEY uk_order_shipment (order_id, shipment_number)
);
```

**Chỉ còn 18 columns!**

---

## ENTITY CLASS TỐI ƯU

```java
@Entity
@Table(name = "shipments")
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
    
    @Column(name = "ghtk_label", length = 100)
    private String ghtkLabel;
    
    @Column(name = "total_weight", nullable = false)
    private Integer totalWeight;
    
    @Column(nullable = false)
    private Double value;
    
    @Column(name = "shipping_fee")
    private Double shippingFee;
    
    @Column(name = "insurance_fee")
    private Double insuranceFee = 0.0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.PENDING;
    
    @Column(name = "ghtk_status_id")
    private Integer ghtkStatusId;
    
    @Column(name = "ghtk_response", columnDefinition = "TEXT")
    private String ghtkResponse;
    
    private String note;
    
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "picked_at")
    private LocalDateTime pickedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL)
    private List<ShipmentItem> items;
    
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL)
    private List<ShipmentTracking> trackingHistory;
    
    // Helper methods
    @Transient
    public String getReceiverName() {
        return order.getCustomerName();
    }
    
    @Transient
    public String getReceiverAddress() {
        return order.getShippingAddress();
    }
    
    @Transient
    public String getReceiverPhone() {
        return order.getCustomerPhone();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

---

## KẾT LUẬN

**Giảm từ 40+ columns xuống còn 18 columns!**

**Cách làm:**
1. ✓ Lấy địa chỉ từ `orders` (không duplicate)
2. ✓ Chỉ lưu mã GHTK quan trọng
3. ✓ Lưu full response vào TEXT field
4. ✓ Bỏ các field ít dùng

**Lợi ích:**
- Gọn nhẹ, dễ đọc
- Không duplicate data
- Dễ maintain
- Performance tốt hơn
