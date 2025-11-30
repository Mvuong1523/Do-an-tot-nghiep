'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import toast from 'react-hot-toast'
import { FiDollarSign, FiAlertCircle, FiCheckCircle, FiFileText } from 'react-icons/fi'

export default function AccountingPage() {
  const router = useRouter()
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState<any>(null)

  useEffect(() => {
    // Đọc từ auth-storage (zustand persist)
    const authStorage = localStorage.getItem('auth-storage')
    if (!authStorage) {
      router.push('/login')
      return
    }

    const authData = JSON.parse(authStorage)
    const userData = authData.state?.user
    
    if (!userData) {
      router.push('/login')
      return
    }

    // Admin hoặc Employee với position ACCOUNTANT
    const isAdmin = userData.role === 'ADMIN'
    const isAccountant = userData.position === 'ACCOUNTANT'
    
    if (!isAdmin && !isAccountant) {
      toast.error('Bạn không có quyền truy cập trang này')
      router.push('/')
      return
    }

    loadStats()
  }, [router])

  const loadStats = async () => {
    try {
      setLoading(true)
      const token = localStorage.getItem('token')
      const response = await fetch('http://localhost:8080/api/accounting/stats', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })

      const result = await response.json()
      if (result.success) {
        setStats(result.data)
      }
    } catch (error) {
      console.error('Error loading stats:', error)
      toast.error('Lỗi khi tải thống kê')
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Kế toán & Đối soát</h1>
          <p className="mt-2 text-gray-600">Quản lý tài chính và đối soát thanh toán</p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Tổng doanh thu</p>
                <p className="text-2xl font-bold text-gray-900 mt-2">
                  {stats?.totalRevenue?.toLocaleString('vi-VN')} ₫
                </p>
              </div>
              <div className="bg-green-100 p-3 rounded-full">
                <FiDollarSign className="text-green-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Chờ đối soát</p>
                <p className="text-2xl font-bold text-orange-600 mt-2">
                  {stats?.pendingReconciliation || 0}
                </p>
              </div>
              <div className="bg-orange-100 p-3 rounded-full">
                <FiAlertCircle className="text-orange-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Đã đối soát</p>
                <p className="text-2xl font-bold text-green-600 mt-2">
                  {stats?.completedReconciliation || 0}
                </p>
              </div>
              <div className="bg-green-100 p-3 rounded-full">
                <FiCheckCircle className="text-green-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Sai lệch</p>
                <p className="text-2xl font-bold text-red-600 mt-2">
                  {stats?.discrepancyAmount?.toLocaleString('vi-VN')} ₫
                </p>
              </div>
              <div className="bg-red-100 p-3 rounded-full">
                <FiAlertCircle className="text-red-600" size={24} />
              </div>
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <button
            onClick={() => router.push('/admin/accounting/reconciliation')}
            className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow text-left"
          >
            <div className="flex items-center space-x-4">
              <div className="bg-blue-100 p-3 rounded-full">
                <FiCheckCircle className="text-blue-600" size={24} />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">Đối soát thanh toán</h3>
                <p className="text-sm text-gray-600 mt-1">So sánh với cổng thanh toán</p>
              </div>
            </div>
          </button>

          <button
            onClick={() => router.push('/admin/accounting/reports')}
            className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow text-left"
          >
            <div className="flex items-center space-x-4">
              <div className="bg-purple-100 p-3 rounded-full">
                <FiFileText className="text-purple-600" size={24} />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">Báo cáo tài chính</h3>
                <p className="text-sm text-gray-600 mt-1">Xem báo cáo chi tiết</p>
              </div>
            </div>
          </button>

          <button
            onClick={() => router.push('/admin/accounting/periods')}
            className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow text-left"
          >
            <div className="flex items-center space-x-4">
              <div className="bg-green-100 p-3 rounded-full">
                <FiDollarSign className="text-green-600" size={24} />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">Quản lý kỳ</h3>
                <p className="text-sm text-gray-600 mt-1">Chốt sổ và mở khóa kỳ</p>
              </div>
            </div>
          </button>
        </div>
      </div>
    </div>
  )
}
