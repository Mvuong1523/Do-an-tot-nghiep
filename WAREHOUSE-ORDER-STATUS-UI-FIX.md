# Fix UI Trạng Thái Đơn Hàng Sau Xuất Kho

## Vấn đề
Sau khi xuất kho thành công:
1. ❌ Vẫn còn nút "Tạo phiếu xuất kho" (không hợp lý)
2. ❌ Trạng thái hiển thị "CONFIRMED" thay vì "Đã chuẩn bị hàng, đợi tài xế đến lấy"

## Giải pháp

### 1. Ẩn nút "Tạo phiếu xuất kho" sau khi đã xuất ✅

**Logic:**
```typescript
{!order.ghnOrderCode && order.status === 'CONFIRMED' ? (
  // Chưa xuất kho - Hiển thị nút
  <button>📦 Tạo phiếu xuất kho</button>
) : (
  // Đã xuất kho - Hiển thị thông báo
  <div>
    ✅ Đã xuất kho thành công
    🚚 Đợi tài xế đến lấy hàng
  </div>
)}
```

### 2. Hiển thị trạng thái rõ ràng ✅

**Trước khi xuất kho:**
```
┌─────────────────────────────┐
│ Thao tác                    │
├─────────────────────────────┤
│ [📦 Tạo phiếu xuất kho]    │
│ [🖨️ In phiếu chuẩn bị]     │
└─────────────────────────────┘

┌─────────────────────────────┐
│ 📊 Tiến trình xử lý         │
├─────────────────────────────┤
│ ✓ Đã giữ hàng              │
│ ○ Chưa xuất kho            │
└─────────────────────────────┘
```

**Sau khi xuất kho:**
```
┌─────────────────────────────┐
│ Thao tác                    │
├─────────────────────────────┤
│ ✅ Đã xuất kho thành công   │
│ Hàng đã chuẩn bị xong       │
│                             │
│ Trạng thái:                 │
│ 🚚 Đợi tài xế đến lấy hàng  │
│                             │
│ Mã vận đơn GHN:             │
│ GHN123456789                │
└─────────────────────────────┘

┌─────────────────────────────┐
│ 📊 Tiến trình xử lý         │
├─────────────────────────────┤
│ ✓ Đã giữ hàng              │
│ ✓ Đã xuất kho              │
│ 🚚 Đợi tài xế đến lấy hàng  │ ← Highlight
└─────────────────────────────┘
```

## Chi tiết thay đổi

### File: `src/frontend/app/warehouse/orders/[id]/page.tsx`

#### 1. Actions Card - Conditional Rendering

**Trước:**
```tsx
<div className="bg-white rounded-lg shadow p-6">
  <h2>Thao tác</h2>
  <button onClick={handleCreateExportOrder}>
    📦 Tạo phiếu xuất kho
  </button>
  <button onClick={() => window.print()}>
    🖨️ In phiếu chuẩn bị
  </button>
</div>
```

**Sau:**
```tsx
<div className="bg-white rounded-lg shadow p-6">
  <h2>Thao tác</h2>
  {!order.ghnOrderCode && order.status === 'CONFIRMED' ? (
    // Chưa xuất - Hiển thị nút
    <>
      <button onClick={handleCreateExportOrder}>
        📦 Tạo phiếu xuất kho
      </button>
      <button onClick={() => window.print()}>
        🖨️ In phiếu chuẩn bị
      </button>
    </>
  ) : (
    // Đã xuất - Hiển thị thông báo
    <div className="bg-green-50 border-2 border-green-200 rounded-lg p-4">
      <div className="flex items-center gap-3 mb-3">
        <span className="text-3xl">✅</span>
        <div>
          <p className="font-bold text-green-900">Đã xuất kho thành công</p>
          <p className="text-sm text-green-700">Hàng đã chuẩn bị xong</p>
        </div>
      </div>
      <div className="bg-white rounded p-3 mb-3">
        <p className="text-xs text-gray-600 mb-1">Trạng thái</p>
        <p className="font-semibold text-orange-600">
          🚚 Đợi tài xế đến lấy hàng
        </p>
      </div>
      {order.ghnOrderCode && (
        <div className="bg-white rounded p-3">
          <p className="text-xs text-gray-600 mb-1">Mã vận đơn GHN</p>
          <p className="font-bold text-green-600">{order.ghnOrderCode}</p>
        </div>
      )}
    </div>
  )}
</div>
```

#### 2. Status Timeline - Hiển thị tiến trình

**Trước:**
```tsx
<div className="bg-blue-50 rounded-lg p-4">
  <h3>📊 Trạng thái xuất kho</h3>
  <div>✓ Đã giữ hàng</div>
  <div>○ Chưa xuất kho</div>
  <div>○ Chưa tạo đơn GHN</div>
</div>
```

