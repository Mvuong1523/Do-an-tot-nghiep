'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import Image from 'next/image'
import { Swiper, SwiperSlide } from 'swiper/react'
import { Autoplay, Pagination, Navigation } from 'swiper/modules'
import 'swiper/css'
import 'swiper/css/pagination'
import 'swiper/css/navigation'
import ProductCard from '@/components/product/ProductCard'
import CategoryCard from '@/components/category/CategoryCard'
import { Product } from '@/store/cartStore'
import { useTranslation } from '@/hooks/useTranslation'
import { categoryApi } from '@/lib/api'

// Mock data - sẽ được thay thế bằng API calls khi backend có đầy đủ
const featuredProducts: Product[] = [
  {
    id: 1,
    name: 'iPhone 16 Pro Max 256GB - Chính hãng VN/A',
    price: 29990000,
    originalPrice: 34990000,
    discount: 14,
    image: '/images/iphone-16-pro-max.jpg',
    rating: 4.8,
    reviews: 1250,
    badge: 'Hot'
  },
  {
    id: 2,
    name: 'Điện thoại Xiaomi POCO C71 4GB/128GB',
    price: 2490000,
    originalPrice: undefined,
    discount: 0,
    image: '/images/xiaomi-poco-c71.jpg',
    rating: 4.5,
    reviews: 890,
    badge: 'Mới'
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
    badge: 'Sale'
  },
  {
    id: 4,
    name: 'Điện thoại Xiaomi 15T 12GB/512GB',
    price: 14990000,
    originalPrice: undefined,
    discount: 0,
    image: '/images/xiaomi-15t.jpg',
    rating: 4.7,
    reviews: 234,
    badge: 'Hot'
  },
]

const defaultCategories = [
  { name: 'Điện thoại', icon: '📱', href: '/products?category=phone', count: 1250 },
  { name: 'Laptop', icon: '💻', href: '/products?category=laptop', count: 450 },
  { name: 'Tablet', icon: '📱', href: '/products?category=tablet', count: 200 },
  { name: 'Màn hình', icon: '🖥️', href: '/products?category=monitor', count: 300 },
  { name: 'Linh kiện máy tính', icon: '🔧', href: '/products?category=computer-parts', count: 800 },
  { name: 'Điện máy', icon: '🏠', href: '/products?category=appliances', count: 150 },
  { name: 'Đồng hồ', icon: '⌚', href: '/products?category=watch', count: 400 },
  { name: 'Âm thanh', icon: '🎵', href: '/products?category=audio', count: 250 },
]

const banners = [
  {
    id: 1,
    title: 'iPhone 16 Pro Max',
    subtitle: 'Mua ngay',
    image: '/images/banner-iphone.jpg',
    href: '/products/iphone-16-pro-max'
  },
  {
    id: 2,
    title: 'Điện thoại Xiaomi 15T',
    subtitle: 'Chạm Đỉnh Tuyệt Tác',
    image: '/images/banner-xiaomi-15t.jpg',
    href: '/products/xiaomi-15t'
  },
  {
    id: 3,
    title: 'iPhone Air',
    subtitle: 'iPhone mỏng nhất từng có',
    image: '/images/banner-iphone-air.jpg',
    href: '/products/iphone-air'
  },
]

