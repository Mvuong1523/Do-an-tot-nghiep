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
  const [selectedPeriod, setSelectedPeriod] = useState('ALL')
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear().toString())
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
  }, [router, selectedType, selectedPeriod, selectedYear])

  const loadTaxReports = async () => {
    try {
      setLoading(true)
      const token = localStorage.getItem('auth_token')
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
        let filteredReports = result.data || []
        
        // Filter by period (month, quarter, year)
        if (selectedPeriod !== 'ALL') {
          filteredReports = filteredReports.filter((report: any) => {
            const startDate = new Date(report.periodStart)
            const year = startDate.getFullYear()
            const month = startDate.getMonth() + 1
            const quarter = Math.ceil(month / 3)
            
            if (year.toString() !== selectedYear) return false
            
            if (selectedPeriod.startsWith('Q')) {
              const selectedQuarter = parseInt(selectedPeriod.substring(1))
              return quarter === selectedQuarter
            } else if (selectedPeriod.startsWith('M')) {
              const selectedMonth = parseInt(selectedPeriod.substring(1))
              return month === selectedMonth
            } else if (selectedPeriod === 'YEAR') {
              return true
            }
            return true
          })
        }
        
        setTaxReports(filteredReports)
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
      const token = localStorage.getItem('auth_token')
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
      const token = localStorage.getItem('auth_token')
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
      const token = localStorage.getItem('auth_token')
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
          <div className="flex justify-between items-center flex-wrap gap-4">
            <div className="flex space-x-4 flex-wrap gap-2">
              <select
                value={selectedType}
                onChange={(e) => setSelectedType(e.target.value)}
                className="border border-gray-300 rounded-lg px-4 py-2"
              >
                <option value="ALL">Tất cả loại thuế</option>
                <option value="VAT">Thuế VAT</option>
                <option value="CORPORATE_TAX">Thuế TNDN</option>
              </select>

              <select
                value={selectedYear}
                onChange={(e) => setSelectedYear(e.target.value)}
                className="border border-gray-300 rounded-lg px-4 py-2"
              >
                {[...Array(5)].map((_, i) => {
                  const year = new Date().getFullYear() - i
                  return <option key={year} value={year}>{year}</option>
                })}
              </select>

              <select
                value={selectedPeriod}
                onChange={(e) => setSelectedPeriod(e.target.value)}
                className="border border-gray-300 rounded-lg px-4 py-2"
              >
                <option value="ALL">Tất cả kỳ</option>
                <option value="YEAR">Cả năm</option>
                <option value="Q1">Quý 1 (T1-T3)</option>
                <option value="Q2">Quý 2 (T4-T6)</option>
                <option value="Q3">Quý 3 (T7-T9)</option>
                <option value="Q4">Quý 4 (T10-T12)</option>
                <option value="M1">Tháng 1</option>
                <option value="M2">Tháng 2</option>
                <option value="M3">Tháng 3</option>
                <option value="M4">Tháng 4</option>
                <option value="M5">Tháng 5</option>
                <option value="M6">Tháng 6</option>
                <option value="M7">Tháng 7</option>
                <option value="M8">Tháng 8</option>
                <option value="M9">Tháng 9</option>
                <option value="M10">Tháng 10</option>
                <option value="M11">Tháng 11</option>
                <option value="M12">Tháng 12</option>
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

        {/* Create/Edit Modal */}
        {(showCreateModal || editingReport) && (
          <TaxReportModal
            report={editingReport}
            onClose={() => {
              setShowCreateModal(false)
              setEditingReport(null)
            }}
            onSuccess={() => {
              setShowCreateModal(false)
              setEditingReport(null)
              loadTaxReports()
              loadTaxSummary()
            }}
          />
        )}
      </div>
    </div>
  )
}

