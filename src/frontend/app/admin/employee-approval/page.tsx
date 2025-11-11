'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'

export default function EmployeeApprovalPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()

  useEffect(() => {
    if (!isAuthenticated || !user || user.role !== 'ADMIN') {
      // Not allowed
      router.replace('/login')
    }
  }, [isAuthenticated, user, router])

  if (!isAuthenticated || !user || user.role !== 'ADMIN') {
    return null
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-4">Duyệt đăng ký nhân viên</h1>
      <p className="text-gray-600">Trang quản trị cho phép duyệt đăng ký nhân viên. (Placeholder)</p>
      {/* TODO: Kết nối API để hiển thị danh sách đăng ký */}
    </div>
  )
}
