# API Endpoints Analysis - Hệ Thống TMDT

## Tổng Quan

Tài liệu này phân tích chi tiết tất cả REST API endpoints trong hệ thống thương mại điện tử, bao gồm:
- Request/Response format
- Authentication requirements
- Error codes
- Nhóm theo module chức năng

## Cấu Trúc Chung

### Base URL
- **Development**: `http://localhost:8080`
- **Production**: `https://api.yourdomain.com`

### Authentication
Hệ thống sử dụng JWT (JSON Web Token) authentication:
- Header: `Authorization: Bearer <token>`
- Token được trả về sau khi login thành công
- Token có thời gian hết hạn (configurable)

### Response Format
Tất cả API đều trả về format chuẩn:
```json
{
  "success": true/false,
  "message": "Thông báo",
  "data": { ... }
}
```

### Common Error Codes
- **200 OK**: Request thành công
- **400 Bad Request**: Dữ liệu không hợp lệ
- **401 Unauthorized**: Chưa đăng nhập hoặc token không hợp lệ
- **403 Forbidden**: Không có quyền truy cập
- **404 Not Found**: Không tìm thấy resource
- **500 Internal Server Error**: Lỗi server

---

## Module 1: Authentication & Authorization

### Base Path: `/api/auth`


#### 1.1. POST `/api/auth/register/send-otp`
**Mô tả**: Gửi OTP để đăng ký tài khoản khách hàng

**Authentication**: None (Public)

**Request Body**:
```json
{
  "email": "customer@example.com",
  "fullName": "Nguyễn Văn A",
  "phone": "0123456789"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã gửi OTP đến email",
  "data": null
}
```

**Error Codes**:
- 400: Email đã tồn tại, số điện thoại không hợp lệ
- 500: Lỗi gửi email

---

#### 1.2. POST `/api/auth/register/verify-otp`
**Mô tả**: Xác thực OTP và hoàn tất đăng ký

**Authentication**: None (Public)

**Request Body**:
```json
{
  "email": "customer@example.com",
  "otp": "123456",
  "password": "SecurePassword123"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "email": "customer@example.com",
      "role": "CUSTOMER"
    }
  }
}
```

**Error Codes**:
- 400: OTP không đúng, OTP hết hạn
- 404: Email không tồn tại

---


#### 1.3. POST `/api/auth/login`
**Mô tả**: Đăng nhập hệ thống

**Authentication**: None (Public)

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "role": "CUSTOMER",
      "position": null,
      "requirePasswordChange": false
    }
  }
}
```

**Error Codes**:
- 400: Email hoặc mật khẩu không đúng
- 401: Tài khoản bị khóa

---

#### 1.4. POST `/api/auth/first-change-password`
**Mô tả**: Đổi mật khẩu lần đầu (cho nhân viên mới)

**Authentication**: Required (JWT)

**Request Body**:
```json
{
  "email": "employee@example.com",
  "oldPassword": "TempPassword123",
  "newPassword": "NewSecurePassword123"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đổi mật khẩu thành công",
  "data": null
}
```

**Error Codes**:
- 400: Mật khẩu cũ không đúng, mật khẩu mới không đủ mạnh
- 401: Chưa đăng nhập

---

#### 1.5. POST `/api/auth/change-password`
**Mô tả**: Đổi mật khẩu

**Authentication**: Required (JWT)

**Request Body**:
```json
{
  "oldPassword": "OldPassword123",
  "newPassword": "NewPassword123"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đổi mật khẩu thành công",
  "data": null
}
```

**Error Codes**:
- 400: Mật khẩu cũ không đúng
- 401: Chưa đăng nhập

---

#### 1.6. GET `/api/auth/me`
**Mô tả**: Lấy thông tin user hiện tại

**Authentication**: Required (JWT)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Thông tin người dùng",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "role": "CUSTOMER",
    "fullName": "Nguyễn Văn A"
  }
}
```

**Error Codes**:
- 401: Chưa đăng nhập

---


### Base Path: `/api/customer`

#### 1.7. GET `/api/customer/profile`
**Mô tả**: Lấy thông tin profile khách hàng

**Authentication**: Required (CUSTOMER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Thông tin khách hàng",
  "data": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "phone": "0123456789",
    "email": "customer@example.com",
    "address": "123 Đường ABC",
    "gender": "MALE",
    "birthDate": "1990-01-01"
  }
}
```

**Error Codes**:
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập
- 404: Không tìm thấy thông tin khách hàng

---

#### 1.8. PUT `/api/customer/profile`
**Mô tả**: Cập nhật thông tin profile

**Authentication**: Required (CUSTOMER, ADMIN)

**Request Body**:
```json
{
  "fullName": "Nguyễn Văn B",
  "phone": "0987654321",
  "address": "456 Đường XYZ",
  "gender": "MALE",
  "birthDate": "1990-01-01"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Cập nhật thông tin thành công",
  "data": { /* updated customer object */ }
}
```

**Error Codes**:
- 400: Dữ liệu không hợp lệ
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập

---

### Base Path: `/api/employee-registration`

#### 1.9. POST `/api/employee-registration/apply`
**Mô tả**: Nhân viên gửi yêu cầu đăng ký

**Authentication**: None (Public)

**Request Body**:
```json
{
  "fullName": "Trần Thị B",
  "email": "employee@example.com",
  "phone": "0123456789",
  "address": "789 Đường DEF",
  "position": "WAREHOUSE",
  "note": "Có kinh nghiệm 2 năm"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã gửi yêu cầu đăng ký",
  "data": {
    "id": 1,
    "status": "PENDING"
  }
}
```

**Error Codes**:
- 400: Email đã tồn tại, dữ liệu không hợp lệ

---

#### 1.10. POST `/api/employee-registration/approve/{id}`
**Mô tả**: Admin duyệt yêu cầu đăng ký nhân viên

**Authentication**: Required (ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã duyệt nhân viên",
  "data": {
    "email": "employee@example.com",
    "temporaryPassword": "TempPass123"
  }
}
```

**Error Codes**:
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập
- 404: Không tìm thấy yêu cầu

---

#### 1.11. GET `/api/employee-registration/list`
**Mô tả**: Admin xem tất cả yêu cầu đăng ký

**Authentication**: Required (ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách yêu cầu",
  "data": [
    {
      "id": 1,
      "fullName": "Trần Thị B",
      "email": "employee@example.com",
      "position": "WAREHOUSE",
      "status": "PENDING",
      "createdAt": "2024-01-01T10:00:00"
    }
  ]
}
```

---

#### 1.12. GET `/api/employee-registration/pending`
**Mô tả**: Admin xem yêu cầu chờ duyệt

**Authentication**: Required (ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách chờ duyệt",
  "data": [ /* pending registrations */ ]
}
```

---


## Module 2: Product Management

### Base Path: `/api/products`

#### 2.1. GET `/api/products`
**Mô tả**: Lấy danh sách sản phẩm (cho khách hàng)

**Authentication**: None (Public)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách sản phẩm",
  "data": [
    {
      "id": 1,
      "name": "iPhone 15 Pro Max",
      "price": 29990000,
      "description": "Điện thoại cao cấp",
      "imageUrl": "https://...",
      "category": {
        "id": 1,
        "name": "Điện thoại"
      },
      "specifications": [
        {
          "key": "RAM",
          "value": "8GB"
        }
      ],
      "active": true
    }
  ]
}
```

**Error Codes**: None (always returns 200)

---

#### 2.2. GET `/api/products/{id}`
**Mô tả**: Lấy chi tiết sản phẩm

**Authentication**: None (Public)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Thông tin sản phẩm",
  "data": {
    "id": 1,
    "name": "iPhone 15 Pro Max",
    "price": 29990000,
    "description": "Điện thoại cao cấp",
    "images": [
      {
        "id": 1,
        "imageUrl": "https://...",
        "isPrimary": true,
        "displayOrder": 1
      }
    ],
    "specifications": [ /* ... */ ],
    "category": { /* ... */ }
  }
}
```

**Error Codes**:
- 404: Không tìm thấy sản phẩm

