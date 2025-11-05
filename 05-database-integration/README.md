# Module 05: Database Integration

## Overview
Learn reactive database access with Vert.x PostgreSQL Client. Implement CRUD operations, transactions, and connection pooling.

## Key Concepts
- **Reactive SQL Client** - Non-blocking database access
- **Connection Pooling** - Efficient resource management
- **Prepared Statements** - SQL injection prevention
- **Transactions** - ACID operations
- **Batch Operations** - Bulk inserts

## Running
```bash
# Start PostgreSQL and application
docker-compose up --build

# Test API
curl http://localhost:8080/api/users
```

## API Endpoints
- `GET /api/users` - Get all users
- `GET /api/users/:id` - Get user by ID
- `POST /api/users` - Create user
- `PUT /api/users/:id` - Update user
- `DELETE /api/users/:id` - Delete user
- `POST /api/users/batch` - Batch insert

## Learning Objectives
✅ Reactive database operations
✅ Connection pool management
✅ Prepared statements and SQL safety
✅ Transaction handling
✅ Batch operations
