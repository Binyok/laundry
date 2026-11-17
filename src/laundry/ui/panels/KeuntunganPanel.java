package laundry.ui.panels;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class KeuntunganPanel extends BasePanel {

    private JLabel lblLabaKotor, lblTotalPengeluaran, lblLabaBersih, lblMarginKeuntungan;
    private JLabel lblFormulaPemasukan, lblFormulaPengeluaran, lblFormulaLabaBersih, lblStatusFinansial;
    private JLabel lblRataRataTransaksi;

    public KeuntunganPanel() {
        super();
        init();
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout(0, 16));

        // 1. TOP SECTION: 4 KARTU STATISTIK KEUNTUNGAN
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        statsPanel.setOpaque(false);

        lblLabaKotor = new JLabel("Rp 0");
        lblTotalPengeluaran = new JLabel("Rp 0");
        lblLabaBersih = new JLabel("Rp 0");
        lblMarginKeuntungan = new JLabel("0.0%");

        statsPanel.add(createStatCard("Laba Kotor (Omzet)", lblLabaKotor, new Color(59, 130, 246), "\u25C6"));
        statsPanel.add(createStatCard("Total Pengeluaran", lblTotalPengeluaran, DANGER, "\u2608"));
        statsPanel.add(createStatCard("Laba Bersih (Keuntungan)", lblLabaBersih, new Color(16, 185, 129), "\u2605"));
        statsPanel.add(createStatCard("Margin Keuntungan", lblMarginKeuntungan, new Color(139, 92, 246), "%"));

        add(statsPanel, BorderLayout.NORTH);

        // 2. BOTTOM SECTION: 2 KOLOM (RINCIAN FORMULA KEUNTUNGAN + TABEL BREAKDOWN PER TANGGAL)
        JPanel contentRow = new JPanel(new GridLayout(1, 2, 16, 0));
        contentRow.setOpaque(false);

        // Kiri: Card Rincian Analisis Keuntungan
        JPanel analysisCard = createCard();
        analysisCard.setLayout(new BorderLayout(0, 14));

        JLabel titleAnalysis = new JLabel("Rincian Kalkulasi Keuntungan Usaha");
        titleAnalysis.setFont(FONT_TITLE);
        titleAnalysis.setForeground(PRIMARY);
        analysisCard.add(titleAnalysis, BorderLayout.NORTH);

        JPanel formulaBox = new JPanel();
        formulaBox.setLayout(new BoxLayout(formulaBox, BoxLayout.Y_AXIS));
        formulaBox.setOpaque(false);
        formulaBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblFormulaPemasukan = createValueLabel(SUCCESS);
        lblFormulaPengeluaran = createValueLabel(DANGER);
        lblFormulaLabaBersih = createValueLabel(new Color(16, 185, 129));
        lblStatusFinansial = createValueLabel(PRIMARY);
        lblRataRataTransaksi = createValueLabel(TEXT_PRIMARY);

        formulaBox.add(createRowItem("(+) Total Pemasukan Laundry (Laba Kotor)", lblFormulaPemasukan));
        formulaBox.add(Box.createVerticalStrut(10));
        formulaBox.add(createRowItem("(-) Beban Pengeluaran & Pembelian Bahan", lblFormulaPengeluaran));
        formulaBox.add(Box.createVerticalStrut(12));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        formulaBox.add(sep);
        formulaBox.add(Box.createVerticalStrut(12));

        formulaBox.add(createRowItem("(=) Keuntungan Bersih (Laba Bersih)", lblFormulaLabaBersih));
        formulaBox.add(Box.createVerticalStrut(10));
        formulaBox.add(createRowItem("Status Keuangan Saat Ini", lblStatusFinansial));
        formulaBox.add(Box.createVerticalStrut(10));
        formulaBox.add(createRowItem("Rata-rata Pendapatan per Transaksi", lblRataRataTransaksi));

        formulaBox.add(Box.createVerticalStrut(16));
        JLabel noteLbl = new JLabel("<html><i>* Angka keuntungan dihitung 100% otomatis secara real-time dari data arus keuangan transaksi dan pengeluaran operasional laundry.</i></html>");
        noteLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteLbl.setForeground(TEXT_SECONDARY);
        formulaBox.add(noteLbl);

        analysisCard.add(formulaBox, BorderLayout.CENTER);
        contentRow.add(analysisCard);

        // Kanan: Tabel Rekap Keuntungan Berdasarkan Tanggal
        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JLabel titleTable = new JLabel("Rekapitulasi Keuntungan per Tanggal");
        titleTable.setFont(FONT_TITLE);
        titleTable.setForeground(TEXT_PRIMARY);
        tableCard.add(titleTable, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{
            "Tanggal", "Pemasukan (Kotor)", "Pengeluaran", "Keuntungan Bersih"
        }, 0);
        table = createStyledTable(tableModel);
        tableCard.add(createScrollPane(table), BorderLayout.CENTER);

        contentRow.add(tableCard);

        add(contentRow, BorderLayout.CENTER);
    }

    private JLabel createValueLabel(Color color) {
        JLabel l = new JLabel("Rp 0");
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(color);
        return l;
    }

    private JPanel createRowItem(String titleText, JLabel valLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        title.setForeground(TEXT_PRIMARY);

        row.add(title, BorderLayout.WEST);
        row.add(valLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor, String icon) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        iconLabel.setForeground(accentColor);
        iconLabel.setPreferredSize(new Dimension(42, 42));
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

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
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
        double totalPemasukan = 0;
        double totalPengeluaran = 0;
        int totalTransaksi = 0;

        // Total Pemasukan
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(jumlah_bayar), 0) FROM pembayaran")) {
            if (rs.next()) totalPemasukan = rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }

        // Total Pengeluaran
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(total), 0) FROM pembelian")) {
            if (rs.next()) totalPengeluaran = rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }

        // Total Transaksi
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM transaksi")) {
            if (rs.next()) totalTransaksi = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }

        double labaBersih = totalPemasukan - totalPengeluaran;
        double margin = totalPemasukan > 0 ? (labaBersih / totalPemasukan) * 100.0 : 0.0;
        double rataRata = totalTransaksi > 0 ? (totalPemasukan / totalTransaksi) : 0.0;

        // Set Top Stat Cards
        lblLabaKotor.setText(String.format("Rp %,.0f", totalPemasukan));
        lblTotalPengeluaran.setText(String.format("Rp %,.0f", totalPengeluaran));
        lblLabaBersih.setText(String.format("Rp %,.0f", labaBersih));
        lblMarginKeuntungan.setText(String.format("%.1f%%", margin));

        if (labaBersih >= 0) {
            lblLabaBersih.setForeground(new Color(16, 185, 129));
            lblStatusFinansial.setText("PROFIT / UNTUNG");
            lblStatusFinansial.setForeground(new Color(16, 185, 129));
        } else {
            lblLabaBersih.setForeground(DANGER);
            lblStatusFinansial.setText("DEFISIT / RUGI");
            lblStatusFinansial.setForeground(DANGER);
        }

        // Set Formula Labels
        lblFormulaPemasukan.setText(String.format("Rp %,.0f", totalPemasukan));
        lblFormulaPengeluaran.setText(String.format("Rp %,.0f", totalPengeluaran));
        lblFormulaLabaBersih.setText(String.format("Rp %,.0f", labaBersih));
        lblRataRataTransaksi.setText(String.format("Rp %,.0f", rataRata));

        // Load Table Rekapitulasi per Tanggal
        tableModel.setRowCount(0);
        String sql =
            "SELECT tanggal, " +
            "SUM(masuk) AS total_masuk, " +
            "SUM(keluar) AS total_keluar " +
            "FROM (" +
            "  SELECT tanggal, jumlah_bayar AS masuk, 0 AS keluar FROM pembayaran " +
            "  UNION ALL " +
            "  SELECT tanggal, 0 AS masuk, total AS keluar FROM pembelian " +
            ") arus " +
            "GROUP BY tanggal " +
            "ORDER BY tanggal DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String tgl = rs.getString("tanggal");
                double msk = rs.getDouble("total_masuk");
                double klr = rs.getDouble("total_keluar");
                double selisih = msk - klr;

                String selisihStr = (selisih >= 0 ? "+ " : "") + String.format("Rp %,.0f", selisih);
                tableModel.addRow(new Object[]{
                    tgl,
                    String.format("Rp %,.0f", msk),
                    String.format("Rp %,.0f", klr),
                    selisihStr
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