---

#### 2.3. POST `/api/products`
**Mô tả**: Tạo sản phẩm mới

**Authentication**: Required (PRODUCT_MANAGER, ADMIN)

**Request Body**:
```json
{
  "name": "iPhone 15 Pro Max",
  "price": 29990000,
  "description": "Điện thoại cao cấp",
  "categoryId": 1,
  "imageUrl": "https://..."
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Tạo sản phẩm thành công",
  "data": { /* created product */ }
}
```

**Error Codes**:
- 400: Dữ liệu không hợp lệ
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập

---

#### 2.4. PUT `/api/products/{id}`
**Mô tả**: Cập nhật sản phẩm

**Authentication**: Required (PRODUCT_MANAGER, ADMIN)

**Request Body**: Same as POST

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Cập nhật sản phẩm thành công",
  "data": { /* updated product */ }
}
```

**Error Codes**:
- 400: Dữ liệu không hợp lệ
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập
- 404: Không tìm thấy sản phẩm

---

#### 2.5. DELETE `/api/products/{id}`
**Mô tả**: Xóa sản phẩm (soft delete)

**Authentication**: Required (ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Xóa sản phẩm thành công",
  "data": null
}
```

**Error Codes**:
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập
- 404: Không tìm thấy sản phẩm

---

#### 2.6. PUT `/api/products/{id}/toggle-active`
**Mô tả**: Bật/tắt trạng thái bán sản phẩm

**Authentication**: Required (PRODUCT_MANAGER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã chuyển sản phẩm sang trạng thái đang bán",
  "data": null
}
```

---

#### 2.7. GET `/api/products/warehouse/list`
**Mô tả**: Lấy danh sách sản phẩm kho để đăng bán

**Authentication**: Required (PRODUCT_MANAGER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách sản phẩm kho",
  "data": [
    {
      "id": 1,
      "sku": "WH-001",
      "internalName": "iPhone 15 Pro Max 256GB",
      "supplier": {
        "id": 1,
        "name": "Nhà cung cấp A"
      },
      "stock": {
        "onHand": 100,
        "available": 80
      }
    }
  ]
}
```

---

#### 2.8. POST `/api/products/warehouse/publish`
**Mô tả**: Đăng bán sản phẩm từ kho

**Authentication**: Required (PRODUCT_MANAGER, ADMIN)

**Request Body**:
```json
{
  "warehouseProductId": 1,
  "name": "iPhone 15 Pro Max",
  "price": 29990000,
  "description": "Điện thoại cao cấp",
  "categoryId": 1,
  "imageUrl": "https://..."
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đăng bán sản phẩm thành công",
  "data": { /* published product */ }
}
```

---

#### 2.9. GET `/api/products/search-by-specs`
**Mô tả**: Tìm kiếm sản phẩm theo thông số kỹ thuật

**Authentication**: None (Public)

**Query Parameters**:
- `keyword`: Từ khóa tìm kiếm

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Tìm thấy 5 sản phẩm",
  "data": [ /* products */ ]
}
```

---

#### 2.10. GET `/api/products/{productId}/images`
**Mô tả**: Lấy danh sách ảnh của sản phẩm

**Authentication**: None (Public)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách ảnh",
  "data": [
    {
      "id": 1,
      "imageUrl": "https://...",
      "isPrimary": true,
      "displayOrder": 1
    }
  ]
}
```

---

#### 2.11. POST `/api/products/{productId}/images`
**Mô tả**: Thêm ảnh cho sản phẩm

**Authentication**: Required (PRODUCT_MANAGER, ADMIN)

**Request Body**:
```json
{
  "imageUrl": "https://...",
  "isPrimary": false
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Thêm ảnh thành công",
  "data": { /* image object */ }
}
```

---

#### 2.12. DELETE `/api/products/images/{imageId}`
**Mô tả**: Xóa ảnh sản phẩm

**Authentication**: Required (PRODUCT_MANAGER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Xóa ảnh thành công",
  "data": null
}
```

---


### Base Path: `/api/categories`

#### 2.13. GET `/api/categories`
**Mô tả**: Lấy danh sách danh mục

**Authentication**: None (Public)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách danh mục",
  "data": [
    {
      "id": 1,
      "name": "Điện thoại",
      "description": "Điện thoại di động",
      "parentId": null,
      "active": true
    }
  ]
}
```

---

#### 2.14. GET `/api/categories/tree`
**Mô tả**: Lấy cây danh mục (hierarchical)

**Authentication**: None (Public)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Cây danh mục",
  "data": [
    {
      "id": 1,
      "name": "Điện tử",
      "children": [
        {
          "id": 2,
          "name": "Điện thoại",
          "children": []
        }
      ]
    }
  ]
}
```

---

#### 2.15. GET `/api/categories/{id}`
**Mô tả**: Lấy chi tiết danh mục và sản phẩm

**Authentication**: None (Public)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Chi tiết danh mục",
  "data": {
    "category": {
      "id": 1,
      "name": "Điện thoại"
    },
    "products": [ /* products in category */ ]
  }
}
```

---

#### 2.16. POST `/api/categories`
**Mô tả**: Tạo danh mục mới

**Authentication**: Required (PRODUCT_MANAGER, ADMIN)

**Request Body**:
```json
{
  "name": "Laptop",
  "description": "Máy tính xách tay",
  "parentId": 1
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Tạo danh mục thành công",
  "data": { /* created category */ }
}
```

---

#### 2.17. PUT `/api/categories/{id}`
**Mô tả**: Cập nhật danh mục

**Authentication**: Required (PRODUCT_MANAGER, ADMIN)

**Request Body**: Same as POST

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Cập nhật danh mục thành công",
  "data": { /* updated category */ }
}
```

---

#### 2.18. DELETE `/api/categories/{id}`
**Mô tả**: Xóa danh mục

**Authentication**: Required (ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Xóa danh mục thành công",
  "data": null
}
```

**Error Codes**:
- 400: Danh mục có sản phẩm hoặc danh mục con

---


## Module 3: Cart Management

### Base Path: `/api/cart`

#### 3.1. GET `/api/cart`
**Mô tả**: Lấy giỏ hàng của khách hàng

**Authentication**: Required (CUSTOMER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Giỏ hàng",
  "data": {
    "id": 1,
    "customerId": 1,
    "items": [
      {
        "id": 1,
        "product": {
          "id": 1,
          "name": "iPhone 15 Pro Max",
          "price": 29990000,
          "imageUrl": "https://..."
        },
        "quantity": 2,
        "subtotal": 59980000
      }
    ],
    "total": 59980000
  }
}
```

**Error Codes**:
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập

---

#### 3.2. POST `/api/cart/items`
**Mô tả**: Thêm sản phẩm vào giỏ hàng

**Authentication**: Required (CUSTOMER, ADMIN)

**Request Body**:
```json
{
  "productId": 1,
  "quantity": 2
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã thêm vào giỏ hàng",
  "data": {
    "cartItem": { /* cart item */ },
    "cart": { /* updated cart */ }
  }
}
```

**Error Codes**:
- 400: Sản phẩm không tồn tại, số lượng không hợp lệ
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập

---

#### 3.3. PUT `/api/cart/items/{itemId}`
**Mô tả**: Cập nhật số lượng sản phẩm trong giỏ

**Authentication**: Required (CUSTOMER, ADMIN)

**Request Body**:
```json
{
  "quantity": 3
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Cập nhật giỏ hàng thành công",
  "data": { /* updated cart */ }
}
```

**Error Codes**:
- 400: Số lượng không hợp lệ
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập
- 404: Không tìm thấy item

---

#### 3.4. DELETE `/api/cart/items/{itemId}`
**Mô tả**: Xóa sản phẩm khỏi giỏ hàng

**Authentication**: Required (CUSTOMER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã xóa khỏi giỏ hàng",
  "data": null
}
```

**Error Codes**:
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập
- 404: Không tìm thấy item

---

#### 3.5. DELETE `/api/cart`
**Mô tả**: Xóa tất cả sản phẩm trong giỏ

