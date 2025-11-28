'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiDownload, FiUpload, FiPackage } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import EmployeeBreadcrumb from '@/components/EmployeeBreadcrumb'

export default function WarehouseReportsPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    if (user?.role !== 'WAREHOUSE' && user?.role !== 'ADMIN') {
      toast.error('Chỉ nhân viên kho mới có quyền truy cập')
      router.push('/')
      return
    }

    setLoading(false)
  }, [isAuthenticated, user, router])

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        <EmployeeBreadcrumb items={[{ name: 'Báo cáo' }]} />

        <h1 className="text-3xl font-bold text-gray-900 mb-8">Báo cáo kho hàng</h1>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Báo cáo nhập kho</h3>
              <FiDownload className="text-green-500" size={24} />
            </div>
            <p className="text-gray-600 text-sm mb-4">
              Thống kê các phiếu nhập kho theo thời gian
            </p>
            <button className="w-full bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 transition-colors">
              Xem báo cáo
            </button>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Báo cáo xuất kho</h3>
              <FiUpload className="text-blue-500" size={24} />
            </div>
            <p className="text-gray-600 text-sm mb-4">
              Thống kê các phiếu xuất kho theo thời gian
            </p>
            <button className="w-full bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors">
              Xem báo cáo
            </button>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Báo cáo tồn kho</h3>
              <FiPackage className="text-purple-500" size={24} />
            </div>
            <p className="text-gray-600 text-sm mb-4">
              Thống kê tình trạng tồn kho hiện tại
            </p>
            <button className="w-full bg-purple-500 text-white px-4 py-2 rounded-lg hover:bg-purple-600 transition-colors">
              Xem báo cáo
            </button>
          </div>
        </div>

        <div className="mt-8 bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Chức năng đang phát triển</h2>
          <p className="text-gray-600">
            Các báo cáo chi tiết đang được phát triển. Vui lòng quay lại sau.
          </p>
        </div>
      </div>
    </div>
  )
}
