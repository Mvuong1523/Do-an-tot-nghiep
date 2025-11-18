'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiShoppingCart, FiTrash2, FiPlus, FiMinus, FiArrowLeft } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useTranslation } from '@/hooks/useTranslation'
import { useAuthStore } from '@/store/authStore'
import { cartApi } from '@/lib/api'
import Image from 'next/image'

export default function CartPage() {
  const { t } = useTranslation()
  const router = useRouter()
  const { isAuthenticated } = useAuthStore()
  
  const [cart, setCart] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [updating, setUpdating] = useState<number | null>(null)

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập để xem giỏ hàng')
      router.push('/login')
      return
    }
    loadCart()
  }, [isAuthenticated, router])

  const loadCart = async () => {
    try {
      setLoading(true)
      const response = await cartApi.getCart()
      setCart(response.data)
    } catch (error) {
      toast.error('Lỗi khi tải giỏ hàng')
    } finally {
      setLoading(false)
    }
  }

  const handleUpdateQuantity = async (itemId: number, newQuantity: number) => {
    if (newQuantity < 1) return
    
    try {
      setUpdating(itemId)
      await cartApi.updateCartItem(itemId, newQuantity)
      await loadCart()
      toast.success('Cập nhật thành công')
    } catch (error: any) {
      toast.error(error.message)
    } finally {
      setUpdating(null)
    }
  }

  const handleRemoveItem = async (itemId: number) => {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return
    
    try {
      await cartApi.removeCartItem(itemId)
      await loadCart()
      toast.success('Đã xóa sản phẩm')
    } catch (error: any) {
      toast.error(error.message)
    }
  }

  const handleClearCart = async () => {
    if (!confirm('Bạn có chắc muốn xóa toàn bộ giỏ hàng?')) return
    
    try {
      await cartApi.clearCart()
      await loadCart()
      toast.success('Đã xóa giỏ hàng')
    } catch (error: any) {
      toast.error(error.message)
    }
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
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
          <Link href="/" className="hover:text-red-500">{t('home')}</Link>
          <span>/</span>
          <span className="text-gray-900">Giỏ hàng</span>
        </nav>

        <div className="flex items-center justify-between mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Giỏ hàng của bạn</h1>
          {cart?.items?.length > 0 && (
            <button
              onClick={handleClearCart}
              className="text-red-500 hover:text-red-600 flex items-center space-x-2"
            >
              <FiTrash2 />
              <span>Xóa tất cả</span>
            </button>
          )}
        </div>

        {!cart || cart.items?.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <FiShoppingCart size={64} className="mx-auto text-gray-400 mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              Giỏ hàng trống
            </h3>
            <p className="text-gray-600 mb-6">
              Bạn chưa có sản phẩm nào trong giỏ hàng
            </p>
            <Link
              href="/products"
              className="inline-flex items-center space-x-2 bg-red-500 text-white px-6 py-3 rounded-lg hover:bg-red-600 transition-colors"
            >
              <FiArrowLeft />
              <span>Tiếp tục mua sắm</span>
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Cart Items */}
            <div className="lg:col-span-2 space-y-4">
              {cart.items.map((item: any) => (
                <div key={item.id} className="bg-white rounded-lg shadow-sm p-4">
                  <div className="flex items-center space-x-4">
                    {/* Product Image */}
                    <div className="w-24 h-24 bg-gray-100 rounded flex-shrink-0 relative">
                      {item.productImage && (
                        <Image
                          src={item.productImage}
                          alt={item.productName}
                          fill
                          className="object-cover rounded"
                        />
                      )}
                    </div>

                    {/* Product Info */}
                    <div className="flex-1">
                      <Link
                        href={`/products/${item.productId}`}
                        className="text-lg font-semibold text-gray-900 hover:text-red-500"
                      >
                        {item.productName}
                      </Link>
                      <p className="text-sm text-gray-500">SKU: {item.sku}</p>
                      <p className="text-lg font-bold text-red-500 mt-1">
                        {formatPrice(item.price)}
                      </p>
                      {item.stockQuantity < 10 && (
                        <p className="text-sm text-orange-500 mt-1">
                          Chỉ còn {item.stockQuantity} sản phẩm
                        </p>
                      )}
                    </div>

                    {/* Quantity Controls */}
                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => handleUpdateQuantity(item.id, item.quantity - 1)}
                        disabled={updating === item.id || item.quantity <= 1}
                        className="p-2 border border-gray-300 rounded hover:bg-gray-100 disabled:opacity-50"
                      >
                        <FiMinus />
                      </button>
                      <span className="w-12 text-center font-semibold">
                        {item.quantity}
                      </span>
                      <button
                        onClick={() => handleUpdateQuantity(item.id, item.quantity + 1)}
                        disabled={updating === item.id || item.quantity >= item.stockQuantity}
                        className="p-2 border border-gray-300 rounded hover:bg-gray-100 disabled:opacity-50"
                      >
                        <FiPlus />
                      </button>
                    </div>

                    {/* Subtotal */}
                    <div className="text-right">
                      <p className="text-lg font-bold text-gray-900">
                        {formatPrice(item.subtotal)}
                      </p>
                      <button
                        onClick={() => handleRemoveItem(item.id)}
                        className="text-red-500 hover:text-red-600 text-sm mt-2"
                      >
                        Xóa
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Order Summary */}
            <div className="lg:col-span-1">
              <div className="bg-white rounded-lg shadow-sm p-6 sticky top-4">
                <h2 className="text-xl font-semibold mb-4">Tổng đơn hàng</h2>
                
                <div className="space-y-3 mb-6">
                  <div className="flex justify-between text-gray-600">
                    <span>Tạm tính ({cart.totalItems} sản phẩm)</span>
                    <span>{formatPrice(cart.totalAmount)}</span>
                  </div>
                  <div className="flex justify-between text-gray-600">
                    <span>Phí vận chuyển</span>
                    <span>Miễn phí</span>
                  </div>
                  <div className="border-t pt-3 flex justify-between text-lg font-bold">
                    <span>Tổng cộng</span>
                    <span className="text-red-500">{formatPrice(cart.totalAmount)}</span>
                  </div>
                </div>

                <Link
                  href="/checkout"
                  className="block w-full bg-red-500 text-white text-center py-3 rounded-lg hover:bg-red-600 transition-colors font-semibold"
                >
                  Tiến hành thanh toán
                </Link>

                <Link
                  href="/products"
                  className="block w-full text-center py-3 text-gray-600 hover:text-gray-900 mt-3"
                >
                  Tiếp tục mua sắm
                </Link>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
