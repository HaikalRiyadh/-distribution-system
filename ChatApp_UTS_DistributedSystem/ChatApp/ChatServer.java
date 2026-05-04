import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;
import javax.swing.border.Border;

/**
 * ============================================================
 *  ChatServer.java - Distributed Systems UTS Project
 *  Jakarta Global University - 2025/2026
 * ============================================================
 *  Deskripsi:
 *  Server utama yang menerima koneksi dari banyak client
 *  secara bersamaan menggunakan multithreading.
 *  Setiap pesan yang masuk akan di-broadcast ke semua
 *  client lain yang sedang terhubung.
 * ============================================================
 */
public class ChatServer {

    // Port yang digunakan server untuk mendengarkan koneksi
    public static final int PORT = 12345;

    // Daftar semua client yang sedang terhubung (thread-safe)
    private static final List<ClientHandler> clients =
            Collections.synchronizedList(new ArrayList<>());

    // Komponen GUI Server
    private static JTextArea logArea;
    private static JTextArea clientListArea;
    private static JLabel statusLabel;

    // -------------------------------------------------------
    // MAIN - Entry point server
    // -------------------------------------------------------
    public static void main(String[] args) {
        buildServerGUI();   // Bangun tampilan server
        startListening();   // Mulai menerima koneksi client
    }

    // -------------------------------------------------------
    // Bangun tampilan GUI Server (opsional - sesuai rubrik)
    // -------------------------------------------------------
    private static void buildServerGUI() {
        JFrame frame = new JFrame("🖥  Chat Server  —  Port " + PORT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(460, 560);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(8, 8));

        // --- Panel Status ---
        statusLabel = new JLabel("  ● Server Starting...", JLabel.LEFT);
        statusLabel.setForeground(Color.ORANGE);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 0));
        frame.add(statusLabel, BorderLayout.NORTH);

        // --- Panel Daftar Client ---
        clientListArea = new JTextArea(6, 30);
        clientListArea.setEditable(false);
        clientListArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        clientListArea.setBackground(new Color(240, 248, 255));
        JScrollPane clientScroll = new JScrollPane(clientListArea);
        clientScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 1),
                "Connected Clients", 0, 0,
                new Font("SansSerif", Font.BOLD, 12), new Color(60, 60, 180)));
        clientScroll.setPreferredSize(new Dimension(440, 160));

        // --- Panel Log Chat ---
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(245, 245, 245));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 1),
                "Log Chat", 0, 0,
                new Font("SansSerif", Font.BOLD, 12), new Color(60, 60, 180)));

        // Susun panel vertikal
        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        centerPanel.add(clientScroll, BorderLayout.NORTH);
        centerPanel.add(logScroll, BorderLayout.CENTER);

        frame.add(centerPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // -------------------------------------------------------
    // Mulai mendengarkan koneksi dari client
    // -------------------------------------------------------
    private static void startListening() {
        log("Server started on port " + PORT + " — Waiting for clients...");
        setStatus("● Running  —  Port " + PORT, new Color(0, 150, 0));

        // ServerSocket berjalan di thread terpisah agar GUI tidak freeze
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (true) {
                    // Terima koneksi baru dari client
                    Socket clientSocket = serverSocket.accept();

                    // Buat handler dan jalankan di thread baru (multithreading)
                    ClientHandler handler = new ClientHandler(clientSocket, ChatServer::broadcast,
                            ChatServer::removeClient, ChatServer::updateClientList, ChatServer::log);
                    clients.add(handler);
                    new Thread(handler).start();   // ← Multithreading untuk tiap client
                }
            } catch (IOException e) {
                log("Server error: " + e.getMessage());
                setStatus("● Server Error", Color.RED);
            }
        }, "ServerAcceptThread").start();
    }

    // -------------------------------------------------------
    // Broadcast pesan ke semua client KECUALI pengirim
    // -------------------------------------------------------
    public static void broadcast(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
        log(message);
    }

    // -------------------------------------------------------
    // Hapus client dari daftar saat disconnect
    // -------------------------------------------------------
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        updateClientList();
    }

    // -------------------------------------------------------
    // Perbarui tampilan daftar client yang terhubung
    // -------------------------------------------------------
    public static void updateClientList() {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();
            synchronized (clients) {
                for (ClientHandler c : clients) {
                    sb.append(c.getClientName()).append(" : Online\n");
                }
            }
            clientListArea.setText(sb.toString());
        });
    }

    // -------------------------------------------------------
    // Tulis log ke area log dengan timestamp
    // -------------------------------------------------------
    public static void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + timestamp + "] " + message + "\n");
            // Auto-scroll ke bawah
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    // -------------------------------------------------------
    // Helper: update status label
    // -------------------------------------------------------
    private static void setStatus(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("  " + text);
            statusLabel.setForeground(color);
        });
    }

    // -------------------------------------------------------
    // Interface untuk callback dari ClientHandler ke Server
    // -------------------------------------------------------
    @FunctionalInterface
    interface BroadcastCallback {
        void broadcast(String message, ClientHandler sender);
    }
}
