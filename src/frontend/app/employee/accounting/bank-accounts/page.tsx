'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { FiStar, FiFileText } from 'react-icons/fi'
import toast from 'react-hot-toast'
import axios from 'axios'
import { useAuthStore } from '@/store/authStore'
import { hasPermission, type Position } from '@/lib/permissions'

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'

export default function EmployeeBankAccountsPage() {
  const router = useRouter()
  const { user, employee, isAuthenticated } = useAuthStore()
  const [accounts, setAccounts] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  const canEdit = hasPermission(employee?.position as Position, 'accounting.bank_accounts.edit')

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login')
      return
    }

    if (user?.role !== 'EMPLOYEE' && user?.role !== 'ADMIN') {
      toast.error('Bạn không có quyền truy cập')
      router.push('/')
      return
    }

    loadAccounts()
  }, [isAuthenticated, user, router])

  const loadAccounts = async () => {
    try {
      const token = localStorage.getItem('auth_token') || localStorage.getItem('token')
      const response = await axios.get(`${API_URL}/admin/bank-accounts`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      setAccounts(response.data.data || [])
    } catch (error: any) {
      toast.error('Lỗi khi tải danh sách tài khoản')
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <div className="p-8">Đang tải...</div>
  }

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Quản lý tài khoản ngân hàng</h1>
      </div>

      {!canEdit && (
        <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-start">
            <FiFileText className="text-blue-500 mt-0.5 mr-3" size={20} />
            <div>
              <h3 className="text-sm font-medium text-blue-900">Quyền hạn của bạn</h3>
              <p className="text-sm text-blue-700 mt-1">
                Bạn chỉ có quyền xem danh sách tài khoản ngân hàng, không thể chỉnh sửa.
              </p>
            </div>
          </div>
        </div>
      )}

      <div className="grid gap-4">
        {accounts.map((account) => (
          <div key={account.id} className="bg-white p-6 rounded-lg shadow-sm border">
            <div className="flex justify-between items-start">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-2">
                  <h3 className="text-lg font-bold">{account.bankName}</h3>
                  {account.isDefault && (
                    <span className="bg-yellow-100 text-yellow-800 text-xs px-2 py-1 rounded flex items-center font-semibold">
                      <FiStar className="mr-1" size={12} /> Mặc định
                    </span>
                  )}
                  {account.isActive && !account.isDefault && (
                    <span className="bg-green-100 text-green-800 text-xs px-2 py-1 rounded">Kích hoạt</span>
                  )}
                  {!account.isActive && (
                    <span className="bg-gray-100 text-gray-800 text-xs px-2 py-1 rounded">Tạm dừng</span>
                  )}
                </div>
                <p className="text-gray-600">Số TK: <span className="font-mono font-bold">{account.accountNumber}</span></p>
                <p className="text-gray-600">Tên TK: {account.accountName}</p>
                {account.description && <p className="text-sm text-gray-500 mt-2">{account.description}</p>}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
