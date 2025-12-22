'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/store/authStore'
import { hasPermission, type Position } from '@/lib/permissions'
import toast from 'react-hot-toast'

export default function WarehouseImportCreateRedirect() {
  const router = useRouter()
  const { employee } = useAuthStore()
  
  useEffect(() => {
    // Check permission
    const canCreate = hasPermission(employee?.position as Position, 'warehouse.import.create')
    
    if (!canCreate) {
      toast.error('Bạn không có quyền tạo phiếu nhập kho')
      router.push('/employee/warehouse/import')
      return
    }
    
    router.push('/admin/inventory/transactions/create?type=IMPORT')
  }, [router, employee])

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
        <p className="mt-4 text-gray-600">Đang chuyển hướng...</p>
      </div>
    </div>
  )
}
