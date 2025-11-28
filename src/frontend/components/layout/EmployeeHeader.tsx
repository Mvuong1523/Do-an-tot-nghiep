'use client'

import { useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiUser, FiChevronDown, FiLogOut, FiHome, FiPackage, FiTag, FiUsers } from 'react-icons/fi'
import { useAuthStore } from '@/store/authStore'
import toast from 'react-hot-toast'
import Logo from './Logo'

interface EmployeeHeaderProps {
  role: 'WAREHOUSE' | 'PRODUCT_MANAGER' | 'ADMIN'
}

export default function EmployeeHeader({ role }: EmployeeHeaderProps) {
  const router = useRouter()
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false)
  const { user, logout } = useAuthStore()

  const handleLogout = () => {
    logout()
    toast.success('Đăng xuất thành công!')
    router.push('/login')
    setIsUserMenuOpen(false)
  }

  const getNavigationLinks = () => {
    switch (role) {
      case 'WAREHOUSE':
        return [
          { name: 'Dashboard', href: '/warehouse', icon: FiHome },
          { name: 'Tồn kho', href: '/warehouse/inventory', icon: FiPackage },
          { name: 'Nhập kho', href: '/warehouse/import', icon: FiPackage },
          { name: 'Xuất kho', href: '/warehouse/export', icon: FiPackage },
        ]
      case 'PRODUCT_MANAGER':
        return [
          { name: 'Dashboard', href: '/product-manager', icon: FiHome },
          { name: 'Đăng bán', href: '/product-manager/products/publish', icon: FiPackage },
          { name: 'Sản phẩm', href: '/product-manager/products', icon: FiPackage },
          { name: 'Danh mục', href: '/product-manager/categories', icon: FiTag },
        ]
      case 'ADMIN':
        return [
          { name: 'Dashboard', href: '/admin', icon: FiHome },
          { name: 'Kho hàng', href: '/admin/inventory', icon: FiPackage },
          { name: 'Sản phẩm', href: '/admin/products', icon: FiPackage },
          { name: 'Duyệt NV', href: '/admin/employee-approval', icon: FiUsers },
        ]
      default:
        return []
    }
  }

  const getRoleName = () => {
    switch (role) {
      case 'WAREHOUSE':
        return 'Nhân viên kho'
      case 'PRODUCT_MANAGER':
        return 'Quản lý sản phẩm'
      case 'ADMIN':
        return 'Quản trị viên'
      default:
        return ''
    }
  }

  const navigationLinks = getNavigationLinks()

  return (
    <header className="bg-white shadow-sm sticky top-0 z-50 border-b-2 border-red-500">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between py-4">
          {/* Logo */}
          <Link href={`/${role.toLowerCase().replace('_', '-')}`} className="flex items-center space-x-3">
            <Logo />
            <div>
              <h1 className="text-xl font-bold text-gray-900">TECH WORLD</h1>
              <p className="text-xs text-gray-500">{getRoleName()}</p>
            </div>
          </Link>

          {/* Navigation Links */}
          <nav className="hidden md:flex items-center space-x-1">
            {navigationLinks.map((link) => (
              <Link
                key={link.name}
                href={link.href}
                className="flex items-center space-x-2 px-4 py-2 text-gray-700 hover:text-red-500 hover:bg-red-50 rounded-lg font-medium transition-colors"
              >
                <link.icon size={18} />
                <span>{link.name}</span>
              </Link>
            ))}
          </nav>

          {/* User Menu */}
          <div className="relative">
            <button
              onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
              className="flex items-center space-x-2 bg-red-500 text-white px-4 py-2 rounded-lg font-medium hover:bg-red-600 transition-colors"
            >
              <FiUser size={18} />
              <span>{user?.fullName || user?.email?.split('@')[0] || 'User'}</span>
              <FiChevronDown size={16} />
            </button>
            
            {isUserMenuOpen && (
              <div className="absolute right-0 top-full mt-2 w-56 bg-white border border-gray-200 rounded-lg shadow-lg z-50">
                <div className="p-3 border-b border-gray-200">
                  <p className="text-sm font-semibold text-gray-900">{user?.fullName || user?.email}</p>
                  <p className="text-xs text-gray-500">{getRoleName()}</p>
                </div>
                <div className="py-1">
                  <Link
                    href="/profile"
                    className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                    onClick={() => setIsUserMenuOpen(false)}
                  >
                    Thông tin tài khoản
                  </Link>
                  <button
                    onClick={handleLogout}
                    className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 flex items-center space-x-2"
                  >
                    <FiLogOut size={16} />
                    <span>Đăng xuất</span>
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}
