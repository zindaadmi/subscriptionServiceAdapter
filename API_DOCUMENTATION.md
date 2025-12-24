# Complete API Documentation & Code Flow

## 📋 Table of Contents
1. [All API Endpoints](#all-api-endpoints)
2. [Code Flow Diagrams](#code-flow-diagrams)
3. [Component Interactions](#component-interactions)
4. [Request/Response Examples](#requestresponse-examples)

---

## 🔌 All API Endpoints

### **Public Endpoints (No Authentication Required)**

#### 1. User Registration
```http
POST /api/auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "mobileNumber": "+1234567890"
}
```

**Response (Success - 200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

**Response (Error - 400 Bad Request):**
```json
{
  "error": "Username already exists"
}
```

---

#### 2. Login with Username/Password
```http
POST /api/auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Response (Success - 200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

**Response (Error - 400 Bad Request):**
```json
{
  "error": "Invalid username or password"
}
```

---

#### 3. Login with Mobile Number
```http
POST /api/auth/login/mobile
Content-Type: application/json
```

**Request Body:**
```json
{
  "mobileNumber": "+1234567890",
  "password": "password123"
}
```

**Response (Success - 200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

**Response (Error - 400 Bad Request):**
```json
{
  "error": "Invalid mobile number or password"
}
```

---

#### 4. Google OAuth2 Login (Redirect Flow)
```http
GET /oauth2/authorization/google
```

**Flow:**
1. User clicks/login → Redirects to Google login page
2. User authenticates with Google
3. Google redirects back to: `/login/oauth2/code/google`
4. Spring Security processes OAuth2 callback
5. Redirects to: `/api/auth/oauth2/success`
6. Returns JWT token in response

**Response (Success - 200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "user@gmail.com",
  "email": "user@gmail.com",
  "roles": ["ROLE_USER"]
}
```

**Response (Error - 400 Bad Request):**
```json
{
  "error": "OAuth2 authentication failed"
}
```

---

#### 5. OAuth2 Success Callback
```http
GET /api/auth/oauth2/success
```
*This endpoint is called automatically after successful Google OAuth2 authentication*

---

#### 6. OAuth2 Failure Callback
```http
GET /api/auth/oauth2/failure
```
*This endpoint is called if Google OAuth2 authentication fails*

---

### **Protected Endpoints (Require JWT Token)**

#### 7. Get Current User Info
```http
GET /api/auth/me
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200 OK):**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "mobileNumber": "+1234567890",
  "provider": "LOCAL",
  "roles": ["ROLE_USER"]
}
```

**Response (Error - 400 Bad Request):**
```json
{
  "error": "Not authenticated"
}
```

---

#### 8. User Profile (Requires: USER, ADMIN, or MODERATOR role)
```http
GET /api/user/profile
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200 OK):**
```json
{
  "message": "This is a user profile endpoint"
}
```

**Response (Error - 403 Forbidden):**
```json
{
  "error": "Access Denied"
}
```

---

#### 9. Admin Dashboard (Requires: ADMIN role)
```http
GET /api/admin/dashboard
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200 OK):**
```json
{
  "message": "This is an admin dashboard endpoint"
}
```

**Response (Error - 403 Forbidden):**
```json
{
  "error": "Access Denied"
}
```

---

#### 10. Moderator Panel (Requires: MODERATOR or ADMIN role)
```http
GET /api/moderator/panel
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200 OK):**
```json
{
  "message": "This is a moderator panel endpoint"
}
```

**Response (Error - 403 Forbidden):**
```json
{
  "error": "Access Denied"
}
```

---

## 🔄 Code Flow Diagrams

### **Flow 1: User Registration**

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ POST /api/auth/register
       │ {username, email, password, mobileNumber}
       ▼
┌─────────────────────┐
│  AuthController     │
│  registerUser()     │
└──────┬──────────────┘
       │
       │ userService.registerUser()
       ▼
┌─────────────────────┐
│    UserService      │
│  registerUser()     │
└──────┬──────────────┘
       │
       ├─► Check username exists
       ├─► Check email exists
       ├─► Check mobile exists
       │
       │ passwordEncoder.encode(password)
       │
       ├─► Get ROLE_USER from RoleRepository
       ├─► Create User entity
       ├─► Set provider = LOCAL
       └─► Save to database
       │
       ▼
┌─────────────────────┐
│  UserRepository     │
│      .save()        │
└──────┬──────────────┘
       │
       │ Return User
       ▼
┌─────────────────────┐
│  AuthController     │
│  (back to)          │
└──────┬──────────────┘
       │
       │ userDetailsService.loadUserByUsername()
       │ jwtTokenUtil.generateToken()
       │
       ▼
┌─────────────────────┐
│   JWT Response      │
│  {token, username,   │
│   email, roles}     │
└─────────────────────┘
```

---

### **Flow 2: Username/Password Login**

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ POST /api/auth/login
       │ {username, password}
       ▼
┌─────────────────────┐
│  AuthController     │
│  authenticateUser() │
└──────┬──────────────┘
       │
       │ authenticationManager.authenticate()
       │ UsernamePasswordAuthenticationToken(username, password)
       ▼
┌─────────────────────┐
│ AuthenticationManager│
└──────┬──────────────┘
       │
       │ Uses DaoAuthenticationProvider
       ▼
┌─────────────────────┐
│DaoAuthentication    │
│    Provider         │
└──────┬──────────────┘
       │
       │ userDetailsService.loadUserByUsername(username)
       ▼
┌─────────────────────┐
│CustomUserDetails    │
│      Service        │
└──────┬──────────────┘
       │
       │ userRepository.findByUsername()
       ▼
┌─────────────────────┐
│  UserRepository     │
└──────┬──────────────┘
       │
       │ Returns User (implements UserDetails)
       │
       │ passwordEncoder.matches(password, user.password)
       ▼
┌─────────────────────┐
│ AuthenticationManager│
│  (validates)        │
└──────┬──────────────┘
       │
       │ Returns Authentication object
       ▼
┌─────────────────────┐
│  AuthController     │
│  (back to)          │
└──────┬──────────────┘
       │
       │ SecurityContextHolder.setAuthentication()
       │ jwtTokenUtil.generateToken(userDetails)
       │
       ▼
┌─────────────────────┐
│   JWT Response      │
└─────────────────────┘
```

---

### **Flow 3: Mobile Login**

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ POST /api/auth/login/mobile
       │ {mobileNumber, password}
       ▼
┌─────────────────────┐
│  AuthController     │
│authenticateUserBy   │
│     Mobile()        │
└──────┬──────────────┘
       │
       │ userDetailsService.loadUserByMobileNumber()
       ▼
┌─────────────────────┐
│CustomUserDetails    │
│      Service        │
└──────┬──────────────┘
       │
       │ userRepository.findByMobileNumber()
       ▼
┌─────────────────────┐
│  UserRepository     │
└──────┬──────────────┘
       │
       │ Returns UserDetails
       ▼
┌─────────────────────┐
│  AuthController     │
│  (back to)          │
└──────┬──────────────┘
       │
       │ authenticationManager.authenticate()
       │ (uses username from UserDetails)
       │
       │ [Same authentication flow as username/password]
       │
       │ jwtTokenUtil.generateToken()
       ▼
┌─────────────────────┐
│   JWT Response      │
└─────────────────────┘
```

---

### **Flow 4: Google OAuth2 Login**

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ GET /oauth2/authorization/google
       ▼
┌─────────────────────┐
│  Spring Security    │
│   OAuth2 Filter     │
└──────┬──────────────┘
       │
       │ Redirects to Google
       ▼
┌─────────────────────┐
│  Google Login Page  │
└──────┬──────────────┘
       │
       │ User authenticates
       │
       │ Redirect: /login/oauth2/code/google
       ▼
┌─────────────────────┐
│  Spring Security    │
│  OAuth2 Callback    │
│     Handler         │
└──────┬──────────────┘
       │
       │ Processes OAuth2 response
       │ Extracts user info from Google
       │
       │ Redirect: /api/auth/oauth2/success
       ▼
┌─────────────────────┐
│  AuthController     │
│   oauth2Success()   │
└──────┬──────────────┘
       │
       │ SecurityContextHolder.getAuthentication()
       │ Extract OAuth2User
       │ Get email, name, providerId (sub)
       │
       │ userService.findByEmail(email)
       │
       ├─► If user exists: Use existing user
       └─► If not: userService.registerGoogleUser()
       │
       ▼
┌─────────────────────┐
│    UserService      │
│ registerGoogleUser() │
└──────┬──────────────┘
       │
       ├─► Check if user exists by providerId
       ├─► Check if user exists by email
       │   └─► If yes: Link Google account
       └─► Create new user with:
           - username = email
           - provider = GOOGLE
           - providerId = sub
           - role = ROLE_USER
       │
       │ userDetailsService.loadUserByUsername()
       │ jwtTokenUtil.generateToken()
       ▼
┌─────────────────────┐
│   JWT Response      │
└─────────────────────┘
```

---

### **Flow 5: Protected Endpoint Access (JWT Authentication)**

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ GET /api/user/profile
       │ Authorization: Bearer <JWT_TOKEN>
       ▼
┌─────────────────────┐
│  JwtAuthentication  │
│      Filter         │
└──────┬──────────────┘
       │
       │ Extract token from Authorization header
       │ jwtTokenUtil.getUsernameFromToken(token)
       │
       ├─► If token invalid/expired: Continue (no auth)
       └─► If token valid:
           │
           │ userDetailsService.loadUserByUsername(username)
           │ jwtTokenUtil.validateToken(token, userDetails)
           │
           │ If valid:
           │   SecurityContextHolder.setAuthentication()
           │
       ▼
┌─────────────────────┐
│  SecurityConfig      │
│  Authorization      │
└──────┬──────────────┘
       │
       │ Check @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
       │ Check user roles from SecurityContext
       │
       ├─► If authorized: Allow access
       └─► If not: Return 403 Forbidden
       │
       ▼
┌─────────────────────┐
│  UserController     │
│   getProfile()      │
└──────┬──────────────┘
       │
       │ Return response
       ▼
┌─────────────────────┐
│   JSON Response     │
└─────────────────────┘
```

---

## 🧩 Component Interactions

### **Key Components:**

1. **AuthController** (`/api/auth/*`)
   - Handles all authentication endpoints
   - Uses `AuthenticationManager` for credential validation
   - Uses `UserService` for user operations
   - Uses `JwtTokenUtil` for token generation

2. **UserService**
   - Business logic for user registration
   - Handles both LOCAL and GOOGLE user registration
   - Validates uniqueness (username, email, mobile)
   - Assigns default roles

3. **CustomUserDetailsService**
   - Implements `UserDetailsService`
   - Loads users by username, email, or mobile number
   - Returns `User` entity (implements `UserDetails`)

4. **JwtAuthenticationFilter**
   - Intercepts all requests
   - Extracts JWT token from Authorization header
   - Validates token and sets authentication in SecurityContext
   - Runs before `UsernamePasswordAuthenticationFilter`

5. **SecurityConfig**
   - Configures Spring Security
   - Defines security filter chain
   - Sets up OAuth2 login
   - Configures role-based access control
   - Configures CORS

6. **JwtTokenUtil**
   - Generates JWT tokens
   - Validates JWT tokens
   - Extracts claims from tokens

---

## 📝 Request/Response Examples

### **Example 1: Complete Registration Flow**

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "securepass123",
    "mobileNumber": "+1234567890"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huZG9lIiwiZXhwIjoxNzA5ODc2ODAwLCJpYXQiOjE3MDk3OTA0MDB9...",
  "type": "Bearer",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

---

### **Example 2: Login and Access Protected Endpoint**

**Step 1: Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "securepass123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

**Step 2: Use Token to Access Protected Endpoint**
```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response:**
```json
{
  "message": "This is a user profile endpoint"
}
```

---

### **Example 3: Mobile Login**

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login/mobile \
  -H "Content-Type: application/json" \
  -d '{
    "mobileNumber": "+1234567890",
    "password": "securepass123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

---

### **Example 4: Get Current User Info**

**Request:**
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "mobileNumber": "+1234567890",
  "provider": "LOCAL",
  "roles": ["ROLE_USER"]
}
```

---

## 🔐 Security Flow Summary

### **JWT Token Lifecycle:**

1. **Token Generation:**
   - User authenticates (login/register/OAuth2)
   - `JwtTokenUtil.generateToken()` creates JWT
   - Token contains: username, roles, expiration
   - Token signed with secret key

2. **Token Usage:**
   - Client includes token in `Authorization: Bearer <token>` header
   - `JwtAuthenticationFilter` intercepts request
   - Extracts and validates token
   - Sets authentication in SecurityContext

3. **Token Validation:**
   - Extract username from token
   - Load user from database
   - Verify token signature
   - Check token expiration
   - Validate username matches

4. **Authorization:**
   - Check user roles from SecurityContext
   - Match against endpoint requirements
   - Allow or deny access

---

## 📊 Database Schema

### **Users Table:**
- `id` (Long, Primary Key)
- `username` (String, Unique)
- `email` (String, Unique)
- `password` (String, Encrypted)
- `mobile_number` (String, Unique)
- `provider` (Enum: LOCAL, GOOGLE)
- `provider_id` (String)
- `enabled` (Boolean)

### **Roles Table:**
- `id` (Long, Primary Key)
- `name` (Enum: ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR)

### **User_Roles Table (Join Table):**
- `user_id` (Foreign Key)
- `role_id` (Foreign Key)

---

## 🎯 Role-Based Access Control (RBAC)

### **Role Hierarchy:**
- **ROLE_USER**: Basic user access
- **ROLE_MODERATOR**: Moderator + User access
- **ROLE_ADMIN**: Admin + Moderator + User access

### **Endpoint Protection:**
- `/api/user/**` → Requires: USER, ADMIN, or MODERATOR
- `/api/moderator/**` → Requires: MODERATOR or ADMIN
- `/api/admin/**` → Requires: ADMIN only

---

## 🔧 Configuration Files

### **application.properties:**
```properties
# JWT Configuration
jwt.secret=your-secret-key-change-this-in-production
jwt.expiration=86400000  # 24 hours

# OAuth2 Google
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
```

---

This documentation covers all APIs and code flows in the authentication service!

