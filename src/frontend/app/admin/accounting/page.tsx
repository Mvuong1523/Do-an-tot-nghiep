'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { FiFileText, FiCalendar, FiDollarSign, FiBarChart, FiTruck } from 'react-icons/fi'

export default function AccountingPage() {
  const router = useRouter()

  useEffect(() => {
    const authStorage = localStorage.getItem('auth-storage')
    if (!authStorage) {
      router.push('/login')
      return
    }

    const authData = JSON.parse(authStorage)
    const userData = authData.state?.user
    
    if (!userData || userData.role !== 'ADMIN') {
      router.push('/')
      return
    }
  }, [router])

  const modules = [
    {
      title: 'Giao dịch tài chính',
      description: 'Quản lý các giao dịch thu chi',
      icon: FiFileText,
      href: '/admin/accounting/transactions',
      color: 'blue'
    },
    {
      title: 'Kỳ kế toán',
      description: 'Quản lý và chốt kỳ kế toán',
      icon: FiCalendar,
      href: '/admin/accounting/periods',
      color: 'green'
    },
    {
      title: 'Quản lý thuế',
      description: 'Báo cáo và theo dõi thuế',
      icon: FiDollarSign,
      href: '/admin/accounting/tax',
      color: 'orange'
    },
    {
      title: 'Báo cáo nâng cao',
      description: 'Phân tích lãi lỗ, dòng tiền, chi phí',
      icon: FiBarChart,
      href: '/admin/accounting/advanced-reports',
      color: 'purple'
    },
    {
      title: 'Đối soát vận chuyển',
      description: 'So sánh phí vận chuyển và chi phí',
      icon: FiTruck,
      href: '/admin/accounting/shipping',
      color: 'red'
    }
  ]

  const getColorClasses = (color: string) => {
    const colors: any = {
      blue: 'bg-blue-100 text-blue-600',
      green: 'bg-green-100 text-green-600',
      orange: 'bg-orange-100 text-orange-600',
      purple: 'bg-purple-100 text-purple-600',
      red: 'bg-red-100 text-red-600'
    }
    return colors[color] || colors.blue
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Kế toán</h1>
          <p className="mt-2 text-gray-600">Quản lý tài chính và kế toán doanh nghiệp</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {modules.map((module) => (
            <button
              key={module.href}
              onClick={() => router.push(module.href)}
              className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow p-6 text-left"
            >
              <div className={`inline-flex p-3 rounded-lg ${getColorClasses(module.color)} mb-4`}>
                <module.icon size={24} />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">{module.title}</h3>
              <p className="text-sm text-gray-600">{module.description}</p>
            </button>
          ))}
        </div>

        <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h3 className="text-sm font-medium text-blue-900 mb-2">📌 Lưu ý:</h3>
          <ul className="text-sm text-blue-800 space-y-1">
            <li>• Tất cả module kế toán chỉ dành cho Admin và Kế toán viên</li>
            <li>• Dữ liệu được tự động đồng bộ từ các giao dịch và đơn hàng</li>
            <li>• Hãy chốt kỳ kế toán định kỳ để theo dõi tài chính chính xác</li>
          </ul>
        </div>
      </div>
    </div>
  )
}
