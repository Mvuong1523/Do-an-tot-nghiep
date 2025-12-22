# 🔄 Hướng dẫn restart frontend để áp dụng thay đổi

## Vấn đề
Frontend đang cache code cũ, vẫn gọi `/api/api/dashboard/stats` thay vì `/dashboard/stats`

## Giải pháp

### Cách 1: Hard Refresh trình duyệt (Nhanh nhất)
1. Mở trang dashboard: http://localhost:3000/employee
2. Nhấn **Ctrl + Shift + R** (Windows) hoặc **Cmd + Shift + R** (Mac)
3. Hoặc mở DevTools (F12) → Network tab → check "Disable cache" → F5

### Cách 2: Restart Next.js dev server
```bash
# Dừng server hiện tại (Ctrl + C)
# Sau đó chạy lại:
cd src/frontend
npm run dev
```

### Cách 3: Xóa cache Next.js
```bash
cd src/frontend
rm -rf .next
npm run dev
```

## Kiểm tra
Sau khi restart, mở DevTools (F12) → Network tab và xem request:
- ✅ ĐÚNG: `GET http://localhost:8080/api/dashboard/stats`
- ❌ SAI: `GET http://localhost:8080/api/api/dashboard/stats`

## Lưu ý
- File `src/frontend/app/employee/page.tsx` đã được sửa đúng
- Backend đang chạy bình thường trên port 8080
- Chỉ cần restart frontend để áp dụng thay đổi
