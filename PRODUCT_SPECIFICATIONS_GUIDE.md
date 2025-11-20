# 📋 Hướng dẫn Thông số sản phẩm

## 🎯 Mục đích
Hiển thị đầy đủ thông số kỹ thuật sản phẩm công nghệ để khách hàng có thể so sánh và đưa ra quyết định mua hàng.

## 📱 Các loại thông số theo danh mục

### 1. Điện thoại & Smartphone
```json
{
  "Màn hình": {
    "Kích thước": "6.7 inch",
    "Công nghệ": "AMOLED",
    "Độ phân giải": "1284 x 2778 pixels",
    "Tần số quét": "120Hz"
  },
  "Hiệu năng": {
    "Chip": "Apple A17 Pro",
    "CPU": "6 nhân",
    "GPU": "6 nhân",
    "RAM": "8GB"
  },
  "Bộ nhớ": {
    "Dung lượng": "256GB",
    "Hỗ trợ thẻ nhớ": "Không"
  },
  "Camera": {
    "Camera sau": "48MP + 12MP + 12MP",
    "Camera trước": "12MP",
    "Quay video": "4K@60fps",
    "Tính năng": "Night mode, Portrait, Cinematic mode"
  },
  "Pin & Sạc": {
    "Dung lượng": "4422 mAh",
    "Sạc nhanh": "20W",
    "Sạc không dây": "15W MagSafe"
  },
  "Kết nối": {
    "Mạng": "5G",
    "SIM": "Nano-SIM và eSIM",
    "WiFi": "WiFi 6E",
    "Bluetooth": "5.3",
    "Cổng sạc": "USB-C"
  },
  "Thiết kế": {
    "Kích thước": "160.9 x 77.8 x 8.25 mm",
    "Trọng lượng": "221g",
    "Chất liệu": "Khung Titanium, mặt kính Ceramic Shield",
    "Màu sắc": "Titan Tự nhiên, Titan Xanh, Titan Trắng, Titan Đen"
  },
  "Hệ điều hành": "iOS 17",
  "Tiện ích": "Face ID, Chống nước IP68, NFC, Apple Pay"
}
```

### 2. Laptop
```json
{
  "Bộ xử lý": {
    "CPU": "Intel Core i7-13700H",
    "Số nhân": "14 nhân (6P + 8E)",
    "Tốc độ": "Turbo boost 5.0GHz",
    "Cache": "24MB"
  },
  "RAM": {
    "Dung lượng": "16GB",
    "Loại": "DDR5",
    "Tốc độ": "4800MHz",
    "Tối đa": "32GB"
  },
  "Ổ cứng": {
    "Loại": "SSD NVMe PCIe 4.0",
    "Dung lượng": "512GB",
    "Khả năng nâng cấp": "Có (1 slot M.2)"
  },
  "Card đồ họa": {
    "GPU": "NVIDIA GeForce RTX 4060",
    "VRAM": "8GB GDDR6",
    "TGP": "140W"
  },
  "Màn hình": {
    "Kích thước": "15.6 inch",
    "Độ phân giải": "2560 x 1440 (2K)",
    "Tần số quét": "165Hz",
    "Độ sáng": "300 nits",
    "Tấm nền": "IPS"
  },
  "Pin": {
    "Dung lượng": "90Wh",
    "Thời gian sử dụng": "6-8 giờ",
    "Sạc": "230W"
  },
  "Kết nối": {
    "WiFi": "WiFi 6E (802.11ax)",
    "Bluetooth": "5.2",
    "Cổng": "3x USB-A 3.2, 1x USB-C Thunderbolt 4, HDMI 2.1, RJ45, Audio Jack"
  },
  "Thiết kế": {
    "Kích thước": "359 x 259 x 22.9 mm",
    "Trọng lượng": "2.3 kg",
    "Chất liệu": "Nhựa + Kim loại"
  },
  "Bàn phím": "RGB per-key, Numpad",
  "Hệ điều hành": "Windows 11 Home",
  "Bảo hành": "24 tháng"
}
```

