'use client'

import { useState, useEffect } from 'react'
import { useSearchParams } from 'next/navigation'
import ProductCard from '@/components/product/ProductCard'
import { FiFilter, FiGrid, FiList, FiChevronDown } from 'react-icons/fi'
import { useTranslation } from '@/hooks/useTranslation'

// Mock data - sẽ được thay thế bằng API calls
const products = [
  {
    id: 1,
    name: 'iPhone 16 Pro Max 256GB - Chính hãng VN/A',
    price: 29990000,
    originalPrice: 34990000,
    discount: 14,
    image: '/images/iphone-16-pro-max.jpg',
    rating: 4.8,
    reviews: 1250,
    badge: 'Hot',
    category: 'phone',
    brand: 'Apple'
  },
  {
    id: 2,
    name: 'Điện thoại Xiaomi POCO C71 4GB/128GB',
    price: 2490000,
    originalPrice: null,
    discount: 0,
    image: '/images/xiaomi-poco-c71.jpg',
    rating: 4.5,
    reviews: 890,
    badge: 'Mới',
    category: 'phone',
    brand: 'Xiaomi'
  },
  {
    id: 3,
    name: 'Điện thoại Xiaomi POCO M6 8GB/256GB',
    price: 3990000,
    originalPrice: 5290000,
    discount: 25,
    image: '/images/xiaomi-poco-m6.jpg',
    rating: 4.6,
    reviews: 567,
    badge: 'Sale',
    category: 'phone',
    brand: 'Xiaomi'
  },
  {
    id: 4,
    name: 'Điện thoại Xiaomi 15T 12GB/512GB',
    price: 14990000,
    originalPrice: null,
    discount: 0,
    image: '/images/xiaomi-15t.jpg',
    rating: 4.7,
    reviews: 234,
    badge: 'Hot',
    category: 'phone',
    brand: 'Xiaomi'
  },
  {
    id: 5,
    name: 'MacBook Pro 14 inch M3 Pro',
    price: 45990000,
    originalPrice: 49990000,
    discount: 8,
    image: '/images/macbook-pro-14.jpg',
    rating: 4.9,
    reviews: 156,
    badge: 'Hot',
    category: 'laptop',
    brand: 'Apple'
  },
  {
    id: 6,
    name: 'Samsung Galaxy S24 Ultra 256GB',
    price: 24990000,
    originalPrice: 27990000,
    discount: 11,
    image: '/images/samsung-s24-ultra.jpg',
    rating: 4.7,
    reviews: 678,
    badge: 'Sale',
    category: 'phone',
    brand: 'Samsung'
  },
]

const brands = ['Tất cả', 'Apple', 'Samsung', 'Xiaomi', 'OPPO', 'Vivo', 'OnePlus']
const priceRanges = [
  { label: 'Tất cả', min: 0, max: Infinity },
  { label: 'Dưới 5 triệu', min: 0, max: 5000000 },
  { label: '5 - 10 triệu', min: 5000000, max: 10000000 },
  { label: '10 - 20 triệu', min: 10000000, max: 20000000 },
  { label: 'Trên 20 triệu', min: 20000000, max: Infinity },
]