**Authentication**: Required (CUSTOMER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã xóa giỏ hàng",
  "data": null
}
```

---


## Module 4: Order Management

### Base Path: `/api/orders` (Customer)

#### 4.1. POST `/api/orders`
**Mô tả**: Tạo đơn hàng từ giỏ hàng

**Authentication**: Required (CUSTOMER, ADMIN)

**Request Body**:
```json
{
  "shippingAddress": {
    "provinceId": 201,
    "provinceName": "Hà Nội",
    "districtId": 1482,
    "districtName": "Quận Hoàn Kiếm",
    "wardCode": "1A0101",
    "wardName": "Phường Hàng Bạc",
    "detailedAddress": "123 Đường ABC"
  },
  "paymentMethod": "COD",
  "note": "Giao giờ hành chính"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đặt hàng thành công",
  "data": {
    "id": 1,
    "orderCode": "ORD20240101001",
    "status": "CONFIRMED",
    "paymentStatus": "PENDING",
    "total": 59980000,
    "shippingFee": 30000,
    "items": [ /* order items */ ],
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

**Error Codes**:
- 400: Giỏ hàng trống, địa chỉ không hợp lệ, sản phẩm hết hàng
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập

---

#### 4.2. GET `/api/orders`
**Mô tả**: Lấy danh sách đơn hàng của khách hàng

**Authentication**: Required (CUSTOMER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách đơn hàng",
  "data": [
    {
      "id": 1,
      "orderCode": "ORD20240101001",
      "status": "SHIPPING",
      "total": 59980000,
      "createdAt": "2024-01-01T10:00:00"
    }
  ]
}
```

---

#### 4.3. GET `/api/orders/{orderId}`
**Mô tả**: Lấy chi tiết đơn hàng

**Authentication**: Required (CUSTOMER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Chi tiết đơn hàng",
  "data": {
    "id": 1,
    "orderCode": "ORD20240101001",
    "status": "SHIPPING",
    "paymentStatus": "PAID",
    "paymentMethod": "ONLINE",
    "total": 59980000,
    "shippingFee": 30000,
    "shippingAddress": "123 Đường ABC, Phường Hàng Bạc, Quận Hoàn Kiếm, Hà Nội",
    "items": [
      {
        "id": 1,
        "product": {
          "id": 1,
          "name": "iPhone 15 Pro Max"
        },
        "quantity": 2,
        "price": 29990000,
        "subtotal": 59980000
      }
    ],
    "ghnOrderCode": "GHNABCD1234",
    "createdAt": "2024-01-01T10:00:00",
    "confirmedAt": "2024-01-01T10:05:00"
  }
}
```

**Error Codes**:
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập
- 404: Không tìm thấy đơn hàng

---

#### 4.4. GET `/api/orders/code/{orderCode}`
**Mô tả**: Lấy đơn hàng theo mã

**Authentication**: Required (CUSTOMER, ADMIN)

**Response**: Same as 4.3

---

#### 4.5. PUT `/api/orders/{orderId}/cancel`
**Mô tả**: Hủy đơn hàng (khách hàng)

**Authentication**: Required (CUSTOMER, ADMIN)

**Query Parameters**:
- `reason`: Lý do hủy (optional)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã hủy đơn hàng",
  "data": null
}
```

**Error Codes**:
- 400: Không thể hủy đơn hàng ở trạng thái này
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập
- 404: Không tìm thấy đơn hàng

---

#### 4.6. GET `/api/orders/{orderId}/shipping-status`
**Mô tả**: Xem trạng thái vận chuyển GHN

**Authentication**: Required (CUSTOMER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Trạng thái vận chuyển",
  "data": {
    "ghnOrderCode": "GHNABCD1234",
    "status": "delivering",
    "statusText": "Đang giao hàng",
    "currentWarehouse": "Kho Hà Nội",
    "expectedDeliveryTime": "2024-01-03T18:00:00"
  }
}
```

---

### Base Path: `/api/admin/orders` (Admin/Staff)

#### 4.7. GET `/api/admin/orders`
**Mô tả**: Lấy tất cả đơn hàng (admin/staff)

**Authentication**: Required (ADMIN, SALES, WAREHOUSE)

**Query Parameters**:
- `status`: Lọc theo trạng thái (optional)
- `page`: Số trang (default: 0)
- `size`: Số lượng/trang (default: 20)
- `search`: Tìm kiếm (optional)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách đơn hàng",
  "data": {
    "content": [ /* orders */ ],
    "totalElements": 100,
    "totalPages": 5,
    "currentPage": 0
  }
}
```

---

#### 4.8. GET `/api/admin/orders/{orderId}`
**Mô tả**: Lấy chi tiết đơn hàng (admin)

**Authentication**: Required (ADMIN, SALES, WAREHOUSE)

**Response**: Same as 4.3

---

#### 4.9. GET `/api/admin/orders/statistics`
**Mô tả**: Lấy thống kê đơn hàng

**Authentication**: Required (ADMIN, SALES)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Thống kê đơn hàng",
  "data": {
    "totalOrders": 1000,
    "pendingOrders": 50,
    "confirmedOrders": 30,
    "shippingOrders": 20,
    "deliveredOrders": 800,
    "cancelledOrders": 100,
    "totalRevenue": 500000000
  }
}
```

---

#### 4.10. PUT `/api/admin/orders/{orderId}/mark-shipping-from-ready`
**Mô tả**: Chuyển đơn từ READY_TO_SHIP sang SHIPPING

**Authentication**: Required (ADMIN, SALES)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã cập nhật trạng thái",
  "data": null
}
```

**Error Codes**:
- 400: Trạng thái không hợp lệ
- 404: Không tìm thấy đơn hàng

---

#### 4.11. PUT `/api/admin/orders/{orderId}/delivered`
**Mô tả**: Đánh dấu đơn hàng đã giao

**Authentication**: Required (ADMIN, SALES)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã cập nhật trạng thái",
  "data": null
}
```

---

#### 4.12. PUT `/api/admin/orders/{orderId}/cancel`
**Mô tả**: Hủy đơn hàng (admin)

**Authentication**: Required (ADMIN, SALES)

**Query Parameters**:
- `reason`: Lý do hủy (optional)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã hủy đơn hàng",
  "data": null
}
```

---

#### 4.13. PUT `/api/admin/orders/{orderId}/status`
**Mô tả**: Cập nhật trạng thái đơn hàng

**Authentication**: Required (ADMIN)

**Query Parameters**:
- `status`: Trạng thái mới

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã cập nhật trạng thái",
  "data": null
}
```

---


## Module 5: Inventory Management

### Base Path: `/api/inventory`

#### 5.1. POST `/api/inventory/create_pchaseOrder`
**Mô tả**: Tạo đơn đặt hàng nhà cung cấp

**Authentication**: Required (WAREHOUSE, ADMIN)

**Request Body**:
```json
{
  "supplierId": 1,
  "items": [
    {
      "warehouseProductId": 1,
      "quantity": 100,
      "unitPrice": 25000000
    }
  ],
  "note": "Đặt hàng tháng 1"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Tạo đơn đặt hàng thành công",
  "data": {
    "id": 1,
    "poCode": "PO20240101001",
    "status": "CREATED",
    "totalAmount": 2500000000
  }
}
```

---

#### 5.2. POST `/api/inventory/import`
**Mô tả**: Hoàn tất nhập kho (complete purchase order)

**Authentication**: Required (WAREHOUSE, ADMIN)

**Request Body**:
```json
{
  "purchaseOrderId": 1,
  "receivedDate": "2024-01-05",
  "note": "Đã nhận đủ hàng"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Nhập kho thành công",
  "data": {
    "purchaseOrder": { /* PO details */ },
    "stockUpdates": [ /* stock changes */ ]
  }
}
```

**Error Codes**:
- 400: PO không ở trạng thái CREATED
- 404: Không tìm thấy PO

---

#### 5.3. POST `/api/inventory/export-for-sale`
**Mô tả**: Xuất kho cho đơn hàng

**Authentication**: Required (WAREHOUSE, ADMIN)

