# Hướng dẫn Restart Backend để Update Database Schema

## Vấn đề đã sửa:
- Thêm `length = 30` cho cột `category` trong `FinancialTransaction` entity
- Thêm `length = 20` cho cột `type` trong `FinancialTransaction` entity

## Cách restart:

### Option 1: Trong IDE (IntelliJ IDEA / Eclipse)
1. Stop backend server (nút Stop màu đỏ)
2. Start lại backend server (nút Run màu xanh)

### Option 2: Command Line
```bash
# Stop backend (Ctrl+C nếu đang chạy)
# Sau đó chạy lại:
mvnw spring-boot:run
```

### Option 3: Nếu chạy bằng JAR
```bash
# Stop process hiện tại
# Rebuild và chạy lại:
mvnw clean package
java -jar target/WEB_TMDT-0.0.1-SNAPSHOT.jar
```

## Sau khi restart:

Hibernate sẽ tự động chạy câu lệnh:
```sql
ALTER TABLE financial_transactions 
MODIFY COLUMN category VARCHAR(30) NOT NULL;

ALTER TABLE financial_transactions 
MODIFY COLUMN type VARCHAR(20) NOT NULL;
```

## Kiểm tra:

Sau khi backend khởi động xong, thử lại chức năng thanh toán NCC. Lỗi "Data truncated" sẽ không còn nữa!

## Lưu ý:

- `spring.jpa.hibernate.ddl-auto=update` sẽ tự động update schema
- Không cần chạy SQL script thủ công
- Nếu vẫn lỗi, có thể cần xóa bảng và tạo lại (hoặc chạy SQL script thủ công)
