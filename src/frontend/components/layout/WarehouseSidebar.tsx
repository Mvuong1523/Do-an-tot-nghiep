'use client'

import { useState } from 'react'
import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { 
  FiHome, 
  FiPackage, 
  FiDownload, 
  FiUpload, 
  FiFileText,
  FiTruck,
  FiBarChart2,
  FiChevronDown,
  FiChevronRight,
  FiUser,
  FiLogOut,
  FiMenu,
  FiX
} from 'react-icons/fi'
import { useAuthStore } from '@/store/authStore'
import toast from 'react-hot-toast'
import Logo from './Logo'

export default function WarehouseSidebar() {
  const pathname = usePathname()
  const router = useRouter()
  const { user, logout } = useAuthStore()
  const [expandedMenus, setExpandedMenus] = useState<string[]>(['import', 'export', 'inventory'])
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)

  const handleLogout = () => {
    logout()
    toast.success('Đăng xuất thành công!')
    router.push('/login')
  }

  const toggleMenu = (menuId: string) => {
    setExpandedMenus(prev => 
      prev.includes(menuId) 
        ? prev.filter(id => id !== menuId)
        : [...prev, menuId]
    )
  }

  const isActive = (path: string) => pathname === path
  const isParentActive = (paths: string[]) => paths.some(path => pathname?.startsWith(path))

  const menuItems = [
    {
      id: 'dashboard',
      name: 'Dashboard',
      icon: FiHome,
      path: '/warehouse',
    },
    {
      id: 'overview',
      name: 'Tổng quan kho',
      icon: FiPackage,
      path: '/admin/inventory',
    },
    {
      id: 'inventory',
      name: 'Quản lý tồn kho',
      icon: FiPackage,
      children: [
        { name: 'Xem tồn kho', path: '/warehouse/inventory' },
        { name: 'Kiểm kê', path: '/warehouse/inventory/check' },
      ]
    },
    {
      id: 'import',
      name: 'Quản lý nhập kho',
      icon: FiDownload,
      children: [
        { name: 'Tạo phiếu nhập', path: '/warehouse/import/create' },
        { name: 'Danh sách phiếu nhập', path: '/warehouse/import/list' },
        { name: 'Hoàn thiện phiếu', path: '/warehouse/import/complete' },
      ]
    },
    {
      id: 'export',
      name: 'Quản lý xuất kho',
      icon: FiUpload,
      children: [
        { name: 'Tạo phiếu xuất', path: '/warehouse/export/create' },
        { name: 'Danh sách phiếu xuất', path: '/warehouse/export/list' },
      ]
    },
    {
      id: 'suppliers',
      name: 'Nhà cung cấp',
      icon: FiTruck,
      path: '/warehouse/suppliers',
    },
    {
      id: 'reports',
      name: 'Báo cáo',
      icon: FiBarChart2,
      path: '/warehouse/reports',
    },
  ]

  const SidebarContent = () => (
    <>
      {/* Logo */}
      <div className="p-6 border-b border-gray-200">
        <Link href="/warehouse" className="flex items-center space-x-3">
          <Logo />
          <div>
            <h1 className="text-lg font-bold text-gray-900">TECH WORLD</h1>
            <p className="text-xs text-gray-500">Quản lý kho</p>
          </div>
        </Link>
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto p-4">
        <div className="space-y-1">
          {menuItems.map((item) => (
            <div key={item.id}>
              {item.children ? (
                // Menu có submenu
                <div>
                  <button
                    onClick={() => toggleMenu(item.id)}
                    className={`w-full flex items-center justify-between px-4 py-3 rounded-lg transition-colors ${
                      isParentActive(item.children.map(c => c.path))
                        ? 'bg-red-50 text-red-600'
                        : 'text-gray-700 hover:bg-gray-100'
                    }`}
                  >
                    <div className="flex items-center space-x-3">
                      <item.icon size={20} />
                      <span className="font-medium">{item.name}</span>
                    </div>
                    {expandedMenus.includes(item.id) ? (
                      <FiChevronDown size={16} />
                    ) : (
                      <FiChevronRight size={16} />
                    )}
                  </button>
                  
                  {expandedMenus.includes(item.id) && (
                    <div className="ml-4 mt-1 space-y-1">
                      {item.children.map((child) => (
                        <Link
                          key={child.path}
                          href={child.path}
                          className={`block px-4 py-2 rounded-lg text-sm transition-colors ${
                            isActive(child.path)
                              ? 'bg-red-100 text-red-600 font-medium'
                              : 'text-gray-600 hover:bg-gray-50'
                          }`}
                        >
                          {child.name}
                        </Link>
                      ))}
                    </div>
                  )}
                </div>
              ) : (
                // Menu đơn
                <Link
                  href={item.path!}
                  className={`flex items-center space-x-3 px-4 py-3 rounded-lg transition-colors ${
                    isActive(item.path!)
                      ? 'bg-red-50 text-red-600'
                      : 'text-gray-700 hover:bg-gray-100'
                  }`}
                >
                  <item.icon size={20} />
                  <span className="font-medium">{item.name}</span>
                </Link>
              )}
            </div>
          ))}
        </div>
      </nav>

      {/* User Info */}
      <div className="p-4 border-t border-gray-200">
        <div className="flex items-center space-x-3 px-4 py-3 bg-gray-50 rounded-lg">
          <div className="w-10 h-10 bg-red-500 rounded-full flex items-center justify-center text-white font-semibold">
            {user?.fullName?.charAt(0) || user?.email?.charAt(0) || 'U'}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-gray-900 truncate">
              {user?.fullName || user?.email}
            </p>
            <p className="text-xs text-gray-500">Nhân viên kho</p>
          </div>
        </div>
        
        <div className="mt-2 space-y-1">
          <Link
            href="/profile"
            className="flex items-center space-x-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-lg"
          >
            <FiUser size={16} />
            <span>Thông tin tài khoản</span>
          </Link>
          <button
            onClick={handleLogout}
            className="w-full flex items-center space-x-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg"
          >
            <FiLogOut size={16} />
            <span>Đăng xuất</span>
          </button>
        </div>
      </div>
    </>
  )

  return (
    <>
      {/* Mobile Menu Button */}
      <button
        onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
        className="lg:hidden fixed top-4 left-4 z-50 p-2 bg-white rounded-lg shadow-lg"
      >
        {isMobileMenuOpen ? <FiX size={24} /> : <FiMenu size={24} />}
      </button>

      {/* Mobile Sidebar */}
      {isMobileMenuOpen && (
        <div
          className="lg:hidden fixed inset-0 bg-black bg-opacity-50 z-40"
          onClick={() => setIsMobileMenuOpen(false)}
        >
          <div
            className="w-64 h-full bg-white flex flex-col"
            onClick={(e) => e.stopPropagation()}
          >
            <SidebarContent />
          </div>
        </div>
      )}

      {/* Desktop Sidebar */}
      <aside className="hidden lg:flex lg:flex-col w-64 bg-white border-r border-gray-200 fixed left-0 top-0 h-screen">
        <SidebarContent />
      </aside>
    </>
  )
}
