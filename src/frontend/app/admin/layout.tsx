'use client'

import { useAuthStore } from '@/store/authStore'
import HorizontalNav from '@/components/layout/HorizontalNav'
import HydratedLayout from '@/components/HydratedLayout'

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const { user } = useAuthStore()
  
  // Detect role based on user position
  let navRole: 'WAREHOUSE' | 'PRODUCT_MANAGER' | 'ADMIN' | 'ACCOUNTANT' | 'SALES' = 'ADMIN'
  
  if (user?.role === 'EMPLOYEE') {
    if (user?.position === 'WAREHOUSE') {
      navRole = 'WAREHOUSE'
    } else if (user?.position === 'ACCOUNTANT') {
      navRole = 'ACCOUNTANT'
    } else if (user?.position === 'SALES') {
      navRole = 'SALES'
    }
  }
  
  return (
    <HydratedLayout>
      <div className="min-h-screen bg-gray-50">
        <HorizontalNav role={navRole} />
        <main>
          {children}
        </main>
      </div>
    </HydratedLayout>
  )
}
