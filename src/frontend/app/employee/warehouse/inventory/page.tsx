'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { FiPackage, FiSearch } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import { inventoryApi } from '@/lib/api'
import { hasPermission, type Position } from '@/lib/permissions'

export default function WarehouseInventoryPage() {
  const { employee } = useAuthStore()
  const [inventory, setInventory] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')

  // Check permissions
  const canImport = hasPermission(employee?.position as Position, 'warehouse.import.create')
  const canExport = hasPermission(employee?.position as Position, 'warehouse.export.create')

  useEffect(() => {
    loadInventory()
  }, [])

  const loadInventory = async () => {
    try {
      const response = await inventoryApi.getStocks()
      if (response.success && response.data) {
        const mappedData = response.data.map((stock: any) => ({
          id: stock.id,
          name: stock.warehouseProduct?.internalName || 'N/A',
          sku: stock.warehouseProduct?.sku || 'N/A',
          quantity: stock.onHand || 0,
          reserved: stock.reserved || 0,
          damaged: stock.damaged || 0,
          sellable: stock.sellable || 0,
          supplier: stock.warehouseProduct?.supplier?.name || 'N/A',
          description: stock.warehouseProduct?.description || ''
        }))
        setInventory(mappedData)
      }
    } catch (error) {
      console.error('Error loading inventory:', error)
      toast.error('Lỗi khi tải kho hàng')
    } finally {
      setLoading(false)
    }
  }

  const filteredInventory = inventory.filter(item =>
    item.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    item.sku?.toLowerCase().includes(searchQuery.toLowerCase())
  )

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải...</p>
        </div>
      </div>
    )
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Tồn kho</h1>
          <p className="text-gray-600 mt-1">Quản lý tồn kho sản phẩm</p>
        </div>
        <div className="flex space-x-2">
          {canImport && (
            <Link
              href="/employee/warehouse/import/create"
              className="bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 transition-colors"
            >
              Nhập hàng
            </Link>
          )}
          {canExport && (
            <Link
              href="/employee/warehouse/export/create"
              className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
            >
              Xuất hàng
            </Link>
          )}
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div className="relative">
          <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Tìm kiếm sản phẩm..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      {filteredInventory.length === 0 ? (
        <div className="bg-white rounded-lg shadow-sm p-12 text-center">
          <FiPackage size={64} className="mx-auto text-gray-400 mb-4" />
          <h3 className="text-xl font-semibold text-gray-900 mb-2">Chưa có sản phẩm nào</h3>
          <p className="text-gray-600">Kho hàng hiện đang trống</p>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Sản phẩm</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">SKU</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nhà cung cấp</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tồn kho</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Đã giữ</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Hỏng</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Có thể bán</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredInventory.map((item) => (
                  <tr key={item.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="text-sm font-medium text-gray-900">{item.name}</div>
                      {item.description && (
                        <div className="text-xs text-gray-500 truncate max-w-xs">{item.description}</div>
                      )}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">{item.sku}</td>
                    <td className="px-6 py-4 text-sm text-gray-500">{item.supplier}</td>
                    <td className="px-6 py-4 text-sm text-gray-900">{item.quantity}</td>
                    <td className="px-6 py-4 text-sm text-gray-500">{item.reserved}</td>
                    <td className="px-6 py-4 text-sm text-gray-500">{item.damaged}</td>
                    <td className="px-6 py-4 text-sm font-medium text-gray-900">{item.sellable}</td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-1 text-xs font-semibold rounded ${
                        item.sellable > 10 ? 'bg-green-100 text-green-800' :
                        item.sellable > 0 ? 'bg-yellow-100 text-yellow-800' :
                        'bg-red-100 text-red-800'
                      }`}>
                        {item.sellable > 10 ? 'Còn hàng' : item.sellable > 0 ? 'Sắp hết' : 'Hết hàng'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
