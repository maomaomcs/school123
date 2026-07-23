import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({ baseURL: '/api', timeout: 20000 })

// 管理端请求自动带 token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token && config.url && config.url.includes('/admin')) {
    config.headers['X-Admin-Token'] = token
  }
  return config
})

api.interceptors.response.use(
  (res) => res.data,
  (err) => {
    const status = err.response?.status
    const msg = err.response?.data?.error || err.message || '请求失败'
    if (status === 401 && location.pathname.startsWith('/admin')) {
      localStorage.removeItem('admin_token')
      if (location.pathname !== '/admin/login') {
        location.href = '/admin/login'
      }
    }
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default api

// ---------- 公开接口 ----------
export const getConfig = () => api.get('/config')
export const getHome = () => api.get('/home')
export const getBanners = () => api.get('/banners')
export const getArticles = (params) => api.get('/articles', { params })
export const getArticle = (id) => api.get(`/articles/${id}`)
export const searchArticles = (params) => api.get('/search', { params })
export const getTeachers = () => api.get('/teachers')
export const getPage = (key) => api.get(`/pages/${key}`)
export const submitMessage = (data) => api.post('/messages', data)
export const trackVisit = (payload) => api.post('/track', payload)

// ---------- 管理接口 ----------
export const adminLogin = (data) => api.post('/admin/login', data)
export const adminLogout = () => api.post('/admin/logout')
export const adminMe = () => api.get('/admin/me')
export const changePassword = (data) => api.post('/admin/change-password', data)

export const adminArticles = (params) => api.get('/admin/articles', { params })
export const adminGetArticle = (id) => api.get(`/admin/articles/${id}`)
export const adminCreateArticle = (data, action = 'draft') => api.post('/admin/articles', data, { params: { action } })
export const adminUpdateArticle = (id, data, action = 'draft') => api.put(`/admin/articles/${id}`, data, { params: { action } })
export const adminDeleteArticle = (id) => api.delete(`/admin/articles/${id}`)
export const approveArticle = (id) => api.post(`/admin/articles/${id}/approve`)
export const rejectArticle = (id, reason) => api.post(`/admin/articles/${id}/reject`, { reason })

// 账号管理(仅校宣)
export const adminAccounts = () => api.get('/admin/accounts')
export const adminCreateAccount = (data) => api.post('/admin/accounts', data)
export const adminResetAccountPwd = (id, newPassword) => api.post(`/admin/accounts/${id}/reset-password`, { newPassword })
export const adminSetAccountEnabled = (id, enabled) => api.patch(`/admin/accounts/${id}`, { enabled })
export const adminDeleteAccount = (id) => api.delete(`/admin/accounts/${id}`)

export const adminBanners = () => api.get('/admin/banners')
export const adminCreateBanner = (data) => api.post('/admin/banners', data)
export const adminUpdateBanner = (id, data) => api.put(`/admin/banners/${id}`, data)
export const adminDeleteBanner = (id) => api.delete(`/admin/banners/${id}`)

export const adminTeachers = () => api.get('/admin/teachers')
export const adminCreateTeacher = (data) => api.post('/admin/teachers', data)
export const adminUpdateTeacher = (id, data) => api.put(`/admin/teachers/${id}`, data)
export const adminDeleteTeacher = (id) => api.delete(`/admin/teachers/${id}`)

export const adminPages = () => api.get('/admin/pages')
export const adminSavePage = (key, data) => api.put(`/admin/pages/${key}`, data)

export const adminMessages = () => api.get('/admin/messages')
export const adminHandleMessage = (id, handled) => api.put(`/admin/messages/${id}/handled`, null, { params: { handled } })
export const adminDeleteMessage = (id) => api.delete(`/admin/messages/${id}`)

export const adminAnalytics = (days = 7) => api.get('/admin/analytics', { params: { days } })

export const uploadUrl = '/api/admin/upload'
