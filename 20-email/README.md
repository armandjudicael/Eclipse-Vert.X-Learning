# Module 20: Email Service Integration

## Overview
This module demonstrates integrating email services with Eclipse Vert.x for sending transactional and marketing emails.

## Key Concepts Covered

### 1. **Email Sending**
- SMTP configuration
- HTML and plain text emails
- Attachments
- Templating

### 2. **Email Services**
- SendGrid integration
- AWS SES integration
- Mailgun integration
- Custom SMTP servers

### 3. **Advanced Features**
- Email queuing
- Retry logic
- Bounce handling
- Delivery tracking

### 4. **Best Practices**
- Template management
- Error handling
- Rate limiting
- Monitoring

## Project Structure
```
20-email/
├── src/main/java/com/vertx/email/
│   └── MainVerticle.java
├── src/main/resources/
│   ├── logback.xml
│   └── templates/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Running the Application

### Using Docker Compose
```bash
cd 20-email
docker-compose up --build

# Send email
curl -X POST http://localhost:8080/api/email/send \
  -H "Content-Type: application/json" \
  -d '{"to":"user@example.com","subject":"Test","body":"Hello"}'
```

## Learning Objectives
- ✅ SMTP configuration
- ✅ Email service integration
- ✅ Template management
- ✅ Error handling and retries
- ✅ Monitoring and analytics

## References
- [SendGrid Documentation](https://docs.sendgrid.com/)
- [AWS SES Documentation](https://docs.aws.amazon.com/ses/)
- [SMTP Protocol](https://tools.ietf.org/html/rfc5321)