'use client'

import Image from 'next/image'
import Link from 'next/link'
import { FiMinus, FiPlus, FiTrash2, FiShoppingBag } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useCartStore } from '@/store/cartStore'
import { useTranslation } from '@/hooks/useTranslation'

export default function CartPage() {
  const { items, updateQuantity, removeFromCart, getCartTotal } = useCartStore()
  const { t } = useTranslation()

  const handleUpdateQuantity = (id: number, newQuantity: number) => {
    if (newQuantity < 1) return
    
    updateQuantity(id, newQuantity)
    toast.success(t('updatedQuantity'))
  }

  const handleRemoveItem = (id: number) => {
    removeFromCart(id)
    toast.success(t('removedFromCart'))
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  const calculateSubtotal = () => {
    return items.reduce((total, item) => total + (item.price * item.quantity), 0)
  }

  const calculateDiscount = () => {
    return items.reduce((total, item) => {
      if (item.originalPrice) {
        return total + ((item.originalPrice - item.price) * item.quantity)
      }
      return total
    }, 0)
  }

  const calculateTotal = () => {
    return calculateSubtotal() - calculateDiscount()
  }

  if (items.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-16">
          <div className="text-center">
            <div className="text-gray-400 mb-6">
              <FiShoppingBag size={120} className="mx-auto" />
            </div>
            <h1 className="text-3xl font-bold text-gray-900 mb-4">{t('cartEmpty')}</h1>
            <p className="text-gray-600 mb-8">
              {t('cartEmptyMessage')}
            </p>
            <Link
              href="/products"
              className="bg-navy-500 text-white px-8 py-3 rounded-lg font-semibold hover:bg-navy-600 transition-colors inline-block"
            >
              {t('continueShopping')}
            </Link>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
          <Link href="/" className="hover:text-navy-500">{t('home')}</Link>
          <span>/</span>
          <span className="text-gray-900">{t('cart')}</span>
        </nav>

        <h1 className="text-3xl font-bold text-gray-900 mb-8">{t('yourCart')}</h1>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Cart Items */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-lg shadow-sm">
              <div className="p-6">
                <div className="space-y-6">
                  {items.map((item) => (
                    <div key={item.id} className="flex items-center space-x-4 p-4 border border-gray-200 rounded-lg">
                      {/* Product Image */}
                      <div className="w-20 h-20 bg-gray-100 rounded-lg overflow-hidden flex-shrink-0">
                        <Image
                          src={item.image}
                          alt={item.name}
                          width={80}
                          height={80}
                          className="w-full h-full object-cover"
                        />
                      </div>

                      {/* Product Info */}
                      <div className="flex-1 min-w-0">
                        <Link href={`/products/${item.id}`}>
                          <h3 className="font-semibold text-gray-900 hover:text-navy-500 transition-colors line-clamp-2">
                            {item.name}
                          </h3>
                        </Link>
                        <div className="mt-1 text-sm text-gray-600">
                          {item.color && <p>Màu: {item.color}</p>}
                          {item.storage && <p>Dung lượng: {item.storage}</p>}
                        </div>
                        
                        {/* Price */}
                        <div className="mt-2 flex items-center space-x-2">
                          <span className="text-lg font-bold text-navy-500">
                            {formatPrice(item.price)}
                          </span>
                          {item.originalPrice && (
                            <span className="text-sm text-gray-500 line-through">
                              {formatPrice(item.originalPrice)}
                            </span>
                          )}
                          {item.discount && item.discount > 0 && (
                            <span className="bg-navy-500 text-white px-2 py-1 text-xs font-semibold rounded">
                              -{item.discount}%
                            </span>
                          )}
                        </div>
                      </div>

                      {/* Quantity Controls */}
                      <div className="flex items-center space-x-3">
                        <button
                          onClick={() => handleUpdateQuantity(item.id, item.quantity - 1)}
                          className="w-8 h-8 bg-gray-200 rounded-lg flex items-center justify-center hover:bg-gray-300 transition-colors"
                        >
                          <FiMinus size={16} />
                        </button>
                        <span className="w-12 text-center font-semibold">{item.quantity}</span>
                        <button
                          onClick={() => handleUpdateQuantity(item.id, item.quantity + 1)}
                          className="w-8 h-8 bg-gray-200 rounded-lg flex items-center justify-center hover:bg-gray-300 transition-colors"
                        >
                          <FiPlus size={16} />
                        </button>
                      </div>

                      {/* Item Total */}
                      <div className="text-right">
                        <div className="text-lg font-bold text-gray-900">
                          {formatPrice(item.price * item.quantity)}
                        </div>
                      </div>

                      {/* Remove Button */}
                      <button
                        onClick={() => handleRemoveItem(item.id)}
                        className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                      >
                        <FiTrash2 size={18} />
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-sm sticky top-24">
              <div className="p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-6">Tóm tắt đơn hàng</h2>
                
                <div className="space-y-4">
                  <div className="flex justify-between">
                    <span className="text-gray-600">{t('subtotal')}:</span>
                    <span className="font-semibold">{formatPrice(calculateSubtotal())}</span>
                  </div>
                  
                  {calculateDiscount() > 0 && (
                    <div className="flex justify-between text-green-600">
                      <span>{t('discount')}:</span>
                      <span className="font-semibold">-{formatPrice(calculateDiscount())}</span>
                    </div>
                  )}
                  
                  <div className="flex justify-between">
                    <span className="text-gray-600">{t('shipping')}:</span>
                    <span className="font-semibold text-green-600">{t('freeShipping')}</span>
                  </div>
                  
                  <div className="border-t pt-4">
                    <div className="flex justify-between text-lg font-bold">
                      <span>{t('total')}:</span>
                      <span className="text-navy-500">{formatPrice(calculateTotal())}</span>
                    </div>
                  </div>
                </div>

                <div className="mt-6 space-y-3">
                  <Link
                    href="/checkout"
                    className="w-full bg-navy-500 text-white py-3 px-6 rounded-lg font-semibold hover:bg-navy-600 transition-colors text-center block"
                  >
                    {t('checkout')}
                  </Link>
                  <Link
                    href="/products"
                    className="w-full bg-gray-200 text-gray-700 py-3 px-6 rounded-lg font-semibold hover:bg-gray-300 transition-colors text-center block"
                  >
                    {t('continueShopping')}
                  </Link>
                </div>

                {/* Promo Code */}
                <div className="mt-6 pt-6 border-t">
                  <h3 className="font-semibold text-gray-900 mb-3">{t('promoCode')}</h3>
                  <div className="flex space-x-2">
                    <input
                      type="text"
                      placeholder={t('enterPromoCode')}
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-navy-500"
                    />
                    <button className="bg-gray-200 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-300 transition-colors">
                      {t('apply')}
                    </button>
                  </div>
                </div>

                {/* Security Info */}
                <div className="mt-6 pt-6 border-t">
                  <div className="flex items-center space-x-2 text-sm text-gray-600">
                    <div className="w-4 h-4 bg-green-500 rounded-full flex items-center justify-center">
                      <span className="text-white text-xs">✓</span>
                    </div>
                    <span>{t('securePayment')}</span>
                  </div>
                  <div className="flex items-center space-x-2 text-sm text-gray-600 mt-2">
                    <div className="w-4 h-4 bg-green-500 rounded-full flex items-center justify-center">
                      <span className="text-white text-xs">✓</span>
                    </div>
                    <span>{t('freeShippingNationwide')}</span>
                  </div>
                  <div className="flex items-center space-x-2 text-sm text-gray-600 mt-2">
                    <div className="w-4 h-4 bg-green-500 rounded-full flex items-center justify-center">
                      <span className="text-white text-xs">✓</span>
                    </div>
                    <span>{t('returnWithin7Days')}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