**Sau:**
```tsx
<div className={order.ghnOrderCode 
  ? "bg-green-50 rounded-lg p-4 border-2 border-green-200" 
  : "bg-blue-50 rounded-lg p-4"}>
  <h3>📊 Tiến trình xử lý</h3>
  
  {/* Bước 1: Đã giữ hàng */}
  <div className="border-l-4 border-green-500">
    <span>✓</span>
    <div>
      <p>Đã giữ hàng</p>
      <p className="text-xs">Hàng đã được reserve</p>
    </div>
  </div>
  
  {/* Bước 2: Xuất kho */}
  <div className={order.ghnOrderCode 
    ? "border-l-4 border-green-500" 
    : "border-l-4 border-gray-300"}>
    <span>{order.ghnOrderCode ? "✓" : "○"}</span>
    <div>
      <p>{order.ghnOrderCode ? "Đã xuất kho" : "Chưa xuất kho"}</p>
      <p className="text-xs">
        {order.ghnOrderCode ? "Hàng đã chuẩn bị xong" : "Đang chờ xuất kho"}
      </p>
    </div>
  </div>
  
  {/* Bước 3: Đợi tài xế (chỉ hiển thị khi đã xuất) */}
  {order.ghnOrderCode && (
    <div className="bg-orange-50 border-l-4 border-orange-500">
      <span>🚚</span>
      <div>
        <p className="font-bold text-orange-900">Đợi tài xế đến lấy hàng</p>
        <p className="text-xs text-orange-700">Đơn GHN đã được tạo</p>
      </div>
    </div>
  )}
</div>
```

## Màu sắc & Styling

### Trước khi xuất kho:
- Background: `bg-blue-50` (xanh nhạt)
- Border: Không có
- Icon: `○` (chưa hoàn thành)

### Sau khi xuất kho:
- Background: `bg-green-50` (xanh lá nhạt)
- Border: `border-2 border-green-200` (viền xanh)
- Icon: `✓` (hoàn thành)
- Highlight: `bg-orange-50` với `border-orange-500` cho trạng thái "Đợi tài xế"

## Điều kiện hiển thị

### Hiển thị nút "Tạo phiếu xuất kho":
```typescript
!order.ghnOrderCode && order.status === 'CONFIRMED'
```

### Hiển thị "Đã xuất kho":
```typescript
order.ghnOrderCode || order.status === 'SHIPPING'
```

### Hiển thị "Đợi tài xế":
```typescript
order.ghnOrderCode && order.status === 'SHIPPING'
```

## User Experience

### Nhân viên kho thấy:

**Khi vào trang chi tiết đơn chưa xuất:**
1. Thấy nút "Tạo phiếu xuất kho" rõ ràng
2. Thấy trạng thái "Chưa xuất kho"
3. Có thể in phiếu chuẩn bị

**Sau khi click "Tạo phiếu xuất kho":**
1. Modal hiện ra để nhập serial
2. Nhập xong → Click "Xác nhận xuất kho"
3. Toast: "Xuất kho thành công! Đơn GHN đã được tạo tự động"
4. Modal đóng, trang refresh

**Sau khi refresh:**
1. ✅ Nút "Tạo phiếu xuất kho" biến mất
2. ✅ Hiển thị "Đã xuất kho thành công"
3. ✅ Hiển thị "🚚 Đợi tài xế đến lấy hàng"
4. ✅ Hiển thị mã vận đơn GHN
5. ✅ Timeline chuyển sang màu xanh

## Testing

### Test Case 1: Đơn hàng chưa xuất kho
```
1. Vào /warehouse/orders
2. Click vào đơn hàng có status = CONFIRMED
3. Kiểm tra:
   ✓ Có nút "Tạo phiếu xuất kho"
   ✓ Có nút "In phiếu chuẩn bị"
   ✓ Timeline hiển thị "Chưa xuất kho"
   ✓ Background màu xanh nhạt
```

### Test Case 2: Xuất kho thành công
```
1. Click "Tạo phiếu xuất kho"
2. Nhập serial numbers
3. Click "Xác nhận xuất kho"
4. Đợi API response
5. Kiểm tra:
   ✓ Toast "Xuất kho thành công"
   ✓ Modal đóng
   ✓ Trang refresh
```

### Test Case 3: Đơn hàng đã xuất kho
```
1. Refresh trang hoặc vào lại đơn hàng
2. Kiểm tra:
   ✓ KHÔNG còn nút "Tạo phiếu xuất kho"
   ✓ Hiển thị "Đã xuất kho thành công"
   ✓ Hiển thị "Đợi tài xế đến lấy hàng"
   ✓ Hiển thị mã vận đơn GHN
   ✓ Timeline có 3 bước, bước 3 highlight màu cam
   ✓ Background màu xanh lá nhạt với viền
```

## Lưu ý

1. **Không thể xuất lại**: Sau khi xuất kho, không thể xuất lại (nút biến mất)
2. **GHN Order Code**: Là điều kiện chính để xác định đã xuất kho
3. **Status SHIPPING**: Backend đã cập nhật, frontend hiển thị tương ứng
4. **Responsive**: UI responsive trên mobile

## Files đã thay đổi

1. ✅ `src/frontend/app/warehouse/orders/[id]/page.tsx`
   - Thêm conditional rendering cho Actions card
   - Cải thiện Status Timeline
   - Thêm highlight cho trạng thái "Đợi tài xế"

## Screenshots (Mô tả)

### Trước khi xuất kho:
- Nút xanh "Tạo phiếu xuất kho" nổi bật
- Timeline có 2 bước: Giữ hàng (✓), Xuất kho (○)

### Sau khi xuất kho:
- Card xanh lá "Đã xuất kho thành công"
- Trạng thái cam "🚚 Đợi tài xế đến lấy hàng"
- Mã GHN hiển thị rõ ràng
- Timeline có 3 bước, bước 3 highlight màu cam