**Request Body**:
```json
{
  "orderId": 1,
  "reason": "Xuất hàng cho đơn ORD20240101001",
  "items": [
    {
      "warehouseProductId": 1,
      "quantity": 2,
      "serialNumbers": ["SN001", "SN002"]
    }
  ]
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Xuất kho thành công",
  "data": {
    "exportOrder": {
      "id": 1,
      "exportCode": "EXP20240101001",
      "status": "COMPLETED",
      "orderId": 1
    },
    "orderStatus": "READY_TO_SHIP"
  }
}
```

**Error Codes**:
- 400: Không đủ hàng, đơn hàng không hợp lệ
- 404: Không tìm thấy đơn hàng

---

#### 5.4. GET `/api/inventory/stock`
**Mô tả**: Xem tồn kho

**Authentication**: Required (WAREHOUSE, PRODUCT_MANAGER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách tồn kho",
  "data": [
    {
      "warehouseProduct": {
        "id": 1,
        "sku": "WH-001",
        "internalName": "iPhone 15 Pro Max 256GB"
      },
      "onHand": 100,
      "reserved": 20,
      "damaged": 0,
      "available": 80
    }
  ]
}
```

---

#### 5.5. GET `/api/inventory/suppliers`
**Mô tả**: Lấy danh sách nhà cung cấp

**Authentication**: Required (WAREHOUSE, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách nhà cung cấp",
  "data": [
    {
      "id": 1,
      "name": "Nhà cung cấp A",
      "contactPerson": "Nguyễn Văn A",
      "phone": "0123456789",
      "email": "supplier@example.com",
      "address": "123 Đường ABC"
    }
  ]
}
```

---

#### 5.6. POST `/api/inventory/suppliers`
**Mô tả**: Tạo nhà cung cấp mới

**Authentication**: Required (WAREHOUSE, ADMIN)

**Request Body**:
```json
{
  "name": "Nhà cung cấp B",
  "contactPerson": "Trần Văn B",
  "phone": "0987654321",
  "email": "supplierb@example.com",
  "address": "456 Đường XYZ"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Tạo nhà cung cấp thành công",
  "data": { /* supplier object */ }
}
```

---

#### 5.7. GET `/api/inventory/purchase-orders`
**Mô tả**: Lấy danh sách đơn đặt hàng

**Authentication**: Required (WAREHOUSE, ADMIN)

**Query Parameters**:
- `status`: Lọc theo trạng thái (optional)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách đơn đặt hàng",
  "data": [
    {
      "id": 1,
      "poCode": "PO20240101001",
      "supplier": {
        "id": 1,
        "name": "Nhà cung cấp A"
      },
      "status": "COMPLETED",
      "totalAmount": 2500000000,
      "createdAt": "2024-01-01T10:00:00"
    }
  ]
}
```

---

#### 5.8. GET `/api/inventory/purchase-orders/{id}`
**Mô tả**: Lấy chi tiết đơn đặt hàng

**Authentication**: Required (WAREHOUSE, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Chi tiết đơn đặt hàng",
  "data": {
    "id": 1,
    "poCode": "PO20240101001",
    "supplier": { /* supplier details */ },
    "status": "COMPLETED",
    "items": [
      {
        "warehouseProduct": { /* product details */ },
        "quantity": 100,
        "unitPrice": 25000000,
        "subtotal": 2500000000
      }
    ],
    "totalAmount": 2500000000,
    "createdAt": "2024-01-01T10:00:00",
    "completedAt": "2024-01-05T14:00:00"
  }
}
```

---

#### 5.9. GET `/api/inventory/export-orders`
**Mô tả**: Lấy danh sách phiếu xuất kho

**Authentication**: Required (WAREHOUSE, ADMIN)

**Query Parameters**:
- `status`: Lọc theo trạng thái (optional)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách phiếu xuất kho",
  "data": [
    {
      "id": 1,
      "exportCode": "EXP20240101001",
      "exportType": "SALE",
      "status": "COMPLETED",
      "orderId": 1,
      "createdBy": "warehouse@example.com",
      "createdAt": "2024-01-01T15:00:00"
    }
  ]
}
```

---

#### 5.10. GET `/api/inventory/export-orders/{id}`
**Mô tả**: Lấy chi tiết phiếu xuất kho

**Authentication**: Required (WAREHOUSE, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Chi tiết phiếu xuất kho",
  "data": {
    "id": 1,
    "exportCode": "EXP20240101001",
    "exportType": "SALE",
    "status": "COMPLETED",
    "order": { /* order details */ },
    "items": [
      {
        "warehouseProduct": { /* product details */ },
        "quantity": 2,
        "serialNumbers": ["SN001", "SN002"]
      }
    ],
    "createdBy": "warehouse@example.com",
    "createdAt": "2024-01-01T15:00:00"
  }
}
```

---

#### 5.11. PUT `/api/inventory/purchase-orders/{id}/cancel`
**Mô tả**: Hủy đơn đặt hàng

**Authentication**: Required (WAREHOUSE, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã hủy đơn đặt hàng",
  "data": null
}
```

---

#### 5.12. PUT `/api/inventory/export-orders/{id}/cancel`
**Mô tả**: Hủy phiếu xuất kho

**Authentication**: Required (WAREHOUSE, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã hủy phiếu xuất kho",
  "data": null
}
```

---

### Base Path: `/api/inventory/orders`

#### 5.13. GET `/api/inventory/orders/pending-export`
**Mô tả**: Lấy đơn hàng chờ xuất kho

**Authentication**: Required (WAREHOUSE, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách đơn chờ xuất kho",
  "data": [
    {
      "id": 1,
      "orderCode": "ORD20240101001",
      "status": "CONFIRMED",
      "items": [ /* order items */ ],
      "createdAt": "2024-01-01T10:00:00"
    }
  ]
}
```

---

#### 5.14. GET `/api/inventory/orders/exported`
**Mô tả**: Lấy đơn hàng đã xuất kho

**Authentication**: Required (WAREHOUSE, ADMIN)

**Query Parameters**:
- `page`: Số trang (default: 0)
- `size`: Số lượng/trang (default: 20)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách đơn đã xuất kho",
  "data": {
    "content": [ /* orders with READY_TO_SHIP status */ ],
    "totalElements": 50,
    "totalPages": 3
  }
}
```

---


## Module 6: Payment Management

### Base Path: `/api/payment`

#### 6.1. POST `/api/payment/create`
**Mô tả**: Tạo thanh toán mới

**Authentication**: Required (CUSTOMER, ADMIN)

**Request Body**:
```json
{
  "orderId": 1,
  "amount": 59980000,
  "paymentMethod": "BANK_TRANSFER"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Tạo thanh toán thành công",
  "data": {
    "id": 1,
    "paymentCode": "PAY20240101001",
    "amount": 59980000,
    "status": "PENDING",
    "qrCodeUrl": "https://...",
    "expiredAt": "2024-01-01T10:15:00"
  }
}
```

---

#### 6.2. GET `/api/payment/{paymentCode}`
**Mô tả**: Lấy thông tin thanh toán theo mã

**Authentication**: Required (CUSTOMER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Thông tin thanh toán",
  "data": {
    "id": 1,
    "paymentCode": "PAY20240101001",
    "orderId": 1,
    "amount": 59980000,
    "status": "COMPLETED",
    "qrCodeUrl": "https://...",
    "createdAt": "2024-01-01T10:00:00",
    "completedAt": "2024-01-01T10:05:00"
  }
}
```

---

#### 6.3. GET `/api/payment/order/{orderId}`
**Mô tả**: Lấy thanh toán theo orderId

**Authentication**: Required (CUSTOMER, ADMIN)

**Response**: Same as 6.2

---

#### 6.4. GET `/api/payment/{paymentCode}/status`
**Mô tả**: Kiểm tra trạng thái thanh toán (polling)

**Authentication**: None (Public for polling)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Trạng thái thanh toán",
  "data": {
    "paymentCode": "PAY20240101001",
    "status": "COMPLETED",
    "amount": 59980000
  }
}
```

