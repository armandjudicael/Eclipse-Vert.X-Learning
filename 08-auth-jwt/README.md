# Module 08: Authentication and JWT

## Overview
Implement secure authentication using JWT (JSON Web Tokens) with BCrypt password hashing, role-based access control, and token refresh mechanisms.

## Key Concepts
- **JWT Authentication** - Stateless token-based auth
- **BCrypt** - Secure password hashing
- **Token Refresh** - Long-lived refresh tokens
- **RBAC** - Role-Based Access Control
- **Protected Routes** - JWT validation middleware

## Features
✅ User registration with password hashing
✅ Login with JWT generation
✅ Token refresh mechanism
✅ Protected routes requiring authentication
✅ Role-based access (admin vs user)
✅ Profile management

## API Endpoints

### Public Routes
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT
- `POST /api/auth/refresh` - Refresh access token

### Protected Routes (Require JWT)
- `GET /api/protected/profile` - Get user profile
- `PUT /api/protected/profile` - Update profile

### Admin Routes
- `GET /api/protected/admin/users` - Get all users (admin only)

## Sample Users
- **Admin**: username: `admin`, password: `admin123`
- **User**: username: `john`, password: `pass123`

## Usage Examples

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "secure123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "pass123"
  }'
```

### Access Protected Route
```bash
TOKEN="your-jwt-token-here"
curl -X GET http://localhost:8080/api/protected/profile \
  -H "Authorization: Bearer $TOKEN"
```

### Refresh Token
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "your-refresh-token"
  }'
```

## Security Features
- ✅ BCrypt password hashing (cost factor: 12)
- ✅ JWT with HS256 algorithm
- ✅ Access tokens expire in 1 hour
- ✅ Refresh tokens expire in 7 days
- ✅ Role-based authorization
- ✅ Protected routes with middleware

## Learning Objectives
✅ Implement JWT authentication
✅ Secure password storage with BCrypt
✅ Token refresh strategy
✅ Role-based access control
✅ Middleware for protected routes
✅ Security best practices

## Production Considerations
⚠️ Change JWT secret in production
⚠️ Use database instead of in-memory storage
⚠️ Implement rate limiting
⚠️ Add HTTPS/TLS
⚠️ Implement token blacklisting
⚠️ Add account lockout after failed attempts
