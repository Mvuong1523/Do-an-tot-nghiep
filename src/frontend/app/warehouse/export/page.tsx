'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'

export default function WarehouseExportRedirect() {
  const router = useRouter()
  
  useEffect(() => {
    // Redirect to create export page
    router.push('/admin/inventory/transactions/create?type=EXPORT')
  }, [router])

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500 mx-auto"></div>
        <p className="mt-4 text-gray-600">Đang chuyển hướng...</p>
      </div>
    </div>
  )
}