---

#### 6.5. GET `/api/payment/my-payments`
**Mô tả**: Lấy danh sách thanh toán của user

**Authentication**: Required (CUSTOMER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách thanh toán",
  "data": [
    {
      "id": 1,
      "paymentCode": "PAY20240101001",
      "orderId": 1,
      "amount": 59980000,
      "status": "COMPLETED",
      "createdAt": "2024-01-01T10:00:00"
    }
  ]
}
```

---

#### 6.6. POST `/api/payment/sepay/webhook`
**Mô tả**: Webhook từ SePay (callback)

**Authentication**: None (Public - SePay calls this)

**Request Body**:
```json
{
  "content": "PAY20240101001",
  "amount": 59980000,
  "transactionId": "SEPAY123456",
  "accountNumber": "3333315012003",
  "bankCode": "MBBank",
  "status": "SUCCESS"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Webhook processed successfully",
  "data": null
}
```

---

#### 6.7. GET/POST `/api/payment/test-webhook/{paymentCode}`
**Mô tả**: Test webhook manually (development only)

**Authentication**: None (for easy testing)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Test webhook thành công",
  "data": null
}
```

---

#### 6.8. POST `/api/payment/admin/expire-old-payments`
**Mô tả**: Hủy các payment hết hạn (manual trigger)

**Authentication**: Required (ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã xử lý các payment hết hạn",
  "data": null
}
```

---

### Base Path: `/api/admin/bank-accounts`

#### 6.9. GET `/api/admin/bank-accounts`
**Mô tả**: Lấy danh sách tài khoản ngân hàng

**Authentication**: Required (ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách tài khoản ngân hàng",
  "data": [
    {
      "id": 1,
      "bankName": "MBBank",
      "accountNumber": "3333315012003",
      "accountName": "CONG TY ABC",
      "isDefault": true,
      "isActive": true
    }
  ]
}
```

---

#### 6.10. POST `/api/admin/bank-accounts`
**Mô tả**: Tạo tài khoản ngân hàng mới

**Authentication**: Required (ADMIN)

**Request Body**:
```json
{
  "bankName": "VietcomBank",
  "accountNumber": "1234567890",
  "accountName": "CONG TY ABC",
  "sepayApiKey": "xxx",
  "sepayAccountNumber": "xxx"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Tạo tài khoản ngân hàng thành công",
  "data": { /* bank account object */ }
}
```

---

#### 6.11. PUT `/api/admin/bank-accounts/{id}`
**Mô tả**: Cập nhật tài khoản ngân hàng

**Authentication**: Required (ADMIN)

**Request Body**: Same as POST

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Cập nhật tài khoản ngân hàng thành công",
  "data": { /* updated bank account */ }
}
```

---

#### 6.12. DELETE `/api/admin/bank-accounts/{id}`
**Mô tả**: Xóa tài khoản ngân hàng

**Authentication**: Required (ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Xóa tài khoản ngân hàng thành công",
  "data": null
}
```

---

#### 6.13. PUT `/api/admin/bank-accounts/{id}/set-default`
**Mô tả**: Đặt tài khoản mặc định

**Authentication**: Required (ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã đặt tài khoản mặc định",
  "data": null
}
```

---

#### 6.14. PUT `/api/admin/bank-accounts/{id}/toggle-active`
**Mô tả**: Bật/tắt tài khoản ngân hàng

**Authentication**: Required (ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã cập nhật trạng thái",
  "data": null
}
```

---


## Module 7: Shipping Management

### Base Path: `/api/shipping`

#### 7.1. POST `/api/shipping/calculate-fee`
**Mô tả**: Tính phí vận chuyển GHN

**Authentication**: None (Public)

**Request Body**:
```json
{
  "toProvinceId": 201,
  "toDistrictId": 1482,
  "toWardCode": "1A0101",
  "weight": 500,
  "length": 20,
  "width": 15,
  "height": 10,
  "codAmount": 0
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Tính phí thành công",
  "data": {
    "total": 30000,
    "serviceFee": 25000,
    "insuranceFee": 5000,
    "expectedDeliveryTime": "2024-01-03T18:00:00"
  }
}
```

**Error Codes**:
- 400: Thông tin không hợp lệ
- 502: Lỗi từ GHN API

---

#### 7.2. GET `/api/shipping/provinces`
**Mô tả**: Lấy danh sách tỉnh/thành phố từ GHN

**Authentication**: None (Public)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Lấy danh sách tỉnh/thành phố thành công",
  "data": [
    {
      "provinceId": 201,
      "provinceName": "Hà Nội"
    },
    {
      "provinceId": 202,
      "provinceName": "Hồ Chí Minh"
    }
  ]
}
```

---

#### 7.3. GET `/api/shipping/districts/{provinceId}`
**Mô tả**: Lấy danh sách quận/huyện theo tỉnh

**Authentication**: None (Public)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Lấy danh sách quận/huyện thành công",
  "data": [
    {
      "districtId": 1482,
      "districtName": "Quận Hoàn Kiếm"
    }
  ]
}
```

---

#### 7.4. GET `/api/shipping/wards/{districtId}`
**Mô tả**: Lấy danh sách phường/xã theo quận

**Authentication**: None (Public)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Lấy danh sách phường/xã thành công",
  "data": [
    {
      "wardCode": "1A0101",
      "wardName": "Phường Hàng Bạc"
    }
  ]
}
```

---

#### 7.5. GET `/api/shipping/ward-name/{districtId}/{wardCode}`
**Mô tả**: Lấy tên phường/xã theo mã

**Authentication**: None (Public)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Lấy tên phường/xã thành công",
  "data": {
    "wardCode": "1A0101",
    "wardName": "Phường Hàng Bạc"
  }
}
```

---

#### 7.6. POST `/api/shipping/fix-ward-names`
**Mô tả**: Cập nhật tên phường/xã cho tất cả đơn hàng (Admin utility)

**Authentication**: Required (ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã cập nhật tên phường/xã cho tất cả đơn hàng",
  "data": {
    "totalOrders": 100,
    "updatedOrders": 95,
    "failedOrders": 5
  }
}
```

---


## Module 8: Webhook Management

### Base Path: `/api/webhooks`

#### 8.1. POST `/api/webhooks/ghn`
**Mô tả**: Webhook từ GHN (callback khi có cập nhật trạng thái vận chuyển)

**Authentication**: None (Public - GHN calls this)

**Request Body**:
```json
{
  "orderCode": "GHNABCD1234",
  "status": "delivered",
  "statusText": "Đã giao hàng",
  "updatedDate": 1701849600,
  "currentWarehouse": "Kho Hà Nội",
  "description": "Giao hàng thành công",
  "codAmount": 500000,
  "shippingFee": 30000,
  "partnerCode": "ORD20240101001"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Webhook processed successfully",
  "data": null
}
```

**GHN Status Mapping**:
- `ready_to_pick`: Chờ lấy hàng
- `picking`: Đang lấy hàng
- `picked`: Đã lấy hàng
- `storing`: Đang lưu kho
- `transporting`: Đang vận chuyển
- `delivering`: Đang giao hàng
- `delivered`: Đã giao hàng
- `return`: Hoàn trả
- `returned`: Đã hoàn trả
- `cancel`: Đã hủy

**Error Handling**:
- Webhook luôn trả về 200 OK để tránh GHN retry
- Lỗi được log và xử lý nội bộ

---


## Module 9: Accounting Management

### Base Path: `/api/accounting`

#### 9.1. GET `/api/accounting/stats`
**Mô tả**: Lấy thống kê kế toán tổng quan

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Thống kê kế toán",
  "data": {
    "totalRevenue": 500000000,
    "totalExpense": 300000000,
    "netProfit": 200000000,
    "pendingPayables": 50000000,
    "cashBalance": 150000000
  }
}
```

---

