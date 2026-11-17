package laundry.ui.panels;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import laundry.db.DatabaseHelper;

public abstract class BasePanel extends JPanel {

    protected static final Color PRIMARY = new Color(30, 58, 138);
    protected static final Color ACCENT = new Color(59, 130, 246);
    protected static final Color SUCCESS = new Color(34, 197, 94);
    protected static final Color DANGER = new Color(239, 68, 68);
    protected static final Color WARNING = new Color(245, 158, 11);
    protected static final Color BG = new Color(241, 245, 249);
    protected static final Color CARD_BG = Color.WHITE;
    protected static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    protected static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    protected static final Color BORDER_COLOR = new Color(226, 232, 240);
    protected static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    protected static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    protected static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    protected static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);

    protected JTable table;
    protected DefaultTableModel tableModel;
    protected Connection conn;

    public BasePanel() {
        conn = DatabaseHelper.getInstance().getConnection();
        setLayout(new BorderLayout(16, 16));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
    }

    protected void init() {
        initComponents();
        loadData();
    }

    public void refreshData() {
        loadData();
    }

    protected abstract void initComponents();
    protected abstract void loadData();

    protected JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        return card;
    }

    protected JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    protected JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_INPUT);
        tf.setPreferredSize(new Dimension(200, 34));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return tf;
    }

    protected JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 36));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            Color original = bg;
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(original);
            }
        });

        return btn;
    }

    protected JTable createStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(36);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(219, 234, 254));
        t.setSelectionForeground(TEXT_PRIMARY);
        t.setBackground(Color.WHITE);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                return this;
            }
        });

        JTableHeader header = t.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(new Color(248, 250, 252));
                setForeground(TEXT_PRIMARY);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                setHorizontalAlignment(SwingConstants.LEFT);
                return this;
            }
        });

        return t;
    }

    protected JScrollPane createScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    protected JComboBox<String> createComboBox() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setFont(FONT_INPUT);
        cb.setPreferredSize(new Dimension(200, 34));
        cb.setBackground(Color.WHITE);
        return cb;
    }

    protected void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    protected void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }

    protected boolean confirmDelete() {
        return JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus data ini?", "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
