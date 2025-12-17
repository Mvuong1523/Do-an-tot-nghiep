# Deploy Frontend Next.js

## 1. Chuẩn bị code

```bash
# Trên server
cd /opt
git clone https://github.com/your-repo.git frontend
cd frontend/src/frontend

# Hoặc upload từ local
scp -r src/frontend root@your-server-ip:/opt/
```

## 2. Tạo .env.production

```bash
nano .env.production
```

```env
# API URL (Backend)
NEXT_PUBLIC_API_URL=https://api.yourdomain.com

# Hoặc nếu cùng domain
NEXT_PUBLIC_API_URL=https://yourdomain.com/api
```

## 3. Build và chạy

### Option A: Build và chạy với PM2 

```bash
# Install dependencies
npm install

# Build
npm run build

# Tạo PM2 config
nano ecosystem.config.js
```

```javascript
module.exports = {
  apps: [{
    name: 'frontend',
    script: 'npm',
    args: 'start',
    cwd: '/opt/frontend/src/frontend',
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '500M',
    env: {
      NODE_ENV: 'production',
      PORT: 3000
    }
  }]
}
```

```bash
# Start
pm2 start ecosystem.config.js
pm2 save

# Check
pm2 logs frontend
pm2 status
```

### Option B: Chạy trực tiếp

```bash
npm run build
npm start
```

## 4. Test frontend

```bash
curl http://localhost:3000
```
