'use client'

import { useState, useEffect, useRef } from 'react'
import Link from 'next/link'
import { useRouter, useParams } from 'next/navigation'
import { FiArrowLeft, FiPrinter, FiDownload, FiEdit, FiTrash2, FiCheck, FiX } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import { useReactToPrint } from 'react-to-print'

interface TransactionItem {
  productId: number
  productName: string
  quantity: number
  price: number
}

interface Transaction {
  id: number
  type: 'IMPORT' | 'EXPORT'
  transactionCode: string
  createdBy: string
  createdAt: string
  totalAmount: number
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED'
  note?: string
  supplier?: string
  invoiceNumber?: string
  items: TransactionItem[]
}

export default function TransactionDetailPage() {
  const router = useRouter()
  const params = useParams()
  const { user, isAuthenticated } = useAuthStore()
  const printRef = useRef<HTMLDivElement>(null)
  
  const [transaction, setTransaction] = useState<Transaction | null>(null)
  const [loading, setLoading] = useState(true)
  const [showCancelModal, setShowCancelModal] = useState(false)

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    // Check if user is admin or employee (tạm thời cho tất cả employee)
    if (user?.role !== 'ADMIN' && user?.role !== 'EMPLOYEE') {
      toast.error('Chỉ quản lý và nhân viên mới có quyền truy cập')
      router.push('/')
      return
    }

    loadTransaction()
  }, [isAuthenticated, user, router, params.id])

  const loadTransaction = async () => {
    try {
      // TODO: Call API
      // const response = await inventoryApi.getTransaction(params.id)
      // setTransaction(response.data)
      
      // Mock data
      setTransaction({
        id: Number(params.id),
        type: 'IMPORT',
        transactionCode: 'IMP-2024-001',
        createdBy: 'Admin',
        createdAt: '2024-01-15T10:30:00',
        totalAmount: 299900000,
        status: 'COMPLETED',
        note: 'Nhập hàng iPhone 16 Pro Max từ nhà cung cấp Apple',
        supplier: 'Apple Vietnam',
        invoiceNumber: 'INV-2024-001',
        items: [
          { productId: 1, productName: 'iPhone 16 Pro Max 256GB - Titan Tự Nhiên', quantity: 5, price: 29990000 },
          { productId: 2, productName: 'iPhone 16 Pro Max 256GB - Titan Đen', quantity: 5, price: 29990000 }
        ]
      })
    } catch (error) {
      toast.error('Lỗi khi tải thông tin phiếu')
      router.push('/admin/inventory/transactions')
    } finally {
      setLoading(false)
    }
  }

  const handlePrint = useReactToPrint({
    content: () => printRef.current,
    documentTitle: transaction?.transactionCode || 'Phiếu xuất nhập kho',
  })

  const handleComplete = async () => {
    if (!transaction) return

    try {
      // TODO: Call API
      // await inventoryApi.completeTransaction(transaction.id)
      
      setTransaction({ ...transaction, status: 'COMPLETED' })
      toast.success('Đã hoàn thành phiếu')
    } catch (error) {
      toast.error('Có lỗi xảy ra')
    }
  }

  const handleCancel = async () => {
    if (!transaction) return

    try {
      // TODO: Call API
      // await inventoryApi.cancelTransaction(transaction.id)
      
      setTransaction({ ...transaction, status: 'CANCELLED' })
      setShowCancelModal(false)
      toast.success('Đã hủy phiếu')
    } catch (error) {
      toast.error('Có lỗi xảy ra')
    }
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('vi-VN')
  }

  const getStatusBadge = (status: string) => {
    const badges = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      COMPLETED: 'bg-green-100 text-green-800',
      CANCELLED: 'bg-red-100 text-red-800'
    }
    const labels = {
      PENDING: 'Chờ xử lý',
      COMPLETED: 'Hoàn thành',
      CANCELLED: 'Đã hủy'
    }
    return (
      <span className={`px-3 py-1 text-sm font-semibold rounded-full ${badges[status as keyof typeof badges]}`}>
        {labels[status as keyof typeof labels]}
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
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-600">Không tìm thấy phiếu</p>
          <Link
            href="/admin/inventory/transactions"
            className="mt-4 inline-block text-red-500 hover:text-red-600"
          >
            Quay lại danh sách
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
          <Link href="/" className="hover:text-red-500">Trang chủ</Link>
          <span>/</span>
          <Link href="/admin" className="hover:text-red-500">Quản trị</Link>
          <span>/</span>
          <Link href="/admin/inventory/transactions" className="hover:text-red-500">Xuất nhập kho</Link>
          <span>/</span>
          <span className="text-gray-900">{transaction.transactionCode}</span>
        </nav>

        {/* Header */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 space-y-4 md:space-y-0">
          <div className="flex items-center space-x-4">
            <Link
              href="/admin/inventory/transactions"
              className="p-2 hover:bg-gray-200 rounded-lg transition-colors"
            >
              <FiArrowLeft size={24} />
            </Link>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">{transaction.transactionCode}</h1>
              <p className="text-gray-600 mt-1">
                {transaction.type === 'IMPORT' ? 'Phiếu nhập kho' : 'Phiếu xuất kho'}
              </p>
            </div>
          </div>
          
          <div className="flex space-x-2">
            <button
              onClick={handlePrint}
              className="flex items-center space-x-2 bg-gray-200 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-300 transition-colors"
            >
              <FiPrinter />
              <span>In phiếu</span>
            </button>
            
            {transaction.status === 'PENDING' && (
              <>
                <button
                  onClick={handleComplete}
                  className="flex items-center space-x-2 bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 transition-colors"
                >
                  <FiCheck />
                  <span>Hoàn thành</span>
                </button>
                <button
                  onClick={() => setShowCancelModal(true)}
                  className="flex items-center space-x-2 bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition-colors"
                >
                  <FiX />
                  <span>Hủy phiếu</span>
                </button>
              </>
            )}
          </div>
        </div>

        {/* Printable Content */}
        <div ref={printRef} className="bg-white rounded-lg shadow-sm p-8">
          {/* Header Info */}
          <div className="border-b pb-6 mb-6">
            <div className="flex justify-between items-start">
              <div>
                <h2 className="text-2xl font-bold text-gray-900 mb-2">
                  {transaction.type === 'IMPORT' ? 'PHIẾU NHẬP KHO' : 'PHIẾU XUẤT KHO'}
                </h2>
                <p className="text-gray-600">Mã phiếu: <span className="font-semibold">{transaction.transactionCode}</span></p>
              </div>
              <div className="text-right">
                {getStatusBadge(transaction.status)}
                <p className="text-sm text-gray-600 mt-2">
                  Ngày tạo: {formatDate(transaction.createdAt)}
                </p>
              </div>
            </div>
          </div>

          {/* Transaction Info */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Thông tin chung</h3>
              <div className="space-y-3">
                <div className="flex">
                  <span className="text-gray-600 w-32">Người tạo:</span>
                  <span className="font-medium text-gray-900">{transaction.createdBy}</span>
                </div>
                <div className="flex">
                  <span className="text-gray-600 w-32">Thời gian:</span>
                  <span className="font-medium text-gray-900">{formatDate(transaction.createdAt)}</span>
                </div>
                {transaction.supplier && (
                  <div className="flex">
                    <span className="text-gray-600 w-32">Nhà cung cấp:</span>
                    <span className="font-medium text-gray-900">{transaction.supplier}</span>
                  </div>
                )}
                {transaction.invoiceNumber && (
                  <div className="flex">
                    <span className="text-gray-600 w-32">Số hóa đơn:</span>
                    <span className="font-medium text-gray-900">{transaction.invoiceNumber}</span>
                  </div>
                )}
              </div>
            </div>

            {transaction.note && (
              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Ghi chú</h3>
                <p className="text-gray-700 bg-gray-50 p-4 rounded-lg">{transaction.note}</p>
              </div>
            )}
          </div>

          {/* Items Table */}
          <div className="mb-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Chi tiết sản phẩm</h3>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 border-b-2 border-gray-200">
                  <tr>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">STT</th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Tên sản phẩm</th>
                    <th className="px-4 py-3 text-center text-sm font-semibold text-gray-700">Số lượng</th>
                    <th className="px-4 py-3 text-right text-sm font-semibold text-gray-700">Đơn giá</th>
                    <th className="px-4 py-3 text-right text-sm font-semibold text-gray-700">Thành tiền</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {transaction.items.map((item, index) => (
                    <tr key={item.productId} className="hover:bg-gray-50">
                      <td className="px-4 py-4 text-sm text-gray-900">{index + 1}</td>
                      <td className="px-4 py-4 text-sm text-gray-900">{item.productName}</td>
                      <td className="px-4 py-4 text-sm text-center text-gray-900">{item.quantity}</td>
                      <td className="px-4 py-4 text-sm text-right text-gray-900">{formatPrice(item.price)}</td>
                      <td className="px-4 py-4 text-sm text-right font-medium text-gray-900">
                        {formatPrice(item.quantity * item.price)}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot className="bg-gray-50 border-t-2 border-gray-200">
                  <tr>
                    <td colSpan={4} className="px-4 py-4 text-right text-sm font-semibold text-gray-900">
                      Tổng cộng:
                    </td>
                    <td className="px-4 py-4 text-right text-lg font-bold text-red-600">
                      {formatPrice(transaction.totalAmount)}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>

          {/* Summary */}
          <div className="border-t pt-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="text-center">
                <p className="text-sm text-gray-600 mb-2">Tổng số lượng</p>
                <p className="text-2xl font-bold text-gray-900">
                  {transaction.items.reduce((sum, item) => sum + item.quantity, 0)}
                </p>
              </div>
              <div className="text-center">
                <p className="text-sm text-gray-600 mb-2">Số mặt hàng</p>
                <p className="text-2xl font-bold text-gray-900">{transaction.items.length}</p>
              </div>
              <div className="text-center">
                <p className="text-sm text-gray-600 mb-2">Tổng giá trị</p>
                <p className="text-2xl font-bold text-red-600">{formatPrice(transaction.totalAmount)}</p>
              </div>
            </div>
          </div>

          {/* Signatures */}
          <div className="grid grid-cols-3 gap-8 mt-12 pt-8 border-t print:block">
            <div className="text-center">
              <p className="font-semibold text-gray-900 mb-16">Người lập phiếu</p>
              <p className="text-gray-600">{transaction.createdBy}</p>
            </div>
            <div className="text-center">
              <p className="font-semibold text-gray-900 mb-16">Thủ kho</p>
              <p className="text-gray-600">_______________</p>
            </div>
            <div className="text-center">
              <p className="font-semibold text-gray-900 mb-16">Giám đốc</p>
              <p className="text-gray-600">_______________</p>
            </div>
          </div>
        </div>

        {/* Activity Log */}
        <div className="bg-white rounded-lg shadow-sm p-6 mt-6 print:hidden">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Lịch sử hoạt động</h3>
          <div className="space-y-4">
            <div className="flex items-start space-x-4">
              <div className="w-2 h-2 bg-green-500 rounded-full mt-2"></div>
              <div className="flex-1">
                <p className="text-sm font-medium text-gray-900">Phiếu được tạo</p>
                <p className="text-xs text-gray-500">{formatDate(transaction.createdAt)} bởi {transaction.createdBy}</p>
              </div>
            </div>
            {transaction.status === 'COMPLETED' && (
              <div className="flex items-start space-x-4">
                <div className="w-2 h-2 bg-blue-500 rounded-full mt-2"></div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">Phiếu đã hoàn thành</p>
                  <p className="text-xs text-gray-500">{formatDate(transaction.createdAt)}</p>
                </div>
              </div>
            )}
            {transaction.status === 'CANCELLED' && (
              <div className="flex items-start space-x-4">
                <div className="w-2 h-2 bg-red-500 rounded-full mt-2"></div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">Phiếu đã bị hủy</p>
                  <p className="text-xs text-gray-500">{formatDate(transaction.createdAt)}</p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Cancel Modal */}
      {showCancelModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full p-6">
            <h3 className="text-xl font-bold text-gray-900 mb-4">Xác nhận hủy phiếu</h3>
            <p className="text-gray-600 mb-6">
              Bạn có chắc chắn muốn hủy phiếu <strong>{transaction.transactionCode}</strong>? 
              Hành động này không thể hoàn tác.
            </p>
            <div className="flex space-x-3">
              <button
                onClick={handleCancel}
                className="flex-1 bg-red-500 text-white py-3 px-6 rounded-lg font-semibold hover:bg-red-600 transition-colors"
              >
                Xác nhận hủy
              </button>
              <button
                onClick={() => setShowCancelModal(false)}
                className="flex-1 bg-gray-200 text-gray-700 py-3 px-6 rounded-lg font-semibold hover:bg-gray-300 transition-colors"
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

      <style jsx global>{`
        @media print {
          body * {
            visibility: hidden;
          }
          .print\\:hidden {
            display: none !important;
          }
          #print-content,
          #print-content * {
            visibility: visible;
          }
          #print-content {
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
          }
        }
      `}</style>
    </div>
  )
}
