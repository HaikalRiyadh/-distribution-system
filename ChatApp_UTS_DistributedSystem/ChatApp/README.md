# 💬 Multi-Client Chat Application
## Distributed Systems — UTS Project
### Jakarta Global University | TA 2025/2026

---

## 📁 Struktur File

```
ChatApp/
├── ChatServer.java      → Server utama (GUI + multithreading)
├── ClientHandler.java   → Handler satu client per thread
├── ChatClient.java      → Aplikasi client dengan Swing GUI
└── README.md            → Petunjuk ini
```

---

## ⚙️ Cara Kompilasi

Buka terminal di folder `ChatApp/`, lalu jalankan:

```bash
javac ChatServer.java ClientHandler.java ChatClient.java
```

---

## ▶️ Cara Menjalankan

### 1. Jalankan Server terlebih dahulu
```bash
java ChatServer
```
Server akan membuka jendela GUI dan menunggu koneksi di **port 12345**.

### 2. Jalankan Client (buka beberapa jendela untuk multi-client)
```bash
java ChatClient
```
Jalankan perintah ini beberapa kali di terminal berbeda untuk mensimulasikan banyak user.

> **Catatan:** Jika server ada di komputer lain, ubah `SERVER_HOST` di `ChatClient.java` dari `"localhost"` menjadi IP address server.

---

## 🏗️ Arsitektur Sistem

```
         ┌──────────┐
         │  SERVER  │  ← port 12345
         └────┬─────┘
       ┌──────┼──────┐
  Thread1  Thread2  Thread3  ...  (1 thread per client)
       │      │      │
  ┌────┴─┐ ┌──┴──┐ ┌─┴────┐
  │ C-1  │ │ C-2 │ │ C-3  │
  └──────┘ └─────┘ └──────┘
```

**Alur pesan:**
1. Client A kirim pesan → Server
2. Server terima via `ClientHandler` (thread A)
3. Server broadcast ke semua thread lain (B, C, ...)
4. Client B dan C menerima pesan secara real-time

---

## 🔧 Teknologi yang Digunakan

| Komponen     | Library                     |
|--------------|-----------------------------|
| Networking   | `java.net.Socket`, `ServerSocket` |
| I/O          | `java.io.BufferedReader`, `PrintWriter` |
| GUI          | `javax.swing.*`, `java.awt.*` |
| Concurrency  | `java.lang.Thread`, `Collections.synchronizedList` |

**JDK:** Java 8 atau lebih baru

---

## ✅ Checklist Fitur (sesuai rubrik)

- [x] Server menerima banyak client secara bersamaan
- [x] Server broadcast pesan ke semua client lain
- [x] Client dapat connect ke server
- [x] Client dapat kirim & terima pesan real-time
- [x] GUI Client: text area riwayat, input field, tombol Send
- [x] GUI Server: daftar client online + log chat
- [x] Multithreading untuk tiap client
- [x] Kode bersih, modular, dan berkommentar lengkap
- [x] Auto-scroll ke pesan terbaru
- [x] Notifikasi saat client join/leave

---

## 👥 Cara Demo

1. Compile semua file
2. Jalankan `java ChatServer` → lihat jendela server
3. Jalankan 3x `java ChatClient` di terminal berbeda → masukkan nama berbeda
4. Kirim pesan dari salah satu client → lihat broadcast di client lain
5. Tutup salah satu client → lihat notifikasi "has left the chat"
