'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'
import { authApi } from '@/lib/api'
import toast from 'react-hot-toast'
import { FiCheck, FiX, FiLoader } from 'react-icons/fi'

interface PendingEmployee {
  id: string
  email: string
  fullName: string
  phone?: string
  createdAt?: string
}

export default function EmployeeApprovalPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  const [employees, setEmployees] = useState<PendingEmployee[]>([])
  const [loading, setLoading] = useState(true)
  const [approving, setApproving] = useState<string | null>(null)

  useEffect(() => {
    if (!isAuthenticated || !user || user.role !== 'ADMIN') {
      router.replace('/login')
      return
    }
    
    loadPendingEmployees()
  }, [isAuthenticated, user, router])

  const loadPendingEmployees = async () => {
    setLoading(true)
    try {
      const res = await authApi.getPendingEmployees()
      if (res.success && res.data) {
        setEmployees(res.data)
      } else {
        toast.error('Không thể tải danh sách nhân viên chờ duyệt')
      }
    } catch (err: any) {
      toast.error(err.message || 'Lỗi khi tải danh sách')
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (employeeId: string) => {
    setApproving(employeeId)
    try {
      const res = await authApi.approveEmployee({ userId: employeeId, status: 'APPROVED' })
      if (res.success) {
        toast.success('Đã phê duyệt nhân viên')
        setEmployees(prev => prev.filter(e => e.id !== employeeId))
      } else {
        toast.error(res.message || 'Phê duyệt thất bại')
      }
    } catch (err: any) {
      toast.error(err.message || 'Phê duyệt thất bại')
    } finally {
      setApproving(null)
    }
  }

  const handleReject = async (employeeId: string) => {
    setApproving(employeeId)
    try {
      const res = await authApi.approveEmployee({ userId: employeeId, status: 'REJECTED', reason: 'Từ chối đơn đăng ký' })
      if (res.success) {
        toast.success('Đã từ chối nhân viên')
        setEmployees(prev => prev.filter(e => e.id !== employeeId))
      } else {
        toast.error(res.message || 'Từ chối thất bại')
      }
    } catch (err: any) {
      toast.error(err.message || 'Từ chối thất bại')
    } finally {
      setApproving(null)
    }
  }

  if (!isAuthenticated || !user || user.role !== 'ADMIN') {
    return null
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Duyệt đăng ký nhân viên</h1>
      
      {loading ? (
        <div className="flex justify-center py-12">
          <FiLoader className="animate-spin text-2xl" />
        </div>
      ) : employees.length === 0 ? (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 text-center">
          <p className="text-blue-800">Không có nhân viên nào chờ duyệt</p>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">Email</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">Tên nhân viên</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">Số điện thoại</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">Ngày đăng ký</th>
                <th className="px-6 py-3 text-center text-sm font-semibold text-gray-900">Hành động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {employees.map((emp) => (
                <tr key={emp.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm text-gray-900">{emp.email}</td>
                  <td className="px-6 py-4 text-sm text-gray-900">{emp.fullName}</td>
                  <td className="px-6 py-4 text-sm text-gray-600">{emp.phone || '-'}</td>
                  <td className="px-6 py-4 text-sm text-gray-600">{emp.createdAt ? new Date(emp.createdAt).toLocaleDateString('vi-VN') : '-'}</td>
                  <td className="px-6 py-4 text-center">
                    <div className="flex gap-2 justify-center">
                      <button
                        onClick={() => handleApprove(emp.id)}
                        disabled={approving === emp.id}
                        className="inline-flex items-center gap-1 bg-green-600 text-white px-3 py-2 rounded text-sm font-medium hover:bg-green-700 disabled:bg-gray-400"
                      >
                        {approving === emp.id ? <FiLoader className="animate-spin" /> : <FiCheck />}
                        Phê duyệt
                      </button>
                      <button
                        onClick={() => handleReject(emp.id)}
                        disabled={approving === emp.id}
                        className="inline-flex items-center gap-1 bg-red-600 text-white px-3 py-2 rounded text-sm font-medium hover:bg-red-700 disabled:bg-gray-400"
                      >
                        {approving === emp.id ? <FiLoader className="animate-spin" /> : <FiX />}
                        Từ chối
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
