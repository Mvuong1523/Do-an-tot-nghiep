'use client'

import { useEffect, useState } from 'react'
import { useRouter, useParams } from 'next/navigation'
import Link from 'next/link'
import { FiArrowLeft, FiPackage, FiMapPin, FiCreditCard, FiClock, FiFileText } from 'react-icons/fi'
import { orderApi } from '@/lib/api'
import { useAuthStore } from '@/store/authStore'
import toast from 'react-hot-toast'

export default function OrderDetailPage() {
  const router = useRouter()
  const params = useParams()
  const orderId = params.id as string
  const { isAuthenticated } = useAuthStore()
  
  const [loading, setLoading] = useState(true)
  const [order, setOrder] = useState<any>(null)

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }
    
    if (!orderId) {
      toast.error('Không tìm thấy thông tin đơn hàng')
      router.push('/orders')
      return
    }
    
    loadOrderDetails()
  }, [orderId, isAuthenticated])

  const loadOrderDetails = async () => {
    try {
      const response = await orderApi.getById(orderId)
      console.log('Order detail response:', response)
      
      if (response.success && response.data) {
        setOrder(response.data)
      } else {
        toast.error('Không thể tải thông tin đơn hàng')
        router.push('/orders')
      }
    } catch (error) {
      console.error('Error loading order:', error)
      toast.error('Lỗi khi tải thông tin đơn hàng')
      router.push('/orders')
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
    if (!dateString) return ''
    try {
      const date = new Date(dateString)
      return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    } catch {
      return dateString
    }
  }

  const getStatusText = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return 'Chờ xác nhận'
      case 'CONFIRMED':
        return 'Đã xác nhận'
      case 'PROCESSING':
        return 'Đang xử lý'
      case 'SHIPPING':
        return 'Đang giao hàng'
      case 'DELIVERED':
        return 'Đã giao hàng'
      case 'CANCELLED':
        return 'Đã hủy'
      default:
        return status
    }
  }

  const getStatusColor = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800'
      case 'CONFIRMED':
        return 'bg-blue-100 text-blue-800'
      case 'PROCESSING':
        return 'bg-blue-100 text-blue-800'
      case 'SHIPPING':
        return 'bg-purple-100 text-purple-800'
      case 'DELIVERED':
        return 'bg-green-100 text-green-800'
      case 'CANCELLED':
        return 'bg-red-100 text-red-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  const getPaymentStatusText = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'UNPAID':
        return 'Chưa thanh toán'
      case 'PAID':
        return 'Đã thanh toán'
      case 'REFUNDED':
        return 'Đã hoàn tiền'
      default:
        return status
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải thông tin đơn hàng...</p>
        </div>
      </div>
    )
  }

  if (!order) {
    return null
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4 max-w-4xl">
        {/* Back Button */}
        <Link
          href="/orders"
          className="inline-flex items-center text-gray-600 hover:text-gray-900 mb-6"
        >
          <FiArrowLeft className="mr-2" />
          Quay lại danh sách đơn hàng
        </Link>

        {/* Order Header */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between mb-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900 mb-2">
                Đơn hàng {order.orderCode}
              </h1>
              <p className="text-gray-600">
                Đặt ngày: {formatDate(order.createdAt)}
              </p>
            </div>
            <div className="mt-4 md:mt-0">
              <span className={`px-4 py-2 rounded-lg font-semibold ${getStatusColor(order.status)}`}>
                {getStatusText(order.status)}
              </span>
            </div>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t">
            <div>
              <p className="text-sm text-gray-600">Trạng thái thanh toán</p>
              <p className="font-medium text-gray-900">{getPaymentStatusText(order.paymentStatus)}</p>
            </div>
            {order.confirmedAt && (
              <div>
                <p className="text-sm text-gray-600">Xác nhận lúc</p>
                <p className="font-medium text-gray-900">{formatDate(order.confirmedAt)}</p>
              </div>
            )}
          </div>
        </div>

        {/* Order Items */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
            <FiPackage className="mr-2" />
            Sản phẩm
          </h2>

          <div className="space-y-4">
            {order.items && order.items.map((item: any, index: number) => (
              <div key={index} className="flex items-center space-x-4 pb-4 border-b last:border-b-0">
                <div className="w-20 h-20 bg-gray-100 rounded flex-shrink-0 overflow-hidden">
                  {item.productImage ? (
                    <img 
                      src={item.productImage} 
                      alt={item.productName} 
                      className="w-full h-full object-contain" 
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center">
                      <FiPackage className="text-gray-400" />
                    </div>
                  )}
                </div>
                <div className="flex-1">
                  <p className="font-medium text-gray-900">{item.productName}</p>
                  <p className="text-sm text-gray-600">Số lượng: {item.quantity}</p>
                </div>
                <div className="text-right">
                  <p className="font-medium text-gray-900">{formatPrice(item.subtotal || (item.price * item.quantity))}</p>
                  <p className="text-sm text-gray-600">{formatPrice(item.price)} x {item.quantity}</p>
                </div>
              </div>
            ))}
          </div>

          {/* Totals */}
          <div className="space-y-2 pt-4 mt-4 border-t">
            <div className="flex justify-between text-gray-600">
              <span>Tạm tính</span>
              <span>{formatPrice(order.subtotal || 0)}</span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Phí vận chuyển</span>
              <span>{order.shippingFee ? formatPrice(order.shippingFee) : 'Miễn phí'}</span>
            </div>
            {order.discount > 0 && (
              <div className="flex justify-between text-green-600">
                <span>Giảm giá</span>
                <span>-{formatPrice(order.discount)}</span>
              </div>
            )}
            <div className="flex justify-between text-lg font-bold text-gray-900 pt-2 border-t">
              <span>Tổng cộng</span>
              <span className="text-red-600">{formatPrice(order.total)}</span>
            </div>
          </div>
        </div>

        {/* Delivery Info */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
            <FiMapPin className="mr-2" />
            Thông tin giao hàng
          </h2>
          
          <div className="space-y-3">
            <div>
              <p className="text-sm text-gray-600">Người nhận</p>
              <p className="font-medium text-gray-900">{order.customerName}</p>
            </div>
            
            <div>
              <p className="text-sm text-gray-600">Số điện thoại</p>
              <p className="font-medium text-gray-900">{order.customerPhone}</p>
            </div>
            
            <div>
              <p className="text-sm text-gray-600">Email</p>
              <p className="font-medium text-gray-900">{order.customerEmail}</p>
            </div>
            
            <div>
              <p className="text-sm text-gray-600">Địa chỉ giao hàng</p>
              <p className="font-medium text-gray-900">{order.shippingAddress}</p>
            </div>
            
            {order.note && (
              <div>
                <p className="text-sm text-gray-600">Ghi chú</p>
                <p className="font-medium text-gray-900">{order.note}</p>
              </div>
            )}
          </div>
        </div>

        {/* Order Timeline */}
        {(order.confirmedAt || order.shippedAt || order.deliveredAt || order.cancelledAt) && (
          <div className="bg-white rounded-lg shadow-sm p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
              <FiClock className="mr-2" />
              Lịch sử đơn hàng
            </h2>
            
            <div className="space-y-4">
              <div className="flex items-start">
                <div className="w-2 h-2 bg-blue-600 rounded-full mt-2 mr-4"></div>
                <div>
                  <p className="font-medium text-gray-900">Đơn hàng đã được tạo</p>
                  <p className="text-sm text-gray-600">{formatDate(order.createdAt)}</p>
                </div>
              </div>
              
              {order.confirmedAt && (
                <div className="flex items-start">
                  <div className="w-2 h-2 bg-blue-600 rounded-full mt-2 mr-4"></div>
                  <div>
                    <p className="font-medium text-gray-900">Đơn hàng đã được xác nhận</p>
                    <p className="text-sm text-gray-600">{formatDate(order.confirmedAt)}</p>
                  </div>
                </div>
              )}
              
              {order.shippedAt && (
                <div className="flex items-start">
                  <div className="w-2 h-2 bg-purple-600 rounded-full mt-2 mr-4"></div>
                  <div>
                    <p className="font-medium text-gray-900">Đơn hàng đang được giao</p>
                    <p className="text-sm text-gray-600">{formatDate(order.shippedAt)}</p>
                  </div>
                </div>
              )}
              
              {order.deliveredAt && (
                <div className="flex items-start">
                  <div className="w-2 h-2 bg-green-600 rounded-full mt-2 mr-4"></div>
                  <div>
                    <p className="font-medium text-gray-900">Đơn hàng đã được giao</p>
                    <p className="text-sm text-gray-600">{formatDate(order.deliveredAt)}</p>
                  </div>
                </div>
              )}
              
              {order.cancelledAt && (
                <div className="flex items-start">
                  <div className="w-2 h-2 bg-red-600 rounded-full mt-2 mr-4"></div>
                  <div>
                    <p className="font-medium text-gray-900">Đơn hàng đã bị hủy</p>
                    <p className="text-sm text-gray-600">{formatDate(order.cancelledAt)}</p>
                    {order.cancelReason && (
                      <p className="text-sm text-gray-600 mt-1">Lý do: {order.cancelReason}</p>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
