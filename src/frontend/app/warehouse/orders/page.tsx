'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';
import toast from 'react-hot-toast';

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
  const { user, isAuthenticated } = useAuthStore();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState<'pending' | 'exported'>('pending');

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập');
      router.push('/login');
      return;
    }

    const isWarehouseStaff = user?.role === 'ADMIN' || 
                             (user?.role === 'EMPLOYEE' && user?.position === 'WAREHOUSE');
    
    if (!isWarehouseStaff) {
      toast.error('Chỉ nhân viên kho mới có quyền truy cập');
      router.push('/');
      return;
    }

    fetchOrders();
  }, [isAuthenticated, user, router, activeTab]);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      setError('');
      const token = localStorage.getItem('token');
      
      let url = '';
      if (activeTab === 'pending') {
        // Lấy đơn chờ xuất kho (CONFIRMED)
        url = 'http://localhost:8080/api/inventory/orders/pending-export';
      } else {
        // Lấy đơn đã xuất kho (SHIPPING)
        url = 'http://localhost:8080/api/inventory/orders/exported';
      }
      
      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      const result = await response.json();
      
      if (result.success) {
        // Filter để đảm bảo đúng status
        const filteredOrders = (result.data || []).filter((order: Order) => {
          if (activeTab === 'pending') {
            return order.status === 'CONFIRMED';
          } else {
            return order.status === 'READY_TO_SHIP';
          }
        });
        setOrders(filteredOrders);
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
        <h1 className="text-3xl font-bold mb-2">� Quả n lý xuất kho</h1>
        <p className="text-gray-600">
          Quản lý đơn hàng cần xuất kho và đơn hàng đã xuất
        </p>
      </div>

      {/* Tabs */}
      <div className="mb-6 border-b border-gray-200">
        <div className="flex gap-4">
          <button
            onClick={() => setActiveTab('pending')}
            className={`pb-4 px-2 font-medium transition-colors relative ${
              activeTab === 'pending'
                ? 'text-blue-600 border-b-2 border-blue-600'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            ⏳ Chờ xuất kho
            {activeTab === 'pending' && orders.length > 0 && (
              <span className="ml-2 px-2 py-1 bg-blue-100 text-blue-600 rounded-full text-xs font-bold">
                {orders.length}
              </span>
            )}
          </button>
          <button
            onClick={() => setActiveTab('exported')}
            className={`pb-4 px-2 font-medium transition-colors relative ${
              activeTab === 'exported'
                ? 'text-green-600 border-b-2 border-green-600'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            ✅ Đã xuất kho
            {activeTab === 'exported' && orders.length > 0 && (
              <span className="ml-2 px-2 py-1 bg-green-100 text-green-600 rounded-full text-xs font-bold">
                {orders.length}
              </span>
            )}
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {orders.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          {activeTab === 'pending' ? (
            <>
              <p className="text-gray-500 text-lg">✅ Không có đơn hàng nào cần xuất kho</p>
              <p className="text-gray-400 text-sm mt-2">Tất cả đơn hàng đã được xử lý</p>
            </>
          ) : (
            <>
              <p className="text-gray-500 text-lg">📦 Chưa có đơn hàng nào đã xuất kho</p>
              <p className="text-gray-400 text-sm mt-2">Các đơn đã xuất sẽ hiển thị ở đây</p>
            </>
          )}
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
                    {activeTab === 'pending' ? (
                      <span className="inline-block px-3 py-1 bg-yellow-100 text-yellow-800 rounded-full text-sm font-medium">
                        ⏳ Chờ xuất kho
                      </span>
                    ) : (
                      <span className="inline-block px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-medium">
                        ✅ Đã xuất kho
                      </span>
                    )}
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
                    {activeTab === 'pending' ? '📦 Xem chi tiết & Xuất kho' : '👁️ Xem chi tiết'}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Summary */}
      {orders.length > 0 && (
        <div className={`mt-6 rounded-lg p-4 ${
          activeTab === 'pending' ? 'bg-blue-50' : 'bg-green-50'
        }`}>
          <div className="flex justify-between items-center">
            <p className={`text-lg font-semibold ${
              activeTab === 'pending' ? 'text-blue-900' : 'text-green-900'
            }`}>
              {activeTab === 'pending' 
                ? `Tổng cộng: ${orders.length} đơn hàng cần xuất kho`
                : `Tổng cộng: ${orders.length} đơn hàng đã xuất kho`
              }
            </p>
            <button
              onClick={fetchOrders}
              className={`px-4 py-2 bg-white rounded-lg transition-colors ${
                activeTab === 'pending' 
                  ? 'text-blue-600 hover:bg-blue-100' 
                  : 'text-green-600 hover:bg-green-100'
              }`}
            >
              🔄 Làm mới
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
