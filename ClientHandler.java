import java.net.*;
import java.io.*;
import java.util.function.*;

/**
 * ============================================================
 *  ClientHandler.java - Distributed Systems UTS Project
 *  Jakarta Global University - 2025/2026
 * ============================================================
 *  Deskripsi:
 *  Kelas ini menangani satu koneksi client dalam sebuah Thread.
 *  Setiap client yang terkoneksi akan memiliki satu instance
 *  ClientHandler yang berjalan secara paralel (multithreading).
 *
 *  Alur kerja:
 *  1. Terima nama client dari pesan pertama yang dikirim
 *  2. Broadcast notifikasi bergabung ke semua client lain
 *  3. Terus baca pesan dan broadcast selama koneksi aktif
 *  4. Saat disconnect, hapus dari daftar dan broadcast notifikasi
 * ============================================================
 */
public class ClientHandler implements Runnable {

    // Socket koneksi ke client ini
    private final Socket socket;

    // Stream untuk membaca pesan dari client
    private BufferedReader in;

    // Stream untuk mengirim pesan ke client
    private PrintWriter out;

    // Nama client (dikirim saat pertama kali terhubung)
    private String clientName;

    // Callback ke server untuk broadcast, remove, update, dan log
    private final ChatServer.BroadcastCallback broadcastCallback;
    private final Consumer<ClientHandler>       removeCallback;
    private final Runnable                      updateListCallback;
    private final Consumer<String>              logCallback;

    // -------------------------------------------------------
    // Constructor
    // -------------------------------------------------------
    public ClientHandler(Socket socket,
                         ChatServer.BroadcastCallback broadcastCallback,
                         Consumer<ClientHandler>      removeCallback,
                         Runnable                     updateListCallback,
                         Consumer<String>             logCallback) {
        this.socket             = socket;
        this.broadcastCallback  = broadcastCallback;
        this.removeCallback     = removeCallback;
        this.updateListCallback = updateListCallback;
        this.logCallback        = logCallback;
        this.clientName         = "Client-" + socket.getPort();
    }

    // -------------------------------------------------------
    // run() — dipanggil saat Thread di-start
    // -------------------------------------------------------
    @Override
    public void run() {
        try {
            // Inisialisasi stream I/O
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Pesan pertama dari client adalah namanya
            String nameMsg = in.readLine();
            if (nameMsg != null && !nameMsg.trim().isEmpty()) {
                clientName = nameMsg.trim();
            }

            logCallback.accept(clientName + " connected from " + socket.getInetAddress());

            // Beritahu semua client lain bahwa ada anggota baru
            broadcastCallback.broadcast("✅ " + clientName + " has joined the chat!", this);

            // Perbarui daftar client di GUI server
            updateListCallback.run();

            // -------------------------------------------
            // Loop utama: baca dan broadcast setiap pesan
            // -------------------------------------------
            String message;
            while ((message = in.readLine()) != null) {
                if (!message.trim().isEmpty()) {
                    // Format: "NamaClient : isi pesan"
                    broadcastCallback.broadcast(clientName + " : " + message, this);
                }
            }

        } catch (IOException e) {
            logCallback.accept(clientName + " disconnected abruptly.");
        } finally {
            // Bersihkan sumber daya dan beritahu client lain
            cleanup();
        }
    }

    // -------------------------------------------------------
    // Kirim pesan ke client ini
    // -------------------------------------------------------
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    // -------------------------------------------------------
    // Bersihkan koneksi saat client disconnect
    // -------------------------------------------------------
    private void cleanup() {
        try {
            socket.close();
        } catch (IOException ignored) {}

        // Hapus dari daftar client aktif di server
        removeCallback.accept(this);

        // Beritahu semua client yang masih terhubung
        broadcastCallback.broadcast("🔴 " + clientName + " has left the chat.", this);

        logCallback.accept(clientName + " removed from client list.");
    }

    // -------------------------------------------------------
    // Getter nama client
    // -------------------------------------------------------
    public String getClientName() {
        return clientName;
    }
}
