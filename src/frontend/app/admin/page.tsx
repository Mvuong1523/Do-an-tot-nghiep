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
  FiArrowRight
} from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useTranslation } from '@/hooks/useTranslation'
import { useAuthStore } from '@/store/authStore'

export default function AdminDashboard() {
  const { t } = useTranslation()
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  
  const [stats, setStats] = useState({
    totalOrders: 0,
    totalRevenue: 0,
    totalProducts: 0,
    totalCustomers: 0,
    pendingOrders: 0,
    lowStockProducts: 0,
  })
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
      // TODO: Call dashboard API
      setStats({
        totalOrders: 1250,
        totalRevenue: 450000000,
        totalProducts: 350,
        totalCustomers: 5420,
        pendingOrders: 23,
        lowStockProducts: 12,
      })
    } catch (error) {
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
              <span className="flex items-center text-green-500 text-sm font-medium">
                <FiTrendingUp className="mr-1" />
                +12%
              </span>
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
              <span className="flex items-center text-green-500 text-sm font-medium">
                <FiTrendingUp className="mr-1" />
                +8%
              </span>
            </div>
            <h3 className="text-2xl font-bold text-gray-900 mb-1">
              {formatPrice(stats.totalRevenue)}
            </h3>
            <p className="text-gray-600 text-sm">Doanh thu</p>
          </div>

          {/* Total Products */}
          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                <FiPackage className="text-purple-500" size={24} />
              </div>
              <span className="flex items-center text-red-500 text-sm font-medium">
                <FiTrendingDown className="mr-1" />
                -3%
              </span>
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
              <span className="flex items-center text-green-500 text-sm font-medium">
                <FiTrendingUp className="mr-1" />
                +15%
              </span>
            </div>
            <h3 className="text-2xl font-bold text-gray-900 mb-1">{stats.totalCustomers}</h3>
            <p className="text-gray-600 text-sm">Khách hàng</p>
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
              href="/admin/reports"
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

        {/* Recent Orders */}
        <div className="bg-white rounded-lg shadow-sm">
          <div className="p-6 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <h2 className="text-xl font-bold text-gray-900">Đơn hàng gần đây</h2>
              <Link href="/admin/orders" className="text-red-500 hover:text-red-600 font-medium">
                Xem tất cả
              </Link>
            </div>
          </div>
          <div className="p-6">
            <p className="text-gray-600 text-center py-8">
              Chưa có đơn hàng nào
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
