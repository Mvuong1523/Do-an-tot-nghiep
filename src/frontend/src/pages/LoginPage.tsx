import React, { useState } from "react"
import { useNavigate } from "react-router-dom"
import { useAuthStore } from "../store/authStore"
import { apiPost } from "../lib/api"
import toast from "react-hot-toast"
export default function LoginPage() {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [loading, setLoading] = useState(false)

    const navigate = useNavigate()
    const { login } = useAuthStore()

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        if (!email || !password) {
            toast.error('Vui lòng nhập đầy đủ thông tin đăng nhập')
            return
        }
        setLoading(true)
        try {
            const response = await apiPost('/auth/login', {
                email, password
            })
            console.log('response: ', response)

            if (response.success && response.data) {
                login(response.data.user, response.data.token)
                toast.success('Đăng nhập thành công')
                navigate('/')
            } else {
                toast.error(response.message || ' Đăng nhập thất bại!')
            }
        } catch (error) {
            console.error('Error:', error)
            toast.error('Có lỗi xảy ra')
        } finally {
            setLoading(false)
        }
        console.log('login with: ', email)
    }

    return (
        <div className="login-container">
            <h2>Đăng nhập</h2>

            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="">Email</label>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="Nhập email"
                    />
                </div>
                <div>
                    <label htmlFor="">Mật khẩu</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)} />
                </div>
                <button type="submit" disabled={loading}>
                    {loading ? "Đang đăng nhập" : "Đăng Nhập"}
                </button>
            </form>
        </div>
    )
}