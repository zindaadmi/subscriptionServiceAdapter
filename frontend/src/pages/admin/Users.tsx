import { useState, useEffect } from 'react'
import { adminAPI } from '../../services/api'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Button } from '../../components/ui/button'

interface User {
  id: number
  username: string
  email: string
  deleted: boolean
  enabled: boolean
}

const Users = () => {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    loadUsers()
  }, [])

  const loadUsers = async () => {
    setLoading(true)
    try {
      // This would need a proper endpoint
      // For now, showing placeholder
      setUsers([])
    } catch (error) {
      console.error('Failed to load users:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSoftDelete = async (id: number) => {
    if (!confirm('Are you sure you want to soft delete this user?')) return
    
    try {
      await adminAPI.softDeleteUser(id)
      loadUsers()
    } catch (error) {
      console.error('Failed to soft delete user:', error)
    }
  }

  const handleRestore = async (id: number) => {
    try {
      await adminAPI.restoreUser(id)
      loadUsers()
    } catch (error) {
      console.error('Failed to restore user:', error)
    }
  }

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Users Management</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div>Loading...</div>
          ) : (
            <div className="space-y-2">
              {users.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  No users found. API endpoint needed.
                </div>
              ) : (
                users.map((user) => (
                  <div key={user.id} className="p-4 border rounded-lg flex justify-between items-center">
                    <div>
                      <div className="font-semibold">{user.username}</div>
                      <div className="text-sm text-gray-600">{user.email}</div>
                      {user.deleted && (
                        <span className="text-xs text-red-600">Deleted</span>
                      )}
                    </div>
                    <div className="flex gap-2">
                      {user.deleted ? (
                        <Button onClick={() => handleRestore(user.id)} variant="outline">
                          Restore
                        </Button>
                      ) : (
                        <Button
                          onClick={() => handleSoftDelete(user.id)}
                          variant="destructive"
                        >
                          Soft Delete
                        </Button>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

export default Users

