'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { FiUpload, FiDownload, FiCheck, FiX, FiAlertCircle } from 'react-icons/fi'
import toast from 'react-hot-toast'
import { useAuthStore } from '@/store/authStore'

interface ImportRow {
  sku: string
  productName: string
  quantity: number
  price: number
  supplier: string
  category: string
  status?: 'valid' | 'error'
  error?: string
}

export default function ImportInventoryPage() {
  const router = useRouter()
  const { user, isAuthenticated } = useAuthStore()
  
  const [file, setFile] = useState<File | null>(null)
  const [previewData, setPreviewData] = useState<ImportRow[]>([])
  const [isProcessing, setIsProcessing] = useState(false)
  const [showPreview, setShowPreview] = useState(false)

  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập')
      router.push('/login')
      return
    }

    if (user?.role !== 'ADMIN' && user?.role !== 'WAREHOUSE') {
      toast.error('Chỉ quản lý và nhân viên mới có quyền truy cập')
      router.push('/')
      return
    }
  }, [isAuthenticated, user, router])

  const downloadTemplate = () => {
    // Tạo file Excel mẫu
    const csvContent = `SKU,Tên sản phẩm,Số lượng,Đơn giá,Nhà cung cấp,Danh mục
IP16PM-256,iPhone 16 Pro Max 256GB,10,29990000,Apple Vietnam,Điện thoại
XM-POCO-C71,Xiaomi POCO C71 4GB/128GB,20,2490000,Xiaomi Vietnam,Điện thoại
MBP14-M3,MacBook Pro 14 inch M3,5,45990000,Apple Vietnam,Laptop`

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = 'template_nhap_hang.csv'
    link.click()
    
    toast.success('Đã tải file mẫu')
  }

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0]
    if (!selectedFile) return

    // Kiểm tra định dạng file
    const validTypes = [
      'text/csv',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    ]
    
    if (!validTypes.includes(selectedFile.type) && 
        !selectedFile.name.endsWith('.csv') && 
        !selectedFile.name.endsWith('.xlsx') && 
        !selectedFile.name.endsWith('.xls')) {
      toast.error('Chỉ chấp nhận file Excel (.xlsx, .xls) hoặc CSV')
      return
    }

    setFile(selectedFile)
    
    // Parse file để preview
    if (selectedFile.name.endsWith('.csv')) {
      parseCSV(selectedFile)
    } else {
      toast('Đang xử lý file Excel...')
      // TODO: Parse Excel file
      // Cần cài package: npm install xlsx
    }
  }

  const parseCSV = (file: File) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const text = e.target?.result as string
      const lines = text.split('\n')
      const headers = lines[0].split(',')
      
      const data: ImportRow[] = []
      for (let i = 1; i < lines.length; i++) {
        if (!lines[i].trim()) continue
        
        const values = lines[i].split(',')
        if (values.length < 6) continue
        
        const row: ImportRow = {
          sku: values[0]?.trim() || '',
          productName: values[1]?.trim() || '',
          quantity: parseInt(values[2]?.trim()) || 0,
          price: parseFloat(values[3]?.trim()) || 0,
          supplier: values[4]?.trim() || '',
          category: values[5]?.trim() || '',
          status: 'valid'
        }
        
        // Validate
        if (!row.sku || !row.productName || row.quantity <= 0 || row.price <= 0) {
          row.status = 'error'
          row.error = 'Thiếu thông tin bắt buộc'
        }
        
        data.push(row)
      }
      
      setPreviewData(data)
      setShowPreview(true)
    }
    reader.readAsText(file)
  }

  const handleImport = async () => {
    if (!file) {
      toast.error('Vui lòng chọn file')
      return
    }

    const validRows = previewData.filter(row => row.status === 'valid')
    if (validRows.length === 0) {
      toast.error('Không có dòng dữ liệu hợp lệ')
      return
    }

    setIsProcessing(true)

    try {
      // Tạo FormData để gửi file
      const formData = new FormData()
      formData.append('file', file)
      
      // TODO: Call API
      const response = await fetch('http://localhost:8080/api/inventory/import', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
        },
        body: formData
      })

      const data = await response.json()

      if (data.success || response.ok) {
        toast.success(`Nhập hàng thành công! ${validRows.length} sản phẩm`)
        router.push('/admin/inventory/transactions')
      } else {
        toast.error(data.message || 'Nhập hàng thất bại')
      }
    } catch (error: any) {
      console.error('Import error:', error)
      toast.error('Có lỗi xảy ra khi nhập hàng')
    } finally {
      setIsProcessing(false)
    }
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
          <Link href="/admin/inventory" className="hover:text-red-500">Quản lý kho</Link>
          <span>/</span>
          <span className="text-gray-900">Nhập hàng từ Excel</span>
        </nav>

        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Nhập hàng từ Excel</h1>
          <p className="text-gray-600 mt-1">
            Tải file Excel để tự động tạo nhà cung cấp, sản phẩm và phiếu nhập kho
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Upload Section */}
          <div className="lg:col-span-2 space-y-6">
            {/* Instructions */}
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
              <h3 className="font-semibold text-blue-900 mb-3 flex items-center">
                <FiAlertCircle className="mr-2" />
                Hướng dẫn
              </h3>
              <ol className="list-decimal list-inside space-y-2 text-sm text-blue-800">
                <li>Tải file Excel mẫu bên dưới</li>
                <li>Điền thông tin sản phẩm theo đúng format</li>
                <li>Tải file lên và kiểm tra preview</li>
                <li>Nhấn "Nhập hàng" để hoàn tất</li>
              </ol>
              <div className="mt-4">
                <button
                  onClick={downloadTemplate}
                  className="flex items-center space-x-2 bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
                >
                  <FiDownload />
                  <span>Tải file Excel mẫu</span>
                </button>
              </div>
            </div>

            {/* Format Info */}
            <div className="bg-white rounded-lg shadow-sm p-6">
              <h3 className="font-semibold text-gray-900 mb-4">Format file Excel</h3>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-4 py-2 text-left">Cột</th>
                      <th className="px-4 py-2 text-left">Bắt buộc</th>
                      <th className="px-4 py-2 text-left">Ví dụ</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y">
                    <tr>
                      <td className="px-4 py-2 font-medium">SKU</td>
                      <td className="px-4 py-2"><span className="text-red-500">✓</span></td>
                      <td className="px-4 py-2 text-gray-600">IP16PM-256</td>
                    </tr>
                    <tr>
                      <td className="px-4 py-2 font-medium">Tên sản phẩm</td>
                      <td className="px-4 py-2"><span className="text-red-500">✓</span></td>
                      <td className="px-4 py-2 text-gray-600">iPhone 16 Pro Max 256GB</td>
                    </tr>
                    <tr>
                      <td className="px-4 py-2 font-medium">Số lượng</td>
                      <td className="px-4 py-2"><span className="text-red-500">✓</span></td>
                      <td className="px-4 py-2 text-gray-600">10</td>
                    </tr>
                    <tr>
                      <td className="px-4 py-2 font-medium">Đơn giá</td>
                      <td className="px-4 py-2"><span className="text-red-500">✓</span></td>
                      <td className="px-4 py-2 text-gray-600">29990000</td>
                    </tr>
                    <tr>
                      <td className="px-4 py-2 font-medium">Nhà cung cấp</td>
                      <td className="px-4 py-2"><span className="text-red-500">✓</span></td>
                      <td className="px-4 py-2 text-gray-600">Apple Vietnam</td>
                    </tr>
                    <tr>
                      <td className="px-4 py-2 font-medium">Danh mục</td>
                      <td className="px-4 py-2"><span className="text-red-500">✓</span></td>
                      <td className="px-4 py-2 text-gray-600">Điện thoại</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            {/* Upload Area */}
            <div className="bg-white rounded-lg shadow-sm p-6">
              <h3 className="font-semibold text-gray-900 mb-4">Tải file lên</h3>
              
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-12 text-center">
                <FiUpload size={48} className="mx-auto text-gray-400 mb-4" />
                <p className="text-gray-600 mb-4">
                  {file ? file.name : 'Kéo thả file hoặc click để chọn'}
                </p>
                <label className="inline-flex items-center space-x-2 bg-red-500 text-white px-6 py-3 rounded-lg hover:bg-red-600 transition-colors cursor-pointer">
                  <FiUpload />
                  <span>Chọn file Excel</span>
                  <input
                    type="file"
                    accept=".xlsx,.xls,.csv"
                    onChange={handleFileChange}
                    className="hidden"
                  />
                </label>
                <p className="text-xs text-gray-500 mt-4">
                  Hỗ trợ: .xlsx, .xls, .csv (tối đa 5MB)
                </p>
              </div>
            </div>

            {/* Preview Data */}
            {showPreview && previewData.length > 0 && (
              <div className="bg-white rounded-lg shadow-sm p-6">
                <h3 className="font-semibold text-gray-900 mb-4">
                  Preview dữ liệu ({previewData.length} dòng)
                </h3>
                
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-4 py-2 text-left">Trạng thái</th>
                        <th className="px-4 py-2 text-left">SKU</th>
                        <th className="px-4 py-2 text-left">Tên SP</th>
                        <th className="px-4 py-2 text-left">SL</th>
                        <th className="px-4 py-2 text-left">Giá</th>
                        <th className="px-4 py-2 text-left">NCC</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {previewData.map((row, index) => (
                        <tr key={index} className={row.status === 'error' ? 'bg-red-50' : ''}>
                          <td className="px-4 py-2">
                            {row.status === 'valid' ? (
                              <FiCheck className="text-green-500" />
                            ) : (
                              <FiX className="text-red-500" title={row.error} />
                            )}
                          </td>
                          <td className="px-4 py-2">{row.sku}</td>
                          <td className="px-4 py-2">{row.productName}</td>
                          <td className="px-4 py-2">{row.quantity}</td>
                          <td className="px-4 py-2">{formatPrice(row.price)}</td>
                          <td className="px-4 py-2">{row.supplier}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>

          {/* Summary Sidebar */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-sm p-6 sticky top-24">
              <h3 className="font-semibold text-gray-900 mb-4">Tổng quan</h3>
              
              <div className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-gray-600">File:</span>
                  <span className="font-medium">{file ? '✓' : '✗'}</span>
                </div>
                
                {showPreview && (
                  <>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Tổng dòng:</span>
                      <span className="font-medium">{previewData.length}</span>
                    </div>
                    
                    <div className="flex justify-between">
                      <span className="text-gray-600">Hợp lệ:</span>
                      <span className="font-medium text-green-600">
                        {previewData.filter(r => r.status === 'valid').length}
                      </span>
                    </div>
                    
                    <div className="flex justify-between">
                      <span className="text-gray-600">Lỗi:</span>
                      <span className="font-medium text-red-600">
                        {previewData.filter(r => r.status === 'error').length}
                      </span>
                    </div>
                    
                    <div className="border-t pt-4">
                      <div className="flex justify-between text-lg font-bold">
                        <span>Tổng tiền:</span>
                        <span className="text-red-500">
                          {formatPrice(
                            previewData
                              .filter(r => r.status === 'valid')
                              .reduce((sum, r) => sum + (r.quantity * r.price), 0)
                          )}
                        </span>
                      </div>
                    </div>
                  </>
                )}
              </div>

              <div className="mt-6 space-y-3">
                <button
                  onClick={handleImport}
                  disabled={!showPreview || isProcessing || previewData.filter(r => r.status === 'valid').length === 0}
                  className="w-full flex items-center justify-center space-x-2 bg-red-500 text-white py-3 px-6 rounded-lg font-semibold hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <FiCheck />
                  <span>{isProcessing ? 'Đang xử lý...' : 'Nhập hàng'}</span>
                </button>

                <Link
                  href="/admin/inventory"
                  className="w-full flex items-center justify-center space-x-2 bg-gray-200 text-gray-700 py-3 px-6 rounded-lg font-semibold hover:bg-gray-300 transition-colors"
                >
                  <FiX />
                  <span>Hủy</span>
                </Link>
              </div>

              <div className="mt-6 pt-6 border-t">
                <p className="text-xs text-gray-500">
                  <strong>Lưu ý:</strong> Hệ thống sẽ tự động tạo nhà cung cấp và sản phẩm mới nếu chưa tồn tại trong database.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
