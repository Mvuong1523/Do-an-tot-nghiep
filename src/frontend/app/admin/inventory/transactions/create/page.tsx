'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'
import { FiPlus, FiTrash2, FiSave, FiX } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'

interface TransactionItem {
  productId: number
  productName: string
  quantity: number
  price: number
}

export default function CreateTransactionPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { user, isAuthenticated } = useAuthStore()
  
  const type = (searchParams.get('type') || 'IMPORT') as 'IMPORT' | 'EXPORT'
  
  const [formData, setFormData] = useState({
    note: '',
    supplier: '',
    invoiceNumber: ''
  })
  
  const [items, setItems] = useState<TransactionItem[]>([])
  const [showProductModal, setShowProductModal] = useState(false)
  const [selectedProduct, setSelectedProduct] = useState<any>(null)
  const [quantity, setQuantity] = useState(1)
  const [price, setPrice] = useState(0)
  const [isSubmitting, setIsSubmitting] = useState(false)

  // Mock products
  const products = [
    { id: 1, name: 'iPhone 16 Pro Max 256GB', price: 29990000, stock: 10 },
    { id: 2, name: 'Xiaomi POCO C71 4GB/128GB', price: 2490000, stock: 25 },
    { id: 3, name: 'Xiaomi POCO M6 8GB/256GB', price: 3990000, stock: 15 },
    { id: 4, name: 'Xiaomi 15T 12GB/512GB', price: 14990000, stock: 8 }
  ]

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    // Check if user is admin or employee (tạm thời cho tất cả employee)
    if (user?.role !== 'ADMIN' && user?.role !== 'EMPLOYEE') {
      toast.error('Chỉ quản lý và nhân viên mới có quyền truy cập')
      router.push('/')
      return
    }
  }, [isAuthenticated, user, router])

  const handleAddProduct = () => {
    if (!selectedProduct) {
      toast.error('Vui lòng chọn sản phẩm')
      return
    }

    if (quantity <= 0) {
      toast.error('Số lượng phải lớn hơn 0')
      return
    }

    if (type === 'EXPORT' && quantity > selectedProduct.stock) {
      toast.error('Số lượng xuất vượt quá tồn kho')
      return
    }

    const existingItem = items.find(item => item.productId === selectedProduct.id)
    
    if (existingItem) {
      setItems(items.map(item =>
        item.productId === selectedProduct.id
          ? { ...item, quantity: item.quantity + quantity, price }
          : item
      ))
    } else {
      setItems([...items, {
        productId: selectedProduct.id,
        productName: selectedProduct.name,
        quantity,
        price
      }])
    }

    setShowProductModal(false)
    setSelectedProduct(null)
    setQuantity(1)
    setPrice(0)
    toast.success('Đã thêm sản phẩm')
  }

  const handleRemoveItem = (productId: number) => {
    setItems(items.filter(item => item.productId !== productId))
    toast.success('Đã xóa sản phẩm')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (items.length === 0) {
      toast.error('Vui lòng thêm ít nhất 1 sản phẩm')
      return
    }

    setIsSubmitting(true)

    try {
      // TODO: Call API
      // const response = await inventoryApi.createTransaction({
      //   type,
      //   items,
      //   note: formData.note,
      //   supplier: formData.supplier,
      //   invoiceNumber: formData.invoiceNumber
      // })

      toast.success(`Tạo phiếu ${type === 'IMPORT' ? 'nhập' : 'xuất'} kho thành công!`)
      router.push('/admin/inventory/transactions')
    } catch (error: any) {
      toast.error(error.message || 'Có lỗi xảy ra')
    } finally {
      setIsSubmitting(false)
    }
  }

  const calculateTotal = () => {
    return items.reduce((sum, item) => sum + (item.quantity * item.price), 0)
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
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
          <Link href="/admin/inventory/transactions" className="hover:text-red-500">Xuất nhập kho</Link>
          <span>/</span>
          <span className="text-gray-900">Tạo phiếu {type === 'IMPORT' ? 'nhập' : 'xuất'}</span>
        </nav>

        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">
              Tạo phiếu {type === 'IMPORT' ? 'nhập kho' : 'xuất kho'}
            </h1>
            <p className="text-gray-600 mt-1">
              {type === 'IMPORT' 
                ? 'Nhập hàng mới vào kho' 
                : 'Xuất hàng ra khỏi kho'}
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Main Form */}
            <div className="lg:col-span-2 space-y-6">
              {/* Transaction Info */}
              <div className="bg-white rounded-lg shadow-sm p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-4">Thông tin phiếu</h2>
                
                <div className="space-y-4">
                  {type === 'IMPORT' && (
                    <>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                          Nhà cung cấp
                        </label>
                        <input
                          type="text"
                          value={formData.supplier}
                          onChange={(e) => setFormData({...formData, supplier: e.target.value})}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                          placeholder="Nhập tên nhà cung cấp"
                        />
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                          Số hóa đơn
                        </label>
                        <input
                          type="text"
                          value={formData.invoiceNumber}
                          onChange={(e) => setFormData({...formData, invoiceNumber: e.target.value})}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                          placeholder="Nhập số hóa đơn"
                        />
                      </div>
                    </>
                  )}

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Ghi chú
                    </label>
                    <textarea
                      value={formData.note}
                      onChange={(e) => setFormData({...formData, note: e.target.value})}
                      rows={3}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      placeholder="Nhập ghi chú (không bắt buộc)"
                    />
                  </div>
                </div>
              </div>

              {/* Products List */}
              <div className="bg-white rounded-lg shadow-sm p-6">
                <div className="flex justify-between items-center mb-4">
                  <h2 className="text-xl font-bold text-gray-900">Danh sách sản phẩm</h2>
                  <button
                    type="button"
                    onClick={() => setShowProductModal(true)}
                    className="flex items-center space-x-2 bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition-colors"
                  >
                    <FiPlus />
                    <span>Thêm sản phẩm</span>
                  </button>
                </div>

                {items.length === 0 ? (
                  <div className="text-center py-12 border-2 border-dashed border-gray-300 rounded-lg">
                    <p className="text-gray-500">Chưa có sản phẩm nào</p>
                    <button
                      type="button"
                      onClick={() => setShowProductModal(true)}
                      className="mt-4 text-red-500 hover:text-red-600 font-medium"
                    >
                      Thêm sản phẩm đầu tiên
                    </button>
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Sản phẩm</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Số lượng</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Đơn giá</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Thành tiền</th>
                          <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Thao tác</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-200">
                        {items.map((item) => (
                          <tr key={item.productId}>
                            <td className="px-4 py-4 text-sm text-gray-900">{item.productName}</td>
                            <td className="px-4 py-4 text-sm text-gray-900">{item.quantity}</td>
                            <td className="px-4 py-4 text-sm text-gray-900">{formatPrice(item.price)}</td>
                            <td className="px-4 py-4 text-sm font-medium text-gray-900">
                              {formatPrice(item.quantity * item.price)}
                            </td>
                            <td className="px-4 py-4 text-right">
                              <button
                                type="button"
                                onClick={() => handleRemoveItem(item.productId)}
                                className="text-red-500 hover:text-red-600"
                              >
                                <FiTrash2 />
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>

            {/* Summary Sidebar */}
            <div className="lg:col-span-1">
              <div className="bg-white rounded-lg shadow-sm p-6 sticky top-24">
                <h2 className="text-xl font-bold text-gray-900 mb-6">Tổng quan</h2>
                
                <div className="space-y-4">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Loại phiếu:</span>
                    <span className={`font-semibold ${
                      type === 'IMPORT' ? 'text-green-600' : 'text-blue-600'
                    }`}>
                      {type === 'IMPORT' ? 'Nhập kho' : 'Xuất kho'}
                    </span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-gray-600">Số sản phẩm:</span>
                    <span className="font-semibold">{items.length}</span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-gray-600">Tổng số lượng:</span>
                    <span className="font-semibold">
                      {items.reduce((sum, item) => sum + item.quantity, 0)}
                    </span>
                  </div>

                  <div className="border-t pt-4">
                    <div className="flex justify-between text-lg font-bold">
                      <span>Tổng tiền:</span>
                      <span className="text-red-500">{formatPrice(calculateTotal())}</span>
                    </div>
                  </div>
                </div>

                <div className="mt-6 space-y-3">
                  <button
                    type="submit"
                    disabled={isSubmitting || items.length === 0}
                    className="w-full flex items-center justify-center space-x-2 bg-red-500 text-white py-3 px-6 rounded-lg font-semibold hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <FiSave />
                    <span>{isSubmitting ? 'Đang xử lý...' : 'Tạo phiếu'}</span>
                  </button>

                  <Link
                    href="/admin/inventory/transactions"
                    className="w-full flex items-center justify-center space-x-2 bg-gray-200 text-gray-700 py-3 px-6 rounded-lg font-semibold hover:bg-gray-300 transition-colors"
                  >
                    <FiX />
                    <span>Hủy</span>
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </form>

        {/* Add Product Modal */}
        {showProductModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
              <div className="p-6">
                <div className="flex justify-between items-center mb-6">
                  <h3 className="text-xl font-bold text-gray-900">Chọn sản phẩm</h3>
                  <button
                    onClick={() => setShowProductModal(false)}
                    className="text-gray-400 hover:text-gray-600"
                  >
                    <FiX size={24} />
                  </button>
                </div>

                <div className="space-y-4">
                  {/* Product Selection */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Sản phẩm *
                    </label>
                    <select
                      value={selectedProduct?.id || ''}
                      onChange={(e) => {
                        const product = products.find(p => p.id === Number(e.target.value))
                        setSelectedProduct(product)
                        setPrice(product?.price || 0)
                      }}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                    >
                      <option value="">Chọn sản phẩm</option>
                      {products.map(product => (
                        <option key={product.id} value={product.id}>
                          {product.name} - Tồn kho: {product.stock}
                        </option>
                      ))}
                    </select>
                  </div>

                  {/* Quantity */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Số lượng *
                    </label>
                    <input
                      type="number"
                      min="1"
                      value={quantity}
                      onChange={(e) => setQuantity(Number(e.target.value))}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                    />
                    {type === 'EXPORT' && selectedProduct && (
                      <p className="text-sm text-gray-500 mt-1">
                        Tồn kho hiện tại: {selectedProduct.stock}
                      </p>
                    )}
                  </div>

                  {/* Price */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Đơn giá *
                    </label>
                    <input
                      type="number"
                      min="0"
                      value={price}
                      onChange={(e) => setPrice(Number(e.target.value))}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                    />
                  </div>

                  {/* Total */}
                  {quantity > 0 && price > 0 && (
                    <div className="bg-gray-50 p-4 rounded-lg">
                      <div className="flex justify-between items-center">
                        <span className="text-gray-600">Thành tiền:</span>
                        <span className="text-xl font-bold text-red-500">
                          {formatPrice(quantity * price)}
                        </span>
                      </div>
                    </div>
                  )}
                </div>

                <div className="flex space-x-3 mt-6">
                  <button
                    type="button"
                    onClick={handleAddProduct}
                    className="flex-1 bg-red-500 text-white py-3 px-6 rounded-lg font-semibold hover:bg-red-600 transition-colors"
                  >
                    Thêm
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowProductModal(false)}
                    className="flex-1 bg-gray-200 text-gray-700 py-3 px-6 rounded-lg font-semibold hover:bg-gray-300 transition-colors"
                  >
                    Hủy
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
