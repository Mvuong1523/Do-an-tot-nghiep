'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { useAuthStore } from '@/store/authStore'
import { FiPackage, FiTruck, FiShoppingCart, FiDollarSign } from 'react-icons/fi'
import toast from 'react-hot-toast'

export default function SalesPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState({
    totalOrders: 0,
    pendingExport: 0,
    shipped: 0,
    totalRevenue: 0
  })

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    if (user?.role !== 'EMPLOYEE' || user?.position !== 'SALES') {
      toast.error('Bạn không có quyền truy cập trang này')
      router.push('/')
      return
    }

    loadStats()
  }, [isAuthenticated, user, router])

  const loadStats = async () => {
    try {
      setLoading(true)
      const token = localStorage.getItem('token')
      
      // Load statistics
      const response = await fetch('http://localhost:8080/api/orders/stats', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })

      const result = await response.json()
      if (result.success && result.data) {
        setStats(result.data)
      }
    } catch (error) {
      console.error('Error loading stats:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Dashboard Bán hàng</h1>
          <p className="mt-2 text-gray-600">Tổng quan hoạt động bán hàng</p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Tổng đơn hàng</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{stats.totalOrders}</p>
              </div>
              <div className="bg-blue-100 p-3 rounded-full">
                <FiShoppingCart className="text-blue-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Chờ xuất kho</p>
                <p className="text-3xl font-bold text-orange-600 mt-2">{stats.pendingExport}</p>
              </div>
              <div className="bg-orange-100 p-3 rounded-full">
                <FiPackage className="text-orange-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Đang giao hàng</p>
                <p className="text-3xl font-bold text-purple-600 mt-2">{stats.shipped}</p>
              </div>
              <div className="bg-purple-100 p-3 rounded-full">
                <FiTruck className="text-purple-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Doanh thu</p>
                <p className="text-xl font-bold text-green-600 mt-2">
                  {new Intl.NumberFormat('vi-VN', {
                    style: 'currency',
                    currency: 'VND'
                  }).format(stats.totalRevenue)}
                </p>
              </div>
              <div className="bg-green-100 p-3 rounded-full">
                <FiDollarSign className="text-green-600" size={24} />
              </div>
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Link
            href="/sales/orders"
            className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow"
          >
            <div className="flex items-center space-x-4">
              <div className="bg-blue-100 p-4 rounded-full">
                <FiShoppingCart className="text-blue-600" size={32} />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-gray-900">Quản lý đơn hàng</h3>
                <p className="text-sm text-gray-600 mt-1">Xem và xử lý đơn hàng</p>
              </div>
            </div>
          </Link>

          <Link
            href="/sales/export"
            className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow"
          >
            <div className="flex items-center space-x-4">
              <div className="bg-orange-100 p-4 rounded-full">
                <FiTruck className="text-orange-600" size={32} />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-gray-900">Xuất kho bán hàng</h3>
                <p className="text-sm text-gray-600 mt-1">Xử lý xuất kho cho đơn hàng</p>
                {stats.pendingExport > 0 && (
                  <span className="inline-block mt-2 px-3 py-1 bg-orange-100 text-orange-800 rounded-full text-xs font-semibold">
                    {stats.pendingExport} đơn chờ xử lý
                  </span>
                )}
              </div>
            </div>
          </Link>
        </div>
      </div>
    </div>
  )
}
