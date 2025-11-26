'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'
import { FiPlus, FiTrash2, FiSave, FiX } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'
import { inventoryApi } from '@/lib/api'

interface TransactionItem {
  sku: string
  productName: string
  quantity: number
  price: number
  warrantyMonths?: number
  techSpecs?: string
  note?: string
}

export default function CreateTransactionPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { user, isAuthenticated } = useAuthStore()
  
  const type = (searchParams.get('type') || 'IMPORT') as 'IMPORT' | 'EXPORT'
  
  const [formData, setFormData] = useState({
    note: '',
    supplierName: '',
    supplierContactName: '',
    supplierTaxCode: '',
    supplierPhone: '',
    supplierEmail: '',
    supplierAddress: '',
    supplierBankAccount: '',
    supplierPaymentTerm: '',
    invoiceNumber: ''
  })
  
  const [items, setItems] = useState<TransactionItem[]>([])
  const [showProductModal, setShowProductModal] = useState(false)
  const [newItem, setNewItem] = useState({
    sku: '',
    productName: '',
    quantity: 1,
    price: 0,
    warrantyMonths: 12,
    techSpecs: '',
    note: ''
  })
  const [techSpecRows, setTechSpecRows] = useState<Array<{key: string, value: string}>>([
    { key: '', value: '' }
  ])
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [suppliers, setSuppliers] = useState<any[]>([])
  const [selectedSupplierId, setSelectedSupplierId] = useState<string>('')

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    // Check if user is admin or warehouse
    if (user?.role !== 'ADMIN' && user?.role !== 'WAREHOUSE') {
      toast.error('Chỉ quản trị viên và nhân viên kho mới có quyền truy cập')
      router.push('/')
      return
    }

    // Load suppliers
    loadSuppliers()
  }, [isAuthenticated, user, router])

  const loadSuppliers = async () => {
    try {
      const response = await inventoryApi.getSuppliers()
      console.log('Suppliers response:', response)
      if (response.success) {
        setSuppliers(response.data || [])
        console.log('Loaded suppliers:', response.data?.length || 0)
      }
    } catch (error) {
      console.error('Error loading suppliers:', error)
      toast.error('Không thể tải danh sách nhà cung cấp')
    }
  }

  const handleSupplierChange = (supplierId: string) => {
    setSelectedSupplierId(supplierId)
    
    if (supplierId) {
      const supplier = suppliers.find(s => s.id.toString() === supplierId)
      console.log('Selected supplier:', supplier)
      
      if (supplier) {
        const newFormData = {
          ...formData,
          supplierName: supplier.name || '',
          supplierContactName: supplier.contactName || supplier.contactPerson || '',
          supplierTaxCode: supplier.taxCode || '',
          supplierPhone: supplier.phone || '',
          supplierEmail: supplier.email || '',
          supplierAddress: supplier.address || '',
          supplierBankAccount: supplier.bankAccount || '',
          supplierPaymentTerm: supplier.paymentTerm || ''
        }
        console.log('New form data:', newFormData)
        setFormData(newFormData)
        toast.success(`Đã điền thông tin: ${supplier.name}`)
      } else {
        toast.error('Không tìm thấy nhà cung cấp')
      }
    } else {
      // Clear form if "Nhập mới" is selected
      setFormData({
        ...formData,
        supplierName: '',
        supplierContactName: '',
        supplierTaxCode: '',
        supplierPhone: '',
        supplierEmail: '',
        supplierAddress: '',
        supplierBankAccount: '',
        supplierPaymentTerm: ''
      })
    }
  }

  // Convert tech spec rows to JSON
  const techSpecsToJson = (): string => {
    const specs: Record<string, string> = {}
    
    techSpecRows.forEach(row => {
      if (row.key.trim() && row.value.trim()) {
        // Giữ nguyên key, chỉ trim và lowercase
        const key = row.key.trim().toLowerCase()
        specs[key] = row.value.trim()
      }
    })
    
    return Object.keys(specs).length > 0 ? JSON.stringify(specs) : '{}'
  }

  const addTechSpecRow = () => {
    setTechSpecRows([...techSpecRows, { key: '', value: '' }])
  }

  const removeTechSpecRow = (index: number) => {
    if (techSpecRows.length > 1) {
      setTechSpecRows(techSpecRows.filter((_, i) => i !== index))
    }
  }

  const updateTechSpecRow = (index: number, field: 'key' | 'value', value: string) => {
    const newRows = [...techSpecRows]
    newRows[index][field] = value
    setTechSpecRows(newRows)
  }

  const handleAddProduct = () => {
    if (!newItem.sku || !newItem.productName) {
      toast.error('Vui lòng nhập SKU và tên sản phẩm')
      return
    }

    if (newItem.quantity <= 0) {
      toast.error('Số lượng phải lớn hơn 0')
      return
    }

    if (newItem.price <= 0) {
      toast.error('Giá phải lớn hơn 0')
      return
    }

    // Kiểm tra SKU trùng
    const existingItem = items.find(item => item.sku === newItem.sku)
    if (existingItem) {
      toast.error('SKU đã tồn tại trong danh sách')
      return
    }

    // Convert tech specs to JSON
    const techSpecsJson = techSpecsToJson()
    
    console.log('Adding product with techSpecs:', techSpecsJson)
    
    setItems([...items, { ...newItem, techSpecs: techSpecsJson }])
    
    // Reset form and tech spec rows
    setNewItem({
      sku: '',
      productName: '',
      quantity: 1,
      price: 0,
      warrantyMonths: 12,
      techSpecs: '',
      note: ''
    })
    setTechSpecRows([{ key: '', value: '' }])
    
    setShowProductModal(false)
    toast.success('Đã thêm sản phẩm')
  }

  const handleOpenProductModal = () => {
    // Reset form when opening modal
    setNewItem({
      sku: '',
      productName: '',
      quantity: 1,
      price: 0,
      warrantyMonths: 12,
      techSpecs: '',
      note: ''
    })
    setTechSpecRows([{ key: '', value: '' }])
    setShowProductModal(true)
  }

  const handleCloseProductModal = () => {
    // Reset form when closing modal
    setNewItem({
      sku: '',
      productName: '',
      quantity: 1,
      price: 0,
      warrantyMonths: 12,
      techSpecs: '',
      note: ''
    })
    setTechSpecRows([{ key: '', value: '' }])
    setShowProductModal(false)
  }

  const handleRemoveItem = (sku: string) => {
    setItems(items.filter(item => item.sku !== sku))
    toast.success('Đã xóa sản phẩm')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (items.length === 0) {
      toast.error('Vui lòng thêm ít nhất 1 sản phẩm')
      return
    }

    if (type === 'IMPORT') {
      if (!formData.supplierName || !formData.supplierTaxCode) {
        toast.error('Vui lòng nhập thông tin nhà cung cấp (Tên và Mã số thuế)')
        return
      }
    }

    setIsSubmitting(true)

    try {
      if (type === 'IMPORT') {
        // Gọi API tạo phiếu nhập kho
        const poCode = `PO-${Date.now()}`
        
        const response = await inventoryApi.createPurchaseOrder({
          poCode,
          createdBy: user?.email || 'admin',
          supplier: {
            name: formData.supplierName,
            contactName: formData.supplierContactName,
            taxCode: formData.supplierTaxCode,
            phone: formData.supplierPhone,
            email: formData.supplierEmail,
            address: formData.supplierAddress,
            bankAccount: formData.supplierBankAccount,
            paymentTerm: formData.supplierPaymentTerm,
            active: true
          },
          items: items.map(item => ({
            sku: item.sku,
            quantity: item.quantity,
            unitCost: item.price,
            internalName: item.productName,
            techSpecsJson: item.techSpecs || '{}',
            warrantyMonths: item.warrantyMonths || 12,
            note: item.note || ''
          })),
          note: formData.note
        })

        if (response.success) {
          toast.success('Tạo phiếu nhập kho thành công!')
          router.push('/warehouse/import/list')
        } else {
          toast.error(response.message || 'Tạo phiếu nhập thất bại')
        }
      } else {
        // TODO: Implement export order
        toast.info('Chức năng xuất kho đang phát triển')
      }
    } catch (error: any) {
      console.error('Error creating purchase order:', error)
      toast.error(error.message || 'Có lỗi xảy ra')
    } finally {
      setIsSubmitting(false)
    }
  }

  const calculateTotal = () => {
    return items.reduce((sum, item) => sum + (item.quantity * item.price), 0)
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-6">
          <Link href="/" className="hover:text-red-500">Trang chủ</Link>
          <span>/</span>
          <Link href="/admin" className="hover:text-red-500">Quản trị</Link>
          <span>/</span>
          <Link href="/warehouse/import/list" className="hover:text-red-500">Danh sách phiếu nhập</Link>
          <span>/</span>
          <span className="text-gray-900">Tạo phiếu {type === 'IMPORT' ? 'nhập' : 'xuất'}</span>
        </nav>

        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">
              Tạo phiếu {type === 'IMPORT' ? 'nhập kho' : 'xuất kho'}
            </h1>
            <p className="text-gray-600 mt-1">
              {type === 'IMPORT' 
                ? 'Nhập hàng mới vào kho' 
                : 'Xuất hàng ra khỏi kho'}
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Main Form */}
            <div className="lg:col-span-2 space-y-6">
              {/* Transaction Info */}
              <div className="bg-white rounded-lg shadow-sm p-6">
                <h2 className="text-xl font-bold text-gray-900 mb-4">Thông tin phiếu</h2>
                
                <div className="space-y-4">
                  {type === 'IMPORT' && (
                    <>
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                          Chọn nhà cung cấp
                        </label>
                        <select
                          value={selectedSupplierId}
                          onChange={(e) => handleSupplierChange(e.target.value)}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500 bg-white"
                        >
                          <option value="">-- Nhập thông tin mới --</option>
                          {suppliers.map(supplier => (
                            <option key={supplier.id} value={supplier.id}>
                              {supplier.name} ({supplier.taxCode})
                            </option>
                          ))}
                        </select>
                        <p className="text-xs text-gray-500 mt-1">
                          Chọn nhà cung cấp có sẵn hoặc nhập mới
                        </p>
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                          Tên nhà cung cấp *
                        </label>
                        <input
                          type="text"
                          required
                          value={formData.supplierName}
                          onChange={(e) => setFormData({...formData, supplierName: e.target.value})}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                          placeholder="Nhập tên nhà cung cấp"
                        />
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                          Mã số thuế *
                        </label>
                        <input
                          type="text"
                          required
                          value={formData.supplierTaxCode}
                          onChange={(e) => setFormData({...formData, supplierTaxCode: e.target.value})}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                          placeholder="Nhập mã số thuế"
                        />
                      </div>

                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">
                            Số điện thoại
                          </label>
                          <input
                            type="text"
                            value={formData.supplierPhone}
                            onChange={(e) => setFormData({...formData, supplierPhone: e.target.value})}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                            placeholder="Nhập SĐT"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">
                            Email
                          </label>
                          <input
                            type="email"
                            value={formData.supplierEmail}
                            onChange={(e) => setFormData({...formData, supplierEmail: e.target.value})}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                            placeholder="Nhập email"
                          />
                        </div>
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                          Tên người liên hệ
                        </label>
                        <input
                          type="text"
                          value={formData.supplierContactName}
                          onChange={(e) => setFormData({...formData, supplierContactName: e.target.value})}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                          placeholder="Nhập tên người liên hệ"
                        />
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                          Địa chỉ
                        </label>
                        <input
                          type="text"
                          value={formData.supplierAddress}
                          onChange={(e) => setFormData({...formData, supplierAddress: e.target.value})}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                          placeholder="Nhập địa chỉ"
                        />
                      </div>

                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">
                            Tài khoản ngân hàng
                          </label>
                          <input
                            type="text"
                            value={formData.supplierBankAccount}
                            onChange={(e) => setFormData({...formData, supplierBankAccount: e.target.value})}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                            placeholder="Số TK ngân hàng"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">
                            Điều khoản thanh toán
                          </label>
                          <input
                            type="text"
                            value={formData.supplierPaymentTerm}
                            onChange={(e) => setFormData({...formData, supplierPaymentTerm: e.target.value})}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                            placeholder="VD: 30 ngày"
                          />
                        </div>
                      </div>
                    </>
                  )}

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Ghi chú
                    </label>
                    <textarea
                      value={formData.note}
                      onChange={(e) => setFormData({...formData, note: e.target.value})}
                      rows={3}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      placeholder="Nhập ghi chú"
                    />
                  </div>
                </div>
              </div>

              {/* Products List */}
              <div className="bg-white rounded-lg shadow-sm p-6">
                <div className="flex justify-between items-center mb-4">
                  <h2 className="text-xl font-bold text-gray-900">Danh sách sản phẩm</h2>
                  <button
                    type="button"
                    onClick={handleOpenProductModal}
                    className="flex items-center space-x-2 bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition-colors"
                  >
                    <FiPlus />
                    <span>Thêm sản phẩm</span>
                  </button>
                </div>

                {items.length === 0 ? (
                  <div className="text-center py-12 border-2 border-dashed border-gray-300 rounded-lg">
                    <p className="text-gray-500">Chưa có sản phẩm nào</p>
                    <button
                      type="button"
                      onClick={handleOpenProductModal}
                      className="mt-4 text-red-500 hover:text-red-600 font-medium"
                    >
                      Thêm sản phẩm đầu tiên
                    </button>
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Sản phẩm</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Số lượng</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Đơn giá</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Thành tiền</th>
                          <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Thao tác</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-200">
                        {items.map((item, index) => (
                          <tr key={item.sku + index}>
                            <td className="px-4 py-4">
                              <div className="text-sm font-medium text-gray-900">{item.productName}</div>
                              <div className="text-xs text-gray-500">SKU: {item.sku}</div>
                            </td>
                            <td className="px-4 py-4 text-sm text-gray-900">{item.quantity}</td>
                            <td className="px-4 py-4 text-sm text-gray-900">{formatPrice(item.price)}</td>
                            <td className="px-4 py-4 text-sm font-medium text-gray-900">
                              {formatPrice(item.quantity * item.price)}
                            </td>
                            <td className="px-4 py-4 text-right">
                              <button
                                type="button"
                                onClick={() => handleRemoveItem(item.sku)}
                                className="text-red-500 hover:text-red-600"
                              >
                                <FiTrash2 />
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>

            {/* Summary Sidebar */}
            <div className="lg:col-span-1">
              <div className="bg-white rounded-lg shadow-sm p-6 sticky top-24">
                <h2 className="text-xl font-bold text-gray-900 mb-6">Tổng quan</h2>
                
                <div className="space-y-4">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Loại phiếu:</span>
                    <span className={`font-semibold ${
                      type === 'IMPORT' ? 'text-green-600' : 'text-blue-600'
                    }`}>
                      {type === 'IMPORT' ? 'Nhập kho' : 'Xuất kho'}
                    </span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-gray-600">Số sản phẩm:</span>
                    <span className="font-semibold">{items.length}</span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-gray-600">Tổng số lượng:</span>
                    <span className="font-semibold">
                      {items.reduce((sum, item) => sum + item.quantity, 0)}
                    </span>
                  </div>

                  <div className="border-t pt-4">
                    <div className="flex justify-between text-lg font-bold">
                      <span>Tổng tiền:</span>
                      <span className="text-red-500">{formatPrice(calculateTotal())}</span>
                    </div>
                  </div>
                </div>

                <div className="mt-6 space-y-3">
                  <button
                    type="submit"
                    disabled={isSubmitting || items.length === 0}
                    className="w-full flex items-center justify-center space-x-2 bg-red-500 text-white py-3 px-6 rounded-lg font-semibold hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <FiSave />
                    <span>{isSubmitting ? 'Đang xử lý...' : 'Tạo phiếu'}</span>
                  </button>

                  <Link
                    href="/warehouse/import/list"
                    className="w-full flex items-center justify-center space-x-2 bg-gray-200 text-gray-700 py-3 px-6 rounded-lg font-semibold hover:bg-gray-300 transition-colors"
                  >
                    <FiX />
                    <span>Hủy</span>
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </form>

        {/* Add Product Modal */}
        {showProductModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
              <div className="p-6">
                <div className="flex justify-between items-center mb-6">
                  <h3 className="text-xl font-bold text-gray-900">Thêm sản phẩm</h3>
                  <button
                    onClick={handleCloseProductModal}
                    className="text-gray-400 hover:text-gray-600"
                  >
                    <FiX size={24} />
                  </button>
                </div>

                <div className="space-y-4">
                  {/* SKU */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      SKU *
                    </label>
                    <input
                      type="text"
                      required
                      value={newItem.sku}
                      onChange={(e) => setNewItem({...newItem, sku: e.target.value})}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      placeholder="Nhập mã SKU"
                    />
                  </div>

                  {/* Product Name */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Tên sản phẩm *
                    </label>
                    <input
                      type="text"
                      required
                      value={newItem.productName}
                      onChange={(e) => setNewItem({...newItem, productName: e.target.value})}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      placeholder="Nhập tên sản phẩm"
                    />
                  </div>

                  {/* Quantity */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Số lượng *
                    </label>
                    <input
                      type="number"
                      min="1"
                      required
                      value={newItem.quantity}
                      onChange={(e) => setNewItem({...newItem, quantity: Number(e.target.value)})}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                    />
                  </div>

                  {/* Price */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Đơn giá *
                    </label>
                    <input
                      type="number"
                      min="0"
                      required
                      value={newItem.price}
                      onChange={(e) => setNewItem({...newItem, price: Number(e.target.value)})}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      placeholder="Nhập giá"
                    />
                  </div>

                  {/* Warranty */}
                  {type === 'IMPORT' && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Bảo hành (tháng)
                      </label>
                      <input
                        type="number"
                        min="0"
                        value={newItem.warrantyMonths}
                        onChange={(e) => setNewItem({...newItem, warrantyMonths: Number(e.target.value)})}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      />
                    </div>
                  )}

                  {/* Tech Specs */}
                  <div>
                    <div className="flex items-center justify-between mb-2">
                      <label className="block text-sm font-medium text-gray-700">
                        Thông số kỹ thuật
                      </label>
                      <div className="flex items-center space-x-2">
                        <button
                          type="button"
                          onClick={async () => {
                            try {
                              const text = await navigator.clipboard.readText()
                              const lines = text.split('\n').filter(line => line.trim())
                              const newRows = lines.map(line => {
                                const parts = line.split(/\t|:/).map(s => s.trim())
                                return {
                                  key: parts[0] || '',
                                  value: parts[1] || ''
                                }
                              }).filter(row => row.key && row.value)
                              
                              if (newRows.length > 0) {
                                setTechSpecRows(newRows)
                                toast.success(`Đã paste ${newRows.length} thông số`)
                              } else {
                                toast.error('Không tìm thấy dữ liệu hợp lệ. Định dạng: Tên: Giá trị hoặc Tên[Tab]Giá trị')
                              }
                            } catch (error) {
                              toast.error('Không thể đọc clipboard. Vui lòng cho phép truy cập clipboard.')
                            }
                          }}
                          className="text-xs text-blue-500 hover:text-blue-600 font-medium"
                        >
                          📋 Paste từ clipboard
                        </button>
                        <button
                          type="button"
                          onClick={addTechSpecRow}
                          className="text-xs text-red-500 hover:text-red-600 font-medium flex items-center space-x-1"
                        >
                          <FiPlus size={14} />
                          <span>Thêm dòng</span>
                        </button>
                      </div>
                    </div>
                    
                    <p className="text-xs text-gray-500 mb-2">
                      💡 Copy từ Excel/Google Sheets hoặc text có định dạng "Tên: Giá trị" rồi click "Paste từ clipboard"
                    </p>
                    
                    <div className="border border-gray-300 rounded-lg overflow-hidden">
                      <table className="w-full">
                        <thead className="bg-gray-50">
                          <tr>
                            <th className="px-3 py-2 text-left text-xs font-medium text-gray-600">Tên thông số</th>
                            <th className="px-3 py-2 text-left text-xs font-medium text-gray-600">Giá trị</th>
                            <th className="px-3 py-2 w-10"></th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                          {techSpecRows.map((row, index) => (
                            <tr key={index}>
                              <td className="px-3 py-2">
                                <input
                                  type="text"
                                  value={row.key}
                                  onChange={(e) => updateTechSpecRow(index, 'key', e.target.value)}
                                  className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-red-500"
                                />
                              </td>
                              <td className="px-3 py-2">
                                <input
                                  type="text"
                                  value={row.value}
                                  onChange={(e) => updateTechSpecRow(index, 'value', e.target.value)}
                                  className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-red-500"
                                />
                              </td>
                              <td className="px-3 py-2 text-center">
                                {techSpecRows.length > 1 && (
                                  <button
                                    type="button"
                                    onClick={() => removeTechSpecRow(index)}
                                    className="text-red-500 hover:text-red-600"
                                  >
                                    <FiTrash2 size={14} />
                                  </button>
                                )}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                    
                    {/* Preview */}
                    {techSpecRows.some(row => row.key.trim() && row.value.trim()) && (
                      <div className="mt-2 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                        <p className="text-xs font-semibold text-blue-900 mb-1">Preview JSON:</p>
                        <div className="text-xs text-blue-800 space-y-1">
                          {techSpecRows
                            .filter(row => row.key.trim() && row.value.trim())
                            .map((row, index) => (
                              <div key={index} className="flex items-center space-x-2">
                                <span className="font-medium">{row.key.toLowerCase()}:</span>
                                <span>{row.value}</span>
                              </div>
                            ))}
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Note */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Ghi chú
                    </label>
                    <input
                      type="text"
                      value={newItem.note}
                      onChange={(e) => setNewItem({...newItem, note: e.target.value})}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                      placeholder="Ghi chú (không bắt buộc)"
                    />
                  </div>

                  {/* Total */}
                  {newItem.quantity > 0 && newItem.price > 0 && (
                    <div className="bg-gray-50 p-4 rounded-lg">
                      <div className="flex justify-between items-center">
                        <span className="text-gray-600">Thành tiền:</span>
                        <span className="text-xl font-bold text-red-500">
                          {formatPrice(newItem.quantity * newItem.price)}
                        </span>
                      </div>
                    </div>
                  )}
                </div>

                <div className="flex space-x-3 mt-6">
                  <button
                    type="button"
                    onClick={handleAddProduct}
                    className="flex-1 bg-red-500 text-white py-3 px-6 rounded-lg font-semibold hover:bg-red-600 transition-colors"
                  >
                    Thêm
                  </button>
                  <button
                    type="button"
                    onClick={handleCloseProductModal}
                    className="flex-1 bg-gray-200 text-gray-700 py-3 px-6 rounded-lg font-semibold hover:bg-gray-300 transition-colors"
                  >
                    Hủy
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
