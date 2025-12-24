import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Label } from '../components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card'

const Login = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [mobileNumber, setMobileNumber] = useState('')
  const [loginType, setLoginType] = useState<'username' | 'mobile'>('username')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  
  const { login, loginMobile } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      if (loginType === 'username') {
        await login(username, password)
      } else {
        await loginMobile(mobileNumber, password)
      }
      navigate('/')
    } catch (err: any) {
      setError(err.response?.data?.error || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Login to Subscription Service</CardTitle>
          <CardDescription>Enter your credentials to access your account</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex gap-2 mb-4">
            <Button
              variant={loginType === 'username' ? 'default' : 'outline'}
              onClick={() => setLoginType('username')}
              className="flex-1"
            >
              Username
            </Button>
            <Button
              variant={loginType === 'mobile' ? 'default' : 'outline'}
              onClick={() => setLoginType('mobile')}
              className="flex-1"
            >
              Mobile
            </Button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {loginType === 'username' ? (
              <div className="space-y-2">
                <Label htmlFor="username">Username</Label>
                <Input
                  id="username"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                />
              </div>
            ) : (
              <div className="space-y-2">
                <Label htmlFor="mobile">Mobile Number</Label>
                <Input
                  id="mobile"
                  type="text"
                  value={mobileNumber}
                  onChange={(e) => setMobileNumber(e.target.value)}
                  required
                />
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            {error && (
              <div className="text-sm text-red-500">{error}</div>
            )}

            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? 'Logging in...' : 'Login'}
            </Button>
          </form>

          <div className="mt-4 text-center">
            <a
              href="http://localhost:8080/oauth2/authorization/google"
              className="text-sm text-blue-600 hover:underline"
            >
              Login with Google
            </a>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

export default Login

