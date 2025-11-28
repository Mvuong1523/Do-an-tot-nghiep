'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'

export default function RoleBasedRedirect() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()

  useEffect(() => {
    if (!isAuthenticated) {
      return
    }

    // Redirect based on role
    switch (user?.role) {
      case 'ADMIN':
        router.push('/admin')
        break
      case 'WAREHOUSE':
        router.push('/warehouse')
        break
      case 'PRODUCT_MANAGER':
        router.push('/product-manager')
        break
      case 'CUSTOMER':
        router.push('/')
        break
      default:
        router.push('/')
    }
  }, [user, isAuthenticated, router])

  return null
}
