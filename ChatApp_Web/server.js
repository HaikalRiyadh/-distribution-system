/**
 * ============================================================
 *  ChatApp Web Server - Socket.io Based
 *  Distributed Systems UTS Project - 2025/2026
 * ============================================================
 *  Fitur:
 *  - Multi-room chat system
 *  - Device-to-device communication
 *  - Real-time messaging dengan WebSocket
 *  - User presence tracking
 *  - Message history per room
 * ============================================================
 */

const express = require('express');
const http = require('http');
const socketIO = require('socket.io');
const path = require('path');

const app = express();
const server = http.createServer(app);
const io = socketIO(server, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});

const PORT = process.env.PORT || 3000;

// Data storage
const rooms = new Map();       // Simpan data per room
const userSockets = new Map(); // Track socket -> user mapping

// Serve static files (HTML, CSS, JS)
app.use(express.static(path.join(__dirname, 'public')));

// Route untuk homepage
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// ============================================================
// Socket.IO Events
// ============================================================

io.on('connection', (socket) => {
    console.log(`[CONNECT] User connected: ${socket.id}`);

    // ============================================================
    // Event: User Join Room
    // ============================================================
    socket.on('join-room', (data) => {
        const { roomName, userName } = data;

        // Inisialisasi room jika belum ada
        if (!rooms.has(roomName)) {
            rooms.set(roomName, {
                name: roomName,
                users: [],
                messages: [],
                createdAt: new Date()
            });
        }

        const room = rooms.get(roomName);
        
        // Tambahkan user ke room
        room.users.push({
            id: socket.id,
            name: userName,
            joinedAt: new Date()
        });

        // Track user
        userSockets.set(socket.id, { roomName, userName });

        // Join socket ke room
        socket.join(roomName);

        // Notifikasi ke semua user di room
        io.to(roomName).emit('user-joined', {
            userName: userName,
            message: `${userName} telah bergabung ke room`,
            timestamp: new Date(),
            userCount: room.users.length
        });

        // Kirim data room dan daftar user ke client yang baru
        socket.emit('room-data', {
            roomName: roomName,
            users: room.users,
            messages: room.messages,
            userCount: room.users.length
        });

        console.log(`[JOIN] ${userName} joined room: ${roomName}`);
    });

    // ============================================================
    // Event: Send Message
    // ============================================================
    socket.on('send-message', (data) => {
        const userData = userSockets.get(socket.id);
        
        if (!userData) return;

        const { roomName, userName } = userData;
        const room = rooms.get(roomName);

        if (!room) return;

        const message = {
            id: socket.id,
            userName: userName,
            text: data.text,
            timestamp: new Date(),
            type: 'message'
        };

        // Simpan ke history room
        room.messages.push(message);

        // Batasi history ke 100 pesan terakhir
        if (room.messages.length > 100) {
            room.messages.shift();
        }

        // Broadcast ke semua user di room
        io.to(roomName).emit('receive-message', message);

        console.log(`[MSG] ${userName} in ${roomName}: ${data.text}`);
    });

    // ============================================================
    // Event: Typing Indicator
    // ============================================================
    socket.on('typing', (data) => {
        const userData = userSockets.get(socket.id);
        if (!userData) return;

        socket.to(userData.roomName).emit('user-typing', {
            userName: userData.userName
        });
    });

    socket.on('stop-typing', (data) => {
        const userData = userSockets.get(socket.id);
        if (!userData) return;

        socket.to(userData.roomName).emit('user-stop-typing', {
            userName: userData.userName
        });
    });

    // ============================================================
    // Event: Get Rooms List
    // ============================================================
    socket.on('get-rooms', () => {
        const roomsList = Array.from(rooms.values()).map(room => ({
            name: room.name,
            userCount: room.users.length,
            createdAt: room.createdAt,
            lastMessageTime: room.messages.length > 0 
                ? room.messages[room.messages.length - 1].timestamp 
                : null
        }));

        socket.emit('rooms-list', roomsList);
    });

    // ============================================================
    // Event: User Leave Room
    // ============================================================
    socket.on('leave-room', () => {
        const userData = userSockets.get(socket.id);
        
        if (userData) {
            const { roomName, userName } = userData;
            const room = rooms.get(roomName);

            if (room) {
                // Hapus user dari daftar room
                room.users = room.users.filter(u => u.id !== socket.id);

                // Notifikasi ke room
                io.to(roomName).emit('user-left', {
                    userName: userName,
                    message: `${userName} telah meninggalkan room`,
                    timestamp: new Date(),
                    userCount: room.users.length
                });

                // Hapus room jika kosong
                if (room.users.length === 0) {
                    rooms.delete(roomName);
                    console.log(`[DELETE] Empty room removed: ${roomName}`);
                }
            }

            socket.leave(roomName);
            userSockets.delete(socket.id);
            console.log(`[LEAVE] ${userName} left room: ${roomName}`);
        }
    });

    // ============================================================
    // Event: User Disconnect
    // ============================================================
    socket.on('disconnect', () => {
        const userData = userSockets.get(socket.id);
        
        if (userData) {
            const { roomName, userName } = userData;
            const room = rooms.get(roomName);

            if (room) {
                room.users = room.users.filter(u => u.id !== socket.id);

                io.to(roomName).emit('user-left', {
                    userName: userName,
                    message: `${userName} disconnected`,
                    timestamp: new Date(),
                    userCount: room.users.length
                });

                if (room.users.length === 0) {
                    rooms.delete(roomName);
                }
            }

            userSockets.delete(socket.id);
        }

        console.log(`[DISCONNECT] User disconnected: ${socket.id}`);
    });
});

// ============================================================
// Server Start
// ============================================================
server.listen(PORT, '0.0.0.0', () => {
    console.log(`
    ╔════════════════════════════════════════════════════╗
    ║       Chat App Web Server - Socket.io Based        ║
    ║          Listening on http://localhost:${PORT}        ║
    ║                                                    ║
    ║  Distributed Systems - UTS Project 2025/2026     ║
    ╚════════════════════════════════════════════════════╝
    `);
});
