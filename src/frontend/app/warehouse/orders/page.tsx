'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';

interface OrderItem {
  itemId: number;
  productId: number;
  productName: string;
  productSku: string;
  quantity: number;
  reserved: boolean;
  exported: boolean;
  serialNumber?: string;
}

interface Order {
  orderId: number;
  orderCode: string;
  status: string;
  customerName: string;
  customerPhone: string;
  shippingAddress: string;
  items: OrderItem[];
  total: number;
  createdAt: string;
  confirmedAt: string;
}

export default function WarehouseOrdersPage() {
  const router = useRouter();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchPendingOrders();
  }, []);

  const fetchPendingOrders = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      
      const response = await fetch('http://localhost:8080/api/inventory/orders/pending-export', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      const result = await response.json();
      
      if (result.success) {
        setOrders(result.data || []);
      } else {
        setError(result.message || 'Không thể tải danh sách đơn hàng');
      }
    } catch (err) {
      console.error('Error fetching orders:', err);
      setError('Lỗi kết nối server');
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetail = (orderId: number) => {
    router.push(`/warehouse/orders/${orderId}`);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-lg">Đang tải...</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-6">
        <h1 className="text-3xl font-bold mb-2">🛒 Đơn hàng cần xuất kho</h1>
        <p className="text-gray-600">
          Danh sách đơn hàng đã xác nhận, cần chuẩn bị hàng và xuất kho
        </p>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {orders.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <p className="text-gray-500 text-lg">✅ Không có đơn hàng nào cần xuất kho</p>
          <p className="text-gray-400 text-sm mt-2">Tất cả đơn hàng đã được xử lý</p>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <div key={order.orderId} className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow">
              <div className="p-6">
                {/* Header */}
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h3 className="text-xl font-semibold text-blue-600">
                      {order.orderCode}
                    </h3>
                    <p className="text-sm text-gray-500 mt-1">
                      Xác nhận: {new Date(order.confirmedAt).toLocaleString('vi-VN')}
                    </p>
                  </div>
                  <div className="text-right">
                    <span className="inline-block px-3 py-1 bg-yellow-100 text-yellow-800 rounded-full text-sm font-medium">
                      ⏳ Chờ xuất kho
                    </span>
                    <p className="text-lg font-bold text-gray-900 mt-2">
                      {order.total.toLocaleString('vi-VN')} ₫
                    </p>
                  </div>
                </div>

                {/* Customer Info */}
                <div className="bg-gray-50 rounded p-4 mb-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-sm text-gray-600">Khách hàng</p>
                      <p className="font-medium">{order.customerName}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Số điện thoại</p>
                      <p className="font-medium">{order.customerPhone}</p>
                    </div>
                  </div>
                  <div className="mt-2">
                    <p className="text-sm text-gray-600">Địa chỉ giao hàng</p>
                    <p className="font-medium">{order.shippingAddress}</p>
                  </div>
                </div>

                {/* Items Summary */}
                <div className="mb-4">
                  <p className="text-sm font-semibold text-gray-700 mb-2">
                    📦 {order.items.length} sản phẩm cần xuất kho
                  </p>
                  <div className="bg-blue-50 rounded p-3">
                    <p className="text-sm text-blue-800">
                      Click "Xem chi tiết" để xem danh sách sản phẩm đầy đủ
                    </p>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex justify-end gap-3">
                  <button
                    onClick={() => handleViewDetail(order.orderId)}
                    className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
                  >
                    📦 Xem chi tiết & Xuất kho
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Summary */}
      {orders.length > 0 && (
        <div className="mt-6 bg-blue-50 rounded-lg p-4">
          <div className="flex justify-between items-center">
            <p className="text-lg font-semibold text-blue-900">
              Tổng cộng: {orders.length} đơn hàng cần xuất kho
            </p>
            <button
              onClick={fetchPendingOrders}
              className="px-4 py-2 bg-white text-blue-600 rounded-lg hover:bg-blue-100 transition-colors"
            >
              🔄 Làm mới
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
