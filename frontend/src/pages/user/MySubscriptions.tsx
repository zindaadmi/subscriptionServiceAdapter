import { useState, useEffect } from 'react'
import { userAPI } from '../../services/api'
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card'
import { Button } from '../../components/ui/button'

const MySubscriptions = () => {
  const [subscriptions, setSubscriptions] = useState<any[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    loadSubscriptions()
  }, [])

  const loadSubscriptions = async () => {
    setLoading(true)
    try {
      const response = await userAPI.getSubscriptions()
      setSubscriptions(response.data)
    } catch (error) {
      console.error('Failed to load subscriptions:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleCancel = async (id: number) => {
    if (!confirm('Are you sure you want to cancel this subscription?')) return
    
    try {
      await userAPI.cancelSubscription(id)
      loadSubscriptions()
    } catch (error) {
      console.error('Failed to cancel subscription:', error)
    }
  }

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>My Subscriptions</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div>Loading...</div>
          ) : (
            <div className="space-y-2">
              {subscriptions.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  No subscriptions found
                </div>
              ) : (
                subscriptions.map((sub) => (
                  <div key={sub.id} className="p-4 border rounded-lg flex justify-between items-center">
                    <div>
                      <div className="font-semibold">{sub.subscription?.name}</div>
                      <div className="text-sm text-gray-600">
                        Price: ₹{sub.negotiatedPrice} | Status: {sub.status}
                      </div>
                    </div>
                    {sub.status === 'ACTIVE' && (
                      <Button
                        onClick={() => handleCancel(sub.id)}
                        variant="destructive"
                      >
                        Cancel
                      </Button>
                    )}
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

export default MySubscriptions

