'use client'

import { useState, useEffect } from 'react'
import { FiPackage, FiAlertCircle, FiTrendingUp, FiTrendingDown } from 'react-icons/fi'

interface InventoryStock {
  id: number
  sku: string
  productName: string
  onHand: number
  available: number
  reserved: number
  minStock: number
  maxStock: number
  lastUpdated: string
}

export default function AdminWarehouseInventoryPage() {
  const [stocks, setStocks] = useState<InventoryStock[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState<string>('ALL')

  useEffect(() => {
    fetchInventory()
  }, [])

  const fetchInventory = async () => {
    try {
      const token = localStorage.getItem('token')
      const res = await fetch('http://localhost:8080/api/inventory/stocks', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      const data = await res.json()
      if (data.success) {
        setStocks(data.data)
      }
    } catch (error) {
      console.error('Error fetching inventory:', error)
    } finally {
      setLoading(false)
    }
  }

  const getStockStatus = (stock: InventoryStock) => {
    if (stock.available <= stock.minStock) {
      return { label: 'Sắp hết', color: 'text-red-600', bg: 'bg-red-50', icon: FiAlertCircle }
    }
    if (stock.available >= stock.maxStock) {
      return { label: 'Dư thừa', color: 'text-orange-600', bg: 'bg-orange-50', icon: FiTrendingUp }
    }
    return { label: 'Bình thường', color: 'text-green-600', bg: 'bg-green-50', icon: FiPackage }
  }

  const filteredStocks = stocks.filter(stock => {
    if (filter === 'LOW') return stock.available <= stock.minStock
    if (filter === 'HIGH') return stock.available >= stock.maxStock
    return true
  })

  const stats = {
    total: stocks.length,
    low: stocks.filter(s => s.available <= s.minStock).length,
    high: stocks.filter(s => s.available >= s.maxStock).length,
    totalValue: stocks.reduce((sum, s) => sum + (s.onHand * 10000000), 0) // Giả định giá trị
  }

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Tồn Kho</h1>
        <p className="text-gray-600 mt-1">Theo dõi số lượng tồn kho và cảnh báo</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Tổng SKU</p>
              <p className="text-2xl font-bold mt-1">{stats.total}</p>
            </div>
            <FiPackage className="w-8 h-8 text-blue-600" />
          </div>
        </div>

        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Sắp hết hàng</p>
              <p className="text-2xl font-bold mt-1 text-red-600">{stats.low}</p>
            </div>
            <FiAlertCircle className="w-8 h-8 text-red-600" />
          </div>
        </div>

        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Dư thừa</p>
              <p className="text-2xl font-bold mt-1 text-orange-600">{stats.high}</p>
            </div>
            <FiTrendingUp className="w-8 h-8 text-orange-600" />
          </div>
        </div>

        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Giá trị tồn</p>
              <p className="text-2xl font-bold mt-1">{(stats.totalValue / 1000000000).toFixed(1)}B</p>
            </div>
            <FiTrendingDown className="w-8 h-8 text-green-600" />
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="flex gap-2 mb-4">
        {[
          { key: 'ALL', label: 'Tất cả' },
          { key: 'LOW', label: 'Sắp hết' },
          { key: 'HIGH', label: 'Dư thừa' }
        ].map(f => (
          <button
            key={f.key}
            onClick={() => setFilter(f.key)}
            className={`px-4 py-2 rounded-lg ${
              filter === f.key
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      {/* Table */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">SKU</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tên sản phẩm</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tồn kho</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Khả dụng</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Đã đặt</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Min/Max</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filteredStocks.map(stock => {
              const status = getStockStatus(stock)
              const StatusIcon = status.icon
              return (
                <tr key={stock.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap font-medium">{stock.sku}</td>
                  <td className="px-6 py-4 whitespace-nowrap">{stock.productName}</td>
                  <td className="px-6 py-4 whitespace-nowrap font-semibold">{stock.onHand}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-green-600">{stock.available}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-orange-600">{stock.reserved}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                    {stock.minStock} / {stock.maxStock}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className={`flex items-center gap-2 ${status.color}`}>
                      <StatusIcon className="w-4 h-4" />
                      <span className="text-sm font-medium">{status.label}</span>
                    </div>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}
