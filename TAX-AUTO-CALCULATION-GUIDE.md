# Hướng Dẫn Tính Thuế Tự Động

## Tính Năng Mới

Hệ thống giờ có thể **tự động tính doanh thu chịu thuế** từ dữ liệu thực tế của cửa hàng và tạo báo cáo thuế tự động.

## API Endpoints

### 1. Tính Toán Doanh Thu Chịu Thuế

**Endpoint:** `GET /api/accounting/tax/calculate-revenue`

**Parameters:**
- `periodStart`: Ngày bắt đầu (YYYY-MM-DD)
- `periodEnd`: Ngày kết thúc (YYYY-MM-DD)

**Response:**
```json
{
  "success": true,
  "message": "Tính toán doanh thu chịu thuế thành công",
  "data": {
    "periodStart": "2025-12-01",
    "periodEnd": "2025-12-31",
    "totalRevenue": 100000000,
    "totalExpense": 50000000,
    "profit": 50000000,
    "vatTaxableRevenue": 100000000,
    "corporateTaxableRevenue": 50000000,
    "estimatedVAT": 10000000,
    "estimatedCorporateTax": 10000000
  }
}
```

**Giải thích:**
- `totalRevenue`: Tổng doanh thu từ financial_transactions (REVENUE)
- `totalExpense`: Tổng chi phí từ financial_transactions (EXPENSE)
- `profit`: Lợi nhuận = Doanh thu - Chi phí
- `vatTaxableRevenue`: Doanh thu chịu thuế VAT (= totalRevenue)
- `corporateTaxableRevenue`: Lợi nhuận chịu thuế TNDN (= profit)
- `estimatedVAT`: Thuế VAT ước tính (10% của doanh thu)
- `estimatedCorporateTax`: Thuế TNDN ước tính (20% của lợi nhuận)

### 2. Tạo Báo Cáo Thuế Tự Động

**Endpoint:** `POST /api/accounting/tax/auto-create`

**Parameters:**
- `periodStart`: Ngày bắt đầu (YYYY-MM-DD)
- `periodEnd`: Ngày kết thúc (YYYY-MM-DD)
- `taxType`: Loại thuế (VAT hoặc CORPORATE_TAX)

**Response:**
```json
{
  "success": true,
  "message": "Tạo báo cáo thuế tự động thành công",
  "data": {
    "id": 1,
    "reportCode": "VAT-122025",
    "taxType": "VAT",
    "periodStart": "2025-12-01",
    "periodEnd": "2025-12-31",
    "taxableRevenue": 100000000,
    "taxRate": 10,
    "taxAmount": 10000000,
    "paidAmount": 0,
    "remainingTax": 10000000,
    "status": "DRAFT"
  }
}
```

## Cách Hoạt Động

### Thuế VAT (10%)
- **Doanh thu chịu thuế**: Tổng doanh thu từ bán hàng
- **Công thức**: `Thuế VAT = Doanh thu × 10%`
- **Nguồn dữ liệu**: `financial_transactions` với `type = REVENUE`

### Thuế TNDN (20%)
- **Doanh thu chịu thuế**: Lợi nhuận (Doanh thu - Chi phí)
- **Công thức**: `Thuế TNDN = Lợi nhuận × 20%`
- **Nguồn dữ liệu**: 
  - Doanh thu: `financial_transactions` với `type = REVENUE`
  - Chi phí: `financial_transactions` với `type = EXPENSE`

## Test API

### Test 1: Tính toán doanh thu tháng 12/2025
```http
GET http://localhost:8080/api/accounting/tax/calculate-revenue?periodStart=2025-12-01&periodEnd=2025-12-31
Authorization: Bearer {{token}}
```

### Test 2: Tạo báo cáo VAT tự động
```http
POST http://localhost:8080/api/accounting/tax/auto-create?periodStart=2025-12-01&periodEnd=2025-12-31&taxType=VAT
Authorization: Bearer {{token}}
```

### Test 3: Tạo báo cáo Thuế TNDN tự động
```http
POST http://localhost:8080/api/accounting/tax/auto-create?periodStart=2025-12-01&periodEnd=2025-12-31&taxType=CORPORATE_TAX
Authorization: Bearer {{token}}
```

## Tích Hợp Frontend

### 1. Thêm nút "Tính toán tự động" trong modal tạo báo cáo

