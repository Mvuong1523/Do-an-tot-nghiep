'use client'

import { useEffect } from 'react'
import { useAuthStore } from '@/store/authStore'
import { authApi } from '@/lib/api'

export default function AuthProvider({ children }: { children: React.ReactNode }) {
  const { setAuth, logout } = useAuthStore()

  useEffect(() => {
    // Auth state is already restored from localStorage by Zustand persist
    // No need to call API on every page load
    // Just verify token exists
    const token = localStorage.getItem('auth_token')
    
    if (!token) {
      // No token, ensure logged out
      logout()
    }
  }, [logout])

  return <>{children}</>
}
