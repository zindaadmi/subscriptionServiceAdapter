import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import AdminDashboard from './pages/admin/AdminDashboard'
import AgentDashboard from './pages/agent/AgentDashboard'
import UserDashboard from './pages/user/UserDashboard'
import ProtectedRoute from './components/ProtectedRoute'
import AuditLogs from './pages/admin/AuditLogs'
import Users from './pages/admin/Users'
import Devices from './pages/admin/Devices'
import Subscriptions from './pages/admin/Subscriptions'
import UserSubscriptions from './pages/agent/UserSubscriptions'
import MySubscriptions from './pages/user/MySubscriptions'

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/*"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/agent/*"
            element={
              <ProtectedRoute requiredRole={['AGENT', 'ADMIN']}>
                <AgentDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/user/*"
            element={
              <ProtectedRoute requiredRole={['USER', 'ADMIN', 'AGENT']}>
                <UserDashboard />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  )
}

export default App

