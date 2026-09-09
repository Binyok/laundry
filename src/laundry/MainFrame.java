package laundry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainFrame extends JFrame {
    private JPanel sidebarPanel;
    private JPanel cardContainer;
    private CardLayout cardLayout;

    private JLabel lblPageTitle;
    private JLabel lblPageSubtitle;
    private JButton btnActiveTab;

    private JLabel lblTodayOrders;
    private JLabel lblInProcess;
    private JLabel lblCompleted;
    private JLabel lblTodayRevenue;
    private JTable tableDashRecent;
    private DefaultTableModel modelDashRecent;

    private JTable tableCustomer;
    private DefaultTableModel modelCustomer;
    private JTextField txtCustName, txtCustPhone, txtCustAddress;
    private JButton btnAddCust, btnEditCust, btnDeleteCust, btnClearCust;

    private JTable tableService;
    private DefaultTableModel modelService;
    private JTextField txtServiceName, txtServicePrice;
    private JButton btnAddServ, btnEditServ, btnDeleteServ, btnClearServ;

    private JComboBox<ComboItem> cbCustomer;
    private JComboBox<ComboItem> cbService;
    private JTextField txtWeight, txtTotalAmount, txtDueDate;
    private JTable tableTransaction;
    private DefaultTableModel modelTransaction;
    private JComboBox<String> cbStatusFilter;
    private JTextField txtSearch;
    private JComboBox<String> cbStatusEdit;

    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public MainFrame() {
        setTitle("AquaWash Laundry - Modern Admin Portal");
        // FIX 1: Ukuran window lebih besar supaya semua konten muat
        setSize(1400, 850);
        setMinimumSize(new Dimension(1280, 760));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        loadDashboardData();
        loadCustomerData();
        loadServiceData();
        loadTransactionData();
        populateComboBoxes();
    }

    private String formatRupiah(double amount) {
        return "Rp " + String.format("%,.0f", amount).replace(',', '.');
    }

    private void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setSelectionForeground(new Color(15, 23, 42));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setBorder(null);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(100, 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
    }

    private void stylePrimaryButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 16)
        ));
    }

    private void styleSidebarButton(JButton btn, String label) {
    btn.setText(label);
    btn.setHorizontalAlignment(SwingConstants.LEFT);

    btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    btn.setForeground(new Color(148, 163, 184));
    btn.setBackground(new Color(15, 23, 42));

    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setOpaque(false);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    // SESUAIKAN DENGAN LEBAR SIDEBAR
    btn.setPreferredSize(new Dimension(140, 42));
    btn.setMinimumSize(new Dimension(140, 42));
    btn.setMaximumSize(new Dimension(140, 42));

    // padding teks dari kiri
    btn.setBorder(new EmptyBorder(0, 10, 0, 0));

    // PENTING
    btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    
}

    private void setActiveTab(JButton btn, String tabName, String title, String subtitle) {
        if (btnActiveTab != null) {
            btnActiveTab.setForeground(new Color(148, 163, 184));
            btnActiveTab.setContentAreaFilled(false);
            btnActiveTab.setOpaque(false);
            btnActiveTab.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            btnActiveTab.setPreferredSize(new Dimension(140, 42));
            btnActiveTab.setMaximumSize(new Dimension(140, 42));
            btnActiveTab.setMinimumSize(new Dimension(140, 42));
        }
        btnActiveTab = btn;
        btnActiveTab.setForeground(Color.WHITE);
        btnActiveTab.setContentAreaFilled(true);
        btnActiveTab.setOpaque(true);
        btnActiveTab.setBackground(new Color(14, 116, 222));
        btnActiveTab.setFont(new Font("Segoe UI", Font.BOLD, 13));
        // Paksa ukuran tetap terkunci supaya tidak melebar keluar sidebar
        btnActiveTab.setPreferredSize(new Dimension(140, 42));
        btnActiveTab.setMaximumSize(new Dimension(140, 42));
        btnActiveTab.setMinimumSize(new Dimension(140, 42));

        lblPageTitle.setText(title);
        lblPageSubtitle.setText(subtitle);
        cardLayout.show(cardContainer, tabName);

        if (tabName.equals("Dashboard")) loadDashboardData();
        else if (tabName.equals("Pelanggan")) loadCustomerData();
        else if (tabName.equals("Layanan")) loadServiceData();
        else if (tabName.equals("Transaksi")) { populateComboBoxes(); loadTransactionData(); }
    }

    private void initComponents() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(248, 250, 252));

        // ==========================================
        // SIDEBAR — FIX 4: Lebar 220px (dari 150px)
        // ==========================================
        sidebarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), 0, getHeight(), new Color(30, 41, 59));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        // Kunci ukuran sidebar agar tidak bisa melar
        sidebarPanel.setPreferredSize(new Dimension(160, 800));
        sidebarPanel.setMinimumSize(new Dimension(160, 600));
        sidebarPanel.setMaximumSize(new Dimension(160, Integer.MAX_VALUE));
        sidebarPanel.setBorder(new EmptyBorder(20, 10, 20, 10));

        JLabel brandLogo = new JLabel("AquaWash");
        brandLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandLogo.setForeground(Color.WHITE);
        brandLogo.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandLogo.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel brandTag = new JLabel("Smart Laundry System");
        brandTag.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        brandTag.setForeground(new Color(56, 189, 248));
        brandTag.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandTag.setBorder(new EmptyBorder(3, 10, 0, 0));

        sidebarPanel.add(brandLogo);
        sidebarPanel.add(brandTag);
        sidebarPanel.add(Box.createVerticalStrut(24));

        JLabel menuHeading = new JLabel("MENU UTAMA");
        menuHeading.setFont(new Font("Segoe UI", Font.BOLD, 9));
        menuHeading.setForeground(new Color(100, 116, 139));
        menuHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuHeading.setBorder(new EmptyBorder(0, 10, 8, 0));
        sidebarPanel.add(menuHeading);
        
        

        JButton btnDash = new JButton();
        styleSidebarButton(btnDash, "Dashboard");
        btnDash.addActionListener(e -> setActiveTab(btnDash, "Dashboard", "Dashboard Overview", "Ringkasan data operasional dan pendapatan laundry hari ini"));

        JButton btnTrans = new JButton();
        styleSidebarButton(btnTrans, "Transaksi Laundry");
        btnTrans.addActionListener(e -> setActiveTab(btnTrans, "Transaksi", "Kelola Transaksi", "Pencatatan cucian baru, hitung tarif, update status & cetak nota"));

        JButton btnCust = new JButton();
        styleSidebarButton(btnCust, "Data Pelanggan");
        btnCust.addActionListener(e -> setActiveTab(btnCust, "Pelanggan", "Master Data Pelanggan", "Daftar direktori pelanggan dan riwayat kontak"));

        JButton btnServ = new JButton();
        styleSidebarButton(btnServ, "Paket Layanan");
        btnServ.addActionListener(e -> setActiveTab(btnServ, "Layanan", "Paket Layanan", "Atur jenis layanan cuci, setrika, kilat & tarif per kilogram"));

        JButton btnRep = new JButton();
        styleSidebarButton(btnRep, "Laporan Keuangan");
        btnRep.addActionListener(e -> setActiveTab(btnRep, "Laporan", "Laporan Penjualan", "Rekapitulasi omset laundry harian dan bulanan"));

        sidebarPanel.add(btnDash);
        sidebarPanel.add(Box.createVerticalStrut(4));
        sidebarPanel.add(btnTrans);
        sidebarPanel.add(Box.createVerticalStrut(4));
        sidebarPanel.add(btnCust);
        sidebarPanel.add(Box.createVerticalStrut(4));
        sidebarPanel.add(btnServ);
        sidebarPanel.add(Box.createVerticalStrut(4));
        sidebarPanel.add(btnRep);
        sidebarPanel.add(Box.createVerticalGlue());

        // User badge
        JPanel userBadge = new JPanel(new BorderLayout(8, 0));
        userBadge.setOpaque(false);
        userBadge.setAlignmentX(Component.LEFT_ALIGNMENT); // TAMBAHKAN BARIS INI

        userBadge.setMaximumSize(new Dimension(220, 50));
        userBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                new EmptyBorder(8, 10, 8, 12)
        ));

        JLabel userIcon = new JLabel("Admin");
        userIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userIcon.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Keluar");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLogout.setForeground(new Color(248, 113, 113));
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> { this.dispose(); new LoginFrame().setVisible(true); });

        userBadge.add(userIcon, BorderLayout.WEST);
        userBadge.add(btnLogout, BorderLayout.EAST);
        sidebarPanel.add(userBadge);

        // ==========================================
        // MAIN AREA
        // ==========================================
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(new Color(248, 250, 252));

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(16, 10, 16, 28)
        ));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        lblPageTitle = new JLabel("Dashboard Overview");
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblPageTitle.setForeground(new Color(15, 23, 42));

        lblPageSubtitle = new JLabel("Ringkasan data operasional dan pendapatan laundry hari ini");
        lblPageSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPageSubtitle.setForeground(new Color(100, 116, 139));

        titleBlock.add(lblPageTitle);
        titleBlock.add(lblPageSubtitle);
        topBar.add(titleBlock, BorderLayout.WEST);

        SimpleDateFormat timeFmt = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        JLabel lblDateHeader = new JLabel(timeFmt.format(new Date()));
        lblDateHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDateHeader.setForeground(new Color(71, 85, 105));
        topBar.add(lblDateHeader, BorderLayout.EAST);

        mainArea.add(topBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(new Color(248, 250, 252));
        cardContainer.setBorder(new EmptyBorder(20, 20, 20, 24));

        cardContainer.add(createDashboardView(), "Dashboard");
        cardContainer.add(createTransactionView(), "Transaksi");
        cardContainer.add(createCustomerView(), "Pelanggan");
        cardContainer.add(createServiceView(), "Layanan");
        cardContainer.add(createReportView(), "Laporan");

        mainArea.add(cardContainer, BorderLayout.CENTER);

        rootPanel.add(sidebarPanel, BorderLayout.WEST);
        rootPanel.add(mainArea, BorderLayout.CENTER);
        setContentPane(rootPanel);

        setActiveTab(btnDash, "Dashboard", "Dashboard Overview", "Ringkasan data operasional dan pendapatan laundry hari ini");
    }

    // ==========================================
    // VIEW 1: DASHBOARD
    // ==========================================
    private JPanel createDashboardView() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 16, 16));
        metricsPanel.setOpaque(false);
        metricsPanel.setPreferredSize(new Dimension(0, 120));

        metricsPanel.add(createMetricCard("PESANAN HARI INI", lblTodayOrders = new JLabel("0"), "Total cucian masuk hari ini", new Color(14, 116, 222)));
        metricsPanel.add(createMetricCard("SEDANG DIPROSES", lblInProcess = new JLabel("0"), "Dalam pencucian & setrika", new Color(245, 158, 11)));
        metricsPanel.add(createMetricCard("SIAP DIAMBIL", lblCompleted = new JLabel("0"), "Selesai & menunggu pelanggan", new Color(16, 185, 129)));
        metricsPanel.add(createMetricCard("OMSET HARI INI", lblTodayRevenue = new JLabel("Rp 0"), "Pendapatan kotor hari ini", new Color(139, 92, 246)));

        panel.add(metricsPanel, BorderLayout.NORTH);

        JPanel tableCard = new JPanel(new BorderLayout(0, 12));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel tableTitle = new JLabel("Aktivitas Transaksi Terbaru");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tableTitle.setForeground(new Color(15, 23, 42));
        tableCard.add(tableTitle, BorderLayout.NORTH);

        modelDashRecent = new DefaultTableModel(new String[]{"ID", "Pelanggan", "Layanan", "Berat (kg)", "Total", "Tanggal Masuk", "Status"}, 0);
        tableDashRecent = new JTable(modelDashRecent);
        styleTable(tableDashRecent);
        setupStatusColumnRenderer(tableDashRecent, 6);

        JScrollPane scroll = new JScrollPane(tableDashRecent);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249)));
        tableCard.add(scroll, BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String subtitle, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(100, 116, 139));
        top.add(lblTitle, BorderLayout.WEST);

        JPanel pill = new JPanel();
        pill.setPreferredSize(new Dimension(8, 8));
        pill.setBackground(accent);
        top.add(pill, BorderLayout.EAST);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(148, 163, 184));

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    // ==========================================
    // VIEW 2: TRANSAKSI — LAYOUT DIPERBAIKI
    // ==========================================
    private JPanel createTransactionView() {
        JPanel mainTransPanel = new JPanel(new BorderLayout(18, 0));
        mainTransPanel.setOpaque(false);

        // FIX 5: Form kiri diperlebar jadi 320px (dari 280px)
        JPanel formCard = new JPanel(new BorderLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setPreferredSize(new Dimension(320, 0));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel formTitle = new JLabel("Buat Pesanan Baru");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(new Color(15, 23, 42));

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;

        JLabel lblCust = new JLabel("Pilih Pelanggan");
        lblCust.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCust.setForeground(new Color(71, 85, 105));
        cbCustomer = new JComboBox<>();
        cbCustomer.setPreferredSize(new Dimension(0, 32));

        JLabel lblServ = new JLabel("Jenis Layanan");
        lblServ.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblServ.setForeground(new Color(71, 85, 105));
        cbService = new JComboBox<>();
        cbService.setPreferredSize(new Dimension(0, 32));

        JLabel lblWeight = new JLabel("Berat Cucian (Kg)");
        lblWeight.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblWeight.setForeground(new Color(71, 85, 105));

        JPanel weightRow = new JPanel(new BorderLayout(6, 0));
        weightRow.setOpaque(false);
        txtWeight = new JTextField();
        txtWeight.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtWeight.setPreferredSize(new Dimension(0, 32));
        JButton btnHitung = new JButton("Hitung Total");
        stylePrimaryButton(btnHitung, new Color(16, 185, 129), Color.WHITE);
        btnHitung.setPreferredSize(new Dimension(100, 32));
        btnHitung.addActionListener(e -> calculateTotal());
        weightRow.add(txtWeight, BorderLayout.CENTER);
        weightRow.add(btnHitung, BorderLayout.EAST);

        JLabel lblTotal = new JLabel("Total Biaya");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotal.setForeground(new Color(71, 85, 105));
        txtTotalAmount = new JTextField();
        txtTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtTotalAmount.setForeground(new Color(16, 185, 129));
        txtTotalAmount.setBackground(new Color(241, 245, 249));
        txtTotalAmount.setEditable(false);
        txtTotalAmount.setPreferredSize(new Dimension(0, 32));

        JLabel lblDue = new JLabel("Estimasi Selesai (YYYY-MM-DD)");
        lblDue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDue.setForeground(new Color(71, 85, 105));
        txtDueDate = new JTextField();
        txtDueDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDueDate.setPreferredSize(new Dimension(0, 32));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        txtDueDate.setText(sdf.format(new Date(System.currentTimeMillis() + 3L * 24 * 60 * 60 * 1000)));

        formFields.add(formTitle, gbc);
        formFields.add(Box.createVerticalStrut(4), gbc);
        formFields.add(lblCust, gbc);
        formFields.add(cbCustomer, gbc);
        formFields.add(Box.createVerticalStrut(2), gbc);
        formFields.add(lblServ, gbc);
        formFields.add(cbService, gbc);
        formFields.add(Box.createVerticalStrut(2), gbc);
        formFields.add(lblWeight, gbc);
        formFields.add(weightRow, gbc);
        formFields.add(Box.createVerticalStrut(2), gbc);
        formFields.add(lblTotal, gbc);
        formFields.add(txtTotalAmount, gbc);
        formFields.add(Box.createVerticalStrut(2), gbc);
        formFields.add(lblDue, gbc);
        formFields.add(txtDueDate, gbc);
        formFields.add(Box.createVerticalStrut(8), gbc);

        JButton btnSaveTrans = new JButton("Simpan Pesanan");
        stylePrimaryButton(btnSaveTrans, new Color(14, 116, 222), Color.WHITE);
        btnSaveTrans.setPreferredSize(new Dimension(0, 34));
        btnSaveTrans.addActionListener(e -> saveTransaction());
        formFields.add(btnSaveTrans, gbc);

        formCard.add(formFields, BorderLayout.CENTER);
        mainTransPanel.add(formCard, BorderLayout.WEST);

        // RIGHT PANEL
        JPanel rightCard = new JPanel(new BorderLayout(0, 14));
        rightCard.setBackground(Color.WHITE);
        rightCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // FIX 6: Toolbar dibuat 2 baris supaya tombol tidak kepotong
        JPanel toolbarPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        toolbarPanel.setOpaque(false);

        // Baris 1: Search
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);
        JLabel lblCari = new JLabel("Cari Transaksi:");
        lblCari.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSearch = new JTextField(14);
        txtSearch.setPreferredSize(new Dimension(180, 34));
        JButton btnSearch = new JButton("Cari");
        stylePrimaryButton(btnSearch, new Color(14, 116, 222), Color.WHITE);
        btnSearch.setPreferredSize(new Dimension(70, 34));
        btnSearch.addActionListener(e -> loadTransactionData());

        searchRow.add(lblCari);
        searchRow.add(txtSearch);
        searchRow.add(btnSearch);

        // Baris 2: Filter + Update Status + Cetak Nota
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter Status:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cbStatusFilter = new JComboBox<>(new String[]{"Semua Status", "Diterima", "Diproses", "Selesai", "Sudah Diambil"});
        cbStatusFilter.setPreferredSize(new Dimension(140, 34));
        cbStatusFilter.addActionListener(e -> loadTransactionData());

        JLabel lblChange = new JLabel("Ubah ke:");
        lblChange.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cbStatusEdit = new JComboBox<>(new String[]{"Diterima", "Diproses", "Selesai", "Sudah Diambil"});
        cbStatusEdit.setPreferredSize(new Dimension(130, 34));

        JButton btnUpdateStatus = new JButton("Update Status");
        stylePrimaryButton(btnUpdateStatus, new Color(245, 158, 11), Color.WHITE);
        btnUpdateStatus.setPreferredSize(new Dimension(120, 34));
        btnUpdateStatus.addActionListener(e -> updateTransactionStatus());

        JButton btnPrintReceipt = new JButton("Cetak Nota");
        stylePrimaryButton(btnPrintReceipt, new Color(139, 92, 246), Color.WHITE);
        btnPrintReceipt.setPreferredSize(new Dimension(110, 34));
        btnPrintReceipt.addActionListener(e -> printReceipt());

        actionRow.add(lblFilter);
        actionRow.add(cbStatusFilter);
        actionRow.add(Box.createHorizontalStrut(12));
        actionRow.add(lblChange);
        actionRow.add(cbStatusEdit);
        actionRow.add(btnUpdateStatus);
        actionRow.add(btnPrintReceipt);

        toolbarPanel.add(searchRow);
        toolbarPanel.add(actionRow);
        rightCard.add(toolbarPanel, BorderLayout.NORTH);

        // Table
        modelTransaction = new DefaultTableModel(new String[]{"ID", "Pelanggan", "Layanan", "Berat", "Total", "Tanggal Masuk", "Est. Selesai", "Status"}, 0);
        tableTransaction = new JTable(modelTransaction);
        styleTable(tableTransaction);
        setupStatusColumnRenderer(tableTransaction, 7);

        tableTransaction.getColumnModel().getColumn(0).setPreferredWidth(45);
        tableTransaction.getColumnModel().getColumn(1).setPreferredWidth(140);
        tableTransaction.getColumnModel().getColumn(2).setPreferredWidth(130);
        tableTransaction.getColumnModel().getColumn(3).setPreferredWidth(60);
        tableTransaction.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableTransaction.getColumnModel().getColumn(5).setPreferredWidth(115);
        tableTransaction.getColumnModel().getColumn(6).setPreferredWidth(110);
        tableTransaction.getColumnModel().getColumn(7).setPreferredWidth(110);

        JScrollPane scrollTrans = new JScrollPane(tableTransaction);
        scrollTrans.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249)));
        rightCard.add(scrollTrans, BorderLayout.CENTER);

        mainTransPanel.add(rightCard, BorderLayout.CENTER);
        return mainTransPanel;
    }

    // ==========================================
    // VIEW 3: PELANGGAN
    // ==========================================
    private JPanel createCustomerView() {
        JPanel mainCustPanel = new JPanel(new BorderLayout(18, 0));
        mainCustPanel.setOpaque(false);

        JPanel formCard = new JPanel(new BorderLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setPreferredSize(new Dimension(320, 0));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel formTitle = new JLabel("Form Pelanggan");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(new Color(15, 23, 42));

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;

        txtCustName = new JTextField();
        txtCustName.setPreferredSize(new Dimension(0, 36));
        txtCustPhone = new JTextField();
        txtCustPhone.setPreferredSize(new Dimension(0, 36));
        txtCustAddress = new JTextField();
        txtCustAddress.setPreferredSize(new Dimension(0, 36));

        formFields.add(formTitle, gbc);
        formFields.add(Box.createVerticalStrut(10), gbc);

        JLabel lbl1 = new JLabel("Nama Lengkap");
        lbl1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formFields.add(lbl1, gbc);
        formFields.add(txtCustName, gbc);
        formFields.add(Box.createVerticalStrut(6), gbc);

        JLabel lbl2 = new JLabel("Nomor Handphone / WhatsApp");
        lbl2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formFields.add(lbl2, gbc);
        formFields.add(txtCustPhone, gbc);
        formFields.add(Box.createVerticalStrut(6), gbc);

        JLabel lbl3 = new JLabel("Alamat Lengkap");
        lbl3.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formFields.add(lbl3, gbc);
        formFields.add(txtCustAddress, gbc);
        formFields.add(Box.createVerticalStrut(16), gbc);

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);

        btnAddCust = new JButton("Tambah");
        btnEditCust = new JButton("Update");
        btnDeleteCust = new JButton("Hapus");
        btnClearCust = new JButton("Reset");

        stylePrimaryButton(btnAddCust, new Color(14, 116, 222), Color.WHITE);
        stylePrimaryButton(btnEditCust, new Color(245, 158, 11), Color.WHITE);
        stylePrimaryButton(btnDeleteCust, new Color(239, 68, 68), Color.WHITE);
        stylePrimaryButton(btnClearCust, new Color(100, 116, 139), Color.WHITE);

        btnGrid.add(btnAddCust);
        btnGrid.add(btnEditCust);
        btnGrid.add(btnDeleteCust);
        btnGrid.add(btnClearCust);
        formFields.add(btnGrid, gbc);

        formCard.add(formFields, BorderLayout.NORTH);
        mainCustPanel.add(formCard, BorderLayout.WEST);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        modelCustomer = new DefaultTableModel(new String[]{"ID", "Nama Pelanggan", "No. Handphone", "Alamat"}, 0);
        tableCustomer = new JTable(modelCustomer);
        styleTable(tableCustomer);

        tableCustomer.getColumnModel().getColumn(0).setPreferredWidth(60);
        tableCustomer.getColumnModel().getColumn(1).setPreferredWidth(160);
        tableCustomer.getColumnModel().getColumn(2).setPreferredWidth(130);
        tableCustomer.getColumnModel().getColumn(3).setPreferredWidth(250);

        JScrollPane scrollCust = new JScrollPane(tableCustomer);
        scrollCust.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249)));
        tableCard.add(scrollCust, BorderLayout.CENTER);

        mainCustPanel.add(tableCard, BorderLayout.CENTER);

        btnAddCust.addActionListener(e -> addCustomer());
        btnEditCust.addActionListener(e -> editCustomer());
        btnDeleteCust.addActionListener(e -> deleteCustomer());
        btnClearCust.addActionListener(e -> clearCustomerFields());
        tableCustomer.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = tableCustomer.getSelectedRow();
            if (selectedRow != -1) {
                txtCustName.setText(modelCustomer.getValueAt(selectedRow, 1).toString());
                txtCustPhone.setText(modelCustomer.getValueAt(selectedRow, 2).toString());
                txtCustAddress.setText(modelCustomer.getValueAt(selectedRow, 3).toString());
            }
        });

        return mainCustPanel;
    }

    // ==========================================
    // VIEW 4: LAYANAN
    // ==========================================
    private JPanel createServiceView() {
        JPanel mainServPanel = new JPanel(new BorderLayout(18, 0));
        mainServPanel.setOpaque(false);

        JPanel formCard = new JPanel(new BorderLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setPreferredSize(new Dimension(320, 0));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel formTitle = new JLabel("Form Layanan");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(new Color(15, 23, 42));

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;

        txtServiceName = new JTextField();
        txtServiceName.setPreferredSize(new Dimension(0, 36));
        txtServicePrice = new JTextField();
        txtServicePrice.setPreferredSize(new Dimension(0, 36));

        formFields.add(formTitle, gbc);
        formFields.add(Box.createVerticalStrut(10), gbc);

        JLabel lbl1 = new JLabel("Nama Layanan");
        lbl1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formFields.add(lbl1, gbc);
        formFields.add(txtServiceName, gbc);
        formFields.add(Box.createVerticalStrut(6), gbc);

        JLabel lbl2 = new JLabel("Tarif Harga (per KG)");
        lbl2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formFields.add(lbl2, gbc);
        formFields.add(txtServicePrice, gbc);
        formFields.add(Box.createVerticalStrut(16), gbc);

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);

        btnAddServ = new JButton("Tambah");
        btnEditServ = new JButton("Update");
        btnDeleteServ = new JButton("Hapus");
        btnClearServ = new JButton("Reset");

        stylePrimaryButton(btnAddServ, new Color(14, 116, 222), Color.WHITE);
        stylePrimaryButton(btnEditServ, new Color(245, 158, 11), Color.WHITE);
        stylePrimaryButton(btnDeleteServ, new Color(239, 68, 68), Color.WHITE);
        stylePrimaryButton(btnClearServ, new Color(100, 116, 139), Color.WHITE);

        btnGrid.add(btnAddServ);
        btnGrid.add(btnEditServ);
        btnGrid.add(btnDeleteServ);
        btnGrid.add(btnClearServ);
        formFields.add(btnGrid, gbc);

        formCard.add(formFields, BorderLayout.NORTH);
        mainServPanel.add(formCard, BorderLayout.WEST);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        modelService = new DefaultTableModel(new String[]{"ID Layanan", "Nama Layanan", "Harga / KG"}, 0);
        tableService = new JTable(modelService);
        styleTable(tableService);

        tableService.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableService.getColumnModel().getColumn(1).setPreferredWidth(250);
        tableService.getColumnModel().getColumn(2).setPreferredWidth(150);

        JScrollPane scrollServ = new JScrollPane(tableService);
        scrollServ.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249)));
        tableCard.add(scrollServ, BorderLayout.CENTER);

        mainServPanel.add(tableCard, BorderLayout.CENTER);

        btnAddServ.addActionListener(e -> addService());
        btnEditServ.addActionListener(e -> editService());
        btnDeleteServ.addActionListener(e -> deleteService());
        btnClearServ.addActionListener(e -> clearServiceFields());
        tableService.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = tableService.getSelectedRow();
            if (selectedRow != -1) {
                txtServiceName.setText(modelService.getValueAt(selectedRow, 1).toString());
                txtServicePrice.setText(modelService.getValueAt(selectedRow, 2).toString());
            }
        });

        return mainServPanel;
    }

    // ==========================================
    // VIEW 5: LAPORAN
    // ==========================================
    private JPanel createReportView() {
        JPanel mainRepPanel = new JPanel(new BorderLayout(0, 16));
        mainRepPanel.setOpaque(false);

        JPanel tableCard = new JPanel(new BorderLayout(0, 14));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnGroup.setOpaque(false);
        JButton btnReportDaily = new JButton("Laporan Harian");
        JButton btnReportMonthly = new JButton("Laporan Bulanan");

        stylePrimaryButton(btnReportDaily, new Color(14, 116, 222), Color.WHITE);
        stylePrimaryButton(btnReportMonthly, new Color(139, 92, 246), Color.WHITE);
        btnReportDaily.setPreferredSize(new Dimension(150, 36));
        btnReportMonthly.setPreferredSize(new Dimension(150, 36));

        btnGroup.add(btnReportDaily);
        btnGroup.add(btnReportMonthly);

        JLabel lblTotalReportRev = new JLabel("Total Omset: Rp 0");
        lblTotalReportRev.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalReportRev.setForeground(new Color(16, 185, 129));

        headerPanel.add(btnGroup, BorderLayout.WEST);
        headerPanel.add(lblTotalReportRev, BorderLayout.EAST);
        tableCard.add(headerPanel, BorderLayout.NORTH);

        DefaultTableModel modelReport = new DefaultTableModel(new String[]{"ID Transaksi", "Pelanggan", "Tanggal Transaksi", "Total Pendapatan", "Status"}, 0);
        JTable tableReport = new JTable(modelReport);
        styleTable(tableReport);
        setupStatusColumnRenderer(tableReport, 4);

        JScrollPane scrollReport = new JScrollPane(tableReport);
        scrollReport.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249)));
        tableCard.add(scrollReport, BorderLayout.CENTER);

        btnReportDaily.addActionListener(e -> loadReportData(modelReport, lblTotalReportRev, true));
        btnReportMonthly.addActionListener(e -> loadReportData(modelReport, lblTotalReportRev, false));

        mainRepPanel.add(tableCard, BorderLayout.CENTER);
        return mainRepPanel;
    }

    // ==========================================
    // STATUS BADGE RENDERER
    // ==========================================
    private void setupStatusColumnRenderer(JTable table, int colIndex) {
        table.getColumnModel().getColumn(colIndex).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));

                if (val != null) {
                    String status = val.toString();
                    if (status.equalsIgnoreCase("Diterima")) {
                        label.setForeground(new Color(14, 116, 222));
                        if (!isSel) label.setBackground(new Color(239, 246, 255));
                    } else if (status.equalsIgnoreCase("Diproses")) {
                        label.setForeground(new Color(217, 119, 6));
                        if (!isSel) label.setBackground(new Color(254, 243, 199));
                    } else if (status.equalsIgnoreCase("Selesai")) {
                        label.setForeground(new Color(22, 163, 74));
                        if (!isSel) label.setBackground(new Color(220, 252, 231));
                    } else {
                        label.setForeground(new Color(71, 85, 105));
                        if (!isSel) label.setBackground(new Color(241, 245, 249));
                    }
                }
                return label;
            }
        });
    }

    // ==========================================
    // DATA LOADERS (tidak diubah, hanya disalin)
    // ==========================================
    private void loadDashboardData() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            String sqlTodayOrders = "SELECT COUNT(*) FROM transactions WHERE date(transaction_date) = date('now', 'localtime')";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlTodayOrders); ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) lblTodayOrders.setText(String.valueOf(rs.getInt(1)));
            }
            String sqlInProcess = "SELECT COUNT(*) FROM transactions WHERE status = 'Diproses'";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInProcess); ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) lblInProcess.setText(String.valueOf(rs.getInt(1)));
            }
            String sqlCompleted = "SELECT COUNT(*) FROM transactions WHERE status = 'Selesai'";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCompleted); ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) lblCompleted.setText(String.valueOf(rs.getInt(1)));
            }
            String sqlTodayRev = "SELECT SUM(total_amount) FROM transactions WHERE date(transaction_date) = date('now', 'localtime')";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlTodayRev); ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) lblTodayRevenue.setText(formatRupiah(rs.getDouble(1)));
            }
            modelDashRecent.setRowCount(0);
            String sqlRecent = "SELECT t.id, c.name, s.service_name, td.weight, t.total_amount, t.transaction_date, t.status " +
                    "FROM transactions t " +
                    "JOIN customers c ON t.customer_id = c.id " +
                    "JOIN transaction_details td ON t.id = td.transaction_id " +
                    "JOIN services s ON td.service_id = s.id " +
                    "ORDER BY t.id DESC LIMIT 10";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlRecent)) {
                while (rs.next()) {
                    modelDashRecent.addRow(new Object[]{
                            rs.getInt("id"), rs.getString("name"), rs.getString("service_name"),
                            rs.getDouble("weight"), formatRupiah(rs.getDouble("total_amount")),
                            rs.getString("transaction_date"), rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadCustomerData() {
        modelCustomer.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {
            while (rs.next()) {
                modelCustomer.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("phone_number"), rs.getString("address")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void addCustomer() {
        String name = txtCustName.getText().trim();
        String phone = txtCustPhone.getText().trim();
        String address = txtCustAddress.getText().trim();
        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama dan No HP wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO customers (name, phone_number, address) VALUES (?, ?, ?)")) {
            pstmt.setString(1, name); pstmt.setString(2, phone); pstmt.setString(3, address);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Pelanggan berhasil ditambahkan!");
            clearCustomerFields(); loadCustomerData(); populateComboBoxes();
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void editCustomer() {
        int selectedRow = tableCustomer.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Pilih baris yang ingin diedit!"); return; }
        int id = (int) modelCustomer.getValueAt(selectedRow, 0);
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE customers SET name = ?, phone_number = ?, address = ? WHERE id = ?")) {
            pstmt.setString(1, txtCustName.getText().trim());
            pstmt.setString(2, txtCustPhone.getText().trim());
            pstmt.setString(3, txtCustAddress.getText().trim());
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Pelanggan berhasil diperbarui!");
            clearCustomerFields(); loadCustomerData(); populateComboBoxes();
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void deleteCustomer() {
        int selectedRow = tableCustomer.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus!"); return; }
        int id = (int) modelCustomer.getValueAt(selectedRow, 0);
        if (JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus pelanggan ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM customers WHERE id = ?")) {
                pstmt.setInt(1, id); pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Pelanggan berhasil dihapus!");
                clearCustomerFields(); loadCustomerData(); populateComboBoxes();
            } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: Pelanggan tidak bisa dihapus karena memiliki transaksi terkait."); }
        }
    }

    private void clearCustomerFields() {
        txtCustName.setText(""); txtCustPhone.setText(""); txtCustAddress.setText("");
        tableCustomer.clearSelection();
    }

    private void loadServiceData() {
        modelService.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM services")) {
            while (rs.next()) {
                modelService.addRow(new Object[]{rs.getInt("id"), rs.getString("service_name"), formatRupiah(rs.getDouble("price_per_kg"))});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void addService() {
        String name = txtServiceName.getText().trim();
        String priceStr = txtServicePrice.getText().trim();
        if (name.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi semua data layanan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double price = Double.parseDouble(priceStr.replace("Rp", "").replace(".", "").replace(",", "").trim());
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO services (service_name, price_per_kg) VALUES (?, ?)")) {
                pstmt.setString(1, name); pstmt.setDouble(2, price);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Layanan berhasil ditambahkan!");
                clearServiceFields(); loadServiceData(); populateComboBoxes();
            }
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Tarif harga harus berupa angka!");
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void editService() {
        int selectedRow = tableService.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Pilih baris yang ingin diedit!"); return; }
        int id = (int) modelService.getValueAt(selectedRow, 0);
        try {
            double price = Double.parseDouble(txtServicePrice.getText().trim().replace("Rp", "").replace(".", "").replace(",", "").trim());
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE services SET service_name = ?, price_per_kg = ? WHERE id = ?")) {
                pstmt.setString(1, txtServiceName.getText().trim()); pstmt.setDouble(2, price); pstmt.setInt(3, id);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Layanan berhasil diperbarui!");
                clearServiceFields(); loadServiceData(); populateComboBoxes();
            }
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Tarif harga harus berupa angka!");
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void deleteService() {
        int selectedRow = tableService.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus!"); return; }
        int id = (int) modelService.getValueAt(selectedRow, 0);
        if (JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus layanan ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM services WHERE id = ?")) {
                pstmt.setInt(1, id); pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Layanan berhasil dihapus!");
                clearServiceFields(); loadServiceData(); populateComboBoxes();
            } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: Layanan tidak bisa dihapus karena masih digunakan pada transaksi."); }
        }
    }

    private void clearServiceFields() {
        txtServiceName.setText(""); txtServicePrice.setText("");
        tableService.clearSelection();
    }

    private void populateComboBoxes() {
        cbCustomer.removeAllItems(); cbService.removeAllItems();
        try (Connection conn = DatabaseHelper.getConnection()) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id, name FROM customers ORDER BY name ASC")) {
                while (rs.next()) cbCustomer.addItem(new ComboItem(rs.getInt("id"), rs.getString("name")));
            }
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id, service_name, price_per_kg FROM services ORDER BY id ASC")) {
                while (rs.next()) cbService.addItem(new ComboItem(rs.getInt("id"), rs.getString("service_name") + " (" + formatRupiah(rs.getDouble("price_per_kg")) + "/kg)"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void calculateTotal() {
        ComboItem selectedService = (ComboItem) cbService.getSelectedItem();
        if (selectedService == null) return;
        try {
            double weight = Double.parseDouble(txtWeight.getText().trim());
            double pricePerKg = 0;
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT price_per_kg FROM services WHERE id = ?")) {
                pstmt.setInt(1, selectedService.getId());
                try (ResultSet rs = pstmt.executeQuery()) { if (rs.next()) pricePerKg = rs.getDouble("price_per_kg"); }
            }
            txtTotalAmount.setText(String.valueOf((long)(weight * pricePerKg)));
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Berat cucian harus berupa angka yang valid!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void saveTransaction() {
        ComboItem customer = (ComboItem) cbCustomer.getSelectedItem();
        ComboItem service = (ComboItem) cbService.getSelectedItem();
        String weightStr = txtWeight.getText().trim();
        String totalStr = txtTotalAmount.getText().trim();
        String dueDate = txtDueDate.getText().trim();
        if (customer == null || service == null || weightStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan lengkapi data pesanan terlebih dahulu!"); return;
        }
        if (totalStr.isEmpty()) { calculateTotal(); totalStr = txtTotalAmount.getText().trim(); if (totalStr.isEmpty()) return; }
        try {
            double weight = Double.parseDouble(weightStr);
            double totalAmount = Double.parseDouble(totalStr);
            Connection conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);
            try {
                double pricePerKg = 0;
                try (PreparedStatement pstmt = conn.prepareStatement("SELECT price_per_kg FROM services WHERE id = ?")) {
                    pstmt.setInt(1, service.getId());
                    try (ResultSet rs = pstmt.executeQuery()) { if (rs.next()) pricePerKg = rs.getDouble("price_per_kg"); }
                }
                PreparedStatement pstmtTrans = conn.prepareStatement(
                        "INSERT INTO transactions (customer_id, total_weight, total_amount, due_date, status) VALUES (?, ?, ?, ?, 'Diterima')",
                        Statement.RETURN_GENERATED_KEYS);
                pstmtTrans.setInt(1, customer.getId()); pstmtTrans.setDouble(2, weight);
                pstmtTrans.setDouble(3, totalAmount); pstmtTrans.setString(4, dueDate);
                pstmtTrans.executeUpdate();
                ResultSet rsKeys = pstmtTrans.getGeneratedKeys();
                int newTransId = rsKeys.next() ? rsKeys.getInt(1) : 0;
                PreparedStatement pstmtDetail = conn.prepareStatement(
                        "INSERT INTO transaction_details (transaction_id, service_id, weight, price_per_kg, subtotal) VALUES (?, ?, ?, ?, ?)");
                pstmtDetail.setInt(1, newTransId); pstmtDetail.setInt(2, service.getId());
                pstmtDetail.setDouble(3, weight); pstmtDetail.setDouble(4, pricePerKg); pstmtDetail.setDouble(5, totalAmount);
                pstmtDetail.executeUpdate();
                conn.commit();
                JOptionPane.showMessageDialog(this, "Pesanan laundry berhasil dicatat!");
                txtWeight.setText(""); txtTotalAmount.setText(""); loadTransactionData();
            } catch (SQLException ex) { conn.rollback(); throw ex; }
            finally { conn.setAutoCommit(true); conn.close(); }
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Input berat atau harga tidak valid!");
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void loadTransactionData() {
        modelTransaction.setRowCount(0);
        String searchVal = txtSearch.getText().trim();
        String filterStatus = cbStatusFilter.getSelectedItem() != null ? cbStatusFilter.getSelectedItem().toString() : "Semua Status";
        StringBuilder sql = new StringBuilder(
                "SELECT t.id, c.name, s.service_name, td.weight, t.total_amount, t.transaction_date, t.due_date, t.status " +
                "FROM transactions t JOIN customers c ON t.customer_id = c.id " +
                "JOIN transaction_details td ON t.id = td.transaction_id " +
                "JOIN services s ON td.service_id = s.id WHERE 1=1 ");
        if (!searchVal.isEmpty()) sql.append(" AND (c.name LIKE ? OR c.phone_number LIKE ? OR t.id LIKE ?) ");
        if (!filterStatus.equals("Semua Status")) sql.append(" AND t.status = ? ");
        sql.append(" ORDER BY t.id DESC");
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (!searchVal.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + searchVal + "%");
                pstmt.setString(paramIndex++, "%" + searchVal + "%");
                pstmt.setString(paramIndex++, "%" + searchVal + "%");
            }
            if (!filterStatus.equals("Semua Status")) pstmt.setString(paramIndex, filterStatus);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    modelTransaction.addRow(new Object[]{
                            rs.getInt("id"), rs.getString("name"), rs.getString("service_name"),
                            rs.getDouble("weight"), formatRupiah(rs.getDouble("total_amount")),
                            rs.getString("transaction_date"), rs.getString("due_date"), rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateTransactionStatus() {
        int selectedRow = tableTransaction.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Pilih transaksi di tabel terlebih dahulu!"); return; }
        int transId = (int) modelTransaction.getValueAt(selectedRow, 0);
        String newStatus = cbStatusEdit.getSelectedItem().toString();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE transactions SET status = ? WHERE id = ?")) {
            pstmt.setString(1, newStatus); pstmt.setInt(2, transId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Status transaksi #" + transId + " berhasil diperbarui menjadi: " + newStatus);
            loadTransactionData();
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void printReceipt() {
        int selectedRow = tableTransaction.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Pilih transaksi di tabel yang ingin dicetak notanya!"); return; }
        JTextArea receiptArea = new JTextArea();
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        receiptArea.append("=========================================\n");
        receiptArea.append("            NOTA AQUAWASH LAUNDRY        \n");
        receiptArea.append("=========================================\n");
        receiptArea.append(" No Transaksi   : #" + modelTransaction.getValueAt(selectedRow, 0) + "\n");
        receiptArea.append(" Pelanggan      : " + modelTransaction.getValueAt(selectedRow, 1) + "\n");
        receiptArea.append(" Layanan        : " + modelTransaction.getValueAt(selectedRow, 2) + "\n");
        receiptArea.append(" Berat Cucian   : " + modelTransaction.getValueAt(selectedRow, 3) + " kg\n");
        receiptArea.append(" Total Biaya    : " + modelTransaction.getValueAt(selectedRow, 4) + "\n");
        receiptArea.append(" Tanggal Masuk  : " + modelTransaction.getValueAt(selectedRow, 5) + "\n");
        receiptArea.append(" Estimasi Jadi  : " + modelTransaction.getValueAt(selectedRow, 6) + "\n");
        receiptArea.append(" Status Pesanan : " + modelTransaction.getValueAt(selectedRow, 7) + "\n");
        receiptArea.append("=========================================\n");
        receiptArea.append("  Terima kasih telah mempercayakan       \n");
        receiptArea.append("  pakaian Anda kepada AquaWash Laundry!  \n");
        receiptArea.append("=========================================\n");
        try { receiptArea.print(); } catch (PrinterException e) { JOptionPane.showMessageDialog(this, "Gagal mencetak: " + e.getMessage()); }
    }

    private void loadReportData(DefaultTableModel modelReport, JLabel lblTotal, boolean isDaily) {
        modelReport.setRowCount(0);
        double grandTotal = 0;
        String query = isDaily
                ? "SELECT t.id, c.name, t.transaction_date, t.total_amount, t.status FROM transactions t JOIN customers c ON t.customer_id = c.id WHERE date(t.transaction_date) = date('now', 'localtime') ORDER BY t.id DESC"
                : "SELECT t.id, c.name, t.transaction_date, t.total_amount, t.status FROM transactions t JOIN customers c ON t.customer_id = c.id WHERE strftime('%Y-%m', t.transaction_date) = strftime('%Y-%m', 'now', 'localtime') ORDER BY t.id DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                double total = rs.getDouble("total_amount");
                grandTotal += total;
                modelReport.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("transaction_date"), formatRupiah(total), rs.getString("status")});
            }
            lblTotal.setText("Total Omset: " + formatRupiah(grandTotal));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static class ComboItem {
        private final int id;
        private final String description;
        public ComboItem(int id, String description) { this.id = id; this.description = description; }
        public int getId() { return id; }
        @Override public String toString() { return description; }
    }
}