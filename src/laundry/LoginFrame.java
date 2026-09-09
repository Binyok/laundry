package laundry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblError;

    public LoginFrame() {
        setTitle("AquaWash Laundry Management System");
        setSize(920, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Deep modern blue-indigo gradient background
                GradientPaint bgGrad = new GradientPaint(0, 0, new Color(15, 23, 42), getWidth(), getHeight(), new Color(30, 58, 138));
                g2.setPaint(bgGrad);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Ambient modern gradient orbs
                g2.setPaint(new RadialGradientPaint(
                        new Point(180, 150), 280,
                        new float[]{0.0f, 1.0f},
                        new Color[]{new Color(56, 189, 248, 50), new Color(56, 189, 248, 0)}
                ));
                g2.fillOval(-100, -130, 560, 560);

                g2.setPaint(new RadialGradientPaint(
                        new Point(getWidth() - 150, getHeight() - 100), 320,
                        new float[]{0.0f, 1.0f},
                        new Color[]{new Color(99, 102, 241, 60), new Color(99, 102, 241, 0)}
                ));
                g2.fillOval(getWidth() - 470, getHeight() - 420, 640, 640);

                // Grid dots pattern for tech look
                g2.setColor(new Color(255, 255, 255, 12));
                for (int x = 20; x < getWidth(); x += 30) {
                    for (int y = 20; y < getHeight(); y += 30) {
                        g2.fillOval(x, y, 2, 2);
                    }
                }

                g2.dispose();
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        // Center Split Card with Glassmorphism / Modern Card Styling
        JPanel card = new JPanel(new GridLayout(1, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Outer soft shadow
                g2.setColor(new Color(0, 0, 0, 70));
                g2.fill(new RoundRectangle2D.Float(6, 6, getWidth() - 12, getHeight() - 12, 24, 24));

                // Card background
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 6, getHeight() - 6, 24, 24));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(820, 460));

        // LEFT HERO PANEL (Illustration & Features)
        JPanel heroPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Hero left gradient
                GradientPaint heroGrad = new GradientPaint(0, 0, new Color(14, 116, 222), getWidth(), getHeight(), new Color(37, 99, 235));
                g2.setPaint(heroGrad);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
                g2.fillRect(getWidth() - 20, 0, 20, getHeight()); // Right straight edge

                // Graphic Elements (Soap Bubbles & Laundry Art)
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillOval(-30, -30, 160, 160);
                g2.fillOval(getWidth() - 90, 40, 130, 130);
                g2.fillOval(50, getHeight() - 110, 100, 100);

                // Washing Machine Illustration
                int cx = getWidth() / 2;
                int cy = 160;

                // Outer Machine Body
                g2.setColor(new Color(255, 255, 255, 240));
                g2.fillRoundRect(cx - 65, cy - 75, 130, 150, 24, 24);

                // Top Control Panel
                g2.setColor(new Color(224, 242, 254));
                g2.fillRoundRect(cx - 52, cy - 62, 50, 14, 6, 6);
                g2.setColor(new Color(14, 116, 222));
                g2.fillOval(cx + 32, cy - 62, 14, 14);
                g2.setColor(new Color(16, 185, 129));
                g2.fillOval(cx + 12, cy - 58, 8, 8);

                // Drum Outer Bezel
                g2.setColor(new Color(203, 213, 225));
                g2.fillOval(cx - 46, cy - 32, 92, 92);

                // Drum Glass Window
                GradientPaint glass = new GradientPaint(cx - 38, cy - 24, new Color(56, 189, 248), cx + 38, cy + 52, new Color(14, 116, 222));
                g2.setPaint(glass);
                g2.fillOval(cx - 38, cy - 24, 76, 76);

                // Water Swirl & Shine
                g2.setColor(new Color(255, 255, 255, 180));
                g2.fillArc(cx - 30, cy - 16, 60, 60, 50, 75);
                g2.setColor(Color.WHITE);
                g2.fillOval(cx - 10, cy + 12, 10, 10);
                g2.fillOval(cx + 10, cy + 4, 8, 8);
                g2.fillOval(cx - 2, cy - 8, 6, 6);

                g2.dispose();
            }
        };
        heroPanel.setOpaque(false);
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        heroPanel.setBorder(new EmptyBorder(30, 35, 30, 35));

        heroPanel.add(Box.createVerticalStrut(225));

        JLabel heroTitle = new JLabel("AquaWash System", JLabel.CENTER);
        heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heroTitle.setForeground(Color.WHITE);
        heroTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroPanel.add(heroTitle);

        heroPanel.add(Box.createVerticalStrut(6));

        JLabel heroTagline = new JLabel("Premium & Fast Laundry POS", JLabel.CENTER);
        heroTagline.setFont(new Font("Segoe UI", Font.BOLD, 13));
        heroTagline.setForeground(new Color(191, 219, 254));
        heroTagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroPanel.add(heroTagline);

        heroPanel.add(Box.createVerticalStrut(15));

        JLabel heroFeatures = new JLabel("<html><center>Transaksi Cepat &nbsp;|&nbsp; Laporan Real-Time<br>Cetak Nota Otomatis &nbsp;|&nbsp; Data Pelanggan</center></html>", JLabel.CENTER);
        heroFeatures.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        heroFeatures.setForeground(new Color(239, 246, 255));
        heroFeatures.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroPanel.add(heroFeatures);

        // RIGHT FORM PANEL
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(35, 40, 35, 40));

        JLabel formHeading = new JLabel("Login Admin");
        formHeading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        formHeading.setForeground(new Color(15, 23, 42));
        formHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel formSub = new JLabel("Masukkan akun untuk mengelola transaksi");
        formSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSub.setForeground(new Color(100, 116, 139));
        formSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblError.setForeground(new Color(239, 68, 68));
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(new Color(71, 85, 105));
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = new JTextField("admin");
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setPreferredSize(new Dimension(300, 40));
        txtUsername.setMaximumSize(new Dimension(320, 40));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(new Color(71, 85, 105));
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField("admin123");
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(300, 40));
        txtPassword.setMaximumSize(new Dimension(320, 40));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login Button
        btnLogin = new JButton("Masuk Sekarang") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(29, 78, 216));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(37, 99, 235));
                } else {
                    g2.setColor(new Color(14, 116, 222));
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(300, 44));
        btnLogin.setMaximumSize(new Dimension(320, 44));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHint = new JLabel("Default Akun: admin / admin123");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(new Color(148, 163, 184));
        lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        formPanel.add(formHeading);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(formSub);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(lblError);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(lblUser);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(txtUsername);
        formPanel.add(Box.createVerticalStrut(14));
        formPanel.add(lblPass);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(txtPassword);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(btnLogin);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(lblHint);

        card.add(heroPanel);
        card.add(formPanel);

        backgroundPanel.add(card);
        setContentPane(backgroundPanel);

        // Key Listeners for smooth ENTER submission
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);
        btnLogin.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Masukkan username dan password!");
            return;
        }

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Instantly open MainFrame without any modal alert
                    this.dispose();
                    SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
                } else {
                    lblError.setText("Username atau Password salah!");
                    txtPassword.setText("");
                    txtPassword.requestFocus();
                }
            }
        } catch (SQLException ex) {
            lblError.setText("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
