'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import toast from 'react-hot-toast'
import { FiUpload, FiDownload, FiFileText } from 'react-icons/fi'
import { useAuthStore } from '@/store/authStore'
import { hasPermission, type Position } from '@/lib/permissions'

export default function EmployeeReconciliationPage() {
  const router = useRouter()
  const { user, employee, isAuthenticated } = useAuthStore()
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<any[]>([])
  const [summary, setSummary] = useState<any>(null)
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [gateway, setGateway] = useState('ALL')

  const canEdit = hasPermission(employee?.position as Position, 'accounting.reconciliation.edit')

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

    // Set default dates (last 30 days)
    const end = new Date()
    const start = new Date()
    start.setDate(start.getDate() - 30)
    
    setStartDate(start.toISOString().split('T')[0])
    setEndDate(end.toISOString().split('T')[0])
  }, [isAuthenticated, user, router])

  const loadReconciliation = async () => {
    if (!startDate || !endDate) {
      toast.error('Vui lòng chọn khoảng thời gian')
      return
    }

    try {
      setLoading(true)
      const token = localStorage.getItem('token') || localStorage.getItem('auth_token')
      const response = await fetch('http://localhost:8080/api/accounting/payment-reconciliation', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          startDate,
          endDate,
          gateway
        })
      })

      const result = await response.json()
      if (result.success) {
        setData(result.data.data || [])
        setSummary(result.data.summary)
        toast.success('Tải dữ liệu thành công')
      } else {
        toast.error(result.message || 'Lỗi khi tải dữ liệu')
      }
    } catch (error) {
      console.error('Error:', error)
      toast.error('Lỗi khi tải dữ liệu đối soát')
    } finally {
      setLoading(false)
    }
  }

  const handleImport = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!canEdit) {
      toast.error('Bạn không có quyền thực hiện đối soát')
      return
    }

    const file = e.target.files?.[0]
    if (!file) return

    const formData = new FormData()
    formData.append('file', file)
    formData.append('gateway', gateway)

    try {
      setLoading(true)
      const token = localStorage.getItem('token') || localStorage.getItem('auth_token')
      const response = await fetch('http://localhost:8080/api/accounting/payment-reconciliation/import', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData
      })

      const result = await response.json()
      if (result.success) {
        toast.success('Import thành công')
        loadReconciliation()
      } else {
        toast.error(result.message || 'Lỗi khi import')
      }
    } catch (error) {
      console.error('Error:', error)
      toast.error('Lỗi khi import file')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Đối soát thanh toán</h1>
          <p className="mt-2 text-gray-600">So sánh dữ liệu hệ thống với cổng thanh toán</p>
        </div>

        {!canEdit && (
          <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div className="flex items-start">
              <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
              <div>
                <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
                <p className="text-sm text-blue-700 mt-1">
                  Bạn chỉ có quyền xem báo cáo đối soát, không thể thực hiện đối soát.
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Filters */}
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Từ ngày</label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-4 py-2"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Đến ngày</label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-4 py-2"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Cổng thanh toán</label>
              <select
                value={gateway}
                onChange={(e) => setGateway(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-4 py-2"
              >
                <option value="ALL">Tất cả</option>
                <option value="SEPAY">SePay</option>
                <option value="VNPAY">VNPay</option>
                <option value="MOMO">MoMo</option>
              </select>
            </div>
            <div className="flex items-end space-x-2">
              <button
                onClick={loadReconciliation}
                disabled={loading}
                className="flex-1 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                Tải dữ liệu
              </button>
              {canEdit && (
                <label className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 cursor-pointer flex items-center">
                  <FiUpload className="mr-2" />
                  Import
                  <input
                    type="file"
                    accept=".csv"
                    onChange={handleImport}
                    className="hidden"
                  />
                </label>
              )}
            </div>
          </div>
        </div>

        {/* Summary */}
        {summary && (
          <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mb-6">
            <div className="bg-white rounded-lg shadow p-4">
              <p className="text-sm text-gray-600">Tổng đơn hàng</p>
              <p className="text-2xl font-bold text-gray-900">{summary.total}</p>
            </div>
            <div className="bg-white rounded-lg shadow p-4">
              <p className="text-sm text-gray-600">Khớp</p>
              <p className="text-2xl font-bold text-green-600">{summary.matched}</p>
            </div>
            <div className="bg-white rounded-lg shadow p-4">
              <p className="text-sm text-gray-600">Sai lệch</p>
              <p className="text-2xl font-bold text-red-600">{summary.mismatched}</p>
            </div>
            <div className="bg-white rounded-lg shadow p-4">
              <p className="text-sm text-gray-600">Thiếu gateway</p>
              <p className="text-2xl font-bold text-orange-600">{summary.missing}</p>
            </div>
            <div className="bg-white rounded-lg shadow p-4">
              <p className="text-sm text-gray-600">Chưa thanh toán</p>
              <p className="text-2xl font-bold text-blue-600">{summary.pending || 0}</p>
            </div>
          </div>
        )}

        {/* Data Table */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Mã đơn</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">TT Đơn</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">TT Thanh toán</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Mã GD</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Cổng TT</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Hệ thống</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Cổng TT</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Sai lệch</th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {data.map((item, index) => (
                  <tr key={index} className={item.paymentStatus === 'PENDING' ? 'bg-gray-50' : ''}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{item.orderId}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <span className={`px-2 py-1 text-xs rounded-full ${
                        item.orderStatus === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                        item.orderStatus === 'DELIVERED' ? 'bg-blue-100 text-blue-800' :
                        item.orderStatus === 'CONFIRMED' ? 'bg-purple-100 text-purple-800' :
                        item.orderStatus === 'CANCELLED' ? 'bg-red-100 text-red-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {item.orderStatus === 'PENDING_PAYMENT' ? 'Chờ TT' :
                         item.orderStatus === 'CONFIRMED' ? 'Đã xác nhận' :
                         item.orderStatus === 'PROCESSING' ? 'Đang xử lý' :
                         item.orderStatus === 'SHIPPING' ? 'Đang giao' :
                         item.orderStatus === 'DELIVERED' ? 'Đã giao' :
                         item.orderStatus === 'COMPLETED' ? 'Hoàn thành' :
                         item.orderStatus === 'CANCELLED' ? 'Đã hủy' : item.orderStatus}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <span className={`px-2 py-1 text-xs rounded-full ${
                        item.paymentStatus === 'PAID' ? 'bg-green-100 text-green-800' :
                        item.paymentStatus === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-red-100 text-red-800'
                      }`}>
                        {item.paymentStatus === 'PAID' ? 'Đã thanh toán' :
                         item.paymentStatus === 'PENDING' ? 'Chờ thanh toán' :
                         item.paymentStatus === 'FAILED' ? 'Thất bại' : item.paymentStatus}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{item.transactionId}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{item.gateway}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-gray-900">
                      {item.systemAmount?.toLocaleString('vi-VN')} ₫
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-gray-900">
                      {item.gatewayAmount > 0 ? item.gatewayAmount?.toLocaleString('vi-VN') + ' ₫' : '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-red-600">
                      {item.discrepancy > 0 ? item.discrepancy?.toLocaleString('vi-VN') + ' ₫' : '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-center">
                      <span className={`px-2 py-1 text-xs rounded-full ${
                        item.status === 'MATCHED' ? 'bg-green-100 text-green-800' :
                        item.status === 'MISMATCHED' ? 'bg-red-100 text-red-800' :
                        item.status === 'PENDING_PAYMENT' ? 'bg-yellow-100 text-yellow-800' :
                        item.status === 'MISSING_IN_GATEWAY' ? 'bg-orange-100 text-orange-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {item.status === 'MATCHED' ? 'Khớp' :
                         item.status === 'MISMATCHED' ? 'Sai lệch' :
                         item.status === 'PENDING_PAYMENT' ? 'Chưa TT' :
                         item.status === 'MISSING_IN_GATEWAY' ? 'Thiếu gateway' :
                         item.status === 'MISSING_IN_SYSTEM' ? 'Thiếu hệ thống' : item.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  )
}
