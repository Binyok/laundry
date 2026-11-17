package laundry.ui.panels;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class DashboardPanel extends BasePanel {

    private JLabel lblPelanggan, lblTransaksi, lblLabaKotor, lblKaryawan;
    private JLabel lblLayanan, lblLabaBersih;

    public DashboardPanel() {
        super();
        init();
    }

    @Override
    protected void initComponents() {
        JPanel cardsRow1 = new JPanel(new GridLayout(1, 3, 16, 0));
        cardsRow1.setOpaque(false);

        lblPelanggan = new JLabel("0");
        lblTransaksi = new JLabel("0");
        lblLabaKotor = new JLabel("Rp 0");
        lblKaryawan = new JLabel("0");
        lblLayanan = new JLabel("0");
        lblLabaBersih = new JLabel("Rp 0");

        cardsRow1.add(createStatCard("Total Pelanggan", lblPelanggan, new Color(59, 130, 246), "\u263A"));
        cardsRow1.add(createStatCard("Total Transaksi", lblTransaksi, new Color(16, 185, 129), "\u2616"));
        cardsRow1.add(createStatCard("Laba Kotor", lblLabaKotor, new Color(245, 158, 11), "\u25C6"));

        JPanel cardsRow2 = new JPanel(new GridLayout(1, 3, 16, 0));
        cardsRow2.setOpaque(false);

        cardsRow2.add(createStatCard("Karyawan", lblKaryawan, new Color(139, 92, 246), "\u2699"));
        cardsRow2.add(createStatCard("Layanan", lblLayanan, new Color(236, 72, 153), "\u2606"));
        cardsRow2.add(createStatCard("Laba Bersih", lblLabaBersih, new Color(20, 184, 166), "\u2605"));

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.add(cardsRow1);
        topSection.add(Box.createVerticalStrut(16));
        topSection.add(cardsRow2);

        add(topSection, BorderLayout.NORTH);

        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout(0, 12));

        JLabel tblTitle = new JLabel("Transaksi Terbaru");
        tblTitle.setFont(FONT_TITLE);
        tblTitle.setForeground(TEXT_PRIMARY);
        tableCard.add(tblTitle, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{
            "Kode", "Pelanggan", "Tanggal", "Total", "Status"
        }, 0);
        table = createStyledTable(tableModel);
        tableCard.add(createScrollPane(table), BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor, String icon) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        iconLabel.setForeground(accentColor);
        iconLabel.setPreferredSize(new Dimension(50, 50));
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

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
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

    @Override
    protected void loadData() {
        try {
            loadCount("SELECT COUNT(*) FROM pelanggan", lblPelanggan);
            loadCount("SELECT COUNT(*) FROM transaksi", lblTransaksi);
            loadCount("SELECT COUNT(*) FROM karyawan", lblKaryawan);
            loadCount("SELECT COUNT(*) FROM layanan", lblLayanan);

            double totalPemasukan = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(jumlah_bayar),0) FROM pembayaran")) {
                if (rs.next()) {
                    totalPemasukan = rs.getDouble(1);
                    lblLabaKotor.setText(String.format("Rp %,.0f", totalPemasukan));
                }
            }

            double totalPengeluaran = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(total),0) FROM pembelian")) {
                if (rs.next()) {
                    totalPengeluaran = rs.getDouble(1);
                }
            }

            double labaBersih = totalPemasukan - totalPengeluaran;
            lblLabaBersih.setText(String.format("Rp %,.0f", labaBersih));

            tableModel.setRowCount(0);
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                    "SELECT t.kode_transaksi, COALESCE(p.nama,'-'), t.tanggal, t.total, t.status " +
                    "FROM transaksi t LEFT JOIN pelanggan p ON t.pelanggan_id = p.id " +
                    "ORDER BY t.id DESC LIMIT 10")) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getString(1), rs.getString(2), rs.getString(3),
                        String.format("Rp %,.0f", rs.getDouble(4)), rs.getString(5)
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCount(String sql, JLabel label) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                label.setText(String.valueOf(rs.getInt(1)));
            }
        }
    }

    public void refreshData() {
        loadData();
    }
}
