'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { FiPlus, FiEye, FiFileText } from 'react-icons/fi'
import { useAuthStore } from '@/store/authStore'
import { hasPermission, type Position } from '@/lib/permissions'
import toast from 'react-hot-toast'

interface ImportOrder {
  id: number
  code: string
  supplierName: string
  totalCost: number
  status: string
  createdAt: string
  createdBy: string
}

export default function WarehouseImportPage() {
  const { employee } = useAuthStore()
  const [importOrders, setImportOrders] = useState<ImportOrder[]>([])
  const [loading, setLoading] = useState(true)

  // Check permissions
  const canCreate = hasPermission(employee?.position as Position, 'warehouse.import.create')
  const canApprove = hasPermission(employee?.position as Position, 'warehouse.import.approve')

  useEffect(() => {
    loadImportOrders()
  }, [])

  const loadImportOrders = async () => {
    try {
      // TODO: Call API to load import orders
      // const response = await api.get('/api/warehouse/import')
      // setImportOrders(response.data)
      
      // Mock data for now
      setImportOrders([
        {
          id: 1,
          code: 'PN001',
          supplierName: 'Nhà cung cấp A',
          totalCost: 50000000,
          status: 'PENDING',
          createdAt: '2024-01-15T10:30:00',
          createdBy: 'Nguyễn Văn A'
        },
        {
          id: 2,
          code: 'PN002',
          supplierName: 'Nhà cung cấp B',
          totalCost: 30000000,
          status: 'APPROVED',
          createdAt: '2024-01-14T14:20:00',
          createdBy: 'Trần Thị B'
        },
      ])
    } catch (error) {
      console.error('Error loading import orders:', error)
      toast.error('Lỗi khi tải danh sách phiếu nhập')
    } finally {
      setLoading(false)
    }
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      APPROVED: 'bg-green-100 text-green-800',
      REJECTED: 'bg-red-100 text-red-800',
    }
    return colors[status] || 'bg-gray-100 text-gray-800'
  }

  const getStatusText = (status: string) => {
    const texts: Record<string, string> = {
      PENDING: 'Chờ duyệt',
      APPROVED: 'Đã duyệt',
      REJECTED: 'Đã từ chối',
    }
    return texts[status] || status
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  return (
    <div>
      {/* Header */}
      <div className="mb-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Phiếu nhập kho</h1>
            <p className="text-gray-600 mt-1">Quản lý phiếu nhập hàng vào kho</p>
          </div>

          {/* Only show "Create" button if user has permission */}
          {canCreate && (
            <Link
              href="/employee/warehouse/import/create"
              className="flex items-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              <FiPlus size={20} />
              <span>Tạo phiếu nhập</span>
            </Link>
          )}

          {/* Show message if user cannot create */}
          {!canCreate && (
            <div className="text-sm text-gray-500 bg-gray-100 px-4 py-2 rounded-lg">
              Bạn chỉ có quyền xem
            </div>
          )}
        </div>
      </div>

      {/* Permission notice */}
      {!canCreate && (
        <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-start">
            <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
            <div>
              <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
              <p className="text-sm text-blue-700 mt-1">
                Bạn có thể xem danh sách và chi tiết phiếu nhập kho, nhưng không thể tạo hoặc chỉnh sửa.
                {canApprove && ' Bạn có quyền duyệt phiếu nhập.'}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Import orders table */}
      <div className="bg-white rounded-lg shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Mã phiếu
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Nhà cung cấp
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Tổng tiền
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Trạng thái
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Người tạo
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Thời gian
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Thao tác
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {importOrders.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-8 text-center text-gray-500">
                    Chưa có phiếu nhập nào
                  </td>
                </tr>
              ) : (
                importOrders.map((order) => (
                  <tr key={order.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">{order.code}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{order.supplierName}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {formatPrice(order.totalCost)}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(order.status)}`}>
                        {getStatusText(order.status)}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{order.createdBy}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatDate(order.createdAt)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <Link
                        href={`/employee/warehouse/import/${order.id}`}
                        className="flex items-center text-blue-600 hover:text-blue-800"
                      >
                        <FiEye className="mr-1" size={16} />
                        Xem
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
