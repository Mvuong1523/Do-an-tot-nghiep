'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import Image from 'next/image'
import Link from 'next/link'
import { FiShoppingCart, FiHeart, FiShare2, FiStar, FiCheck, FiTruck, FiShield, FiRefreshCw } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useCartStore } from '@/store/cartStore'
import { useTranslation } from '@/hooks/useTranslation'
import { productApi } from '@/lib/api'

export default function ProductDetailPage() {
  const params = useParams()
  const router = useRouter()
  const { t } = useTranslation()
  const { addToCart } = useCartStore()
  
  const [product, setProduct] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [selectedImage, setSelectedImage] = useState(0)
  const [quantity, setQuantity] = useState(1)

  useEffect(() => {
    const loadProduct = async () => {
      try {
        const response = await productApi.getById(params.id as string)
        if (response.success && response.data) {
          setProduct(response.data)
        } else {
          toast.error('Không tìm thấy sản phẩm')
          router.push('/products')
        }
      } catch (error) {
        toast.error('Lỗi khi tải sản phẩm')
        router.push('/products')
      } finally {
        setLoading(false)
      }
    }

    if (params.id) {
      loadProduct()
    }
  }, [params.id, router])

  const handleAddToCart = () => {
    if (!product) return
    
    addToCart({
      id: product.id,
      name: product.name,
      price: product.price,
      image: product.imageUrl || '/images/placeholder.jpg',
      quantity: quantity,
    })
    
    toast.success(t('addedToCart'))
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

  if (!product) {
    return null
  }

  const images = product.imageUrl ? [product.imageUrl] : ['/images/placeholder.jpg']

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
          <Link href="/" className="hover:text-red-500">{t('home')}</Link>
          <span>/</span>
          <Link href="/products" className="hover:text-red-500">{t('allProducts')}</Link>
          <span>/</span>
          <span className="text-gray-900">{product.name}</span>
        </nav>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-12">
          {/* Product Images */}
          <div>
            <div className="bg-white rounded-lg p-4 mb-4">
              <div className="aspect-square relative">
                <Image
                  src={images[selectedImage]}
                  alt={product.name}
                  fill
                  className="object-contain"
                />
              </div>
            </div>
            
            {images.length > 1 && (
              <div className="grid grid-cols-4 gap-2">
                {images.map((img, index) => (
                  <button
                    key={index}
                    onClick={() => setSelectedImage(index)}
                    className={`aspect-square bg-white rounded-lg p-2 border-2 ${
                      selectedImage === index ? 'border-red-500' : 'border-gray-200'
                    }`}
                  >
                    <Image
                      src={img}
                      alt={`${product.name} ${index + 1}`}
                      width={100}
                      height={100}
                      className="w-full h-full object-contain"
                    />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Product Info */}
          <div>
            <h1 className="text-3xl font-bold text-gray-900 mb-4">{product.name}</h1>
            
            {/* Rating */}
            <div className="flex items-center space-x-4 mb-6">
              <div className="flex items-center">
                {[...Array(5)].map((_, i) => (
                  <FiStar
                    key={i}
                    className={`${i < 4 ? 'text-yellow-400 fill-current' : 'text-gray-300'}`}
                    size={20}
                  />
                ))}
                <span className="ml-2 text-gray-600">4.8 (1,250 đánh giá)</span>
              </div>
              <span className="text-gray-400">|</span>
              <span className="text-gray-600">Đã bán: {product.stockQuantity || 0}</span>
            </div>

            {/* Price */}
            <div className="bg-gray-50 rounded-lg p-6 mb-6">
              <div className="flex items-baseline space-x-4">
                <span className="text-4xl font-bold text-red-500">
                  {formatPrice(product.price)}
                </span>
              </div>
              <p className="text-sm text-gray-600 mt-2">Giá đã bao gồm VAT</p>
            </div>

            {/* SKU */}
            {product.sku && (
              <div className="mb-6">
                <span className="text-gray-600">Mã sản phẩm: </span>
                <span className="font-semibold">{product.sku}</span>
              </div>
            )}

            {/* Quantity */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Số lượng:
              </label>
              <div className="flex items-center space-x-4">
                <div className="flex items-center border border-gray-300 rounded-lg">
                  <button
                    onClick={() => setQuantity(Math.max(1, quantity - 1))}
                    className="px-4 py-2 hover:bg-gray-100"
                  >
                    -
                  </button>
                  <span className="px-6 py-2 border-x border-gray-300">{quantity}</span>
                  <button
                    onClick={() => setQuantity(quantity + 1)}
                    className="px-4 py-2 hover:bg-gray-100"
                  >
                    +
                  </button>
                </div>
                <span className="text-gray-600">
                  {product.stockQuantity > 0 ? `${product.stockQuantity} sản phẩm có sẵn` : 'Hết hàng'}
                </span>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex space-x-4 mb-6">
              <button
                onClick={handleAddToCart}
                disabled={!product.stockQuantity || product.stockQuantity === 0}
                className="flex-1 bg-red-500 text-white py-3 px-6 rounded-lg font-semibold hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
              >
                <FiShoppingCart className="mr-2" />
                {t('addToCart')}
              </button>
              <button className="p-3 border border-gray-300 rounded-lg hover:bg-gray-50">
                <FiHeart size={24} />
              </button>
              <button className="p-3 border border-gray-300 rounded-lg hover:bg-gray-50">
                <FiShare2 size={24} />
              </button>
            </div>

            {/* Features */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
              <div className="flex items-center space-x-3 p-4 bg-gray-50 rounded-lg">
                <FiTruck className="text-red-500" size={24} />
                <div>
                  <div className="font-semibold text-sm">Miễn phí vận chuyển</div>
                  <div className="text-xs text-gray-600">Toàn quốc</div>
                </div>
              </div>
              <div className="flex items-center space-x-3 p-4 bg-gray-50 rounded-lg">
                <FiShield className="text-red-500" size={24} />
                <div>
                  <div className="font-semibold text-sm">Bảo hành chính hãng</div>
                  <div className="text-xs text-gray-600">12 tháng</div>
                </div>
              </div>
              <div className="flex items-center space-x-3 p-4 bg-gray-50 rounded-lg">
                <FiRefreshCw className="text-red-500" size={24} />
                <div>
                  <div className="font-semibold text-sm">Đổi trả dễ dàng</div>
                  <div className="text-xs text-gray-600">Trong 7 ngày</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Product Description */}
        <div className="bg-white rounded-lg p-6 mb-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Mô tả sản phẩm</h2>
          <div className="prose max-w-none">
            <p className="text-gray-700 whitespace-pre-line">
              {product.description || 'Chưa có mô tả chi tiết cho sản phẩm này.'}
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
