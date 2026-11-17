package laundry.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import laundry.db.DatabaseHelper;
import laundry.ui.panels.*;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel sidebarPanel;
    private JLabel titleLabel;
    private JButton activeButton;

    private static final Color PRIMARY = new Color(30, 58, 138);
    private static final Color PRIMARY_DARK = new Color(23, 37, 84);
    private static final Color ACCENT = new Color(59, 130, 246);
    private static final Color BG = new Color(241, 245, 249);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT_LIGHT = new Color(203, 213, 225);
    private static final Color SIDEBAR_HOVER = new Color(37, 73, 165);

    private String[] menuItems = {
        "Dashboard", "Pelanggan", "Karyawan", "Layanan",
        "Transaksi", "Keuangan", "Keuntungan", "Cetak Nota"
    };

    private String[] menuIcons = {
        "\u2302", "\u263A", "\u2699", "\u2606",
        "\u2616", "\u2713", "\u2605", "\u2709"
    };

    public MainFrame() {
        DatabaseHelper.getInstance();
        initUI();
    }

    private void initUI() {
        setTitle("Laundry Siska Sigro - Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        add(createSidebar(), BorderLayout.WEST);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BorderLayout());
        sidebarPanel.setPreferredSize(new Dimension(240, 0));
        sidebarPanel.setBackground(PRIMARY);

        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBackground(PRIMARY_DARK);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(22, 16, 20, 16));

        JLabel logo = createAvatarLogo(80);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandPanel.add(logo);

        brandPanel.add(Box.createVerticalStrut(10));

        JLabel brandTitle = new JLabel("Laundry Siska Sigro");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brandTitle.setForeground(WHITE);
        brandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandPanel.add(brandTitle);

        brandPanel.add(Box.createVerticalStrut(4));

        JLabel tagline = new JLabel("Management System");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tagline.setForeground(TEXT_LIGHT);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandPanel.add(tagline);

        sidebarPanel.add(brandPanel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(PRIMARY);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < menuItems.length; i++) {
            JButton btn = createMenuButton(menuIcons[i] + "  " + menuItems[i], menuItems[i]);
            menuPanel.add(btn);
            menuPanel.add(Box.createVerticalStrut(2));
            if (i == 0) {
                activeButton = btn;
                btn.setBackground(ACCENT);
            }
        }

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(PRIMARY);
        scrollPane.getViewport().setBackground(PRIMARY);
        sidebarPanel.add(scrollPane, BorderLayout.CENTER);

        return sidebarPanel;
    }

    private JLabel createAvatarLogo(int size) {
        try {
            java.net.URL url = getClass().getResource("/laundry/logo_circle.png");
            if (url == null) {
                url = getClass().getResource("/laundry/logo laundry.jpeg");
            }
            if (url != null) {
                ImageIcon rawIcon = new ImageIcon(url);
                Image scaled = rawIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                setIconImage(scaled);
                return new JLabel(new ImageIcon(scaled));
            }
        } catch (Exception e) {
            // fallback
        }
        JLabel fallback = new JLabel("\u2668");
        fallback.setFont(new Font("Segoe UI", Font.BOLD, 36));
        fallback.setForeground(WHITE);
        return fallback;
    }

    private JButton createMenuButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(WHITE);
        btn.setBackground(PRIMARY);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(220, 42));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(PRIMARY);
                }
            }
        });

        btn.addActionListener(e -> {
            if (activeButton != null) {
                activeButton.setBackground(PRIMARY);
            }
            activeButton = btn;
            btn.setBackground(ACCENT);
            cardLayout.show(contentPanel, cardName);
            titleLabel.setText(cardName);

            for (Component comp : contentPanel.getComponents()) {
                if (comp.isVisible() && comp instanceof BasePanel) {
                    ((BasePanel) comp).refreshData();
                }
            }
        });

        return btn;
    }

    private JPanel createContent() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(30, 41, 59));
        topBar.add(titleLabel, BorderLayout.WEST);

        wrapper.add(topBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG);

        contentPanel.add(new DashboardPanel(), "Dashboard");
        contentPanel.add(new PelangganPanel(), "Pelanggan");
        contentPanel.add(new KaryawanPanel(), "Karyawan");
        contentPanel.add(new LayananPanel(), "Layanan");
        contentPanel.add(new TransaksiPanel(), "Transaksi");
        contentPanel.add(new KeuanganPanel(), "Keuangan");
        contentPanel.add(new KeuntunganPanel(), "Keuntungan");
        contentPanel.add(new CetakNotaPanel(), "Cetak Nota");

        wrapper.add(contentPanel, BorderLayout.CENTER);

        return wrapper;
    }
}
