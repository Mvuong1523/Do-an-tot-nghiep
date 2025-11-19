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
                        <div className="text-xs">
                          {(() => {
                            try {
                              const specs = JSON.parse(item.warehouseProduct.techSpecsJson)
                              const entries = Object.entries(specs)
                              if (entries.length === 0) return <span className="text-gray-400">-</span>
                              
                              return (
                                <div className="space-y-1">
                                  {entries.map(([key, value]: [string, any], idx: number) => (
                                    <div key={idx} className="flex items-start">
                                      <span className="text-gray-500 capitalize min-w-[100px]">{key.replace(/_/g, ' ')}:</span>
                                      <span className="text-gray-900 font-medium ml-2">{String(value)}</span>
                                    </div>
                                  ))}
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
                      {item.productDetails && item.productDetails.length > 0 ? (
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
                        <span className="text-xs text-gray-400 italic">
                          {order.status === 'CREATED' ? 'Chưa nhập' : '-'}
                        </span>
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
            <Link
              href="/warehouse/import/complete"
              className="w-full bg-red-500 text-white px-6 py-3 rounded-lg hover:bg-red-600 transition-colors flex items-center justify-center space-x-2 font-semibold"
            >
              <FiPackage />
              <span>Hoàn thiện phiếu nhập</span>
            </Link>
          </div>
        )}
      </div>
    </div>
  )
}
