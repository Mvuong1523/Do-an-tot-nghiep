'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'

export default function AdminInventoryPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()

  useEffect(() => {
    if (!isAuthenticated || !user || (user.role !== 'ADMIN' && user.role !== 'WAREHOUSE' && user.role !== 'EMPLOYEE')) {
      router.replace('/login')
    }
  }, [isAuthenticated, user, router])

  if (!isAuthenticated || !user || (user.role !== 'ADMIN' && user.role !== 'WAREHOUSE' && user.role !== 'EMPLOYEE')) {
    return null
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-4">Quản lý kho</h1>
      <p className="text-gray-600">Giao diện quản lý kho (Placeholder)</p>
    </div>
  )
}