// Tax Report Modal Component
function TaxReportModal({ report, onClose, onSuccess }: any) {
  const [loading, setLoading] = useState(false)
  const [form, setForm] = useState({
    taxType: report?.taxType || 'VAT',
    periodStart: report?.periodStart?.split('T')[0] || '',
    periodEnd: report?.periodEnd?.split('T')[0] || '',
    taxableRevenue: report?.taxableRevenue || '',
    taxRate: report?.taxRate || '10',
    notes: report?.notes || ''
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!form.periodStart || !form.periodEnd) {
      toast.error('Vui lòng chọn kỳ báo cáo')
      return
    }

    if (!form.taxableRevenue || parseFloat(form.taxableRevenue) < 0) {
      toast.error('Vui lòng nhập doanh thu chịu thuế hợp lệ')
      return
    }

    try {
      setLoading(true)
      const token = localStorage.getItem('auth_token')
      const url = report
        ? `http://localhost:8080/api/accounting/tax/reports/${report.id}`
        : 'http://localhost:8080/api/accounting/tax/reports'

      const response = await fetch(url, {
        method: report ? 'PUT' : 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          ...form,
          taxableRevenue: parseFloat(form.taxableRevenue),
          taxRate: parseFloat(form.taxRate),
          periodStart: form.periodStart + 'T00:00:00',
          periodEnd: form.periodEnd + 'T23:59:59'
        })
      })

      const result = await response.json()
      if (result.success) {
        toast.success(report ? 'Cập nhật báo cáo thành công' : 'Tạo báo cáo thành công')
        onSuccess()
      } else {
        toast.error(result.message || 'Có lỗi xảy ra')
      }
    } catch (error) {
      console.error('Error:', error)
      toast.error('Có lỗi xảy ra')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">
            {report ? 'Sửa báo cáo thuế' : 'Tạo báo cáo thuế mới'}
          </h2>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Loại thuế *</label>
                <select
                  value={form.taxType}
                  onChange={(e) => {
                    const newTaxType = e.target.value
                    setForm({
                      ...form, 
                      taxType: newTaxType,
                      taxRate: newTaxType === 'VAT' ? '10' : '20'
                    })
                  }}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2"
                  required
                  disabled={!!report}
                >
                  <option value="VAT">Thuế VAT (10%)</option>
                  <option value="CORPORATE_TAX">Thuế TNDN (20%)</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Thuế suất (%) *</label>
                <input
                  type="number"
                  value={form.taxRate}
                  onChange={(e) => setForm({...form, taxRate: e.target.value})}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2"
                  required
                  min="0"
                  max="100"
                  step="0.1"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Từ ngày *</label>
                <input
                  type="date"
                  value={form.periodStart}
                  onChange={(e) => setForm({...form, periodStart: e.target.value})}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Đến ngày *</label>
                <input
                  type="date"
                  value={form.periodEnd}
                  onChange={(e) => setForm({...form, periodEnd: e.target.value})}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2"
                  required
                />
              </div>

              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-700 mb-2">Doanh thu chịu thuế *</label>
                <input
                  type="number"
                  value={form.taxableRevenue}
                  onChange={(e) => setForm({...form, taxableRevenue: e.target.value})}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2"
                  placeholder="0"
                  required
                  min="0"
                  step="1000"
                />
                <p className="text-sm text-gray-500 mt-1">
                  Số thuế phải nộp: {(parseFloat(form.taxableRevenue || '0') * parseFloat(form.taxRate) / 100).toLocaleString('vi-VN')} ₫
                </p>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Ghi chú</label>
              <textarea
                value={form.notes}
                onChange={(e) => setForm({...form, notes: e.target.value})}
                className="w-full border border-gray-300 rounded-lg px-4 py-2"
                rows={3}
                placeholder="Nhập ghi chú về báo cáo thuế..."
              />
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <h4 className="text-sm font-medium text-blue-900 mb-2">Lưu ý:</h4>
              <ul className="text-sm text-blue-800 space-y-1">
                <li>• Thuế VAT: Thường là 10% trên doanh thu bán hàng</li>
                <li>• Thuế TNDN: Thường là 20% trên lợi nhuận trước thuế</li>
                <li>• Báo cáo sẽ ở trạng thái "Nháp" sau khi tạo</li>
                <li>• Cần "Nộp báo cáo" trước khi "Đánh dấu đã thanh toán"</li>
              </ul>
            </div>

            <div className="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
                disabled={loading}
              >
                Hủy
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
                disabled={loading}
              >
                {loading ? 'Đang xử lý...' : (report ? 'Cập nhật' : 'Tạo báo cáo')}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}