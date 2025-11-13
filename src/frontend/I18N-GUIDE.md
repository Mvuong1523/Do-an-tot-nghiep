# 🌍 Hướng dẫn sử dụng tính năng đa ngôn ngữ

## ✅ Tính năng đã hoàn thành

### 🎯 **Hệ thống đa ngôn ngữ hoàn chỉnh:**

1. **Language Store với Zustand**:
   - Quản lý ngôn ngữ hiện tại (`vi` | `en`)
   - Persistent storage (lưu khi refresh)
   - Type-safe với TypeScript

2. **Translation System**:
   - File translations đầy đủ cho tiếng Việt và tiếng Anh
   - Hook `useTranslation()` dễ sử dụng
   - Type-safe translation keys

3. **Dropdown ngôn ngữ trong Header**:
   - Hiển thị ngôn ngữ hiện tại
   - Dropdown với flag và tên ngôn ngữ
   - Visual indicator cho ngôn ngữ được chọn
   - Smooth animation khi thay đổi

4. **Real-time Language Switching**:
   - Thay đổi ngôn ngữ ngay lập tức
   - Tất cả components cập nhật tự động
   - Persistent across page refreshes

## 🚀 Cách sử dụng

### **Thay đổi ngôn ngữ:**
1. Click vào dropdown ngôn ngữ ở header
2. Chọn "Tiếng Việt" hoặc "English"
3. Toàn bộ website sẽ chuyển đổi ngôn ngữ ngay lập tức

### **Sử dụng trong code:**
```typescript
import { useTranslation } from '@/hooks/useTranslation'

function MyComponent() {
  const { t } = useTranslation()
  
  return (
    <div>
      <h1>{t('home')}</h1>
      <p>{t('searchPlaceholder')}</p>
    </div>
  )
}
```

## 📁 Cấu trúc files mới

```
src/frontend/
├── store/
│   └── languageStore.ts      # Zustand store cho ngôn ngữ
├── translations/
│   └── index.ts              # File translations đầy đủ
├── hooks/
│   └── useTranslation.ts     # Hook để sử dụng translations
└── components/
    └── layout/
        └── Header.tsx         # Dropdown ngôn ngữ
```

## 🔧 Technical Implementation

### **Language Store (Zustand)**
```typescript
interface LanguageStore {
  currentLanguage: Language    // 'vi' | 'en'
  setLanguage: (lang) => void // Thay đổi ngôn ngữ
}
```

### **Translation System**
```typescript
// translations/index.ts
export const translations = {
  vi: { home: 'Trang chủ', ... },
  en: { home: 'Home', ... }
}

// hooks/useTranslation.ts
export const useTranslation = () => {
  const { currentLanguage } = useLanguageStore()
  const t = (key: TranslationKey) => translations[currentLanguage][key]
  return { t, currentLanguage }
}
```

### **Header Integration**
```typescript
// components/layout/Header.tsx
const { currentLanguage, setLanguage } = useLanguageStore()
const { t } = useTranslation()

// Dropdown với visual feedback
<button onClick={() => setLanguage('en')}>
  {currentLanguage === 'en' ? '✓' : ''}
</button>
```

## 🌐 Supported Languages

### **Tiếng Việt (vi)**
- ✅ Header navigation
- ✅ Product cards
- ✅ Cart page
- ✅ Wishlist page
- ✅ Contact page
- ✅ Toast notifications
- ✅ Form labels & placeholders

### **English (en)**
- ✅ Header navigation
- ✅ Product cards
- ✅ Cart page
- ✅ Wishlist page
- ✅ Contact page
- ✅ Toast notifications
- ✅ Form labels & placeholders

## 📝 Translation Keys

### **Header & Navigation**
- `home`, `product1s`, `contact`
- `wishlist`, `cart`, `login`
- `searchPlaceholder`

### **Product & Cart**
- `addToCart`, `addToWishlist`, `removeFromWishlist`
- `cartEmpty`, `wishlistEmpty`
- `subtotal`, `total`, `discount`

### **Contact & Forms**
- `contactUs`, `sendMessage`
- `fullName`, `email`, `phone`
- `support`, `sales`, `warranty`

### **Notifications**
- `addedToCart`, `removedFromWishlist`
- `updatedQuantity`, `removedFromCart`

## 🎨 UI/UX Features

### **Language Dropdown**
- Clean design với flag icons
- Visual indicator cho ngôn ngữ hiện tại
- Smooth hover animations
- Mobile responsive

### **Real-time Updates**
- Instant language switching
- No page reload required
- Persistent across sessions
- Smooth transitions

## 🔄 State Management

### **Persistent Storage**
- Language preference saved in localStorage
- Restored on page refresh
- Sync across browser tabs

### **Type Safety**
- TypeScript support
- Compile-time translation key validation
- IntelliSense for translation keys

## 🚀 Advanced Features

### **Easy Extension**
```typescript
// Thêm ngôn ngữ mới
export type Language = 'vi' | 'en' | 'ja' | 'ko'

// Thêm translations
export const translations = {
  vi: { ... },
  en: { ... },
  ja: { ... },  // Japanese
  ko: { ... }   // Korean
}
```

### **Dynamic Content**
- Product names (có thể mở rộng)
- Dynamic pricing formats
- Date/time localization

## 📱 Mobile Support

- Responsive dropdown design
- Touch-friendly language selection
- Optimized for mobile screens

## 🔧 Development Tips

### **Adding New Translations**
1. Add key to `TranslationKey` type
2. Add Vietnamese translation in `translations.vi`
3. Add English translation in `translations.en`
4. Use `t('newKey')` in components

### **Testing Language Switching**
1. Open browser dev tools
2. Check localStorage for `language-storage`
3. Verify language persistence across refreshes

## 🎯 Next Steps

1. **Add more languages**: Japanese, Korean, Chinese
2. **RTL support**: Arabic, Hebrew
3. **Dynamic content**: Product descriptions, blog posts
4. **SEO optimization**: Language-specific URLs
5. **Analytics**: Track language preferences

## 📞 Support

Nếu có vấn đề với tính năng đa ngôn ngữ:
1. Check browser console for errors
2. Verify localStorage data
3. Test language switching functionality
4. Check translation key existence

**Tech World** - Đa ngôn ngữ, đa văn hóa! 🌍🚀
