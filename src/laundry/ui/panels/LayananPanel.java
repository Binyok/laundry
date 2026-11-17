package laundry.ui.panels;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class LayananPanel extends BasePanel {

    private JTextField tfNama, tfSatuan, tfHarga, tfEstimasi;
    private int selectedId = -1;

    public LayananPanel() {
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
        tfSatuan = createTextField();
        tfHarga = createTextField();
        tfEstimasi = createTextField();

        gbc.gridx = 0; gbc.gridy = 0; formCard.add(createLabel("Nama Layanan"), gbc);
        gbc.gridx = 1; formCard.add(tfNama, gbc);
        gbc.gridx = 2; formCard.add(createLabel("Satuan (Kg/Pcs)"), gbc);
        gbc.gridx = 3; formCard.add(tfSatuan, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formCard.add(createLabel("Harga (Rp)"), gbc);
        gbc.gridx = 1; formCard.add(tfHarga, gbc);
        gbc.gridx = 2; formCard.add(createLabel("Estimasi (Hari)"), gbc);
        gbc.gridx = 3; formCard.add(tfEstimasi, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);

        JButton btnSave = createButton("Simpan", ACCENT);
        JButton btnUpdate = createButton("Update", WARNING);
        JButton btnDelete = createButton("Hapus", DANGER);
        JButton btnClear = createButton("Clear", new Color(100, 116, 139));

        btnSave.addActionListener(e -> saveData());
        btnUpdate.addActionListener(e -> updateData());
        btnDelete.addActionListener(e -> deleteData());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnSave);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        formCard.add(btnPanel, gbc);

        add(formCard, BorderLayout.NORTH);

        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Nama Layanan", "Satuan", "Harga", "Estimasi (Hari)"}, 0);
        table = createStyledTable(tableModel);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    selectedId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                    tfNama.setText(str(tableModel.getValueAt(row, 1)));
                    tfSatuan.setText(str(tableModel.getValueAt(row, 2)));
                    String hargaStr = str(tableModel.getValueAt(row, 3)).replace("Rp ", "").replace(".", "").replace(",", ".");
                    tfHarga.setText(hargaStr);
                    tfEstimasi.setText(str(tableModel.getValueAt(row, 4)));
                }
            }
        });

        tableCard.add(createScrollPane(table), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }

    private void saveData() {
        if (tfNama.getText().trim().isEmpty() || tfHarga.getText().trim().isEmpty()) {
            showError("Nama dan harga harus diisi!"); return;
        }
        double harga; int estimasi = 1;
        try {
            harga = Double.parseDouble(tfHarga.getText().trim());
            if (!tfEstimasi.getText().trim().isEmpty()) estimasi = Integer.parseInt(tfEstimasi.getText().trim());
        } catch (NumberFormatException e) { showError("Format angka salah!"); return; }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO layanan(nama,satuan,harga,estimasi_hari) VALUES(?,?,?,?)")) {
            ps.setString(1, tfNama.getText().trim());
            ps.setString(2, tfSatuan.getText().trim());
            ps.setDouble(3, harga);
            ps.setInt(4, estimasi);
            ps.executeUpdate();
            showSuccess("Data layanan berhasil disimpan!");
            clearForm();
            loadData();
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void updateData() {
        if (selectedId == -1) { showError("Pilih data yang akan diupdate!"); return; }
        if (tfNama.getText().trim().isEmpty() || tfHarga.getText().trim().isEmpty()) {
            showError("Nama dan harga harus diisi!"); return;
        }
        double harga; int estimasi = 1;
        try {
            harga = Double.parseDouble(tfHarga.getText().trim());
            if (!tfEstimasi.getText().trim().isEmpty()) estimasi = Integer.parseInt(tfEstimasi.getText().trim());
        } catch (NumberFormatException e) { showError("Format angka salah!"); return; }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE layanan SET nama=?,satuan=?,harga=?,estimasi_hari=? WHERE id=?")) {
            ps.setString(1, tfNama.getText().trim());
            ps.setString(2, tfSatuan.getText().trim());
            ps.setDouble(3, harga);
            ps.setInt(4, estimasi);
            ps.setInt(5, selectedId);
            ps.executeUpdate();
            showSuccess("Data layanan berhasil diupdate!");
            clearForm();
            loadData();
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void deleteData() {
        if (selectedId == -1) { showError("Pilih data yang akan dihapus!"); return; }
        if (!confirmDelete()) return;
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM layanan WHERE id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            showSuccess("Data layanan berhasil dihapus!");
            clearForm();
            loadData();
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void clearForm() {
        selectedId = -1;
        tfNama.setText("");
        tfSatuan.setText("");
        tfHarga.setText("");
        tfEstimasi.setText("");
        table.clearSelection();
    }

    @Override
    protected void loadData() {
        tableModel.setRowCount(0);
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM layanan ORDER BY id DESC")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("nama"), rs.getString("satuan"),
                    String.format("Rp %,.0f", rs.getDouble("harga")), rs.getInt("estimasi_hari")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
