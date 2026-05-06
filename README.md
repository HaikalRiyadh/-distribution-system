# 💬 Distributed Chat System - UTS Project

Aplikasi chat terdistribusi dengan dukungan **multi-room**, **multi-device**, dan **real-time messaging**.

## 📁 Project Structure

```
.
├── ChatApp_Web/                 ← MAIN PROJECT (Node.js + Socket.io)
│   ├── server.js               (Backend server)
│   ├── package.json            (Dependencies)
│   ├── public/                 (Frontend files)
│   │   ├── index.html          (HTML interface)
│   │   ├── style.css           (Styling)
│   │   └── client.js           (Client logic)
│   └── README.md               (Detailed documentation)
├── .gitignore                   (Git ignore rules)
└── README.md                    (This file)
```

---

## 🚀 Quick Start

### Prerequisites
- **Node.js** >= 14.0.0
- **npm** (biasanya sudah termasuk)

### Installation & Running

```bash
# 1. Navigate to project
cd ChatApp_Web

# 2. Install dependencies (pertama kali saja)
npm install

# 3. Start server
npm start
```

Server akan berjalan di: `http://localhost:3000`

---

## 💻 Cara Menggunakan

### Single Device - Multiple Tabs
```
1. Tab 1: Name="Alice", Room="kelas1"
2. Tab 2: Name="Bob", Room="kelas1"
3. Chat antar tab! 💬
```

### Multiple Devices (Same WiFi)
```
1. Laptop: npm start (dapatkan IP dari ipconfig)
2. Phone: Akses http://[IP-LAPTOP]:3000
3. Chat device-to-device! 📱💻
```

---

## ✨ Features

✅ **Real-time Messaging** - WebSocket communication
✅ **Multi-Room Support** - Unlimited rooms
✅ **User Presence** - See who's online
✅ **Typing Indicators** - Know when someone's typing
✅ **Message History** - Chat history per room
✅ **Mobile Responsive** - Works on all devices
✅ **Beautiful UI** - Modern & clean design

---

## 🛠️ Tech Stack

**Backend:**
- Node.js
- Express.js
- Socket.io

**Frontend:**
- HTML5
- CSS3
- Vanilla JavaScript

---

## 📝 API Events (Socket.IO)
         │  SERVER  │  ← port 12345
         └────┬─────┘
       ┌──────┼──────┐
  Thread1  Thread2  Thread3  ...  (1 thread per client)
       │      │      │
  ┌────┴─┐ ┌──┴──┐ ┌─┴────┐
  │ C-1  │ │ C-2 │ │ C-3  │
  └──────┘ └─────┘ └──────┘
```

### Client → Server
```javascript
- join-room: Join ke room
- send-message: Kirim pesan
- typing: User sedang mengetik
- stop-typing: User selesai mengetik
- leave-room: Tinggalkan room
- get-rooms: Minta daftar rooms
```

### Server → Client
```javascript
- room-data: Data room diterima
- user-joined: User bergabung
- user-left: User pergi
- receive-message: Pesan baru
- user-typing: User mengetik
- user-stop-typing: User berhenti mengetik
- rooms-list: Update daftar rooms
```

---

## 🌐 Deployment

### Deploy ke Heroku
```bash
heroku login
heroku create chat-app-web
git push heroku main
```

### Deploy ke Railway
1. Connect GitHub repo
2. Auto-deploy on push

---

## 📱 Browser Support

✅ Chrome/Chromium
✅ Firefox
✅ Safari
✅ Edge
✅ Mobile Browsers

---

## 🐛 Troubleshooting

### "Cannot find module 'express'"
```bash
npm install
```

### Port 3000 sudah digunakan
```powershell
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

### Tidak bisa konek dari device lain
- Cek WiFi sama
- Cek firewall
- Gunakan IP lokal bukan localhost

---

## 📊 Performance

- Supports 100+ concurrent users
- Message history: 100 per room
- Auto-cleanup empty rooms
- Optimized with WebSocket

---

## 👨‍💻 Development

### File Structure di ChatApp_Web
```
public/
├── index.html    - Main HTML (struktur DOM)
├── style.css     - CSS styling (responsive design)
└── client.js     - JavaScript logic (Socket.io events)

server.js        - Express + Socket.io backend
package.json     - Dependencies config
```

### Modifying Code

**Frontend Changes:** Edit files di `public/` folder
**Backend Changes:** Edit `server.js`
**Styling:** Modifikasi `style.css`

Refresh browser untuk melihat changes.

---

## 📄 License

MIT License - Distributed Systems UTS Project 2025/2026

---

## 👨‍🎓 Author

**Haikal Riyadh** - Jakarta Global University

---

## 📚 Dokumentasi Lengkap

Untuk dokumentasi lebih detail, lihat: [ChatApp_Web/README.md](ChatApp_Web/README.md)
