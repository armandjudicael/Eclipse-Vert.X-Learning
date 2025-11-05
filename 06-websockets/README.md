# Module 06: WebSockets Real-time Communication

## Overview
Build real-time applications with WebSockets in Vert.x. Learn bidirectional communication, broadcasting, and room-based messaging.

## Key Concepts
- **WebSocket Connections** - Persistent bidirectional communication
- **Broadcasting** - Send messages to all connected clients
- **Room-based Messaging** - Group communication
- **Connection Management** - Handle connects/disconnects
- **Keep-alive (Ping/Pong)** - Maintain active connections

## Features Demonstrated
- ✅ WebSocket server setup
- ✅ Connection lifecycle management
- ✅ Broadcast messages to all clients
- ✅ Room-based chat functionality
- ✅ Private messaging
- ✅ Automatic ping/pong keep-alive
- ✅ HTML/JavaScript client interface

## Running
```bash
# Build and run
mvn clean package && java -jar target/websockets-fat.jar

# Or with Docker
docker-compose up --build

# Open browser
http://localhost:8080/static/index.html
```

## Testing
1. Open multiple browser windows at `http://localhost:8080/static/index.html`
2. Test broadcast messages (all clients receive)
3. Join rooms and send room-specific messages
4. Watch connection/disconnection notifications

## WebSocket Message Types

### Client to Server
- `broadcast` - Send message to all clients
- `join-room` - Join a chat room
- `leave-room` - Leave a chat room
- `room-message` - Send message to room members
- `private-message` - Send to specific client

### Server to Client
- `welcome` - Connection established
- `user-joined` - New user connected
- `user-left` - User disconnected
- `room-joined` - Successfully joined room
- `room-message` - Message from room member
- `error` - Error notification

## Learning Objectives
✅ WebSocket protocol and handshake
✅ Bidirectional communication
✅ Connection state management
✅ Broadcasting patterns
✅ Room/group messaging
✅ Error handling in real-time apps
