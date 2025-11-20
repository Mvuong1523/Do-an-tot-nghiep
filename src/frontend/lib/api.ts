import axios from 'axios'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add token to requests if available
apiClient.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  }
  return config
})

interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
  error?: string
}

// Auth API
export const authApi = {
  login: async (data: { email: string; password: string }): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/auth/login', data)
      if (response.data.token) {
        localStorage.setItem('auth_token', response.data.token)
      }
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Đăng nhập thất bại')
    }
  },

  register: async (data: any): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/auth/register', data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Đăng ký thất bại')
    }
  },

  sendOtp: async (data: any): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/auth/register/send-otp', data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Gửi OTP thất bại')
    }
  },

  verifyOtp: async (data: { email: string; otpCode: string }): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/auth/register/verify-otp', data)
      if (response.data.token) {
        localStorage.setItem('auth_token', response.data.token)
      }
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Xác thực OTP thất bại')
    }
  },

  logout: async (): Promise<void> => {
    localStorage.removeItem('auth_token')
  },

  getCurrentUser: async (): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.get('/auth/me')
      return response.data
    } catch (error: any) {
      return {
        success: false,
        error: 'Không thể lấy thông tin người dùng'
      }
    }
  },

  approveEmployee: async (data: { userId: string; status: 'APPROVED' | 'REJECTED'; reason?: string }): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/employee-registration/approve/{id}', data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi duyệt nhân viên')
    }
  },

  getPendingEmployees: async (): Promise<ApiResponse<any[]>> => {
    try {
      const response = await apiClient.get('/employee-registration/list')
      return {
        success: true,
        data: response.data || [],
      }
    } catch (error: any) {
      return {
        success: false,
        data: [],
        error: error.message,
      }
    }
  },

  registerEmployee: async (data: {
    fullName: string
    email: string
    phone: string
    address?: string
    position: string
    note?: string
  }): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/employee-registration/apply', data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi đăng ký nhân viên')
    }
  },
}

// Category API
export const categoryApi = {
  getAll: async (): Promise<ApiResponse<any[]>> => {
    try {
      const response = await apiClient.get('/categories')
      console.log('Category API raw response:', response)
      
      // Kiểm tra nếu response.data là ApiResponse
      if (response.data && response.data.data) {
        return {
          success: true,
          data: Array.isArray(response.data.data) ? response.data.data : [],
        }
      }
      
      // Nếu response.data là array trực tiếp
      return {
        success: true,
        data: Array.isArray(response.data) ? response.data : [],
      }
    } catch (error: any) {
      console.error('Category API error:', error)
      return {
        success: false,
        data: [],
        error: error.message,
      }
    }
  },

  getById: async (id: string | number): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.get(`/categories/${id}`)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi lấy thông tin danh mục')
    }
  },

  getActiveCategories: async (): Promise<ApiResponse<any[]>> => {
    try {
      const response = await apiClient.get('/categories/active')
      console.log('Active categories response:', response)
      
      if (response.data && response.data.data) {
        return {
          success: true,
          data: Array.isArray(response.data.data) ? response.data.data : [],
        }
      }
      
      return {
        success: true,
        data: Array.isArray(response.data) ? response.data : [],
      }
    } catch (error: any) {
      console.error('Active categories error:', error)
      return {
        success: false,
        data: [],
        error: error.message,
      }
    }
  },

  getCategoriesTree: async (): Promise<ApiResponse<any[]>> => {
    try {
      const response = await apiClient.get('/categories/tree')
      console.log('Categories tree response:', response)
      
      if (response.data && response.data.data) {
        return {
          success: true,
          data: Array.isArray(response.data.data) ? response.data.data : [],
        }
      }
      
      return {
        success: true,
        data: Array.isArray(response.data) ? response.data : [],
      }
    } catch (error: any) {
      console.error('Categories tree error:', error)
      return {
        success: false,
        data: [],
        error: error.message,
      }
    }
  },

  create: async (data: any): Promise<ApiResponse<any>> => {
    try {
      console.log('🔑 Creating category with token:', localStorage.getItem('auth_token')?.substring(0, 20) + '...')
      const response = await apiClient.post('/categories', data)
      return response.data
    } catch (error: any) {
      console.error('❌ Category create error:', error.response?.data)
      console.error('❌ Status:', error.response?.status)
      console.error('❌ Headers:', error.response?.headers)
      throw new Error(error.response?.data?.message || error.response?.data?.error || 'Lỗi khi tạo danh mục')
    }
  },

  update: async (id: number, data: any): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.put(`/categories/${id}`, data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi cập nhật danh mục')
    }
  },

  delete: async (id: number): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.delete(`/categories/${id}`)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi xóa danh mục')
    }
  },
}

