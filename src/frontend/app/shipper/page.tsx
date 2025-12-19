'use client'

import { useEffect, useState } from 'react'
import { useAuthStore } from '@/store/authStore'
import { useRouter } from 'next/navigation'
import toast from 'react-hot-toast'
import { FiPackage, FiTruck, FiCheckCircle, FiClock } from 'react-icons/fi'

export default function ShipperPage() {
  const { user, isAuthenticated } = useAuthStore()
  const router = useRouter()
  const [orders, setOrders] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!isAuthenticated || user?.position !== 'SHIPPER') {
      router.push('/login')
      return
    }

    fetchOrders()
  }, [isAuthenticated, user, router])

  const fetchOrders = async () => {
    try {
      const token = localStorage.getItem('auth_token')
      const response = await fetch('http://localhost:8080/api/admin/orders', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })

      if (response.ok) {
        const data = await response.json()
        // Filter orders that are ready for shipping or being shipped
        const shippingOrders = data.data?.filter((order: any) => 
          ['READY_TO_SHIP', 'SHIPPING', 'DELIVERED'].includes(order.status)
        ) || []
        setOrders(shippingOrders)
      }
    } catch (error) {
      console.error('Error fetching orders:', error)
      toast.error('Không thể tải danh sách đơn hàng')
    } finally {
      setLoading(false)
    }
  }

  const updateOrderStatus = async (orderId: number, newStatus: string) => {
    try {
      const token = localStorage.getItem('auth_token')
      const response = await fetch(`http://localhost:8080/api/admin/orders/${orderId}/status`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: newStatus })
      })

      if (response.ok) {
        toast.success('Cập nhật trạng thái thành công')
        fetchOrders()
      } else {
        toast.error('Không thể cập nhật trạng thái')
      }
    } catch (error) {
      console.error('Error updating status:', error)
      toast.error('Lỗi khi cập nhật trạng thái')
    }
  }

  const getStatusBadge = (status: string) => {
    const statusMap: any = {
      'READY_TO_SHIP': { label: 'Chờ lấy hàng', color: 'bg-blue-100 text-blue-800', icon: FiPackage },
      'SHIPPING': { label: 'Đang giao', color: 'bg-yellow-100 text-yellow-800', icon: FiTruck },
      'DELIVERED': { label: 'Đã giao', color: 'bg-green-100 text-green-800', icon: FiCheckCircle }
    }
    const config = statusMap[status] || { label: status, color: 'bg-gray-100 text-gray-800', icon: FiPackage }
    const Icon = config.icon
    return (
      <span className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium ${config.color}`}>
        <Icon className="w-4 h-4" />
        {config.label}
      </span>
    )
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3">
            <FiTruck className="text-red-500" />
            Quản lý giao hàng
          </h1>
          <p className="text-gray-600 mt-2">Xin chào, {user?.fullName || user?.email}</p>
        </div>

        {/* Statistics */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-600 text-sm">Chờ lấy hàng</p>
                <p className="text-2xl font-bold text-blue-600">
                  {orders.filter(o => o.status === 'READY_TO_SHIP').length}
                </p>
              </div>
              <FiPackage className="w-10 h-10 text-blue-500" />
            </div>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-600 text-sm">Đang giao</p>
                <p className="text-2xl font-bold text-yellow-600">
                  {orders.filter(o => o.status === 'SHIPPING').length}
                </p>
              </div>
              <FiTruck className="w-10 h-10 text-yellow-500" />
            </div>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-600 text-sm">Đã giao</p>
                <p className="text-2xl font-bold text-green-600">
                  {orders.filter(o => o.status === 'DELIVERED').length}
                </p>
              </div>
              <FiCheckCircle className="w-10 h-10 text-green-500" />
            </div>
          </div>
        </div>

        {/* Orders List */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-xl font-semibold text-gray-900">Danh sách đơn hàng</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Mã đơn</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Khách hàng</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Địa chỉ</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tổng tiền</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Hành động</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {orders.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-6 py-8 text-center text-gray-500">
                      Không có đơn hàng nào
                    </td>
                  </tr>
                ) : (
                  orders.map((order) => (
                    <tr key={order.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm font-medium text-gray-900">#{order.id}</td>
                      <td className="px-6 py-4 text-sm text-gray-900">
                        <div>{order.receiverName}</div>
                        <div className="text-gray-500">{order.receiverPhone}</div>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-600 max-w-xs truncate">
                        {order.shippingAddress}
                      </td>
                      <td className="px-6 py-4 text-sm font-medium text-gray-900">
                        {order.totalAmount?.toLocaleString('vi-VN')}đ
                      </td>
                      <td className="px-6 py-4">{getStatusBadge(order.status)}</td>
                      <td className="px-6 py-4">
                        <div className="flex gap-2">
                          {order.status === 'READY_TO_SHIP' && (
                            <button
                              onClick={() => updateOrderStatus(order.id, 'SHIPPING')}
                              className="px-3 py-1 bg-yellow-500 text-white text-sm rounded hover:bg-yellow-600"
                            >
                              Nhận đơn & Bắt đầu giao
                            </button>
                          )}
                          {order.status === 'SHIPPING' && (
                            <button
                              onClick={() => updateOrderStatus(order.id, 'DELIVERED')}
                              className="px-3 py-1 bg-green-500 text-white text-sm rounded hover:bg-green-600"
                            >
                              Đã giao
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  )
}
