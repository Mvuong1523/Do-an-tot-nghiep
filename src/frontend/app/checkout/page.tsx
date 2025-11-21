'use client'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { FiShoppingCart, FiMapPin, FiCreditCard } from 'react-icons/fi'
import { cartApi, orderApi } from '@/lib/api'
import { useAuthStore } from '@/store/authStore'
import toast from 'react-hot-toast'

export default function CheckoutPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { user, isAuthenticated } = useAuthStore()
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [items, setItems] = useState<any[]>([])
  const [form, setForm] = useState({
    customerName: '',
    customerPhone: '',
    customerEmail: user?.email || '',
    province: '',
    district: '',
    ward: '',
    address: '',
    note: '',
    shippingFee: 0
  })
  const [shippingMethod, setShippingMethod] = useState<'internal' | 'ghtk'>('internal')

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }
    loadOrderData()
  }, [isAuthenticated])

  const loadOrderData = async () => {
    try {
      const type = searchParams.get('type')
      
      if (type === 'quick') {
        // Mua ngay - Lấy từ sessionStorage
        const quickBuyData = sessionStorage.getItem('quickBuyOrder')
        if (quickBuyData) {
          const data = JSON.parse(quickBuyData)
          setItems(data.items)
        } else {
          toast.error('Không tìm thấy thông tin đơn hàng')
          router.push('/')
        }
      } else {
        // Từ giỏ hàng - Lấy từ API
        const response = await cartApi.getCart()
        if (response.success && response.data?.items) {
          setItems(response.data.items.map((item: any) => ({
            productId: item.product.id,
            productName: item.product.name,
            price: item.price,
            quantity: item.quantity,
            imageUrl: item.product.imageUrl
          })))
        }
      }
    } catch (error) {
      console.error('Error loading order data:', error)
      toast.error('Lỗi khi tải thông tin đơn hàng')
    } finally {
      setLoading(false)
    }
  }

  const calculateSubtotal = () => {
    return items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
  }

  const calculateTotal = () => {
    return calculateSubtotal() + form.shippingFee
  }

  // Tự động tính phí ship khi thay đổi tỉnh
  useEffect(() => {
    if (form.province) {
      const isHanoi = form.province.toLowerCase().includes('hà nội') || 
                      form.province.toLowerCase().includes('ha noi') ||
                      form.province.toLowerCase().includes('hanoi')
      
      if (isHanoi) {
        // Nội thành Hà Nội - Miễn phí
        setShippingMethod('internal')
        setForm(prev => ({ ...prev, shippingFee: 0 }))
      } else {
        // Ngoài Hà Nội - GHTK (tạm tính 30k)
        setShippingMethod('ghtk')
        setForm(prev => ({ ...prev, shippingFee: 30000 }))
      }
    }
  }, [form.province])

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!form.customerName || !form.customerPhone || !form.customerEmail || 
        !form.province || !form.district || !form.ward || !form.address) {
      toast.error('Vui lòng điền đầy đủ thông tin')
      return
    }

    if (items.length === 0) {
      toast.error('Không có sản phẩm nào để đặt hàng')
      return
    }

    setSubmitting(true)
    try {
      const orderData = {
        customerName: form.customerName,
        customerPhone: form.customerPhone,
        customerEmail: form.customerEmail,
        province: form.province,
        district: form.district,
        ward: form.ward,
        address: form.address,
        note: form.note,
        shippingFee: form.shippingFee
      }

      const response = await orderApi.create(orderData)
      
      if (response.success) {
        toast.success('Đặt hàng thành công!')
        
        // Xóa quickBuyOrder nếu có
        sessionStorage.removeItem('quickBuyOrder')
        
        // Chuyển đến trang đơn hàng
        router.push(`/orders/${response.data.id}`)
      } else {
        toast.error(response.message || 'Đặt hàng thất bại')
      }
    } catch (error: any) {
      console.error('Order error:', error)
      toast.error(error.message || 'Lỗi khi đặt hàng')
    } finally {
      setSubmitting(false)
    }
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

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Thanh toán</h1>

        <form onSubmit={handleSubmit}>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Form */}
            <div className="lg:col-span-2 space-y-6">
              {/* Thông tin giao hàng */}
              <div className="bg-white rounded-lg shadow-sm p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
                  <FiMapPin className="mr-2" />
                  Thông tin giao hàng
                </h2>
                
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Họ và tên <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={form.customerName}
                      onChange={(e) => setForm({...form, customerName: e.target.value})}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                      required
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Số điện thoại <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="tel"
                        value={form.customerPhone}
                        onChange={(e) => setForm({...form, customerPhone: e.target.value})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Email <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="email"
                        value={form.customerEmail}
                        onChange={(e) => setForm({...form, customerEmail: e.target.value})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        required
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-3 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Tỉnh/Thành phố <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="text"
                        value={form.province}
                        onChange={(e) => setForm({...form, province: e.target.value})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="Hà Nội"
                        required
                      />
                      {form.province && (
                        <p className="text-xs mt-1">
                          {shippingMethod === 'internal' ? (
                            <span className="text-green-600">✓ Miễn phí ship nội thành HN</span>
                          ) : (
                            <span className="text-blue-600">📦 Giao qua GHTK: {formatPrice(form.shippingFee)}</span>
                          )}
                        </p>
                      )}
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Quận/Huyện <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="text"
                        value={form.district}
                        onChange={(e) => setForm({...form, district: e.target.value})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="Cầu Giấy"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Phường/Xã <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="text"
                        value={form.ward}
                        onChange={(e) => setForm({...form, ward: e.target.value})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="Dịch Vọng"
                        required
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Địa chỉ cụ thể <span className="text-red-500">*</span>
                    </label>
                    <textarea
                      value={form.address}
                      onChange={(e) => setForm({...form, address: e.target.value})}
                      rows={2}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Số nhà, tên đường..."
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Ghi chú
                    </label>
                    <textarea
                      value={form.note}
                      onChange={(e) => setForm({...form, note: e.target.value})}
                      rows={2}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Ghi chú thêm về đơn hàng..."
                    />
                  </div>
                </div>
              </div>

              {/* Phương thức thanh toán */}
              <div className="bg-white rounded-lg shadow-sm p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
                  <FiCreditCard className="mr-2" />
                  Phương thức thanh toán
                </h2>
                
                <div className="space-y-3">
                  <label className="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="COD"
                      checked={form.paymentMethod === 'COD'}
                      onChange={(e) => setForm({...form, paymentMethod: e.target.value})}
                      className="mr-3"
                    />
                    <div>
                      <p className="font-medium">Thanh toán khi nhận hàng (COD)</p>
                      <p className="text-sm text-gray-600">Thanh toán bằng tiền mặt khi nhận hàng</p>
                    </div>
                  </label>

                  <label className="flex items-center p-4 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50 opacity-50">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="BANK"
                      disabled
                      className="mr-3"
                    />
                    <div>
                      <p className="font-medium">Chuyển khoản ngân hàng</p>
                      <p className="text-sm text-gray-600">Đang phát triển...</p>
                    </div>
                  </label>
                </div>
              </div>
            </div>

            {/* Summary */}
            <div className="lg:col-span-1">
              <div className="bg-white rounded-lg shadow-sm p-6 sticky top-4">
                <h2 className="text-xl font-bold text-gray-900 mb-4">Đơn hàng</h2>
                
                {/* Items */}
                <div className="space-y-3 mb-4 max-h-60 overflow-y-auto">
                  {items.map((item, index) => (
                    <div key={index} className="flex items-center space-x-3 pb-3 border-b">
                      <div className="w-16 h-16 bg-gray-100 rounded flex-shrink-0 overflow-hidden">
                        {item.imageUrl ? (
                          <img src={item.imageUrl} alt={item.productName} className="w-full h-full object-contain" />
                        ) : (
                          <div className="w-full h-full flex items-center justify-center">
                            <FiShoppingCart className="text-gray-400" />
                          </div>
                        )}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gray-900 truncate">{item.productName}</p>
                        <p className="text-sm text-gray-600">x{item.quantity}</p>
                      </div>
                      <p className="text-sm font-medium">{formatPrice(item.price * item.quantity)}</p>
                    </div>
                  ))}
                </div>

                {/* Total */}
                <div className="space-y-3 mb-4">
                  <div className="flex justify-between text-gray-600">
                    <span>Tạm tính</span>
                    <span>{formatPrice(calculateSubtotal())}</span>
                  </div>
                  <div className="flex justify-between text-gray-600">
                    <div>
                      <p>Phí vận chuyển</p>
                      {shippingMethod === 'internal' && (
                        <p className="text-xs text-green-600">Shipper nội thành HN</p>
                      )}
                      {shippingMethod === 'ghtk' && (
                        <p className="text-xs text-blue-600">Giao Hàng Tiết Kiệm</p>
                      )}
                    </div>
                    <span className={form.shippingFee === 0 ? 'text-green-600 font-medium' : ''}>
                      {form.shippingFee === 0 ? 'Miễn phí' : formatPrice(form.shippingFee)}
                    </span>
                  </div>
                  <div className="border-t pt-3 flex justify-between text-lg font-bold">
                    <span>Tổng cộng</span>
                    <span className="text-red-600">{formatPrice(calculateTotal())}</span>
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={submitting}
                  className="w-full bg-red-600 text-white py-3 rounded-lg hover:bg-red-700 transition-colors font-semibold disabled:bg-gray-400"
                >
                  {submitting ? 'Đang xử lý...' : 'Đặt hàng'}
                </button>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  )
}
