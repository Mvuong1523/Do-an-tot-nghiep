'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiPackage, FiEye, FiEdit, FiTrash2, FiPlus, FiCheck, FiX } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import { productApi, categoryApi } from '@/lib/api'

export default function PublishProductsPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  
  const [warehouseProducts, setWarehouseProducts] = useState<any[]>([])
  const [categories, setCategories] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [showPublishModal, setShowPublishModal] = useState(false)
  const [selectedProduct, setSelectedProduct] = useState<any>(null)
  const [formData, setFormData] = useState({
    name: '',
    categoryId: '',
    price: '',
    description: '',
    imageUrl: ''
  })

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    if (user?.role !== 'ADMIN' && user?.role !== 'PRODUCT_MANAGER') {
      toast.error('Chỉ quản lý và nhân viên mới có quyền truy cập')
      router.push('/')
      return
    }

    loadData()
  }, [isAuthenticated, user, router])

  const loadData = async () => {
    try {
      setLoading(true)
      const [productsRes, categoriesRes] = await Promise.all([
        productApi.getWarehouseProductsForPublish(),
        categoryApi.getAll()
      ])
      
      console.log('Products Response:', productsRes)
      console.log('Categories Response:', categoriesRes)
      
      if (productsRes.success && Array.isArray(productsRes.data)) {
        setWarehouseProducts(productsRes.data)
      } else {
        setWarehouseProducts([])
      }
      
      if (categoriesRes.success && Array.isArray(categoriesRes.data)) {
        setCategories(categoriesRes.data)
      } else if (categoriesRes.data && Array.isArray(categoriesRes.data)) {
        // Trường hợp API trả về data trực tiếp
        setCategories(categoriesRes.data)
      } else {
        console.error('Categories data is not an array:', categoriesRes)
        setCategories([])
      }
    } catch (error) {
      console.error('Error loading data:', error)
      toast.error('Lỗi khi tải dữ liệu')
      setWarehouseProducts([])
      setCategories([])
    } finally {
      setLoading(false)
    }
  }

  const handlePublish = (product: any) => {
    setSelectedProduct(product)
    setFormData({
      name: product.internalName || '',
      categoryId: '',
      price: '',
      description: product.description || '',
      imageUrl: ''
    })
    setShowPublishModal(true)
  }

  const handleSubmitPublish = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!selectedProduct) return
    
    try {
      const data = {
        warehouseProductId: selectedProduct.id,
        name: formData.name,
        categoryId: parseInt(formData.categoryId),
        price: parseFloat(formData.price),
        description: formData.description,
        imageUrl: formData.imageUrl
      }
      
      const response = await productApi.createProductFromWarehouse(data)
      
      if (response.success) {
        toast.success('Đăng bán sản phẩm thành công!')
        setShowPublishModal(false)
        loadData()
      } else {
        toast.error(response.message || 'Có lỗi xảy ra')
      }
    } catch (error: any) {
      toast.error(error.message || 'Lỗi khi đăng bán sản phẩm')
    }
  }

  const handleUnpublish = async (productId: number) => {
    if (!confirm('Bạn có chắc muốn gỡ sản phẩm này khỏi trang bán?')) return
    
    try {
      const response = await productApi.unpublishProduct(productId)
      
      if (response.success) {
        toast.success('Gỡ sản phẩm thành công!')
        loadData()
      } else {
        toast.error(response.message || 'Có lỗi xảy ra')
      }
    } catch (error: any) {
      toast.error(error.message || 'Lỗi khi gỡ sản phẩm')
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
        {/* Breadcrumb */}
        <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
          <Link href="/" className="hover:text-red-500">Trang chủ</Link>
          <span>/</span>
          <Link href="/admin" className="hover:text-red-500">Quản trị</Link>
          <span>/</span>
          <span className="text-gray-900">Đăng bán sản phẩm</span>
        </nav>

        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Đăng bán sản phẩm từ kho</h1>
        </div>

        {/* Products Table */}
        <div className="bg-white rounded-lg shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">SKU</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tên sản phẩm</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nhà cung cấp</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tồn kho</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Có thể bán</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Thao tác</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {warehouseProducts.map((product) => (
                  <tr key={product.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {product.sku}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      <div className="font-medium">{product.internalName}</div>
                      {product.description && (
                        <div className="text-xs text-gray-500 truncate max-w-xs">{product.description}</div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {product.supplierName || 'N/A'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {product.stockQuantity || 0}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {product.sellableQuantity || 0}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {product.isPublished ? (
                        <span className="px-2 py-1 text-xs font-semibold rounded bg-green-100 text-green-800 flex items-center space-x-1 w-fit">
                          <FiCheck />
                          <span>Đã đăng bán</span>
                        </span>
                      ) : (
                        <span className="px-2 py-1 text-xs font-semibold rounded bg-gray-100 text-gray-800 flex items-center space-x-1 w-fit">
                          <FiX />
                          <span>Chưa đăng bán</span>
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      {product.isPublished ? (
                        <div className="flex items-center justify-end space-x-2">
                          <Link
                            href={`/admin/products/edit/${product.publishedProductId}`}
                            className="text-blue-500 hover:text-blue-600"
                          >
                            <FiEdit />
                          </Link>
                          <button
                            onClick={() => handleUnpublish(product.publishedProductId)}
                            className="text-red-500 hover:text-red-600"
                          >
                            <FiTrash2 />
                          </button>
                        </div>
                      ) : (
                        <button
                          onClick={() => handlePublish(product)}
                          className="text-green-500 hover:text-green-600 flex items-center space-x-1 ml-auto"
                          disabled={(product.sellableQuantity || 0) === 0}
                        >
                          <FiPlus />
                          <span>Đăng bán</span>
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Publish Modal */}
        {showPublishModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
              <h2 className="text-2xl font-bold mb-4">Đăng bán sản phẩm</h2>
              
              <form onSubmit={handleSubmitPublish}>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      SKU
                    </label>
                    <input
                      type="text"
                      value={selectedProduct?.sku || ''}
                      disabled
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-100"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Tên sản phẩm <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      required
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Danh mục <span className="text-red-500">*</span>
                    </label>
                    <select
                      value={formData.categoryId}
                      onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                      required
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                    >
                      <option value="">Chọn danh mục</option>
                      {Array.isArray(categories) && categories.map((cat) => (
                        <option key={cat.id} value={cat.id}>{cat.name}</option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Giá bán <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="number"
                      value={formData.price}
                      onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                      required
                      min="0"
                      step="1000"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Mô tả
                    </label>
                    <textarea
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                      rows={4}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      URL hình ảnh
                    </label>
                    <input
                      type="text"
                      value={formData.imageUrl}
                      onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                    />
                  </div>
                </div>

                <div className="flex justify-end space-x-3 mt-6">
                  <button
                    type="button"
                    onClick={() => setShowPublishModal(false)}
                    className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                  >
                    Hủy
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600"
                  >
                    Đăng bán
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
