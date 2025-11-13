'use client'

import { useState } from 'react'
import Image from 'next/image'
import Link from 'next/link'
import { FiShoppingCart, FiHeart, FiStar, FiShare2, FiTruck, FiShield, FiRotateCcw } from 'react-icons/fi'
import { FaPhone } from 'react-icons/fa'
import toast from 'react-hot-toast'

// Mock data - sẽ được thay thế bằng API call
const product = {
  id: 1,
  name: 'iPhone 16 Pro Max 256GB - Chính hãng VN/A',
  price: 29990000,
  originalPrice: 34990000,
  discount: 14,
  images: [
    '/images/iphone-16-pro-max-1.jpg',
    '/images/iphone-16-pro-max-2.jpg',
    '/images/iphone-16-pro-max-3.jpg',
    '/images/iphone-16-pro-max-4.jpg',
  ],
  rating: 4.8,
  reviews: 1250,
  badge: 'Hot',
  description: 'iPhone 16 Pro Max với chip A18 Pro mạnh mẽ, camera 48MP chuyên nghiệp và màn hình Super Retina XDR 6.7 inch.',
  specifications: {
    'Màn hình': '6.7 inch Super Retina XDR',
    'Chip': 'A18 Pro',
    'Camera': '48MP + 12MP + 12MP',
    'Pin': '4422 mAh',
    'Bộ nhớ': '256GB',
    'Hệ điều hành': 'iOS 18',
    'Màu sắc': 'Titanium Natural, Titanium Blue, Titanium White, Titanium Black'
  },
  colors: [
    { name: 'Titanium Natural', color: '#F5F5DC', available: true },
    { name: 'Titanium Blue', color: '#4169E1', available: true },
    { name: 'Titanium White', color: '#FFFFFF', available: true },
    { name: 'Titanium Black', color: '#000000', available: false },
  ],
  storage: [
    { size: '256GB', price: 29990000, available: true },
    { size: '512GB', price: 34990000, available: true },
    { size: '1TB', price: 39990000, available: false },
  ]
}

