# Chat App - Web Version

Aplikasi chat berbasis web dengan dukungan **multi-room** dan **device-to-device** communication.

## 🚀 Fitur Utama

### Backend (Node.js + Socket.io)
- ✅ Real-time messaging menggunakan WebSocket
- ✅ Multi-room support (unlimited rooms)
- ✅ Message history per room
- ✅ User presence tracking
- ✅ Typing indicators
- ✅ Auto-cleanup untuk room kosong

### Frontend (HTML + CSS + JavaScript)
- ✅ Modern & responsive UI
- ✅ Real-time message sync
- ✅ User list display
- ✅ Typing indicator
- ✅ Multiple room management
- ✅ System notifications
- ✅ Mobile-friendly design

## 📋 Persyaratan

- **Node.js** >= 14.0.0
- **npm** (biasanya sudah termasuk dengan Node.js)

## ⚙️ Instalasi & Setup

### 1. Install Dependencies
```bash
cd ChatApp_Web
npm install
```

Ini akan menginstall:
- `express` - Web server framework
- `socket.io` - Real-time communication library

### 2. Jalankan Server
```bash
npm start
```

atau

```bash
node server.js
```

Output:
```
╔════════════════════════════════════════════════════╗
║       Chat App Web Server - Socket.io Based        ║
║          Listening on http://localhost:3000        ║
║                                                    ║
║  Distributed Systems - UTS Project 2025/2026     ║
╚════════════════════════════════════════════════════╝
```

### 3. Akses Aplikasi

Buka browser dan pergi ke:
```
http://localhost:3000
```

## 💻 Cara Menggunakan

### Single Device - Multiple Users
1. Buka `http://localhost:3000` di browser utama
2. Masukkan nama user (e.g., "Alice")
3. Masukkan nama room (e.g., "Kelas1")
4. Klik "Join Chat"
5. Buka tab baru, masukkan nama user berbeda (e.g., "Bob")
6. Masukkan room yang sama ("Kelas1")
7. Sekarang mereka bisa chat satu sama lain! 💬

### Multiple Devices - Same Network
1. **Device 1 (Server)**: Jalankan aplikasi dengan IP lokal
   ```bash
   npm start
   ```
   Lihat IP address Anda (e.g., 192.168.1.100)

2. **Device 1 (Client)**: Buka `http://localhost:3000`

3. **Device 2**: Buka `http://192.168.1.100:3000`
   - Ganti 192.168.1.100 dengan IP device server Anda
   - Masukkan nama user dan room yang sama
   - Chat langsung dengan device lain! 📱💻

### Multiple Devices - Internet (Remote)
1. Deploy server ke hosting (Heroku, Vercel, Railway, etc.)
2. Akses dari mana saja menggunakan URL yang di-deploy
3. Multiple users dari multiple devices bisa chat real-time!

## 🏗️ Struktur Project

```
ChatApp_Web/
├── package.json           # Dependencies
├── server.js              # Backend server
└── public/
    ├── index.html         # Frontend HTML
    ├── style.css          # Styling
    └── client.js          # Client-side JavaScript
```

## 🔧 Konfigurasi

### Port Default
Edit di `server.js`:
```javascript
const PORT = process.env.PORT || 3000;
```

### Room Settings
- **Max messages per room**: 100 (dapat diubah di server.js)
- **Auto-cleanup**: Room otomatis dihapus saat kosong
- **Message history**: Disimpan selama room aktif

## 📝 API Events (Socket.IO)

### Client → Server
- `join-room` - Join ke room
- `send-message` - Kirim pesan
- `typing` - User sedang mengetik
- `stop-typing` - User selesai mengetik
- `leave-room` - Tinggalkan room
- `get-rooms` - Minta daftar room

### Server → Client
- `room-data` - Data room diterima
- `user-joined` - User bergabung
- `user-left` - User pergi
- `receive-message` - Pesan baru
- `user-typing` - User mengetik
- `user-stop-typing` - User berhenti mengetik
- `rooms-list` - Daftar room update

## 🎨 Customization

### Ubah Warna
Edit di `style.css`:
```css
:root {
    --primary-color: #3498db;
    --secondary-color: #2ecc71;
    --danger-color: #e74c3c;
}
```

### Ubah Port
Edit di `server.js`:
```javascript
const PORT = 5000; // Ganti port
```

## 🐛 Troubleshooting

### Error: "Cannot find module 'express'"
```bash
npm install
```

### Port sudah digunakan
```bash
# Kill process di port 3000
# Windows:
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# macOS/Linux:
lsof -i :3000
kill -9 <PID>
```

### Tidak bisa connect dari device lain
1. Pastikan firewall memperbolehkan port 3000
2. Gunakan IP lokal device (bukan localhost)
3. Pastikan devices di network yang sama

## 📱 Responsive Design

Aplikasi ini responsif dan bekerja di:
- ✅ Desktop browsers
- ✅ Tablet
- ✅ Mobile phones

## 🚀 Deploy ke Production

### Option 1: Heroku
```bash
heroku login
heroku create chat-app-web
git push heroku main
```

### Option 2: Railway
1. Connect GitHub repo
2. Deploy secara otomatis

### Option 3: Vercel (untuk frontend saja)
1. Host Node.js di tempat lain
2. Update Socket.IO URL di client.js

## 📊 Performance Tips

- Server bisa handle ratusan concurrent connections
- Message history dibatasi 100 per room untuk efficiency
- WebSocket lebih cepat dari polling HTTP
- Compression otomatis untuk data transfer

## 👨‍💻 Kontribusi

Silakan modifikasi dan improve aplikasi ini!

## 📄 License

MIT License - Distributed Systems UTS Project 2025/2026

---

**Made with ❤️ for Jakarta Global University**
