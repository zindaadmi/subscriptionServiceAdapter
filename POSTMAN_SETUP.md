# Postman Collection Setup Guide

## Importing the Collection

1. **Open Postman**
2. Click **Import** button (top left)
3. Select **File** tab
4. Choose `Subscription_Service.postman_collection.json`
5. Click **Import**

## Environment Variables

Create a new environment in Postman with these variables:

```
baseUrl: http://localhost:8080
token: (will be auto-set after login)
userId: (will be auto-set after login)
subscriptionId: 1
deviceId: 1
deviceApiKey: 550e8400e29b41d4a716446655440000
userSubscriptionId: 1
billingId: 1
```

## Quick Start

### 1. Login First
1. Go to **Authentication** → **Login (Username/Password)**
2. Use credentials: `admin` / `admin123`
3. Token will be automatically saved to `{{token}}` variable

### 2. Test Admin APIs
- All Admin APIs require ADMIN role
- Token from admin login will work

### 3. Test Agent APIs
- Login as `agent` / `agent123`
- Agent can view subscriptions but not create

### 4. Test User APIs
- Login as `user` / `user123`
- User can view own subscriptions

### 5. Test Device Verification
- Uses API key authentication
- No JWT token needed
- Use `X-API-Key` header

## Collection Structure

```
Subscription Service API
├── Authentication
│   ├── Register User
│   ├── Login (Username/Password) [Auto-saves token]
│   ├── Login (Mobile)
│   └── Get Current User
├── Admin APIs
│   ├── Users (Soft Delete, Restore)
│   ├── Subscriptions (Create, Assign)
│   ├── Features (Create, Manage)
│   └── Audit Logs (View, Search, Statistics)
├── Agent APIs
│   ├── Subscriptions (View Only)
│   ├── User Subscriptions (Assign, Manage)
│   └── Devices (Create, Assign)
├── User APIs
│   ├── Profile (Get, Update)
│   └── Subscriptions (View, Cancel)
├── Device Verification
│   ├── Verify Subscription
│   ├── Get Device Info
│   └── Health Check
├── Billing
│   ├── Generate Monthly Bills
│   ├── Get Pending Bills
│   └── Mark Bill as Paid
└── Migration
    └── Import User
```

## Testing Workflow

### Complete User Journey

1. **Register New User**
   - Authentication → Register User

2. **Login**
   - Authentication → Login (Username/Password)
   - Token auto-saved

3. **Admin: Create Subscription**
   - Admin APIs → Subscriptions → Create Subscription

4. **Admin: Assign to User**
   - Admin APIs → Subscriptions → Assign Subscription to User

5. **Agent: Assign Device**
   - Agent APIs → Devices → Assign Device to User

6. **User: View Subscriptions**
   - User APIs → Get My Subscriptions

7. **Device: Verify Access**
   - Device Verification → Verify Subscription

## Tips

- **Auto Token Saving**: Login requests automatically save token
- **Environment Variables**: Update variables as you test
- **Error Responses**: Check response body for error details
- **Status Codes**: 
  - 200 = Success
  - 400 = Bad Request
  - 401 = Unauthorized
  - 403 = Forbidden
  - 404 = Not Found
  - 500 = Server Error

## Example Requests

### Create Subscription (Admin)
```json
POST /api/admin/subscriptions
{
  "name": "Premium Plan",
  "deviceId": 1,
  "basePrice": 150.00,
  "level": "PREMIUM",
  "billingCycle": "MONTHLY",
  "featureIds": [1, 2, 3]
}
```

### Assign Subscription (Agent)
```json
POST /api/agent/user-subscriptions/assign
{
  "subscriptionId": 1,
  "userId": 3,
  "negotiatedPrice": 120.00,
  "durationMonths": 12
}
```

### Device Verification
```json
POST /api/device/verify-subscription
Headers: X-API-Key: 550e8400e29b41d4a716446655440000
Body: {
  "deviceSerial": "USER-TV-001"
}
```

