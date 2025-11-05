# Module 09: Security Best Practices

## Overview
Comprehensive security implementation covering OWASP Top 10 mitigations, input validation, XSS/CSRF prevention, security headers, and rate limiting.

## Key Security Features

### 1. Security Headers
- **HSTS** - HTTP Strict Transport Security
- **CSP** - Content Security Policy
- **X-Frame-Options** - Clickjacking protection
- **X-Content-Type-Options** - MIME sniffing protection
- **X-XSS-Protection** - XSS filter
- **Referrer-Policy** - Referrer information control
- **Permissions-Policy** - Feature restrictions

### 2. Input Validation
- Username validation (alphanumeric, 3-20 chars)
- Email format validation
- Password strength requirements
- SQL injection pattern detection
- Path parameter validation

### 3. XSS Prevention
- HTML encoding with OWASP Encoder
- Output sanitization
- Safe rendering of user content

### 4. CSRF Protection
- Token generation and validation
- Session-based CSRF tokens
- Required for state-changing operations

### 5. Rate Limiting
- Per-IP rate limiting (10 req/sec)
- Brute force protection
- 429 status code with Retry-After header

### 6. SQL Injection Prevention
- Pattern-based detection
- Input sanitization
- Parameterized queries (demonstrated)

### 7. File Upload Security
- Extension whitelist (jpg, jpeg, png, pdf)
- File size limits (5MB max)
- Upload directory isolation

### 8. Request Logging
- Sanitized logging (no injection attacks in logs)
- IP address tracking
- Security event monitoring

## API Endpoints

### Public
- `GET /` - API information
- `GET /health` - Health check
- `GET /api/csrf-token` - Get CSRF token

### Protected (Require CSRF Token)
- `POST /api/register` - Register user (validated)
- `POST /api/search` - Search (SQL injection protected)
- `POST /api/comment` - Add comment (XSS protected)
- `GET /api/user/:id` - Get user (validated ID)
- `POST /api/upload` - Upload file (validated)

## Running the Module

### Using Maven
```bash
cd 09-security
mvn clean package
java -jar target/security-best-practices-fat.jar
```

### Using Docker
```bash
cd 09-security
docker-compose up --build
```

## Testing Examples

### 1. Get CSRF Token
```bash
curl http://localhost:8080/api/csrf-token
# Response: {"sessionId":"xxx","csrfToken":"yyy"}
```

### 2. Register User (with CSRF)
```bash
SESSION_ID="your-session-id"
CSRF_TOKEN="your-csrf-token"

curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -H "X-Session-ID: $SESSION_ID" \
  -H "X-CSRF-Token: $CSRF_TOKEN" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "secure123"
  }'
```

### 3. Test SQL Injection Prevention
```bash
# This will be blocked
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -H "X-Session-ID: $SESSION_ID" \
  -H "X-CSRF-Token: $CSRF_TOKEN" \
  -d '{"query":"test OR 1=1; DROP TABLE users--"}'
```

### 4. Test XSS Prevention
```bash
curl -X POST http://localhost:8080/api/comment \
  -H "Content-Type: application/json" \
  -H "X-Session-ID: $SESSION_ID" \
  -H "X-CSRF-Token: $CSRF_TOKEN" \
  -d '{"comment":"<script>alert('XSS')</script>"}'

# Output will be HTML-encoded
```

### 5. Test Rate Limiting
```bash
# Rapid requests to trigger rate limit
for i in {1..15}; do
  curl http://localhost:8080/health
done
# After 10 requests, you'll get 429 Too Many Requests
```

### 6. Test CSRF Protection
```bash
# Without CSRF token (will fail)
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"pass123"}'
# Response: 403 Forbidden
```

### 7. Validate User ID
```bash
# Valid ID (numeric)
curl http://localhost:8080/api/user/123

# Invalid ID (will be rejected)
curl http://localhost:8080/api/user/abc
curl http://localhost:8080/api/user/"1 OR 1=1"
```

### 8. File Upload
```bash
# Valid file
curl -X POST http://localhost:8080/api/upload \
  -H "X-Session-ID: $SESSION_ID" \
  -H "X-CSRF-Token: $CSRF_TOKEN" \
  -F "file=@test.jpg"

# Invalid file type (will be rejected)
curl -X POST http://localhost:8080/api/upload \
  -H "X-Session-ID: $SESSION_ID" \
  -H "X-CSRF-Token: $CSRF_TOKEN" \
  -F "file=@malicious.exe"
```

## Security Headers in Response

Every response includes these security headers:
```
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
Content-Security-Policy: default-src 'self'; script-src 'self'; ...
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), microphone=(), camera=()
```

## OWASP Top 10 Coverage

| Risk | Mitigation |
|------|------------|
| A01 Broken Access Control | CSRF tokens, rate limiting |
| A02 Cryptographic Failures | HSTS, secure headers |
| A03 Injection | SQL injection detection, input validation |
| A04 Insecure Design | Security-first architecture |
| A05 Security Misconfiguration | Secure defaults, headers |
| A06 Vulnerable Components | Latest dependencies |
| A07 Auth Failures | Rate limiting, strong validation |
| A08 Data Integrity | CSRF protection |
| A09 Logging Failures | Sanitized logging |
| A10 SSRF | Input validation |

## Best Practices Implemented

✅ **Defense in Depth** - Multiple layers of security
✅ **Fail Securely** - Errors don't expose information
✅ **Least Privilege** - Minimal permissions
✅ **Input Validation** - Whitelist approach
✅ **Output Encoding** - XSS prevention
✅ **Secure Defaults** - Security by default
✅ **Don't Trust Input** - Validate everything
✅ **Log Security Events** - Audit trail

## Learning Objectives

After this module, you understand:
✅ Implementing security headers
✅ Input validation and sanitization
✅ XSS and CSRF prevention
✅ SQL injection mitigation
✅ Rate limiting implementation
✅ Secure file upload handling
✅ OWASP Top 10 mitigations
✅ Security logging

## Production Checklist

- [ ] Use HTTPS/TLS in production
- [ ] Store secrets in environment variables
- [ ] Implement proper session management
- [ ] Add authentication (JWT/OAuth)
- [ ] Use database-backed rate limiting (Redis)
- [ ] Implement audit logging
- [ ] Regular security audits
- [ ] Dependency vulnerability scanning
- [ ] WAF (Web Application Firewall)
- [ ] Intrusion detection system

## Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP Cheat Sheet Series](https://cheatsheetseries.owasp.org/)
- [Content Security Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP)
- [Security Headers](https://securityheaders.com/)
