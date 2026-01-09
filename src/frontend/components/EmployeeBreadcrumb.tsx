'use client'

import Link from 'next/link'
import { useAuthStore } from '@/store/authStore'

interface BreadcrumbItem {
  name: string
  href?: string
}

interface EmployeeBreadcrumbProps {
  items: BreadcrumbItem[]
}

export default function EmployeeBreadcrumb({ items }: EmployeeBreadcrumbProps) {
  const { user } = useAuthStore()

  const getHomePath = () => {
    switch (user?.role) {
      case 'WAREHOUSE':
        return '/warehouse'
      case 'PRODUCT_MANAGER':
        return '/product-manager'
      case 'ADMIN':
        return '/admin'
      default:
        return '/'
    }
  }

  const getHomeName = () => {
    switch (user?.role) {
      case 'WAREHOUSE':
        return 'Kho hàng'
      case 'PRODUCT_MANAGER':
        return 'Quản lý SP'
      case 'ADMIN':
        return 'Quản trị'
      default:
        return 'Trang chủ'
    }
  }

  return (
    <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
      <Link href={getHomePath()} className="hover:text-red-500">
        {getHomeName()}
      </Link>
      {items.map((item, index) => (
        <span key={index} className="flex items-center space-x-2">
          <span>/</span>
          {item.href ? (
            <Link href={item.href} className="hover:text-red-500">
              {item.name}
            </Link>
          ) : (
            <span className="text-gray-900">{item.name}</span>
          )}
        </span>
      ))}
    </nav>
  )
}
