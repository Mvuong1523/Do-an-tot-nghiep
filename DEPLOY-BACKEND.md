# Deploy Backend Spring Boot

## 1. Build JAR file trên local

```bash
# Trong thư mục backend
./mvnw clean package -DskipTests

# File JAR sẽ ở: target/WEB_TMDT-0.0.1-SNAPSHOT.jar
```

## 2. Upload lên server

```bash
# Từ máy local
scp target/WEB_TMDT-0.0.1-SNAPSHOT.jar root@your-server-ip:/opt/backend/

# Hoặc dùng Git
ssh root@your-server-ip
cd /opt
git clone https://github.com/your-repo.git backend
cd backend
./mvnw clean package -DskipTests
```

## 3. Tạo application-prod.properties

```bash
# Trên server
cd /opt/backend
nano src/main/resources/application-prod.properties
```

```properties
# Database Production
spring.datasource.url=jdbc:mysql://localhost:3306/web3?createDatabaseIfNotExist=true
spring.datasource.username=web3user
spring.datasource.password=your-strong-password
spring.jpa.hibernate.ddl-auto=update

# Server
server.port=8080

# JWT
app.jwt.secret=CHANGE_THIS_TO_VERY_LONG_RANDOM_STRING_FOR_PRODUCTION
app.jwt.expiration-ms=86400000

# GHN API (Production)
ghn.api.url=https://online-gateway.ghn.vn/shiip/public-api
ghn.api.token=YOUR_PRODUCTION_GHN_TOKEN
ghn.shop.id=YOUR_SHOP_ID

# SePay (Production)
sepay.merchant-id=YOUR_PRODUCTION_MERCHANT_ID
sepay.secret-key=YOUR_PRODUCTION_SECRET_KEY
sepay.api-url=https://api.sepay.vn/v2/payment-qr

# Cloudinary
cloudinary.url=cloudinary://YOUR_CLOUDINARY_URL

# Logging
logging.level.root=INFO
logging.level.com.doan.WEB_TMDT=INFO
```

## 4. Chạy với PM2

```bash
# Tạo file ecosystem
nano ecosystem.config.js
```

```javascript
module.exports = {
  apps: [{
    name: 'backend',
    script: 'java',
    args: '-jar target/WEB_TMDT-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod',
    cwd: '/opt/backend',
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '1G',
    env: {
      NODE_ENV: 'production'
    }
  }]
}
```

```bash
# Start backend
pm2 start ecosystem.config.js
pm2 save
pm2 startup

# Check logs
pm2 logs backend
pm2 status
```

## 5. Test backend

```bash
curl http://localhost:8080/api/products
```
