'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiCheck, FiX, FiUser, FiMail, FiPhone, FiCalendar } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useTranslation } from '@/hooks/useTranslation'
import { useAuthStore } from '@/store/authStore'
import { authApi } from '@/lib/api'

export default function EmployeeApprovalPage() {
  const { t } = useTranslation()
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  
  const [employees, setEmployees] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [processingId, setProcessingId] = useState<string | null>(null)
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
  }, [])

  useEffect(() => {
    if (!mounted) return

    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    // Check if user is admin
    if (user?.role !== 'ADMIN') {
      toast.error('Bạn không có quyền truy cập trang này')
      router.push('/')
      return
    }

    loadPendingEmployees()
  }, [mounted, isAuthenticated, user, router])

  const loadPendingEmployees = async () => {
    try {
      // Đảm bảo chỉ chạy ở client-side
      if (typeof window === 'undefined') {
        return
      }

      const token = localStorage.getItem('auth_token')
      
      if (!token) {
        console.log('No auth token found')
        toast.error('Vui lòng đăng nhập để xem trang này')
        setTimeout(() => router.push('/login'), 1000)
        return
      }

      console.log('Fetching pending employees with token:', token.substring(0, 20) + '...')

      const response = await fetch('http://localhost:8080/api/employee-registration/pending', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      console.log('Response status:', response.status)

      if (response.status === 401 || response.status === 403) {
        toast.error('Phiên đăng nhập hết hạn hoặc bạn không có quyền. Vui lòng đăng nhập lại.')
        localStorage.removeItem('auth_token')
        setTimeout(() => router.push('/login'), 1000)
        return
      }

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const data = await response.json()
      console.log('Received data:', data)
      
      if (data.success && data.data) {
        setEmployees(Array.isArray(data.data) ? data.data : [])
      } else if (Array.isArray(data)) {
        setEmployees(data)
      } else {
        setEmployees([])
      }
    } catch (error: any) {
      console.error('Error loading employees:', error)
      toast.error(error.message || 'Lỗi khi tải danh sách nhân viên')
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (employeeId: string) => {
    setProcessingId(employeeId)
    try {
      const response = await fetch(`http://localhost:8080/api/employee-registration/approve/${employeeId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
        }
      })
      
      const data = await response.json()
      
      if (data.success || response.ok) {
        toast.success('Đã duyệt nhân viên thành công')
        loadPendingEmployees()
      } else {
        toast.error(data.message || 'Lỗi khi duyệt nhân viên')
      }
    } catch (error: any) {
      toast.error(error.message || 'Lỗi khi duyệt nhân viên')
    } finally {
      setProcessingId(null)
    }
  }

  const handleReject = async (employeeId: string) => {
    const reason = prompt('Nhập lý do từ chối:')
    if (!reason) return

    setProcessingId(employeeId)
    try {
      // TODO: Backend cần thêm endpoint reject với reason
      toast.info('Chức năng từ chối đang được phát triển')
    } catch (error: any) {
      toast.error(error.message || 'Lỗi khi từ chối nhân viên')
    } finally {
      setProcessingId(null)
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  }

  if (!mounted || loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
          <Link href="/" className="hover:text-red-500">{t('home')}</Link>
          <span>/</span>
          <Link href="/admin" className="hover:text-red-500">Quản trị</Link>
          <span>/</span>
          <span className="text-gray-900">Duyệt nhân viên</span>
        </nav>

        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Duyệt nhân viên</h1>
          <div className="text-sm text-gray-600">
            Có {employees.length} yêu cầu đang chờ duyệt
          </div>
        </div>

        {employees.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <FiUser size={64} className="mx-auto text-gray-400 mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              Không có yêu cầu nào
            </h3>
            <p className="text-gray-600">
              Hiện tại không có yêu cầu đăng ký nhân viên nào cần duyệt
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {employees.map((employee) => (
              <div key={employee.id} className="bg-white rounded-lg shadow-sm overflow-hidden">
                <div className="p-6">
                  {/* Employee Info */}
                  <div className="flex items-center space-x-4 mb-6">
                    <div className="w-16 h-16 bg-red-500 rounded-full flex items-center justify-center text-white text-2xl font-bold">
                      {employee.fullName?.charAt(0) || 'N'}
                    </div>
                    <div>
                      <h3 className="font-semibold text-gray-900">{employee.fullName}</h3>
                      <p className="text-sm text-gray-600">{employee.position || 'Nhân viên'}</p>
                    </div>
                  </div>

                  {/* Details */}
                  <div className="space-y-3 mb-6">
                    <div className="flex items-center space-x-3 text-sm">
                      <FiMail className="text-gray-400" />
                      <span className="text-gray-700">{employee.email}</span>
                    </div>
                    <div className="flex items-center space-x-3 text-sm">
                      <FiPhone className="text-gray-400" />
                      <span className="text-gray-700">{employee.phone || 'Chưa cập nhật'}</span>
                    </div>
                    <div className="flex items-center space-x-3 text-sm">
                      <FiCalendar className="text-gray-400" />
                      <span className="text-gray-700">
                        Đăng ký: {formatDate(employee.createdAt || new Date().toISOString())}
                      </span>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex space-x-2">
                    <button
                      onClick={() => handleApprove(employee.id)}
                      disabled={processingId === employee.id}
                      className="flex-1 flex items-center justify-center space-x-2 bg-green-500 text-white py-2 px-4 rounded-lg hover:bg-green-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <FiCheck />
                      <span>Duyệt</span>
                    </button>
                    <button
                      onClick={() => handleReject(employee.id)}
                      disabled={processingId === employee.id}
                      className="flex-1 flex items-center justify-center space-x-2 bg-red-500 text-white py-2 px-4 rounded-lg hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <FiX />
                      <span>Từ chối</span>
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
