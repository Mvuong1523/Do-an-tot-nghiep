import { create } from 'zustand'

export interface User {
  id?: string
  email: string
  fullName?: string
  role: 'CUSTOMER' | 'ADMIN' | 'WAREHOUSE' | 'PRODUCT_MANAGER'
  status?: string
}

interface AuthStore {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  login: (user: User) => void
  logout: () => void
  setUser: (user: User | null) => void
  setAuth: (user: User, token: string) => void
}

export const useAuthStore = create<AuthStore>((set: any) => ({
  user: null,
  token: null,
  isAuthenticated: false,
  login: (user: User) => set({ user, isAuthenticated: true }),
  logout: () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('auth_token')
    }
    set({ user: null, isAuthenticated: false, token: null })
  },
  setUser: (user: User | null) => set({ user, isAuthenticated: !!user }),
  setAuth: (user: User, token: string) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('auth_token', token)
    }
    set({ user, token, isAuthenticated: true })
  },
}))