export default function ProductDetailPage() {
  const [selectedImage, setSelectedImage] = useState(0)
  const [selectedColor, setSelectedColor] = useState(0)
  const [selectedStorage, setSelectedStorage] = useState(0)
  const [quantity, setQuantity] = useState(1)
  const [isLiked, setIsLiked] = useState(false)

  const handleAddToCart = () => {
    toast.success('Đã thêm vào giỏ hàng!')
  }

  const handleBuyNow = () => {
    toast.success('Chuyển đến trang thanh toán!')
  }

  const handleToggleLike = () => {
    setIsLiked(!isLiked)
    toast.success(isLiked ? 'Đã bỏ yêu thích' : 'Đã thêm vào yêu thích')
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
          <Link href="/product1s" className="hover:text-red-500">Sản phẩm</Link>
          <span>/</span>
          <Link href="/product1s?category=phone" className="hover:text-red-500">Điện thoại</Link>
          <span>/</span>
          <span className="text-gray-900">{product.name}</span>
        </nav>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Product Images */}
          <div className="space-y-4">
            {/* Main Image */}
            <div className="aspect-square bg-white rounded-lg overflow-hidden">
              <Image
                src={product.images[selectedImage]}
                alt={product.name}
                width={600}
                height={600}
                className="w-full h-full object-cover"
              />
            </div>

            {/* Thumbnail Images */}
            <div className="grid grid-cols-4 gap-2">
              {product.images.map((image, index) => (
                <button
                  key={index}
                  onClick={() => setSelectedImage(index)}
                  className={`aspect-square bg-white rounded-lg overflow-hidden border-2 ${
                    selectedImage === index ? 'border-red-500' : 'border-gray-200'
                  }`}
                >
                  <Image
                    src={image}
                    alt={`${product.name} ${index + 1}`}
                    width={150}
                    height={150}
                    className="w-full h-full object-cover"
                  />
                </button>
              ))}
            </div>
          </div>

          {/* Product Info */}
          <div className="space-y-6">
            {/* Product Title */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="bg-red-500 text-white px-3 py-1 text-sm font-semibold rounded-full">
                  {product.badge}
                </span>
                <div className="flex items-center space-x-2">
                  <button
                    onClick={handleToggleLike}
                    className={`p-2 rounded-full ${
                      isLiked ? 'bg-red-500 text-white' : 'bg-gray-200 text-gray-600'
                    } hover:bg-red-500 hover:text-white transition-colors`}
                  >
                    <FiHeart size={20} fill={isLiked ? 'currentColor' : 'none'} />
                  </button>
                  <button className="p-2 bg-gray-200 text-gray-600 rounded-full hover:bg-gray-300 transition-colors">
                    <FiShare2 size={20} />
                  </button>
                </div>
              </div>
              <h1 className="text-2xl font-bold text-gray-900 mb-2">{product.name}</h1>
              
              {/* Rating */}
              <div className="flex items-center mb-4">
                <div className="flex items-center">
                  {[...Array(5)].map((_, i) => (
                    <FiStar
                      key={i}
                      size={16}
                      className={`${
                        i < Math.floor(product.rating)
                          ? 'text-yellow-400 fill-current'
                          : 'text-gray-300'
                      }`}
                    />
                  ))}
                </div>
                <span className="ml-2 text-sm text-gray-600">
                  {product.rating} ({product.reviews} đánh giá)
                </span>
              </div>
            </div>

            {/* Price */}
            <div className="bg-gray-50 rounded-lg p-4">
              <div className="flex items-center space-x-4 mb-2">
                <span className="text-3xl font-bold text-red-500">
                  {formatPrice(product.storage[selectedStorage].price)}
                </span>
                {product.originalPrice && (
                  <span className="text-lg text-gray-500 line-through">
                    {formatPrice(product.originalPrice)}
                  </span>
                )}
                {product.discount > 0 && (
                  <span className="bg-red-500 text-white px-2 py-1 text-sm font-semibold rounded">
                    -{product.discount}%
                  </span>
                )}
              </div>
              <p className="text-sm text-gray-600">
                Giá đã bao gồm VAT. Miễn phí vận chuyển toàn quốc.
              </p>
            </div>

            {/* Storage Options */}
            <div>
              <h3 className="font-semibold text-gray-900 mb-3">Dung lượng:</h3>
              <div className="grid grid-cols-3 gap-2">
                {product.storage.map((option, index) => (
                  <button
                    key={index}
                    onClick={() => setSelectedStorage(index)}
                    disabled={!option.available}
                    className={`p-3 rounded-lg border-2 text-center ${
                      selectedStorage === index
                        ? 'border-red-500 bg-red-50 text-red-500'
                        : option.available
                        ? 'border-gray-200 hover:border-red-300'
                        : 'border-gray-200 bg-gray-100 text-gray-400 cursor-not-allowed'
                    }`}
                  >
                    <div className="font-semibold">{option.size}</div>
                    <div className="text-sm">{formatPrice(option.price)}</div>
                    {!option.available && <div className="text-xs">Hết hàng</div>}
                  </button>
                ))}
              </div>
            </div>

            {/* Color Options */}
            <div>
              <h3 className="font-semibold text-gray-900 mb-3">Màu sắc:</h3>
              <div className="flex space-x-3">
                {product.colors.map((color, index) => (
                  <button
                    key={index}
                    onClick={() => setSelectedColor(index)}
                    disabled={!color.available}
                    className={`w-12 h-12 rounded-full border-2 ${
                      selectedColor === index ? 'border-red-500' : 'border-gray-300'
                    } ${!color.available ? 'opacity-50 cursor-not-allowed' : ''}`}
                    style={{ backgroundColor: color.color }}
                    title={color.name}
                  />
                ))}
              </div>
              <p className="text-sm text-gray-600 mt-2">
                {product.colors[selectedColor].name}
              </p>
            </div>

            {/* Quantity */}
            <div>
              <h3 className="font-semibold text-gray-900 mb-3">Số lượng:</h3>
              <div className="flex items-center space-x-3">
                <button
                  onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  className="w-10 h-10 bg-gray-200 rounded-lg flex items-center justify-center hover:bg-gray-300 transition-colors"
                >
                  -
                </button>
                <span className="w-16 text-center font-semibold">{quantity}</span>
                <button
                  onClick={() => setQuantity(quantity + 1)}
                  className="w-10 h-10 bg-gray-200 rounded-lg flex items-center justify-center hover:bg-gray-300 transition-colors"
                >
                  +
                </button>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="space-y-3">
              <button
                onClick={handleAddToCart}
                className="w-full bg-red-500 text-white py-3 px-6 rounded-lg font-semibold hover:bg-red-600 transition-colors flex items-center justify-center space-x-2"
              >
                <FiShoppingCart size={20} />
                <span>Thêm vào giỏ hàng</span>
              </button>
              <button
                onClick={handleBuyNow}
                className="w-full bg-orange-500 text-white py-3 px-6 rounded-lg font-semibold hover:bg-orange-600 transition-colors"
              >
                Mua ngay
              </button>
            </div>

            {/* Contact */}
            <div className="bg-blue-50 rounded-lg p-4">
              <div className="flex items-center space-x-3">
                <FaPhone className="text-blue-500" size={20} />
                <div>
                  <p className="font-semibold text-blue-900">Tư vấn mua hàng</p>
                  <p className="text-blue-700">1900.2091 (Nhánh 1)</p>
                  <p className="text-sm text-blue-600">Từ 8h30 - 21h30</p>
                </div>
              </div>
            </div>

            {/* Features */}
            <div className="grid grid-cols-3 gap-4">
              <div className="text-center">
                <FiTruck className="mx-auto text-green-500 mb-2" size={24} />
                <p className="text-sm font-semibold">Miễn phí vận chuyển</p>
              </div>
              <div className="text-center">
                <FiShield className="mx-auto text-blue-500 mb-2" size={24} />
                <p className="text-sm font-semibold">Bảo hành chính hãng</p>
              </div>
              <div className="text-center">
                <FiRotateCcw className="mx-auto text-orange-500 mb-2" size={24} />
                <p className="text-sm font-semibold">Đổi trả trong 7 ngày</p>
              </div>
            </div>
          </div>
        </div>

        {/* Product Details */}
        <div className="mt-12 bg-white rounded-lg p-6">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">Thông tin sản phẩm</h2>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            {/* Description */}
            <div>
              <h3 className="font-semibold text-gray-900 mb-3">Mô tả sản phẩm</h3>
              <p className="text-gray-700 leading-relaxed">{product.description}</p>
            </div>

            {/* Specifications */}
            <div>
              <h3 className="font-semibold text-gray-900 mb-3">Thông số kỹ thuật</h3>
              <div className="space-y-2">
                {Object.entries(product.specifications).map(([key, value]) => (
                  <div key={key} className="flex justify-between py-2 border-b border-gray-100">
                    <span className="font-medium text-gray-600">{key}:</span>
                    <span className="text-gray-900">{value}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
