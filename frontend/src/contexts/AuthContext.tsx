import React, { createContext, useContext, useState, useEffect } from 'react'
import { authAPI } from '../services/api'

interface User {
  id: number
  username: string
  email: string
  roles: string[]
}

interface AuthContextType {
  user: User | null
  token: string | null
  refreshToken: string | null
  login: (username: string, password: string) => Promise<void>
  loginMobile: (mobileNumber: string, password: string) => Promise<void>
  logout: () => Promise<void>
  refreshAccessToken: () => Promise<void>
  isAuthenticated: boolean
  hasRole: (role: string) => boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [refreshToken, setRefreshToken] = useState<string | null>(null)

  useEffect(() => {
    const storedToken = localStorage.getItem('token')
    const storedRefreshToken = localStorage.getItem('refreshToken')
    const storedUser = localStorage.getItem('user')
    
    if (storedToken && storedUser) {
      setToken(storedToken)
      setUser(JSON.parse(storedUser))
    }
    if (storedRefreshToken) {
      setRefreshToken(storedRefreshToken)
    }
  }, [])

  const login = async (username: string, password: string) => {
    const response = await authAPI.login(username, password)
    const { token: newToken, refreshToken: newRefreshToken, user: newUser } = response.data
    setToken(newToken)
    setRefreshToken(newRefreshToken)
    setUser(newUser)
    localStorage.setItem('token', newToken)
    if (newRefreshToken) {
      localStorage.setItem('refreshToken', newRefreshToken)
    }
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  const loginMobile = async (mobileNumber: string, password: string) => {
    const response = await authAPI.loginMobile(mobileNumber, password)
    const { token: newToken, refreshToken: newRefreshToken, user: newUser } = response.data
    setToken(newToken)
    setRefreshToken(newRefreshToken)
    setUser(newUser)
    localStorage.setItem('token', newToken)
    if (newRefreshToken) {
      localStorage.setItem('refreshToken', newRefreshToken)
    }
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  const logout = async () => {
    try {
      // Call logout API to blacklist token
      await authAPI.logout()
    } catch (error) {
      console.error('Logout API error:', error)
      // Continue with logout even if API call fails
    } finally {
      setToken(null)
      setRefreshToken(null)
      setUser(null)
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    }
  }

  const refreshAccessToken = async () => {
    try {
      const storedRefreshToken = localStorage.getItem('refreshToken')
      if (!storedRefreshToken) {
        throw new Error('No refresh token available')
      }

      const response = await authAPI.refreshToken(storedRefreshToken)
      const { accessToken, refreshToken: newRefreshToken } = response.data
      
      setToken(accessToken)
      localStorage.setItem('token', accessToken)
      
      if (newRefreshToken) {
        setRefreshToken(newRefreshToken)
        localStorage.setItem('refreshToken', newRefreshToken)
      }
    } catch (error) {
      console.error('Token refresh error:', error)
      // If refresh fails, logout user
      await logout()
      throw error
    }
  }

  const hasRole = (role: string): boolean => {
    if (!user) return false
    return user.roles.some(r => r.includes(role))
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        refreshToken,
        login,
        loginMobile,
        logout,
        refreshAccessToken,
        isAuthenticated: !!token,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

