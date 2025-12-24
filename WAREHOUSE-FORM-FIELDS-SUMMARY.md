# Warehouse Form Fields Summary - Tổng hợp các trường trong form kho

## 1. Form Tạo Phiếu Nhập Kho

### A. Thông tin cơ bản
- ✅ **Mã phiếu nhập** (poCode) - Auto-generated
- ✅ **Người tạo** (createdBy) - Auto-fill từ user
- ✅ **Ghi chú** (note) - Textarea

### B. Thông tin Nhà cung cấp (2 options)

#### Option 1: Chọn NCC có sẵn
- ✅ **Dropdown chọn NCC** - Hiển thị: Tên - Mã số thuế

#### Option 2: Tạo NCC mới (8 trường)
1. ✅ **Tên NCC** (name) * Required
2. ✅ **Mã số thuế** (taxCode) * Required  
3. ✅ **Người liên hệ** (contactName)
4. ✅ **Số điện thoại** (phone)
5. ✅ **Email** (email)
6. ✅ **Địa chỉ** (address)
7. ✅ **Tài khoản ngân hàng** (bankAccount)
8. ✅ **Điều khoản thanh toán** (paymentTerm)
9. ⚠️ **Số ngày nợ** (paymentTermDays) - THIẾU trong form hiện tại!

### C. Danh sách sản phẩm (7 trường mỗi sản phẩm)
1. ✅ **SKU** (sku) * Required
2. ✅ **Tên sản phẩm** (internalName) * Required
3. ✅ **Số lượng** (quantity) * Required
4. ✅ **Đơn giá** (unitCost) * Required
5. ✅ **Bảo hành (tháng)** (warrantyMonths)
6. ✅ **Thành tiền** (calculated) - Display only
7. ✅ **Thông số kỹ thuật JSON** (techSpecsJson) - Textarea
8. ✅ **Ghi chú** (note)

## 2. Vấn đề cần sửa

### Vấn đề 1: Thiếu trường `paymentTermDays` trong form NCC mới
**Hiện tại:** Form chỉ có 6 trường cho NCC mới
**Cần có:** 9 trường (thêm paymentTermDays và active)

### Vấn đề 2: techSpecsJson có thể không được lưu
**Nguyên nhân:** Backend có thể không xử lý đúng JSON string
**Giải pháp:** Cần validate JSON format trước khi submit

### Vấn đề 3: Form có thể quá dài
**Hiện tại:** Tất cả trường đều có nhưng cần scroll xuống
**Giải pháp:** Đã có, chỉ cần scroll

## 3. Backend DTO Requirements

### POItemRequest (7 fields)
```java
@NotBlank String sku;
@NotNull @Positive Long quantity;
String internalName;
String techSpecsJson;  // ← Cần validate JSON
@NotNull @Positive Double unitCost;
@PositiveOrZero Integer warrantyMonths;
String note;
```

### CreateSupplierRequest (10 fields)
```java
String name;
String contactName;
String phone;
String email;
String address;
String taxCode;
String bankAccount;
String paymentTerm;
Integer paymentTermDays;  // ← THIẾU trong form!
Boolean active = true;     // ← THIẾU trong form!
```

## 4. Cần sửa ngay

### Sửa form NCC mới - Thêm 2 trường:

```typescript
// Thêm vào newSupplier state
const [newSupplier, setNewSupplier] = useState({
  name: '',
  taxCode: '',
  contactName: '',
  phone: '',
  email: '',
  address: '',
  bankAccount: '',
  paymentTerm: '',
  paymentTermDays: 30,  // ← THÊM
  active: true           // ← THÊM
})
```

### Thêm UI cho 2 trường mới:

```tsx
<div>
  <label>Số ngày nợ</label>
  <input
    type="number"
    value={newSupplier.paymentTermDays}
    onChange={(e) => setNewSupplier({ 
      ...newSupplier, 
      paymentTermDays: parseInt(e.target.value) || 30 
    })}
    min="0"
  />
</div>

<div>
  <label>Trạng thái</label>
  <select
    value={newSupplier.active ? 'true' : 'false'}
    onChange={(e) => setNewSupplier({ 
      ...newSupplier, 
      active: e.target.value === 'true' 
    })}
  >
    <option value="true">Hoạt động</option>
    <option value="false">Ngừng hoạt động</option>
  </select>
</div>
```

## 5. Validate techSpecsJson

### Thêm validation trước khi submit:

```typescript
// Validate JSON format
for (let i = 0; i < items.length; i++) {
  const item = items[i]
  if (item.techSpecsJson) {
    try {
      JSON.parse(item.techSpecsJson)
    } catch (error) {
      toast.error(`Sản phẩm ${i + 1}: Thông số kỹ thuật không đúng định dạng JSON`)
      return
    }
  }
}
```

## 6. Kiểm tra Database

### Kiểm tra xem techSpecsJson có được lưu không:

```sql
-- Kiểm tra warehouse_products table
SELECT id, sku, internal_name, tech_specs_json 
FROM warehouse_products 
WHERE tech_specs_json IS NOT NULL;

-- Kiểm tra purchase_order_items table  
SELECT id, sku, tech_specs_json
FROM purchase_order_items
WHERE tech_specs_json IS NOT NULL;
```

### Nếu column không tồn tại, cần thêm:

```sql
-- Thêm column nếu chưa có
ALTER TABLE warehouse_products 
ADD COLUMN IF NOT EXISTS tech_specs_json TEXT;

ALTER TABLE purchase_order_items
ADD COLUMN IF NOT EXISTS tech_specs_json TEXT;
```

## 7. Files cần sửa

### Frontend
1. ✅ `src/frontend/app/admin/warehouse/import/create/page.tsx`
2. ✅ `src/frontend/app/employee/warehouse/import/create/page.tsx`

### Backend (nếu cần)
1. ⚠️ Kiểm tra `InventoryServiceImpl.java` - method `createPurchaseOrder`
2. ⚠️ Kiểm tra entity `WarehouseProduct.java` - có field `techSpecsJson`?
3. ⚠️ Kiểm tra entity `PurchaseOrderItem.java` - có field `techSpecsJson`?

## 8. Test Cases

### Test 1: Tạo NCC mới với đầy đủ trường
- Nhập tất cả 9 trường
- Submit
- ✅ NCC được tạo với paymentTermDays và active

### Test 2: Nhập sản phẩm với techSpecsJson
- Nhập JSON: `{"cpu":"i7","ram":"16GB"}`
- Submit
- ✅ Check database: tech_specs_json có giá trị

### Test 3: Nhập JSON sai format
- Nhập: `{cpu:i7}` (thiếu quotes)
- Submit
- ✅ Hiển thị lỗi: "Thông số kỹ thuật không đúng định dạng JSON"

## 9. Kết luận

**Các trường đã có trong form:**
- ✅ 7/7 trường cho sản phẩm (bao gồm techSpecsJson)
- ✅ 7/9 trường cho NCC mới

**Cần thêm:**
- ❌ paymentTermDays (số ngày nợ)
- ❌ active (trạng thái NCC)

**Cần kiểm tra:**
- ⚠️ Database có column tech_specs_json?
- ⚠️ Backend có lưu techSpecsJson?
- ⚠️ JSON validation trước khi submit
