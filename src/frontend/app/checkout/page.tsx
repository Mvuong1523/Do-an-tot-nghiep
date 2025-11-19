'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { FiArrowLeft, FiMapPin, FiUser, FiPhone, FiMail, FiTruck } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'

// Danh sách tỉnh/thành phố (rút gọn)
const PROVINCES = [
  'Hà Nội', 'TP Hồ Chí Minh', 'Đà Nẵng', 'Hải Phòng', 'Cần Thơ',
  'Bắc Ninh', 'Bắc Giang', 'Hải Dương', 'Hưng Yên', 'Vĩnh Phúc'
]

// Quận/Huyện Hà Nội (nội thành - miễn phí ship)
const HANOI_DISTRICTS = [
  'Ba Đình', 'Hoàn Kiếm', 'Hai Bà Trưng', 'Đống Đa',
  'Tây Hồ', 'Cầu Giấy', 'Thanh Xuân', 'Hoàng Mai',
  'Long Biên', 'Nam Từ Liêm', 'Bắc Từ Liêm', 'Hà Đông',
  'Sóc Sơn', 'Đông Anh', 'Gia Lâm' // Ngoại thành
]

export default function CheckoutPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  
  const [cart, setCart] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  
  const [formData, setFormData] = useState({
    customerName: '',
    customerPhone: '',
    customerEmail: '',
    province: 'Hà Nội',
    district: '',
    ward: '',
    address: '',
    note: ''
  })

  const [shippingFee, setShippingFee] = useState(0)
  const [isFreeShip, setIsFreeShip] = useState(false)

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    loadCart()
    loadUserInfo()
  }, [isAuthenticated, router])

  useEffect(() => {
    // Tính phí ship khi thay đổi địa chỉ
    calculateShippingFee()
  }, [formData.province, formData.district])

  const loadCart = async () => {
    try {
      // TODO: Call cart API
      // const response = await cartApi.getCart()
      
      // Mock data
      setCart({
        items: [
          {
            itemId: 1,
            productName: 'iPhone 16 Pro Max 256GB',
            price: 29990000,
            quantity: 1,
            subtotal: 29990000
          }
        ],
        subtotal: 29990000,
        totalItems: 1
      })
    } catch (error) {
      toast.error('Lỗi khi tải giỏ hàng')
      router.push('/cart')
    } finally {
      setLoading(false)
    }
  }

  const loadUserInfo = () => {
    if (user) {
      setFormData(prev => ({
        ...prev,
        customerName: user.fullName || '',
        customerEmail: user.email || '',
        customerPhone: user.phone || ''
      }))
    }
  }

  const calculateShippingFee = () => {
    const { province, district } = formData

    // Check nội thành Hà Nội
    const innerDistricts = [
      'Ba Đình', 'Hoàn Kiếm', 'Hai Bà Trưng', 'Đống Đa',
      'Tây Hồ', 'Cầu Giấy', 'Thanh Xuân', 'Hoàng Mai',
      'Long Biên', 'Nam Từ Liêm', 'Bắc Từ Liêm', 'Hà Đông'
    ]

    if (province === 'Hà Nội' && innerDistricts.includes(district)) {
      setShippingFee(0)
      setIsFreeShip(true)
    } else if (province === 'Hà Nội') {
      // Ngoại thành HN
      setShippingFee(25000)
      setIsFreeShip(false)
    } else if (province) {
      // Tỉnh khác - tính theo khu vực
      const nearbyProvinces = ['Bắc Ninh', 'Bắc Giang', 'Hải Dương', 'Hưng Yên', 'Vĩnh Phúc']
      if (nearbyProvinces.includes(province)) {
        setShippingFee(30000)
      } else {
        setShippingFee(40000)
      }
      setIsFreeShip(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    // Validate
    if (!formData.customerName || !formData.customerPhone || !formData.customerEmail) {
      toast.error('Vui lòng điền đầy đủ thông tin')
      return
    }

    if (!formData.province || !formData.district || !formData.ward || !formData.address) {
      toast.error('Vui lòng điền đầy đủ địa chỉ giao hàng')
      return
    }

    setSubmitting(true)

    try {
      // TODO: Call create order API
      // const response = await orderApi.createOrder({
      //   ...formData,
      //   shippingFee
      // })

      // Mock response
      const orderCode = 'ORD20231119001'
      
      toast.success('Đặt hàng thành công!')
      
      // Redirect to payment page
      router.push(`/payment/${orderCode}`)
    } catch (error: any) {
      toast.error(error.message || 'Lỗi khi đặt hàng')
    } finally {
      setSubmitting(false)
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
    router.push('/cart')
    return null
  }

  const total = cart.subtotal + shippingFee

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-6">
          <Link href="/cart" className="inline-flex items-center text-gray-600 hover:text-red-500 mb-4">
            <FiArrowLeft className="mr-2" />
            Quay lại giỏ hàng
          </Link>
          <h1 className="text-3xl font-bold">Thanh toán</h1>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Form */}
            <div className="lg:col-span-2 space-y-6">
              {/* Thông tin người nhận */}
              <div className="bg-white rounded-lg shadow-sm p-6">
                <h2 className="text-xl font-bold mb-4 flex items-center">
                  <FiUser className="mr-2" />
                  Thông tin người nhận
                </h2>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium mb-2">
                      Họ và tên <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={formData.customerName}
                      onChange={(e) => setFormData({ ...formData, customerName: e.target.value })}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-2">
                      Số điện thoại <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="tel"
                      value={formData.customerPhone}
                      onChange={(e) => setFormData({ ...formData, customerPhone: e.target.value })}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      required
                    />
                  </div>

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium mb-2">
                      Email <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="email"
                      value={formData.customerEmail}
                      onChange={(e) => setFormData({ ...formData, customerEmail: e.target.value })}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      required
                    />
                  </div>
                </div>
              </div>

              {/* Địa chỉ giao hàng */}
              <div className="bg-white rounded-lg shadow-sm p-6">
                <h2 className="text-xl font-bold mb-4 flex items-center">
                  <FiMapPin className="mr-2" />
                  Địa chỉ giao hàng
                </h2>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label className="block text-sm font-medium mb-2">
                      Tỉnh/Thành phố <span className="text-red-500">*</span>
                    </label>
                    <select
                      value={formData.province}
                      onChange={(e) => setFormData({ ...formData, province: e.target.value, district: '' })}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      required
                    >
                      <option value="">Chọn tỉnh/thành</option>
                      {PROVINCES.map(p => (
                        <option key={p} value={p}>{p}</option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-2">
                      Quận/Huyện <span className="text-red-500">*</span>
                    </label>
                    <select
                      value={formData.district}
                      onChange={(e) => setFormData({ ...formData, district: e.target.value })}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      required
                      disabled={!formData.province}
                    >
                      <option value="">Chọn quận/huyện</option>
                      {formData.province === 'Hà Nội' && HANOI_DISTRICTS.map(d => (
                        <option key={d} value={d}>{d}</option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-2">
                      Phường/Xã <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={formData.ward}
                      onChange={(e) => setFormData({ ...formData, ward: e.target.value })}
                      placeholder="Nhập phường/xã"
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      required
                    />
                  </div>

                  <div className="md:col-span-3">
                    <label className="block text-sm font-medium mb-2">
                      Địa chỉ cụ thể <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={formData.address}
                      onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                      placeholder="Số nhà, tên đường..."
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      required
                    />
                  </div>

                  <div className="md:col-span-3">
                    <label className="block text-sm font-medium mb-2">
                      Ghi chú (tùy chọn)
                    </label>
                    <textarea
                      value={formData.note}
                      onChange={(e) => setFormData({ ...formData, note: e.target.value })}
                      placeholder="Ghi chú về đơn hàng, ví dụ: thời gian hay địa điểm giao hàng chi tiết hơn"
                      rows={3}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                    />
                  </div>
                </div>

                {/* Shipping info */}
                {formData.district && (
                  <div className="mt-4 p-4 bg-blue-50 rounded-lg">
                    <div className="flex items-start">
                      <FiTruck className="text-blue-600 mt-1 mr-3" size={20} />
                      <div>
                        <div className="font-medium text-blue-900">
                          {isFreeShip ? 'Miễn phí vận chuyển' : `Phí vận chuyển: ${formatPrice(shippingFee)}`}
                        </div>
                        <div className="text-sm text-blue-700 mt-1">
                          {isFreeShip 
                            ? 'Giao hàng bởi shipper riêng (1-2 ngày)'
                            : 'Giao hàng bởi Giao Hàng Tiết Kiệm (3-5 ngày)'
                          }
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Order Summary */}
            <div className="lg:col-span-1">
              <div className="bg-white rounded-lg shadow-sm p-6 sticky top-4">
                <h2 className="text-xl font-bold mb-4">Đơn hàng ({cart.totalItems} sản phẩm)</h2>

                {/* Items */}
                <div className="space-y-3 mb-4 max-h-60 overflow-y-auto">
                  {cart.items.map((item: any) => (
                    <div key={item.itemId} className="flex justify-between text-sm">
                      <div className="flex-1">
                        <div className="font-medium line-clamp-2">{item.productName}</div>
                        <div className="text-gray-500">SL: {item.quantity}</div>
                      </div>
                      <div className="font-medium ml-2">{formatPrice(item.subtotal)}</div>
                    </div>
                  ))}
                </div>

                <div className="border-t pt-4 space-y-3">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Tạm tính</span>
                    <span className="font-medium">{formatPrice(cart.subtotal)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Phí vận chuyển</span>
                    <span className="font-medium">
                      {isFreeShip ? (
                        <span className="text-green-600">Miễn phí</span>
                      ) : (
                        formatPrice(shippingFee)
                      )}
                    </span>
                  </div>
                </div>

                <div className="border-t pt-4 mt-4">
                  <div className="flex justify-between items-center mb-6">
                    <span className="text-lg font-bold">Tổng cộng</span>
                    <span className="text-2xl font-bold text-red-600">
                      {formatPrice(total)}
                    </span>
                  </div>

                  <button
                    type="submit"
                    disabled={submitting}
                    className="w-full bg-red-500 text-white py-3 rounded-lg hover:bg-red-600 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {submitting ? 'Đang xử lý...' : 'Đặt hàng'}
                  </button>

                  <div className="mt-4 text-xs text-gray-600 space-y-1">
                    <p>✓ Bảo mật thông tin thanh toán</p>
                    <p>✓ Kiểm tra hàng trước khi thanh toán</p>
                    <p>✓ Hoàn tiền 100% nếu có vấn đề</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  )
}
