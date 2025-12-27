# 📊 API Dashboard Kế Toán - Hướng Dẫn Frontend

## ✅ Đã Hoàn Thành

Logic dashboard đã được **gộp vào AccountingService** và có 2 controller để frontend gọi:

### 1. Controller Chung (Cho tất cả nhân viên)
**Endpoint**: `/api/dashboard`
- `GET /api/dashboard/stats` - Lấy thống kê dashboard
- `GET /api/dashboard/recent-orders?limit=10` - Lấy đơn hàng gần đây

**Quyền truy cập**: `ROLE_ADMIN`, `ROLE_EMPLOYEE`

### 2. Controller Kế Toán (Chỉ cho kế toán viên)
**Endpoint**: `/api/accounting/dashboard`
- `GET /api/accounting/dashboard/stats` - Lấy thống kê dashboard
- `GET /api/accounting/dashboard/recent-orders?limit=10` - Lấy đơn hàng gần đây

**Quyền truy cập**: `ADMIN`, `ACCOUNTANT`

---

## 🎯 Cách Sử Dụng Ở Frontend

### Option 1: Dùng Controller Chung (Khuyến nghị cho trang chung)

**File**: `src/frontend/app/employee/page.tsx` (Dashboard chung cho tất cả nhân viên)

```typescript
useEffect(() => {
  const fetchDashboard = async () => {
    try {
      const response = await fetch('/api/dashboard/stats', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      const result = await response.json();
      
      if (result.success) {
        setDashboardData(result.data);
      }
    } catch (error) {
      console.error('Error fetching dashboard:', error);
    }
  };
  
  fetchDashboard();
}, []);
```

### Option 2: Dùng Controller Kế Toán (Cho trang kế toán)

**File**: `src/frontend/app/employee/accounting/page.tsx`

```typescript
useEffect(() => {
  const fetchAccountingDashboard = async () => {
    try {
      const response = await fetch('/api/accounting/dashboard/stats', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      const result = await response.json();
      
      if (result.success) {
        setDashboardData(result.data);
      }
    } catch (error) {
      console.error('Error fetching accounting dashboard:', error);
    }
  };
  
  fetchAccountingDashboard();
}, []);
```

---

## 📦 Response Format

### GET /api/dashboard/stats hoặc /api/accounting/dashboard/stats

**Response**:
```json
{
  "success": true,
  "message": "Dashboard stats",
  "data": {
    "totalRevenue": 15000000,
    "totalOrders": 25,
    "totalProducts": 150,
    "totalCustomers": 80,
    "revenueChangePercent": 12.5,
    "ordersChangePercent": -5.2,
    "totalProfit": null,
    "profitMargin": null,
    "pendingOrders": null,
    "lowStockProducts": null,
    "overdueOrders": null,
    "overduePayables": null,
    "ordersChangePercent": -5.2,
    "profitChangePercent": null,
    "productsChangePercent": null,
    "customersChangePercent": null
  }
}
```

### GET /api/dashboard/recent-orders?limit=10

**Response**:
```json
{
  "success": true,
  "message": "Recent orders",
  "data": [
    {
      "id": 123,
      "orderCode": "ORD-20241227-001",
      "totalAmount": 500000,
      "status": "DELIVERED",
      "createdAt": "2024-12-27T10:30:00",
      "customerName": "Nguyễn Văn A",
      "customerEmail": "nguyenvana@example.com"
    }
  ]
}
```

---

## 🔧 Backend Implementation

### AccountingService.java
```java
public interface AccountingService {
    ApiResponse getDashboardStats();
    ApiResponse getRecentOrders(int limit);
    // ... các method khác
}
```

### AccountingServiceImpl.java
- ✅ `getDashboardStats()` - Tính toán thống kê từ orders, products, users
- ✅ `getRecentOrders(limit)` - Lấy đơn hàng gần đây với pagination
- ✅ `convertToDTO()` - Convert Order entity sang OrderDTO
- ✅ `calculatePercentageChange()` - Tính % thay đổi so với hôm qua

### Controllers
1. **DashboardController** (`/api/dashboard`) - Cho tất cả nhân viên
2. **AccountingDashboardController** (`/api/accounting/dashboard`) - Chỉ cho kế toán

---

## 📝 Lưu Ý

1. **Phân quyền**:
   - `/api/dashboard/*` → `ROLE_ADMIN`, `ROLE_EMPLOYEE`
   - `/api/accounting/dashboard/*` → `ADMIN`, `ACCOUNTANT`

2. **Data hiện tại**:
   - `totalRevenue` - Doanh thu hôm nay (đơn DELIVERED)
   - `totalOrders` - Số đơn hàng hôm nay
   - `revenueChangePercent` - % thay đổi so với hôm qua
   - `ordersChangePercent` - % thay đổi số đơn so với hôm qua

3. **Các field null**:
   - Một số field trong `DashboardStatsDTO` chưa được tính (profit, lowStock, overdue...)
   - Frontend nên check null trước khi hiển thị

4. **Performance**:
   - Hiện tại dùng `findAll()` + stream filter (không tối ưu)
   - Nên thêm query method trong repository sau:
     ```java
     List<Order> findByCreatedAtBetweenAndStatus(
         LocalDateTime start, 
         LocalDateTime end, 
         OrderStatus status
     );
     ```

---

## 🚀 Next Steps

1. **Frontend**: Cập nhật API call từ `/api/dashboard/stats` hoặc `/api/accounting/dashboard/stats`
2. **Test**: Kiểm tra quyền truy cập và response data
3. **Optimize**: Thêm query methods để tối ưu performance
4. **Extend**: Thêm các metrics khác nếu cần (profit, lowStock, overdue...)

---

**Tạo ngày**: 2024-12-27  
**Version**: 1.0
