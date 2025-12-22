'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { FiPackage, FiSearch, FiFileText } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'

export default function EmployeeInventoryPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  const [products, setProducts] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    if (user?.role !== 'EMPLOYEE' && user?.role !== 'ADMIN') {
      toast.error('Bạn không có quyền truy cập')
      router.push('/')
      return
    }

    loadInventory()
  }, [isAuthenticated, user, router])

  const loadInventory = async () => {
    try {
      const token = localStorage.getItem('token') || localStorage.getItem('auth_token')
      const response = await fetch('http://localhost:8080/api/warehouse/products', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })

      const data = await response.json()
      if (data.success) {
        setProducts(data.data || [])
      }
    } catch (error) {
      console.error('Error loading inventory:', error)
      toast.error('Lỗi khi tải tồn kho')
    } finally {
      setLoading(false)
    }
  }

  const filteredProducts = products.filter(product =>
    product.internalName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    product.sku?.toLowerCase().includes(searchQuery.toLowerCase())
  )

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Tồn kho sản phẩm</h1>

        <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-start">
            <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
            <div>
              <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
              <p className="text-sm text-blue-700 mt-1">
                Bạn có thể xem thông tin tồn kho sản phẩm.
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
          <div className="relative">
            <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Tìm kiếm sản phẩm..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        {filteredProducts.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <FiPackage size={64} className="mx-auto text-gray-400 mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Chưa có sản phẩm nào</h3>
            <p className="text-gray-600">Không có sản phẩm nào trong kho</p>
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">SKU</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tên sản phẩm</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nhà cung cấp</th>
                    <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Tồn kho</th>
                    <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Có thể bán</th>
                    <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Đang giữ</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredProducts.map((product) => (
                    <tr key={product.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-xs font-mono font-semibold text-gray-900">{product.sku}</span>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-gray-900">{product.internalName}</div>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        {product.supplier?.name || 'N/A'}
                      </td>
                      <td className="px-6 py-4 text-center">
                        <span className="text-sm font-bold text-gray-900">
                          {product.stockQuantity || 0}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-center">
                        <span className="text-sm font-bold text-green-600">
                          {product.sellableQuantity || 0}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-center">
                        <span className="text-sm font-bold text-orange-600">
                          {product.reservedQuantity || 0}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