#### 9.2. POST `/api/accounting/payment-reconciliation`
**Mô tả**: Đối soát thanh toán

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Request Body**:
```json
{
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "gateway": "SEPAY"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Báo cáo đối soát",
  "data": {
    "totalTransactions": 100,
    "matchedTransactions": 95,
    "unmatchedTransactions": 5,
    "totalAmount": 500000000,
    "discrepancies": [
      {
        "transactionId": "SEPAY123",
        "amount": 1000000,
        "reason": "Không tìm thấy đơn hàng"
      }
    ]
  }
}
```

---

#### 9.3. POST `/api/accounting/payment-reconciliation/import`
**Mô tả**: Import file đối soát từ ngân hàng

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Request Parameters**:
- `file`: MultipartFile (Excel/CSV)
- `gateway`: String (SEPAY, MOMO, etc.)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Import thành công",
  "data": {
    "totalRows": 100,
    "successRows": 95,
    "errorRows": 5
  }
}
```

---

#### 9.4. GET `/api/accounting/shipping-reconciliation`
**Mô tả**: Đối soát phí vận chuyển GHN

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Query Parameters**:
- `startDate`: LocalDate (required)
- `endDate`: LocalDate (required)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Báo cáo đối soát vận chuyển",
  "data": {
    "totalOrders": 100,
    "totalShippingFee": 3000000,
    "totalCODAmount": 50000000,
    "orders": [
      {
        "orderCode": "ORD20240101001",
        "ghnOrderCode": "GHNABCD1234",
        "shippingFee": 30000,
        "codAmount": 500000,
        "status": "DELIVERED"
      }
    ]
  }
}
```

---

#### 9.5. GET `/api/accounting/reports`
**Mô tả**: Lấy báo cáo tài chính

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Query Parameters**:
- `startDate`: LocalDate (required)
- `endDate`: LocalDate (required)
- `viewMode`: String (ORDERS, TRANSACTIONS) - default: ORDERS

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Báo cáo tài chính",
  "data": {
    "period": {
      "startDate": "2024-01-01",
      "endDate": "2024-01-31"
    },
    "revenue": {
      "totalRevenue": 500000000,
      "orderCount": 100
    },
    "expenses": {
      "totalExpense": 300000000,
      "breakdown": {
        "PURCHASE": 250000000,
        "SHIPPING": 30000000,
        "OTHER": 20000000
      }
    },
    "profit": {
      "grossProfit": 200000000,
      "netProfit": 180000000
    }
  }
}
```

---

#### 9.6. GET `/api/accounting/periods`
**Mô tả**: Lấy danh sách kỳ kế toán

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách kỳ kế toán",
  "data": [
    {
      "id": 1,
      "name": "Tháng 1/2024",
      "startDate": "2024-01-01",
      "endDate": "2024-01-31",
      "status": "CLOSED",
      "closedAt": "2024-02-01T10:00:00"
    }
  ]
}
```

---

#### 9.7. POST `/api/accounting/periods/{id}/close`
**Mô tả**: Đóng kỳ kế toán

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã đóng kỳ kế toán",
  "data": null
}
```

**Error Codes**:
- 400: Kỳ đã đóng, có giao dịch chưa hoàn tất

---

#### 9.8. POST `/api/accounting/periods/{id}/reopen`
**Mô tả**: Mở lại kỳ kế toán (Admin only)

**Authentication**: Required (ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã mở lại kỳ kế toán",
  "data": null
}
```

---

#### 9.9. GET `/api/accounting/transactions`
**Mô tả**: Lấy danh sách giao dịch tài chính

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Query Parameters**:
- `page`: int (default: 0)
- `size`: int (default: 20)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách giao dịch",
  "data": {
    "content": [
      {
        "id": 1,
        "transactionCode": "TXN20240101001",
        "type": "REVENUE",
        "category": "ORDER_REVENUE",
        "amount": 500000,
        "description": "Doanh thu đơn hàng ORD20240101001",
        "transactionDate": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 5
  }
}
```

---

#### 9.10. POST `/api/accounting/transactions`
**Mô tả**: Tạo giao dịch tài chính thủ công

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Request Body**:
```json
{
  "type": "EXPENSE",
  "category": "OTHER_EXPENSE",
  "amount": 1000000,
  "description": "Chi phí văn phòng",
  "transactionDate": "2024-01-01T10:00:00"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Tạo giao dịch thành công",
  "data": { /* transaction object */ }
}
```

---

#### 9.11. PUT `/api/accounting/transactions/{id}`
**Mô tả**: Cập nhật giao dịch tài chính

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Request Body**: Same as POST

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Cập nhật giao dịch thành công",
  "data": { /* updated transaction */ }
}
```

---

#### 9.12. DELETE `/api/accounting/transactions/{id}`
**Mô tả**: Xóa giao dịch tài chính

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Xóa giao dịch thành công",
  "data": null
}
```

---

#### 9.13. POST `/api/accounting/reports/profit-loss`
**Mô tả**: Báo cáo lãi/lỗ

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Request Body**:
```json
{
  "startDate": "2024-01-01",
  "endDate": "2024-01-31"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Báo cáo lãi/lỗ",
  "data": {
    "revenue": 500000000,
    "costOfGoodsSold": 300000000,
    "grossProfit": 200000000,
    "operatingExpenses": 50000000,
    "netProfit": 150000000,
    "profitMargin": 30.0
  }
}
```

---

#### 9.14. POST `/api/accounting/reports/cash-flow`
**Mô tả**: Báo cáo dòng tiền

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Request Body**:
```json
{
  "startDate": "2024-01-01",
  "endDate": "2024-01-31"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Báo cáo dòng tiền",
  "data": {
    "openingBalance": 100000000,
    "cashInflow": 500000000,
    "cashOutflow": 300000000,
    "netCashFlow": 200000000,
    "closingBalance": 300000000
  }
}
```

---

#### 9.15. GET `/api/accounting/tax/reports`
**Mô tả**: Lấy danh sách báo cáo thuế

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách báo cáo thuế",
  "data": [
    {
      "id": 1,
      "taxType": "VAT",
      "period": "2024-01",
      "taxableAmount": 500000000,
      "taxAmount": 50000000,
      "status": "SUBMITTED",
      "dueDate": "2024-02-20"
    }
  ]
}
```

---

#### 9.16. POST `/api/accounting/tax/reports`
**Mô tả**: Tạo báo cáo thuế

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Request Body**:
```json
{
  "taxType": "VAT",
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "dueDate": "2024-02-20"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Tạo báo cáo thuế thành công",
  "data": { /* tax report object */ }
}
```

---

#### 9.17. POST `/api/accounting/tax/reports/{id}/submit`
**Mô tả**: Nộp báo cáo thuế

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đã nộp báo cáo thuế",
  "data": null
}
```

---


### Base Path: `/api/accounting/payables`

#### 9.18. GET `/api/accounting/payables`
**Mô tả**: Lấy tất cả công nợ nhà cung cấp

**Authentication**: Required (ADMIN, ACCOUNTANT, WAREHOUSE_MANAGER)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Danh sách công nợ",
  "data": [
    {
      "id": 1,
      "payableCode": "PAY20240101001",
      "supplier": {
        "id": 1,
        "name": "Nhà cung cấp A"
      },
      "totalAmount": 250000000,
      "paidAmount": 100000000,
      "remainingAmount": 150000000,
      "status": "PARTIAL_PAID",
      "invoiceDate": "2024-01-01",
      "dueDate": "2024-02-01"
    }
  ]
}
```

---

#### 9.19. GET `/api/accounting/payables/{id}`
**Mô tả**: Lấy chi tiết công nợ

**Authentication**: Required (ADMIN, ACCOUNTANT, WAREHOUSE_MANAGER)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Chi tiết công nợ",
  "data": {
    "id": 1,
    "payableCode": "PAY20240101001",
    "supplier": { /* supplier details */ },
    "purchaseOrder": { /* PO details */ },
    "totalAmount": 250000000,
    "paidAmount": 100000000,
    "remainingAmount": 150000000,
    "status": "PARTIAL_PAID",
    "invoiceDate": "2024-01-01",
    "dueDate": "2024-02-01",
    "payments": [
      {
        "id": 1,
        "amount": 100000000,
        "paymentDate": "2024-01-15",
        "note": "Thanh toán đợt 1"
      }
    ]
  }
}
```

---

#### 9.20. GET `/api/accounting/payables/supplier/{supplierId}`
**Mô tả**: Lấy công nợ theo nhà cung cấp

**Authentication**: Required (ADMIN, ACCOUNTANT, WAREHOUSE_MANAGER)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Công nợ nhà cung cấp",
  "data": [
    { /* payable objects */ }
  ]
}
```

