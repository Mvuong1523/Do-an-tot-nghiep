'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import toast from 'react-hot-toast'
import { FiPlus, FiEdit, FiCheck, FiDollarSign } from 'react-icons/fi'

export default function TaxPage() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const [taxReports, setTaxReports] = useState<any[]>([])
  const [taxSummary, setTaxSummary] = useState<any>(null)
  const [selectedType, setSelectedType] = useState('ALL')
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [editingReport, setEditingReport] = useState<any>(null)

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

    loadTaxReports()
    loadTaxSummary()
  }, [router, selectedType])

  const loadTaxReports = async () => {
    try {
      setLoading(true)
      const token = localStorage.getItem('token')
      const url = selectedType === 'ALL' 
        ? 'http://localhost:8080/api/accounting/tax/reports'
        : `http://localhost:8080/api/accounting/tax/reports/${selectedType}`
      
      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })

      const result = await response.json()
      if (result.success) {
        setTaxReports(result.data || [])
      } else {
        toast.error(result.message || 'Lỗi khi tải báo cáo thuế')
      }
    } catch (error) {
      console.error('Error:', error)
      toast.error('Lỗi khi tải báo cáo thuế')
    } finally {
      setLoading(false)
    }
  }

  const loadTaxSummary = async () => {
    try {
      const token = localStorage.getItem('token')
      const response = await fetch('http://localhost:8080/api/accounting/tax/summary', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })

      const result = await response.json()
      if (result.success) {
        setTaxSummary(result.data)
      }
    } catch (error) {
      console.error('Error loading tax summary:', error)
    }
  }

  const submitTaxReport = async (id: number) => {
    if (!confirm('Bạn có chắc muốn nộp báo cáo thuế này?')) {
      return
    }

    try {
      setLoading(true)
      const token = localStorage.getItem('token')
      const response = await fetch(`http://localhost:8080/api/accounting/tax/reports/${id}/submit`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })

      const result = await response.json()
      if (result.success) {
        toast.success('Nộp báo cáo thuế thành công')
        loadTaxReports()
        loadTaxSummary()
      } else {
        toast.error(result.message || 'Lỗi khi nộp báo cáo')
      }
    } catch (error) {
      console.error('Error:', error)
      toast.error('Lỗi khi nộp báo cáo thuế')
    } finally {
      setLoading(false)
    }
  }

  const markAsPaid = async (id: number) => {
    if (!confirm('Bạn có chắc đã thanh toán thuế này?')) {
      return
    }

    try {
      setLoading(true)
      const token = localStorage.getItem('token')
      const response = await fetch(`http://localhost:8080/api/accounting/tax/reports/${id}/mark-paid`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })

      const result = await response.json()
      if (result.success) {
        toast.success('Đánh dấu đã thanh toán thành công')
        loadTaxReports()
        loadTaxSummary()
      } else {
        toast.error(result.message || 'Lỗi khi cập nhật')
      }
    } catch (error) {
      console.error('Error:', error)
      toast.error('Lỗi khi cập nhật trạng thái')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Quản lý thuế</h1>
          <p className="mt-2 text-gray-600">Báo cáo và nộp thuế VAT, thuế TNDN</p>
        </div>

        {/* Tax Summary */}
        {taxSummary && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">VAT còn nợ</p>
                  <p className="text-2xl font-bold text-red-600 mt-2">
                    {taxSummary.vatOwed?.toLocaleString('vi-VN')} ₫
                  </p>
                </div>
                <div className="bg-red-100 p-3 rounded-full">
                  <FiDollarSign className="text-red-600" size={24} />
                </div>
              </div>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">VAT đã nộp</p>
                  <p className="text-2xl font-bold text-green-600 mt-2">
                    {taxSummary.vatPaid?.toLocaleString('vi-VN')} ₫
                  </p>
                </div>
                <div className="bg-green-100 p-3 rounded-full">
                  <FiCheck className="text-green-600" size={24} />
                </div>
              </div>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">TNDN còn nợ</p>
                  <p className="text-2xl font-bold text-orange-600 mt-2">
                    {taxSummary.corporateOwed?.toLocaleString('vi-VN')} ₫
                  </p>
                </div>
                <div className="bg-orange-100 p-3 rounded-full">
                  <FiDollarSign className="text-orange-600" size={24} />
                </div>
              </div>
            </div>

            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Tổng nợ thuế</p>
                  <p className="text-2xl font-bold text-red-600 mt-2">
                    {taxSummary.totalOwed?.toLocaleString('vi-VN')} ₫
                  </p>
                </div>
                <div className="bg-red-100 p-3 rounded-full">
                  <FiDollarSign className="text-red-600" size={24} />
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Filters and Actions */}
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <div className="flex justify-between items-center">
            <div className="flex space-x-4">
              <select
                value={selectedType}
                onChange={(e) => setSelectedType(e.target.value)}
                className="border border-gray-300 rounded-lg px-4 py-2"
              >
                <option value="ALL">Tất cả</option>
                <option value="VAT">Thuế VAT</option>
                <option value="CORPORATE_TAX">Thuế TNDN</option>
              </select>
            </div>
            <button
              onClick={() => setShowCreateModal(true)}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 flex items-center"
            >
              <FiPlus className="mr-2" />
              Tạo báo cáo thuế
            </button>
          </div>
        </div>

        {/* Tax Reports Table */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Mã BC</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Loại thuế</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Kỳ</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">DT chịu thuế</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Thuế suất</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Số thuế</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Còn nợ</th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Thao tác</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {taxReports.map((report) => (
                  <tr key={report.id}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {report.reportCode}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      {report.taxType === 'VAT' ? 'Thuế VAT' : 'Thuế TNDN'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      {new Date(report.periodStart).toLocaleDateString('vi-VN')} - {new Date(report.periodEnd).toLocaleDateString('vi-VN')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-gray-900">
                      {report.taxableRevenue?.toLocaleString('vi-VN')} ₫
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-gray-600">
                      {report.taxRate}%
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-gray-900">
                      {report.taxAmount?.toLocaleString('vi-VN')} ₫
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-red-600">
                      {report.remainingTax?.toLocaleString('vi-VN')} ₫
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-center">
                      <span className={`px-2 py-1 text-xs rounded-full ${
                        report.status === 'PAID' ? 'bg-green-100 text-green-800' :
                        report.status === 'SUBMITTED' ? 'bg-blue-100 text-blue-800' :
                        'bg-yellow-100 text-yellow-800'
                      }`}>
                        {report.status === 'PAID' ? 'Đã nộp' :
                         report.status === 'SUBMITTED' ? 'Đã gửi' : 'Nháp'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-center">
                      <div className="flex justify-center space-x-2">
                        {report.status === 'DRAFT' && (
                          <>
                            <button
                              onClick={() => setEditingReport(report)}
                              className="text-blue-600 hover:text-blue-800"
                            >
                              <FiEdit size={16} />
                            </button>
                            <button
                              onClick={() => submitTaxReport(report.id)}
                              disabled={loading}
                              className="text-green-600 hover:text-green-800 disabled:opacity-50"
                            >
                              <FiCheck size={16} />
                            </button>
                          </>
                        )}
                        {report.status === 'SUBMITTED' && (
                          <button
                            onClick={() => markAsPaid(report.id)}
                            disabled={loading}
                            className="text-green-600 hover:text-green-800 disabled:opacity-50"
                          >
                            <FiDollarSign size={16} />
                          </button>
                        )}
                      </div>
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