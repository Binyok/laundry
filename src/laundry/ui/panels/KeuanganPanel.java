package laundry.ui.panels;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class KeuanganPanel extends BasePanel {

    private JLabel lblTotalPemasukan, lblTotalPengeluaran, lblSaldoKas;
    private JLabel tblTitle;

    // Filter mode: 1 = Output Pemasukan (default), 2 = Output Pengeluaran, 0 = Semua Hasil Output
    private int currentFilter = 1;
    private JButton btnFilterPemasukan, btnFilterPengeluaran, btnFilterSemua;
    private JTabbedPane formTabs;

    // Form Pemasukan (Textfields & Combos)
    private JComboBox<String> cbTransaksiPem, cbMetodePem;
    private JTextField tfTagihanPem, tfJumlahPem, tfTanggalPem;
    private Map<String, Integer> transaksiMap = new HashMap<>();
    private Map<String, Double> transaksiTotalMap = new HashMap<>();
    private int selectedPemasukanId = -1;

    // Form Pengeluaran (Textfields)
    private JTextField tfNamaBarangPeng, tfTempatBeliPeng, tfJumlahPeng, tfHargaSatuanPeng, tfTotalPeng, tfTanggalPeng;
    private int selectedPengeluaranId = -1;

    public KeuanganPanel() {
        super();
        init();
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // 1. MIDDLE SECTION: FORM PENCATATAN KEUANGAN (KOMPAK / RINGKAS)
        JPanel formCard = new JPanel(new BorderLayout(0, 8));
        formCard.setBackground(CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        JLabel titleForm = new JLabel("Form Pencatatan Keuangan");
        titleForm.setFont(FONT_TITLE);
        titleForm.setForeground(TEXT_PRIMARY);
        formCard.add(titleForm, BorderLayout.NORTH);

        formTabs = new JTabbedPane();
        formTabs.setFont(FONT_BUTTON);

        // --- Tab 1: Pemasukan Form ---
        JPanel tabPemasukan = new JPanel(new BorderLayout(0, 8));
        tabPemasukan.setOpaque(false);
        tabPemasukan.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel formPemGrid = new JPanel(new GridBagLayout());
        formPemGrid.setOpaque(false);
        GridBagConstraints gbcPem = new GridBagConstraints();
        gbcPem.insets = new Insets(3, 5, 3, 5);
        gbcPem.fill = GridBagConstraints.HORIZONTAL;

        cbTransaksiPem = createComboBox();
        cbTransaksiPem.setPreferredSize(new Dimension(240, 28));
        tfTagihanPem = createTextField();
        tfTagihanPem.setPreferredSize(new Dimension(110, 28));
        tfTagihanPem.setEditable(false);
        tfJumlahPem = createTextField();
        tfJumlahPem.setPreferredSize(new Dimension(110, 28));

        cbMetodePem = createComboBox();
        cbMetodePem.setPreferredSize(new Dimension(140, 28));
        cbMetodePem.addItem("Cash");
        cbMetodePem.addItem("Transfer Bank");
        cbMetodePem.addItem("QRIS / E-Wallet");
        cbMetodePem.addItem("Debit Card");

        tfTanggalPem = createTextField();
        tfTanggalPem.setPreferredSize(new Dimension(105, 28));
        tfTanggalPem.setText(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));

        gbcPem.gridx = 0; gbcPem.gridy = 0; formPemGrid.add(createLabel("Transaksi:"), gbcPem);
        gbcPem.gridx = 1; formPemGrid.add(cbTransaksiPem, gbcPem);
        gbcPem.gridx = 2; formPemGrid.add(createLabel("Tagihan:"), gbcPem);
        gbcPem.gridx = 3; formPemGrid.add(tfTagihanPem, gbcPem);
        gbcPem.gridx = 4; formPemGrid.add(createLabel("Jumlah Bayar:"), gbcPem);
        gbcPem.gridx = 5; formPemGrid.add(tfJumlahPem, gbcPem);

        gbcPem.gridx = 0; gbcPem.gridy = 1; formPemGrid.add(createLabel("Metode:"), gbcPem);
        gbcPem.gridx = 1; formPemGrid.add(cbMetodePem, gbcPem);
        gbcPem.gridx = 2; formPemGrid.add(createLabel("Tanggal:"), gbcPem);
        gbcPem.gridx = 3; formPemGrid.add(tfTanggalPem, gbcPem);

        JPanel btnPanelPem = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanelPem.setOpaque(false);
        JButton btnSavePem = createButton("Simpan", SUCCESS);
        btnSavePem.setPreferredSize(new Dimension(85, 30));
        JButton btnUpdatePem = createButton("Update", WARNING);
        btnUpdatePem.setPreferredSize(new Dimension(85, 30));
        JButton btnDeletePem = createButton("Hapus", DANGER);
        btnDeletePem.setPreferredSize(new Dimension(85, 30));
        JButton btnClearPem = createButton("Clear", new Color(100, 116, 139));
        btnClearPem.setPreferredSize(new Dimension(85, 30));

        btnSavePem.addActionListener(e -> savePemasukan());
        btnUpdatePem.addActionListener(e -> updatePemasukan());
        btnDeletePem.addActionListener(e -> deletePemasukan());
        btnClearPem.addActionListener(e -> clearPemasukanForm());

        btnPanelPem.add(btnSavePem);
        btnPanelPem.add(btnUpdatePem);
        btnPanelPem.add(btnDeletePem);
        btnPanelPem.add(btnClearPem);

        gbcPem.gridx = 4; gbcPem.gridy = 1; gbcPem.gridwidth = 2;
        formPemGrid.add(btnPanelPem, gbcPem);

        tabPemasukan.add(formPemGrid, BorderLayout.CENTER);
        formTabs.addTab("+ Pemasukan (Pembayaran Pelanggan)", tabPemasukan);

        // --- Tab 2: Pengeluaran Form ---
        JPanel tabPengeluaran = new JPanel(new BorderLayout(0, 8));
        tabPengeluaran.setOpaque(false);
        tabPengeluaran.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel formPengGrid = new JPanel(new GridBagLayout());
        formPengGrid.setOpaque(false);
        GridBagConstraints gbcPeng = new GridBagConstraints();
        gbcPeng.insets = new Insets(3, 5, 3, 5);
        gbcPeng.fill = GridBagConstraints.HORIZONTAL;

        tfNamaBarangPeng = createTextField();
        tfNamaBarangPeng.setPreferredSize(new Dimension(180, 28));
        tfTempatBeliPeng = createTextField();
        tfTempatBeliPeng.setPreferredSize(new Dimension(150, 28));
        tfJumlahPeng = createTextField();
        tfJumlahPeng.setPreferredSize(new Dimension(65, 28));
        tfHargaSatuanPeng = createTextField();
        tfHargaSatuanPeng.setPreferredSize(new Dimension(100, 28));
        tfTotalPeng = createTextField();
        tfTotalPeng.setPreferredSize(new Dimension(115, 28));
        tfTotalPeng.setEditable(false);
        tfTanggalPeng = createTextField();
        tfTanggalPeng.setPreferredSize(new Dimension(105, 28));
        tfTanggalPeng.setText(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));

        KeyAdapter calcAdapter = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calcPengeluaranTotal();
            }
        };
        tfJumlahPeng.addKeyListener(calcAdapter);
        tfHargaSatuanPeng.addKeyListener(calcAdapter);

        gbcPeng.gridx = 0; gbcPeng.gridy = 0; formPengGrid.add(createLabel("Nama Barang:"), gbcPeng);
        gbcPeng.gridx = 1; formPengGrid.add(tfNamaBarangPeng, gbcPeng);
        gbcPeng.gridx = 2; formPengGrid.add(createLabel("Tempat Beli:"), gbcPeng);
        gbcPeng.gridx = 3; formPengGrid.add(tfTempatBeliPeng, gbcPeng);
        gbcPeng.gridx = 4; formPengGrid.add(createLabel("Jumlah:"), gbcPeng);
        gbcPeng.gridx = 5; formPengGrid.add(tfJumlahPeng, gbcPeng);

        gbcPeng.gridx = 0; gbcPeng.gridy = 1; formPengGrid.add(createLabel("Harga Satuan:"), gbcPeng);
        gbcPeng.gridx = 1; formPengGrid.add(tfHargaSatuanPeng, gbcPeng);
        gbcPeng.gridx = 2; formPengGrid.add(createLabel("Total Biaya:"), gbcPeng);
        gbcPeng.gridx = 3; formPengGrid.add(tfTotalPeng, gbcPeng);
        gbcPeng.gridx = 4; formPengGrid.add(createLabel("Tanggal:"), gbcPeng);
        gbcPeng.gridx = 5; formPengGrid.add(tfTanggalPeng, gbcPeng);

        JPanel btnPanelPeng = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanelPeng.setOpaque(false);
        JButton btnSavePeng = createButton("Simpan", DANGER);
        btnSavePeng.setPreferredSize(new Dimension(85, 30));
        JButton btnUpdatePeng = createButton("Update", WARNING);
        btnUpdatePeng.setPreferredSize(new Dimension(85, 30));
        JButton btnDeletePeng = createButton("Hapus", DANGER);
        btnDeletePeng.setPreferredSize(new Dimension(85, 30));
        JButton btnClearPeng = createButton("Clear", new Color(100, 116, 139));
        btnClearPeng.setPreferredSize(new Dimension(85, 30));

        btnSavePeng.addActionListener(e -> savePengeluaran());
        btnUpdatePeng.addActionListener(e -> updatePengeluaran());
        btnDeletePeng.addActionListener(e -> deletePengeluaran());
        btnClearPeng.addActionListener(e -> clearPengeluaranForm());

        btnPanelPeng.add(btnSavePeng);
        btnPanelPeng.add(btnUpdatePeng);
        btnPanelPeng.add(btnDeletePeng);
        btnPanelPeng.add(btnClearPeng);

        gbcPeng.gridx = 0; gbcPeng.gridy = 2; gbcPeng.gridwidth = 6;
        formPengGrid.add(btnPanelPeng, gbcPeng);

        tabPengeluaran.add(formPengGrid, BorderLayout.CENTER);
        formTabs.addTab("- Pengeluaran (Pembelian & Operasional)", tabPengeluaran);

        formTabs.addChangeListener(e -> {
            int selectedTab = formTabs.getSelectedIndex();
            if (selectedTab == 0) {
                setFilter(1);
            } else if (selectedTab == 1) {
                setFilter(2);
            }
        });

        formCard.add(formTabs, BorderLayout.CENTER);
        add(formCard, BorderLayout.NORTH);

        // 3. BOTTOM SECTION: TABEL OUTPUT HASIL PENGISIAN FORM PENCATATAN KEUANGAN
        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout(0, 12));

        JPanel filterBar = new JPanel(new BorderLayout());
        filterBar.setOpaque(false);

        tblTitle = new JLabel("Tabel Output Hasil Pengisian Form Pencatatan Keuangan");
        tblTitle.setFont(FONT_TITLE);
        tblTitle.setForeground(TEXT_PRIMARY);
        filterBar.add(tblTitle, BorderLayout.WEST);

        JPanel filterBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterBtns.setOpaque(false);

        btnFilterPemasukan = createFilterButton("Output Pemasukan", true);
        btnFilterPengeluaran = createFilterButton("Output Pengeluaran", false);
        btnFilterSemua = createFilterButton("Semua Hasil Output", false);

        btnFilterPemasukan.addActionListener(e -> {
            formTabs.setSelectedIndex(0);
            setFilter(1);
        });
        btnFilterPengeluaran.addActionListener(e -> {
            formTabs.setSelectedIndex(1);
            setFilter(2);
        });
        btnFilterSemua.addActionListener(e -> setFilter(0));

        filterBtns.add(btnFilterPemasukan);
        filterBtns.add(btnFilterPengeluaran);
        filterBtns.add(btnFilterSemua);
        filterBar.add(filterBtns, BorderLayout.EAST);

        tableCard.add(filterBar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        table = createStyledTable(tableModel);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleTableClick();
            }
        });

        tableCard.add(createScrollPane(table), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        cbTransaksiPem.addActionListener(e -> {
            if (cbTransaksiPem.getSelectedItem() != null) {
                Double tagihan = transaksiTotalMap.get(cbTransaksiPem.getSelectedItem().toString());
                if (tagihan != null) {
                    tfTagihanPem.setText(String.format("Rp %,.0f", tagihan));
                    tfJumlahPem.setText(String.format("%.0f", tagihan));
                }
            }
        });

        loadTransaksiCombos();
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accentColor, String icon) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        iconLabel.setForeground(accentColor);
        iconLabel.setPreferredSize(new Dimension(46, 46));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(iconLabel, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(TEXT_SECONDARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(titleLbl);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(valueLabel);

        JPanel indicator = new JPanel();
        indicator.setBackground(accentColor);
        indicator.setPreferredSize(new Dimension(4, 0));

        card.add(indicator, BorderLayout.EAST);
        card.add(info, BorderLayout.CENTER);

        return card;
    }

    private JButton createFilterButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 32));
        updateFilterButtonStyle(btn, active);
        return btn;
    }

    private void updateFilterButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(TEXT_PRIMARY);
            btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        }
    }

    private void setFilter(int filter) {
        currentFilter = filter;
        updateFilterButtonStyle(btnFilterSemua, filter == 0);
        updateFilterButtonStyle(btnFilterPemasukan, filter == 1);
        updateFilterButtonStyle(btnFilterPengeluaran, filter == 2);

        if (filter == 1) {
            tblTitle.setText("Tabel Output Data Pemasukan (Pembayaran Pelanggan)");
        } else if (filter == 2) {
            tblTitle.setText("Tabel Output Data Pengeluaran (Pembelian & Operasional)");
        } else {
            tblTitle.setText("Tabel Output Hasil Pengisian Form Pencatatan Keuangan (Semua)");
        }

        loadData();
    }

    private void calcPengeluaranTotal() {
        try {
            int qty = Integer.parseInt(tfJumlahPeng.getText().trim());
            double harga = Double.parseDouble(tfHargaSatuanPeng.getText().trim());
            tfTotalPeng.setText(String.format("%.0f", qty * harga));
        } catch (Exception ignored) {
            tfTotalPeng.setText("0");
        }
    }

    private void loadTransaksiCombos() {
        cbTransaksiPem.removeAllItems();
        transaksiMap.clear();
        transaksiTotalMap.clear();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT t.id, t.kode_transaksi, t.total, COALESCE(p.nama, '-') " +
                "FROM transaksi t " +
                "LEFT JOIN pelanggan p ON t.pelanggan_id = p.id " +
                "ORDER BY t.id DESC")) {
            while (rs.next()) {
                String label = rs.getString(2) + " (" + rs.getString(4) + ")";
                transaksiMap.put(label, rs.getInt(1));
                transaksiTotalMap.put(label, rs.getDouble(3));
                cbTransaksiPem.addItem(label);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        if (cbTransaksiPem.getItemCount() > 0) {
            cbTransaksiPem.setSelectedIndex(0);
            Double tagihan = transaksiTotalMap.get(cbTransaksiPem.getSelectedItem().toString());
            if (tagihan != null) {
                tfTagihanPem.setText(String.format("Rp %,.0f", tagihan));
                tfJumlahPem.setText(String.format("%.0f", tagihan));
            }
        }
    }

    private void savePemasukan() {
        if (cbTransaksiPem.getSelectedItem() == null || tfJumlahPem.getText().trim().isEmpty()) {
            showError("Pilih transaksi dan masukkan jumlah bayar!");
            return;
        }
        double jumlah;
        try {
            jumlah = Double.parseDouble(tfJumlahPem.getText().trim());
            if (jumlah <= 0) { showError("Jumlah bayar harus lebih dari 0!"); return; }
        } catch (NumberFormatException e) {
            showError("Format jumlah bayar salah!");
            return;
        }

        int trxId = transaksiMap.get(cbTransaksiPem.getSelectedItem().toString());

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO pembayaran(transaksi_id, jumlah_bayar, metode, tanggal) VALUES(?,?,?,?)")) {
            ps.setInt(1, trxId);
            ps.setDouble(2, jumlah);
            ps.setString(3, cbMetodePem.getSelectedItem().toString());
            ps.setString(4, tfTanggalPem.getText().trim());
            ps.executeUpdate();
            showSuccess("Data pembayaran berhasil disimpan dan masuk ke tabel output!");
            clearPemasukanForm();
            loadData();
        } catch (SQLException e) {
            showError("Gagal mencatat pembayaran: " + e.getMessage());
        }
    }

    private void updatePemasukan() {
        if (selectedPemasukanId == -1) {
            showError("Pilih data pembayaran pada tabel yang akan diupdate!");
            return;
        }
        if (cbTransaksiPem.getSelectedItem() == null || tfJumlahPem.getText().trim().isEmpty()) {
            showError("Pilih transaksi dan masukkan jumlah bayar!");
            return;
        }
        double jumlah;
        try {
            jumlah = Double.parseDouble(tfJumlahPem.getText().trim());
            if (jumlah <= 0) { showError("Jumlah bayar harus lebih dari 0!"); return; }
        } catch (NumberFormatException e) {
            showError("Format jumlah bayar salah!");
            return;
        }

        int trxId = transaksiMap.get(cbTransaksiPem.getSelectedItem().toString());

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE pembayaran SET transaksi_id=?, jumlah_bayar=?, metode=?, tanggal=? WHERE id=?")) {
            ps.setInt(1, trxId);
            ps.setDouble(2, jumlah);
            ps.setString(3, cbMetodePem.getSelectedItem().toString());
            ps.setString(4, tfTanggalPem.getText().trim());
            ps.setInt(5, selectedPemasukanId);
            ps.executeUpdate();
            showSuccess("Data pembayaran berhasil diupdate!");
            clearPemasukanForm();
            loadData();
        } catch (SQLException e) {
            showError("Gagal mengupdate pembayaran: " + e.getMessage());
        }
    }

    private void deletePemasukan() {
        if (selectedPemasukanId == -1) {
            showError("Pilih data pembayaran pada tabel yang ingin dihapus!");
            return;
        }
        if (!confirmDelete()) return;
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM pembayaran WHERE id=?")) {
            ps.setInt(1, selectedPemasukanId);
            ps.executeUpdate();
            showSuccess("Data pembayaran berhasil dihapus!");
            clearPemasukanForm();
            loadData();
        } catch (SQLException e) {
            showError("Gagal menghapus pembayaran: " + e.getMessage());
        }
    }

    private void clearPemasukanForm() {
        selectedPemasukanId = -1;
        tfTanggalPem.setText(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        table.clearSelection();
        loadTransaksiCombos();
    }

    private void savePengeluaran() {
        if (tfNamaBarangPeng.getText().trim().isEmpty() || tfJumlahPeng.getText().trim().isEmpty() || tfHargaSatuanPeng.getText().trim().isEmpty()) {
            showError("Nama barang, jumlah, dan harga satuan harus diisi!");
            return;
        }
        int qty; double harga, total;
        try {
            qty = Integer.parseInt(tfJumlahPeng.getText().trim());
            harga = Double.parseDouble(tfHargaSatuanPeng.getText().trim());
            total = qty * harga;
            if (qty <= 0 || harga <= 0) { showError("Jumlah dan harga harus lebih dari 0!"); return; }
        } catch (NumberFormatException e) {
            showError("Format angka salah!");
            return;
        }

        String tempat = tfTempatBeliPeng.getText().trim();
        if (tempat.isEmpty()) tempat = "-";

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO pembelian(nama_barang, tempat_beli, jumlah, harga_satuan, total, tanggal) VALUES(?,?,?,?,?,?)")) {
            ps.setString(1, tfNamaBarangPeng.getText().trim());
            ps.setString(2, tempat);
            ps.setInt(3, qty);
            ps.setDouble(4, harga);
            ps.setDouble(5, total);
            ps.setString(6, tfTanggalPeng.getText().trim());
            ps.executeUpdate();
            showSuccess("Data pengeluaran berhasil disimpan dan masuk ke tabel output!");
            clearPengeluaranForm();
            loadData();
        } catch (SQLException e) {
            showError("Gagal menyimpan pengeluaran: " + e.getMessage());
        }
    }

    private void updatePengeluaran() {
        if (selectedPengeluaranId == -1) {
            showError("Pilih data pengeluaran pada tabel yang akan diupdate!");
            return;
        }
        if (tfNamaBarangPeng.getText().trim().isEmpty() || tfJumlahPeng.getText().trim().isEmpty() || tfHargaSatuanPeng.getText().trim().isEmpty()) {
            showError("Nama barang, jumlah, dan harga satuan harus diisi!");
            return;
        }
        int qty; double harga, total;
        try {
            qty = Integer.parseInt(tfJumlahPeng.getText().trim());
            harga = Double.parseDouble(tfHargaSatuanPeng.getText().trim());
            total = qty * harga;
            if (qty <= 0 || harga <= 0) { showError("Jumlah dan harga harus lebih dari 0!"); return; }
        } catch (NumberFormatException e) {
            showError("Format angka salah!");
            return;
        }

        String tempat = tfTempatBeliPeng.getText().trim();
        if (tempat.isEmpty()) tempat = "-";

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE pembelian SET nama_barang=?, tempat_beli=?, jumlah=?, harga_satuan=?, total=?, tanggal=? WHERE id=?")) {
            ps.setString(1, tfNamaBarangPeng.getText().trim());
            ps.setString(2, tempat);
            ps.setInt(3, qty);
            ps.setDouble(4, harga);
            ps.setDouble(5, total);
            ps.setString(6, tfTanggalPeng.getText().trim());
            ps.setInt(7, selectedPengeluaranId);
            ps.executeUpdate();
            showSuccess("Data pengeluaran berhasil diupdate!");
            clearPengeluaranForm();
            loadData();
        } catch (SQLException e) {
            showError("Gagal mengupdate pengeluaran: " + e.getMessage());
        }
    }

    private void deletePengeluaran() {
        if (selectedPengeluaranId == -1) {
            showError("Pilih data pengeluaran yang akan dihapus!");
            return;
        }
        if (!confirmDelete()) return;
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM pembelian WHERE id=?")) {
            ps.setInt(1, selectedPengeluaranId);
            ps.executeUpdate();
            showSuccess("Data pengeluaran berhasil dihapus!");
            clearPengeluaranForm();
            loadData();
        } catch (SQLException e) {
            showError("Gagal menghapus pengeluaran: " + e.getMessage());
        }
    }

    private void clearPengeluaranForm() {
        selectedPengeluaranId = -1;
        tfNamaBarangPeng.setText("");
        tfTempatBeliPeng.setText("");
        tfJumlahPeng.setText("");
        tfHargaSatuanPeng.setText("");
        tfTotalPeng.setText("");
        tfTanggalPeng.setText(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        table.clearSelection();
    }

    private void handleTableClick() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        if (currentFilter == 1) { // Output Pemasukan
            selectedPemasukanId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            String kodeTrx = str(tableModel.getValueAt(row, 1));
            for (int i = 0; i < cbTransaksiPem.getItemCount(); i++) {
                if (cbTransaksiPem.getItemAt(i).startsWith(kodeTrx)) {
                    cbTransaksiPem.setSelectedIndex(i);
                    break;
                }
            }
            tfTanggalPem.setText(str(tableModel.getValueAt(row, 3)));
            String jm = str(tableModel.getValueAt(row, 5)).replace("Rp ", "").replace(".", "").replace(",", ".").trim();
            tfJumlahPem.setText(jm);
            cbMetodePem.setSelectedItem(str(tableModel.getValueAt(row, 6)));
            formTabs.setSelectedIndex(0);

        } else if (currentFilter == 2) { // Output Pengeluaran
            selectedPengeluaranId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            tfNamaBarangPeng.setText(str(tableModel.getValueAt(row, 1)));
            tfTempatBeliPeng.setText(str(tableModel.getValueAt(row, 2)));
            tfJumlahPeng.setText(str(tableModel.getValueAt(row, 3)));
            String hs = str(tableModel.getValueAt(row, 4)).replace("Rp ", "").replace(".", "").replace(",", ".").trim();
            tfHargaSatuanPeng.setText(hs);
            String tot = str(tableModel.getValueAt(row, 5)).replace("Rp ", "").replace(".", "").replace(",", ".").trim();
            tfTotalPeng.setText(tot);
            tfTanggalPeng.setText(str(tableModel.getValueAt(row, 6)));
            formTabs.setSelectedIndex(1);

        } else if (currentFilter == 0) { // Semua Hasil Output
            String jenis = str(tableModel.getValueAt(row, 2));
            String tgl = str(tableModel.getValueAt(row, 1));
            String namaItem = str(tableModel.getValueAt(row, 3));
            String pihakTempat = str(tableModel.getValueAt(row, 4));

            if (jenis.equalsIgnoreCase("Pemasukan")) {
                formTabs.setSelectedIndex(0);
                tfTanggalPem.setText(tgl);
                for (int i = 0; i < cbTransaksiPem.getItemCount(); i++) {
                    if (cbTransaksiPem.getItemAt(i).contains(namaItem)) {
                        cbTransaksiPem.setSelectedIndex(i);
                        break;
                    }
                }
                String nom = str(tableModel.getValueAt(row, 7)).replace("Rp ", "").replace(".", "").replace(",", ".").trim();
                tfJumlahPem.setText(nom);

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT pb.id, pb.metode FROM pembayaran pb " +
                        "JOIN transaksi t ON pb.transaksi_id = t.id " +
                        "WHERE t.kode_transaksi=? AND pb.tanggal=? LIMIT 1")) {
                    ps.setString(1, namaItem);
                    ps.setString(2, tgl);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        selectedPemasukanId = rs.getInt(1);
                        cbMetodePem.setSelectedItem(rs.getString(2));
                    }
                } catch (SQLException ignored) {}

            } else { // Pengeluaran
                formTabs.setSelectedIndex(1);
                tfNamaBarangPeng.setText(namaItem);
                tfTempatBeliPeng.setText(pihakTempat);
                tfTanggalPeng.setText(tgl);
                String totalNom = str(tableModel.getValueAt(row, 7)).replace("Rp ", "").replace(".", "").replace(",", ".").trim();
                tfTotalPeng.setText(totalNom);

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, jumlah, harga_satuan FROM pembelian WHERE nama_barang=? AND tanggal=? LIMIT 1")) {
                    ps.setString(1, namaItem);
                    ps.setString(2, tgl);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        selectedPengeluaranId = rs.getInt(1);
                        tfJumlahPeng.setText(String.valueOf(rs.getInt(2)));
                        tfHargaSatuanPeng.setText(String.format("%.0f", rs.getDouble(3)));
                    }
                } catch (SQLException ignored) {}
            }
        }
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }

    @Override
    protected void loadData() {
        double totalPemasukan = 0;
        double totalPengeluaran = 0;

        // Calculate Totals
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(jumlah_bayar),0) FROM pembayaran")) {
            if (rs.next()) totalPemasukan = rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(total),0) FROM pembelian")) {
            if (rs.next()) totalPengeluaran = rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }

        double saldoKas = totalPemasukan - totalPengeluaran;

        // Load Table Data based on filter
        tableModel.setRowCount(0);

        if (currentFilter == 1) { // 1. OUTPUT PEMASUKAN (Hasil pengisian form pemasukan)
            tableModel.setDataVector(new Object[][]{}, new String[]{
                "ID", "Kode Transaksi", "Pelanggan", "Tanggal Pembayaran", "Harga per Kg / Satuan (Rp)", "Total Tagihan (Rp)", "Metode Pembayaran", "Status"
            });

            String sql =
                "SELECT pb.id, t.kode_transaksi, COALESCE(p.nama, '-'), pb.tanggal, " +
                "COALESCE((SELECT dt.harga FROM detail_transaksi dt WHERE dt.transaksi_id = t.id LIMIT 1), 0) AS harga_satuan, " +
                "t.total, pb.metode, t.status " +
                "FROM pembayaran pb " +
                "JOIN transaksi t ON pb.transaksi_id = t.id " +
                "LEFT JOIN pelanggan p ON t.pelanggan_id = p.id " +
                "ORDER BY pb.tanggal DESC, pb.id DESC";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        String.format("Rp %,.0f", rs.getDouble(5)),
                        String.format("Rp %,.0f", rs.getDouble(6)),
                        rs.getString(7),
                        rs.getString(8)
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }

        } else if (currentFilter == 2) { // 2. OUTPUT PENGELUARAN (Hasil pengisian form pengeluaran)
            tableModel.setDataVector(new Object[][]{}, new String[]{
                "ID", "Nama Barang / Kebutuhan", "Tempat Beli / Keterangan", "Jumlah (Qty)", "Harga Satuan (Rp)", "Total Biaya (Rp)", "Tanggal"
            });

            String sql =
                "SELECT id, nama_barang, COALESCE(tempat_beli, '-'), jumlah, harga_satuan, total, tanggal " +
                "FROM pembelian ORDER BY tanggal DESC, id DESC";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getInt(4) + " item",
                        String.format("Rp %,.0f", rs.getDouble(5)),
                        String.format("Rp %,.0f", rs.getDouble(6)),
                        rs.getString(7)
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }

        } else if (currentFilter == 0) { // 3. SEMUA HASIL OUTPUT FORM
            tableModel.setDataVector(new Object[][]{}, new String[]{
                "No", "Tanggal", "Jenis Form", "Nama Barang / Transaksi", "Tempat Beli / Pelanggan", "Jumlah / Qty", "Harga Satuan (Rp)", "Total Biaya / Bayar (Rp)", "Metode / Info"
            });

            String sqlSemua =
                "SELECT pb.id, pb.tanggal, 'Pemasukan' AS jenis, " +
                "t.kode_transaksi AS item_name, " +
                "COALESCE(p.nama, '-') AS pihak_name, " +
                "'1 Trx' AS qty_info, " +
                "pb.jumlah_bayar AS harga_sat, " +
                "pb.jumlah_bayar AS total_nom, " +
                "pb.metode AS info_det " +
                "FROM pembayaran pb " +
                "LEFT JOIN transaksi t ON pb.transaksi_id = t.id " +
                "LEFT JOIN pelanggan p ON t.pelanggan_id = p.id " +
                "UNION ALL " +
                "SELECT pm.id, pm.tanggal, 'Pengeluaran' AS jenis, " +
                "pm.nama_barang AS item_name, " +
                "COALESCE(pm.tempat_beli, '-') AS pihak_name, " +
                "CONCAT(pm.jumlah, ' item') AS qty_info, " +
                "pm.harga_satuan AS harga_sat, " +
                "pm.total AS total_nom, " +
                "'-' AS info_det " +
                "FROM pembelian pm " +
                "ORDER BY tanggal DESC, id DESC";

            int no = 1;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlSemua)) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        no++,
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        String.format("Rp %,.0f", rs.getDouble(7)),
                        String.format("Rp %,.0f", rs.getDouble(8)),
                        rs.getString(9)
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
