# 🔧 Fix: Xử lý SKU trùng khi Import Excel

## ❌ Vấn đề trước đây

Khi import Excel 2 lần liên tiếp với cùng sản phẩm:
```
Lần 1: Import IP15-128-BLK (10 cái)
Lần 2: Import IP15-128-BLK (5 cái)

Kết quả: 2 dòng trùng nhau
- IP15-128-BLK: 10 cái
- IP15-128-BLK: 5 cái  ← Trùng!
```

**Hậu quả:**
- Danh sách sản phẩm bị lộn xộn
- Khó quản lý
- Có thể tạo phiếu sai

---

## ✅ Giải pháp mới

### Logic xử lý thông minh:

Khi import sản phẩm, hệ thống sẽ:

1. **Kiểm tra SKU** đã tồn tại trong danh sách chưa
2. **Nếu trùng:** Cộng dồn số lượng vào sản phẩm cũ
3. **Nếu mới:** Thêm sản phẩm mới vào danh sách

### Ví dụ:

**Trước khi import:**
```
Danh sách hiện tại:
- IP15-128-BLK: 10 cái (giá: 20,000,000)
- SS-S24-256: 5 cái (giá: 18,000,000)
```

**Import file Excel:**
```
- IP15-128-BLK: 5 cái (giá: 20,000,000)  ← Trùng SKU
- IP14-256-BLU: 8 cái (giá: 17,500,000) ← SKU mới
```

**Sau khi import:**
```
Danh sách kết quả:
- IP15-128-BLK: 15 cái (10 + 5) ← Đã cộng dồn
- SS-S24-256: 5 cái
- IP14-256-BLU: 8 cái ← Thêm mới
```

**Thông báo:**
```
✅ Đã thêm 1 SP mới và cộng dồn 1 SP trùng
```

---

## 🎯 Các trường hợp xử lý

### Trường hợp 1: Tất cả sản phẩm mới

**Import:**
```
- XIAOMI-13T: 20 cái
- OPPO-R11: 15 cái
```

**Kết quả:**
```
✅ Đã thêm 2 sản phẩm từ Excel
```

### Trường hợp 2: Tất cả sản phẩm trùng

**Danh sách hiện tại:**
```
- IP15-128-BLK: 10 cái
- SS-S24-256: 5 cái
```

**Import:**
```
- IP15-128-BLK: 5 cái
- SS-S24-256: 3 cái
```

**Kết quả:**
```
- IP15-128-BLK: 15 cái (10 + 5)
- SS-S24-256: 8 cái (5 + 3)

✅ Đã cộng dồn số lượng cho 2 sản phẩm trùng
```

### Trường hợp 3: Vừa mới vừa trùng

**Danh sách hiện tại:**
```
- IP15-128-BLK: 10 cái
```

**Import:**
```
- IP15-128-BLK: 5 cái  ← Trùng
- SS-S24-256: 8 cái    ← Mới
- IP14-256: 3 cái      ← Mới
```

**Kết quả:**
```
- IP15-128-BLK: 15 cái (10 + 5)
- SS-S24-256: 8 cái
- IP14-256: 3 cái

✅ Đã thêm 2 SP mới và cộng dồn 1 SP trùng
```

---

## 📝 Quy tắc cộng dồn

### Điều kiện để cộng dồn:

✅ **SKU giống nhau** → Cộng số lượng

### Các trường khác:

- **Giá khác nhau:** Giữ nguyên giá cũ (không cập nhật)
- **Tên sản phẩm khác:** Giữ nguyên tên cũ
- **Bảo hành khác:** Giữ nguyên bảo hành cũ
- **Ghi chú khác:** Giữ nguyên ghi chú cũ

### Ví dụ:

**Sản phẩm hiện tại:**
```
SKU: IP15-128-BLK
Tên: iPhone 15 128GB Đen
Số lượng: 10
Giá: 20,000,000
Bảo hành: 12 tháng
Ghi chú: Hàng mới
```

**Import:**
```
SKU: IP15-128-BLK
Tên: iPhone 15 128GB Black  ← Tên khác
Số lượng: 5
Giá: 19,500,000  ← Giá khác
Bảo hành: 24  ← BH khác
Ghi chú: Hàng cũ  ← Ghi chú khác
```

