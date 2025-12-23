# 🧪 Test Accounting APIs

## Cách test nhanh các API

### 1. Test bằng trình duyệt

Mở trình duyệt và truy cập trực tiếp các URL sau (sau khi đăng nhập):

```
http://localhost:8080/api/accounting/transactions?page=0&size=10
http://localhost:8080/api/accounting/periods
http://localhost:8080/api/accounting/tax/reports
```

### 2. Test bằng curl

```bash
# Lấy token sau khi login
TOKEN="your-jwt-token-here"

# Test transactions
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/accounting/transactions?page=0&size=10

# Test periods
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/accounting/periods

# Test tax reports
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/accounting/tax/reports
```

### 3. Kiểm tra backend logs

Xem backend console để thấy lỗi cụ thể khi API được gọi.

## Các lỗi thường gặp

### 1. 404 Not Found
- API endpoint không tồn tại
- Kiểm tra controller có được Spring Boot scan không

### 2. 500 Internal Server Error  
- Lỗi trong code backend
- Xem backend logs để biết chi tiết

### 3. 403 Forbidden
- Không có quyền truy cập
- Kiểm tra @PreAuthorize annotation
- Kiểm tra user có role ADMIN hoặc position ACCOUNTANT không

## Debug Steps

1. **Kiểm tra backend đang chạy**
   ```
   netstat -ano | findstr :8080
   ```

2. **Kiểm tra database tables đã được tạo**
   ```sql
   USE web3;
   SHOW TABLES LIKE '%accounting%';
   SHOW TABLES LIKE '%financial%';
   SHOW TABLES LIKE '%tax%';
   ```

3. **Kiểm tra user role/position**
   ```sql
   SELECT u.email, u.role, e.position 
   FROM users u 
   LEFT JOIN employees e ON u.id = e.user_id 
   WHERE u.email = 'your-email@example.com';
   ```

4. **Test API trực tiếp**
   - Mở Postman hoặc browser
   - Gọi API với token
   - Xem response

## Nếu vẫn lỗi

Gửi cho tôi:
1. URL trang bạn đang truy cập
2. Lỗi trong browser console (F12)
3. Backend logs (nếu có)
