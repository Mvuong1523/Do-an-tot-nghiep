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
      throw new Error('Không thể lấy thông tin người dùng')
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
      const response = await apiClient.get(`/categories/${id}`)
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi lấy thông tin danh mục')
    }
  },
}

// Product API
export const productApi = {
  getAll: async (params?: any): Promise<ApiResponse<any[]>> => {
    try {
      const response = await apiClient.get('/product', { params })
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
      const response = await apiClient.get(`/product/${id}`)
      return {
        success: true,
        data: response.data,
      }
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Lỗi khi lấy thông tin sản phẩm')
    }
  },

  search: async (query: string): Promise<ApiResponse<any[]>> => {
    try {
      const response = await apiClient.get('/product/search', { params: { q: query } })
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
}

export default apiClient
