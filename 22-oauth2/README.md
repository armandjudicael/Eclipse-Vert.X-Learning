# Module 22: OAuth2 and Social Login

## Overview
This module demonstrates implementing OAuth2 authentication and social login integration with Eclipse Vert.x.

## Key Concepts Covered

### 1. **OAuth2 Flow**
- Authorization code flow
- Implicit flow
- Client credentials flow
- Resource owner password flow

### 2. **Social Login Providers**
- Google OAuth
- GitHub OAuth
- Facebook Login
- Microsoft Azure AD

### 3. **Advanced Features**
- Token refresh
- Scope management
- User profile federation
- Multi-provider support

### 4. **Best Practices**
- Security considerations
- State parameter validation
- PKCE for mobile apps
- Token storage

## Project Structure
```
22-oauth2/
├── src/main/java/com/vertx/oauth2/
│   └── MainVerticle.java
├── src/main/resources/
│   └── logback.xml
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Running the Application

### Using Docker Compose
```bash
cd 22-oauth2
docker-compose up --build

# Access login page
# http://localhost:8080/login
```

## OAuth2 Configuration

```java
OAuth2Options options = new OAuth2Options()
    .setClientID("your-client-id")
    .setClientSecret("your-client-secret")
    .setTokenPath("https://oauth.example.com/token")
    .setAuthorizationPath("https://oauth.example.com/authorize");
```

## Learning Objectives
- ✅ OAuth2 protocol and flows
- ✅ Social login integration
- ✅ Token management
- ✅ User profile handling
- ✅ Security best practices

## References
- [OAuth 2.0 Specification](https://tools.ietf.org/html/rfc6749)
- [OpenID Connect](https://openid.net/connect/)
- [Vert.x OAuth2](https://vertx.io/docs/vertx-auth-oauth2/java/)