---

#### 9.21. GET `/api/accounting/payables/overdue`
**Mô tả**: Lấy công nợ quá hạn

**Authentication**: Required (ADMIN, ACCOUNTANT, WAREHOUSE_MANAGER)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Công nợ quá hạn",
  "data": [
    {
      "id": 1,
      "payableCode": "PAY20240101001",
      "supplier": { /* supplier */ },
      "remainingAmount": 150000000,
      "dueDate": "2024-02-01",
      "daysOverdue": 15
    }
  ]
}
```

---

#### 9.22. GET `/api/accounting/payables/upcoming`
**Mô tả**: Lấy công nợ sắp đến hạn

**Authentication**: Required (ADMIN, ACCOUNTANT, WAREHOUSE_MANAGER)

**Query Parameters**:
- `days`: int (default: 7) - số ngày sắp đến hạn

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Công nợ sắp đến hạn",
  "data": [
    {
      "id": 2,
      "payableCode": "PAY20240102001",
      "supplier": { /* supplier */ },
      "remainingAmount": 200000000,
      "dueDate": "2024-01-25",
      "daysUntilDue": 5
    }
  ]
}
```

---

#### 9.23. POST `/api/accounting/payables/payments`
**Mô tả**: Thanh toán công nợ

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Request Body**:
```json
{
  "payableId": 1,
  "amount": 50000000,
  "paymentDate": "2024-01-20",
  "paymentMethod": "BANK_TRANSFER",
  "note": "Thanh toán đợt 2"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Thanh toán thành công",
  "data": {
    "payment": { /* payment object */ },
    "updatedPayable": { /* updated payable */ }
  }
}
```

**Error Codes**:
- 400: Số tiền vượt quá công nợ còn lại
- 404: Không tìm thấy công nợ

---

#### 9.24. GET `/api/accounting/payables/{payableId}/payments`
**Mô tả**: Lấy lịch sử thanh toán công nợ

**Authentication**: Required (ADMIN, ACCOUNTANT, WAREHOUSE_MANAGER)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Lịch sử thanh toán",
  "data": [
    {
      "id": 1,
      "amount": 100000000,
      "paymentDate": "2024-01-15",
      "paymentMethod": "BANK_TRANSFER",
      "note": "Thanh toán đợt 1"
    }
  ]
}
```

---

#### 9.25. GET `/api/accounting/payables/stats`
**Mô tả**: Thống kê công nợ

**Authentication**: Required (ADMIN, ACCOUNTANT, WAREHOUSE_MANAGER)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Thống kê công nợ",
  "data": {
    "totalPayables": 500000000,
    "totalPaid": 200000000,
    "totalRemaining": 300000000,
    "overdueAmount": 50000000,
    "upcomingAmount": 100000000,
    "bySupplier": [
      {
        "supplierId": 1,
        "supplierName": "Nhà cung cấp A",
        "totalAmount": 250000000,
        "remainingAmount": 150000000
      }
    ]
  }
}
```

---

#### 9.26. GET `/api/accounting/payables/report`
**Mô tả**: Báo cáo công nợ theo khoảng thời gian

**Authentication**: Required (ADMIN, ACCOUNTANT, WAREHOUSE_MANAGER)

**Query Parameters**:
- `startDate`: LocalDate (required)
- `endDate`: LocalDate (required)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Báo cáo công nợ",
  "data": {
    "period": {
      "startDate": "2024-01-01",
      "endDate": "2024-01-31"
    },
    "summary": {
      "totalPayables": 500000000,
      "totalPaid": 200000000,
      "totalRemaining": 300000000
    },
    "details": [ /* payable details */ ]
  }
}
```

---

### Base Path: `/api/accounting/financial-statement`

#### 9.27. GET `/api/accounting/financial-statement`
**Mô tả**: Lấy báo cáo tài chính tổng hợp

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Query Parameters**:
- `startDate`: LocalDate (required)
- `endDate`: LocalDate (required)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Báo cáo tài chính",
  "data": {
    "period": {
      "startDate": "2024-01-01",
      "endDate": "2024-01-31"
    },
    "incomeStatement": {
      "revenue": 500000000,
      "costOfGoodsSold": 300000000,
      "grossProfit": 200000000,
      "operatingExpenses": 50000000,
      "netIncome": 150000000
    },
    "balanceSheet": {
      "assets": 1000000000,
      "liabilities": 300000000,
      "equity": 700000000
    },
    "cashFlow": {
      "operatingActivities": 150000000,
      "investingActivities": -50000000,
      "financingActivities": 0,
      "netCashFlow": 100000000
    }
  }
}
```

**Error Codes**:
- 400: Ngày bắt đầu phải trước ngày kết thúc, khoảng thời gian không được vượt quá 1 năm

---

#### 9.28. GET `/api/accounting/financial-statement/revenue`
**Mô tả**: Báo cáo doanh thu

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Query Parameters**: Same as 9.27

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Báo cáo doanh thu",
  "data": {
    "totalRevenue": 500000000,
    "orderCount": 100,
    "averageOrderValue": 5000000,
    "byCategory": [ /* revenue by category */ ],
    "byDay": [ /* daily revenue */ ]
  }
}
```

---

#### 9.29. GET `/api/accounting/financial-statement/expenses`
**Mô tả**: Báo cáo chi phí

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Query Parameters**: Same as 9.27

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Báo cáo chi phí",
  "data": {
    "totalExpenses": 300000000,
    "byCategory": {
      "PURCHASE": 250000000,
      "SHIPPING": 30000000,
      "OTHER": 20000000
    }
  }
}
```

---

#### 9.30. GET `/api/accounting/financial-statement/profit`
**Mô tả**: Báo cáo lợi nhuận

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Query Parameters**: Same as 9.27

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Báo cáo lợi nhuận",
  "data": {
    "revenue": 500000000,
    "expenses": 300000000,
    "grossProfit": 200000000,
    "netProfit": 150000000,
    "profitMargin": 30.0
  }
}
```

---

#### 9.31. GET `/api/accounting/financial-statement/dashboard`
**Mô tả**: Dashboard tổng quan (tháng hiện tại)

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Dashboard tổng quan",
  "data": {
    "currentMonth": {
      "revenue": 500000000,
      "expenses": 300000000,
      "profit": 200000000
    },
    "comparison": {
      "revenueGrowth": 15.5,
      "expenseGrowth": 10.2,
      "profitGrowth": 25.3
    }
  }
}
```

---

#### 9.32. GET `/api/accounting/financial-statement/monthly/{year}/{month}`
**Mô tả**: Báo cáo theo tháng

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Path Parameters**:
- `year`: int (2000 - current year + 1)
- `month`: int (1-12)

**Response**: Same as 9.27

---

#### 9.33. GET `/api/accounting/financial-statement/quarterly/{year}/{quarter}`
**Mô tả**: Báo cáo theo quý

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Path Parameters**:
- `year`: int (2000 - current year + 1)
- `quarter`: int (1-4)

**Response**: Same as 9.27

---

#### 9.34. GET `/api/accounting/financial-statement/yearly/{year}`
**Mô tả**: Báo cáo theo năm

**Authentication**: Required (ADMIN, ACCOUNTANT)

**Path Parameters**:
- `year`: int (2000 - current year + 1)

**Response**: Same as 9.27

---


## Module 10: File Management

### Base Path: `/api/files`

#### 10.1. POST `/api/files/upload`
**Mô tả**: Upload ảnh lên Cloudinary

**Authentication**: Required (PRODUCT_MANAGER, ADMIN)

