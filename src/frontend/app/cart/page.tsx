'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiTrash2, FiShoppingCart, FiArrowLeft } from 'react-icons/fi'
import { cartApi } from '@/lib/api'
import { useAuthStore } from '@/store/authStore'
import toast from 'react-hot-toast'

export default function CartPage() {
  const router = useRouter()
  const { isAuthenticated } = useAuthStore()
  const [cart, setCart] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    console.log('Cart page - isAuthenticated:', isAuthenticated)
    
    if (!isAuthenticated) {
      console.log('Not authenticated, redirecting to login')
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }
    
    console.log('Loading cart...')
    loadCart()
  }, [isAuthenticated])

  const loadCart = async () => {
    try {
      const response = await cartApi.getCart()
      console.log('Cart API response:', response)
      console.log('Cart data:', response.data)
      
      if (response.success) {
        setCart(response.data)
      } else {
        console.warn('Cart API returned success=false')
      }
    } catch (error) {
      console.error('Error loading cart:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleUpdateQuantity = async (itemId: number, newQuantity: number) => {
    if (newQuantity < 1) return
    
    try {
      const response = await cartApi.updateCartItem(itemId, newQuantity)
      if (response.success) {
        loadCart()
        toast.success('Đã cập nhật số lượng')
      }
    } catch (error: any) {
      toast.error(error.message || 'Lỗi khi cập nhật')
    }
  }

  const handleRemoveItem = async (itemId: number) => {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return
    
    try {
      const response = await cartApi.removeCartItem(itemId)
      if (response.success) {
        loadCart()
        toast.success('Đã xóa sản phẩm')
      }
    } catch (error: any) {
      toast.error(error.message || 'Lỗi khi xóa')
    }
  }

  const handleCheckout = () => {
    router.push('/checkout')
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  const calculateTotal = () => {
    if (!cart?.items) return 0
    return cart.items.reduce((sum: number, item: any) => 
      sum + (item.price * item.quantity), 0
    )
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

  const isEmpty = !cart?.items || cart.items.length === 0

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4">
        <div className="mb-6">
          <Link href="/" className="flex items-center text-blue-600 hover:text-blue-700">
            <FiArrowLeft className="mr-2" />
            Tiếp tục mua sắm
          </Link>
        </div>

        <h1 className="text-3xl font-bold text-gray-900 mb-8">Giỏ hàng của bạn</h1>

        {isEmpty ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <FiShoppingCart size={64} className="mx-auto text-gray-400 mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Giỏ hàng trống</h3>
            <p className="text-gray-600 mb-6">Hãy thêm sản phẩm vào giỏ hàng để tiếp tục</p>
            <Link
              href="/"
              className="inline-block bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Khám phá sản phẩm
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Cart Items */}
            <div className="lg:col-span-2 space-y-4">
              {cart.items.map((item: any) => (
                <div key={item.id} className="bg-white rounded-lg shadow-sm p-6">
                  <div className="flex items-center space-x-4">
                    {/* Image */}
                    <div className="w-24 h-24 bg-gray-100 rounded-lg flex-shrink-0 overflow-hidden">
                      {item.product?.imageUrl ? (
                        <img 
                          src={item.product.imageUrl} 
                          alt={item.product.name}
                          className="w-full h-full object-contain"
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-gray-400">
                          <FiShoppingCart size={32} />
                        </div>
                      )}
                    </div>

                    {/* Info */}
                    <div className="flex-1">
                      <Link 
                        href={`/products/${item.product?.id}`}
                        className="text-lg font-semibold text-gray-900 hover:text-blue-600"
                      >
                        {item.product?.name || 'Sản phẩm'}
                      </Link>
                      <p className="text-red-600 font-bold mt-1">
                        {formatPrice(item.price)}
                      </p>
                    </div>

                    {/* Quantity */}
                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => handleUpdateQuantity(item.id, item.quantity - 1)}
                        className="w-8 h-8 border border-gray-300 rounded hover:bg-gray-100"
                      >
                        -
                      </button>
                      <input
                        type="number"
                        value={item.quantity}
                        onChange={(e) => handleUpdateQuantity(item.id, parseInt(e.target.value) || 1)}
                        className="w-16 text-center border border-gray-300 rounded py-1"
                      />
                      <button
                        onClick={() => handleUpdateQuantity(item.id, item.quantity + 1)}
                        className="w-8 h-8 border border-gray-300 rounded hover:bg-gray-100"
                      >
                        +
                      </button>
                    </div>

                    {/* Subtotal */}
                    <div className="text-right w-32">
                      <p className="text-lg font-bold text-gray-900">
                        {formatPrice(item.price * item.quantity)}
                      </p>
                    </div>

                    {/* Remove */}
                    <button
                      onClick={() => handleRemoveItem(item.id)}
                      className="text-red-500 hover:text-red-700 p-2"
                    >
                      <FiTrash2 size={20} />
                    </button>
                  </div>
                </div>
              ))}
            </div>

            {/* Summary */}
            <div className="lg:col-span-1">
              <div className="bg-white rounded-lg shadow-sm p-6 sticky top-4">
                <h2 className="text-xl font-bold text-gray-900 mb-4">Tóm tắt đơn hàng</h2>
                
                <div className="space-y-3 mb-4">
                  <div className="flex justify-between text-gray-600">
                    <span>Tạm tính</span>
                    <span>{formatPrice(calculateTotal())}</span>
                  </div>
                  <div className="flex justify-between text-gray-600">
                    <span>Phí vận chuyển</span>
                    <span className="text-green-600">Miễn phí</span>
                  </div>
                  <div className="border-t pt-3 flex justify-between text-lg font-bold">
                    <span>Tổng cộng</span>
                    <span className="text-red-600">{formatPrice(calculateTotal())}</span>
                  </div>
                </div>

                <button
                  onClick={handleCheckout}
                  className="w-full bg-red-600 text-white py-3 rounded-lg hover:bg-red-700 transition-colors font-semibold"
                >
                  Tiến hành thanh toán
                </button>

                <div className="mt-4 space-y-2 text-sm text-gray-600">
                  <p className="flex items-center">
                    <span className="mr-2">✓</span>
                    Miễn phí vận chuyển toàn quốc
                  </p>
                  <p className="flex items-center">
                    <span className="mr-2">✓</span>
                    Thanh toán khi nhận hàng
                  </p>
                  <p className="flex items-center">
                    <span className="mr-2">✓</span>
                    Đổi trả trong 7 ngày
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
