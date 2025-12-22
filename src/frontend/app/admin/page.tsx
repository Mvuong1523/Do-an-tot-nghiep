'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { 
  FiUsers, 
  FiPackage, 
  FiShoppingCart, 
  FiDollarSign,
  FiTrendingUp,
  FiTrendingDown,
  FiArrowRight,
  FiClock
} from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useTranslation } from '@/hooks/useTranslation'
import { useAuthStore } from '@/store/authStore'
import api from '@/lib/api'

interface DashboardStats {
  totalOrders: number
  totalRevenue: number
  totalProfit: number
  profitMargin: number
  totalProducts: number
  totalCustomers: number
  pendingOrders: number
  lowStockProducts: number
  ordersChangePercent: number
  revenueChangePercent: number
  profitChangePercent: number
  productsChangePercent: number
  customersChangePercent: number
}

interface RecentOrder {
  id: number
  orderCode: string
  totalAmount: number
  status: string
  createdAt: string
  customerName: string
  customerEmail: string
}

export default function AdminDashboard() {
  const { t } = useTranslation()
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  
  const [stats, setStats] = useState<DashboardStats>({
    totalOrders: 0,
    totalRevenue: 0,
    totalProfit: 0,
    profitMargin: 0,
    totalProducts: 0,
    totalCustomers: 0,
    pendingOrders: 0,
    lowStockProducts: 0,
    ordersChangePercent: 0,
    revenueChangePercent: 0,
    profitChangePercent: 0,
    productsChangePercent: 0,
    customersChangePercent: 0,
  })
  const [recentOrders, setRecentOrders] = useState<RecentOrder[]>([])
  const [loading, setLoading] = useState(true)
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
  }, [])

  useEffect(() => {
    if (!mounted) return

    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    // Check if user is admin
    if (user?.role !== 'ADMIN') {
      toast.error('Bạn không có quyền truy cập trang này')
      router.push('/')
      return
    }

    loadDashboardData()
  }, [mounted, isAuthenticated, user, router])

  const loadDashboardData = async () => {
    try {
      // Load dashboard stats
      const statsResponse = await api.get('/api/dashboard/stats')
      setStats(statsResponse.data)

      // Load recent orders
      const ordersResponse = await api.get('/api/dashboard/recent-orders?limit=5')
      setRecentOrders(ordersResponse.data)
    } catch (error) {
      console.error('Error loading dashboard data:', error)
      toast.error('Lỗi khi tải dữ liệu')
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
    return new Date(dateString).toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      CONFIRMED: 'bg-blue-100 text-blue-800',
      SHIPPING: 'bg-purple-100 text-purple-800',
      DELIVERED: 'bg-green-100 text-green-800',
      CANCELLED: 'bg-red-100 text-red-800',
    }
    return colors[status] || 'bg-gray-100 text-gray-800'
  }

  const getStatusText = (status: string) => {
    const texts: Record<string, string> = {
      PENDING: 'Chờ xác nhận',
      CONFIRMED: 'Đã xác nhận',
      SHIPPING: 'Đang giao',
      DELIVERED: 'Đã giao',
      CANCELLED: 'Đã hủy',
    }
    return texts[status] || status
  }

  if (!mounted || loading) {
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
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Trang quản trị</h1>
          <p className="text-gray-600">Chào mừng trở lại, {user?.fullName || user?.email}</p>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {/* Total Orders */}
          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <FiShoppingCart className="text-blue-500" size={24} />
              </div>
              {stats.ordersChangePercent !== 0 && (
                <span className={`flex items-center text-sm font-medium ${
                  stats.ordersChangePercent > 0 ? 'text-green-500' : 'text-red-500'
                }`}>
                  {stats.ordersChangePercent > 0 ? <FiTrendingUp className="mr-1" /> : <FiTrendingDown className="mr-1" />}
                  {Math.abs(stats.ordersChangePercent)}%
                </span>
              )}
            </div>
            <h3 className="text-2xl font-bold text-gray-900 mb-1">{stats.totalOrders}</h3>
            <p className="text-gray-600 text-sm">Tổng đơn hàng</p>
          </div>

          {/* Total Revenue */}
          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                <FiDollarSign className="text-green-500" size={24} />
              </div>
              {stats.revenueChangePercent !== 0 && (
                <span className={`flex items-center text-sm font-medium ${
                  stats.revenueChangePercent > 0 ? 'text-green-500' : 'text-red-500'
                }`}>
                  {stats.revenueChangePercent > 0 ? <FiTrendingUp className="mr-1" /> : <FiTrendingDown className="mr-1" />}
                  {Math.abs(stats.revenueChangePercent)}%
                </span>
              )}
            </div>
            <h3 className="text-2xl font-bold text-gray-900 mb-1">
              {formatPrice(stats.totalRevenue)}
            </h3>
            <p className="text-gray-600 text-sm">Doanh thu</p>
          </div>

          {/* Total Profit */}
          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-emerald-100 rounded-lg flex items-center justify-center">
                <FiTrendingUp className="text-emerald-500" size={24} />
              </div>
              {stats.profitChangePercent !== 0 && (
                <span className={`flex items-center text-sm font-medium ${
                  stats.profitChangePercent > 0 ? 'text-green-500' : 'text-red-500'
                }`}>
                  {stats.profitChangePercent > 0 ? <FiTrendingUp className="mr-1" /> : <FiTrendingDown className="mr-1" />}
                  {Math.abs(stats.profitChangePercent)}%
                </span>
              )}
            </div>
            <h3 className="text-2xl font-bold text-gray-900 mb-1">
              {formatPrice(stats.totalProfit)}
            </h3>
            <p className="text-gray-600 text-sm">
              Lợi nhuận 
              <span className="ml-2 text-emerald-600 font-semibold">
                ({stats.profitMargin}%)
              </span>
            </p>
          </div>

          {/* Total Products */}
          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                <FiPackage className="text-purple-500" size={24} />
              </div>
              {stats.productsChangePercent !== 0 && (
                <span className={`flex items-center text-sm font-medium ${
                  stats.productsChangePercent > 0 ? 'text-green-500' : 'text-red-500'
                }`}>
                  {stats.productsChangePercent > 0 ? <FiTrendingUp className="mr-1" /> : <FiTrendingDown className="mr-1" />}
                  {Math.abs(stats.productsChangePercent)}%
                </span>
              )}
            </div>
            <h3 className="text-2xl font-bold text-gray-900 mb-1">{stats.totalProducts}</h3>
            <p className="text-gray-600 text-sm">Sản phẩm</p>
          </div>

          {/* Total Customers */}
          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
                <FiUsers className="text-red-500" size={24} />
              </div>
              {stats.customersChangePercent !== 0 && (
                <span className={`flex items-center text-sm font-medium ${
                  stats.customersChangePercent > 0 ? 'text-green-500' : 'text-red-500'
                }`}>
                  {stats.customersChangePercent > 0 ? <FiTrendingUp className="mr-1" /> : <FiTrendingDown className="mr-1" />}
                  {Math.abs(stats.customersChangePercent)}%
                </span>
              )}
            </div>
            <h3 className="text-2xl font-bold text-gray-900 mb-1">{stats.totalCustomers}</h3>
            <p className="text-gray-600 text-sm">Khách hàng</p>
          </div>
        </div>

        {/* Recent Orders - Moved up */}
        <div className="bg-white rounded-lg shadow-sm mb-8">
          <div className="p-6 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <h2 className="text-xl font-bold text-gray-900">Đơn hàng gần đây</h2>
              <Link href="/admin/orders" className="text-red-500 hover:text-red-600 font-medium flex items-center">
                Xem tất cả
                <FiArrowRight className="ml-1" />
              </Link>
            </div>
          </div>
          <div className="overflow-x-auto">
            {recentOrders.length > 0 ? (
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Mã đơn
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Khách hàng
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Tổng tiền
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Trạng thái
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Thời gian
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {recentOrders.map((order) => (
                    <tr key={order.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <Link 
                          href={`/admin/orders/${order.id}`}
                          className="text-blue-600 hover:text-blue-800 font-medium"
                        >
                          {order.orderCode}
                        </Link>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">{order.customerName}</div>
                        <div className="text-sm text-gray-500">{order.customerEmail}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {formatPrice(order.totalAmount)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(order.status)}`}>
                          {getStatusText(order.status)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        <div className="flex items-center">
                          <FiClock className="mr-1" />
                          {formatDate(order.createdAt)}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="p-6">
                <p className="text-gray-600 text-center py-8">
                  Chưa có đơn hàng nào
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Quick Actions - ADMIN có tất cả quyền */}
        <div className="mb-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Quản trị hệ thống</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <Link
              href="/admin/employee-approval"
              className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">Duyệt nhân viên</h3>
                <FiArrowRight className="text-gray-400" />
              </div>
              <p className="text-gray-600 text-sm mb-2">
                Quản lý yêu cầu đăng ký nhân viên mới
              </p>
              <div className="text-red-500 font-semibold">
                {stats.pendingOrders} yêu cầu đang chờ
              </div>
            </Link>

            <Link
              href="/admin/customers"
              className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">Quản lý người dùng</h3>
                <FiArrowRight className="text-gray-400" />
              </div>
              <p className="text-gray-600 text-sm mb-2">
                Xem thông tin khách hàng và nhân viên
              </p>
              <div className="text-purple-500 font-semibold">
                {stats.totalCustomers} người dùng
              </div>
            </Link>

            <Link
              href="/admin/accounting"
              className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">Báo cáo tổng hợp</h3>
                <FiArrowRight className="text-gray-400" />
              </div>
              <p className="text-gray-600 text-sm mb-2">
                Xem báo cáo doanh thu và thống kê
              </p>
              <div className="text-indigo-500 font-semibold">
                Xem báo cáo
              </div>
            </Link>
          </div>
        </div>

        <div className="mb-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Quản lý kho hàng</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <Link
              href="/admin/inventory"
              className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">Quản lý kho</h3>
                <FiArrowRight className="text-gray-400" />
              </div>
              <p className="text-gray-600 text-sm mb-2">
                Quản lý nhập xuất và tồn kho
              </p>
              <div className="text-yellow-500 font-semibold">
                {stats.lowStockProducts} sản phẩm sắp hết
              </div>
            </Link>

            <Link
              href="/admin/suppliers"
              className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">Nhà cung cấp</h3>
                <FiArrowRight className="text-gray-400" />
              </div>
              <p className="text-gray-600 text-sm mb-2">
                Quản lý nhà cung cấp
              </p>
              <div className="text-orange-500 font-semibold">
                Quản lý NCC
              </div>
            </Link>
          </div>
        </div>

        <div className="mb-8">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Quản lý sản phẩm</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <Link
              href="/admin/products/publish"
              className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">Đăng bán sản phẩm</h3>
                <FiArrowRight className="text-gray-400" />
              </div>
              <p className="text-gray-600 text-sm mb-2">
                Đăng sản phẩm từ kho lên trang bán
              </p>
              <div className="text-blue-500 font-semibold">
                Quản lý đăng bán
              </div>
            </Link>

            <Link
              href="/admin/products"
              className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">Quản lý sản phẩm</h3>
                <FiArrowRight className="text-gray-400" />
              </div>
              <p className="text-gray-600 text-sm mb-2">
                Xem danh sách sản phẩm đang bán
              </p>
              <div className="text-blue-500 font-semibold">
                {stats.totalProducts} sản phẩm
              </div>
            </Link>

            <Link
              href="/admin/categories"
              className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">Quản lý danh mục</h3>
                <FiArrowRight className="text-gray-400" />
              </div>
              <p className="text-gray-600 text-sm mb-2">
                Tạo và chỉnh sửa danh mục sản phẩm
              </p>
              <div className="text-purple-500 font-semibold">
                Quản lý danh mục
              </div>
            </Link>

            <Link
              href="/admin/orders"
              className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">Quản lý đơn hàng</h3>
                <FiArrowRight className="text-gray-400" />
              </div>
              <p className="text-gray-600 text-sm mb-2">
                Xem và xử lý đơn hàng
              </p>
              <div className="text-green-500 font-semibold">
                {stats.totalOrders} đơn hàng
              </div>
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
