# Module Support Frontend - Thiết kế Chi tiết

## 🎯 Tổng quan
Module Support được thiết kế để cung cấp trải nghiệm hỗ trợ khách hàng toàn diện với giao diện hiện đại, thân thiện và dễ sử dụng.

## 📁 Cấu trúc thư mục

```
src/frontend/app/support/
├── page.tsx                     # Trang chủ Support Hub
├── tickets/
│   ├── page.tsx                 # Danh sách tickets
│   ├── create/page.tsx          # Tạo ticket mới
│   └── [id]/page.tsx           # Chi tiết ticket
├── knowledge-base/
│   ├── page.tsx                 # Trang chủ Knowledge Base
│   ├── [category]/page.tsx      # Danh mục bài viết
│   └── article/[id]/page.tsx    # Chi tiết bài viết
├── live-chat/
│   └── page.tsx                 # Live chat interface
├── warranty/
│   ├── page.tsx                 # Tra cứu bảo hành
│   └── check/page.tsx          # Kiểm tra bảo hành
├── repair/
│   ├── page.tsx                 # Dịch vụ sửa chữa
│   └── booking/page.tsx        # Đặt lịch sửa chữa
└── contact/
    └── page.tsx                 # Thông tin liên hệ

src/frontend/components/support/
├── SupportHub.tsx               # Component trang chủ
├── TicketSystem/
│   ├── TicketList.tsx
│   ├── TicketCard.tsx
│   ├── TicketForm.tsx
│   ├── TicketDetail.tsx
│   └── TicketChat.tsx
├── KnowledgeBase/
│   ├── SearchBar.tsx
│   ├── CategoryGrid.tsx
│   ├── ArticleCard.tsx
│   ├── ArticleDetail.tsx
│   └── RelatedArticles.tsx
├── LiveChat/
│   ├── ChatWindow.tsx
│   ├── ChatMessage.tsx
│   ├── ChatInput.tsx
│   └── ChatHeader.tsx
├── Warranty/
│   ├── WarrantyChecker.tsx
│   ├── WarrantyStatus.tsx
│   └── WarrantyHistory.tsx
├── Repair/
│   ├── RepairBooking.tsx
│   ├── RepairStatus.tsx
│   └── RepairHistory.tsx
└── Common/
    ├── SupportCard.tsx
    ├── StatusBadge.tsx
    ├── PriorityBadge.tsx
    ├── FileUpload.tsx
    └── RatingStars.tsx
```

## 🎨 Design System

### Color Palette
- **Primary**: Navy blue (#4338ca) - Chính
- **Secondary**: Gray (#64748b) - Phụ
- **Success**: Green (#10b981) - Thành công
- **Warning**: Orange (#f59e0b) - Cảnh báo
- **Error**: Red (#ef4444) - Lỗi
- **Info**: Blue (#3b82f6) - Thông tin

### Typography
- **Heading**: Inter font, font-bold
- **Body**: Inter font, font-normal
- **Caption**: Inter font, font-medium, text-sm

### Components Style
- **Cards**: Rounded-lg, shadow-sm, hover:shadow-md
- **Buttons**: Rounded-md, transition-colors
- **Inputs**: Rounded-md, border-gray-300, focus:ring-2
- **Badges**: Rounded-full, px-3 py-1, text-xs font-medium

## 🚀 Tính năng chính

### 1. Support Hub (Trang chủ)
- Dashboard tổng quan
- Quick actions
- Recent tickets
- Popular articles
- Live chat widget

### 2. Ticket System
- Tạo ticket mới
- Danh sách tickets
- Chat real-time
- File attachments
- Priority levels
- Status tracking

### 3. Knowledge Base
- Search functionality
- Category browsing
- Article rating
- Related articles
- Popular articles

### 4. Live Chat
- Real-time messaging
- File sharing
- Typing indicators
- Agent availability
- Chat history

### 5. Warranty Check
- Serial number lookup
- Warranty status
- Warranty history
- Download certificates

### 6. Repair Service
- Book repair appointment
- Track repair status
- Repair history
- Cost estimation

## 📱 Responsive Design
- Mobile-first approach
- Breakpoints: sm (640px), md (768px), lg (1024px), xl (1280px)
- Touch-friendly interface
- Optimized for all devices

## 🔧 Technical Stack
- **Framework**: Next.js 14 (App Router)
- **Styling**: Tailwind CSS
- **State**: Zustand
- **API**: Axios
- **Real-time**: WebSocket/Socket.io
- **Icons**: React Icons
- **Notifications**: React Hot Toast