'use client'

import { useState } from 'react'
import Link from 'next/link'
import { FiUser, FiMail, FiPhone, FiMapPin, FiCreditCard, FiTruck } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useTranslation } from '@/hooks/useTranslation'

export default function CheckoutPage() {
  const { t } = useTranslation()
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    district: '',
    ward: '',
    paymentMethod: 'cod',
    notes: ''
  })

  const [isProcessing, setIsProcessing] = useState(false)
  const [errors, setErrors] = useState<{[key: string]: string}>({})

  const validateForm = () => {
    const newErrors: {[key: string]: string} = {}
    
    // Validate required fields
    if (!formData.fullName.trim()) {
      newErrors.fullName = t('fullNameRequired')
    }
    
    if (!formData.email.trim()) {
      newErrors.email = t('emailRequired')
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = t('emailInvalid')
    }
    
    if (!formData.phone.trim()) {
      newErrors.phone = t('phoneRequired')
    } else if (!/^[0-9]{10,11}$/.test(formData.phone.replace(/\s/g, ''))) {
      newErrors.phone = t('phoneInvalid')
    }
    
    if (!formData.address.trim()) {
      newErrors.address = t('addressRequired')
    }
    
    if (!formData.city) {
      newErrors.city = t('cityRequired')
    }
    
    if (!formData.district) {
      newErrors.district = t('districtRequired')
    }
    
    if (!formData.ward) {
      newErrors.ward = t('wardRequired')
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }))
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    // Validate form before submitting
    if (!validateForm()) {
      toast.error(t('formValidationError'))
      setIsProcessing(false)
      return
    }
    
    setIsProcessing(true)
    
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    toast.success(t('orderSuccess'))
    setIsProcessing(false)
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  // Mock order data
  const orderItems = [
    {
      id: 1,
      name: 'iPhone 16 Pro Max 256GB - Chính hãng VN/A',
      price: 29990000,
      quantity: 1,
      image: '/images/iphone-16-pro-max.jpg'
    },
    {
      id: 2,
      name: 'Điện thoại Xiaomi POCO C71 4GB/128GB',
      price: 2490000,
      quantity: 2,
      image: '/images/xiaomi-poco-c71.jpg'
    }
  ]

  const subtotal = orderItems.reduce((total, item) => total + (item.price * item.quantity), 0)
  const shipping = 0 // Free shipping
  const total = subtotal + shipping

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
          <Link href="/" className="hover:text-red-500">{t('home')}</Link>
          <span>/</span>
          <Link href="/cart" className="hover:text-red-500">{t('cart')}</Link>
          <span>/</span>
          <span className="text-gray-900">{t('checkoutTitle')}</span>
        </nav>

        <h1 className="text-3xl font-bold text-gray-900 mb-8">{t('checkoutTitle')}</h1>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Checkout Form */}
          <div className="lg:col-span-2">
            <form onSubmit={handleSubmit} className="space-y-8">
              {/* Contact Information */}
              <div className="bg-white rounded-lg shadow-sm p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-6 flex items-center">
                  <FiUser className="mr-2" />
                  {t('shippingInfo')}
                </h2>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      {t('fullName')} *
                    </label>
                    <input
                      type="text"
                      name="fullName"
                      value={formData.fullName}
                      onChange={handleInputChange}
                      required
                      className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 ${
                        errors.fullName ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-red-500'
                      }`}
                      placeholder={t('enterFullName')}
                    />
                    {errors.fullName && (
                      <p className="text-red-500 text-sm mt-1">{errors.fullName}</p>
                    )}
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      {t('email')} *
                    </label>
                    <input
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      required
                      className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 ${
                        errors.email ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-red-500'
                      }`}
                      placeholder={t('enterEmail')}
                    />
                    {errors.email && (
                      <p className="text-red-500 text-sm mt-1">{errors.email}</p>
                    )}
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      {t('phone')} *
                    </label>
                    <input
                      type="tel"
                      name="phone"
                      value={formData.phone}
                      onChange={handleInputChange}
                      required
                      className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 ${
                        errors.phone ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-red-500'
                      }`}
                      placeholder={t('enterPhone')}
                    />
                    {errors.phone && (
                      <p className="text-red-500 text-sm mt-1">{errors.phone}</p>
                    )}
                  </div>
                </div>
              </div>

              {/* Shipping Address */}
              <div className="bg-white rounded-lg shadow-sm p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-6 flex items-center">
                  <FiMapPin className="mr-2" />
                  {t('shippingAddress')}
                </h2>
                
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      {t('address')} *
                    </label>
                    <input
                      type="text"
                      name="address"
                      value={formData.address}
                      onChange={handleInputChange}
                      required
                      className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 ${
                        errors.address ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-red-500'
                      }`}
                      placeholder={t('enterAddress')}
                    />
                    {errors.address && (
                      <p className="text-red-500 text-sm mt-1">{errors.address}</p>
                    )}
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        {t('city')} *
                      </label>
                      <select
                        name="city"
                        value={formData.city}
                        onChange={handleInputChange}
                        required
                        className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 ${
                          errors.city ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-red-500'
                        }`}
                      >
                        <option value="">{t('selectCity')}</option>
                        <option value="hanoi">Hà Nội</option>
                        <option value="hcm">TP. Hồ Chí Minh</option>
                        <option value="danang">Đà Nẵng</option>
                      </select>
                      {errors.city && (
                        <p className="text-red-500 text-sm mt-1">{errors.city}</p>
                      )}
                    </div>
                    
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        {t('district')} *
                      </label>
                      <select
                        name="district"
                        value={formData.district}
                        onChange={handleInputChange}
                        required
                        className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 ${
                          errors.district ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-red-500'
                        }`}
                      >
                        <option value="">{t('selectDistrict')}</option>
                        <option value="quan1">Quận 1</option>
                        <option value="quan2">Quận 2</option>
                        <option value="quan3">Quận 3</option>
                      </select>
                      {errors.district && (
                        <p className="text-red-500 text-sm mt-1">{errors.district}</p>
                      )}
                    </div>
                    
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        {t('ward')} *
                      </label>
                      <select
                        name="ward"
                        value={formData.ward}
                        onChange={handleInputChange}
                        required
                        className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 ${
                          errors.ward ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-red-500'
                        }`}
                      >
                        <option value="">{t('selectWard')}</option>
                        <option value="phuong1">Phường 1</option>
                        <option value="phuong2">Phường 2</option>
                        <option value="phuong3">Phường 3</option>
                      </select>
                      {errors.ward && (
                        <p className="text-red-500 text-sm mt-1">{errors.ward}</p>
                      )}
                    </div>
                  </div>
                </div>
              </div>

              {/* Payment Method */}
              <div className="bg-white rounded-lg shadow-sm p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-6 flex items-center">
                  <FiCreditCard className="mr-2" />
                  {t('paymentMethod')}
                </h2>
                
                <div className="space-y-4">
                  <label className="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="cod"
                      checked={formData.paymentMethod === 'cod'}
                      onChange={handleInputChange}
                      className="mr-3"
                    />
                    <div className="flex items-center">
                      <FiTruck className="mr-3 text-gray-600" size={20} />
                      <div>
                        <div className="font-semibold">{t('cod')}</div>
                        <div className="text-sm text-gray-600">{t('codDescription')}</div>
                      </div>
                    </div>
                  </label>
                  
                  <label className="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="banking"
                      checked={formData.paymentMethod === 'banking'}
                      onChange={handleInputChange}
                      className="mr-3"
                    />
                    <div className="flex items-center">
                      <FiCreditCard className="mr-3 text-gray-600" size={20} />
                      <div>
                        <div className="font-semibold">{t('bankTransfer')}</div>
                        <div className="text-sm text-gray-600">{t('bankTransferDescription')}</div>
                      </div>
                    </div>
                  </label>
                </div>
              </div>

              {/* Order Notes */}
              <div className="bg-white rounded-lg shadow-sm p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-6">{t('orderNotes')}</h2>
                <textarea
                  name="notes"
                  value={formData.notes}
                  onChange={handleInputChange}
                  rows={4}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                  placeholder={t('enterNotes')}
                />
              </div>
            </form>
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-sm sticky top-24">
              <div className="p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-6">{t('orderSummary')}</h2>
                
                {/* Order Items */}
                <div className="space-y-4 mb-6">
                  {orderItems.map((item) => (
                    <div key={item.id} className="flex items-center space-x-3">
                      <div className="w-16 h-16 bg-gray-100 rounded-lg overflow-hidden flex-shrink-0">
                        <img
                          src={item.image}
                          alt={item.name}
                          className="w-full h-full object-cover"
                        />
                      </div>
                      <div className="flex-1 min-w-0">
                        <h3 className="font-semibold text-gray-900 text-sm line-clamp-2">
                          {item.name}
                        </h3>
                        <div className="text-sm text-gray-600">
                          {t('quantity')}: {item.quantity}
                        </div>
                        <div className="font-semibold text-red-500">
                          {formatPrice(item.price * item.quantity)}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Order Total */}
                <div className="space-y-3 border-t pt-4">
                  <div className="flex justify-between">
                    <span className="text-gray-600">{t('subtotal')}:</span>
                    <span className="font-semibold">{formatPrice(subtotal)}</span>
                  </div>
                  
                  <div className="flex justify-between">
                    <span className="text-gray-600">{t('shipping')}:</span>
                    <span className="font-semibold text-green-600">{t('freeShipping')}</span>
                  </div>
                  
                  <div className="border-t pt-3">
                    <div className="flex justify-between text-lg font-bold">
                      <span>{t('total')}:</span>
                      <span className="text-red-500">{formatPrice(total)}</span>
                    </div>
                  </div>
                </div>

                {/* Place Order Button */}
                <button
                  onClick={handleSubmit}
                  disabled={isProcessing}
                  className="w-full mt-6 bg-red-500 text-white py-3 px-6 rounded-lg font-semibold hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isProcessing ? t('processing') : t('placeOrder')}
                </button>

                {/* Security Info */}
                <div className="mt-6 pt-6 border-t">
                  <div className="flex items-center space-x-2 text-sm text-gray-600 mb-2">
                    <div className="w-4 h-4 bg-green-500 rounded-full flex items-center justify-center">
                      <span className="text-white text-xs">✓</span>
                    </div>
                    <span>{t('securePayment')}</span>
                  </div>
                  <div className="flex items-center space-x-2 text-sm text-gray-600 mb-2">
                    <div className="w-4 h-4 bg-green-500 rounded-full flex items-center justify-center">
                      <span className="text-white text-xs">✓</span>
                    </div>
                    <span>{t('freeShippingNationwide')}</span>
                  </div>
                  <div className="flex items-center space-x-2 text-sm text-gray-600">
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
