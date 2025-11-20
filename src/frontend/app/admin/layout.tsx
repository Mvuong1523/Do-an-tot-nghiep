'use client'

import { useAuthStore } from '@/store/authStore'
import HorizontalNav from '@/components/layout/HorizontalNav'

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const { user } = useAuthStore()
  
  // Detect role: nếu user là WAREHOUSE thì hiển thị navbar warehouse
  const navRole = user?.role === 'WAREHOUSE' ? 'WAREHOUSE' : 'ADMIN'
  
  return (
    <div className="min-h-screen bg-gray-50">
      <HorizontalNav role={navRole as any} />
      <main>
        {children}
      </main>
    </div>
  )
}