// Product API
export const productApi = {
  getAll: async (params?: any): Promise<ApiResponse<any[]>> => {
    try {
      const response = await apiClient.get('/products', { params })
      return response.data
    } catch (error: any) {
      return {
        success: false,
        data: [],
        error: error.message,
      }
    }
  },

  getById: async (id: string | number): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.get(`/products/${id}`)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi lấy thông tin sản phẩm')
    }
  },

  search: async (query: string): Promise<ApiResponse<any[]>> => {
    try {
      const response = await apiClient.get('/products/search', { params: { q: query } })
      return {
        success: true,
        data: response.data || [],
      }
    } catch (error: any) {
      return {
        success: false,
        data: [],
        error: error.message,
      }
    }
  },

  // Warehouse product management for publishing
  getWarehouseProductsForPublish: async (): Promise<ApiResponse<any[]>> => {
    try {
      const response = await apiClient.get('/products/warehouse/list')
      return response.data
    } catch (error: any) {
      return {
        success: false,
        data: [],
        error: error.message,
      }
    }
  },

  createProductFromWarehouse: async (data: any): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/products/warehouse/publish', data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi đăng bán sản phẩm')
    }
  },

  updatePublishedProduct: async (productId: number, data: any): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.put(`/products/warehouse/publish/${productId}`, data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi cập nhật sản phẩm')
    }
  },

  unpublishProduct: async (productId: number): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.delete(`/products/warehouse/unpublish/${productId}`)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi gỡ sản phẩm')
    }
  },
}

// Order API
export const orderApi = {
  create: async (data: any): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/orders', data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi tạo đơn hàng')
    }
  },

  getAll: async (params?: any): Promise<ApiResponse<any[]>> => {
    try {
      const response = await apiClient.get('/orders', { params })
      return {
        success: true,
        data: response.data || [],
      }
    } catch (error: any) {
      return {
        success: false,
        data: [],
        error: error.message,
      }
    }
  },

  getById: async (id: string | number): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.get(`/orders/${id}`)
      return {
        success: true,
        data: response.data,
      }
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi lấy thông tin đơn hàng')
    }
  },
}

// Contact API
export const contactApi = {
  sendMessage: async (data: any): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/contact/messages', data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi gửi tin nhắn')
    }
  },
}

// Inventory API
export const inventoryApi = {
  createPurchaseOrder: async (data: any): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/inventory/create_pchaseOrder', data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi tạo đơn nhập hàng')
    }
  },

  createSupplier: async (data: any): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/inventory/suppliers', data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi tạo nhà cung cấp')
    }
  },

  getProductsBySupplier: async (supplierId: number): Promise<ApiResponse<any[]>> => {
    try {
      const response = await apiClient.get(`/inventory/supplier/${supplierId}/products`)
      return response.data
    } catch (error: any) {
      return {
        success: false,
        data: [],
        error: error.message,
      }
    }
  },

  importStock: async (data: any): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/inventory/import', data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi nhập hàng')
    }
  },

  createExportOrder: async (data: any): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/inventory/create', data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi tạo đơn xuất hàng')
    }
  },

  getPurchaseOrders: async (status?: string): Promise<ApiResponse<any[]>> => {
    try {
      const params = status ? { status } : {}
      const response = await apiClient.get('/inventory/purchase-orders', { params })
      return response.data
    } catch (error: any) {
      return {
        success: false,
        data: [],
        error: error.message,
      }
    }
  },

  getExportOrders: async (status?: string): Promise<ApiResponse<any[]>> => {
    try {
      const params = status ? { status } : {}
      const response = await apiClient.get('/inventory/export-orders', { params })
      return response.data
    } catch (error: any) {
      return {
        success: false,
        data: [],
        error: error.message,
      }
    }
  },

  getPurchaseOrderDetail: async (id: number): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.get(`/inventory/purchase-orders/${id}`)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi tải chi tiết phiếu nhập')
    }
  },

  getExportOrderDetail: async (id: number): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.get(`/inventory/export-orders/${id}`)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi tải chi tiết phiếu xuất')
    }
  },

  completePurchaseOrder: async (data: any): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/inventory/import', data)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi hoàn tất đơn nhập')
    }
  },

  cancelTransaction: async (id: number, type: string): Promise<ApiResponse<any>> => {
    try {
      const endpoint = type === 'IMPORT' ? 'purchase-orders' : 'export-orders'
      const response = await apiClient.put(`/inventory/${endpoint}/${id}/cancel`)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi hủy phiếu')
    }
  },

  getStocks: async (): Promise<ApiResponse<any[]>> => {
    try {
      const response = await apiClient.get('/inventory/stock')
      return response.data
    } catch (error: any) {
      return {
        success: false,
        data: [],
        error: error.message,
      }
    }
  },
}

// Cart API
export const cartApi = {
  getCart: async (): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.get('/cart')
      return response.data
    } catch (error: any) {
      return {
        success: false,
        data: null,
        error: error.message,
      }
    }
  },

  addToCart: async (productId: number, quantity: number): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.post('/cart/items', { productId, quantity })
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi thêm vào giỏ hàng')
    }
  },

  updateCartItem: async (itemId: number, quantity: number): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.put(`/cart/items/${itemId}`, { quantity })
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi cập nhật giỏ hàng')
    }
  },

  removeCartItem: async (itemId: number): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.delete(`/cart/items/${itemId}`)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi xóa sản phẩm')
    }
  },

  clearCart: async (): Promise<ApiResponse<any>> => {
    try {
      const response = await apiClient.delete('/cart')
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi xóa giỏ hàng')
    }
  },
}

export default apiClient
