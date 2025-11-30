'use client'

import { useState } from 'react'
import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { 
  FiHome, 
  FiPackage, 
  FiDownload, 
  FiUpload, 
  FiTruck,
  FiBarChart2,
  FiChevronDown,
  FiUser,
  FiLogOut,
  FiTag,
  FiUsers,
  FiShoppingCart,
  FiFileText
} from 'react-icons/fi'
import { useAuthStore } from '@/store/authStore'
import toast from 'react-hot-toast'
import Logo from './Logo'

interface MenuItem {
  name: string
  icon: any
  path?: string
  children?: { name: string; path: string }[]
}

interface HorizontalNavProps {
  role: 'WAREHOUSE' | 'PRODUCT_MANAGER' | 'ADMIN' | 'ACCOUNTANT' | 'SALES'
}

export default function HorizontalNav({ role }: HorizontalNavProps) {
  const pathname = usePathname()
  const router = useRouter()
  const { user, logout } = useAuthStore()
  const [openDropdown, setOpenDropdown] = useState<string | null>(null)
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false)

  const handleLogout = () => {
    logout()
    toast.success('Đăng xuất thành công!')
    router.push('/login')
    setIsUserMenuOpen(false)
  }

  const isActive = (path: string) => pathname === path
  const isParentActive = (paths: string[]) => paths.some(path => pathname?.startsWith(path))

  const getMenuItems = (): MenuItem[] => {
    switch (role) {
      case 'WAREHOUSE':
        return [
          { name: 'Dashboard', icon: FiHome, path: '/warehouse' },
          {
            name: 'Nhập kho',
            icon: FiDownload,
            children: [
              { name: 'Tạo phiếu nhập', path: '/warehouse/import/create' },
              { name: 'Danh sách phiếu', path: '/warehouse/import/list' },
              { name: 'Hoàn thiện phiếu', path: '/warehouse/import/complete' },
            ]
          },
          {
            name: 'Xuất kho',
            icon: FiUpload,
            children: [
              { name: 'Tạo phiếu xuất', path: '/warehouse/export/create' },
              { name: 'Danh sách phiếu', path: '/warehouse/export/list' },
            ]
          },
          {
            name: 'Tồn kho',
            icon: FiPackage,
            children: [
              { name: 'Xem tồn kho', path: '/warehouse/inventory' },
              { name: 'Kiểm kê', path: '/warehouse/inventory/check' },
            ]
          },
          { name: 'Nhà cung cấp', icon: FiTruck, path: '/warehouse/suppliers' },
          { name: 'Báo cáo', icon: FiBarChart2, path: '/warehouse/reports' },
        ]
      case 'PRODUCT_MANAGER':
        return [
          { name: 'Dashboard', icon: FiHome, path: '/product-manager' },
          {
            name: 'Sản phẩm',
            icon: FiPackage,
            children: [
              { name: 'Đăng bán', path: '/product-manager/products/publish' },
              { name: 'Danh sách', path: '/product-manager/products' },
            ]
          },
          { name: 'Danh mục', icon: FiTag, path: '/product-manager/categories' },
        ]
      case 'ADMIN':
        return [
          { name: 'Dashboard', icon: FiHome, path: '/admin' },
          { name: 'Kho hàng', icon: FiPackage, path: '/admin/inventory' },
          {
            name: 'Sản phẩm',
            icon: FiShoppingCart,
            children: [
              { name: 'Danh sách', path: '/admin/products' },
              { name: 'Danh mục', path: '/admin/categories' },
            ]
          },
          {
            name: 'Kế toán',
            icon: FiFileText,
            children: [
              { name: 'Tổng quan', path: '/admin/accounting' },
              { name: 'Đối soát', path: '/admin/accounting/reconciliation' },
              { name: 'Báo cáo', path: '/admin/accounting/reports' },
            ]
          },
          { name: 'Duyệt nhân viên', icon: FiUsers, path: '/admin/employee-approval' },
        ]
      case 'ACCOUNTANT':
        return [
          { name: 'Dashboard', icon: FiHome, path: '/admin/accounting' },
          { name: 'Đối soát', icon: FiFileText, path: '/admin/accounting/reconciliation' },
          { name: 'Báo cáo', icon: FiBarChart2, path: '/admin/accounting/reports' },
          { name: 'Quản lý kỳ', icon: FiFileText, path: '/admin/accounting/periods' },
        ]
      case 'SALES':
        return [
          { name: 'Dashboard', icon: FiHome, path: '/sales' },
          { name: 'Đơn hàng', icon: FiShoppingCart, path: '/sales/orders' },
          { name: 'Xuất kho bán hàng', icon: FiUpload, path: '/sales/export' },
        ]
      default:
        return []
    }
  }

  const getRoleName = () => {
    switch (role) {
      case 'WAREHOUSE':
        return 'Quản lý kho'
      case 'PRODUCT_MANAGER':
        return 'Quản lý sản phẩm'
      case 'ADMIN':
        return 'Quản trị viên'
      case 'ACCOUNTANT':
        return 'Kế toán'
      case 'SALES':
        return 'Nhân viên bán hàng'
      default:
        return ''
    }
  }

  const menuItems = getMenuItems()

  return (
    <header className="bg-white shadow-sm sticky top-0 z-50 border-b border-gray-200">
      <div className="max-w-[1920px] mx-auto px-6">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link href={`/${role.toLowerCase().replace('_', '-')}`} className="flex items-center space-x-3">
            <Logo />
            <div>
              <h1 className="text-lg font-bold text-gray-900">TECH WORLD</h1>
              <p className="text-xs text-gray-500">{getRoleName()}</p>
            </div>
          </Link>

          {/* Navigation */}
          <nav className="hidden lg:flex items-center space-x-1">
            {menuItems.map((item) => (
              <div
                key={item.name}
                className="relative group"
              >
                {item.children ? (
                  // Menu với dropdown
                  <>
                    <button
                      className={`flex items-center space-x-2 px-4 py-2 rounded-lg font-medium transition-colors ${
                        isParentActive(item.children.map(c => c.path))
                          ? 'bg-red-50 text-red-600'
                          : 'text-gray-700 hover:text-red-600 hover:bg-gray-50'
                      }`}
                    >
                      <item.icon size={18} />
                      <span>{item.name}</span>
                      <FiChevronDown size={14} className="transition-transform group-hover:rotate-180" />
                    </button>
                    
                    <div className="absolute top-full left-0 mt-1 w-56 bg-white border border-gray-200 rounded-lg shadow-lg py-1 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200">
                      {item.children.map((child) => (
                        <Link
                          key={child.path}
                          href={child.path}
                          className={`block px-4 py-2 text-sm transition-colors ${
                            isActive(child.path)
                              ? 'bg-red-50 text-red-600 font-medium'
                              : 'text-gray-700 hover:bg-gray-50'
                          }`}
                        >
                          {child.name}
                        </Link>
                      ))}
                    </div>
                  </>
                ) : (
                  // Menu đơn
                  <Link
                    href={item.path!}
                    className={`flex items-center space-x-2 px-4 py-2 rounded-lg font-medium transition-colors ${
                      isActive(item.path!)
                        ? 'bg-red-50 text-red-600'
                        : 'text-gray-700 hover:text-red-600 hover:bg-gray-50'
                    }`}
                  >
                    <item.icon size={18} />
                    <span>{item.name}</span>
                  </Link>
                )}
              </div>
            ))}
          </nav>

          {/* User Menu */}
          <div className="relative">
            <button
              onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
              className="flex items-center space-x-2 bg-red-500 text-white px-4 py-2 rounded-lg font-medium hover:bg-red-600 transition-colors"
            >
              <div className="w-8 h-8 bg-white bg-opacity-20 rounded-full flex items-center justify-center text-sm font-bold">
                {user?.fullName?.charAt(0) || user?.email?.charAt(0) || 'U'}
              </div>
              <span className="hidden md:inline">{user?.fullName || user?.email?.split('@')[0] || 'User'}</span>
              <FiChevronDown size={16} />
            </button>
            
            {isUserMenuOpen && (
              <>
                <div 
                  className="fixed inset-0 z-40" 
                  onClick={() => setIsUserMenuOpen(false)}
                />
                <div className="absolute right-0 top-full mt-2 w-56 bg-white border border-gray-200 rounded-lg shadow-lg z-50">
                  <div className="p-3 border-b border-gray-200">
                    <p className="text-sm font-semibold text-gray-900">{user?.fullName || user?.email}</p>
                    <p className="text-xs text-gray-500">{getRoleName()}</p>
                  </div>
                  <div className="py-1">
                    <Link
                      href="/profile"
                      className="flex items-center space-x-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                      onClick={() => setIsUserMenuOpen(false)}
                    >
                      <FiUser size={16} />
                      <span>Thông tin tài khoản</span>
                    </Link>
                    <button
                      onClick={handleLogout}
                      className="w-full flex items-center space-x-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50"
                    >
                      <FiLogOut size={16} />
                      <span>Đăng xuất</span>
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}
