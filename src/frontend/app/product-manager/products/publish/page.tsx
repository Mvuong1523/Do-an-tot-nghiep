'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiPackage, FiSearch, FiPlus } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import { productApi, inventoryApi, categoryApi } from '@/lib/api'

export default function PublishProductPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  const [warehouseProducts, setWarehouseProducts] = useState<any[]>([])
  const [categories, setCategories] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedProduct, setSelectedProduct] = useState<any>(null)
  const [showPublishModal, setShowPublishModal] = useState(false)
  const [publishForm, setPublishForm] = useState({
    warehouseProductId: 0,
    displayName: '',
    description: '',
    price: 0,
    categoryId: 0,
    imageUrl: '',
    active: true
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

    loadData()
  }, [isAuthenticated, user, router])

  const loadData = async () => {
    try {
      // Dùng API warehouse products có sẵn
      const [productsRes, categoriesRes] = await Promise.all([
        productApi.getWarehouseProductsForPublish(),
        categoryApi.getAll()
      ])

      console.log('Warehouse Products Response:', productsRes)
      console.log('Categories Response:', categoriesRes)

      setWarehouseProducts(productsRes.data || [])
      setCategories(categoriesRes.data || [])
      
      if (!productsRes.data || productsRes.data.length === 0) {
        console.warn('No warehouse products found')
      }
    } catch (error) {
      console.error('Error loading data:', error)
      toast.error('Lỗi khi tải dữ liệu')
    } finally {
      setLoading(false)
    }
  }

  const handlePublish = (product: any) => {
    setSelectedProduct(product)
    setPublishForm({
      warehouseProductId: product.id,
      displayName: product.internalName || '',
      description: product.description || '',
      price: 0,
      categoryId: 0,
      imageUrl: '',
      active: true
    })
    setShowPublishModal(true)
  }

  const handleSubmitPublish = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!publishForm.displayName || !publishForm.price || !publishForm.categoryId) {
      toast.error('Vui lòng điền đầy đủ thông tin')
      return
    }

    try {
      const response = await productApi.createProductFromWarehouse(publishForm)
      if (response.success) {
        toast.success('Đăng bán sản phẩm thành công!')
        setShowPublishModal(false)
        loadData()
      } else {
        toast.error(response.message || 'Có lỗi xảy ra')
      }
    } catch (error: any) {
      console.error('Error publishing product:', error)
      toast.error(error.response?.data?.message || 'Lỗi khi đăng bán sản phẩm')
    }
  }

  const filteredProducts = warehouseProducts.filter(product =>
    product.internalName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    product.sku?.toLowerCase().includes(searchQuery.toLowerCase())
  )

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

        <h1 className="text-3xl font-bold text-gray-900 mb-8">Đăng bán sản phẩm</h1>

        <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
          <div className="relative">
            <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Tìm kiếm sản phẩm trong kho..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
            />
          </div>
        </div>

        {filteredProducts.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <FiPackage size={64} className="mx-auto text-gray-400 mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Chưa có sản phẩm nào</h3>
            <p className="text-gray-600">Không có sản phẩm nào trong kho để đăng bán</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredProducts.map((product) => (
              <div key={product.id} className="bg-white rounded-lg shadow-sm overflow-hidden hover:shadow-md transition-shadow border border-gray-200">
                <div className="h-48 bg-gradient-to-br from-gray-50 to-gray-100 flex items-center justify-center border-b border-gray-200">
                  <FiPackage size={64} className="text-gray-300" />
                </div>
                <div className="p-5">
                  <div className="mb-3">
                    <h3 className="text-lg font-bold text-gray-900 mb-1">{product.internalName}</h3>
                    <p className="text-xs text-gray-500">SKU: <span className="font-mono font-semibold text-gray-700">{product.sku}</span></p>
                  </div>
                  
                  <div className="space-y-2 mb-4">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-gray-600">Nhà cung cấp:</span>
                      <span className="font-medium text-gray-900">{product.supplier?.name || 'N/A'}</span>
                    </div>
                    
                    {product.stockQuantity !== undefined && (
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-gray-600">Tồn kho:</span>
                        <span className="font-bold text-green-600">
                          {product.sellableQuantity || 0} / {product.stockQuantity || 0}
                        </span>
                      </div>
                    )}
                  </div>

                  {product.description && (
                    <p className="text-sm text-gray-600 mb-4 line-clamp-2 min-h-[40px]">{product.description}</p>
                  )}
                  
                  {product.isPublished ? (
                    <div className="w-full bg-gray-100 text-gray-600 px-4 py-2.5 rounded-lg text-center font-medium">
                      ✓ Đã đăng bán
                    </div>
                  ) : (
                    <button
                      onClick={() => handlePublish(product)}
                      className="w-full bg-red-500 text-white px-4 py-2.5 rounded-lg hover:bg-red-600 transition-colors flex items-center justify-center space-x-2 font-semibold shadow-sm hover:shadow"
                    >
                      <FiPlus size={18} />
                      <span>Đăng bán</span>
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Publish Modal */}
        {showPublishModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
              <div className="p-6">
                <h2 className="text-2xl font-bold text-gray-900 mb-6">Đăng bán sản phẩm</h2>
                
                <form onSubmit={handleSubmitPublish}>
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Tên hiển thị <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="text"
                        value={publishForm.displayName}
                        onChange={(e) => setPublishForm({...publishForm, displayName: e.target.value})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Mô tả
                      </label>
                      <textarea
                        value={publishForm.description}
                        onChange={(e) => setPublishForm({...publishForm, description: e.target.value})}
                        rows={4}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Giá bán <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="number"
                        value={publishForm.price}
                        onChange={(e) => setPublishForm({...publishForm, price: Number(e.target.value)})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                        required
                        min="0"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Danh mục <span className="text-red-500">*</span>
                      </label>
                      <select
                        value={publishForm.categoryId}
                        onChange={(e) => setPublishForm({...publishForm, categoryId: Number(e.target.value)})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                        required
                      >
                        <option value="">Chọn danh mục</option>
                        {categories.map((cat) => (
                          <option key={cat.id} value={cat.id}>{cat.name}</option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        URL hình ảnh
                      </label>
                      <input
                        type="text"
                        value={publishForm.imageUrl}
                        onChange={(e) => setPublishForm({...publishForm, imageUrl: e.target.value})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                        placeholder="https://example.com/image.jpg"
                      />
                    </div>

                    <div className="flex items-center">
                      <input
                        type="checkbox"
                        id="active"
                        checked={publishForm.active}
                        onChange={(e) => setPublishForm({...publishForm, active: e.target.checked})}
                        className="w-4 h-4 text-red-500 border-gray-300 rounded focus:ring-red-500"
                      />
                      <label htmlFor="active" className="ml-2 text-sm text-gray-700">
                        Kích hoạt ngay
                      </label>
                    </div>
                  </div>

                  <div className="flex space-x-4 mt-6">
                    <button
                      type="button"
                      onClick={() => setShowPublishModal(false)}
                      className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                      Hủy
                    </button>
                    <button
                      type="submit"
                      className="flex-1 bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition-colors"
                    >
                      Đăng bán
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}
    </div>
  )
}
