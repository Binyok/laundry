package laundry.ui.panels;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TransaksiPanel extends BasePanel {

    private JComboBox<String> cbPelanggan, cbKaryawan, cbStatus, cbLayanan;
    private JTextField tfKode, tfTanggal, tfQty, tfHarga, tfTotal;
    private Map<String, Integer> pelangganMap = new HashMap<>();
    private Map<String, Integer> karyawanMap = new HashMap<>();
    private Map<String, Integer> layananMap = new HashMap<>();
    private Map<String, Double> layananHargaMap = new HashMap<>();
    private Map<String, String> layananSatuanMap = new HashMap<>();
    private int selectedId = -1;

    public TransaksiPanel() {
        super();
        init();
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // 1. TOP SECTION: FORM INPUT TRANSAKSI LAUNDRY (KOMPAK)
        JPanel formCard = new JPanel(new BorderLayout(0, 8));
        formCard.setBackground(CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        JLabel titleForm = new JLabel("Form Transaksi Laundry");
        titleForm.setFont(FONT_TITLE);
        titleForm.setForeground(TEXT_PRIMARY);
        formCard.add(titleForm, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        tfKode = createTextField();
        tfKode.setPreferredSize(new Dimension(130, 28));
        tfKode.setEditable(false);
        generateKode();

        tfTanggal = createTextField();
        tfTanggal.setPreferredSize(new Dimension(100, 28));
        tfTanggal.setText(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));

        cbPelanggan = createComboBox();
        cbPelanggan.setPreferredSize(new Dimension(150, 28));

        cbKaryawan = createComboBox();
        cbKaryawan.setPreferredSize(new Dimension(130, 28));

        cbLayanan = createComboBox();
        cbLayanan.setPreferredSize(new Dimension(160, 28));

        tfHarga = createTextField();
        tfHarga.setPreferredSize(new Dimension(95, 28));
        tfHarga.setEditable(false);

        tfQty = createTextField();
        tfQty.setPreferredSize(new Dimension(65, 28));
        tfQty.setText("1");

        tfTotal = createTextField();
        tfTotal.setPreferredSize(new Dimension(115, 28));
        tfTotal.setEditable(false);

        cbStatus = createComboBox();
        cbStatus.setPreferredSize(new Dimension(105, 28));
        cbStatus.addItem("Proses");
        cbStatus.addItem("Selesai");
        cbStatus.addItem("Diambil");
        cbStatus.addItem("Batal");

        // Event hitung total otomatis saat layanan atau qty berubah
        cbLayanan.addActionListener(e -> updateHargaDanTotal());
        tfQty.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                hitungTotalOtomatis();
            }
        });

        // Baris 0: Kode, Tanggal, Pelanggan, Karyawan
        gbc.gridx = 0; gbc.gridy = 0; formGrid.add(createLabel("Kode:"), gbc);
        gbc.gridx = 1; formGrid.add(tfKode, gbc);
        gbc.gridx = 2; formGrid.add(createLabel("Tanggal:"), gbc);
        gbc.gridx = 3; formGrid.add(tfTanggal, gbc);
        gbc.gridx = 4; formGrid.add(createLabel("Pelanggan:"), gbc);
        gbc.gridx = 5; formGrid.add(cbPelanggan, gbc);
        gbc.gridx = 6; formGrid.add(createLabel("Karyawan:"), gbc);
        gbc.gridx = 7; formGrid.add(cbKaryawan, gbc);

        // Baris 1: Layanan, Tarif, Jumlah (Qty), Total
        gbc.gridx = 0; gbc.gridy = 1; formGrid.add(createLabel("Layanan:"), gbc);
        gbc.gridx = 1; formGrid.add(cbLayanan, gbc);
        gbc.gridx = 2; formGrid.add(createLabel("Tarif:"), gbc);
        gbc.gridx = 3; formGrid.add(tfHarga, gbc);
        gbc.gridx = 4; formGrid.add(createLabel("Jumlah:"), gbc);
        gbc.gridx = 5; formGrid.add(tfQty, gbc);
        gbc.gridx = 6; formGrid.add(createLabel("Total Biaya:"), gbc);
        gbc.gridx = 7; formGrid.add(tfTotal, gbc);

        // Baris 2: Status Transaksi + Tombol Aksi (Simpan, Update, Hapus, Transaksi Baru)
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);

        actionRow.add(createLabel("Status:"));
        actionRow.add(cbStatus);
        actionRow.add(Box.createHorizontalStrut(12));

        JButton btnSave = createButton("Simpan", ACCENT);
        btnSave.setPreferredSize(new Dimension(85, 30));
        JButton btnUpdate = createButton("Update", WARNING);
        btnUpdate.setPreferredSize(new Dimension(85, 30));
        JButton btnDelete = createButton("Hapus", DANGER);
        btnDelete.setPreferredSize(new Dimension(80, 30));
        JButton btnClear = createButton("Transaksi Baru", new Color(100, 116, 139));
        btnClear.setPreferredSize(new Dimension(130, 30));

        btnSave.addActionListener(e -> saveData());
        btnUpdate.addActionListener(e -> updateData());
        btnDelete.addActionListener(e -> deleteData());
        btnClear.addActionListener(e -> clearForm());

        actionRow.add(btnSave);
        actionRow.add(btnUpdate);
        actionRow.add(btnDelete);
        actionRow.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 8;
        formGrid.add(actionRow, gbc);

        formCard.add(formGrid, BorderLayout.CENTER);
        add(formCard, BorderLayout.NORTH);

        // 2. BOTTOM SECTION: TABEL DAFTAR TRANSAKSI (FULL-WIDTH)
        JPanel tableCard = new JPanel(new BorderLayout(0, 8));
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 16, 12, 16)
        ));

        JLabel titleTbl = new JLabel("Daftar Transaksi Laundry");
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
                    selectedId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                    tfKode.setText(str(tableModel.getValueAt(row, 1)));
                    cbPelanggan.setSelectedItem(str(tableModel.getValueAt(row, 2)));

                    String layName = str(tableModel.getValueAt(row, 3));
                    cbLayanan.setSelectedItem(layName);

                    String qtyStr = str(tableModel.getValueAt(row, 4)).replaceAll("[^0-9.]", "").trim();
                    tfQty.setText(qtyStr.isEmpty() ? "1" : qtyStr);

                    cbKaryawan.setSelectedItem(str(tableModel.getValueAt(row, 5)));
                    tfTanggal.setText(str(tableModel.getValueAt(row, 6)));

                    String totStr = str(tableModel.getValueAt(row, 7)).replace("Rp ", "").replace(".", "").replace(",", ".").trim();
                    tfTotal.setText(totStr);

                    cbStatus.setSelectedItem(str(tableModel.getValueAt(row, 8)));

                    updateHargaDanTotal();
                }
            }
        });

        tableCard.add(createScrollPane(table), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        loadCombos();
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private void generateKode() {
        tfKode.setText("TRX-" + (System.currentTimeMillis() % 1000000));
    }

    private void updateHargaDanTotal() {
        if (cbLayanan.getSelectedItem() != null) {
            String name = cbLayanan.getSelectedItem().toString();
            Double harga = layananHargaMap.get(name);
            if (harga != null) {
                tfHarga.setText(String.format("Rp %,.0f", harga));
                hitungTotalOtomatis();
            }
        }
    }

    private void hitungTotalOtomatis() {
        try {
            if (cbLayanan.getSelectedItem() != null) {
                String name = cbLayanan.getSelectedItem().toString();
                Double harga = layananHargaMap.get(name);
                if (harga != null) {
                    double qty = Double.parseDouble(tfQty.getText().trim());
                    double total = qty * harga;
                    tfTotal.setText(String.format("%.0f", total));
                }
            }
        } catch (Exception ignored) {
            tfTotal.setText("0");
        }
    }

    private void loadCombos() {
        cbPelanggan.removeAllItems();
        pelangganMap.clear();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nama FROM pelanggan ORDER BY nama")) {
            while (rs.next()) {
                String name = rs.getString("nama");
                pelangganMap.put(name, rs.getInt("id"));
                cbPelanggan.addItem(name);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        cbKaryawan.removeAllItems();
        karyawanMap.clear();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nama FROM karyawan ORDER BY nama")) {
            while (rs.next()) {
                String name = rs.getString("nama");
                karyawanMap.put(name, rs.getInt("id"));
                cbKaryawan.addItem(name);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        cbLayanan.removeAllItems();
        layananMap.clear();
        layananHargaMap.clear();
        layananSatuanMap.clear();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nama, satuan, harga FROM layanan ORDER BY nama")) {
            while (rs.next()) {
                String name = rs.getString("nama");
                layananMap.put(name, rs.getInt("id"));
                layananHargaMap.put(name, rs.getDouble("harga"));
                layananSatuanMap.put(name, rs.getString("satuan"));
                cbLayanan.addItem(name);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        updateHargaDanTotal();
    }

    private void saveData() {
        if (cbPelanggan.getSelectedItem() == null || cbKaryawan.getSelectedItem() == null || cbLayanan.getSelectedItem() == null) {
            showError("Pilih pelanggan, karyawan, dan layanan laundry!");
            return;
        }

        double qty;
        try {
            qty = Double.parseDouble(tfQty.getText().trim());
            if (qty <= 0) { showError("Jumlah harus lebih dari 0!"); return; }
        } catch (NumberFormatException e) {
            showError("Format jumlah harus berupa angka!");
            return;
        }

        int pelId = pelangganMap.get(cbPelanggan.getSelectedItem().toString());
        int karId = karyawanMap.get(cbKaryawan.getSelectedItem().toString());
        String layName = cbLayanan.getSelectedItem().toString();
        int layId = layananMap.get(layName);
        double harga = layananHargaMap.get(layName);
        double total = qty * harga;

        try {
            conn.setAutoCommit(false);
            int newTrxId = -1;

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO transaksi(kode_transaksi, pelanggan_id, karyawan_id, tanggal, total, status) VALUES(?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, tfKode.getText().trim());
                ps.setInt(2, pelId);
                ps.setInt(3, karId);
                ps.setString(4, tfTanggal.getText().trim());
                ps.setDouble(5, total);
                ps.setString(6, cbStatus.getSelectedItem().toString());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) newTrxId = rs.getInt(1);
            }

            if (newTrxId != -1) {
                try (PreparedStatement psDet = conn.prepareStatement(
                        "INSERT INTO detail_transaksi(transaksi_id, layanan_id, qty, harga, subtotal) VALUES(?,?,?,?,?)")) {
                    psDet.setInt(1, newTrxId);
                    psDet.setInt(2, layId);
                    psDet.setDouble(3, qty);
                    psDet.setDouble(4, harga);
                    psDet.setDouble(5, total);
                    psDet.executeUpdate();
                }
            }

            conn.commit();
            conn.setAutoCommit(true);

            showSuccess("Transaksi " + tfKode.getText() + " berhasil disimpan!");
            clearForm();
            loadData();
        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            showError("Gagal menyimpan transaksi: " + e.getMessage());
        }
    }

    private void updateData() {
        if (selectedId == -1) {
            showError("Pilih transaksi pada tabel di bawah yang akan diupdate!");
            return;
        }
        if (cbPelanggan.getSelectedItem() == null || cbKaryawan.getSelectedItem() == null || cbLayanan.getSelectedItem() == null) {
            showError("Pilih pelanggan, karyawan, dan layanan!");
            return;
        }

        double qty;
        try {
            qty = Double.parseDouble(tfQty.getText().trim());
            if (qty <= 0) { showError("Jumlah harus lebih dari 0!"); return; }
        } catch (NumberFormatException e) {
            showError("Format jumlah harus berupa angka!");
            return;
        }

        int pelId = pelangganMap.get(cbPelanggan.getSelectedItem().toString());
        int karId = karyawanMap.get(cbKaryawan.getSelectedItem().toString());
        String layName = cbLayanan.getSelectedItem().toString();
        int layId = layananMap.get(layName);
        double harga = layananHargaMap.get(layName);
        double total = qty * harga;

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE transaksi SET pelanggan_id=?, karyawan_id=?, tanggal=?, total=?, status=? WHERE id=?")) {
                ps.setInt(1, pelId);
                ps.setInt(2, karId);
                ps.setString(3, tfTanggal.getText().trim());
                ps.setDouble(4, total);
                ps.setString(5, cbStatus.getSelectedItem().toString());
                ps.setInt(6, selectedId);
                ps.executeUpdate();
            }

            // Sync detail_transaksi
            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM detail_transaksi WHERE transaksi_id=?")) {
                psDel.setInt(1, selectedId);
                psDel.executeUpdate();
            }
            try (PreparedStatement psDet = conn.prepareStatement(
                    "INSERT INTO detail_transaksi(transaksi_id, layanan_id, qty, harga, subtotal) VALUES(?,?,?,?,?)")) {
                psDet.setInt(1, selectedId);
                psDet.setInt(2, layId);
                psDet.setDouble(3, qty);
                psDet.setDouble(4, harga);
                psDet.setDouble(5, total);
                psDet.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            showSuccess("Transaksi " + tfKode.getText() + " berhasil diupdate!");
            clearForm();
            loadData();
        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            showError("Gagal mengupdate transaksi: " + e.getMessage());
        }
    }

    private void deleteData() {
        if (selectedId == -1) {
            showError("Pilih transaksi yang akan dihapus!");
            return;
        }
        if (!confirmDelete()) return;

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM transaksi WHERE id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            showSuccess("Transaksi berhasil dihapus!");
            clearForm();
            loadData();
        } catch (SQLException e) {
            showError("Gagal menghapus transaksi: " + e.getMessage());
        }
    }

    private void clearForm() {
        selectedId = -1;
        generateKode();
        tfTanggal.setText(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        if (cbPelanggan.getItemCount() > 0) cbPelanggan.setSelectedIndex(0);
        if (cbKaryawan.getItemCount() > 0) cbKaryawan.setSelectedIndex(0);
        if (cbLayanan.getItemCount() > 0) cbLayanan.setSelectedIndex(0);
        tfQty.setText("1");
        cbStatus.setSelectedIndex(0);
        table.clearSelection();
        updateHargaDanTotal();
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