export default function HomePage() {
  const { t } = useTranslation()
  const [currentTime, setCurrentTime] = useState(new Date())
  const [categories, setCategories] = useState(defaultCategories)
  const [loadingCategories, setLoadingCategories] = useState(false)

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date())
    }, 1000)

    return () => clearInterval(timer)
  }, [])

  // Load categories from API
  useEffect(() => {
    const loadCategories = async () => {
      setLoadingCategories(true)
      try {
        const response = await categoryApi.getAll()
        if (response.success && response.data && Array.isArray(response.data)) {
          // Map API categories to frontend format
          const mappedCategories = response.data.map((cat: any) => ({
            name: cat.name,
            icon: '📦', // Default icon
            href: `/products?category=${cat.id}`,
            count: cat.productCount || 0,
          }))
          if (mappedCategories.length > 0) {
            setCategories(mappedCategories)
          }
        }
      } catch (error) {
        console.error('Failed to load categories:', error)
        // Keep default categories on error
      } finally {
        setLoadingCategories(false)
      }
    }

    loadCategories()
  }, [])

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Banner */}
      <section className="relative">
        <Swiper
          modules={[Autoplay, Pagination, Navigation]}
          spaceBetween={30}
          centeredSlides={true}
          autoplay={{
            delay: 5000,
            disableOnInteraction: false,
          }}
          pagination={{
            clickable: true,
          }}
          navigation={true}
          className="hero-swiper"
        >
          {banners.map((banner) => (
            <SwiperSlide key={banner.id}>
              <div className="relative h-96 bg-gradient-to-r from-navy-800 to-navy-900 flex items-center justify-center">
                <div className="text-center text-white z-10">
                  <h1 className="text-4xl md:text-6xl font-bold mb-4">{banner.title}</h1>
                  <p className="text-xl md:text-2xl mb-6">{banner.subtitle}</p>
                  <Link
                    href={banner.href}
                    className="inline-block bg-white text-navy-500 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-colors"
                  >
                    Mua ngay
                  </Link>
                </div>
              </div>
            </SwiperSlide>
          ))}
        </Swiper>
      </section>

      {/* Categories */}
      <section className="py-12 bg-white">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-8">{t('categories')}</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-8 gap-4">
            {categories.map((category) => (
              <CategoryCard key={category.name} category={category} />
            ))}
          </div>
        </div>
      </section>

      {/* Featured Products */}
      <section className="py-12 bg-gray-50">
        <div className="container mx-auto px-4">
          <div className="flex justify-between items-center mb-8">
            <h2 className="text-3xl font-bold">{t('featuredProducts')}</h2>
            <Link href="/products" className="text-navy-500 hover:text-navy-600 font-semibold">
              {t('viewAll')} →
            </Link>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {featuredProducts.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        </div>
      </section>

      {/* Promotional Banner */}
      <section className="py-12 bg-white">
        <div className="container mx-auto px-4">
          <div className="bg-gradient-to-r from-navy-800 to-navy-900 rounded-2xl p-8 text-white text-center">
            <h3 className="text-2xl md:text-3xl font-bold mb-4">
              Đang diễn ra • 29-10
            </h3>
            <p className="text-lg mb-6">Kết thúc trong</p>
            <div className="flex justify-center space-x-4 mb-6">
              <div className="bg-white/20 rounded-lg p-4">
                <div className="text-2xl font-bold">02</div>
                <div className="text-sm">Ngày</div>
              </div>
              <div className="bg-white/20 rounded-lg p-4">
                <div className="text-2xl font-bold">14</div>
                <div className="text-sm">Giờ</div>
              </div>
              <div className="bg-white/20 rounded-lg p-4">
                <div className="text-2xl font-bold">32</div>
                <div className="text-sm">Phút</div>
              </div>
              <div className="bg-white/20 rounded-lg p-4">
                <div className="text-2xl font-bold">15</div>
                <div className="text-sm">Giây</div>
              </div>
            </div>
            <Link
              href="/promotions"
              className="inline-block bg-white text-navy-500 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-colors"
            >
              {t('viewPromotions')}
            </Link>
          </div>
        </div>
      </section>

      {/* Brand Showcase */}
      <section className="py-12 bg-gray-50">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-8">Thương hiệu nổi bật</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            <div className="bg-white rounded-lg p-6 text-center hover:shadow-lg transition-shadow">
              <div className="text-4xl mb-2">🍎</div>
              <h3 className="font-semibold">Apple chính hãng</h3>
            </div>
            <div className="bg-white rounded-lg p-6 text-center hover:shadow-lg transition-shadow">
              <div className="text-4xl mb-2">📱</div>
              <h3 className="font-semibold">Samsung đỉnh cao công nghệ</h3>
            </div>
            <div className="bg-white rounded-lg p-6 text-center hover:shadow-lg transition-shadow">
              <div className="text-4xl mb-2">⚡</div>
              <h3 className="font-semibold">Đại tiệc Xiaomi</h3>
            </div>
            <div className="bg-white rounded-lg p-6 text-center hover:shadow-lg transition-shadow">
              <div className="text-4xl mb-2">💎</div>
              <h3 className="font-semibold">Máy Cũ Giá Hời</h3>
            </div>
          </div>
        </div>
      </section>

      {/* Customer Reviews */}
      <section className="py-12 bg-white">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-8">{t('customerReviews')}</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[
              {
                name: 'NSND Trung Anh',
                role: 'Diễn viên truyền hình',
                content: 'Tôi biết Hoàng Hà qua 1 lần hợp tác quảng cáo, từ đó về sau là biết tới 1 thương hiệu giá cạnh tranh mà ngày càng phát triển.'
              },
              {
                name: 'MC Mù Tạt',
                role: 'BTV/MC VTV, Diễn viên',
                content: 'Lần đầu tới mua iPhone 13 Pro Max thấy các bạn nhân viên Hoàng Hà rất thân thiện, giá thì quá tốt rồi.'
              },
              {
                name: 'Hà Bùi',
                role: 'Phi công + Content Creator',
                content: 'Không nghĩ rằng mình và Hoàng Hà Mobile có duyên đến thế. Chúc cho Hoàng Hà Mobile luôn giữ vững phong độ.'
              }
            ].map((review, index) => (
              <div key={index} className="bg-gray-50 rounded-lg p-6">
                <div className="flex items-center mb-4">
                  <div className="w-12 h-12 bg-red-500 rounded-full flex items-center justify-center text-white font-bold">
                    {review.name.charAt(0)}
                  </div>
                  <div className="ml-4">
                    <h4 className="font-semibold">{review.name}</h4>
                    <p className="text-sm text-gray-600">{review.role}</p>
                  </div>
                </div>
                <p className="text-gray-700">&quot;{review.content}&quot;</p>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  )
}
