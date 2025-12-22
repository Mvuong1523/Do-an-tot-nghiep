'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { FiAlertCircle, FiClock, FiDollarSign, FiFilter, FiSearch, FiFileText } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { payableApi } from '@/lib/api'
import { useAuthStore } from '@/store/authStore'
import { hasPermission, type Position } from '@/lib/permissions'

export default function EmployeePayablesPage() {
  const router = useRouter()
  const { user, employee, isAuthenticated } = useAuthStore()
  const [payables, setPayables] = useState<any[]>([])
  const [filteredPayables, setFilteredPayables] = useState<any[]>([])
  const [stats, setStats] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('ALL')

  const canCreate = hasPermission(employee?.position as Position, 'accounting.payables.create')
  const canEdit = hasPermission(employee?.position as Position, 'accounting.payables.edit')
  const canDelete = hasPermission(employee?.position as Position, 'accounting.payables.delete')

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

    fetchData()
  }, [isAuthenticated, user, router])

  useEffect(() => {
    filterPayables()
  }, [searchTerm, statusFilter, payables])

  const fetchData = async () => {
    try {
      setLoading(true)
      const [payablesRes, statsRes] = await Promise.all([
        payableApi.getAll(),
        payableApi.getStats()
      ])

      if (payablesRes.success && payablesRes.data) {
        setPayables(payablesRes.data)
      }

      if (statsRes.success && statsRes.data) {
        setStats(statsRes.data)
      }
    } catch (error: any) {
      toast.error(error.message || 'Lỗi khi tải dữ liệu')
    } finally {
      setLoading(false)
    }
  }

  const filterPayables = () => {
    let filtered = [...payables]

    if (searchTerm) {
      filtered = filtered.filter(p =>
        p.payableCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.supplierName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.purchaseOrderCode.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(p => p.status === statusFilter)
    }

    setFilteredPayables(filtered)
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('vi-VN')
  }

  const getStatusBadge = (status: string) => {
    const styles = {
      UNPAID: 'bg-yellow-100 text-yellow-800',
      PARTIAL: 'bg-blue-100 text-blue-800',
      PAID: 'bg-green-100 text-green-800',
      OVERDUE: 'bg-red-100 text-red-800'
    }

    const labels = {
      UNPAID: 'Chưa trả',
      PARTIAL: 'Trả một phần',
      PAID: 'Đã trả',
      OVERDUE: 'Quá hạn'
    }

    return (
      <span className={`px-2 py-1 rounded-full text-xs font-semibold ${styles[status as keyof typeof styles]}`}>
        {labels[status as keyof typeof labels]}
      </span>
    )
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Quản lý Công nợ Nhà cung cấp</h1>
        <p className="text-gray-600 mt-1">Theo dõi và quản lý các khoản phải trả cho nhà cung cấp</p>
      </div>

      {!canCreate && !canEdit && (
        <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-start">
            <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
            <div>
              <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
              <p className="text-sm text-blue-700 mt-1">
                Bạn chỉ có quyền xem công nợ nhà cung cấp, không thể thêm hoặc chỉnh sửa.
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Stats Cards */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Tổng công nợ</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {formatCurrency(stats.totalOutstanding)}
                </p>
              </div>
              <div className="bg-blue-100 p-3 rounded-full">
                <FiDollarSign className="text-blue-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Quá hạn</p>
                <p className="text-2xl font-bold text-red-600 mt-1">{stats.overdueCount}</p>
                <p className="text-xs text-gray-500 mt-1">{formatCurrency(stats.overdueAmount)}</p>
              </div>
              <div className="bg-red-100 p-3 rounded-full">
                <FiAlertCircle className="text-red-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Sắp đến hạn</p>
                <p className="text-2xl font-bold text-yellow-600 mt-1">{stats.upcomingCount}</p>
                <p className="text-xs text-gray-500 mt-1">Trong 7 ngày tới</p>
              </div>
              <div className="bg-yellow-100 p-3 rounded-full">
                <FiClock className="text-yellow-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Tổng số công nợ</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">{payables.length}</p>
              </div>
              <div className="bg-gray-100 p-3 rounded-full">
                <FiFilter className="text-gray-600" size={24} />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Filters */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="relative">
            <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Tìm kiếm theo mã, NCC, PO..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="ALL">Tất cả trạng thái</option>
            <option value="UNPAID">Chưa trả</option>
            <option value="PARTIAL">Trả một phần</option>
            <option value="OVERDUE">Quá hạn</option>
            <option value="PAID">Đã trả</option>
          </select>
        </div>
      </div>

      {/* Payables Table */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Mã công nợ</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nhà cung cấp</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Mã PO</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tổng tiền</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Đã trả</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Còn nợ</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ngày hạn</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredPayables.map((payable) => (
                <tr key={payable.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {payable.payableCode}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">{payable.supplierName}</div>
                    <div className="text-sm text-gray-500">{payable.supplierTaxCode}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {payable.purchaseOrderCode}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {formatCurrency(payable.totalAmount)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-green-600">
                    {formatCurrency(payable.paidAmount)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-red-600">
                    {formatCurrency(payable.remainingAmount)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{formatDate(payable.dueDate)}</div>
                    {payable.daysOverdue && payable.daysOverdue > 0 && (
                      <div className="text-xs text-red-600">Quá {payable.daysOverdue} ngày</div>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {getStatusBadge(payable.status)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {filteredPayables.length === 0 && (
            <div className="text-center py-12">
              <p className="text-gray-500">Không có công nợ nào</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
