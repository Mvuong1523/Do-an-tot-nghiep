'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import Link from 'next/link'
import { FiShoppingCart, FiHeart, FiStar, FiMinus, FiPlus, FiTruck, FiShield, FiRefreshCw } from 'react-icons/fi'
import { productApi } from '@/lib/api'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'

export default function ProductDetailPage() {
  const params = useParams()
  const router = useRouter()
  const { user } = useAuthStore()
  const [product, setProduct] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [quantity, setQuantity] = useState(1)
  const [selectedImage, setSelectedImage] = useState(0)

  useEffect(() => {
    if (params.id) {
      loadProduct()
    }
  }, [params.id])

  const loadProduct = async () => {
    try {
      const response = await productApi.getById(params.id as string)
      if (response.success) {
        setProduct(response.data)
      } else {
        toast.error('Không tìm thấy sản phẩm')
        router.push('/')
      }
    } catch (error) {
      console.error('Error loading product:', error)
      toast.error('Lỗi khi tải sản phẩm')
      router.push('/')
    } finally {
      setLoading(false)
    }
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  const handleAddToCart = () => {
    if (!user) {
      toast.error('Vui lòng đăng nhập để thêm vào giỏ hàng')
      router.push('/login')
      return
    }

    // TODO: Call cart API
    toast.success('Đã thêm vào giỏ hàng')
  }

  const handleBuyNow = () => {
    if (!user) {
      toast.error('Vui lòng đăng nhập để mua hàng')
      router.push('/login')
      return
    }

    // TODO: Add to cart and redirect to checkout
    toast.success('Chuyển đến trang thanh toán')
    router.push('/checkout')
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

  if (!product) {
    return null
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="container mx-auto px-4 py-4">
          <nav className="flex items-center space-x-2 text-sm text-gray-600">
            <Link href="/" className="hover:text-red-500">Trang chủ</Link>
            <span>/</span>
            {product.category && (
              <>
                <Link href={`/?category=${product.category.id}`} className="hover:text-red-500">
                  {product.category.name}
                </Link>
                <span>/</span>
              </>
            )}
            <span className="text-gray-900">{product.name}</span>
          </nav>
        </div>
      </header>

      {/* Main Content */}
      <div className="container mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Images */}
            <div>
              <div className="aspect-square bg-gray-100 rounded-lg mb-4 overflow-hidden">
                {product.imageUrl ? (
                  <img
                    src={product.imageUrl}
                    alt={product.name}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-gray-400">
                    No Image
                  </div>
                )}
              </div>
              {/* Thumbnail images would go here */}
            </div>

            {/* Product Info */}
            <div>
              <h1 className="text-2xl font-bold mb-4">{product.name}</h1>

              {/* Rating */}
              <div className="flex items-center mb-4">
                <div className="flex text-yellow-400">
                  {[...Array(5)].map((_, i) => (
                    <FiStar key={i} size={20} fill="currentColor" />
                  ))}
                </div>
                <span className="ml-2 text-gray-600">(0 đánh giá)</span>
              </div>

              {/* Price */}
              <div className="bg-gray-50 rounded-lg p-4 mb-6">
                <div className="text-3xl font-bold text-red-600 mb-2">
                  {formatPrice(product.price || 0)}
                </div>
                {product.stockQuantity > 0 ? (
                  <div className="flex items-center text-green-600">
                    <span className="font-medium">Còn hàng</span>
                    <span className="ml-2 text-sm">({product.stockQuantity} sản phẩm)</span>
                  </div>
                ) : (
                  <div className="text-red-600 font-medium">Hết hàng</div>
                )}
              </div>

              {/* Quantity */}
              <div className="mb-6">
                <label className="block text-sm font-medium mb-2">Số lượng:</label>
                <div className="flex items-center space-x-4">
                  <div className="flex items-center border border-gray-300 rounded-lg">
                    <button
                      onClick={() => setQuantity(Math.max(1, quantity - 1))}
                      className="p-2 hover:bg-gray-100"
                      disabled={quantity <= 1}
                    >
                      <FiMinus />
                    </button>
                    <input
                      type="number"
                      value={quantity}
                      onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                      className="w-16 text-center border-x border-gray-300 focus:outline-none"
                      min="1"
                      max={product.stockQuantity}
                    />
                    <button
                      onClick={() => setQuantity(Math.min(product.stockQuantity, quantity + 1))}
                      className="p-2 hover:bg-gray-100"
                      disabled={quantity >= product.stockQuantity}
                    >
                      <FiPlus />
                    </button>
                  </div>
                </div>
              </div>

              {/* Actions */}
              <div className="flex space-x-4 mb-6">
                <button
                  onClick={handleAddToCart}
                  disabled={product.stockQuantity === 0}
                  className="flex-1 flex items-center justify-center space-x-2 px-6 py-3 border-2 border-red-500 text-red-500 rounded-lg hover:bg-red-50 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <FiShoppingCart />
                  <span>Thêm vào giỏ</span>
                </button>
                <button
                  onClick={handleBuyNow}
                  disabled={product.stockQuantity === 0}
                  className="flex-1 px-6 py-3 bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Mua ngay
                </button>
                <button className="p-3 border border-gray-300 rounded-lg hover:bg-gray-50">
                  <FiHeart size={24} />
                </button>
              </div>

              {/* Features */}
              <div className="border-t pt-6 space-y-4">
                <div className="flex items-start space-x-3">
                  <FiTruck className="text-red-500 mt-1" size={20} />
                  <div>
                    <div className="font-medium">Miễn phí vận chuyển</div>
                    <div className="text-sm text-gray-600">Cho đơn hàng từ 500.000đ</div>
                  </div>
                </div>
                <div className="flex items-start space-x-3">
                  <FiShield className="text-red-500 mt-1" size={20} />
                  <div>
                    <div className="font-medium">Bảo hành chính hãng</div>
                    <div className="text-sm text-gray-600">12 tháng tại các trung tâm bảo hành</div>
                  </div>
                </div>
                <div className="flex items-start space-x-3">
                  <FiRefreshCw className="text-red-500 mt-1" size={20} />
                  <div>
                    <div className="font-medium">Đổi trả trong 7 ngày</div>
                    <div className="text-sm text-gray-600">Nếu sản phẩm lỗi hoặc không đúng mô tả</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Description */}
          <div className="mt-8 border-t pt-8">
            <h2 className="text-xl font-bold mb-4">Mô tả sản phẩm</h2>
            <div className="prose max-w-none">
              {product.description ? (
                <p className="text-gray-700 whitespace-pre-wrap">{product.description}</p>
              ) : (
                <p className="text-gray-500">Chưa có mô tả</p>
              )}
            </div>
          </div>

          {/* Specifications */}
          {product.specifications && Object.keys(product.specifications).length > 0 && (
            <div className="mt-8 border-t pt-8">
              <h2 className="text-xl font-bold mb-4">Thông số kỹ thuật</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {Object.entries(product.specifications).map(([key, value]: [string, any]) => (
                  <div key={key} className="flex border-b pb-2">
                    <span className="font-medium w-1/3">{key}:</span>
                    <span className="text-gray-700 w-2/3">{value}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
