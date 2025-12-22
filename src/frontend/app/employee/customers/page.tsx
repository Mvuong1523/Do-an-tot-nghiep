'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { FiUsers, FiSearch, FiFileText } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import { hasPermission, type Position } from '@/lib/permissions'

export default function EmployeeCustomersPage() {
  const router = useRouter()
  const { user, employee, isAuthenticated } = useAuthStore()
  const [customers, setCustomers] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')

  const canEdit = hasPermission(employee?.position as Position, 'customers.edit')

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    if (user?.role !== 'EMPLOYEE' && user?.role !== 'ADMIN') {
      toast.error('Bạn không có quyền truy cập')
      router.push('/')
      return
    }

    loadCustomers()
  }, [isAuthenticated, user, router])

  const loadCustomers = async () => {
    try {
      // TODO: Call API to load customers
      setCustomers([])
    } catch (error) {
      console.error('Error loading customers:', error)
      toast.error('Lỗi khi tải khách hàng')
    } finally {
      setLoading(false)
    }
  }

  const filteredCustomers = customers.filter(customer =>
    customer.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    customer.email?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    customer.phone?.includes(searchQuery)
  )

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Danh sách khách hàng</h1>

        {!canEdit && (
          <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div className="flex items-start">
              <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
              <div>
                <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
                <p className="text-sm text-blue-700 mt-1">
                  Bạn chỉ có quyền xem thông tin khách hàng, không thể chỉnh sửa.
                </p>
              </div>
            </div>
          </div>
        )}

        <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
          <div className="relative">
            <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Tìm kiếm khách hàng..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        {filteredCustomers.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <FiUsers size={64} className="mx-auto text-gray-400 mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Chưa có khách hàng nào</h3>
            <p className="text-gray-600">Danh sách khách hàng sẽ hiển thị ở đây</p>
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tên</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Số điện thoại</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Địa chỉ</th>
                    <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Đơn hàng</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredCustomers.map((customer) => (
                    <tr key={customer.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-gray-900">{customer.name}</div>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        {customer.email}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        {customer.phone}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        {customer.address}
                      </td>
                      <td className="px-6 py-4 text-center">
                        <span className="text-sm font-bold text-blue-600">
                          {customer.orderCount || 0}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
