'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiDownload, FiUpload, FiPlus, FiSearch, FiFilter, FiEye, FiCalendar } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'

type TransactionType = 'IMPORT' | 'EXPORT' | 'ALL'
type TransactionStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED'

interface InventoryTransaction {
  id: number
  type: 'IMPORT' | 'EXPORT'
  transactionCode: string
  createdBy: string
  createdAt: string
  totalAmount: number
  status: TransactionStatus
  note?: string
  items: {
    productId: number
    productName: string
    quantity: number
    price: number
  }[]
}

export default function InventoryTransactionsPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  
  const [transactions, setTransactions] = useState<InventoryTransaction[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [filterType, setFilterType] = useState<TransactionType>('ALL')
  const [filterStatus, setFilterStatus] = useState<TransactionStatus | 'ALL'>('ALL')
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [createType, setCreateType] = useState<'IMPORT' | 'EXPORT'>('IMPORT')

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    // Check if user is admin or employee (tạm thời cho tất cả employee)
    if (user?.role !== 'ADMIN' && user?.role !== 'EMPLOYEE') {
      toast.error('Chỉ quản lý và nhân viên mới có quyền truy cập')
      router.push('/')
      return
    }

    loadTransactions()
  }, [isAuthenticated, user, router])

  const loadTransactions = async () => {
    try {
      // TODO: Call API
      // const response = await inventoryApi.getTransactions()
      // setTransactions(response.data)
      
      // Mock data
      setTransactions([
        {
          id: 1,
          type: 'IMPORT',
          transactionCode: 'IMP-2024-001',
          createdBy: 'Admin',
          createdAt: '2024-01-15T10:30:00',
          totalAmount: 50000000,
          status: 'COMPLETED',
          note: 'Nhập hàng iPhone 16 Pro Max',
          items: [
            { productId: 1, productName: 'iPhone 16 Pro Max 256GB', quantity: 10, price: 29990000 }
          ]
        },
        {
          id: 2,
          type: 'EXPORT',
          transactionCode: 'EXP-2024-001',
          createdBy: 'Nhân viên A',
          createdAt: '2024-01-16T14:20:00',
          totalAmount: 15000000,
          status: 'COMPLETED',
          note: 'Xuất hàng cho đơn #12345',
          items: [
            { productId: 2, productName: 'Xiaomi POCO C71', quantity: 5, price: 2490000 }
          ]
        }
      ])
    } catch (error) {
      toast.error('Lỗi khi tải danh sách')
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
    return new Date(dateString).toLocaleString('vi-VN')
  }

  const getStatusBadge = (status: TransactionStatus) => {
    const badges = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      COMPLETED: 'bg-green-100 text-green-800',
      CANCELLED: 'bg-red-100 text-red-800'
    }
    const labels = {
      PENDING: 'Chờ xử lý',
      COMPLETED: 'Hoàn thành',
      CANCELLED: 'Đã hủy'
    }
    return (
      <span className={`px-2 py-1 text-xs font-semibold rounded ${badges[status]}`}>
        {labels[status]}
      </span>
    )
  }

  const filteredTransactions = transactions.filter(trans => {
    const matchSearch = trans.transactionCode.toLowerCase().includes(searchQuery.toLowerCase()) ||
                       trans.createdBy.toLowerCase().includes(searchQuery.toLowerCase())
    const matchType = filterType === 'ALL' || trans.type === filterType
    const matchStatus = filterStatus === 'ALL' || trans.status === filterStatus
    return matchSearch && matchType && matchStatus
  })

  const handleCreateTransaction = (type: 'IMPORT' | 'EXPORT') => {
    setCreateType(type)
    router.push(`/admin/inventory/transactions/create?type=${type}`)
  }

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
        {/* Breadcrumb */}
        <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
          <Link href="/" className="hover:text-red-500">Trang chủ</Link>
          <span>/</span>
          <Link href="/admin" className="hover:text-red-500">Quản trị</Link>
          <span>/</span>
          <Link href="/admin/inventory" className="hover:text-red-500">Quản lý kho</Link>
          <span>/</span>
          <span className="text-gray-900">Xuất nhập kho</span>
        </nav>

        {/* Header */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 space-y-4 md:space-y-0">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Quản lý xuất nhập kho</h1>
            <p className="text-gray-600 mt-1">Theo dõi và quản lý các phiếu xuất nhập kho</p>
          </div>
          
          <div className="flex space-x-2">
            <button
              onClick={() => handleCreateTransaction('IMPORT')}
              className="flex items-center space-x-2 bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 transition-colors"
            >
              <FiDownload />
              <span>Nhập hàng</span>
            </button>
            <button
              onClick={() => handleCreateTransaction('EXPORT')}
              className="flex items-center space-x-2 bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
            >
              <FiUpload />
              <span>Xuất hàng</span>
            </button>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Tổng phiếu nhập</p>
                <p className="text-2xl font-bold text-green-600">
                  {transactions.filter(t => t.type === 'IMPORT').length}
                </p>
              </div>
              <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                <FiDownload className="text-green-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Tổng phiếu xuất</p>
                <p className="text-2xl font-bold text-blue-600">
                  {transactions.filter(t => t.type === 'EXPORT').length}
                </p>
              </div>
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <FiUpload className="text-blue-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Chờ xử lý</p>
                <p className="text-2xl font-bold text-yellow-600">
                  {transactions.filter(t => t.status === 'PENDING').length}
                </p>
              </div>
              <div className="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
                <FiCalendar className="text-yellow-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Tổng giá trị</p>
                <p className="text-2xl font-bold text-red-600">
                  {formatPrice(transactions.reduce((sum, t) => sum + t.totalAmount, 0))}
                </p>
              </div>
              <div className="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
                <span className="text-red-600 text-xl font-bold">₫</span>
              </div>
            </div>
          </div>
        </div>

        {/* Filters */}
        <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="relative">
              <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder="Tìm mã phiếu, người tạo..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
              />
            </div>

            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value as TransactionType)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
            >
              <option value="ALL">Tất cả loại</option>
              <option value="IMPORT">Phiếu nhập</option>
              <option value="EXPORT">Phiếu xuất</option>
            </select>

            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value as TransactionStatus | 'ALL')}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="PENDING">Chờ xử lý</option>
              <option value="COMPLETED">Hoàn thành</option>
              <option value="CANCELLED">Đã hủy</option>
            </select>

            <button className="flex items-center justify-center space-x-2 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
              <FiFilter />
              <span>Lọc nâng cao</span>
            </button>
          </div>
        </div>

        {/* Transactions Table */}
        <div className="bg-white rounded-lg shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Mã phiếu</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Loại</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Người tạo</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Thời gian</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tổng tiền</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Thao tác</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredTransactions.map((trans) => (
                  <tr key={trans.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">{trans.transactionCode}</div>
                      {trans.note && (
                        <div className="text-xs text-gray-500">{trans.note}</div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 py-1 text-xs font-semibold rounded ${
                        trans.type === 'IMPORT' 
                          ? 'bg-green-100 text-green-800' 
                          : 'bg-blue-100 text-blue-800'
                      }`}>
                        {trans.type === 'IMPORT' ? 'Nhập kho' : 'Xuất kho'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {trans.createdBy}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatDate(trans.createdAt)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {formatPrice(trans.totalAmount)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getStatusBadge(trans.status)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <Link
                        href={`/admin/inventory/transactions/${trans.id}`}
                        className="text-red-500 hover:text-red-600 inline-flex items-center space-x-1"
                      >
                        <FiEye />
                        <span>Chi tiết</span>
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {filteredTransactions.length === 0 && (
            <div className="text-center py-12">
              <p className="text-gray-500">Không tìm thấy phiếu nào</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
