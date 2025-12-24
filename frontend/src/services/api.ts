import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/subscription-service/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor for error handling and token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // If 401 and not already retried, try to refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        const refreshToken = localStorage.getItem('refreshToken')
        if (refreshToken) {
          const response = await api.post('/auth/refresh', { refreshToken })
          const { accessToken, refreshToken: newRefreshToken } = response.data
          
          localStorage.setItem('token', accessToken)
          if (newRefreshToken) {
            localStorage.setItem('refreshToken', newRefreshToken)
          }
          
          // Retry original request with new token
          originalRequest.headers.Authorization = `Bearer ${accessToken}`
          return api(originalRequest)
        }
      } catch (refreshError) {
        // Refresh failed, logout user
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    // If refresh didn't work or other error, redirect to login
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    
    return Promise.reject(error)
  }
)

// Auth API
export const authAPI = {
  login: (username: string, password: string) =>
    api.post('/auth/login', { username, password }),
  loginMobile: (mobileNumber: string, password: string) =>
    api.post('/auth/login/mobile', { mobileNumber, password }),
  register: (data: any) => api.post('/auth/register', data),
  getMe: () => api.get('/auth/me'),
  logout: () => api.post('/auth/logout'),
  refreshToken: (refreshToken: string) =>
    api.post('/auth/refresh', { refreshToken }),
}

// User API
export const userAPI = {
  getProfile: () => api.get('/user/profile'),
  updateProfile: (data: any) => api.put('/user/profile', data),
  getSubscriptions: () => api.get('/user/subscriptions'),
  getActiveSubscriptions: () => api.get('/user/subscriptions/active'),
  cancelSubscription: (id: number) => api.post(`/user/subscriptions/${id}/cancel`),
}

// Admin API
export const adminAPI = {
  // Users
  softDeleteUser: (id: number) => api.post(`/admin/users/${id}/soft-delete`),
  restoreUser: (id: number) => api.post(`/admin/users/${id}/restore`),
  getDeletedUsers: () => api.get('/admin/users/deleted'),
  
  // Devices
  softDeleteDevice: (id: number) => api.post(`/admin/devices/${id}/soft-delete`),
  restoreDevice: (id: number) => api.post(`/admin/devices/${id}/restore`),
  getDeletedDevices: () => api.get('/admin/devices/deleted'),
  
  // Subscriptions
  createSubscription: (data: any) => api.post('/admin/subscriptions', data),
  softDeleteSubscription: (id: number) => api.post(`/admin/subscriptions/${id}/soft-delete`),
  restoreSubscription: (id: number) => api.post(`/admin/subscriptions/${id}/restore`),
  getDeletedSubscriptions: () => api.get('/admin/subscriptions/deleted'),
  assignSubscription: (data: any) => api.post('/admin/user-subscriptions/assign'),
  
  // Features
  createFeature: (data: any) => api.post('/admin/features', data),
  getAllFeatures: () => api.get('/admin/features'),
  addFeaturesToSubscription: (id: number, featureIds: number[]) =>
    api.post(`/admin/subscriptions/${id}/features`, { featureIds }),
}

// Agent API
export const agentAPI = {
  // Subscriptions (view only)
  getAllSubscriptions: () => api.get('/agent/subscriptions'),
  getSubscriptionsByDevice: (deviceId: number) =>
    api.get(`/agent/subscriptions/device/${deviceId}`),
  
  // User Subscriptions
  assignSubscription: (data: any) => api.post('/agent/user-subscriptions/assign'),
  getAllUserSubscriptions: () => api.get('/agent/user-subscriptions'),
  getUserSubscriptions: (userId: number) =>
    api.get(`/agent/user-subscriptions/user/${userId}`),
  updateNegotiatedPrice: (id: number, price: number) =>
    api.put(`/agent/user-subscriptions/${id}/negotiated-price`, { negotiatedPrice: price }),
  cancelSubscription: (id: number) => api.post(`/agent/user-subscriptions/${id}/cancel`),
  
  // Devices
  createDevice: (data: any) => api.post('/agent/devices', data),
  getAllDevices: () => api.get('/agent/devices'),
  assignDevice: (data: any) => api.post('/agent/user-devices/assign'),
  getUserDevices: (userId: number) => api.get(`/agent/user-devices/user/${userId}`),
}

// Audit Log API
export const auditAPI = {
  getAll: (page = 0, size = 50) => api.get(`/audit?page=${page}&size=${size}`),
  getTrail: (entityType: string, entityId: number) =>
    api.get(`/audit/trail/${entityType}/${entityId}`),
  getByUser: (userId: number, page = 0, size = 50) =>
    api.get(`/audit/user/${userId}?page=${page}&size=${size}`),
  getByAction: (action: string, page = 0, size = 50) =>
    api.get(`/audit/action/${action}?page=${page}&size=${size}`),
  getByEntityType: (entityType: string, page = 0, size = 50) =>
    api.get(`/audit/entity/${entityType}?page=${page}&size=${size}`),
  getFailed: (page = 0, size = 50) => api.get(`/audit/failed?page=${page}&size=${size}`),
  search: (keyword: string, page = 0, size = 50) =>
    api.get(`/audit/search?keyword=${keyword}&page=${page}&size=${size}`),
  getStatistics: () => api.get('/audit/statistics'),
}

// Billing API
// Valid payment methods: "Credit Card", "Debit Card", "UPI"
export const billingAPI = {
  generateMonthly: () => api.post('/billing/generate-monthly'),
  getPending: () => api.get('/billing/pending'),
  markPaid: (id: number, paymentMethod?: string) =>
    api.put(`/billing/${id}/mark-paid`, { paymentMethod }),
  payBill: (id: number, paymentMethod?: string) =>
    api.put(`/billing/${id}/pay`, { paymentMethod }),
  getBillsByUserSubscription: (userSubscriptionId: number) =>
    api.get(`/billing/user-subscription/${userSubscriptionId}`),
  markOverdue: () => api.post('/billing/mark-overdue'),
}

export default api

