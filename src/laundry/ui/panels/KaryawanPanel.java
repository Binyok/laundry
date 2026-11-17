package laundry.ui.panels;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class KaryawanPanel extends BasePanel {

    private JTextField tfNama, tfJabatan, tfTelepon, tfAlamat, tfGaji;
    private int selectedId = -1;

    public KaryawanPanel() {
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
        tfJabatan = createTextField();
        tfTelepon = createTextField();
        tfAlamat = createTextField();
        tfGaji = createTextField();

        gbc.gridx = 0; gbc.gridy = 0; formCard.add(createLabel("Nama"), gbc);
        gbc.gridx = 1; formCard.add(tfNama, gbc);
        gbc.gridx = 2; formCard.add(createLabel("Jabatan"), gbc);
        gbc.gridx = 3; formCard.add(tfJabatan, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formCard.add(createLabel("Telepon"), gbc);
        gbc.gridx = 1; formCard.add(tfTelepon, gbc);
        gbc.gridx = 2; formCard.add(createLabel("Alamat"), gbc);
        gbc.gridx = 3; formCard.add(tfAlamat, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formCard.add(createLabel("Gaji (Rp)"), gbc);
        gbc.gridx = 1; formCard.add(tfGaji, gbc);

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

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        formCard.add(btnPanel, gbc);

        add(formCard, BorderLayout.NORTH);

        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "Jabatan", "Telepon", "Alamat", "Gaji"}, 0);
        table = createStyledTable(tableModel);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    selectedId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                    tfNama.setText(str(tableModel.getValueAt(row, 1)));
                    tfJabatan.setText(str(tableModel.getValueAt(row, 2)));
                    tfTelepon.setText(str(tableModel.getValueAt(row, 3)));
                    tfAlamat.setText(str(tableModel.getValueAt(row, 4)));
                    String gajiStr = str(tableModel.getValueAt(row, 5)).replace("Rp ", "").replace(".", "").replace(",", ".");
                    tfGaji.setText(gajiStr);
                }
            }
        });

        tableCard.add(createScrollPane(table), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }

    private void saveData() {
        if (tfNama.getText().trim().isEmpty()) { showError("Nama harus diisi!"); return; }
        double gaji = 0;
        try { if (!tfGaji.getText().trim().isEmpty()) gaji = Double.parseDouble(tfGaji.getText().trim()); } catch (NumberFormatException e) { showError("Format gaji salah!"); return; }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO karyawan(nama,jabatan,telepon,alamat,gaji) VALUES(?,?,?,?,?)")) {
            ps.setString(1, tfNama.getText().trim());
            ps.setString(2, tfJabatan.getText().trim());
            ps.setString(3, tfTelepon.getText().trim());
            ps.setString(4, tfAlamat.getText().trim());
            ps.setDouble(5, gaji);
            ps.executeUpdate();
            showSuccess("Data karyawan berhasil disimpan!");
            clearForm();
            loadData();
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void updateData() {
        if (selectedId == -1) { showError("Pilih data yang akan diupdate!"); return; }
        if (tfNama.getText().trim().isEmpty()) { showError("Nama harus diisi!"); return; }
        double gaji = 0;
        try { if (!tfGaji.getText().trim().isEmpty()) gaji = Double.parseDouble(tfGaji.getText().trim()); } catch (NumberFormatException e) { showError("Format gaji salah!"); return; }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE karyawan SET nama=?,jabatan=?,telepon=?,alamat=?,gaji=? WHERE id=?")) {
            ps.setString(1, tfNama.getText().trim());
            ps.setString(2, tfJabatan.getText().trim());
            ps.setString(3, tfTelepon.getText().trim());
            ps.setString(4, tfAlamat.getText().trim());
            ps.setDouble(5, gaji);
            ps.setInt(6, selectedId);
            ps.executeUpdate();
            showSuccess("Data karyawan berhasil diupdate!");
            clearForm();
            loadData();
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void deleteData() {
        if (selectedId == -1) { showError("Pilih data yang akan dihapus!"); return; }
        if (!confirmDelete()) return;
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM karyawan WHERE id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            showSuccess("Data karyawan berhasil dihapus!");
            clearForm();
            loadData();
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void clearForm() {
        selectedId = -1;
        tfNama.setText("");
        tfJabatan.setText("");
        tfTelepon.setText("");
        tfAlamat.setText("");
        tfGaji.setText("");
        table.clearSelection();
    }

    @Override
    protected void loadData() {
        tableModel.setRowCount(0);
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM karyawan ORDER BY id DESC")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("nama"), rs.getString("jabatan"),
                    rs.getString("telepon"), rs.getString("alamat"),
                    String.format("Rp %,.0f", rs.getDouble("gaji"))
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
