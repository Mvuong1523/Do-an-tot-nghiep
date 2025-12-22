'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { FiFileText } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'

export default function EmployeeFinancialStatementsPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login')
      return
    }

    if (user?.role !== 'EMPLOYEE' && user?.role !== 'ADMIN') {
      toast.error('Bạn không có quyền truy cập')
      router.push('/')
      return
    }
  }, [isAuthenticated, user, router])

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Báo cáo tài chính</h1>
      
      <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-start">
          <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
          <div>
            <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
            <p className="text-sm text-blue-700 mt-1">
              Bạn có thể xem và tải xuống báo cáo tài chính.
            </p>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm p-6">
        <p className="text-gray-500 text-center">Chưa có báo cáo nào</p>
      </div>
    </div>
  )
}