export default function ProductsPage() {
  const { t } = useTranslation()
  const searchParams = useSearchParams()
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid')
  const [sortBy, setSortBy] = useState('default')
  const [selectedBrand, setSelectedBrand] = useState('Tất cả')
  const [selectedPriceRange, setSelectedPriceRange] = useState('Tất cả')
  const [showFilters, setShowFilters] = useState(false)
  const [filteredProducts, setFilteredProducts] = useState(products)

  const category = searchParams.get('category') || 'all'

  useEffect(() => {
    let filtered = products

    // Filter by category
    if (category !== 'all') {
      filtered = filtered.filter(product => product.category === category)
    }

    // Filter by brand
    if (selectedBrand !== 'Tất cả') {
      filtered = filtered.filter(product => product.brand === selectedBrand)
    }

    // Filter by price range
    const priceRange = priceRanges.find(range => range.label === selectedPriceRange)
    if (priceRange) {
      filtered = filtered.filter(product => 
        product.price >= priceRange.min && product.price <= priceRange.max
      )
    }

    // Sort products
    switch (sortBy) {
      case 'price-low':
        filtered.sort((a, b) => a.price - b.price)
        break
      case 'price-high':
        filtered.sort((a, b) => b.price - a.price)
        break
      case 'rating':
        filtered.sort((a, b) => b.rating - a.rating)
        break
      case 'newest':
        filtered.sort((a, b) => b.id - a.id)
        break
      default:
        // Keep original order
        break
    }

    setFilteredProducts(filtered)
  }, [category, selectedBrand, selectedPriceRange, sortBy])

  const getCategoryTitle = () => {
    switch (category) {
      case 'phone': return 'Điện thoại'
      case 'laptop': return 'Laptop'
      case 'tablet': return 'Tablet'
      case 'monitor': return 'Màn hình'
      case 'computer-parts': return 'Linh kiện máy tính'
      case 'appliances': return 'Điện máy'
      case 'watch': return 'Đồng hồ'
      case 'audio': return 'Âm thanh'
      case 'smart-home': return 'Smart home'
      case 'accessories': return 'Phụ kiện'
      default: return 'Tất cả sản phẩm'
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
          <a href="/" className="hover:text-red-500">Trang chủ</a>
          <span>/</span>
          <span className="text-gray-900">{getCategoryTitle()}</span>
        </nav>

        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">{getCategoryTitle()}</h1>
          <p className="text-gray-600">
            {t('noProductsFound')}: {filteredProducts.length} {t('allProducts').toLowerCase()}
          </p>
        </div>

        <div className="flex flex-col lg:flex-row gap-8">
          {/* Filters Sidebar */}
          <div className="lg:w-64">
            <div className="bg-white rounded-lg p-6 sticky top-24">
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-semibold text-gray-900">{t('filterBy')}</h3>
                <button
                  onClick={() => setShowFilters(!showFilters)}
                  className="lg:hidden p-2 hover:bg-gray-100 rounded-lg"
                >
                  <FiFilter size={20} />
                </button>
              </div>

              <div className={`space-y-6 ${showFilters ? 'block' : 'hidden lg:block'}`}>
                {/* Brand Filter */}
                <div>
                  <h4 className="font-medium text-gray-900 mb-3">{t('brand')}</h4>
                  <div className="space-y-2">
                    {brands.map((brand) => (
                      <label key={brand} className="flex items-center">
                        <input
                          type="radio"
                          name="brand"
                          value={brand}
                          checked={selectedBrand === brand}
                          onChange={(e) => setSelectedBrand(e.target.value)}
                          className="mr-2"
                        />
                        <span className="text-sm text-gray-700">{brand}</span>
                      </label>
                    ))}
                  </div>
                </div>

                {/* Price Range Filter */}
                <div>
                  <h4 className="font-medium text-gray-900 mb-3">{t('priceRange')}</h4>
                  <div className="space-y-2">
                    {priceRanges.map((range) => (
                      <label key={range.label} className="flex items-center">
                        <input
                          type="radio"
                          name="priceRange"
                          value={range.label}
                          checked={selectedPriceRange === range.label}
                          onChange={(e) => setSelectedPriceRange(e.target.value)}
                          className="mr-2"
                        />
                        <span className="text-sm text-gray-700">{range.label}</span>
                      </label>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Products Section */}
          <div className="flex-1">
            {/* Toolbar */}
            <div className="bg-white rounded-lg p-4 mb-6">
              <div className="flex flex-col sm:flex-row justify-between items-center space-y-4 sm:space-y-0">
                {/* Sort Options */}
                <div className="flex items-center space-x-4">
                  <span className="text-sm text-gray-600">{t('sortBy')}:</span>
                  <select
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value)}
                    className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
                  >
                    <option value="default">{t('sortDefault')}</option>
                    <option value="price-low">{t('sortPriceLow')}</option>
                    <option value="price-high">{t('sortPriceHigh')}</option>
                    <option value="rating">{t('sortRating')}</option>
                    <option value="newest">{t('sortNewest')}</option>
                  </select>
                </div>

                {/* View Mode */}
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => setViewMode('grid')}
                    className={`p-2 rounded-lg ${
                      viewMode === 'grid' ? 'bg-red-500 text-white' : 'bg-gray-200 text-gray-600'
                    }`}
                  >
                    <FiGrid size={18} />
                  </button>
                  <button
                    onClick={() => setViewMode('list')}
                    className={`p-2 rounded-lg ${
                      viewMode === 'list' ? 'bg-red-500 text-white' : 'bg-gray-200 text-gray-600'
                    }`}
                  >
                    <FiList size={18} />
                  </button>
                </div>
              </div>
            </div>

            {/* Products Grid */}
            <div className={
              viewMode === 'grid'
                ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6'
                : 'space-y-4'
            }>
              {filteredProducts.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>

            {/* No Products Message */}
            {filteredProducts.length === 0 && (
              <div className="text-center py-12">
                <div className="text-gray-400 mb-4">
                  <FiFilter size={64} className="mx-auto" />
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  Không tìm thấy sản phẩm
                </h3>
                <p className="text-gray-600 mb-4">
                  Hãy thử điều chỉnh bộ lọc để tìm sản phẩm phù hợp
                </p>
                <button
                  onClick={() => {
                    setSelectedBrand('Tất cả')
                    setSelectedPriceRange('Tất cả')
                    setSortBy('default')
                  }}
                  className="bg-red-500 text-white px-6 py-2 rounded-lg hover:bg-red-600 transition-colors"
                >
                  Xóa bộ lọc
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
