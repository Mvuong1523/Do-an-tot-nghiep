'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import toast from 'react-hot-toast'
import Logo from '@/components/layout/Logo'
import { authApi } from '@/lib/api'

export default function EmployeeRegisterPage() {
  const router = useRouter()
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', password: '', confirmPassword: '' })
  const [isLoading, setIsLoading] = useState(false)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (form.password !== form.confirmPassword) {
      toast.error('Mật khẩu xác nhận không khớp')
      return
    }
    setIsLoading(true)
    try {
      // Send registration request. Backend should handle employee role or approval.
      const payload = {
        fullName: form.fullName,
        email: form.email,
        phone: form.phone,
        password: form.password,
        role: 'EMPLOYEE'
      }
      const res = await authApi.register(payload)
      if (res && res.success) {
        toast.success('Gửi yêu cầu đăng ký nhân viên thành công. Vui lòng chờ phê duyệt.')
        router.push('/login')
      } else {
        toast.error(res?.message || 'Đăng ký thất bại')
      }
    } catch (err: any) {
      toast.error(err.message || 'Đăng ký thất bại')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4">
      <div className="w-full max-w-md bg-white p-8 rounded-lg shadow">
        <div className="flex justify-center mb-4">
          <Logo />
        </div>
        <h2 className="text-2xl font-bold text-center mb-2">Đăng ký nhân viên</h2>
        <p className="text-sm text-center text-gray-600 mb-6">
          Nếu bạn là nhân viên, vui lòng đăng ký để chờ quản trị phê duyệt.
        </p>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Họ và tên</label>
            <input name="fullName" value={form.fullName} onChange={handleChange} required className="w-full rounded border px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input name="email" type="email" value={form.email} onChange={handleChange} required className="w-full rounded border px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Số điện thoại</label>
            <input name="phone" value={form.phone} onChange={handleChange} className="w-full rounded border px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Mật khẩu</label>
            <input name="password" type="password" value={form.password} onChange={handleChange} required className="w-full rounded border px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Xác nhận mật khẩu</label>
            <input name="confirmPassword" type="password" value={form.confirmPassword} onChange={handleChange} required className="w-full rounded border px-3 py-2" />
          </div>
          <div className="flex items-center justify-between">
            <Link href="/login" className="text-sm text-blue-500">Quay lại đăng nhập</Link>
            <button type="submit" disabled={isLoading} className="bg-blue-600 text-white px-4 py-2 rounded">
              {isLoading ? 'Đang gửi...' : 'Gửi yêu cầu'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
