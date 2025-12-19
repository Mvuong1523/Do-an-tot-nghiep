'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { FiPackage, FiTruck, FiCheck, FiX, FiClock, FiEye } from 'react-icons/fi'
import { useTranslation } from '@/hooks/useTranslation'
import { useAuthStore } from '@/store/authStore'
import { orderApi } from '@/lib/api'
import { useRouter } from 'next/navigation'
import toast from 'react-hot-toast'

export default function OrdersPage() {
  const { t } = useTranslation()
  const router = useRouter()
  const { isAuthenticated } = useAuthStore()
  const [orders, setOrders] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all')
  const [mounted, setMounted] = useState(false)

  // Wait for hydration
  useEffect(() => {
    setMounted(true)
  }, [])

  useEffect(() => {
    if (!mounted) return
    
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập để xem đơn hàng')
      router.push('/login')
      return
    }

    const loadOrders = async () => {
      try {
        console.log('Loading orders...')
        const response = await orderApi.getAll()
        console.log('Orders response:', response)
        
        if (response.success && response.data) {
          // Backend trả về {success, message, data: [...]}
          const ordersData = Array.isArray(response.data) ? response.data : []
          console.log('Orders data:', ordersData)
          setOrders(ordersData)
        } else {
          console.warn('No orders data')
          setOrders([])
        }
      } catch (error) {
        console.error('Error loading orders:', error)
        toast.error('Lỗi khi tải đơn hàng')
        setOrders([])
      } finally {
        setLoading(false)
      }
    }

    loadOrders()
  }, [mounted, isAuthenticated, router])

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending_payment':
        return <FiClock className="text-orange-500" size={20} />
      case 'pending':
        return <FiClock className="text-yellow-500" size={20} />
      case 'confirmed':
        return <FiPackage className="text-blue-500" size={20} />
      case 'ready_to_ship':
        return <FiTruck className="text-purple-600" size={20} />
      case 'processing':
        return <FiPackage className="text-blue-500" size={20} />
      case 'shipping':
        return <FiTruck className="text-purple-500" size={20} />
      case 'delivered':
        return <FiCheck className="text-green-500" size={20} />
      case 'cancelled':
        return <FiX className="text-red-500" size={20} />
      default:
        return <FiPackage className="text-gray-500" size={20} />
    }
  }

  const getStatusText = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending_payment':
        return 'Chờ thanh toán'
      case 'pending':
        return 'Chờ xác nhận'
      case 'confirmed':
        return 'Đã xác nhận'
      case 'ready_to_ship':
        return 'Đã chuẩn bị hàng - Đợi tài xế'
      case 'shipping':
        return 'Đang giao hàng'
      case 'delivered':
        return 'Đã giao hàng'
      case 'cancelled':
        return 'Đã hủy'
      case 'processing':
        return 'Đang xử lý'
      default:
        return status
    }
  }

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending_payment':
        return 'bg-orange-100 text-orange-800'
      case 'pending':
        return 'bg-yellow-100 text-yellow-800'
      case 'confirmed':
        return 'bg-blue-100 text-blue-800'
      case 'ready_to_ship':
        return 'bg-purple-100 text-purple-800 border-2 border-purple-300'
      case 'processing':
        return 'bg-blue-100 text-blue-800'
      case 'shipping':
        return 'bg-purple-100 text-purple-800'
      case 'delivered':
        return 'bg-green-100 text-green-800'
      case 'cancelled':
        return 'bg-red-100 text-red-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  const filteredOrders = Array.isArray(orders) 
    ? (filter === 'all' 
        ? orders 
        : orders.filter(order => order.status?.toUpperCase() === filter.toUpperCase()))
    : []

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải đơn hàng...</p>
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
          <span className="text-gray-900">Đơn hàng của tôi</span>
        </nav>

        <h1 className="text-3xl font-bold text-gray-900 mb-8">Đơn hàng của tôi</h1>

        {/* Filter Tabs */}
        <div className="bg-white rounded-lg shadow-sm mb-6">
          <div className="flex overflow-x-auto">
            {[
              { key: 'all', label: 'Tất cả' },
              { key: 'pending_payment', label: 'Chờ thanh toán' },
              { key: 'confirmed', label: 'Đã xác nhận' },
              { key: 'ready_to_ship', label: '🚚 Đợi tài xế lấy hàng', highlight: true },
              { key: 'shipping', label: 'Đang giao' },
              { key: 'delivered', label: 'Đã giao' },
              { key: 'cancelled', label: 'Đã hủy' },
            ].map((tab) => (
              <button
                key={tab.key}
                onClick={() => setFilter(tab.key)}
                className={`px-6 py-4 font-medium whitespace-nowrap border-b-2 transition-colors ${
                  filter === tab.key
                    ? (tab.highlight ? 'border-purple-500 text-purple-600 bg-purple-50' : 'border-red-500 text-red-500')
                    : (tab.highlight ? 'border-transparent text-purple-600 hover:text-purple-700 hover:bg-purple-50' : 'border-transparent text-gray-600 hover:text-gray-900')
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {/* Orders List */}
        {filteredOrders.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <FiPackage size={64} className="mx-auto text-gray-400 mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              Chưa có đơn hàng nào
            </h3>
            <p className="text-gray-600 mb-6">
              Bạn chưa có đơn hàng nào trong danh mục này
            </p>
            <Link
              href="/products"
              className="inline-block bg-red-500 text-white px-6 py-3 rounded-lg font-semibold hover:bg-red-600 transition-colors"
            >
              Tiếp tục mua sắm
            </Link>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredOrders.map((order) => (
              <div key={order.orderId} className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
                <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                  {/* Left: Order Info */}
                  <div className="flex items-center space-x-4 flex-1">
                    {getStatusIcon(order.status)}
                    <div className="flex-1">
                      <div className="flex items-center space-x-2 mb-1">
                        <span className="font-bold text-gray-900 text-lg">
                          {order.orderCode}
                        </span>
                        <span className={`px-3 py-1 text-xs font-semibold rounded-full ${getStatusColor(order.status)}`}>
                          {getStatusText(order.status)}
                        </span>
                      </div>
                      <p className="text-sm text-gray-600">
                        {formatDate(order.createdAt || new Date().toISOString())}
                      </p>
                    </div>
                  </div>

                  {/* Center: Total */}
                  <div className="text-center md:text-right">
                    <p className="text-sm text-gray-600 mb-1">Tổng tiền</p>
                    <p className="text-2xl font-bold text-red-600">
                      {formatPrice(order.total || 0)}
                    </p>
                  </div>

                  {/* Right: Action Buttons */}
                  <div className="flex flex-col gap-2">
                    <Link
                      href={`/orders/${order.orderId}`}
                      className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
                    >
                      <FiEye className="mr-2" />
                      Xem chi tiết
                    </Link>
                    
                    {/* Nút cập nhật sang Đang giao - CHỈ hiện khi READY_TO_SHIP */}
                    {order.status?.toUpperCase() === 'READY_TO_SHIP' && (
                      <button
                        onClick={async () => {
                          if (confirm('⚠️ Xác nhận chuyển đơn hàng sang "Đang giao hàng"?\n\n✅ Chỉ nhấn khi tài xế đã đến lấy hàng hoặc hàng đã được giao cho đơn vị vận chuyển.')) {
                            try {
                              const { adminOrderApi } = await import('@/lib/api')
                              const response = await adminOrderApi.markShippingFromReady(order.orderId)
                              if (response.success) {
                                toast.success('✅ Đã cập nhật sang "Đang giao hàng"')
                                // Reload orders
                                const ordersResponse = await orderApi.getAll()
                                if (ordersResponse.success && ordersResponse.data) {
                                  setOrders(Array.isArray(ordersResponse.data) ? ordersResponse.data : [])
                                }
                              }
                            } catch (error: any) {
                              toast.error(error.message || 'Lỗi khi cập nhật trạng thái')
                            }
                          }
                        }}
                        className="inline-flex items-center px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors font-medium shadow-md hover:shadow-lg"
                      >
                        <FiTruck className="mr-2" />
                        🚚 Chuyển sang Đang giao
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
