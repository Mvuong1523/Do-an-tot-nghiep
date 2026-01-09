'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter, useSearchParams } from 'next/navigation'
import Link from 'next/link'
import { FiArrowLeft, FiPackage, FiCheck, FiX, FiEdit } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { inventoryApi } from '@/lib/api'

export default function TransactionDetailPage() {
  const params = useParams()
  const router = useRouter()
  const searchParams = useSearchParams()
  const [transaction, setTransaction] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [showSerialModal, setShowSerialModal] = useState(false)
  const [serialInputs, setSerialInputs] = useState<Record<string, string[]>>({})

  useEffect(() => {
    loadTransactionDetail()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.id, searchParams])

  const loadTransactionDetail = async () => {
    try {
      setLoading(true)
      const idParam = params.id as string
      const typeParam = searchParams.get('type')
      
      const numericId = parseInt(idParam)
      const isPO = typeParam === 'IMPORT'
      
      if (isNaN(numericId)) {
        throw new Error('ID không hợp lệ')
      }
      
      const response = isPO
        ? await inventoryApi.getPurchaseOrderDetail(numericId)
        : await inventoryApi.getExportOrderDetail(numericId)
      
      console.log('API Response:', response.data)
      console.log('Transaction ID:', response.data?.id)
      
      setTransaction({ ...response.data, type: isPO ? 'IMPORT' : 'EXPORT' })
      
      // Initialize serial inputs for purchase orders
      if (isPO && response.data.status === 'CREATED') {
        const inputs: Record<string, string[]> = {}
        response.data.items?.forEach((item: any) => {
          inputs[item.sku] = Array(item.quantity).fill('')
        })
        setSerialInputs(inputs)
      }
    } catch (error: any) {
      console.error('Error loading transaction:', error)
      toast.error(error.message || 'Lỗi khi tải chi tiết phiếu')
      router.push('/admin/inventory')
    } finally {
      setLoading(false)
    }
  }

  const handleCompleteOrder = async () => {
    if (!transaction) return

    // Validate all serials are filled
    const allSerials: any[] = []
    const allSerialNumbers: string[] = []
    
    for (const item of transaction.items) {
      const serials = serialInputs[item.sku] || []
      
      // Check empty serials
      if (serials.some(s => !s.trim())) {
        toast.error(`Vui lòng nhập đủ serial cho sản phẩm ${item.sku}`)
        return
      }
      
      // Check duplicate serials within this order
      for (const serial of serials) {
        if (allSerialNumbers.includes(serial.trim())) {
          toast.error(`Serial "${serial}" bị trùng lặp trong đơn hàng này!`)
          return
        }
        allSerialNumbers.push(serial.trim())
      }
      
      allSerials.push({
        productSku: item.sku,
        serialNumbers: serials.map(s => s.trim())
      })
    }

    try {
      // Validate transaction.id
      if (!transaction.id) {
        toast.error('Không tìm thấy ID phiếu nhập')
        console.error('Transaction object:', transaction)
        return
      }

      const requestData = {
        poId: Number(transaction.id), // Ensure it's a number
        serials: allSerials,
        receivedDate: new Date().toISOString()
      }

      console.log('Sending complete request:', requestData)
      
      await inventoryApi.completePurchaseOrder(requestData)
      toast.success('Hoàn tất nhập hàng thành công!')
      loadTransactionDetail()
    } catch (error: any) {
      console.error('Error completing order:', error)
      toast.error(error.message || 'Lỗi khi hoàn tất đơn hàng')
    }
  }

  const handleCancelOrder = async () => {
    if (!confirm('Bạn có chắc muốn hủy phiếu này?')) return

    try {
      await inventoryApi.cancelTransaction(transaction.id, transaction.type)
      toast.success('Đã hủy phiếu thành công')
      loadTransactionDetail()
    } catch (error: any) {
      toast.error(error.message || 'Lỗi khi hủy phiếu')
    }
  }

  const updateSerial = (sku: string, index: number, value: string) => {
    setSerialInputs(prev => ({
      ...prev,
      [sku]: prev[sku].map((s, i) => i === index ? value : s)
    }))
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('vi-VN')
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  const getStatusBadge = (status: string) => {
    const statusMap: Record<string, { label: string; className: string }> = {
      CREATED: { label: 'Chờ xử lý', className: 'bg-yellow-100 text-yellow-800' },
      RECEIVED: { label: 'Đã nhập', className: 'bg-green-100 text-green-800' },
      COMPLETED: { label: 'Hoàn thành', className: 'bg-blue-100 text-blue-800' },
      CANCELLED: { label: 'Đã hủy', className: 'bg-red-100 text-red-800' }
    }
    const config = statusMap[status] || { label: status, className: 'bg-gray-100 text-gray-800' }
    return (
      <span className={`px-3 py-1 text-sm font-semibold rounded ${config.className}`}>
        {config.label}
      </span>
    )
  }

  const getProductStatusBadge = (status: string) => {
    const statusMap: Record<string, { label: string; className: string }> = {
      IN_STOCK: { label: 'Trong kho', className: 'bg-green-100 text-green-800' },
      SOLD: { label: 'Đã bán', className: 'bg-blue-100 text-blue-800' },
      RESERVED: { label: 'Đã đặt', className: 'bg-yellow-100 text-yellow-800' },
      DAMAGED: { label: 'Hỏng', className: 'bg-red-100 text-red-800' },
      RETURNED: { label: 'Trả lại', className: 'bg-purple-100 text-purple-800' }
    }
    const config = statusMap[status] || { label: status, className: 'bg-gray-100 text-gray-800' }
    return (
      <span className={`px-2 py-1 text-xs font-semibold rounded ${config.className}`}>
        {config.label}
      </span>
    )
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

  if (!transaction) {
    return null
  }

  const isPurchaseOrder = transaction.poCode !== undefined
  const canComplete = isPurchaseOrder && transaction.status === 'CREATED'
  const canCancel = transaction.status === 'CREATED'

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
          <Link href="/" className="hover:text-red-500">Trang chủ</Link>
          <span>/</span>
          <Link href="/admin" className="hover:text-red-500">Quản trị</Link>
          <span>/</span>
          <Link href="/admin/inventory" className="hover:text-red-500">Quản lý kho</Link>
          <span>/</span>
          <span className="text-gray-900">Chi tiết phiếu</span>
        </nav>

        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center space-x-4">
            <button
              onClick={() => router.back()}
              className="p-2 hover:bg-gray-200 rounded-lg transition-colors"
            >
              <FiArrowLeft size={24} />
            </button>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">
                {isPurchaseOrder ? 'Phiếu nhập hàng' : 'Phiếu xuất hàng'}
              </h1>
              <p className="text-gray-600 mt-1">
                Mã: {transaction.poCode || transaction.exportCode}
              </p>
            </div>
          </div>
          <div className="flex items-center space-x-3">
            {getStatusBadge(transaction.status)}
            {canComplete && (
              <button
                onClick={() => setShowSerialModal(true)}
                className="flex items-center space-x-2 bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 transition-colors"
              >
                <FiCheck />
                <span>Hoàn tất nhập hàng</span>
              </button>
            )}
            {canCancel && (
              <button
                onClick={handleCancelOrder}
                className="flex items-center space-x-2 bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition-colors"
              >
                <FiX />
                <span>Hủy phiếu</span>
              </button>
            )}
          </div>
        </div>

        {/* Transaction Info */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <h2 className="text-lg font-semibold mb-4">Thông tin chung</h2>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-gray-600">Ngày tạo:</span>
                <span className="font-medium">{formatDate(transaction.orderDate || transaction.exportDate)}</span>
              </div>
              {transaction.receivedDate && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Ngày nhận:</span>
                  <span className="font-medium">{formatDate(transaction.receivedDate)}</span>
                </div>
              )}
              <div className="flex justify-between">
                <span className="text-gray-600">Người tạo:</span>
                <span className="font-medium">{transaction.createdBy || 'N/A'}</span>
              </div>
              {transaction.note && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Ghi chú:</span>
                  <span className="font-medium">{transaction.note}</span>
                </div>
              )}
              {transaction.reason && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Lý do:</span>
                  <span className="font-medium">{transaction.reason}</span>
                </div>
              )}
            </div>
          </div>

          {isPurchaseOrder && transaction.supplier && (
            <div className="bg-white rounded-lg shadow-sm p-6">
              <h2 className="text-lg font-semibold mb-4">Nhà cung cấp</h2>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-gray-600">Tên:</span>
                  <span className="font-medium">{transaction.supplier.name}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Mã số thuế:</span>
                  <span className="font-medium">{transaction.supplier.taxCode}</span>
                </div>
                {transaction.supplier.phone && (
                  <div className="flex justify-between">
                    <span className="text-gray-600">Điện thoại:</span>
                    <span className="font-medium">{transaction.supplier.phone}</span>
                  </div>
                )}
                {transaction.supplier.email && (
                  <div className="flex justify-between">
                    <span className="text-gray-600">Email:</span>
                    <span className="font-medium">{transaction.supplier.email}</span>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Items Table */}
        <div className="bg-white rounded-lg shadow-sm overflow-hidden">
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-lg font-semibold">Danh sách sản phẩm</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Sản phẩm</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Thông số kỹ thuật</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">SKU</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">SL</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Đơn giá</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Thành tiền</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ngày nhập</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">BH</th>
                  {transaction.status !== 'CREATED' && (
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Serial</th>
                  )}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {transaction.items?.map((item: any, index: number) => (
                  <tr key={index} className="hover:bg-gray-50">
                    {/* Cột Sản phẩm */}
                    <td className="px-6 py-4">
                      <div className="text-sm font-semibold text-gray-900 mb-1">
                        {item.warehouseProduct?.internalName || 'N/A'}
                      </div>
                      {item.warehouseProduct?.description && (
                        <div className="text-xs text-gray-600">
                          {item.warehouseProduct.description}
                        </div>
                      )}
                      {item.note && (
                        <div className="text-xs text-orange-600 mt-2 italic">
                          💬 {item.note}
                        </div>
                      )}
                    </td>
                    
                    {/* Cột Thông số kỹ thuật */}
                    <td className="px-6 py-4">
                      {item.warehouseProduct?.techSpecsJson && item.warehouseProduct.techSpecsJson !== '{}' ? (
                        <div className="text-xs text-gray-700 space-y-1">
                          {(() => {
                            try {
                              const specs = JSON.parse(item.warehouseProduct.techSpecsJson)
                              return Object.entries(specs).map(([key, value]: [string, any]) => (
                                <div key={key} className="flex">
                                  <span className="font-semibold text-gray-900 min-w-[100px]">{key}:</span>
                                  <span className="text-gray-700">{value}</span>
                                </div>
                              ))
                            } catch {
                              return <div className="text-gray-500">{item.warehouseProduct.techSpecsJson}</div>
                            }
                          })()}
                        </div>
                      ) : (
                        <span className="text-xs text-gray-400 italic">Chưa có thông số</span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-sm font-mono font-semibold text-gray-900">{item.sku}</td>
                    <td className="px-6 py-4 text-sm text-center font-bold text-gray-900">{item.quantity}</td>
                    <td className="px-6 py-4 text-sm text-gray-900">{formatPrice(item.unitCost || item.totalCost / item.quantity)}</td>
                    <td className="px-6 py-4 text-sm font-semibold text-red-600">
                      {formatPrice((item.unitCost || item.totalCost / item.quantity) * item.quantity)}
                    </td>
                    <td className="px-6 py-4 text-xs text-gray-600">
                      {item.productDetails?.[0]?.importDate 
                        ? formatDate(item.productDetails[0].importDate)
                        : '-'}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600">{item.warrantyMonths || 0} tháng</td>
                    {transaction.status !== 'CREATED' && (
                      <td className="px-6 py-4">
                        {item.productDetails?.length > 0 ? (
                          <div className="space-y-1">
                            {item.productDetails.map((detail: any, idx: number) => (
                              <div key={idx} className="flex items-center space-x-2 text-xs">
                                <span className="font-mono font-medium text-gray-900">
                                  {detail.serialNumber}
                                </span>
                                {getProductStatusBadge(detail.status)}
                              </div>
                            ))}
                          </div>
                        ) : (
                          <span className="text-xs text-gray-400">Chưa nhập</span>
                        )}
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
              <tfoot className="bg-gray-50 border-t-2 border-gray-300">
                <tr>
                  <td colSpan={5} className="px-6 py-4 text-right font-semibold text-gray-900">
                    Tổng cộng:
                  </td>
                  <td className="px-6 py-4 text-sm font-bold text-red-600">
                    {formatPrice(
                      transaction.items?.reduce((sum: number, item: any) => 
                        sum + ((item.unitCost || item.totalCost / item.quantity) * item.quantity), 0
                      ) || 0
                    )}
                  </td>
                  <td colSpan={transaction.status !== 'CREATED' ? 3 : 2}></td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>

        {/* Serial Input Modal */}
        {showSerialModal && canComplete && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
              <div className="p-6 border-b border-gray-200 sticky top-0 bg-white">
                <h2 className="text-xl font-semibold">Nhập Serial cho sản phẩm</h2>
                <p className="text-sm text-gray-600 mt-1">Vui lòng nhập đầy đủ serial cho tất cả sản phẩm</p>
              </div>
              
              <div className="p-6 space-y-6">
                {transaction.items?.map((item: any) => (
                  <div key={item.sku} className="border border-gray-200 rounded-lg p-4">
                    <h3 className="font-semibold mb-3">
                      {item.sku} - Số lượng: {item.quantity}
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                      {Array.from({ length: item.quantity }).map((_, index) => (
                        <div key={index}>
                          <label className="block text-sm font-medium text-gray-700 mb-1">
                            Serial #{index + 1}
                          </label>
                          <input
                            type="text"
                            value={serialInputs[item.sku]?.[index] || ''}
                            onChange={(e) => updateSerial(item.sku, index, e.target.value)}
                            placeholder={`Nhập serial ${index + 1}`}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                          />
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              </div>

              <div className="p-6 border-t border-gray-200 flex justify-end space-x-3 sticky bottom-0 bg-white">
                <button
                  onClick={() => setShowSerialModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  Hủy
                </button>
                <button
                  onClick={handleCompleteOrder}
                  className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
                >
                  Hoàn tất nhập hàng
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
