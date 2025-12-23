'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { FiFileText, FiDownload, FiCalendar } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import api from '@/lib/api'

interface FinancialStatement {
  startDate: string
  endDate: string
  totalRevenue: number
  totalExpenses: number
  netProfit: number
  profitMargin: number
}

export default function EmployeeFinancialStatementsPage() {
  const router = useRouter()
  const { user, employee, isAuthenticated } = useAuthStore()
  const [loading, setLoading] = useState(false)
  const [statement, setStatement] = useState<FinancialStatement | null>(null)
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')

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

    // Set default date range (current month)
    const now = new Date()
    const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
    const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0)
    
    setStartDate(firstDay.toISOString().split('T')[0])
    setEndDate(lastDay.toISOString().split('T')[0])
  }, [isAuthenticated, user, router])

  const loadStatement = async () => {
    if (!startDate || !endDate) {
      toast.error('Vui lòng chọn khoảng thời gian')
      return
    }

    setLoading(true)
    try {
      const response = await api.get('/accounting/financial-statement', {
        params: { startDate, endDate }
      })
      
      if (response.data.success) {
        setStatement(response.data.data)
      } else {
        toast.error(response.data.message || 'Không thể tải báo cáo')
      }
    } catch (error: any) {
      console.error('Error loading statement:', error)
      toast.error(error.response?.data?.message || 'Lỗi khi tải báo cáo')
    } finally {
      setLoading(false)
    }
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('vi-VN')
  }

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Báo cáo tài chính</h1>
      
      <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-start">
          <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
          <div>
            <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
            <p className="text-sm text-blue-700 mt-1">
              {employee?.position === 'ACCOUNTANT' 
                ? 'Bạn có thể xem và tải xuống báo cáo tài chính.'
                : 'Bạn có thể xem báo cáo tài chính.'}
            </p>
          </div>
        </div>
      </div>

      {/* Date Range Filter */}
      <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Chọn khoảng thời gian</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Từ ngày
            </label>
            <div className="relative">
              <FiCalendar className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Đến ngày
            </label>
            <div className="relative">
              <FiCalendar className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>
          
          <div className="flex items-end">
            <button
              onClick={loadStatement}
              disabled={loading}
              className="w-full bg-blue-500 text-white px-6 py-2 rounded-lg hover:bg-blue-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Đang tải...' : 'Xem báo cáo'}
            </button>
          </div>
        </div>
      </div>

      {/* Financial Statement */}
      {statement && (
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-xl font-bold text-gray-900">
                Báo cáo tài chính
              </h2>
              <p className="text-sm text-gray-600 mt-1">
                Từ {formatDate(statement.startDate)} đến {formatDate(statement.endDate)}
              </p>
            </div>
            <button
              onClick={() => window.print()}
              className="flex items-center px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
            >
              <FiDownload className="mr-2" size={18} />
              Tải xuống
            </button>
          </div>

          <div className="space-y-4">
            {/* Revenue */}
            <div className="flex items-center justify-between p-4 bg-green-50 rounded-lg">
              <div>
                <p className="text-sm text-gray-600">Tổng doanh thu</p>
                <p className="text-2xl font-bold text-green-600">
                  {formatPrice(statement.totalRevenue)}
                </p>
              </div>
            </div>

            {/* Expenses */}
            <div className="flex items-center justify-between p-4 bg-red-50 rounded-lg">
              <div>
                <p className="text-sm text-gray-600">Tổng chi phí</p>
                <p className="text-2xl font-bold text-red-600">
                  {formatPrice(statement.totalExpenses)}
                </p>
              </div>
            </div>

            {/* Net Profit */}
            <div className="flex items-center justify-between p-4 bg-blue-50 rounded-lg">
              <div>
                <p className="text-sm text-gray-600">Lợi nhuận ròng</p>
                <p className="text-2xl font-bold text-blue-600">
                  {formatPrice(statement.netProfit)}
                </p>
              </div>
            </div>

            {/* Profit Margin */}
            <div className="flex items-center justify-between p-4 bg-purple-50 rounded-lg">
              <div>
                <p className="text-sm text-gray-600">Tỷ suất lợi nhuận</p>
                <p className="text-2xl font-bold text-purple-600">
                  {statement.profitMargin != null ? statement.profitMargin.toFixed(2) : '0.00'}%
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {!statement && !loading && (
        <div className="bg-white rounded-lg shadow-sm p-6">
          <p className="text-gray-500 text-center">
            Chọn khoảng thời gian và nhấn "Xem báo cáo" để xem báo cáo tài chính
          </p>
        </div>
      )}
    </div>
  )
}
