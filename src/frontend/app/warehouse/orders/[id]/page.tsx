'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';
import toast from 'react-hot-toast';

interface OrderItem {
  itemId: number;
  productId: number;
  productName: string;
  productSku: string;
  productImage: string;
  quantity: number;
  price: number;
  subtotal: number;
  reserved: boolean;
  exported: boolean;
  serialNumber?: string;
}

interface Order {
  orderId: number;
  orderCode: string;
  status: string;
  paymentStatus: string;
  customerName: string;
  customerPhone: string;
  customerEmail: string;
  shippingAddress: string;
  note?: string;
  items: OrderItem[];
  subtotal: number;
  shippingFee: number;
  discount: number;
  total: number;
  createdAt: string;
  confirmedAt: string;
}

export default function WarehouseOrderDetailPage() {
  const router = useRouter();
  const params = useParams();
  const orderId = params.id as string;
  const { user, isAuthenticated } = useAuthStore();

  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

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

    if (orderId) {
      fetchOrderDetail();
    }
  }, [isAuthenticated, user, router, orderId]);

  const fetchOrderDetail = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      
      const response = await fetch(`http://localhost:8080/api/inventory/orders/${orderId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      const result = await response.json();
      
      if (result.success) {
        setOrder(result.data);
      } else {
        setError(result.message || 'Không thể tải thông tin đơn hàng');
      }
    } catch (err) {
      console.error('Error fetching order:', err);
      setError('Lỗi kết nối server');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateExportOrder = () => {
    // TODO: Implement tạo phiếu xuất kho
    alert('Chức năng tạo phiếu xuất kho sẽ được implement sau');
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-lg">Đang tải...</div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error || 'Không tìm thấy đơn hàng'}
        </div>
        <button
          onClick={() => router.back()}
          className="mt-4 px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
        >
          ← Quay lại
        </button>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-6">
        <button
          onClick={() => router.back()}
          className="mb-4 text-blue-600 hover:text-blue-800 flex items-center gap-2"
        >
          ← Quay lại danh sách
        </button>
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold mb-2">Chi tiết đơn hàng</h1>
            <p className="text-xl text-blue-600 font-semibold">{order.orderCode}</p>
          </div>
          <div className="text-right">
            <span className="inline-block px-4 py-2 bg-yellow-100 text-yellow-800 rounded-full font-medium">
              {order.status}
            </span>
            <p className="text-sm text-gray-500 mt-2">
              Xác nhận: {new Date(order.confirmedAt).toLocaleString('vi-VN')}
            </p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Customer Info */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Thông tin khách hàng</h2>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-600">Họ tên</p>
                <p className="font-medium">{order.customerName}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Số điện thoại</p>
                <p className="font-medium">{order.customerPhone}</p>
              </div>
              <div className="col-span-2">
                <p className="text-sm text-gray-600">Email</p>
                <p className="font-medium">{order.customerEmail}</p>
              </div>
              <div className="col-span-2">
                <p className="text-sm text-gray-600">Địa chỉ giao hàng</p>
                <p className="font-medium">{order.shippingAddress}</p>
              </div>
              {order.note && (
                <div className="col-span-2">
                  <p className="text-sm text-gray-600">Ghi chú</p>
                  <p className="font-medium text-orange-600">{order.note}</p>
                </div>
              )}
            </div>
          </div>

          {/* Items */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">
              📦 Sản phẩm cần xuất ({order.items.length})
            </h2>
            <div className="space-y-4">
              {order.items.map((item) => (
                <div key={item.itemId} className="border rounded-lg p-4">
                  <div className="flex gap-4">
                    {item.productImage && (
                      <img
                        src={item.productImage}
                        alt={item.productName}
                        className="w-20 h-20 object-cover rounded"
                      />
                    )}
                    <div className="flex-1">
                      <h3 className="font-semibold text-lg">{item.productName}</h3>
                      <p className="text-sm text-gray-600">SKU: {item.productSku}</p>
                      <p className="text-sm text-gray-600">
                        Giá: {item.price.toLocaleString('vi-VN')} ₫
                      </p>
                      {item.serialNumber && (
                        <p className="text-sm text-blue-600 font-medium mt-1">
                          Serial: {item.serialNumber}
                        </p>
                      )}
                    </div>
                    <div className="text-right">
                      <p className="text-2xl font-bold text-blue-600 mb-2">
                        x{item.quantity}
                      </p>
                      <p className="text-sm text-gray-600">
                        {item.subtotal.toLocaleString('vi-VN')} ₫
                      </p>
                      <div className="mt-2 space-y-1">
                        {item.reserved && (
                          <span className="block px-2 py-1 bg-green-100 text-green-800 rounded text-xs">
                            ✓ Đã giữ hàng
                          </span>
                        )}
                        {item.exported && (
                          <span className="block px-2 py-1 bg-blue-100 text-blue-800 rounded text-xs">
                            ✓ Đã xuất kho
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Summary */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Tổng kết</h2>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-gray-600">Tạm tính</span>
                <span className="font-medium">{order.subtotal.toLocaleString('vi-VN')} ₫</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Phí vận chuyển</span>
                <span className="font-medium">{order.shippingFee.toLocaleString('vi-VN')} ₫</span>
              </div>
              {order.discount > 0 && (
                <div className="flex justify-between text-red-600">
                  <span>Giảm giá</span>
                  <span className="font-medium">-{order.discount.toLocaleString('vi-VN')} ₫</span>
                </div>
              )}
              <div className="border-t pt-3 flex justify-between text-lg font-bold">
                <span>Tổng cộng</span>
                <span className="text-blue-600">{order.total.toLocaleString('vi-VN')} ₫</span>
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Thao tác</h2>
            <div className="space-y-3">
              <button
                onClick={handleCreateExportOrder}
                className="w-full px-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
              >
                📦 Tạo phiếu xuất kho
              </button>
              <button
                onClick={() => window.print()}
                className="w-full px-4 py-3 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors font-medium"
              >
                🖨️ In phiếu chuẩn bị
              </button>
            </div>
          </div>

          {/* Status */}
          <div className="bg-blue-50 rounded-lg p-4">
            <h3 className="font-semibold text-blue-900 mb-2">Trạng thái xuất kho</h3>
            <div className="space-y-2 text-sm">
              <div className="flex items-center gap-2">
                <span className="text-green-600">✓</span>
                <span>Đã giữ hàng</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-gray-400">○</span>
                <span className="text-gray-600">Chưa tạo phiếu xuất</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-gray-400">○</span>
                <span className="text-gray-600">Chưa xuất kho</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
