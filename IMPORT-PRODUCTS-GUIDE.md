# 📦 Hướng dẫn Import Sản phẩm

## 🎯 Tổng quan

Hệ thống hỗ trợ import sản phẩm từ file CSV/Excel với đầy đủ thông số kỹ thuật dạng key-value.

## 📋 Format File Import

### Các cột bắt buộc:

| Cột | Mô tả | Ví dụ |
|-----|-------|-------|
| **SKU** | Mã sản phẩm (duy nhất) | `IP15PM-256-BLK` |
| **Tên sản phẩm** | Tên đầy đủ | `iPhone 15 Pro Max 256GB Đen Titan` |
| **Loại sản phẩm** | Danh mục | `Điện thoại` |
| **Giá bán** | Giá bán lẻ (VNĐ) | `32900000` |
| **Số lượng** | Tồn kho ban đầu | `15` |
| **Mô tả** | Mô tả ngắn gọn | `Flagship cao cấp nhất của Apple 2023` |
| **Thông số kỹ thuật (JSON)** | Thông số dạng JSON | Xem bên dưới |

### 🔧 Format Thông số kỹ thuật

Thông số kỹ thuật phải là chuỗi JSON hợp lệ với format:

```json
{
  "Key1": "Value1",
  "Key2": "Value2",
  "Key3": "Value3"
}
```

**Ví dụ cho Điện thoại:**
```json
{
  "Chip": "A17 Pro",
  "Camera chính": "48MP",
  "Màn hình": "6.7 inch Super Retina XDR",
  "Dung lượng": "256GB",
  "RAM": "8GB",
  "Pin": "4422 mAh",
  "Màu sắc": "Đen Titan",
  "Hệ điều hành": "iOS 17"
}
```

**Ví dụ cho Laptop:**
```json
{
  "CPU": "Intel Core i7-13700H",
  "RAM": "16GB DDR5",
  "Ổ cứng": "512GB NVMe SSD",
  "Màn hình": "15.6 inch FHD+ InfinityEdge",
  "Card đồ họa": "NVIDIA RTX 4050 6GB",
  "Pin": "Lên đến 10 giờ",
  "Màu sắc": "Bạc",
  "Hệ điều hành": "Windows 11 Pro"
}
```

**⚠️ Lưu ý quan trọng:**
- Trong CSV, JSON phải được bọc trong dấu ngoặc kép và escape dấu `"` thành `""`
- Ví dụ: `"{""Chip"":""A17 Pro"",""RAM"":""8GB""}"`

## 📝 Các bước Import

### 1. Chuẩn bị file CSV

Sử dụng file mẫu: `sample-import-products-with-specs.csv`

### 2. Mở bằng Excel

- Mở file CSV bằng Excel
- Chỉnh sửa dữ liệu theo nhu cầu
- **Không** thay đổi tên cột header

### 3. Nhập thông số kỹ thuật

**Cách 1: Nhập trực tiếp trong Excel**
```
"{""Chip"":""A17 Pro"",""RAM"":""8GB"",""Màn hình"":""6.7 inch""}"
```

**Cách 2: Sử dụng công cụ tạo JSON** (khuyến nghị)
- Tạo file JSON riêng
- Copy và paste vào cột "Thông số kỹ thuật"
- Nhớ escape dấu `"` thành `""`

### 4. Lưu file

- **File > Save As > CSV (Comma delimited) (*.csv)**
- Đảm bảo encoding là **UTF-8**

### 5. Import vào hệ thống

- Vào trang quản lý sản phẩm
- Chọn "Import từ Excel"
- Upload file CSV
- Kiểm tra preview
- Xác nhận import

## 🎨 Template Excel đẹp

Sử dụng file `template-import-products.html` để tạo template Excel với:
- Màu sắc phân biệt rõ ràng
- Dropdown cho loại sản phẩm
- Validation cho các trường
- Hướng dẫn ngay trong file

## 📱 Quét QR để nhập Serial

Sử dụng file `qr-scan-serial-input.html` để:
- Quét QR code/barcode sản phẩm
- Tự động lưu danh sách serial
- Xuất ra CSV để import vào hệ thống
- Kiểm tra trùng lặp tự động

### Cách sử dụng QR Scanner:

1. Mở file `qr-scan-serial-input.html` bằng trình duyệt
2. Cho phép truy cập camera
3. Quét mã QR/barcode trên sản phẩm
4. Hệ thống tự động lưu serial
5. Nhấn "Xuất Excel" để tải file CSV
6. Import file CSV vào hệ thống

## ✅ Checklist trước khi Import

- [ ] SKU không trùng lặp
- [ ] Giá bán > 0
- [ ] Số lượng >= 0
- [ ] Loại sản phẩm đã tồn tại trong hệ thống
- [ ] Thông số kỹ thuật là JSON hợp lệ
- [ ] File encoding UTF-8
- [ ] Đã kiểm tra preview trước khi import

## 🔍 Xử lý lỗi thường gặp

### Lỗi: "Invalid JSON format"
- Kiểm tra format JSON
- Đảm bảo escape dấu `"` thành `""`
- Sử dụng JSON validator online

### Lỗi: "Duplicate SKU"
- SKU đã tồn tại trong hệ thống
- Đổi SKU hoặc cập nhật sản phẩm cũ

### Lỗi: "Category not found"
- Loại sản phẩm chưa tồn tại
- Tạo loại sản phẩm trước khi import

### Lỗi: "Invalid price"
- Giá phải là số dương
- Không có ký tự đặc biệt

## 📊 Ví dụ hoàn chỉnh

```csv
SKU,Tên sản phẩm,Loại sản phẩm,Giá bán,Số lượng,Mô tả,Thông số kỹ thuật (JSON)
IP15PM-256-BLK,iPhone 15 Pro Max 256GB Đen Titan,Điện thoại,32900000,15,Flagship cao cấp nhất của Apple 2023,"{""Chip"":""A17 Pro"",""Camera chính"":""48MP"",""Màn hình"":""6.7 inch Super Retina XDR"",""Dung lượng"":""256GB"",""RAM"":""8GB"",""Pin"":""4422 mAh"",""Màu sắc"":""Đen Titan"",""Hệ điều hành"":""iOS 17""}"
```

## 🎯 Tips & Tricks

1. **Sử dụng Excel Formula** để tạo JSON tự động:
   ```excel
   ="{""Chip"":"""&A2&""",""RAM"":"""&B2&""",""Màn hình"":"""&C2&"""}"
   ```

2. **Copy từ JSON Formatter** online rồi replace `"` thành `""`

3. **Import từng batch nhỏ** (50-100 sản phẩm) để dễ kiểm soát

4. **Backup database** trước khi import số lượng lớn

5. **Test với 1-2 sản phẩm** trước khi import hàng loạt

## 📞 Hỗ trợ

Nếu gặp vấn đề, liên hệ:
- Email: support@example.com
- Hotline: 1900-xxxx
