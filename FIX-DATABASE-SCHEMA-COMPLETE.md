# Fix Database Schema - Complete Guide

## Problem
Backend fails to start because database `web2` has missing or corrupted tables. Foreign key constraints are failing during schema creation.

## Root Cause
- Database `web2` exists but is incomplete or corrupted
- Hibernate `ddl-auto=create` tried to create tables but foreign key constraints failed
- Some tables were created, others weren't, leaving the database in an inconsistent state

## Solution: Complete Database Reset

### Step 1: Drop and Recreate Database

Run the SQL script to completely reset the database:

```bash
mysql -u root -p < drop-and-recreate-database.sql
```

Or manually in MySQL:
```sql
DROP DATABASE IF EXISTS web2;
CREATE DATABASE web2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE web2;
```

### Step 2: Verify application.properties

Current setting (CORRECT for initial schema creation):
```properties
spring.jpa.hibernate.ddl-auto=create
```

### Step 3: Restart Backend

The backend will now create all tables in the correct order without foreign key errors.

**Expected output:**
- Hibernate will create all tables
- No foreign key constraint errors
- Application starts successfully
- Tables: users, payments, orders, products, categories, suppliers, etc. will be created

### Step 4: Change ddl-auto to 'update' (IMPORTANT!)

After successful startup, **immediately** change `application.properties`:

```properties
spring.jpa.hibernate.ddl-auto=update
```

This prevents data loss on future restarts.

### Step 5: Import Sample Data

After schema is created and ddl-auto is changed to 'update', import sample data:

```bash
# Note: The sample data script uses database 'web_tmdt' but we need 'web2'
# We'll need to modify it or create a new one
```

Create a modified sample data script for web2:

```sql
USE web2;

-- Insert admin user (password: admin123)
INSERT INTO users (email, password, full_name, phone, role, active, created_at, updated_at)
VALUES 
('admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Admin User', '0901234567', 'ADMIN', true, NOW(), NOW()),
('user@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Test User', '0907654321', 'USER', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE email = email;

-- Insert categories
INSERT INTO categories (name, description, active, created_at, updated_at)
VALUES 
('Laptop', 'Máy tính xách tay các loại', true, NOW(), NOW()),
('Điện thoại', 'Smartphone và phụ kiện', true, NOW(), NOW()),
('Tablet', 'Máy tính bảng', true, NOW(), NOW()),
('Phụ kiện', 'Phụ kiện công nghệ', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = name;

-- Insert suppliers
INSERT INTO suppliers (name, tax_code, email, phone, address, payment_term, payment_term_days, active, auto_created, created_at)
VALUES 
('Công ty TNHH Công nghệ ABC', '0123456789', 'abc@tech.com', '0281234567', '456 Đường XYZ, TP.HCM', 'NET_30', 30, true, false, NOW()),
('Công ty CP Điện tử DEF', '9876543210', 'def@electronics.com', '0287654321', '789 Đường MNO, Hà Nội', 'NET_15', 15, true, false, NOW())
ON DUPLICATE KEY UPDATE name = name;
```

## Verification Steps

### 1. Check if database exists and is empty
```sql
USE web2;
SHOW TABLES;
-- Should show empty result before backend starts
```

### 2. After backend starts, verify tables were created
```sql
USE web2;
SHOW TABLES;
-- Should show all tables: users, payments, orders, products, etc.
```

### 3. Check table structure
```sql
DESCRIBE users;
DESCRIBE payments;
DESCRIBE orders;
```

### 4. Test login
```bash
# Use test-auth.http or curl
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}'
```

## Troubleshooting

### If foreign key errors still occur:
1. Check MySQL version (should be 8.0+)
2. Verify InnoDB engine is being used
3. Check for circular dependencies in entity relationships

### If tables are not created:
1. Check Hibernate logs for errors
2. Verify database connection in application.properties
3. Ensure MySQL user has CREATE privileges

### If application starts but tables are missing:
1. Check that `ddl-auto=create` is set
2. Verify entity classes are properly annotated
3. Check package scanning in main application class

## Summary

✅ **Task 1**: Merge DashboardController → COMPLETE
✅ **Task 2**: Fix Database Schema → IN PROGRESS

**Current Status**: 
- Database needs to be dropped and recreated
- Backend is configured with `ddl-auto=create`
- Ready to restart after database reset

**Next Steps**:
1. Run `drop-and-recreate-database.sql`
2. Restart backend
3. Change `ddl-auto` to `update`
4. Import sample data
5. Test dashboard endpoints
