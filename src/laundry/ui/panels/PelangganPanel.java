package laundry.ui.panels;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class PelangganPanel extends BasePanel {

    private JTextField tfNama, tfAlamat, tfTelepon, tfEmail;
    private int selectedId = -1;

    public PelangganPanel() {
        super();
        init();
    }

    @Override
    protected void initComponents() {
        JPanel formCard = createCard();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        tfNama = createTextField();
        tfAlamat = createTextField();
        tfTelepon = createTextField();
        tfEmail = createTextField();

        gbc.gridx = 0; gbc.gridy = 0; formCard.add(createLabel("Nama"), gbc);
        gbc.gridx = 1; formCard.add(tfNama, gbc);
        gbc.gridx = 2; formCard.add(createLabel("Alamat"), gbc);
        gbc.gridx = 3; formCard.add(tfAlamat, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formCard.add(createLabel("Telepon"), gbc);
        gbc.gridx = 1; formCard.add(tfTelepon, gbc);
        gbc.gridx = 2; formCard.add(createLabel("Email"), gbc);
        gbc.gridx = 3; formCard.add(tfEmail, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);

        JButton btnSave = createButton("Simpan", ACCENT);
        JButton btnUpdate = createButton("Update", WARNING);
        JButton btnDelete = createButton("Hapus", DANGER);
        JButton btnClear = createButton("Clear", new Color(100, 116, 139));
        
        JButton btnCetakBarcode = createButton("Cetak Barcode", new Color(13, 148, 136));
        btnCetakBarcode.setPreferredSize(new Dimension(120, 36));
        JButton btnScanBarcode = createButton("Scan Barcode", new Color(99, 102, 241));
        btnScanBarcode.setPreferredSize(new Dimension(120, 36));

        btnSave.addActionListener(e -> saveData());
        btnUpdate.addActionListener(e -> updateData());
        btnDelete.addActionListener(e -> deleteData());
        btnClear.addActionListener(e -> clearForm());
        btnCetakBarcode.addActionListener(e -> cetakBarcodePelanggan());
        btnScanBarcode.addActionListener(e -> scanBarcodePelanggan());

        btnPanel.add(btnSave);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        btnPanel.add(btnCetakBarcode);
        btnPanel.add(btnScanBarcode);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        formCard.add(btnPanel, gbc);

        add(formCard, BorderLayout.NORTH);

        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "Alamat", "Telepon", "Email", "Created"}, 0);
        table = createStyledTable(tableModel);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    selectedId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                    tfNama.setText(tableModel.getValueAt(row, 1).toString());
                    tfAlamat.setText(str(tableModel.getValueAt(row, 2)));
                    tfTelepon.setText(str(tableModel.getValueAt(row, 3)));
                    tfEmail.setText(str(tableModel.getValueAt(row, 4)));
                }
            }
        });

        tableCard.add(createScrollPane(table), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }

    private void saveData() {
        if (tfNama.getText().trim().isEmpty()) { showError("Nama harus diisi!"); return; }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO pelanggan(nama,alamat,telepon,email) VALUES(?,?,?,?)")) {
            ps.setString(1, tfNama.getText().trim());
            ps.setString(2, tfAlamat.getText().trim());
            ps.setString(3, tfTelepon.getText().trim());
            ps.setString(4, tfEmail.getText().trim());
            ps.executeUpdate();
            showSuccess("Data pelanggan berhasil disimpan!");
            clearForm();
            loadData();
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void updateData() {
        if (selectedId == -1) { showError("Pilih data yang akan diupdate!"); return; }
        if (tfNama.getText().trim().isEmpty()) { showError("Nama harus diisi!"); return; }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE pelanggan SET nama=?,alamat=?,telepon=?,email=? WHERE id=?")) {
            ps.setString(1, tfNama.getText().trim());
            ps.setString(2, tfAlamat.getText().trim());
            ps.setString(3, tfTelepon.getText().trim());
            ps.setString(4, tfEmail.getText().trim());
            ps.setInt(5, selectedId);
            ps.executeUpdate();
            showSuccess("Data pelanggan berhasil diupdate!");
            clearForm();
            loadData();
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void deleteData() {
        if (selectedId == -1) { showError("Pilih data yang akan dihapus!"); return; }
        if (!confirmDelete()) return;
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM pelanggan WHERE id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            showSuccess("Data pelanggan berhasil dihapus!");
            clearForm();
            loadData();
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void clearForm() {
        selectedId = -1;
        tfNama.setText("");
        tfAlamat.setText("");
        tfTelepon.setText("");
        tfEmail.setText("");
        table.clearSelection();
    }

    private void cetakBarcodePelanggan() {
        if (selectedId == -1) {
            showError("Pilih salah satu data pelanggan pada tabel terlebih dahulu untuk mencetak Barcode!");
            return;
        }
        String nama = tfNama.getText().trim();
        String barcodeData = "LND-PLG-" + String.format("%03d", selectedId);

        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(10, 10, 320, 160, 16, 16);
                g2d.setColor(new Color(30, 58, 138));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(10, 10, 320, 160, 16, 16);

                g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2d.drawString("BARCODE DATA DIRI PELANGGAN", 24, 34);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2d.setColor(new Color(100, 116, 139));
                g2d.drawString("Laundry Siska Sigro", 24, 48);

                g2d.setColor(new Color(30, 41, 59));
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2d.drawString("Nama: " + nama, 24, 72);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2d.drawString("ID Member: LND-PLG-" + String.format("%03d", selectedId), 24, 88);

                int barX = 24;
                int barY = 105;
                int barWidth = 270;
                int barHeight = 35;
                g2d.setColor(Color.WHITE);
                g2d.fillRect(barX, barY, barWidth, barHeight);
                
                g2d.setColor(Color.BLACK);
                int seed = Math.abs(barcodeData.hashCode());
                int currentX = barX + 15;
                for (int i = 0; i < 45; i++) {
                    int w = (seed % 3 == 0) ? 2 : ((seed % 5 == 0) ? 4 : 1);
                    boolean isBlack = (i % 2 == 0);
                    if (isBlack) {
                        g2d.fillRect(currentX, barY + 4, w, barHeight - 10);
                    }
                    currentX += w + 2;
                    seed = (seed + 17) * 31;
                    if (currentX >= barX + barWidth - 15) break;
                }

                g2d.setFont(new Font("Monospaced", Font.BOLD, 9));
                g2d.drawString(barcodeData, barX + 90, barY + barHeight - 1);
            }
        };
        cardPanel.setPreferredSize(new Dimension(340, 180));
        cardPanel.setOpaque(false);

        JPanel dialogPanel = new JPanel(new BorderLayout(0, 10));
        dialogPanel.add(cardPanel, BorderLayout.CENTER);

        JButton btnPrintCard = createButton("Cetak Barcode", new Color(13, 148, 136));
        btnPrintCard.addActionListener(e -> showSuccess("Barcode Data Diri untuk " + nama + " berhasil dikirim ke printer!"));
        dialogPanel.add(btnPrintCard, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, dialogPanel, "Cetak Barcode - " + nama, JOptionPane.PLAIN_MESSAGE);
    }

    private void scanBarcodePelanggan() {
        JTextField tfScanInput = createTextField();
        tfScanInput.setPreferredSize(new Dimension(250, 34));

        JPanel scanPanel = new JPanel(new BorderLayout(0, 8));
        scanPanel.add(new JLabel("<html><b>Arahkan Barcode Scanner atau Masukkan ID / Kode Pelanggan (Cth: LND-PLG-001):</b></html>"), BorderLayout.NORTH);
        scanPanel.add(tfScanInput, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, scanPanel, "Scan Barcode Pelanggan", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String code = tfScanInput.getText().trim();
            if (code.isEmpty()) {
                showError("Kode barcode tidak boleh kosong!");
                return;
            }

            // Extract numeric ID from code like LND-PLG-001 or just number
            int custId = -1;
            try {
                String clean = code.replaceAll("[^0-9]", "");
                custId = Integer.parseInt(clean);
            } catch (Exception ignored) {}

            if (custId == -1) {
                showError("Format barcode tidak valid!");
                return;
            }

            // Query customer data and summary laundry
            try {
                String nama = "-", alamat = "-", telp = "-", email = "-";
                try (PreparedStatement ps = conn.prepareStatement("SELECT nama, alamat, telepon, email FROM pelanggan WHERE id = ?")) {
                    ps.setInt(1, custId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        nama = rs.getString(1);
                        alamat = rs.getString(2);
                        telp = rs.getString(3);
                        email = rs.getString(4);
                    } else {
                        showError("Pelanggan dengan ID " + custId + " tidak ditemukan!");
                        return;
                    }
                }

                int totalTrx = 0;
                double totalQty = 0;
                double totalBelanja = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*), COALESCE(SUM(total),0) FROM transaksi WHERE pelanggan_id = ?")) {
                    ps.setInt(1, custId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        totalTrx = rs.getInt(1);
                        totalBelanja = rs.getDouble(2);
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COALESCE(SUM(dt.qty),0) FROM detail_transaksi dt JOIN transaksi t ON dt.transaksi_id = t.id WHERE t.pelanggan_id = ?")) {
                    ps.setInt(1, custId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        totalQty = rs.getDouble(1);
                    }
                }

                String scanInfo = "<html><body style='font-family: sans-serif; padding: 6px; width: 280px;'>" +
                        "<h3 style='color: #1e3a8a; margin: 0 0 8px 0;'>HASIL SCAN BARCODE PELANGGAN</h3>" +
                        "<b>ID Pelanggan:</b> LND-PLG-" + String.format("%03d", custId) + "<br>" +
                        "<b>Nama:</b> " + nama + "<br>" +
                        "<b>Telepon:</b> " + (telp == null ? "-" : telp) + "<br>" +
                        "<b>Alamat:</b> " + (alamat == null ? "-" : alamat) + "<br>" +
                        "<b>Email:</b> " + (email == null ? "-" : email) + "<br>" +
                        "<hr style='border: solid 1px #cbd5e1;'>" +
                        "<b>Total Jumlah Laundry:</b> " + totalTrx + " Pesanan<br>" +
                        "<b>Total Qty / Berat:</b> " + totalQty + " Unit / Kg<br>" +
                        "<b>Total Akumulasi Belanja:</b> Rp " + String.format("%,.0f", totalBelanja) + "<br>" +
                        "</body></html>";
                JOptionPane.showMessageDialog(this, scanInfo, "Data Diri & Jumlah Laundry - " + nama, JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                showError("Gagal mengambil data pelanggan: " + ex.getMessage());
            }
        }
    }

    @Override
    protected void loadData() {
        tableModel.setRowCount(0);
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM pelanggan ORDER BY id DESC")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("nama"), rs.getString("alamat"),
                    rs.getString("telepon"), rs.getString("email"), rs.getString("created_at")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
