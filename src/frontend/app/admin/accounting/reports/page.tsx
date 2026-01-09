'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import toast from 'react-hot-toast'
import { FiDownload } from 'react-icons/fi'

export default function ReportsPage() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<any[]>([])
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [viewMode, setViewMode] = useState('ORDERS')

  useEffect(() => {
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

    const isAdmin = userData.role === 'ADMIN'
    const isAccountant = userData.position === 'ACCOUNTANT'
    
    if (!isAdmin && !isAccountant) {
      toast.error('Bạn không có quyền truy cập')
      router.push('/')
      return
    }

    // Set default dates
    const end = new Date()
    const start = new Date()
    start.setDate(start.getDate() - 30)
    
    setStartDate(start.toISOString().split('T')[0])
    setEndDate(end.toISOString().split('T')[0])
  }, [router])

  const loadReports = async () => {
    if (!startDate || !endDate) {
      toast.error('Vui lòng chọn khoảng thời gian')
      return
    }

    try {
      setLoading(true)
      const token = localStorage.getItem('token')
      const response = await fetch(
        `http://localhost:8080/api/accounting/reports?startDate=${startDate}&endDate=${endDate}&viewMode=${viewMode}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      )

      const result = await response.json()
      if (result.success) {
        setData(result.data || [])
        toast.success('Tải báo cáo thành công')
      } else {
        toast.error(result.message || 'Lỗi khi tải báo cáo')
      }
    } catch (error) {
      console.error('Error:', error)
      toast.error('Lỗi khi tải báo cáo')
    } finally {
      setLoading(false)
    }
  }

  const exportExcel = async () => {
    try {
      setLoading(true)
      const token = localStorage.getItem('token')
      const response = await fetch(
        `http://localhost:8080/api/accounting/reports/export?startDate=${startDate}&endDate=${endDate}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      )

      const result = await response.json()
      if (result.success) {
        // Decode base64 and download
        const blob = new Blob([
          Uint8Array.from(atob(result.data.data), c => c.charCodeAt(0))
        ], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })

        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = result.data.fileName
        a.click()
        
        toast.success('Xuất Excel thành công')
      } else {
        toast.error(result.message || 'Lỗi khi xuất Excel')
      }
    } catch (error) {
      console.error('Error:', error)
      toast.error('Lỗi khi xuất Excel')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Báo cáo tài chính</h1>
          <p className="mt-2 text-gray-600">Xem chi tiết doanh thu, chi phí và lợi nhuận</p>
        </div>

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
              <label className="block text-sm font-medium text-gray-700 mb-2">Chế độ xem</label>
              <select
                value={viewMode}
                onChange={(e) => setViewMode(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-4 py-2"
              >
                <option value="ORDERS">Chi tiết đơn hàng</option>
                <option value="DAILY">Tổng hợp theo ngày</option>
                <option value="MONTHLY">Tổng hợp theo tháng</option>
              </select>
            </div>
            <div className="flex items-end space-x-2">
              <button
                onClick={loadReports}
                disabled={loading}
                className="flex-1 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                Xem báo cáo
              </button>
              <button
                onClick={exportExcel}
                disabled={loading}
                className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 disabled:opacity-50 flex items-center"
              >
                <FiDownload className="mr-2" />
                Excel
              </button>
            </div>
          </div>
        </div>

        {/* Data Table */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    {viewMode === 'ORDERS' ? 'Mã đơn' : 'Kỳ'}
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ngày</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Doanh thu</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Chi phí VC</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Phí cổng TT</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Tổng chi phí</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Lợi nhuận gộp</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">VAT (10%)</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Thuế TNDN (20%)</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Lợi nhuận ròng</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {data.map((item, index) => (
                  <tr key={index}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {item.orderId || item.period}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      {item.date || item.period}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-green-600 font-medium">
                      {item.revenue?.toLocaleString('vi-VN')} ₫
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-red-600">
                      -{item.shippingCost?.toLocaleString('vi-VN')} ₫
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-red-600">
                      -{item.paymentGatewayCost?.toLocaleString('vi-VN')} ₫
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-red-600 font-medium">
                      -{item.totalCosts?.toLocaleString('vi-VN')} ₫
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-blue-600 font-bold">
                      {item.grossProfit?.toLocaleString('vi-VN')} ₫
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-orange-600">
                      -{item.vat?.toLocaleString('vi-VN')} ₫
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-orange-600">
                      -{item.corporateTax?.toLocaleString('vi-VN')} ₫
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-green-600 font-bold">
                      {item.netProfit?.toLocaleString('vi-VN')} ₫
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
