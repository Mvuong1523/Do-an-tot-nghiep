'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiPackage, FiDownload, FiUpload, FiTrendingUp, FiAlertCircle, FiArrowRight } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import { inventoryApi } from '@/lib/api'

export default function WarehouseDashboard() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState({
    totalStock: 0,
    lowStock: 0,
    pendingImports: 0,
    pendingExports: 0
  })

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    if (user?.role !== 'WAREHOUSE' && user?.role !== 'ADMIN') {
      toast.error('Chỉ nhân viên kho mới có quyền truy cập')
      router.push('/')
      return
    }

    loadStats()
  }, [isAuthenticated, user, router])

  const loadStats = async () => {
    try {
      const [stockRes, poRes, eoRes] = await Promise.all([
        inventoryApi.getStocks(),
        inventoryApi.getPurchaseOrders('CREATED'),
        inventoryApi.getExportOrders('CREATED')
      ])

      const lowStockCount = stockRes.data?.filter((s: any) => s.sellable < 10).length || 0

      setStats({
        totalStock: stockRes.data?.length || 0,
        lowStock: lowStockCount,
        pendingImports: poRes.data?.length || 0,
        pendingExports: eoRes.data?.length || 0
      })
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
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="p-6">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Dashboard Kho hàng</h1>
        <p className="text-gray-600">Chào mừng, {user?.fullName || user?.email}</p>
      </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Tổng sản phẩm</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{stats.totalStock}</p>
              </div>
              <div className="bg-blue-100 p-3 rounded-lg">
                <FiPackage className="text-blue-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Sắp hết hàng</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{stats.lowStock}</p>
              </div>
              <div className="bg-yellow-100 p-3 rounded-lg">
                <FiAlertCircle className="text-yellow-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Chờ nhập kho</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{stats.pendingImports}</p>
              </div>
              <div className="bg-green-100 p-3 rounded-lg">
                <FiDownload className="text-green-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Chờ xuất kho</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{stats.pendingExports}</p>
              </div>
              <div className="bg-purple-100 p-3 rounded-lg">
                <FiUpload className="text-purple-600" size={24} />
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <Link
            href="/warehouse/inventory"
            className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Tồn kho</h3>
              <FiArrowRight className="text-gray-400" />
            </div>
            <p className="text-gray-600 text-sm mb-2">Xem và quản lý tồn kho</p>
            <div className="text-blue-500 font-semibold">{stats.totalStock} sản phẩm</div>
          </Link>

          <Link
            href="/warehouse/import"
            className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Nhập kho</h3>
              <FiArrowRight className="text-gray-400" />
            </div>
            <p className="text-gray-600 text-sm mb-2">Tạo và xử lý phiếu nhập</p>
            <div className="text-green-500 font-semibold">{stats.pendingImports} chờ xử lý</div>
          </Link>

          <Link
            href="/warehouse/export"
            className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Xuất kho</h3>
              <FiArrowRight className="text-gray-400" />
            </div>
            <p className="text-gray-600 text-sm mb-2">Tạo và xử lý phiếu xuất</p>
            <div className="text-purple-500 font-semibold">{stats.pendingExports} chờ xử lý</div>
          </Link>

          <Link
            href="/warehouse/transactions"
            className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Lịch sử giao dịch</h3>
              <FiArrowRight className="text-gray-400" />
            </div>
            <p className="text-gray-600 text-sm mb-2">Xem phiếu nhập xuất</p>
            <div className="text-indigo-500 font-semibold">Xem lịch sử</div>
          </Link>

          <Link
            href="/warehouse/suppliers"
            className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Nhà cung cấp</h3>
              <FiArrowRight className="text-gray-400" />
            </div>
            <p className="text-gray-600 text-sm mb-2">Quản lý nhà cung cấp</p>
            <div className="text-orange-500 font-semibold">Quản lý NCC</div>
          </Link>

          <Link
            href="/warehouse/reports"
            className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Báo cáo</h3>
              <FiArrowRight className="text-gray-400" />
            </div>
            <p className="text-gray-600 text-sm mb-2">Báo cáo nhập xuất tồn</p>
            <div className="text-red-500 font-semibold">Xem báo cáo</div>
          </Link>
        </div>
    </div>
  )
}