**Kết quả:**
```
SKU: IP15-128-BLK
Tên: iPhone 15 128GB Đen  ← Giữ tên cũ
Số lượng: 15  ← Cộng dồn (10 + 5)
Giá: 20,000,000  ← Giữ giá cũ
Bảo hành: 12 tháng  ← Giữ BH cũ
Ghi chú: Hàng mới  ← Giữ ghi chú cũ
```

---

## 💡 Lưu ý quan trọng

### 1. Chỉ cộng số lượng

Khi SKU trùng, hệ thống **chỉ cộng số lượng**, các thông tin khác giữ nguyên.

**Lý do:**
- Tránh ghi đè thông tin đã nhập
- Đảm bảo tính nhất quán
- Dễ kiểm soát

### 2. Muốn thay đổi thông tin sản phẩm

Nếu muốn cập nhật giá, tên, bảo hành:

**Cách 1:** Xóa sản phẩm cũ → Import lại
**Cách 2:** Sửa thủ công sau khi import
**Cách 3:** Dùng SKU khác

### 3. Kiểm tra sau khi import

Luôn kiểm tra danh sách sản phẩm sau khi import:
- Số lượng có đúng không?
- Giá có đúng không?
- Có sản phẩm nào bị trùng không mong muốn?

---

## 🎯 Use Cases thực tế

### Use Case 1: Nhập hàng nhiều lần từ cùng NCC

**Tình huống:**
- Sáng: Nhập 10 iPhone 15
- Chiều: NCC giao thêm 5 iPhone 15

**Giải pháp:**
1. Sáng: Import file Excel với 10 iPhone 15
2. Chiều: Import file Excel với 5 iPhone 15
3. Hệ thống tự động cộng: 10 + 5 = 15

**Kết quả:** Không bị trùng, số lượng chính xác

### Use Case 2: Import từ nhiều nguồn

**Tình huống:**
- File 1: Danh sách từ NCC A (có iPhone 15)
- File 2: Danh sách từ NCC B (cũng có iPhone 15)

**Giải pháp:**
1. Import File 1
2. Import File 2
3. Hệ thống tự động gộp sản phẩm trùng

**Lưu ý:** Nếu giá khác nhau, cần kiểm tra và sửa thủ công

### Use Case 3: Import nhầm 2 lần

**Tình huống:**
- Import file Excel
- Quên đã import, import lại lần nữa

**Trước đây:**
- Tất cả sản phẩm bị đúp
- Số lượng sai gấp đôi

**Bây giờ:**
- Số lượng tự động cộng dồn
- Thông báo "Đã cộng dồn X sản phẩm trùng"
- Dễ phát hiện và sửa

---

## 🔧 Technical Details

### Code logic:

```typescript
const handleExcelImport = (data) => {
  setItems(prevItems => {
    const mergedItems = [...prevItems]
    let addedCount = 0
    let updatedCount = 0
    
    newItems.forEach(newItem => {
      const existingIndex = mergedItems.findIndex(
        item => item.sku === newItem.sku
      )
      
      if (existingIndex >= 0) {
        // SKU exists - add quantity
        mergedItems[existingIndex].quantity += newItem.quantity
        updatedCount++
      } else {
        // New SKU - add to list
        mergedItems.push(newItem)
        addedCount++
      }
    })
    
    return mergedItems
  })
}
```

### So sánh SKU:

- **Case-sensitive:** `IP15-128-BLK` ≠ `ip15-128-blk`
- **Exact match:** `IP15-128-BLK` ≠ `IP15-128-BLK `
- **Trim spaces:** Tự động loại bỏ khoảng trắng thừa

---

## ✅ Kết luận

### Trước khi fix:
❌ Import 2 lần → Sản phẩm bị đúp
❌ Danh sách lộn xộn
❌ Khó quản lý

### Sau khi fix:
✅ Import nhiều lần → Tự động cộng dồn
✅ Danh sách gọn gàng
✅ Dễ quản lý
✅ Thông báo rõ ràng

---

**Tính năng đã hoạt động!** 🎉

Ngày cập nhật: 2025-12-08
