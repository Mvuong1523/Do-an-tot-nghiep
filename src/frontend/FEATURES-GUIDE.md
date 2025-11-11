# Hướng dẫn sử dụng Tech World Frontend

## 🎯 Tính năng mới đã cập nhật

### 1. **Header mới theo thiết kế**
- ✅ Layout horizontal với logo, search bar, navigation links
- ✅ Buttons: Yêu thích, Giỏ hàng, Đăng nhập
- ✅ Dropdown ngôn ngữ (Tiếng Việt/English)
- ✅ Cart counter hiển thị số lượng sản phẩm real-time
- ✅ Search bar với nút xóa
- ✅ Mobile responsive

### 2. **Tính năng Wishlist (Yêu thích)**
- ✅ Thêm/bỏ yêu thích sản phẩm
- ✅ Trang danh sách yêu thích (`/wishlist`)
- ✅ Lưu trữ persistent với Zustand
- ✅ Heart icon với animation

### 3. **Cart Management (Quản lý giỏ hàng)**
- ✅ State management với Zustand
- ✅ Thêm sản phẩm vào giỏ hàng
- ✅ Cập nhật số lượng real-time
- ✅ Xóa sản phẩm khỏi giỏ hàng
- ✅ Tính toán tổng tiền tự động
- ✅ Persistent storage (lưu khi refresh)

### 4. **Trang liên hệ**
- ✅ Form liên hệ đầy đủ
- ✅ Thông tin công ty
- ✅ FAQ section
- ✅ Responsive design

## 🚀 Cách sử dụng

### **Thêm sản phẩm vào giỏ hàng:**
1. Click nút "Thêm vào giỏ" trên ProductCard
2. Hoặc click icon giỏ hàng khi hover
3. Giỏ hàng sẽ cập nhật ngay lập tức
4. Counter trên header sẽ hiển thị số lượng

### **Thêm sản phẩm vào yêu thích:**
1. Click icon trái tim trên ProductCard
2. Hoặc click icon trái tim khi hover
3. Sản phẩm sẽ được lưu vào wishlist
4. Truy cập `/wishlist` để xem danh sách

### **Quản lý giỏ hàng:**
1. Truy cập `/cart` để xem giỏ hàng
2. Thay đổi số lượng bằng nút +/- 
3. Xóa sản phẩm bằng nút trash
4. Tổng tiền sẽ cập nhật tự động

## 📁 Cấu trúc files mới

```
src/frontend/
├── store/
│   └── cartStore.ts          # Zustand store cho cart & wishlist
├── app/
│   ├── wishlist/
│   │   └── page.tsx          # Trang danh sách yêu thích
│   └── contact/
│       └── page.tsx          # Trang liên hệ
├── components/
│   ├── layout/
│   │   └── Header.tsx        # Header mới với cart counter
│   └── product/
│       └── ProductCard.tsx   # Tích hợp wishlist & cart
```

## 🔧 State Management

### **Cart Store (Zustand)**
```typescript
interface CartStore {
  items: CartItem[]           // Sản phẩm trong giỏ
  wishlist: number[]          // IDs sản phẩm yêu thích
  addToCart: (product) => void
  removeFromCart: (id) => void
  updateQuantity: (id, qty) => void
  addToWishlist: (id) => void
  removeFromWishlist: (id) => void
  getCartCount: () => number
  getCartTotal: () => number
}
```

### **Persistent Storage**
- Cart và wishlist được lưu trong localStorage
- Tự động restore khi refresh trang
- Sync giữa các tab/window

## 🎨 UI/UX Improvements

### **Header Design**
- Clean horizontal layout
- Purple accent colors
- Smooth hover animations
- Mobile-first responsive

### **Cart Counter**
- Real-time updates
- Badge với số lượng
- Smooth animations

### **Wishlist Integration**
- Heart icon với fill animation
- Toast notifications
- Persistent state

## 📱 Responsive Design

- **Desktop**: Full horizontal layout
- **Tablet**: Collapsed navigation
- **Mobile**: Hamburger menu + stacked layout

## 🔄 Real-time Updates

- Cart counter updates instantly
- Wishlist state persists across pages
- Toast notifications for user feedback
- Smooth animations and transitions

## 🚀 Next Steps

1. **Tích hợp API**: Thay thế mock data bằng real API calls
2. **Authentication**: Thêm user login/logout
3. **Payment**: Tích hợp payment gateway
4. **Search**: Implement search functionality
5. **Filters**: Advanced product filtering

## 📞 Support

Nếu có vấn đề gì, hãy kiểm tra:
1. Console errors trong browser
2. Network requests
3. LocalStorage data
4. Component state

**Tech World Frontend** - Công nghệ hàng đầu! 🚀
