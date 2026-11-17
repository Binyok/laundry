package laundry.ui.panels;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.print.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.Preferences;

public class CetakNotaPanel extends BasePanel {

    private JEditorPane sketsaNotaPane;
    private JLabel lblWirelessStatus;
    private String currentSelectedKode = "-";
    private String currentSelectedPelanggan = "-";
    private String currentSelectedLayanan = "-";
    private String currentSelectedQty = "-";
    private String currentSelectedKaryawan = "-";
    private String currentSelectedTanggal = "-";
    private String currentSelectedTotal = "0";
    private String currentSelectedStatus = "Proses";

    private final Preferences prefs = Preferences.userNodeForPackage(CetakNotaPanel.class);
    private static final String PREF_IP = "wireless_printer_ip";
    private static final String PREF_PORT = "wireless_printer_port";
    private static final String PREF_AUTOCUT = "wireless_printer_autocut";

    public CetakNotaPanel() {
        super();
        init();
    }

    private String getSavedIp() {
        return prefs.get(PREF_IP, "192.168.1.200");
    }

    private int getSavedPort() {
        return prefs.getInt(PREF_PORT, 9100);
    }

    private boolean getSavedAutoCut() {
        return prefs.getBoolean(PREF_AUTOCUT, true);
    }

    private void updateWirelessStatusLabel() {
        if (lblWirelessStatus != null) {
            lblWirelessStatus.setText("<html>Printer Wireless Aktif: <b>" + getSavedIp() + ":" + getSavedPort() + "</b> (Auto-Cut: " + (getSavedAutoCut() ? "ON" : "OFF") + ")</html>");
        }
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // 1. TOP SECTION: TABEL DAFTAR TRANSAKSI LAUNDRY (ATAS & DOMINAN)
        JPanel tableCard = new JPanel(new BorderLayout(0, 6));
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JLabel titleTbl = new JLabel("Daftar Transaksi Laundry (Pilih untuk Cetak Nota)");
        titleTbl.setFont(FONT_TITLE);
        titleTbl.setForeground(TEXT_PRIMARY);
        tableCard.add(titleTbl, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{
            "ID", "Kode Transaksi", "Pelanggan", "Layanan Laundry", "Jumlah", "Karyawan", "Tanggal", "Total (Rp)", "Status"
        }, 0);
        table = createStyledTable(tableModel);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    currentSelectedKode = str(tableModel.getValueAt(row, 1));
                    currentSelectedPelanggan = str(tableModel.getValueAt(row, 2));
                    currentSelectedLayanan = str(tableModel.getValueAt(row, 3));
                    currentSelectedQty = str(tableModel.getValueAt(row, 4));
                    currentSelectedKaryawan = str(tableModel.getValueAt(row, 5));
                    currentSelectedTanggal = str(tableModel.getValueAt(row, 6));
                    currentSelectedTotal = str(tableModel.getValueAt(row, 7));
                    currentSelectedStatus = str(tableModel.getValueAt(row, 8));
                    updateSketsaNota();
                }
            }
        });

        JScrollPane tableScroll = createScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(0, 220));
        tableCard.add(tableScroll, BorderLayout.CENTER);
        add(tableCard, BorderLayout.NORTH);

        // 2. BOTTOM SECTION: PUSAT PENCETAKAN & SKETSA NOTA 80 MM
        JPanel bottomCard = new JPanel(new BorderLayout(14, 0));
        bottomCard.setOpaque(false);

        // Panel Kiri Bawah: Tombol Aksi & Informasi
        JPanel actionInfoCard = createCard();
        actionInfoCard.setLayout(new BorderLayout(0, 10));

        JLabel titleHeader = new JLabel("Pusat Cetak Nota (Thermal 80mm & Wireless)");
        titleHeader.setFont(FONT_TITLE);
        titleHeader.setForeground(PRIMARY);
        actionInfoCard.add(titleHeader, BorderLayout.NORTH);

        JPanel infoContent = new JPanel();
        infoContent.setLayout(new BoxLayout(infoContent, BoxLayout.Y_AXIS));
        infoContent.setOpaque(false);

        JLabel descHeader = new JLabel("<html><b>Petunjuk:</b><br>" +
                "1. Pilih transaksi pada tabel di atas.<br>" +
                "2. Cek pratinjau struk di kotak sebelah kanan.<br>" +
                "3. Klik <b>Cetak Nota</b> untuk print via Wi-Fi/Wireless atau Bluetooth/USB.<br>" +
                "4. Gunakan tombol <b>Pengaturan Wireless</b> untuk setting & tes printer IP.</html>");
        descHeader.setFont(FONT_LABEL);
        descHeader.setForeground(TEXT_SECONDARY);
        descHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoContent.add(descHeader);
        infoContent.add(Box.createVerticalStrut(10));

        lblWirelessStatus = new JLabel();
        lblWirelessStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblWirelessStatus.setForeground(new Color(13, 148, 136));
        lblWirelessStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        updateWirelessStatusLabel();
        infoContent.add(lblWirelessStatus);

        actionInfoCard.add(infoContent, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setOpaque(false);

        JButton btnPrintSelected = createButton("Cetak Nota Pesanan", new Color(13, 148, 136));
        btnPrintSelected.setPreferredSize(new Dimension(170, 38));
        btnPrintSelected.addActionListener(e -> cetakNotaSelectedRow());

        JButton btnWirelessConfig = createButton("Pengaturan Wireless", PRIMARY);
        btnWirelessConfig.setPreferredSize(new Dimension(170, 38));
        btnWirelessConfig.addActionListener(e -> showWirelessSettingsDialog());

        btnRow.add(btnPrintSelected);
        btnRow.add(btnWirelessConfig);

        actionInfoCard.add(btnRow, BorderLayout.SOUTH);

        bottomCard.add(actionInfoCard, BorderLayout.CENTER);

        // Panel Kanan Bawah: Sketsa Nota 80 mm (Live Preview Struk Proporsional)
        JPanel sketsaCard = new JPanel(new BorderLayout(0, 4));
        sketsaCard.setBackground(new Color(254, 252, 232)); // Warm yellow receipt tint
        sketsaCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(234, 179, 8), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        sketsaCard.setPreferredSize(new Dimension(280, 210));

        JLabel lblSketsaTitle = new JLabel("Sketsa Nota 80 mm (Live Preview)");
        lblSketsaTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSketsaTitle.setForeground(new Color(161, 98, 7));
        sketsaCard.add(lblSketsaTitle, BorderLayout.NORTH);

        sketsaNotaPane = new JEditorPane("text/html", "");
        sketsaNotaPane.setEditable(false);
        sketsaNotaPane.setBackground(Color.WHITE);
        JScrollPane sketsaScroll = new JScrollPane(sketsaNotaPane);
        sketsaScroll.setBorder(BorderFactory.createLineBorder(new Color(253, 224, 71), 1));
        sketsaCard.add(sketsaScroll, BorderLayout.CENTER);

        bottomCard.add(sketsaCard, BorderLayout.EAST);

        add(bottomCard, BorderLayout.CENTER);

        updateSketsaNota();
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private void updateSketsaNota() {
        if (sketsaNotaPane == null) return;
        double totVal = 0;
        try {
            totVal = Double.parseDouble(currentSelectedTotal.replace("Rp ", "").replace(".", "").replace(",", ".").trim());
        } catch (Exception ignored) {}

        String html = "<html><body style='font-family: monospace; font-size: 9px; padding: 6px; background: #fff; color: #111; width: 240px;'>" +
                "<div align='center'><b>LAUNDRY SISKA SIGRO</b><br><span style='font-size:7px;'>STRUK THERMAL 80MM</span></div>" +
                "========================================<br>" +
                "<b>Kode:</b> " + currentSelectedKode + "<br>" +
                "<b>Tgl:</b> " + currentSelectedTanggal + "<br>" +
                "<b>Pelanggan:</b> " + currentSelectedPelanggan + "<br>" +
                "<b>Kasir:</b> " + currentSelectedKaryawan + "<br>" +
                "========================================<br>" +
                "<b>Rincian:</b><br>" +
                " * " + currentSelectedLayanan + " (" + currentSelectedQty + ")<br>" +
                "========================================<br>" +
                "<div align='right'><h3 style='margin:2px 0;'>Total: Rp " + String.format("%,.0f", totVal) + "</h3></div>" +
                "<div align='center'><b>Status: " + currentSelectedStatus + "</b></div>" +
                "========================================<br>" +
                "<div align='center' style='font-size:8px;'>Terima Kasih Atas Kepercayaan Anda!<br><i>Laundry Bersih, Wangi, & Rapi</i></div>" +
                "</body></html>";
        sketsaNotaPane.setText(html);
    }

    public void showWirelessSettingsDialog() {
        JTextField tfIp = createTextField();
        tfIp.setText(getSavedIp());
        tfIp.setPreferredSize(new Dimension(170, 32));

        JTextField tfPort = createTextField();
        tfPort.setText(String.valueOf(getSavedPort()));
        tfPort.setPreferredSize(new Dimension(80, 32));

        JCheckBox cbCut = new JCheckBox("Potong Kertas Otomatis (Auto-Cut ESC/POS)", getSavedAutoCut());
        cbCut.setOpaque(false);
        cbCut.setFont(FONT_LABEL);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; form.add(createLabel("IP Address Printer:"), g);
        g.gridx = 1; form.add(tfIp, g);

        g.gridx = 0; g.gridy = 1; form.add(createLabel("Port Printer (Default 9100):"), g);
        g.gridx = 1; form.add(tfPort, g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; form.add(cbCut, g);

        JLabel tip = new JLabel("<html><i style='color: #64748b; font-size: 11px;'>" +
                "* Tips Cek IP Printer: Matikan printer, tahan tombol FEED, lalu nyalakan printer.<br>" +
                "Kertas Self-Test akan mencetak IP Address printer Wi-Fi / LAN Anda.</i></html>");
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; form.add(tip, g);

        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        container.add(form, BorderLayout.CENTER);

        JPanel testRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        testRow.setOpaque(false);
        JButton btnTest = createButton("Uji Koneksi & Cetak Sampel", new Color(13, 148, 136));
        btnTest.setPreferredSize(new Dimension(210, 34));
        btnTest.addActionListener(e -> {
            String ip = tfIp.getText().trim();
            int port = 9100;
            try { port = Integer.parseInt(tfPort.getText().trim()); } catch (Exception ignored) {}
            if (ip.isEmpty()) {
                showError("Masukkan IP Address printer terlebih dahulu!");
                return;
            }
            boolean success = testWirelessConnection(ip, port, cbCut.isSelected());
            if (success) {
                showSuccess("Berhasil terhubung ke printer wireless di " + ip + ":" + port + "!\nSampel pengujian telah dikirim dan dicetak.");
            } else {
                showError("Gagal terhubung ke printer di " + ip + ":" + port + ".\n\n" +
                        "Pastikan:\n" +
                        "1. Printer thermal dalam kondisi MENYALA (ON).\n" +
                        "2. Komputer dan printer terhubung ke Wi-Fi / router yang SAMA.\n" +
                        "3. IP Address sudah sesuai dengan hasil cetak Self-Test printer.");
            }
        });
        testRow.add(btnTest);
        container.add(testRow, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, container,
                "Pengaturan Printer Wireless (Wi-Fi / LAN)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String ip = tfIp.getText().trim();
            int port = 9100;
            try { port = Integer.parseInt(tfPort.getText().trim()); } catch (Exception ignored) {}
            if (!ip.isEmpty()) {
                prefs.put(PREF_IP, ip);
                prefs.putInt(PREF_PORT, port);
                prefs.putBoolean(PREF_AUTOCUT, cbCut.isSelected());
                updateWirelessStatusLabel();
                showSuccess("Pengaturan printer wireless tersimpan!\nIP: " + ip + ":" + port);
            }
        }
    }

    private boolean testWirelessConnection(String ip, int port, boolean autoCut) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), 3000);
            try (OutputStream out = socket.getOutputStream()) {
                ByteArrayOutputStream testBaos = new ByteArrayOutputStream();
                testBaos.write(new byte[]{0x1B, 0x40}); // ESC @ (Inisialisasi)
                testBaos.write(new byte[]{0x1B, 0x61, 0x01}); // ESC a 1 (Tengah)
                testBaos.write(new byte[]{0x1B, 0x45, 0x01}); // ESC E 1 (Bold ON)
                testBaos.write("LAUNDRY SISKA SIGRO\n".getBytes("CP437"));
                testBaos.write(new byte[]{0x1B, 0x45, 0x00}); // Bold OFF
                testBaos.write("UJI KONEKSI PRINTER WIRELESS\n".getBytes("CP437"));
                testBaos.write("========================================\n".getBytes("CP437"));
                testBaos.write(new byte[]{0x1B, 0x61, 0x00}); // ESC a 0 (Kiri)
                testBaos.write(("IP Printer : " + ip + "\n").getBytes("CP437"));
                testBaos.write(("Port       : " + port + "\n").getBytes("CP437"));
                testBaos.write(("Waktu Tes  : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) + "\n").getBytes("CP437"));
                testBaos.write("Status     : TERHUBUNG SUKSES (OK)\n".getBytes("CP437"));
                testBaos.write("========================================\n".getBytes("CP437"));
                testBaos.write(new byte[]{0x1B, 0x61, 0x01}); // Tengah
                testBaos.write("Printer Siap Digunakan Kasir\n".getBytes("CP437"));
                testBaos.write("\n\n\n\n".getBytes("CP437"));

                if (autoCut) {
                    testBaos.write(new byte[]{0x1D, 0x56, 0x42, 0x00}); // Auto-cut
                }

                out.write(testBaos.toByteArray());
                out.flush();
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private byte[] buildEscPosNota(boolean autoCut) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            double totVal = 0;
            try {
                totVal = Double.parseDouble(currentSelectedTotal.replace("Rp ", "").replace(".", "").replace(",", ".").trim());
            } catch (Exception ignored) {}

            // Inisialisasi ESC @
            baos.write(new byte[]{0x1B, 0x40});

            // Center Align
            baos.write(new byte[]{0x1B, 0x61, 0x01});
            // Bold ON
            baos.write(new byte[]{0x1B, 0x45, 0x01});
            baos.write("LAUNDRY SISKA SIGRO\n".getBytes("CP437"));
            // Bold OFF
            baos.write(new byte[]{0x1B, 0x45, 0x00});
            baos.write("Management System\n".getBytes("CP437"));
            baos.write("========================================\n".getBytes("CP437"));

            // Left Align
            baos.write(new byte[]{0x1B, 0x61, 0x00});
            baos.write(String.format("Kode Transaksi : %s\n", currentSelectedKode).getBytes("CP437"));
            baos.write(String.format("Tanggal        : %s\n", currentSelectedTanggal).getBytes("CP437"));
            baos.write(String.format("Pelanggan      : %s\n", currentSelectedPelanggan).getBytes("CP437"));
            baos.write(String.format("Kasir/Petugas  : %s\n", currentSelectedKaryawan).getBytes("CP437"));
            baos.write("========================================\n".getBytes("CP437"));
            baos.write("Rincian Layanan:\n".getBytes("CP437"));
            baos.write(String.format(" * %s\n", currentSelectedLayanan).getBytes("CP437"));
            baos.write(String.format("   Jumlah/Berat: %s\n", currentSelectedQty).getBytes("CP437"));
            baos.write("========================================\n".getBytes("CP437"));

            // Total Bold
            baos.write(new byte[]{0x1B, 0x45, 0x01});
            baos.write(String.format("TOTAL BIAYA    : Rp %,.0f\n", totVal).getBytes("CP437"));
            baos.write(new byte[]{0x1B, 0x45, 0x00});
            baos.write(String.format("STATUS CUCIAN  : %s\n", currentSelectedStatus).getBytes("CP437"));
            baos.write("========================================\n".getBytes("CP437"));

            // Center Align Footer
            baos.write(new byte[]{0x1B, 0x61, 0x01});
            baos.write("Terima Kasih Atas Kepercayaan Anda!\n".getBytes("CP437"));
            baos.write("Laundry Bersih, Wangi, & Rapi\n".getBytes("CP437"));
            baos.write("\n\n\n\n".getBytes("CP437"));

            if (autoCut) {
                baos.write(new byte[]{0x1D, 0x56, 0x42, 0x00}); // Auto-cut
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private void cetakNotaSelectedRow() {
        if (currentSelectedKode.equals("-")) {
            showError("Pilih salah satu transaksi pada tabel di bagian atas terlebih dahulu!");
            return;
        }

        double totVal = 0;
        try {
            totVal = Double.parseDouble(currentSelectedTotal.replace("Rp ", "").replace(".", "").replace(",", ".").trim());
        } catch (Exception ignored) {}

        String notaText = "          LAUNDRY SISKA SIGRO          \n" +
                "           Management System           \n" +
                "========================================\n" +
                "Kode Transaksi : " + currentSelectedKode + "\n" +
                "Tanggal        : " + currentSelectedTanggal + "\n" +
                "Pelanggan      : " + currentSelectedPelanggan + "\n" +
                "Kasir/Petugas  : " + currentSelectedKaryawan + "\n" +
                "========================================\n" +
                "Rincian Layanan:\n" +
                " * " + currentSelectedLayanan + "\n" +
                "   Jumlah: " + currentSelectedQty + "\n" +
                "========================================\n" +
                "TOTAL          : Rp " + String.format("%,.0f", totVal) + "\n" +
                "STATUS         : " + currentSelectedStatus + "\n" +
                "========================================\n" +
                "   Terima Kasih Atas Kepercayaan Anda!  \n" +
                "      Laundry Bersih, Wangi, & Rapi     \n\n\n";

        String savedIp = getSavedIp();
        int savedPort = getSavedPort();
        boolean savedCut = getSavedAutoCut();

        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        java.util.List<String> optionsList = new java.util.ArrayList<>();
        String optWirelessDefault = "[ 1-KLIK WIRELESS ] Cetak ke Printer Wi-Fi (" + savedIp + ":" + savedPort + ")";
        String optWirelessConfig = "[ + GANTI IP / PENGATURAN WIRELESS ]";

        optionsList.add(optWirelessDefault);
        optionsList.add(optWirelessConfig);

        if (printServices != null) {
            for (PrintService ps : printServices) {
                optionsList.add(ps.getName());
            }
        }

        String selectedOption = (String) JOptionPane.showInputDialog(
                this,
                "Pilih Metode Cetak untuk Nota " + currentSelectedKode + ":",
                "Pusat Cetak Nota Thermal 80mm - " + currentSelectedKode,
                JOptionPane.QUESTION_MESSAGE,
                null,
                optionsList.toArray(),
                optWirelessDefault
        );

        if (selectedOption != null) {
            if (selectedOption.equals(optWirelessDefault)) {
                // Cetak langsung ke IP yang tersimpan dengan perintah ESC/POS native & auto cut
                byte[] data = buildEscPosNota(savedCut);
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(savedIp, savedPort), 3000);
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write(data);
                        out.flush();
                    }
                    showSuccess("Nota " + currentSelectedKode + " berhasil dicetak secara WIRELESS ke IP: " + savedIp);
                } catch (Exception ex) {
                    showError("Gagal terhubung ke printer wireless IP " + savedIp + " (Port " + savedPort + "):\n" +
                            ex.getMessage() + "\n\n" +
                            "Solusi:\n" +
                            "1. Periksa apakah printer dalam kondisi ON.\n" +
                            "2. Pastikan PC dan printer ada di Wi-Fi yang sama.\n" +
                            "3. Gunakan menu 'Pengaturan Wireless' untuk mengecek atau mengubah IP.");
                }
            } else if (selectedOption.equals(optWirelessConfig)) {
                showWirelessSettingsDialog();
            } else {
                // Cetak via Windows PrintService (Driver USB / Bluetooth)
                PrintService chosenService = null;
                for (PrintService ps : printServices) {
                    if (ps.getName().equals(selectedOption)) {
                        chosenService = ps;
                        break;
                    }
                }

                if (chosenService != null) {
                    try {
                        PrinterJob job = PrinterJob.getPrinterJob();
                        job.setPrintService(chosenService);

                        // Atur ukuran kertas pas 80mm thermal (80mm = ~226.7 points)
                        PageFormat pf = job.defaultPage();
                        Paper paper = pf.getPaper();
                        double width80mm = 80 * 2.83465;
                        double heightAuto = 700;
                        paper.setSize(width80mm, heightAuto);
                        paper.setImageableArea(3, 3, width80mm - 6, heightAuto - 6);
                        pf.setPaper(paper);

                        job.setPrintable(new Printable() {
                            @Override
                            public int print(Graphics g, PageFormat pageFormat, int page) throws PrinterException {
                                if (page > 0) return NO_SUCH_PAGE;
                                Graphics2D g2d = (Graphics2D) g;
                                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                                g2d.setFont(new Font("Monospaced", Font.PLAIN, 7));

                                int y = 10;
                                for (String line : notaText.split("\n")) {
                                    g2d.drawString(line, 2, y);
                                    y += 9;
                                }
                                return PAGE_EXISTS;
                            }
                        }, pf);

                        job.print();
                        showSuccess("Nota 80mm " + currentSelectedKode + " berhasil dicetak ke Printer: " + chosenService.getName());
                    } catch (Exception ex) {
                        showError("Gagal mencetak nota: " + ex.getMessage());
                    }
                }
            }
        }
    }

    @Override
    protected void loadData() {
        tableModel.setRowCount(0);
        String sql =
            "SELECT t.id, t.kode_transaksi, COALESCE(p.nama, '-'), " +
            "COALESCE(MAX(l.nama), '-'), COALESCE(MAX(dt.qty), 1), COALESCE(MAX(l.satuan), 'Kg'), " +
            "COALESCE(k.nama, '-'), t.tanggal, t.total, t.status " +
            "FROM transaksi t " +
            "LEFT JOIN pelanggan p ON t.pelanggan_id = p.id " +
            "LEFT JOIN karyawan k ON t.karyawan_id = k.id " +
            "LEFT JOIN detail_transaksi dt ON dt.transaksi_id = t.id " +
            "LEFT JOIN layanan l ON dt.layanan_id = l.id " +
            "GROUP BY t.id, t.kode_transaksi, p.nama, k.nama, t.tanggal, t.total, t.status " +
            "ORDER BY t.id DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String qtySat = rs.getDouble(5) + " " + rs.getString(6);
                tableModel.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    qtySat,
                    rs.getString(7),
                    rs.getString(8),
                    String.format("Rp %,.0f", rs.getDouble(9)),
                    rs.getString(10)
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
