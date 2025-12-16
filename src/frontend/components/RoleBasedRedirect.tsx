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

    // Redirect based on role and position
    if (user?.role === 'EMPLOYEE') {
      // For employees, check position
      switch (user?.position) {
        case 'WAREHOUSE':
          router.push('/warehouse')
          break
        case 'PRODUCT_MANAGER':
          router.push('/product-manager')
          break
        case 'ACCOUNTANT':
          router.push('/admin/accounting')
          break
        case 'SALES':
        case 'SALE':
          router.push('/sales')
          break
        case 'CSKH':
          router.push('/admin')
          break
        default:
          router.push('/admin')
      }
    } else {
      // For non-employees, check role
      switch (user?.role) {
        case 'ADMIN':
          router.push('/admin')
          break
        case 'CUSTOMER':
          router.push('/')
          break
        default:
          router.push('/')
      }
    }
  }, [user, isAuthenticated, router])

  return null
}
