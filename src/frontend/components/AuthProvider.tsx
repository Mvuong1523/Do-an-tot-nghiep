'use client'

import { useEffect } from 'react'
import { useAuthStore } from '@/store/authStore'
import { authApi } from '@/lib/api'

export default function AuthProvider({ children }: { children: React.ReactNode }) {
  const { setAuth, logout } = useAuthStore()

  useEffect(() => {
    // Restore auth state from localStorage on mount
    const token = localStorage.getItem('auth_token')
    
    if (token) {
      // Verify token and get user info
      authApi.getCurrentUser()
        .then((response) => {
          if (response.success && response.data) {
            // Determine actual role from position
            let actualRole = response.data.role
            if (response.data.role === 'EMPLOYEE' && response.data.position) {
              actualRole = response.data.position
            }
            
            setAuth(
              {
                id: response.data.id,
                email: response.data.email,
                fullName: response.data.fullName,
                role: actualRole,
                status: response.data.status,
              },
              token
            )
          } else {
            // Invalid token, clear it
            logout()
          }
        })
        .catch(() => {
          // Token expired or invalid
          logout()
        })
    }
  }, [setAuth, logout])

  return <>{children}</>
}
