'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiPackage, FiPlus, FiDownload, FiUpload, FiSearch, FiFileText, FiEye } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useTranslation } from '@/hooks/useTranslation'
import { useAuthStore } from '@/store/authStore'
import { inventoryApi } from '@/lib/api'

type TabType = 'inventory' | 'transactions'
type TransactionType = 'all' | 'import' | 'export'

export default function InventoryPage() {
  const { t } = useTranslation()
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  
  const [activeTab, setActiveTab] = useState<TabType>('inventory')
  const [inventory, setInventory] = useState<any[]>([])
  const [purchaseOrders, setPurchaseOrders] = useState<any[]>([])
  const [exportOrders, setExportOrders] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [transactionType, setTransactionType] = useState<TransactionType>('all')
  const [statusFilter, setStatusFilter] = useState('')

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

    loadInventory()
  }, [isAuthenticated, user, router])

  const loadInventory = async () => {
    try {
      const response = await inventoryApi.getStocks()
      console.log('Inventory Response:', response)
      
      if (response.success && response.data) {
        // Map data from API to display format
        const mappedData = response.data.map((stock: any) => ({
          id: stock.id,
          name: stock.warehouseProduct?.internalName || 'N/A',
          sku: stock.warehouseProduct?.sku || 'N/A',
          category: 'N/A', // TODO: Add category mapping if needed
          price: 0, // Price is not in inventory_stock, might need to join with product table
          quantity: stock.onHand || 0,
          reserved: stock.reserved || 0,
          damaged: stock.damaged || 0,
          sellable: stock.sellable || 0,
          supplier: stock.warehouseProduct?.supplier?.name || 'N/A',
          lastImportDate: stock.warehouseProduct?.lastImportDate || null,
          description: stock.warehouseProduct?.description || '',
          image: '/images/placeholder.jpg'
        }))
        setInventory(mappedData)
      } else {
        setInventory([])
      }
    } catch (error) {
      console.error('Error loading inventory:', error)
      toast.error('Lỗi khi tải kho hàng')
      setInventory([])
    } finally {
      setLoading(false)
    }
  }

  const loadTransactions = async () => {
    try {
      setLoading(true)
      
      // Load purchase orders
      const poResponse = await inventoryApi.getPurchaseOrders(statusFilter || undefined)
      console.log('Purchase Orders Response:', poResponse)
      setPurchaseOrders(poResponse.data || [])
      
      // Load export orders
      const eoResponse = await inventoryApi.getExportOrders(statusFilter || undefined)
      console.log('Export Orders Response:', eoResponse)
      setExportOrders(eoResponse.data || [])
      
      console.log('Total transactions:', {
        purchaseOrders: poResponse.data?.length || 0,
        exportOrders: eoResponse.data?.length || 0
      })
    } catch (error) {
      console.error('Error loading transactions:', error)
      toast.error('Lỗi khi tải phiếu xuất nhập')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (activeTab === 'transactions') {
      loadTransactions()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab, statusFilter])

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  const filteredInventory = inventory.filter(item =>
    item.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    item.sku?.toLowerCase().includes(searchQuery.toLowerCase())
  )

  const filteredTransactions = () => {
    let transactions: any[] = []
    
    if (transactionType === 'all') {
      transactions = [
        ...purchaseOrders.map(po => ({ ...po, type: 'IMPORT' })),
        ...exportOrders.map(eo => ({ ...eo, type: 'EXPORT' }))
      ]
    } else if (transactionType === 'import') {
      transactions = purchaseOrders.map(po => ({ ...po, type: 'IMPORT' }))
    } else {
      transactions = exportOrders.map(eo => ({ ...eo, type: 'EXPORT' }))
    }

    return transactions.filter(t =>
      t.poCode?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      t.exportCode?.toLowerCase().includes(searchQuery.toLowerCase())
    )
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getStatusBadge = (status: string) => {
    const statusMap: Record<string, { label: string; className: string }> = {
      CREATED: { label: 'Chờ xử lý', className: 'bg-yellow-100 text-yellow-800' },
      RECEIVED: { label: 'Đã nhập', className: 'bg-green-100 text-green-800' },
      COMPLETED: { label: 'Hoàn thành', className: 'bg-blue-100 text-blue-800' },
      CANCELLED: { label: 'Đã hủy', className: 'bg-red-100 text-red-800' }
    }
    const config = statusMap[status] || { label: status, className: 'bg-gray-100 text-gray-800' }
    return (
      <span className={`px-2 py-1 text-xs font-semibold rounded ${config.className}`}>
        {config.label}
      </span>
    )
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
          <Link href="/" className="hover:text-red-500">{t('home')}</Link>
          <span>/</span>
          <Link href="/admin" className="hover:text-red-500">Quản trị</Link>
          <span>/</span>
          <span className="text-gray-900">Quản lý kho</span>
        </nav>

        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 space-y-4 md:space-y-0">
          <h1 className="text-3xl font-bold text-gray-900">Quản lý kho</h1>
          
          <div className="flex space-x-2">
            <Link
              href="/admin/inventory/transactions/create?type=IMPORT"
              className="flex items-center space-x-2 bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 transition-colors"
            >
              <FiDownload />
              <span>Nhập hàng</span>
            </Link>
            <Link
              href="/admin/inventory/transactions/create?type=EXPORT"
              className="flex items-center space-x-2 bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
            >
              <FiUpload />
              <span>Xuất hàng</span>
            </Link>
            <Link
              href="/admin/products/create"
              className="flex items-center space-x-2 bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition-colors"
            >
              <FiPlus />
              <span>Thêm sản phẩm</span>
            </Link>
          </div>
        </div>

        {/* Tabs */}
        <div className="mb-6 border-b border-gray-200">
          <nav className="flex space-x-8">
            <button
              onClick={() => setActiveTab('inventory')}
              className={`pb-4 px-1 border-b-2 font-medium text-sm transition-colors ${
                activeTab === 'inventory'
                  ? 'border-red-500 text-red-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              <div className="flex items-center space-x-2">
                <FiPackage />
                <span>Tồn kho</span>
              </div>
            </button>
            <button
              onClick={() => setActiveTab('transactions')}
              className={`pb-4 px-1 border-b-2 font-medium text-sm transition-colors ${
                activeTab === 'transactions'
                  ? 'border-red-500 text-red-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              <div className="flex items-center space-x-2">
                <FiFileText />
                <span>Phiếu xuất nhập</span>
              </div>
            </button>
          </nav>
        </div>

        {/* Search and Filter */}
        <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
          <div className="flex flex-col md:flex-row space-y-4 md:space-y-0 md:space-x-4">
            <div className="flex-1 relative">
              <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder={activeTab === 'inventory' ? 'Tìm kiếm sản phẩm...' : 'Tìm kiếm phiếu...'}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
              />
            </div>
            
            {activeTab === 'inventory' ? (
              <>
                <select className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500">
                  <option value="">Tất cả trạng thái</option>
                  <option value="in-stock">Còn hàng (&gt;10)</option>
                  <option value="low-stock">Sắp hết (1-10)</option>
                  <option value="out-of-stock">Hết hàng (0)</option>
                </select>
              </>
            ) : (
              <>
                <select 
                  value={transactionType}
                  onChange={(e) => setTransactionType(e.target.value as TransactionType)}
                  className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                >
                  <option value="all">Tất cả loại phiếu</option>
                  <option value="import">Phiếu nhập</option>
                  <option value="export">Phiếu xuất</option>
                </select>
                <select 
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                >
                  <option value="">Tất cả trạng thái</option>
                  <option value="CREATED">Chờ xử lý</option>
                  <option value="RECEIVED">Đã nhập</option>
                  <option value="COMPLETED">Hoàn thành</option>
                  <option value="CANCELLED">Đã hủy</option>
                </select>
              </>
            )}
          </div>
        </div>

        {/* Content based on active tab */}
        {activeTab === 'inventory' ? (
          /* Inventory Table */
          filteredInventory.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <FiPackage size={64} className="mx-auto text-gray-400 mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              Chưa có sản phẩm nào
            </h3>
            <p className="text-gray-600 mb-6">
              Bắt đầu thêm sản phẩm vào kho để quản lý
            </p>
            <button className="bg-red-500 text-white px-6 py-3 rounded-lg hover:bg-red-600 transition-colors">
              Thêm sản phẩm đầu tiên
            </button>
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Sản phẩm
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      SKU
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Nhà cung cấp
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Tồn kho
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Đã giữ
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Có thể bán
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Trạng thái
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Thao tác
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredInventory.map((item) => (
                    <tr key={item.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="w-10 h-10 bg-gray-100 rounded flex-shrink-0"></div>
                          <div className="ml-4">
                            <div className="text-sm font-medium text-gray-900">{item.name}</div>
                            {item.description && (
                              <div className="text-xs text-gray-500 truncate max-w-xs">{item.description}</div>
                            )}
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {item.sku}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {item.supplier}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {item.quantity}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {item.reserved || 0}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {item.sellable || 0}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-2 py-1 text-xs font-semibold rounded ${
                          (item.sellable || 0) > 10
                            ? 'bg-green-100 text-green-800'
                            : (item.sellable || 0) > 0
                            ? 'bg-yellow-100 text-yellow-800'
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {(item.sellable || 0) > 10 ? 'Còn hàng' : (item.sellable || 0) > 0 ? 'Sắp hết' : 'Hết hàng'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <button className="text-red-500 hover:text-red-600 mr-3">
                          Chi tiết
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )
        ) : (
          /* Transactions Table */
          <div className="bg-white rounded-lg shadow-sm overflow-hidden">
            {filteredTransactions().length === 0 ? (
              <div className="p-12 text-center">
                <FiFileText size={64} className="mx-auto text-gray-400 mb-4" />
                <h3 className="text-xl font-semibold text-gray-900 mb-2">
                  Chưa có phiếu xuất nhập nào
                </h3>
                <p className="text-gray-600">
                  Các phiếu xuất nhập sẽ hiển thị ở đây
                </p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Mã phiếu
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Loại
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Ngày tạo
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Người tạo
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Trạng thái
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Ghi chú
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Thao tác
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {filteredTransactions().map((transaction) => (
                      <tr key={`${transaction.type}-${transaction.id}`} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {transaction.poCode || transaction.exportCode}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`px-2 py-1 text-xs font-semibold rounded ${
                            transaction.type === 'IMPORT'
                              ? 'bg-green-100 text-green-800'
                              : 'bg-blue-100 text-blue-800'
                          }`}>
                            {transaction.type === 'IMPORT' ? 'Nhập hàng' : 'Xuất hàng'}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatDate(transaction.orderDate || transaction.exportDate)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {transaction.createdBy || 'N/A'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getStatusBadge(transaction.status)}
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-500 max-w-xs truncate">
                          {transaction.note || transaction.reason || '-'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <Link 
                            href={`/admin/inventory/transactions/${transaction.id}?type=${transaction.type}`}
                            className="text-red-500 hover:text-red-600 flex items-center space-x-1 ml-auto"
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
            )}
          </div>
        )}
      </div>
    </div>
  )
}