**Request Parameters**:
- `file`: MultipartFile (image file)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Upload thành công",
  "data": "https://res.cloudinary.com/xxx/image/upload/v123/product.jpg"
}
```

**Error Codes**:
- 400: File không hợp lệ, kích thước quá lớn
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập
- 500: Lỗi upload

---

#### 10.2. POST `/api/files/upload-local`
**Mô tả**: Upload ảnh lên server local (backup method)

**Authentication**: Required (PRODUCT_MANAGER, ADMIN)

**Request Parameters**:
- `file`: MultipartFile (image file)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Upload thành công",
  "data": "/api/files/abc123.jpg"
}
```

---

#### 10.3. GET `/api/files/{filename}`
**Mô tả**: Lấy ảnh từ server local

**Authentication**: None (Public)

**Response**: Image file (binary)

**Error Codes**:
- 404: File không tồn tại

---

#### 10.4. DELETE `/api/files/{filename}`
**Mô tả**: Xóa ảnh từ server local

**Authentication**: Required (PRODUCT_MANAGER, ADMIN)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Xóa file thành công",
  "data": null
}
```

---


## Module 11: Dashboard & Statistics

### Base Path: `/api/dashboard`

#### 11.1. GET `/api/dashboard/stats`
**Mô tả**: Lấy thống kê dashboard tổng quan

**Authentication**: Required (ADMIN, EMPLOYEE)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Thống kê dashboard",
  "data": {
    "totalOrders": 1000,
    "totalRevenue": 500000000,
    "totalCustomers": 500,
    "pendingOrders": 50,
    "todayOrders": 20,
    "todayRevenue": 10000000,
    "monthlyRevenue": 150000000,
    "topProducts": [
      {
        "productId": 1,
        "productName": "iPhone 15 Pro Max",
        "soldQuantity": 50,
        "revenue": 150000000
      }
    ]
  }
}
```

**Error Codes**:
- 401: Chưa đăng nhập
- 403: Không có quyền truy cập

---

#### 11.2. GET `/api/dashboard/recent-orders`
**Mô tả**: Lấy danh sách đơn hàng gần đây

**Authentication**: Required (ADMIN, EMPLOYEE)

**Query Parameters**:
- `limit`: int (default: 10) - số lượng đơn hàng

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Đơn hàng gần đây",
  "data": [
    {
      "id": 1,
      "orderCode": "ORD20240101001",
      "customerName": "Nguyễn Văn A",
      "total": 59980000,
      "status": "SHIPPING",
      "createdAt": "2024-01-01T10:00:00"
    }
  ]
}
```

---


## Tổng Kết API Endpoints

### Thống Kê Tổng Quan

| Module | Số lượng Endpoints | Authentication Required |
|--------|-------------------|------------------------|
| Authentication & Authorization | 12 | Mixed (Public + Auth) |
| Product Management | 18 | Mixed |
| Cart Management | 5 | Required |
| Order Management | 13 | Required |
| Inventory Management | 14 | Required |
| Payment Management | 14 | Mixed |
| Shipping Management | 6 | Mixed |
| Webhook Management | 1 | Public |
| Accounting Management | 34 | Required |
| File Management | 4 | Mixed |
| Dashboard & Statistics | 2 | Required |
| **TỔNG CỘNG** | **123** | - |

---

### Phân Loại Theo Authentication

#### Public Endpoints (Không cần đăng nhập)
- Đăng ký, đăng nhập
- Xem sản phẩm, danh mục
- Tính phí vận chuyển
- Lấy danh sách tỉnh/quận/phường
- Webhook callbacks (GHN, SePay)
- Kiểm tra trạng thái thanh toán (polling)

**Tổng: ~25 endpoints**

#### Customer Endpoints
- Quản lý giỏ hàng
- Đặt hàng
- Xem đơn hàng của mình
- Thanh toán
- Cập nhật profile

**Tổng: ~15 endpoints**

#### Employee Endpoints

**Warehouse Staff**:
- Quản lý kho (nhập/xuất)
- Quản lý nhà cung cấp
- Xem tồn kho
- Xem đơn hàng cần xuất kho

**Sales Staff**:
- Xem và xử lý đơn hàng
- Cập nhật trạng thái đơn hàng
- Thống kê đơn hàng

**Product Manager**:
- Quản lý sản phẩm
- Quản lý danh mục
- Đăng bán sản phẩm từ kho
- Upload ảnh

**Accountant**:
- Xem báo cáo tài chính
- Quản lý công nợ
- Đối soát thanh toán
- Quản lý thuế

**Tổng: ~60 endpoints**

#### Admin Endpoints
- Tất cả endpoints của Employee
- Quản lý nhân viên
- Quản lý tài khoản ngân hàng
- Đóng/mở kỳ kế toán
- Xóa dữ liệu

**Tổng: ~80 endpoints (bao gồm cả Employee)**

---

### Error Handling Strategy

#### Standard Error Response Format
```json
{
  "success": false,
  "message": "Mô tả lỗi chi tiết",
  "data": null
}
```

#### Common Error Scenarios

**Validation Errors (400)**:
- Dữ liệu không hợp lệ
- Thiếu trường bắt buộc
- Format không đúng
- Business rule violation

**Authentication Errors (401)**:
- Token không hợp lệ
- Token hết hạn
- Chưa đăng nhập

**Authorization Errors (403)**:
- Không có quyền truy cập endpoint
- Không có quyền thao tác trên resource

**Not Found Errors (404)**:
- Resource không tồn tại
- Endpoint không tồn tại

**Server Errors (500)**:
- Lỗi database
- Lỗi external API
- Lỗi không xác định

**Service Unavailable (503)**:
- External service timeout
- Database connection lost

---

### Rate Limiting & Security

#### Rate Limiting
- **Public endpoints**: 100 requests/minute/IP
- **Authenticated endpoints**: 1000 requests/minute/user
- **Webhook endpoints**: No limit (trusted sources)

#### Security Measures
1. **JWT Authentication**: Token-based auth với expiration
2. **CORS**: Configured cho frontend domain
3. **SQL Injection Prevention**: Sử dụng JPA/Hibernate
4. **XSS Protection**: Input sanitization
5. **HTTPS**: Required trong production
6. **Webhook Signature Verification**: Cho GHN và SePay webhooks

---

### API Versioning Strategy

Hiện tại hệ thống chưa implement versioning. Khi cần thiết, có thể áp dụng:

**URL Versioning** (Recommended):
```
/api/v1/products
/api/v2/products
```

**Header Versioning**:
```
Accept: application/vnd.api+json;version=1
```

---

### Performance Optimization

#### Caching Strategy
- **Product List**: Cache 5 phút
- **Category Tree**: Cache 10 phút
- **Province/District/Ward**: Cache 1 giờ
- **User Profile**: Cache 1 phút

#### Pagination
- Default page size: 20
- Max page size: 100
- Sử dụng Spring Data Pageable

#### Database Optimization
- Indexes trên các trường thường query
- Lazy loading cho relationships
- Connection pooling
- Query optimization

---

### Testing Strategy

#### Unit Tests
- Service layer logic
- Validation logic
- Business rules

#### Integration Tests
- Controller endpoints
- Database operations
- External API calls

#### API Testing Tools
- Postman collections
- HTTP files (IntelliJ)
- Automated test scripts

---

### Documentation & Tools

#### API Documentation
- Swagger/OpenAPI: `http://localhost:8080/swagger-ui.html`
- Postman Collection: Available in project
- HTTP Test Files: Available in project root

#### Monitoring & Logging
- Application logs: SLF4J + Logback
- Error tracking: Console logs
- Performance monitoring: Spring Actuator (if enabled)

---

## Kết Luận

Hệ thống API được thiết kế với:
- **123 endpoints** phục vụ đầy đủ các chức năng
- **RESTful design** chuẩn
- **JWT authentication** bảo mật
- **Role-based access control** phân quyền chi tiết
- **Consistent response format** dễ sử dụng
- **Comprehensive error handling** rõ ràng
- **External API integration** (GHN, SePay, Cloudinary)
- **Event-driven accounting** tự động hóa

API được tổ chức theo module rõ ràng, dễ bảo trì và mở rộng trong tương lai.

