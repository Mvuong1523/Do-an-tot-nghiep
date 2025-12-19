'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';
import toast from 'react-hot-toast';

interface ProductSpecification {
  key: string;
  value: string;
}

interface OrderItem {
  itemId: number;
  productId: number;
  productName: string;
  productSku: string;
  productImage: string;
  productImages?: string[];
  quantity: number;
  price: number;
  subtotal: number;
  reserved: boolean;
  exported: boolean;
  serialNumber?: string;
  // Thông tin chi tiết sản phẩm
  brand?: string;
  manufacturer?: string;
  category?: string;
  description?: string;
  specifications?: ProductSpecification[];
  warrantyPeriod?: number;
  weight?: number;
  dimensions?: string;
}

interface Order {
  orderId: number;
  orderCode: string;
  status: string;
  paymentStatus: string;
  paymentMethod: string;
  customerName: string;
  customerPhone: string;
  customerEmail: string;
  shippingAddress: string;
  province: string;
  district: string;
  ward: string;
  wardName: string;
  address: string;
  note?: string;
  items: OrderItem[];
  subtotal: number;
  shippingFee: number;
  discount: number;
  total: number;
  createdAt: string;
  confirmedAt: string;
  ghnOrderCode?: string;
  ghnShippingStatus?: string;
  ghnExpectedDeliveryTime?: string;
}

