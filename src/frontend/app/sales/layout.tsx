import Link from 'next/link'
import { FiShoppingBag, FiPackage } from 'react-icons/fi'

export default function SalesLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Nhân viên bán hàng
          </h1>
          <p className="text-gray-600">Quản lý đơn hàng và bán hàng</p>
        </div>

        {/* Navigation */}
        <div className="bg-white rounded-lg shadow-sm mb-6">
          <nav className="flex space-x-1 p-2">
            <Link
              href="/sales/orders"
              className="flex items-center px-4 py-2 rounded-lg text-gray-700 hover:bg-gray-100 transition-colors"
            >
              <FiPackage className="mr-2" />
              Quản lý đơn hàng
            </Link>
          </nav>
        </div>

        {/* Content */}
        <div>{children}</div>
      </div>
    </div>
  )
}
