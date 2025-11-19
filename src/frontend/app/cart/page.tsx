'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiTrash2, FiMinus, FiPlus, FiShoppingCart, FiArrowLeft } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'

export default function CartPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
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
      // TODO: Call cart API
      // const response = await cartApi.getCart()
      // setCart(response.data)

      // Mock data
      setCart({
        cartId: 1,
        items: [
          {
            itemId: 1,
            productId: 1,
            productName: 'iPhone 16 Pro Max 256GB',
            productImage: '/images/iphone.jpg',
            productSku: 'IP16PM-256',
            price: 29990000,
            quantity: 1,
            stockQuantity: 10,
            subtotal: 29990000,
            available: true
          },
          {
            itemId: 2,
            productId: 2,
            productName: 'MacBook Pro 14 inch M3',
            productImage: '/images/macbook.jpg',
            productSku: 'MBP14-M3',
            price: 45990000,
            quantity: 1,
            stockQuantity: 5,
            subtotal: 45990000,
            available: true
          }
        ],
        totalItems: 2,
        subtotal: 75980000,
        shippingFee: 0,
        discount: 0,
        total: 75980000
      })
    } catch (error) {
      console.error('Error loading cart:', error)
      toast.error('Lỗi khi tải giỏ hàng')
    } finally {
      setLoading(false)
    }
  }

  const updateQuantity = async (itemId: number, newQuantity: number) => {
    if (newQuantity < 1) return

    setUpdating(itemId)
    try {
      // TODO: Call update API
      // await cartApi.updateCartItem(itemId, { quantity: newQuantity })
      
      // Mock update
      setCart((prev: any) => ({
        ...prev,
        items: prev.items.map((item: any) =>
          item.itemId === itemId
            ? { ...item, quantity: newQuantity, subtotal: item.price * newQuantity }
            : item
        )
      }))

      // Recalculate totals
      setTimeout(() => {
        setCart((prev: any) => {
          const subtotal = prev.items.reduce((sum: number, item: any) => sum + item.subtotal, 0)
          return {
            ...prev,
            subtotal,
            total: subtotal + prev.shippingFee - prev.discount
          }
        })
      }, 100)

      toast.success('Đã cập nhật số lượng')
    } catch (error) {
      toast.error('Lỗi khi cập nhật')
    } finally {
      setUpdating(null)
    }
  }

  const removeItem = async (itemId: number) => {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return

    try {
      // TODO: Call remove API
      // await cartApi.removeCartItem(itemId)

      setCart((prev: any) => ({
        ...prev,
        items: prev.items.filter((item: any) => item.itemId !== itemId)
      }))

      toast.success('Đã xóa sản phẩm')
    } catch (error) {
      toast.error('Lỗi khi xóa sản phẩm')
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

  if (!cart || cart.items.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <div className="max-w-2xl mx-auto">
            <div className="bg-white rounded-lg shadow-sm p-12 text-center">
              <FiShoppingCart size={64} className="mx-auto text-gray-400 mb-4" />
              <h2 className="text-2xl font-bold mb-2">Giỏ hàng trống</h2>
              <p className="text-gray-600 mb-6">Bạn chưa có sản phẩm nào trong giỏ hàng</p>
              <Link
                href="/"
                className="inline-flex items-center space-x-2 bg-red-500 text-white px-6 py-3 rounded-lg hover:bg-red-600"
              >
                <FiArrowLeft />
                <span>Tiếp tục mua sắm</span>
              </Link>
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-6">
          <Link href="/" className="inline-flex items-center text-gray-600 hover:text-red-500 mb-4">
            <FiArrowLeft className="mr-2" />
            Tiếp tục mua sắm
          </Link>
          <h1 className="text-3xl font-bold">Giỏ hàng của bạn</h1>
          <p className="text-gray-600">{cart.totalItems} sản phẩm</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Cart Items */}
          <div className="lg:col-span-2 space-y-4">
            {cart.items.map((item: any) => (
              <div key={item.itemId} className="bg-white rounded-lg shadow-sm p-4">
                <div className="flex gap-4">
                  {/* Image */}
                  <Link href={`/products/${item.productId}`} className="flex-shrink-0">
                    <div className="w-24 h-24 bg-gray-100 rounded-lg overflow-hidden">
                      {item.productImage ? (
                        <img
                          src={item.productImage}
                          alt={item.productName}
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-gray-400 text-xs">
                          No Image
                        </div>
                      )}
                    </div>
                  </Link>

                  {/* Info */}
                  <div className="flex-1 min-w-0">
                    <Link
                      href={`/products/${item.productId}`}
                      className="font-medium hover:text-red-500 line-clamp-2"
                    >
                      {item.productName}
                    </Link>
                    <p className="text-sm text-gray-500 mt-1">SKU: {item.productSku}</p>
                    <div className="flex items-center justify-between mt-3">
                      <div className="text-red-600 font-bold">
                        {formatPrice(item.price)}
                      </div>
                      {!item.available && (
                        <span className="text-xs text-red-600">Hết hàng</span>
                      )}
                    </div>
                  </div>

                  {/* Quantity & Actions */}
                  <div className="flex flex-col items-end justify-between">
                    <button
                      onClick={() => removeItem(item.itemId)}
                      className="text-gray-400 hover:text-red-500"
                    >
                      <FiTrash2 size={20} />
                    </button>

                    <div className="flex items-center border border-gray-300 rounded-lg">
                      <button
                        onClick={() => updateQuantity(item.itemId, item.quantity - 1)}
                        disabled={item.quantity <= 1 || updating === item.itemId}
                        className="p-2 hover:bg-gray-100 disabled:opacity-50"
                      >
                        <FiMinus size={16} />
                      </button>
                      <input
                        type="number"
                        value={item.quantity}
                        onChange={(e) => {
                          const val = parseInt(e.target.value) || 1
                          updateQuantity(item.itemId, val)
                        }}
                        className="w-12 text-center border-x border-gray-300 focus:outline-none"
                        min="1"
                        max={item.stockQuantity}
                      />
                      <button
                        onClick={() => updateQuantity(item.itemId, item.quantity + 1)}
                        disabled={item.quantity >= item.stockQuantity || updating === item.itemId}
                        className="p-2 hover:bg-gray-100 disabled:opacity-50"
                      >
                        <FiPlus size={16} />
                      </button>
                    </div>

                    <div className="text-right mt-2">
                      <div className="font-bold">{formatPrice(item.subtotal)}</div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-sm p-6 sticky top-4">
              <h2 className="text-xl font-bold mb-4">Tóm tắt đơn hàng</h2>

              <div className="space-y-3 mb-4">
                <div className="flex justify-between">
                  <span className="text-gray-600">Tạm tính</span>
                  <span className="font-medium">{formatPrice(cart.subtotal)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Phí vận chuyển</span>
                  <span className="font-medium">
                    {cart.shippingFee === 0 ? 'Miễn phí' : formatPrice(cart.shippingFee)}
                  </span>
                </div>
                {cart.discount > 0 && (
                  <div className="flex justify-between text-green-600">
                    <span>Giảm giá</span>
                    <span>-{formatPrice(cart.discount)}</span>
                  </div>
                )}
              </div>

              <div className="border-t pt-4 mb-6">
                <div className="flex justify-between items-center">
                  <span className="text-lg font-bold">Tổng cộng</span>
                  <span className="text-2xl font-bold text-red-600">
                    {formatPrice(cart.total)}
                  </span>
                </div>
              </div>

              <Link
                href="/checkout"
                className="block w-full bg-red-500 text-white text-center py-3 rounded-lg hover:bg-red-600 font-medium"
              >
                Tiến hành thanh toán
              </Link>

              <div className="mt-4 text-sm text-gray-600 space-y-2">
                <p>✓ Miễn phí vận chuyển nội thành HN</p>
                <p>✓ Đổi trả trong 7 ngày</p>
                <p>✓ Bảo hành chính hãng 12 tháng</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
