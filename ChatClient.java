import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

/**
 * ============================================================
 *  ChatClient.java 
 * ============================================================
 *  Deskripsi:
 *  Aplikasi client dengan antarmuka grafis (Swing GUI).
 *  Setiap instance mewakili satu pengguna dalam jaringan chat.
 *
 *  Fitur UI:
 *  - Text area untuk menampilkan riwayat percakapan
 *  - Text field untuk mengetik pesan
 *  - Tombol "Send" untuk mengirim pesan
 *  - Auto-scroll ke pesan terbaru
 *  - Indikator status koneksi
 *
 *  Fitur Teknis:
 *  - Thread terpisah untuk menerima pesan (non-blocking)
 *  - Kirim pesan dengan Enter atau klik tombol Send
 *  - Notifikasi saat koneksi terputus
 * ============================================================
 */
public class ChatClient {

    // Konfigurasi koneksi — ganti HOST jika server di mesin lain
    private static final String SERVER_HOST = "localhost";
    private static final int    SERVER_PORT = 12345;

    // Nama pengguna yang diinput saat login
    private String clientName;

    // Komponen GUI
    private JFrame    frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton   sendButton;
    private JLabel    statusLabel;

    // Stream untuk komunikasi dengan server
    private PrintWriter out;
    private boolean     connected = false;

    // -------------------------------------------------------
    // Constructor — inisialisasi nama, GUI, dan koneksi
    // -------------------------------------------------------
    public ChatClient() {
        askForName();       // Minta nama pengguna
        buildGUI();         // Bangun antarmuka
        connectToServer();  // Hubungkan ke server
    }

    // -------------------------------------------------------
    // Minta nama pengguna sebelum masuk ke chat
    // -------------------------------------------------------
    private void askForName() {
        while (true) {
            clientName = JOptionPane.showInputDialog(
                    null,
                    "Masukkan nama Anda:",
                    "Login Chat",
                    JOptionPane.PLAIN_MESSAGE
            );

            // Tutup aplikasi jika pengguna tekan Cancel
            if (clientName == null) {
                System.exit(0);
            }

            clientName = clientName.trim();
            if (!clientName.isEmpty()) break;

            JOptionPane.showMessageDialog(null,
                    "Nama tidak boleh kosong!", "Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // -------------------------------------------------------
    // Bangun antarmuka grafis client
    // -------------------------------------------------------
    private void buildGUI() {
        // ── Jendela utama ──────────────────────────────────
        frame = new JFrame("Chat Client  —  " + clientName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(480, 560);
        frame.setMinimumSize(new Dimension(380, 400));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(0, 0));

        // Warna tema biru muda
        Color headerColor = new Color(60, 100, 200);
        Color bgColor     = new Color(245, 247, 252);

        // ── Header ─────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(headerColor);
        header.setPreferredSize(new Dimension(0, 55));

        JLabel titleLabel = new JLabel("  💬  " + clientName, JLabel.LEFT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        statusLabel = new JLabel("Connecting...  ", JLabel.RIGHT);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(200, 230, 255));

        header.add(titleLabel,  BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.EAST);
        frame.add(header, BorderLayout.NORTH);

        // ── Area percakapan ────────────────────────────────
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        chatArea.setBackground(bgColor);
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chatArea.setForeground(new Color(30, 30, 30));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        frame.add(scrollPane, BorderLayout.CENTER);

        // ── Panel input bawah ──────────────────────────────
        JPanel inputPanel = new JPanel(new BorderLayout(6, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 210)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 220), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        sendButton = new JButton("Send ➤");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        sendButton.setBackground(headerColor);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        sendButton.setEnabled(false); // Aktif setelah terhubung

        inputPanel.add(inputField,  BorderLayout.CENTER);
        inputPanel.add(sendButton,  BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // ── Event handler ──────────────────────────────────
        // Klik tombol Send
        sendButton.addActionListener(e -> sendMessage());

        // Tekan Enter di input field
        inputField.addActionListener(e -> sendMessage());

        frame.setVisible(true);
    }

    // -------------------------------------------------------
    // Hubungkan ke server dan mulai thread penerima
    // -------------------------------------------------------
    private void connectToServer() {
        new Thread(() -> {
            try {
                Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);

                // Kirim nama ke server sebagai pesan pertama
                out.println(clientName);

                connected = true;

                // Update UI ke status "Connected"
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("● Connected  ");
                    statusLabel.setForeground(new Color(150, 240, 150));
                    sendButton.setEnabled(true);
                    inputField.requestFocus();
                    appendMessage("✅ Terhubung ke server. Selamat chatting!\n");
                });

                // ── Loop menerima pesan dari server ────────
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                String message;
                while ((message = in.readLine()) != null) {
                    final String msg = message;
                    SwingUtilities.invokeLater(() -> {
                        appendMessage(msg + "\n");
                    });
                }

            } catch (IOException e) {
                // Koneksi gagal atau terputus
                SwingUtilities.invokeLater(() -> {
                    connected = false;
                    sendButton.setEnabled(false);
                    inputField.setEnabled(false);
                    statusLabel.setText("● Offline  ");
                    statusLabel.setForeground(new Color(255, 120, 120));
                    appendMessage("\nYou're Offline.....\n");
                });
            }
        }, "ClientReceiverThread").start();
    }

    // -------------------------------------------------------
    // Kirim pesan ke server
    // -------------------------------------------------------
    private void sendMessage() {
        if (!connected || out == null) return;

        String message = inputField.getText().trim();
        if (message.isEmpty()) return;

        // Tampilkan pesan milik sendiri di chat lokal
        appendMessage(clientName + " : " + message + "\n");

        // Kirim ke server (server akan broadcast ke client lain)
        out.println(message);

        // Kosongkan input field
        inputField.setText("");
        inputField.requestFocus();
    }

    // -------------------------------------------------------
    // Tambah teks ke chat area dengan auto-scroll
    // -------------------------------------------------------
    private void appendMessage(String text) {
        chatArea.append(text);
        // Auto-scroll ke pesan terbaru
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // -------------------------------------------------------
    // MAIN — Jalankan client
    // -------------------------------------------------------
    public static void main(String[] args) {
        // Gunakan system look and feel agar tampilan lebih native
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Jalankan di Event Dispatch Thread (thread-safe untuk Swing)
        SwingUtilities.invokeLater(ChatClient::new);
    }
}