export default function WarehouseOrderDetailPage() {
  const router = useRouter();
  const params = useParams();
  const orderId = params.id as string;
  const { user, isAuthenticated } = useAuthStore();

  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showExportModal, setShowExportModal] = useState(false);
  const [exportData, setExportData] = useState<{[sku: string]: string[]}>({});
  const [exporting, setExporting] = useState(false);
  const [wardDisplayName, setWardDisplayName] = useState<string>('');

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
        const orderData = result.data;
        setOrder(orderData);
        
        // If wardName is empty but ward code exists, fetch ward name from GHN
        if (orderData.ward && !orderData.wardName) {
          fetchWardName(orderData.ward);
        } else if (orderData.wardName) {
          setWardDisplayName(orderData.wardName);
        }
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

  const fetchWardName = async (wardCode: string) => {
    try {
      // Try to get ward name by calling GHN API through our backend
      // For now, just display the code
      setWardDisplayName(wardCode);
      console.log('Ward code:', wardCode, '- Need to fetch name from GHN');
    } catch (err) {
      console.error('Error fetching ward name:', err);
      setWardDisplayName(wardCode);
    }
  };

  const handleCreateExportOrder = () => {
    // Initialize export data with empty arrays for each SKU
    const initialData: {[sku: string]: string[]} = {};
    order?.items.forEach(item => {
      initialData[item.productSku] = Array(item.quantity).fill('');
    });
    setExportData(initialData);
    setShowExportModal(true);
  };

  const handleSerialChange = (sku: string, index: number, value: string) => {
    setExportData(prev => ({
      ...prev,
      [sku]: prev[sku].map((serial, i) => i === index ? value : serial)
    }));
  };

  const handleSubmitExport = async () => {
    if (!order) return;

    // Validate all serials are filled
    for (const [sku, serials] of Object.entries(exportData)) {
      if (serials.some(s => !s.trim())) {
        toast.error(`Vui lòng nhập đầy đủ serial cho SKU: ${sku}`);
        return;
      }
    }

    setExporting(true);
    try {
      const token = localStorage.getItem('token');
      
      // Build items array
      const items = Object.entries(exportData).map(([sku, serials]) => ({
        productSku: sku,
        serialNumbers: serials
      }));

      const response = await fetch('http://localhost:8080/api/inventory/export-for-sale', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({
          orderId: order.orderId,
          reason: 'Xuất kho bán hàng - Giao cho khách',
          note: order.note || '',
          items: items
        })
      });

      const result = await response.json();
      
      if (result.success) {
        toast.success('Xuất kho thành công! Đơn GHN đã được tạo tự động.');
        setShowExportModal(false);
        // Refresh order data
        fetchOrderDetail();
      } else {
        toast.error(result.message || 'Xuất kho thất bại');
      }
    } catch (err) {
      console.error('Error exporting:', err);
      toast.error('Lỗi kết nối server');
    } finally {
      setExporting(false);
    }
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
          {/* Order Info Card */}
          <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg shadow-lg p-6 mb-6">
            <h2 className="text-2xl font-bold mb-4 text-blue-900">📋 Thông tin đơn hàng</h2>
            <div className="grid grid-cols-2 gap-4 bg-white rounded-lg p-4">
              <div>
                <p className="text-sm text-gray-600">Mã đơn hàng</p>
                <p className="font-bold text-lg text-blue-600">{order.orderCode}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Trạng thái</p>
                <p className="font-semibold text-yellow-600">{order.status}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Thanh toán</p>
                <p className="font-semibold">{order.paymentMethod}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Trạng thái TT</p>
                <p className="font-semibold">{order.paymentStatus}</p>
              </div>
              <div className="col-span-2">
                <p className="text-sm text-gray-600">Thời gian tạo</p>
                <p className="font-medium">{new Date(order.createdAt).toLocaleString('vi-VN')}</p>
              </div>
              <div className="col-span-2">
                <p className="text-sm text-gray-600">Thời gian xác nhận</p>
                <p className="font-medium">{new Date(order.confirmedAt).toLocaleString('vi-VN')}</p>
              </div>
            </div>
          </div>

          {/* Customer Info */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
              <span>👤</span>
              <span>Thông tin khách hàng</span>
            </h2>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-gray-50 p-3 rounded-lg">
                  <p className="text-xs text-gray-600 mb-1">Họ và tên</p>
                  <p className="font-semibold text-gray-900">{order.customerName}</p>
                </div>
                <div className="bg-gray-50 p-3 rounded-lg">
                  <p className="text-xs text-gray-600 mb-1">Số điện thoại</p>
                  <p className="font-semibold text-blue-600">{order.customerPhone}</p>
                </div>
              </div>
              <div className="bg-gray-50 p-3 rounded-lg">
                <p className="text-xs text-gray-600 mb-1">Email</p>
                <p className="font-medium text-gray-900">{order.customerEmail}</p>
              </div>
              
              <div className="border-t pt-4">
                <h3 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                  <span>📍</span>
                  <span>Địa chỉ giao hàng chi tiết</span>
                </h3>
                <div className="space-y-2">
                  <div className="flex">
                    <span className="text-sm text-gray-600 w-32">Địa chỉ:</span>
                    <span className="text-sm font-medium text-gray-900">{order.address}</span>
                  </div>
                  <div className="flex">
                    <span className="text-sm text-gray-600 w-32">Phường/Xã:</span>
                    <span className="text-sm font-medium text-gray-900">
                      {order.wardName || wardDisplayName || 'Chưa có thông tin'}
                    </span>
                    {!order.wardName && order.ward && (
                      <span className="ml-2 text-xs text-orange-500">(Mã: {order.ward} - Cần cập nhật)</span>
                    )}
                  </div>
                  <div className="flex">
                    <span className="text-sm text-gray-600 w-32">Quận/Huyện:</span>
                    <span className="text-sm font-medium text-gray-900">{order.district}</span>
                  </div>
                  <div className="flex">
                    <span className="text-sm text-gray-600 w-32">Tỉnh/TP:</span>
                    <span className="text-sm font-medium text-gray-900">{order.province}</span>
                  </div>
                  <div className="mt-3 p-3 bg-blue-50 rounded-lg">
                    <p className="text-sm font-medium text-blue-900">
                      📦 Địa chỉ đầy đủ:
                    </p>
                    <p className="text-sm text-blue-800 mt-1">
                      {order.shippingAddress}
                    </p>
                  </div>
                </div>
              </div>

              {order.note && (
                <div className="bg-orange-50 border-l-4 border-orange-400 p-4 rounded">
                  <p className="text-sm font-semibold text-orange-900 mb-1">⚠️ Ghi chú từ khách hàng:</p>
                  <p className="text-sm text-orange-800">{order.note}</p>
                </div>
              )}
            </div>
          </div>

          {/* Items - FULL DETAILS */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">
              📦 Sản phẩm cần xuất ({order.items.length})
            </h2>
            <div className="space-y-6">
              {order.items.map((item) => (
                <div key={item.itemId} className="border-2 rounded-lg p-6 hover:border-blue-300 transition-colors">
                  {/* Header with Image and Basic Info */}
                  <div className="flex gap-6 mb-4">
                    <div className="flex-shrink-0">
                      {item.productImage && (
                        <img
                          src={item.productImage}
                          alt={item.productName}
                          className="w-32 h-32 object-cover rounded-lg border-2 border-gray-200"
                        />
                      )}
                      {item.productImages && item.productImages.length > 1 && (
                        <div className="flex gap-1 mt-2">
                          {item.productImages.slice(1, 4).map((img, idx) => (
                            <img
                              key={idx}
                              src={img}
                              alt={`${item.productName} ${idx + 2}`}
                              className="w-10 h-10 object-cover rounded border"
                            />
                          ))}
                        </div>
                      )}
                    </div>
                    
                    <div className="flex-1">
                      <div className="flex justify-between items-start mb-3">
                        <div>
                          <h3 className="font-bold text-xl text-gray-900 mb-1">{item.productName}</h3>
                          <div className="flex gap-4 text-sm">
                            <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full font-medium">
                              SKU: {item.productSku}
                            </span>
                            {item.category && (
                              <span className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full">
                                {item.category}
                              </span>
                            )}
                          </div>
                        </div>
                        <div className="text-right">
                          <p className="text-3xl font-bold text-blue-600 mb-1">
                            x{item.quantity}
                          </p>
                          <p className="text-lg font-semibold text-gray-900">
                            {item.subtotal.toLocaleString('vi-VN')} ₫
                          </p>
                          <p className="text-sm text-gray-500">
                            Đơn giá: {item.price.toLocaleString('vi-VN')} ₫
                          </p>
                        </div>
                      </div>

                      {/* Manufacturer & Brand */}
                      <div className="grid grid-cols-2 gap-4 mb-3">
                        {item.manufacturer && (
                          <div className="flex items-center gap-2">
                            <span className="text-gray-600 text-sm">🏭 Nhà sản xuất:</span>
                            <span className="font-semibold text-gray-900">{item.manufacturer}</span>
                          </div>
                        )}
                        {item.brand && (
                          <div className="flex items-center gap-2">
                            <span className="text-gray-600 text-sm">🏷️ Thương hiệu:</span>
                            <span className="font-semibold text-gray-900">{item.brand}</span>
                          </div>
                        )}
                      </div>

                      {/* Description */}
                      {item.description && (
                        <div className="mb-3">
                          <p className="text-sm text-gray-700 line-clamp-2">{item.description}</p>
                        </div>
                      )}

                      {/* Physical Info */}
                      <div className="flex gap-6 text-sm text-gray-600 mb-3">
                        {item.weight && (
                          <div className="flex items-center gap-1">
                            <span>⚖️ Khối lượng:</span>
                            <span className="font-medium">{item.weight}g</span>
                          </div>
                        )}
                        {item.dimensions && (
                          <div className="flex items-center gap-1">
                            <span>📏 Kích thước:</span>
                            <span className="font-medium">{item.dimensions}</span>
                          </div>
                        )}
                        {item.warrantyPeriod && (
                          <div className="flex items-center gap-1">
                            <span>🛡️ Bảo hành:</span>
                            <span className="font-medium">{item.warrantyPeriod} tháng</span>
                          </div>
                        )}
                      </div>

                      {/* Status Badges */}
                      <div className="flex gap-2">
                        {item.reserved && (
                          <span className="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-medium">
                            ✓ Đã giữ hàng
                          </span>
                        )}
                        {item.exported && (
                          <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-medium">
                            ✓ Đã xuất kho
                          </span>
                        )}
                        {item.serialNumber && (
                          <span className="px-3 py-1 bg-purple-100 text-purple-800 rounded-full text-sm font-medium">
                            Serial: {item.serialNumber}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Technical Specifications */}
                  {item.specifications && item.specifications.length > 0 && (
                    <div className="mt-4 pt-4 border-t">
                      <h4 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                        <span>⚙️</span>
                        <span>Thông số kỹ thuật</span>
                      </h4>
                      <div className="grid grid-cols-2 gap-x-6 gap-y-2">
                        {item.specifications.map((spec, idx) => (
                          <div key={idx} className="flex justify-between py-2 border-b border-gray-100">
                            <span className="text-sm text-gray-600">{spec.key}:</span>
                            <span className="text-sm font-medium text-gray-900">{spec.value}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
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
              {!order.ghnOrderCode && order.status === 'CONFIRMED' ? (
                // Chưa xuất kho - Hiển thị nút xuất
                <>
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
                </>
              ) : (
                // Đã xuất kho - Hiển thị thông báo
                <div className="bg-green-50 border-2 border-green-200 rounded-lg p-4">
                  <div className="flex items-center gap-3 mb-3">
                    <span className="text-3xl">✅</span>
                    <div>
                      <p className="font-bold text-green-900">Đã xuất kho thành công</p>
                      <p className="text-sm text-green-700">Hàng đã chuẩn bị xong</p>
                    </div>
                  </div>
                  <div className="bg-white rounded p-3 mb-3">
                    <p className="text-xs text-gray-600 mb-1">Trạng thái</p>
                    <p className="font-semibold text-orange-600">
                      🚚 Đợi tài xế đến lấy hàng
                    </p>
                  </div>
                  {order.ghnOrderCode && (
                    <div className="bg-white rounded p-3">
                      <p className="text-xs text-gray-600 mb-1">Mã vận đơn GHN</p>
                      <p className="font-bold text-green-600">{order.ghnOrderCode}</p>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          {/* GHN Info */}
          {order.ghnOrderCode && (
            <div className="bg-green-50 rounded-lg shadow p-6 border-2 border-green-200">
              <h3 className="font-bold text-green-900 mb-3 flex items-center gap-2">
                <span>🚚</span>
                <span>Thông tin GHN</span>
              </h3>
              <div className="space-y-3">
                <div className="bg-white p-3 rounded">
                  <p className="text-xs text-gray-600 mb-1">Mã vận đơn GHN</p>
                  <p className="font-bold text-green-600">{order.ghnOrderCode}</p>
                </div>
                {order.ghnShippingStatus && (
                  <div className="bg-white p-3 rounded">
                    <p className="text-xs text-gray-600 mb-1">Trạng thái vận chuyển</p>
                    <p className="font-semibold text-gray-900">{order.ghnShippingStatus}</p>
                  </div>
                )}
                {order.ghnExpectedDeliveryTime && (
                  <div className="bg-white p-3 rounded">
                    <p className="text-xs text-gray-600 mb-1">Dự kiến giao hàng</p>
                    <p className="font-medium text-gray-900">
                      {new Date(order.ghnExpectedDeliveryTime).toLocaleString('vi-VN')}
                    </p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Status Timeline */}
          <div className={order.ghnOrderCode ? "bg-green-50 rounded-lg p-4 border-2 border-green-200" : "bg-blue-50 rounded-lg p-4"}>
            <h3 className="font-semibold text-gray-900 mb-3">📊 Tiến trình xử lý</h3>
            <div className="space-y-3 text-sm">
              {/* Bước 1: Đã giữ hàng */}
              <div className="flex items-center gap-3 p-3 bg-white rounded-lg border-l-4 border-green-500">
                <span className="text-green-600 text-2xl">✓</span>
                <div className="flex-1">
                  <p className="font-semibold text-gray-900">Đã giữ hàng</p>
                  <p className="text-xs text-gray-600">Hàng đã được reserve</p>
                </div>
              </div>
              
              {/* Bước 2: Xuất kho */}
              <div className={`flex items-center gap-3 p-3 bg-white rounded-lg border-l-4 ${
                order.ghnOrderCode ? 'border-green-500' : 'border-gray-300'
              }`}>
                <span className={order.ghnOrderCode ? "text-green-600 text-2xl" : "text-gray-400 text-2xl"}>
                  {order.ghnOrderCode ? "✓" : "○"}
                </span>
                <div className="flex-1">
                  <p className={`font-semibold ${order.ghnOrderCode ? 'text-gray-900' : 'text-gray-600'}`}>
                    {order.ghnOrderCode ? "Đã xuất kho" : "Chưa xuất kho"}
                  </p>
                  <p className="text-xs text-gray-600">
                    {order.ghnOrderCode ? "Hàng đã chuẩn bị xong" : "Đang chờ xuất kho"}
                  </p>
                </div>
              </div>
              
              {/* Bước 3: Đợi tài xế */}
              {order.ghnOrderCode && (
                <div className="flex items-center gap-3 p-3 bg-orange-50 rounded-lg border-l-4 border-orange-500">
                  <span className="text-orange-600 text-2xl">🚚</span>
                  <div className="flex-1">
                    <p className="font-bold text-orange-900">Đợi tài xế đến lấy hàng</p>
                    <p className="text-xs text-orange-700">Đơn GHN đã được tạo</p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Export Modal */}
      {showExportModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-5xl w-full max-h-[90vh] overflow-y-auto shadow-2xl">
            <div className="p-6 border-b sticky top-0 bg-gradient-to-r from-blue-600 to-blue-700 text-white z-10">
              <div className="flex justify-between items-center">
                <div>
                  <h2 className="text-2xl font-bold flex items-center gap-2">
                    <span>📦</span>
                    <span>Tạo phiếu xuất kho bán hàng</span>
                  </h2>
                  <p className="text-sm text-blue-100 mt-1">
                    Đơn hàng: <span className="font-semibold">{order?.orderCode}</span>
                  </p>
                </div>
                <button
                  onClick={() => setShowExportModal(false)}
                  className="text-white hover:bg-white hover:bg-opacity-20 rounded-full w-10 h-10 flex items-center justify-center text-2xl transition-colors"
                >
                  ×
                </button>
              </div>
              <div className="mt-4 bg-white bg-opacity-20 rounded-lg p-3">
                <p className="text-sm text-white flex items-start gap-2">
                  <span className="text-yellow-300">⚠️</span>
                  <span>
                    <strong>Lưu ý quan trọng:</strong> Nhập serial number cho từng sản phẩm. 
                    Sau khi xuất kho, hệ thống sẽ tự động tạo đơn GHN và <strong>không thể hoàn tác</strong>.
                  </span>
                </p>
              </div>
            </div>

            <div className="p-6 space-y-6">
              {order?.items.map((item) => (
                <div key={item.itemId} className="border-2 border-gray-200 rounded-lg overflow-hidden">
                  {/* Header với ảnh và thông tin cơ bản */}
                  <div className="bg-gradient-to-r from-blue-50 to-blue-100 p-4">
                    <div className="flex items-start gap-4">
                      <div className="flex-shrink-0">
                        {item.productImage && (
                          <img
                            src={item.productImage}
                            alt={item.productName}
                            className="w-24 h-24 object-cover rounded-lg border-2 border-white shadow-md"
                          />
                        )}
                        {item.productImages && item.productImages.length > 1 && (
                          <div className="flex gap-1 mt-2">
                            {item.productImages.slice(1, 4).map((img, idx) => (
                              <img
                                key={idx}
                                src={img}
                                alt={`${item.productName} ${idx + 2}`}
                                className="w-7 h-7 object-cover rounded border border-white"
                              />
                            ))}
                          </div>
                        )}
                      </div>
                      
                      <div className="flex-1">
                        <h3 className="font-bold text-lg text-gray-900 mb-2">{item.productName}</h3>
                        <div className="grid grid-cols-2 gap-3 text-sm">
                          <div className="flex items-center gap-2">
                            <span className="text-gray-600">📦 SKU:</span>
                            <span className="font-semibold text-blue-600">{item.productSku}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="text-gray-600">🔢 Số lượng:</span>
                            <span className="font-bold text-lg text-blue-600">{item.quantity}</span>
                          </div>
                          {item.category && (
                            <div className="flex items-center gap-2">
                              <span className="text-gray-600">📂 Danh mục:</span>
                              <span className="font-medium">{item.category}</span>
                            </div>
                          )}
                          {item.brand && (
                            <div className="flex items-center gap-2">
                              <span className="text-gray-600">🏷️ Thương hiệu:</span>
                              <span className="font-medium">{item.brand}</span>
                            </div>
                          )}
                        </div>
                      </div>

                      <div className="text-right">
                        <p className="text-sm text-gray-600 mb-1">Đơn giá</p>
                        <p className="text-lg font-bold text-gray-900">
                          {item.price.toLocaleString('vi-VN')} ₫
                        </p>
                        <p className="text-sm text-gray-600 mt-2">Thành tiền</p>
                        <p className="text-xl font-bold text-blue-600">
                          {item.subtotal.toLocaleString('vi-VN')} ₫
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Thông tin chi tiết sản phẩm - Luôn hiển thị */}
                  {(item.manufacturer || item.weight || item.dimensions || item.warrantyPeriod || 
                    (item.specifications && item.specifications.length > 0) || item.description) && (
                    <div className="p-4 bg-gray-50 border-t border-b">
                      <h4 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                        <span>📋</span>
                        <span>Thông tin sản phẩm (Data gửi GHN)</span>
                      </h4>
                      
                      {/* Grid thông tin cơ bản */}
                      {(item.manufacturer || item.weight || item.dimensions || item.warrantyPeriod) && (
                        <div className="grid grid-cols-2 gap-4 mb-4">
                          {item.manufacturer && (
                            <div className="bg-white p-3 rounded-lg">
                              <p className="text-xs text-gray-600 mb-1">🏭 Nhà sản xuất</p>
                              <p className="font-semibold text-gray-900">{item.manufacturer}</p>
                            </div>
                          )}
                          {item.weight && (
                            <div className="bg-white p-3 rounded-lg">
                              <p className="text-xs text-gray-600 mb-1">⚖️ Khối lượng</p>
                              <p className="font-semibold text-gray-900">{item.weight}g</p>
                            </div>
                          )}
                          {item.dimensions && (
                            <div className="bg-white p-3 rounded-lg">
                              <p className="text-xs text-gray-600 mb-1">📏 Kích thước</p>
                              <p className="font-semibold text-gray-900">{item.dimensions}</p>
                            </div>
                          )}
                          {item.warrantyPeriod && (
                            <div className="bg-white p-3 rounded-lg">
                              <p className="text-xs text-gray-600 mb-1">🛡️ Bảo hành</p>
                              <p className="font-semibold text-gray-900">{item.warrantyPeriod} tháng</p>
                            </div>
                          )}
                        </div>
                      )}

                      {/* Thông số kỹ thuật */}
                      {item.specifications && item.specifications.length > 0 && (
                        <div className="mb-4">
                          <p className="text-sm font-semibold text-gray-700 mb-2">⚙️ Thông số kỹ thuật:</p>
                          <div className="grid grid-cols-2 gap-2">
                            {item.specifications.slice(0, 6).map((spec, idx) => (
                              <div key={idx} className="bg-white px-3 py-2 rounded text-xs">
                                <span className="text-gray-600">{spec.key}:</span>{' '}
                                <span className="font-medium text-gray-900">{spec.value}</span>
                              </div>
                            ))}
                          </div>
                          {item.specifications.length > 6 && (
                            <p className="text-xs text-gray-500 mt-2">
                              ... và {item.specifications.length - 6} thông số khác
                            </p>
                          )}
                        </div>
                      )}

                      {/* Mô tả */}
                      {item.description && (
                        <div className="bg-white p-3 rounded-lg">
                          <p className="text-xs text-gray-600 mb-1">📝 Mô tả sản phẩm</p>
                          <p className="text-sm text-gray-700 line-clamp-2">{item.description}</p>
                        </div>
                      )}
                    </div>
                  )}

                  {/* Nhập Serial Numbers */}
                  <div className="p-4 bg-white">
                    <div className="flex items-center justify-between mb-3">
                      <label className="text-sm font-bold text-gray-900 flex items-center gap-2">
                        <span>🔑</span>
                        <span>Nhập Serial Numbers ({item.quantity} sản phẩm):</span>
                      </label>
                      <span className="text-xs text-orange-600 font-medium">
                        ⚠️ Bắt buộc nhập đầy đủ
                      </span>
                    </div>
                    <div className="space-y-2">
                      {Array.from({ length: item.quantity }).map((_, index) => (
                        <div key={index} className="flex items-center gap-3">
                          <span className="text-sm font-semibold text-gray-700 w-12 text-center bg-gray-100 py-2 rounded">
                            #{index + 1}
                          </span>
                          <input
                            type="text"
                            value={exportData[item.productSku]?.[index] || ''}
                            onChange={(e) => handleSerialChange(item.productSku, index, e.target.value)}
                            placeholder={`Nhập serial number ${index + 1} cho ${item.productName}`}
                            className="flex-1 px-4 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                          />
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div className="p-6 border-t bg-gradient-to-r from-gray-50 to-gray-100 sticky bottom-0 shadow-lg">
              {/* Thông tin giao hàng */}
              <div className="mb-4 p-4 bg-white rounded-lg border-2 border-blue-200">
                <h4 className="font-bold text-gray-900 mb-3 flex items-center gap-2">
                  <span>🚚</span>
                  <span>Thông tin giao hàng (Data gửi GHN)</span>
                </h4>
                <div className="grid grid-cols-2 gap-3 text-sm">
                  <div className="flex items-start gap-2">
                    <span className="text-gray-600">👤 Người nhận:</span>
                    <span className="font-semibold">{order?.customerName}</span>
                  </div>
                  <div className="flex items-start gap-2">
                    <span className="text-gray-600">📞 SĐT:</span>
                    <span className="font-semibold">{order?.customerPhone}</span>
                  </div>
                  <div className="col-span-2 flex items-start gap-2">
                    <span className="text-gray-600">📍 Địa chỉ:</span>
                    <span className="font-medium">{order?.shippingAddress}</span>
                  </div>
                  <div className="flex items-start gap-2">
                    <span className="text-gray-600">💰 COD:</span>
                    <span className="font-bold text-green-600">
                      {order?.paymentMethod === 'COD' 
                        ? order?.total.toLocaleString('vi-VN') + ' ₫' 
                        : '0 ₫ (Đã thanh toán)'}
                    </span>
                  </div>
                  <div className="flex items-start gap-2">
                    <span className="text-gray-600">📦 Tổng SL:</span>
                    <span className="font-bold text-blue-600">
                      {order?.items.reduce((sum, item) => sum + item.quantity, 0)} sản phẩm
                    </span>
                  </div>
                </div>
              </div>

              {/* Action buttons */}
              <div className="flex gap-3 justify-end">
                <button
                  onClick={() => setShowExportModal(false)}
                  disabled={exporting}
                  className="px-8 py-3 border-2 border-gray-300 rounded-lg hover:bg-gray-100 transition-colors font-medium text-gray-700"
                >
                  ❌ Hủy bỏ
                </button>
                <button
                  onClick={handleSubmitExport}
                  disabled={exporting}
                  className="px-8 py-3 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-lg hover:from-blue-700 hover:to-blue-800 transition-all font-bold shadow-lg disabled:from-gray-400 disabled:to-gray-500 flex items-center gap-2"
                >
                  {exporting ? (
                    <>
                      <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                      </svg>
                      <span>Đang xử lý...</span>
                    </>
                  ) : (
                    <>
                      <span>✓</span>
                      <span>Xác nhận xuất kho & Tạo đơn GHN</span>
                    </>
                  )}
                </button>
              </div>
              <div className="mt-3 p-3 bg-orange-50 border border-orange-200 rounded-lg">
                <p className="text-xs text-orange-800 text-center flex items-center justify-center gap-2">
                  <span className="text-lg">⚠️</span>
                  <span>
                    <strong>Cảnh báo:</strong> Sau khi xuất kho, đơn GHN sẽ được tạo tự động và <strong>KHÔNG THỂ HOÀN TÁC</strong>. 
                    Vui lòng kiểm tra kỹ thông tin trước khi xác nhận.
                  </span>
                </p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
