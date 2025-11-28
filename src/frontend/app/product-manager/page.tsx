'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiPackage, FiTag, FiEye, FiTrendingUp } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import { inventoryApi, productApi, categoryApi } from '@/lib/api'

export default function ProductManagerDashboard() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState({
    warehouseProducts: 0,
    publishedProducts: 0,
    categories: 0,
    lowStock: 0
  })

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    if (user?.role !== 'PRODUCT_MANAGER' && user?.role !== 'ADMIN') {
      toast.error('Chỉ quản lý sản phẩm mới có quyền truy cập')
      router.push('/')
      return
    }

    loadStats()
  }, [isAuthenticated, user, router])

  const loadStats = async () => {
    try {
      const [stockRes, productsRes, categoriesRes] = await Promise.all([
        inventoryApi.getStocks(),
        productApi.getAll(),
        categoryApi.getAll()
      ])

      const lowStockCount = stockRes.data?.filter((s: any) => s.sellable < 10).length || 0

      setStats({
        warehouseProducts: stockRes.data?.length || 0,
        publishedProducts: productsRes.data?.length || 0,
        categories: categoriesRes.data?.length || 0,
        lowStock: lowStockCount
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
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Không cần breadcrumb ở dashboard */}

        <h1 className="text-3xl font-bold text-gray-900 mb-8">Dashboard Quản lý sản phẩm</h1>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Sản phẩm trong kho</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{stats.warehouseProducts}</p>
              </div>
              <div className="bg-blue-100 p-3 rounded-lg">
                <FiPackage className="text-blue-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Đã đăng bán</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{stats.publishedProducts}</p>
              </div>
              <div className="bg-green-100 p-3 rounded-lg">
                <FiTrendingUp className="text-green-600" size={24} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Danh mục</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{stats.categories}</p>
              </div>
              <div className="bg-purple-100 p-3 rounded-lg">
                <FiTag className="text-purple-600" size={24} />
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
                <FiEye className="text-yellow-600" size={24} />
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Link href="/product-manager/products/publish" className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
            <div className="flex items-center space-x-4">
              <div className="bg-red-100 p-4 rounded-lg">
                <FiPackage className="text-red-600" size={32} />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-gray-900">Đăng bán sản phẩm</h3>
                <p className="text-gray-600 mt-1">Chọn sản phẩm từ kho để đăng bán</p>
              </div>
            </div>
          </Link>

          <Link href="/product-manager/categories" className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
            <div className="flex items-center space-x-4">
              <div className="bg-purple-100 p-4 rounded-lg">
                <FiTag className="text-purple-600" size={32} />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-gray-900">Quản lý danh mục</h3>
                <p className="text-gray-600 mt-1">Tạo và chỉnh sửa danh mục sản phẩm</p>
              </div>
            </div>
          </Link>

          <Link href="/product-manager/inventory" className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
            <div className="flex items-center space-x-4">
              <div className="bg-blue-100 p-4 rounded-lg">
                <FiEye className="text-blue-600" size={32} />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-gray-900">Xem tồn kho</h3>
                <p className="text-gray-600 mt-1">Theo dõi số lượng sản phẩm trong kho</p>
              </div>
            </div>
          </Link>

          <Link href="/product-manager/products" className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
            <div className="flex items-center space-x-4">
              <div className="bg-green-100 p-4 rounded-lg">
                <FiTrendingUp className="text-green-600" size={32} />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-gray-900">Sản phẩm đã đăng</h3>
                <p className="text-gray-600 mt-1">Quản lý sản phẩm đang bán</p>
              </div>
            </div>
          </Link>
        </div>
      </div>
    </div>
  )
}
