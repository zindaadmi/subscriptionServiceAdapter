import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

const Dashboard = () => {
  const { user, hasRole } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    if (!user) return

    if (hasRole('ADMIN')) {
      navigate('/admin')
    } else if (hasRole('AGENT')) {
      navigate('/agent')
    } else {
      navigate('/user')
    }
  }, [user, hasRole, navigate])

  return <div>Redirecting...</div>
}

export default Dashboard