```typescript
const calculateRevenue = async () => {
  try {
    const response = await fetch(
      `http://localhost:8080/api/accounting/tax/calculate-revenue?periodStart=${form.periodStart}&periodEnd=${form.periodEnd}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    )
    
    const result = await response.json()
    if (result.success) {
      const data = result.data
      
      // Tự động điền doanh thu chịu thuế
      if (form.taxType === 'VAT') {
        setForm({
          ...form,
          taxableRevenue: data.vatTaxableRevenue
        })
      } else {
        setForm({
          ...form,
          taxableRevenue: data.corporateTaxableRevenue
        })
      }
      
      toast.success('Đã tính toán doanh thu chịu thuế')
    }
  } catch (error) {
    toast.error('Lỗi khi tính toán')
  }
}
```

### 2. Thêm nút "Tạo tự động" trong trang Quản lý thuế

```typescript
const autoCreateReport = async (taxType: 'VAT' | 'CORPORATE_TAX') => {
  try {
    const response = await fetch(
      `http://localhost:8080/api/accounting/tax/auto-create?periodStart=${periodStart}&periodEnd=${periodEnd}&taxType=${taxType}`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    )
    
    const result = await response.json()
    if (result.success) {
      toast.success('Tạo báo cáo tự động thành công')
      loadTaxReports()
    }
  } catch (error) {
    toast.error('Lỗi khi tạo báo cáo')
  }
}
```

## Lưu Ý

1. **Dữ liệu nguồn**: Hệ thống lấy dữ liệu từ bảng `financial_transactions`
2. **Kỳ báo cáo**: Nên tạo báo cáo theo tháng hoặc quý
3. **Kiểm tra trùng**: Hệ thống tự động kiểm tra và không cho tạo báo cáo trùng
4. **Trạng thái**: Báo cáo mới tạo sẽ ở trạng thái DRAFT
5. **Quy trình**: DRAFT → SUBMITTED → PAID

## Ví Dụ Sử Dụng

### Tạo báo cáo thuế tháng 12/2025

1. **Bước 1**: Tính toán doanh thu
```
GET /api/accounting/tax/calculate-revenue?periodStart=2025-12-01&periodEnd=2025-12-31
```

2. **Bước 2**: Xem kết quả
```json
{
  "totalRevenue": 100000000,
  "profit": 50000000,
  "estimatedVAT": 10000000,
  "estimatedCorporateTax": 10000000
}
```

3. **Bước 3**: Tạo báo cáo VAT tự động
```
POST /api/accounting/tax/auto-create?periodStart=2025-12-01&periodEnd=2025-12-31&taxType=VAT
```

4. **Bước 4**: Tạo báo cáo Thuế TNDN tự động
```
POST /api/accounting/tax/auto-create?periodStart=2025-12-01&periodEnd=2025-12-31&taxType=CORPORATE_TAX
```

5. **Bước 5**: Kiểm tra và nộp báo cáo
- Xem lại báo cáo trong trang Quản lý thuế
- Click "Nộp báo cáo" khi đã kiểm tra
- Click "Đánh dấu đã thanh toán" sau khi nộp thuế

## Troubleshooting

### Lỗi: "Không có doanh thu/lợi nhuận trong kỳ này"
- **Nguyên nhân**: Không có giao dịch trong kỳ báo cáo
- **Giải pháp**: Kiểm tra dữ liệu trong bảng `financial_transactions`

### Lỗi: "Báo cáo thuế cho kỳ này đã tồn tại"
- **Nguyên nhân**: Đã có báo cáo với cùng mã
- **Giải pháp**: Sửa báo cáo hiện có hoặc xóa báo cáo cũ

### Doanh thu tính toán không đúng
- **Kiểm tra**: Xem dữ liệu trong `financial_transactions`
- **Đảm bảo**: 
  - Giao dịch REVENUE được ghi nhận đúng
  - Giao dịch EXPENSE được ghi nhận đúng
  - Thời gian giao dịch nằm trong kỳ báo cáo

## Summary

✅ **Tính năng đã thêm:**
1. API tính toán doanh thu chịu thuế tự động
2. API tạo báo cáo thuế tự động
3. Tích hợp với dữ liệu thực tế từ financial_transactions
4. Tự động tính VAT (10%) và Thuế TNDN (20%)

✅ **Lợi ích:**
- Tiết kiệm thời gian nhập liệu
- Giảm sai sót do nhập tay
- Dữ liệu chính xác từ hệ thống
- Tự động cập nhật theo doanh thu thực tế

**Restart backend để áp dụng các thay đổi!**
