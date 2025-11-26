'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter, useParams } from 'next/navigation'
import { FiArrowLeft, FiPackage, FiCalendar, FiUser, FiFileText } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import { inventoryApi } from '@/lib/api'

export default function ImportDetailPage() {
  const router = useRouter()
  const params = useParams()
  const { user, isAuthenticated } = useAuthStore()
  const [order, setOrder] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [isCompleting, setIsCompleting] = useState(false)
  const [serialInputs, setSerialInputs] = useState<Record<number, string[]>>({})

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    if (user?.role !== 'WAREHOUSE' && user?.role !== 'ADMIN') {
      toast.error('Chỉ nhân viên kho mới có quyền truy cập')
      router.push('/')
      return
    }

    loadOrderDetail()
  }, [isAuthenticated, user, router, params.id])

  const loadOrderDetail = async () => {
    try {
      const response = await inventoryApi.getPurchaseOrderDetail(Number(params.id))
      console.log('Purchase Order Detail Response:', response)
      if (response.success) {
        console.log('Order data:', response.data)
        console.log('Order items:', response.data.items)
        setOrder(response.data)
        
        // Initialize serial inputs for each item
        const initialSerials: Record<number, string[]> = {}
        response.data.items?.forEach((item: any) => {
          initialSerials[item.id] = Array(item.quantity).fill('')
        })
        setSerialInputs(initialSerials)
      } else {
        toast.error('Không tìm thấy phiếu nhập')
        router.push('/warehouse/import/list')
      }
    } catch (error) {
      console.error('Error loading order detail:', error)
      toast.error('Lỗi khi tải chi tiết phiếu')
      router.push('/warehouse/import/list')
    } finally {
      setLoading(false)
    }
  }

  const handleSerialChange = (itemId: number, index: number, value: string) => {
    setSerialInputs(prev => ({
      ...prev,
      [itemId]: prev[itemId].map((s, i) => i === index ? value : s)
    }))
  }

  const handleCompleteImport = async () => {
    try {
      // Validate all serials are filled
      for (const item of order.items) {
        const serials = serialInputs[item.id] || []
        if (serials.some(s => !s.trim())) {
          toast.error(`Vui lòng nhập đầy đủ serial cho sản phẩm: ${item.warehouseProduct?.internalName}`)
          return
        }
        
        // Check for duplicates within item
        const uniqueSerials = new Set(serials)
        if (uniqueSerials.size !== serials.length) {
          toast.error(`Serial bị trùng trong sản phẩm: ${item.warehouseProduct?.internalName}`)
          return
        }
      }

      setIsCompleting(true)

      // Prepare data - Backend expects: { productSku: string, serialNumbers: string[] }[]
      const serialsByProduct = order.items.map((item: any) => {
        const serials = serialInputs[item.id] || []
        console.log('Item:', item)
        console.log('SKU:', item.sku)
        console.log('Serials:', serials)
        return {
          productSku: item.sku,
          serialNumbers: serials.map(s => s.trim())
        }
      })

      console.log('Request data:', {
        poId: order.id,
        serials: serialsByProduct
      })

      const response = await inventoryApi.completePurchaseOrder({
        poId: order.id,
        serials: serialsByProduct
      })

      if (response.success) {
        toast.success('Hoàn thiện phiếu nhập thành công!')
        loadOrderDetail() // Reload to show updated data
      } else {
        toast.error(response.message || 'Có lỗi xảy ra')
      }
    } catch (error: any) {
      console.error('Error completing import:', error)
      toast.error(error.response?.data?.message || 'Lỗi khi hoàn thiện phiếu nhập')
    } finally {
      setIsCompleting(false)
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getStatusBadge = (status: string) => {
    const statusMap: Record<string, { label: string; className: string }> = {
      CREATED: { label: 'Chờ nhập', className: 'bg-yellow-100 text-yellow-800' },
      RECEIVED: { label: 'Đã nhập', className: 'bg-green-100 text-green-800' },
      COMPLETED: { label: 'Hoàn thành', className: 'bg-blue-100 text-blue-800' },
      CANCELLED: { label: 'Đã hủy', className: 'bg-red-100 text-red-800' }
    }
    const config = statusMap[status] || { label: status, className: 'bg-gray-100 text-gray-800' }
    return (
      <span className={`px-3 py-1 text-sm font-semibold rounded-full ${config.className}`}>
        {config.label}
      </span>
    )
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  if (!order) {
    return null
  }

  return (
    <div className="p-6">
      <div className="mb-6">
        <Link
          href="/warehouse/import/list"
          className="inline-flex items-center space-x-2 text-gray-600 hover:text-gray-900 mb-4"
        >
          <FiArrowLeft />
          <span>Quay lại danh sách</span>
        </Link>
        
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Chi tiết phiếu nhập</h1>
            <p className="text-gray-600 mt-1">Mã phiếu: {order.poCode}</p>
          </div>
          {getStatusBadge(order.status)}
        </div>
      </div>

      <div className="space-y-6">
        {/* Thông tin chung */}
        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center space-x-2">
            <FiFileText className="text-red-500" />
            <span>Thông tin chung</span>
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="flex items-start space-x-3">
              <FiCalendar className="text-gray-400 mt-1" />
              <div>
                <p className="text-sm text-gray-600">Ngày tạo</p>
                <p className="font-medium text-gray-900">{formatDate(order.orderDate)}</p>
              </div>
            </div>
            <div className="flex items-start space-x-3">
              <FiUser className="text-gray-400 mt-1" />
              <div>
                <p className="text-sm text-gray-600">Người tạo</p>
                <p className="font-medium text-gray-900">{order.createdBy || 'N/A'}</p>
              </div>
            </div>
            {order.receivedDate && (
              <div className="flex items-start space-x-3">
                <FiCalendar className="text-gray-400 mt-1" />
                <div>
                  <p className="text-sm text-gray-600">Ngày nhập kho</p>
                  <p className="font-medium text-gray-900">{formatDate(order.receivedDate)}</p>
                </div>
              </div>
            )}
          </div>
          {order.note && (
            <div className="mt-4 pt-4 border-t border-gray-200">
              <p className="text-sm text-gray-600 mb-1">Ghi chú</p>
              <p className="font-medium text-gray-900">{order.note}</p>
            </div>
          )}
        </div>

        {/* Thông tin nhà cung cấp */}
        {order.supplier && (
          <div className="bg-white rounded-lg shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Thông tin nhà cung cấp</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              <div>
                <p className="text-sm text-gray-600">Tên công ty</p>
                <p className="font-medium text-gray-900">{order.supplier.name}</p>
              </div>
              {order.supplier.taxCode && (
                <div>
                  <p className="text-sm text-gray-600">Mã số thuế</p>
                  <p className="font-medium text-gray-900">{order.supplier.taxCode}</p>
                </div>
              )}
              {order.supplier.contactPerson && (
                <div>
                  <p className="text-sm text-gray-600">Người liên hệ</p>
                  <p className="font-medium text-gray-900">{order.supplier.contactPerson}</p>
                </div>
              )}
              {order.supplier.phone && (
                <div>
                  <p className="text-sm text-gray-600">Điện thoại</p>
                  <p className="font-medium text-gray-900">{order.supplier.phone}</p>
                </div>
              )}
              {order.supplier.email && (
                <div>
                  <p className="text-sm text-gray-600">Email</p>
                  <p className="font-medium text-gray-900">{order.supplier.email}</p>
                </div>
              )}
              {order.supplier.address && (
                <div>
                  <p className="text-sm text-gray-600">Địa chỉ</p>
                  <p className="font-medium text-gray-900">{order.supplier.address}</p>
                </div>
              )}
              {order.supplier.bankAccount && (
                <div>
                  <p className="text-sm text-gray-600">Tài khoản ngân hàng</p>
                  <p className="font-medium text-gray-900">{order.supplier.bankAccount}</p>
                </div>
              )}
              {order.supplier.paymentTerm && (
                <div>
                  <p className="text-sm text-gray-600">Điều khoản thanh toán</p>
                  <p className="font-medium text-gray-900">{order.supplier.paymentTerm}</p>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Danh sách sản phẩm - Bảng chuyên nghiệp */}
        <div className="bg-white rounded-lg shadow-sm overflow-hidden">
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">Danh sách sản phẩm</h2>
            {order.status === 'CREATED' && (
              <p className="text-sm text-amber-600 mt-2">
                ⚠️ Vui lòng nhập serial number cho từng sản phẩm ở cột Serial Numbers
              </p>
            )}
          </div>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">STT</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">SKU</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Tên sản phẩm</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Thông số kỹ thuật</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Serial Numbers</th>
                  <th className="px-6 py-4 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">Bảo hành</th>
                  <th className="px-6 py-4 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">Số lượng</th>
                  <th className="px-6 py-4 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">Đơn giá</th>
                  <th className="px-6 py-4 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">Thành tiền</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {order.items?.map((item: any, index: number) => (
                  <tr key={item.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {index + 1}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="text-sm font-mono font-semibold text-gray-900">{item.sku}</span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm font-medium text-gray-900">
                        {item.warehouseProduct?.internalName || '-'}
                      </div>
                      {item.warehouseProduct?.description && (
                        <div className="text-xs text-gray-500 mt-1">{item.warehouseProduct.description}</div>
                      )}
                      {item.note && (
                        <div className="text-xs text-blue-600 mt-1 italic">Ghi chú: {item.note}</div>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      {item.warehouseProduct?.techSpecsJson && item.warehouseProduct.techSpecsJson !== '{}' ? (
                        <div className="text-xs min-w-[300px] max-w-md">
                          {(() => {
                            try {
                              const specs = JSON.parse(item.warehouseProduct.techSpecsJson)
                              const entries = Object.entries(specs)
                              if (entries.length === 0) return <span className="text-gray-400">-</span>
                              
                              return (
                                <div className="bg-gray-50 rounded p-2 max-h-40 overflow-y-auto">
                                  <table className="w-full text-xs">
                                    <tbody>
                                      {entries.map(([key, value]: [string, any], idx: number) => (
                                        <tr key={idx} className="border-b border-gray-200 last:border-0">
                                          <td className="py-1.5 pr-3 text-gray-600 font-medium capitalize whitespace-nowrap align-top">
                                            {key.replace(/_/g, ' ')}
                                          </td>
                                          <td className="py-1.5 text-gray-900">
                                            {String(value)}
                                          </td>
                                        </tr>
                                      ))}
                                    </tbody>
                                  </table>
                                </div>
                              )
                            } catch {
                              return <span className="text-gray-400">-</span>
                            }
                          })()}
                        </div>
                      ) : (
                        <span className="text-gray-400 text-sm">-</span>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      {order.status === 'CREATED' ? (
                        <div className="space-y-2 min-w-[200px]">
                          {Array.from({ length: item.quantity }).map((_, idx) => (
                            <input
                              key={idx}
                              type="text"
                              value={serialInputs[item.id]?.[idx] || ''}
                              onChange={(e) => handleSerialChange(item.id, idx, e.target.value)}
                              placeholder={`Serial #${idx + 1}`}
                              className="w-full px-2 py-1 text-xs border border-gray-300 rounded focus:ring-2 focus:ring-red-500 focus:border-transparent font-mono"
                            />
                          ))}
                        </div>
                      ) : (
                        item.productDetails && item.productDetails.length > 0 ? (
                          <div className="space-y-1 max-h-40 overflow-y-auto">
                            {item.productDetails.map((detail: any, idx: number) => (
                              <div key={detail.id} className="flex items-center space-x-2">
                                <span className="text-xs text-gray-400">{idx + 1}.</span>
                                <span className="text-xs font-mono font-semibold text-blue-600">{detail.serialNumber}</span>
                                <span className={`px-1.5 py-0.5 text-xs rounded ${
                                  detail.status === 'IN_STOCK' ? 'bg-green-100 text-green-700' :
                                  detail.status === 'SOLD' ? 'bg-blue-100 text-blue-700' :
                                  detail.status === 'DAMAGED' ? 'bg-red-100 text-red-700' :
                                  'bg-gray-100 text-gray-700'
                                }`}>
                                  {detail.status === 'IN_STOCK' ? 'Kho' :
                                   detail.status === 'SOLD' ? 'Bán' :
                                   detail.status === 'DAMAGED' ? 'Hỏng' :
                                   detail.status}
                                </span>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <span className="text-xs text-gray-400 italic">Chưa nhập</span>
                        )
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-center text-sm text-gray-900">
                      {item.warrantyMonths ? `${item.warrantyMonths} tháng` : '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-semibold text-gray-900">
                      {item.quantity || 0}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-900">
                      {formatPrice(item.unitCost || 0)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-bold text-red-600">
                      {formatPrice((item.quantity || 0) * (item.unitCost || 0))}
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot className="bg-gray-50">
                <tr>
                  <td colSpan={8} className="px-6 py-4 text-right text-sm font-semibold text-gray-900">
                    Tổng cộng:
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-lg font-bold text-red-600">
                    {formatPrice(
                      order.items?.reduce((sum: number, item: any) => sum + ((item.quantity || 0) * (item.unitCost || 0)), 0) || 0
                    )}
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>

        {/* Thao tác */}
        {order.status === 'CREATED' && (
          <div className="bg-white rounded-lg shadow-sm p-6">
            <button
              onClick={handleCompleteImport}
              disabled={isCompleting}
              className="w-full bg-red-500 text-white px-6 py-3 rounded-lg hover:bg-red-600 transition-colors flex items-center justify-center space-x-2 font-semibold disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {isCompleting ? (
                <>
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                  <span>Đang xử lý...</span>
                </>
              ) : (
                <>
                  <FiPackage />
                  <span>Hoàn thiện phiếu nhập hàng</span>
                </>
              )}
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
