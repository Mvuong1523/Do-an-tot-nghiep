# Backend Restart Required

## Vấn đề

Sau khi sửa code backend (Java files), vẫn gặp lỗi 403 Forbidden vì backend đang chạy code CŨ.

## Nguyên nhân

Spring Boot cần **restart** để:
1. Recompile Java files
2. Load class definitions mới
3. Apply @PreAuthorize annotations mới

## Giải pháp

### Option 1: Restart từ terminal (Recommended)

```bash
# Stop backend (Ctrl+C trong terminal đang chạy mvn)
# Then start again
mvn spring-boot:run
```

### Option 2: Restart từ IDE

Nếu đang chạy từ IntelliJ IDEA:
1. Click nút Stop (red square)
2. Click nút Run (green play button)

### Option 3: Kill process và restart

```bash
# Windows
taskkill /F /IM java.exe

# Then start
mvn spring-boot:run
```

## Kiểm tra backend đã restart

### 1. Check logs
Xem log xuất hiện:
```
Started WEB_TMDT in X.XXX seconds
```

### 2. Test API endpoint
```bash
curl http://localhost:8080/api/inventory/purchase-orders \
  -H "Authorization: Bearer YOUR_TOKEN"
```

Nếu trả về 200 OK → Backend đã restart thành công
Nếu vẫn 403 → Cần kiểm tra lại code

### 3. Check browser console
Refresh trang employee và xem:
- ✅ Không còn 403 errors
- ✅ Data hiển thị đúng

## Files đã sửa cần restart

### Backend (Java)
- ✅ `src/main/java/com/doan/WEB_TMDT/module/inventory/controller/InventoryController.java`
  - Thêm `'EMPLOYEE'` authority cho tất cả GET endpoints

### Frontend (TypeScript/React)
- ✅ Không cần restart, Next.js auto-reload
- ✅ Chỉ cần refresh browser

## Timeline

1. **Sửa InventoryController.java** - Thêm EMPLOYEE authority
2. **Backend vẫn chạy code cũ** - Chưa recompile
3. **Frontend gọi API** - Nhận 403 vì backend dùng code cũ
4. **Restart backend** - Recompile và load code mới ✅
5. **Test lại** - Giờ sẽ trả về 200 OK ✅

## Waiting for Backend Startup

Backend đang khởi động, đợi khoảng 30-60 giây để:
1. Maven download dependencies (nếu cần)
2. Compile Java files
3. Spring Boot initialize
4. Database connection established
5. Application ready

Xem log để biết khi nào ready:
```
2024-XX-XX XX:XX:XX.XXX  INFO --- [  restartedMain] c.d.WEB_TMDT.WebTmdtApplication         : Started WebTmdtApplication in X.XXX seconds
```

## Testing After Restart

### 1. Login as Employee (WAREHOUSE position)
```
1. Navigate to /employee/warehouse/import
2. Open DevTools → Network tab
3. Check API call to /api/inventory/purchase-orders
4. Verify: Status 200 OK (not 403)
5. Verify: Data displays in table
```

### 2. Login as Employee (SALE position)
```
1. Navigate to /employee/warehouse/import
2. Should see data (read-only)
3. Should see yellow warning banner
4. Create button should be disabled
```

### 3. Check all warehouse pages
- [ ] /employee/warehouse - Dashboard
- [ ] /employee/warehouse/import - Import list
- [ ] /employee/warehouse/export - Export list
- [ ] /employee/warehouse/products - Products list
- [ ] /employee/warehouse/inventory - Inventory
- [ ] /employee/warehouse/suppliers - Suppliers

All should display data without 403 errors.

## Common Issues

### Issue 1: Port 8080 already in use
```
Error: Port 8080 is already in use
```

**Solution**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /F /PID <PID>

# Then restart
mvn spring-boot:run
```

### Issue 2: Compilation errors
```
[ERROR] Failed to execute goal ... compilation failure
```

**Solution**:
1. Check InventoryController.java syntax
2. Make sure all imports are correct
3. Run: `mvn clean compile`

### Issue 3: Database connection failed
```
Error: Could not connect to database
```

**Solution**:
1. Check MySQL is running
2. Check application.properties
3. Verify database credentials

## Summary

- ✅ Backend code đã sửa
- ✅ Frontend code đã sửa
- ⏳ Backend đang restart
- ⏳ Đợi backend ready (~30-60s)
- ⏳ Test lại sau khi backend ready

Sau khi thấy log "Started WebTmdtApplication", refresh browser và test!
