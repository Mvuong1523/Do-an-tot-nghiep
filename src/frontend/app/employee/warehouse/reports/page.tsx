'use client'

import { useState } from 'react'
import { FiDownload, FiUpload, FiPackage } from 'react-icons/fi'

export default function WarehouseReportsPage() {
  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Báo cáo kho hàng</h1>
        <p className="text-gray-600 mt-1">Xem các báo cáo về hoạt động kho</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900">Báo cáo nhập kho</h3>
            <FiDownload className="text-green-500" size={24} />
          </div>
          <p className="text-gray-600 text-sm mb-4">
            Thống kê các phiếu nhập kho theo thời gian
          </p>
          <button className="w-full bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 transition-colors">
            Xem báo cáo
          </button>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900">Báo cáo xuất kho</h3>
            <FiUpload className="text-blue-500" size={24} />
          </div>
          <p className="text-gray-600 text-sm mb-4">
            Thống kê các phiếu xuất kho theo thời gian
          </p>
          <button className="w-full bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors">
            Xem báo cáo
          </button>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900">Báo cáo tồn kho</h3>
            <FiPackage className="text-purple-500" size={24} />
          </div>
          <p className="text-gray-600 text-sm mb-4">
            Thống kê tình trạng tồn kho hiện tại
          </p>
          <button className="w-full bg-purple-500 text-white px-4 py-2 rounded-lg hover:bg-purple-600 transition-colors">
            Xem báo cáo
          </button>
        </div>
      </div>

      <div className="mt-8 bg-white rounded-lg shadow-sm p-6">
        <h2 className="text-xl font-bold text-gray-900 mb-4">Chức năng đang phát triển</h2>
        <p className="text-gray-600">
          Các báo cáo chi tiết đang được phát triển. Vui lòng quay lại sau.
        </p>
      </div>
    </div>
  )
}
