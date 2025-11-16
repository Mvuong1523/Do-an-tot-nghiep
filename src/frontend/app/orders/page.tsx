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

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập để xem đơn hàng')
      router.push('/login')
      return
    }

    const loadOrders = async () => {
      try {
        // TODO: Khi backend có Order API, gọi API thay vì đọc localStorage
        // const response = await orderApi.getAll()
        // if (response.success && response.data) {
        //   setOrders(response.data)
        // }

        // Tạm thời đọc từ localStorage
        const storedOrders = JSON.parse(localStorage.getItem('orders') || '[]')
        setOrders(storedOrders)
      } catch (error) {
        toast.error('Lỗi khi tải đơn hàng')
      } finally {
        setLoading(false)
      }
    }

    loadOrders()
  }, [isAuthenticated, router])

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
      case 'pending':
        return <FiClock className="text-yellow-500" size={20} />
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
      case 'pending':
        return 'Chờ xác nhận'
      case 'processing':
        return 'Đang xử lý'
      case 'shipping':
        return 'Đang giao hàng'
      case 'delivered':
        return 'Đã giao hàng'
      case 'cancelled':
        return 'Đã hủy'
      default:
        return status
    }
  }

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending':
        return 'bg-yellow-100 text-yellow-800'
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

  const filteredOrders = filter === 'all' 
    ? orders 
    : orders.filter(order => order.status.toLowerCase() === filter)

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
              { key: 'pending', label: 'Chờ xác nhận' },
              { key: 'processing', label: 'Đang xử lý' },
              { key: 'shipping', label: 'Đang giao' },
              { key: 'delivered', label: 'Đã giao' },
              { key: 'cancelled', label: 'Đã hủy' },
            ].map((tab) => (
              <button
                key={tab.key}
                onClick={() => setFilter(tab.key)}
                className={`px-6 py-4 font-medium whitespace-nowrap border-b-2 transition-colors ${
                  filter === tab.key
                    ? 'border-red-500 text-red-500'
                    : 'border-transparent text-gray-600 hover:text-gray-900'
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
              <div key={order.id} className="bg-white rounded-lg shadow-sm overflow-hidden">
                {/* Order Header */}
                <div className="p-6 border-b border-gray-200">
                  <div className="flex flex-col md:flex-row md:items-center md:justify-between">
                    <div className="flex items-center space-x-4 mb-4 md:mb-0">
                      {getStatusIcon(order.status)}
                      <div>
                        <div className="flex items-center space-x-2">
                          <span className="font-semibold text-gray-900">
                            Đơn hàng #{order.id}
                          </span>
                          <span className={`px-2 py-1 text-xs font-semibold rounded ${getStatusColor(order.status)}`}>
                            {getStatusText(order.status)}
                          </span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">
                          Đặt ngày: {formatDate(order.createdAt || new Date().toISOString())}
                        </p>
                      </div>
                    </div>
                    <Link
                      href={`/orders/${order.id}`}
                      className="inline-flex items-center text-red-500 hover:text-red-600 font-medium"
                    >
                      <FiEye className="mr-2" />
                      Xem chi tiết
                    </Link>
                  </div>
                </div>

                {/* Order Items */}
                <div className="p-6">
                  <div className="space-y-4">
                    {order.items && order.items.map((item: any, index: number) => (
                      <div key={index} className="flex items-center space-x-4">
                        <div className="w-20 h-20 bg-gray-100 rounded-lg flex-shrink-0"></div>
                        <div className="flex-1">
                          <h4 className="font-semibold text-gray-900">
                            {item.productName || `Sản phẩm #${item.productId}`}
                          </h4>
                          <p className="text-sm text-gray-600">Số lượng: {item.quantity || 1}</p>
                        </div>
                        <div className="text-right">
                          <div className="font-semibold text-gray-900">
                            {formatPrice(item.price || 0)}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Order Total */}
                  <div className="mt-6 pt-6 border-t border-gray-200">
                    <div className="flex justify-between items-center">
                      <span className="text-gray-600">Tổng cộng:</span>
                      <span className="text-2xl font-bold text-red-500">
                        {formatPrice(order.totalAmount || 0)}
                      </span>
                    </div>
                  </div>

                  {/* Shipping Address */}
                  {order.shippingAddress && (
                    <div className="mt-4 p-4 bg-gray-50 rounded-lg">
                      <h5 className="font-semibold text-gray-900 mb-2">Địa chỉ giao hàng:</h5>
                      <p className="text-gray-700">{order.shippingAddress}</p>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
