'use client'

import { useState, useEffect } from 'react'
import { FiPackage, FiCheck, FiTruck, FiClock, FiX, FiEye } from 'react-icons/fi'
import { adminOrderApi } from '@/lib/api'
import toast from 'react-hot-toast'
import Link from 'next/link'

export default function OrderManagementPage() {
  const [orders, setOrders] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('ALL')
  const [processingId, setProcessingId] = useState<number | null>(null)

  useEffect(() => {
    loadOrders()
  }, [filter])

  const loadOrders = async () => {
    try {
      setLoading(true)
      
      // Debug: Check token
      const token = localStorage.getItem('token') || localStorage.getItem('auth_token')
      console.log('🔑 Token exists:', !!token)
      console.log('🔑 Token preview:', token?.substring(0, 50) + '...')
      
      const response = await adminOrderApi.getAll(filter === 'ALL' ? undefined : filter)
      
      if (response.success && response.data) {
        const ordersData = Array.isArray(response.data) ? response.data : []
        setOrders(ordersData)
      } else {
        setOrders([])
      }
    } catch (error: any) {
      console.error('Error loading orders:', error)
      console.error('Error response:', error.response?.data)
      console.error('Error status:', error.response?.status)
      toast.error('Lỗi khi tải đơn hàng')
      setOrders([])
    } finally {
      setLoading(false)
    }
  }

  const handleConfirmOrder = async (orderId: number) => {
    if (!confirm('Xác nhận đơn hàng này?')) return
    
    try {
      setProcessingId(orderId)
      const response = await adminOrderApi.confirmOrder(orderId)
      
      if (response.success) {
        toast.success('Đã xác nhận đơn hàng')
        loadOrders()
      } else {
        toast.error(response.message || 'Không thể xác nhận đơn hàng')
      }
    } catch (error: any) {
      toast.error(error.message || 'Lỗi khi xác nhận đơn hàng')
    } finally {
      setProcessingId(null)
    }
  }

  const handleMarkAsShipping = async (orderId: number) => {
    if (!confirm('Đánh dấu đơn hàng đang giao?')) return
    
    try {
      setProcessingId(orderId)
      const response = await adminOrderApi.markAsShipping(orderId)
      
      if (response.success) {
        toast.success('Đã chuyển sang đang giao hàng')
        loadOrders()
      } else {
        toast.error(response.message || 'Không thể cập nhật trạng thái')
      }
    } catch (error: any) {
      toast.error(error.message || 'Lỗi khi cập nhật trạng thái')
    } finally {
      setProcessingId(null)
    }
  }

  const handleMarkAsDelivered = async (orderId: number) => {
    if (!confirm('Xác nhận đã giao hàng thành công?')) return
    
    try {
      setProcessingId(orderId)
      const response = await adminOrderApi.markAsDelivered(orderId)
      
      if (response.success) {
        toast.success('Đã xác nhận giao hàng thành công')
        loadOrders()
      } else {
        toast.error(response.message || 'Không thể cập nhật trạng thái')
      }
    } catch (error: any) {
      toast.error(error.message || 'Lỗi khi cập nhật trạng thái')
    } finally {
      setProcessingId(null)
    }
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  const formatDate = (dateString: string) => {
    if (!dateString) return ''
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getStatusText = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'PENDING_PAYMENT': return 'Chờ thanh toán'
      case 'PENDING': return 'Chờ xác nhận'
      case 'CONFIRMED': return 'Đã xác nhận'
      case 'SHIPPING': return 'Đang giao'
      case 'DELIVERED': return 'Đã giao'
      case 'CANCELLED': return 'Đã hủy'
      default: return status
    }
  }

  const getStatusColor = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'PENDING_PAYMENT': return 'bg-orange-100 text-orange-800'
      case 'PENDING': return 'bg-yellow-100 text-yellow-800'
      case 'CONFIRMED': return 'bg-blue-100 text-blue-800'
      case 'SHIPPING': return 'bg-purple-100 text-purple-800'
      case 'DELIVERED': return 'bg-green-100 text-green-800'
      case 'CANCELLED': return 'bg-red-100 text-red-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'PENDING_PAYMENT': return <FiClock className="text-orange-600" size={20} />
      case 'PENDING': return <FiClock className="text-yellow-600" size={20} />
      case 'CONFIRMED': return <FiCheck className="text-blue-600" size={20} />
      case 'SHIPPING': return <FiTruck className="text-purple-600" size={20} />
      case 'DELIVERED': return <FiCheck className="text-green-600" size={20} />
      case 'CANCELLED': return <FiX className="text-red-600" size={20} />
      default: return <FiPackage className="text-gray-600" size={20} />
    }
  }

  const getActionButtons = (order: any) => {
    const isProcessing = processingId === order.orderId
    
    switch (order.status?.toUpperCase()) {
      case 'PENDING':
        return (
          <button
            onClick={() => handleConfirmOrder(order.orderId)}
            disabled={isProcessing}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 transition-colors text-sm font-medium"
          >
            {isProcessing ? 'Đang xử lý...' : 'Xác nhận'}
          </button>
        )
      case 'CONFIRMED':
        return (
          <button
            onClick={() => handleMarkAsShipping(order.orderId)}
            disabled={isProcessing}
            className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:bg-gray-400 transition-colors text-sm font-medium"
          >
            {isProcessing ? 'Đang xử lý...' : 'Đang giao'}
          </button>
        )
      case 'SHIPPING':
        return (
          <button
            onClick={() => handleMarkAsDelivered(order.orderId)}
            disabled={isProcessing}
            className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-gray-400 transition-colors text-sm font-medium"
          >
            {isProcessing ? 'Đang xử lý...' : 'Đã giao'}
          </button>
        )
      default:
        return null
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Quản lý đơn hàng</h1>
        <div className="text-sm text-gray-600">
          Tổng: <span className="font-semibold">{orders.length}</span> đơn hàng
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="bg-white rounded-lg shadow-sm">
        <div className="flex overflow-x-auto">
          {[
            { key: 'ALL', label: 'Tất cả', count: orders.length },
            { key: 'PENDING_PAYMENT', label: 'Chờ thanh toán' },
            { key: 'CONFIRMED', label: 'Đã xác nhận' },
            { key: 'SHIPPING', label: 'Đang giao' },
            { key: 'DELIVERED', label: 'Đã giao' },
            { key: 'CANCELLED', label: 'Đã hủy' },
          ].map((tab) => (
            <button
              key={tab.key}
              onClick={() => setFilter(tab.key)}
              className={`px-6 py-4 font-medium whitespace-nowrap border-b-2 transition-colors ${
                filter === tab.key
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Orders List */}
      {orders.length === 0 ? (
        <div className="bg-white rounded-lg shadow-sm p-12 text-center">
          <FiPackage size={64} className="mx-auto text-gray-400 mb-4" />
          <h3 className="text-xl font-semibold text-gray-900 mb-2">
            Không có đơn hàng nào
          </h3>
          <p className="text-gray-600">
            Chưa có đơn hàng nào trong danh mục này
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <div key={order.orderId} className="bg-white rounded-lg shadow-sm p-6">
              <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
                {/* Left: Order Info */}
                <div className="flex items-start space-x-4 flex-1">
                  <div className="mt-1">
                    {getStatusIcon(order.status)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center space-x-2 mb-2">
                      <span className="font-bold text-gray-900 text-lg">
                        {order.orderCode}
                      </span>
                      <span className={`px-3 py-1 text-xs font-semibold rounded-full ${getStatusColor(order.status)}`}>
                        {getStatusText(order.status)}
                      </span>
                    </div>
                    <div className="space-y-1 text-sm text-gray-600">
                      <p><strong>Khách hàng:</strong> {order.customerName} - {order.customerPhone}</p>
                      <p><strong>Ngày đặt:</strong> {formatDate(order.createdAt)}</p>
                      <p className="truncate"><strong>Địa chỉ:</strong> {order.shippingAddress}</p>
                    </div>
                  </div>
                </div>

                {/* Center: Total */}
                <div className="text-center lg:text-right">
                  <p className="text-sm text-gray-600 mb-1">Tổng tiền</p>
                  <p className="text-xl font-bold text-red-600">
                    {formatPrice(order.total)}
                  </p>
                  <p className="text-xs text-gray-500 mt-1">
                    {order.items?.length || 0} sản phẩm
                  </p>
                </div>

                {/* Right: Actions */}
                <div className="flex items-center space-x-2">
                  {getActionButtons(order)}
                  <Link
                    href={`/orders/${order.orderId}`}
                    className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors text-sm font-medium inline-flex items-center"
                  >
                    <FiEye className="mr-2" />
                    Chi tiết
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