### 3. Tai nghe
```json
{
  "Loại": "True Wireless",
  "Driver": "10mm Dynamic",
  "Kết nối": {
    "Bluetooth": "5.3",
    "Codec": "AAC, SBC, LDAC",
    "Phạm vi": "10m"
  },
  "Pin": {
    "Tai nghe": "6 giờ",
    "Case sạc": "24 giờ",
    "Sạc nhanh": "10 phút = 1 giờ"
  },
  "Tính năng": {
    "Chống ồn": "ANC -35dB",
    "Chống nước": "IPX4",
    "Điều khiển": "Cảm ứng",
    "Mic": "4 mic với AI"
  },
  "Trọng lượng": "4.5g/tai",
  "Màu sắc": "Đen, Trắng, Xanh"
}
```

### 4. Sạc dự phòng
```json
{
  "Dung lượng": "20000mAh",
  "Công suất đầu vào": "18W (USB-C)",
  "Công suất đầu ra": {
    "USB-C": "20W PD",
    "USB-A": "18W QC 3.0"
  },
  "Số cổng": "2 (1 USB-C + 1 USB-A)",
  "Sạc nhanh": "PD 3.0, QC 3.0",
  "Kích thước": "150 x 70 x 28 mm",
  "Trọng lượng": "420g",
  "Chất liệu": "Nhựa ABS",
  "Màu sắc": "Đen, Trắng"
}
```

## 🎨 Cách hiển thị trên UI

### Trang chi tiết sản phẩm:
```
┌─────────────────────────────────────────┐
│ [Hình ảnh]  │  Tên sản phẩm             │
│             │  ⭐⭐⭐⭐⭐ (123 đánh giá)  │
│             │  💰 15.990.000đ           │
│             │  [Thêm vào giỏ]           │
├─────────────────────────────────────────┤
│ 📋 Thông số kỹ thuật                    │
│                                         │
│ 📱 Màn hình                             │
│   • Kích thước: 6.7 inch               │
│   • Công nghệ: AMOLED                  │
│   • Độ phân giải: 1284 x 2778 pixels  │
│                                         │
│ ⚡ Hiệu năng                            │
│   • Chip: Apple A17 Pro                │
│   • RAM: 8GB                           │
│   • Bộ nhớ: 256GB                      │
│                                         │
│ 📷 Camera                               │
│   • Camera sau: 48MP + 12MP + 12MP     │
│   • Camera trước: 12MP                 │
│                                         │
│ 🔋 Pin & Sạc                            │
│   • Dung lượng: 4422 mAh               │
│   • Sạc nhanh: 20W                     │
└─────────────────────────────────────────┘
```

### Trang so sánh sản phẩm:
```
┌──────────────┬──────────────┬──────────────┐
│ Thông số     │ iPhone 15 Pro│ Samsung S24  │
├──────────────┼──────────────┼──────────────┤
│ Màn hình     │ 6.7" AMOLED │ 6.8" AMOLED  │
│ Chip         │ A17 Pro      │ Snapdragon 8 │
│ RAM          │ 8GB          │ 12GB         │
│ Camera       │ 48MP         │ 200MP        │
│ Pin          │ 4422 mAh     │ 5000 mAh     │
│ Giá          │ 29.990.000đ  │ 27.990.000đ  │
└──────────────┴──────────────┴──────────────┘
```

## 💡 Gợi ý triển khai

### 1. Cấu trúc dữ liệu
Sử dụng `ProductSpecification` đã có:
- `specKey`: Tên nhóm (VD: "Màn hình", "Camera")
- `specValue`: JSON chứa các thông số chi tiết

### 2. Component hiển thị
- `ProductSpecsCard`: Hiển thị thông số dạng card
- `ProductSpecsTable`: Hiển thị dạng bảng
- `ProductCompare`: So sánh nhiều sản phẩm

### 3. Tính năng nâng cao
- ✅ Lọc sản phẩm theo thông số (RAM, Chip, Camera...)
- ✅ So sánh sản phẩm
- ✅ Highlight điểm nổi bật
- ✅ Tìm kiếm theo thông số

## 📝 Checklist triển khai

- [ ] Thêm form nhập thông số khi đăng bán sản phẩm
- [ ] Component hiển thị thông số trên trang chi tiết
- [ ] Tính năng lọc theo thông số
- [ ] Tính năng so sánh sản phẩm
- [ ] Highlight thông số nổi bật
- [ ] Responsive design cho mobile
