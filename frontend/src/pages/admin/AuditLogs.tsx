import { useState, useEffect } from 'react'
import { auditAPI } from '../../services/api'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Button } from '../../components/ui/button'
import { Input } from '../../components/ui/input'

interface AuditLog {
  id: number
  entityType: string
  entityId: number
  action: string
  username: string
  userRole: string
  description: string
  timestamp: string
  success: boolean
  errorMessage?: string
}

const AuditLogs = () => {
  const [logs, setLogs] = useState<AuditLog[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(false)
  const [searchKeyword, setSearchKeyword] = useState('')
  const [filterAction, setFilterAction] = useState('')
  const [filterEntityType, setFilterEntityType] = useState('')

  useEffect(() => {
    loadLogs()
  }, [page, filterAction, filterEntityType, searchKeyword])

  const loadLogs = async () => {
    setLoading(true)
    try {
      let response
      if (searchKeyword) {
        response = await auditAPI.search(searchKeyword, page, 50)
      } else if (filterAction) {
        response = await auditAPI.getByAction(filterAction, page, 50)
      } else if (filterEntityType) {
        response = await auditAPI.getByEntityType(filterEntityType, page, 50)
      } else {
        response = await auditAPI.getAll(page, 50)
      }
      setLogs(response.data.content || response.data)
      setTotalPages(response.data.totalPages || 1)
    } catch (error) {
      console.error('Failed to load audit logs:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Audit Logs</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex gap-4 mb-4">
            <Input
              placeholder="Search..."
              value={searchKeyword}
              onChange={(e) => {
                setSearchKeyword(e.target.value)
                setPage(0)
              }}
              className="max-w-sm"
            />
            <select
              value={filterAction}
              onChange={(e) => {
                setFilterAction(e.target.value)
                setPage(0)
              }}
              className="px-3 py-2 border rounded-md"
            >
              <option value="">All Actions</option>
              <option value="CREATE">CREATE</option>
              <option value="UPDATE">UPDATE</option>
              <option value="DELETE">DELETE</option>
              <option value="SOFT_DELETE">SOFT_DELETE</option>
              <option value="RESTORE">RESTORE</option>
              <option value="ASSIGN">ASSIGN</option>
              <option value="CANCEL">CANCEL</option>
            </select>
            <select
              value={filterEntityType}
              onChange={(e) => {
                setFilterEntityType(e.target.value)
                setPage(0)
              }}
              className="px-3 py-2 border rounded-md"
            >
              <option value="">All Entities</option>
              <option value="User">User</option>
              <option value="Device">Device</option>
              <option value="Subscription">Subscription</option>
              <option value="Billing">Billing</option>
              <option value="Feature">Feature</option>
            </select>
          </div>

          {loading ? (
            <div>Loading...</div>
          ) : (
            <div className="space-y-2">
              {logs.map((log) => (
                <div
                  key={log.id}
                  className={`p-4 border rounded-lg ${
                    log.success ? 'bg-white' : 'bg-red-50'
                  }`}
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <div className="font-semibold">
                        {log.action} - {log.entityType} #{log.entityId}
                      </div>
                      <div className="text-sm text-gray-600">{log.description}</div>
                      <div className="text-xs text-gray-500 mt-1">
                        By {log.username} ({log.userRole}) at {new Date(log.timestamp).toLocaleString()}
                      </div>
                      {log.errorMessage && (
                        <div className="text-sm text-red-600 mt-1">
                          Error: {log.errorMessage}
                        </div>
                      )}
                    </div>
                    <span
                      className={`px-2 py-1 rounded text-xs ${
                        log.success
                          ? 'bg-green-100 text-green-800'
                          : 'bg-red-100 text-red-800'
                      }`}
                    >
                      {log.success ? 'Success' : 'Failed'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}

          <div className="flex justify-between items-center mt-4">
            <Button
              onClick={() => setPage(page - 1)}
              disabled={page === 0}
              variant="outline"
            >
              Previous
            </Button>
            <span>
              Page {page + 1} of {totalPages}
            </span>
            <Button
              onClick={() => setPage(page + 1)}
              disabled={page >= totalPages - 1}
              variant="outline"
            >
              Next
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

export default AuditLogs

