package laundry.db;

import java.sql.*;
import javax.swing.JOptionPane;

public class DatabaseHelper {

    private static final String DB_HOST = System.getProperty("db.host", "localhost");
    private static final String DB_PORT = System.getProperty("db.port", "3306");
    private static final String DB_NAME = System.getProperty("db.name", "laundry_db");
    private static final String DB_USER = System.getProperty("db.user", "root");
    private static final String DB_PASS = System.getProperty("db.pass", "");

    private static DatabaseHelper instance;
    private Connection connection;

    private DatabaseHelper() {
        try {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Class.forName("com.mysql.jdbc.Driver");
            }

            // Create database if not exists
            String serverUrl = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try (Connection tempConn = DriverManager.getConnection(serverUrl, DB_USER, DB_PASS);
                 Statement stmt = tempConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + DB_NAME + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
            } catch (SQLException ex) {
                // If permission denied or already exists, continue
            }

            // Connect to laundry_db
            String dbUrl = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            connection = DriverManager.getConnection(dbUrl, DB_USER, DB_PASS);
            connection.setAutoCommit(true);

            createTables();
            runMigrations();
            seedInitialData();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Gagal terhubung ke database MySQL!\n\n" +
                "Detail Error: " + e.getMessage() + "\n\n" +
                "Pastikan MySQL di XAMPP / MariaDB sudah dalam kondisi START/RUNNING di port " + DB_PORT + ".\n" +
                "Username: " + DB_USER + " (Password kosong)",
                "Koneksi Database Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS pelanggan ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "nama VARCHAR(100) NOT NULL,"
                    + "alamat VARCHAR(255),"
                    + "telepon VARCHAR(30),"
                    + "email VARCHAR(100),"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.execute("CREATE TABLE IF NOT EXISTS karyawan ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "nama VARCHAR(100) NOT NULL,"
                    + "jabatan VARCHAR(50),"
                    + "telepon VARCHAR(30),"
                    + "alamat VARCHAR(255),"
                    + "gaji DOUBLE DEFAULT 0,"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.execute("CREATE TABLE IF NOT EXISTS layanan ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "nama VARCHAR(100) NOT NULL,"
                    + "satuan VARCHAR(20),"
                    + "harga DOUBLE NOT NULL,"
                    + "estimasi_hari INT DEFAULT 1,"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.execute("CREATE TABLE IF NOT EXISTS pembelian ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "nama_barang VARCHAR(100) NOT NULL,"
                    + "jumlah INT DEFAULT 1,"
                    + "harga_satuan DOUBLE NOT NULL,"
                    + "total DOUBLE NOT NULL,"
                    + "tempat_beli VARCHAR(100),"
                    + "tanggal DATE DEFAULT (CURRENT_DATE),"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.execute("CREATE TABLE IF NOT EXISTS transaksi ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "kode_transaksi VARCHAR(50) NOT NULL UNIQUE,"
                    + "pelanggan_id INT,"
                    + "karyawan_id INT,"
                    + "tanggal DATE DEFAULT (CURRENT_DATE),"
                    + "total DOUBLE DEFAULT 0,"
                    + "status VARCHAR(30) DEFAULT 'Proses',"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY(pelanggan_id) REFERENCES pelanggan(id) ON DELETE SET NULL,"
                    + "FOREIGN KEY(karyawan_id) REFERENCES karyawan(id) ON DELETE SET NULL"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.execute("CREATE TABLE IF NOT EXISTS detail_transaksi ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "transaksi_id INT NOT NULL,"
                    + "layanan_id INT NOT NULL,"
                    + "qty DOUBLE DEFAULT 1,"
                    + "harga DOUBLE NOT NULL,"
                    + "subtotal DOUBLE NOT NULL,"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY(transaksi_id) REFERENCES transaksi(id) ON DELETE CASCADE,"
                    + "FOREIGN KEY(layanan_id) REFERENCES layanan(id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.execute("CREATE TABLE IF NOT EXISTS pembayaran ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "transaksi_id INT NOT NULL,"
                    + "jumlah_bayar DOUBLE NOT NULL,"
                    + "metode VARCHAR(50) DEFAULT 'Cash',"
                    + "tanggal DATE DEFAULT (CURRENT_DATE),"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY(transaksi_id) REFERENCES transaksi(id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
    }

    private void runMigrations() {
        try (Statement stmt = connection.createStatement()) {
            // Check pembelian table for tempat_beli column
            try {
                stmt.execute("ALTER TABLE pembelian ADD COLUMN tempat_beli VARCHAR(100) NULL AFTER total");
            } catch (SQLException ignored) {}

            // Migrate supplier names if table exists
            try {
                stmt.executeUpdate("UPDATE pembelian p JOIN supplier s ON p.supplier_id = s.id SET p.tempat_beli = s.nama WHERE p.tempat_beli IS NULL OR p.tempat_beli = ''");
            } catch (SQLException ignored) {}

            stmt.executeUpdate("UPDATE pembelian SET tempat_beli = '-' WHERE tempat_beli IS NULL OR tempat_beli = ''");

            // Drop foreign key fk_pembelian_supplier if exists
            try {
                stmt.execute("ALTER TABLE pembelian DROP FOREIGN KEY fk_pembelian_supplier");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE pembelian DROP FOREIGN KEY pembelian_ibfk_1");
            } catch (SQLException ignored) {}

            // Drop supplier_id column if exists
            try {
                stmt.execute("ALTER TABLE pembelian DROP COLUMN supplier_id");
            } catch (SQLException ignored) {}

            // Drop supplier, pengeluaran, and modal tables
            try {
                stmt.execute("DROP TABLE IF EXISTS supplier");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("DROP TABLE IF EXISTS pengeluaran");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("DROP TABLE IF EXISTS modal");
            } catch (SQLException ignored) {}

            // Populate additional transactions and payments to ensure positive profit
            try {
                stmt.execute("INSERT IGNORE INTO transaksi (id, kode_transaksi, pelanggan_id, karyawan_id, tanggal, total, status) VALUES "
                        + "(3, 'TRX-1003', 3, 1, CURDATE(), 70000, 'Selesai'),"
                        + "(4, 'TRX-1004', 1, 2, CURDATE(), 120000, 'Selesai'),"
                        + "(5, 'TRX-1005', 2, 1, CURDATE(), 90000, 'Selesai'),"
                        + "(6, 'TRX-1006', 3, 2, CURDATE(), 75000, 'Selesai'),"
                        + "(7, 'TRX-1007', 1, 1, CURDATE(), 84000, 'Selesai'),"
                        + "(8, 'TRX-1008', 2, 2, CURDATE(), 60000, 'Selesai'),"
                        + "(9, 'TRX-1009', 3, 1, CURDATE(), 50000, 'Selesai'),"
                        + "(10, 'TRX-1010', 1, 2, CURDATE(), 60000, 'Selesai')");

                stmt.execute("INSERT IGNORE INTO detail_transaksi (id, transaksi_id, layanan_id, qty, harga, subtotal) VALUES "
                        + "(3, 3, 1, 10, 7000, 70000),"
                        + "(4, 4, 2, 10, 12000, 120000),"
                        + "(5, 5, 6, 3, 30000, 90000),"
                        + "(6, 6, 5, 3, 25000, 75000),"
                        + "(7, 7, 1, 12, 7000, 84000),"
                        + "(8, 8, 4, 15, 4000, 60000),"
                        + "(9, 9, 3, 10, 5000, 50000),"
                        + "(10, 10, 6, 2, 30000, 60000)");

                stmt.execute("INSERT IGNORE INTO pembayaran (id, transaksi_id, jumlah_bayar, metode, tanggal) VALUES "
                        + "(3, 3, 70000, 'Cash', CURDATE()),"
                        + "(4, 4, 120000, 'Transfer Bank', CURDATE()),"
                        + "(5, 5, 90000, 'QRIS / E-Wallet', CURDATE()),"
                        + "(6, 6, 75000, 'Cash', CURDATE()),"
                        + "(7, 7, 84000, 'Transfer Bank', CURDATE()),"
                        + "(8, 8, 60000, 'Cash', CURDATE()),"
                        + "(9, 9, 50000, 'QRIS / E-Wallet', CURDATE()),"
                        + "(10, 10, 60000, 'Cash', CURDATE())");
            } catch (SQLException ignored) {}
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void seedInitialData() {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pelanggan");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO pelanggan (nama, alamat, telepon, email) VALUES "
                        + "('Ahmad Yani', 'Jl. Merdeka No. 12', '081234567890', 'ahmad@gmail.com'),"
                        + "('Siti Nurhaliza', 'Jl. Sudirman No. 45', '081398765432', 'siti@yahoo.com'),"
                        + "('Budi Santoso', 'Jl. Diponegoro No. 8', '085712345678', 'budi@gmail.com')");

                stmt.execute("INSERT INTO karyawan (nama, jabatan, telepon, alamat, gaji) VALUES "
                        + "('Dewi Lestari', 'Kasir', '082111222333', 'Jl. Kenanga No. 3', 3000000),"
                        + "('Rian Hidayat', 'Operasional', '082222333444', 'Jl. Mawar No. 7', 2800000)");

                stmt.execute("INSERT INTO layanan (nama, satuan, harga, estimasi_hari) VALUES "
                        + "('Cuci Komplit Reguler', 'Kg', 7000, 2),"
                        + "('Cuci Komplit Kilat', 'Kg', 12000, 1),"
                        + "('Cuci Kering Saja', 'Kg', 5000, 1),"
                        + "('Setrika Saja', 'Kg', 4000, 1),"
                        + "('Cuci Bed Cover Besar', 'Pcs', 25000, 3),"
                        + "('Cuci Sepatu', 'Pasang', 30000, 2)");

                stmt.execute("INSERT INTO pembelian (nama_barang, jumlah, harga_satuan, total, tempat_beli, tanggal) VALUES "
                        + "('Deterjen Cair 20L', 2, 120000, 240000, 'CV Berkah Sabun', CURDATE()),"
                        + "('Parfum Laundry Sakura 5L', 1, 95000, 95000, 'Toko Wangi Parfum', CURDATE()),"
                        + "('Deterjen Bubuk Konsentrat', 1, 50000, 50000, 'Toko Sabun Bersih', CURDATE()),"
                        + "('Plastik Packing Laundry', 2, 25000, 50000, 'Toko Plastik Makmur', CURDATE())");

                stmt.execute("INSERT INTO transaksi (id, kode_transaksi, pelanggan_id, karyawan_id, tanggal, total, status) VALUES "
                        + "(1, 'TRX-1001', 1, 1, CURDATE(), 35000, 'Proses'),"
                        + "(2, 'TRX-1002', 2, 1, CURDATE(), 25000, 'Selesai'),"
                        + "(3, 'TRX-1003', 3, 1, CURDATE(), 70000, 'Selesai'),"
                        + "(4, 'TRX-1004', 1, 2, CURDATE(), 120000, 'Selesai'),"
                        + "(5, 'TRX-1005', 2, 1, CURDATE(), 90000, 'Selesai'),"
                        + "(6, 'TRX-1006', 3, 2, CURDATE(), 75000, 'Selesai'),"
                        + "(7, 'TRX-1007', 1, 1, CURDATE(), 84000, 'Selesai'),"
                        + "(8, 'TRX-1008', 2, 2, CURDATE(), 60000, 'Selesai'),"
                        + "(9, 'TRX-1009', 3, 1, CURDATE(), 50000, 'Selesai'),"
                        + "(10, 'TRX-1010', 1, 2, CURDATE(), 60000, 'Selesai')");

                stmt.execute("INSERT INTO detail_transaksi (id, transaksi_id, layanan_id, qty, harga, subtotal) VALUES "
                        + "(1, 1, 1, 5, 7000, 35000),"
                        + "(2, 5, 1, 25000, 25000),"
                        + "(3, 3, 1, 10, 7000, 70000),"
                        + "(4, 4, 2, 10, 12000, 120000),"
                        + "(5, 5, 6, 3, 30000, 90000),"
                        + "(6, 6, 5, 3, 25000, 75000),"
                        + "(7, 7, 1, 12, 7000, 84000),"
                        + "(8, 8, 4, 15, 4000, 60000),"
                        + "(9, 9, 3, 10, 5000, 50000),"
                        + "(10, 10, 6, 2, 30000, 60000)");

                stmt.execute("INSERT INTO pembayaran (id, transaksi_id, jumlah_bayar, metode, tanggal) VALUES "
                        + "(1, 1, 35000, 'Cash', CURDATE()),"
                        + "(2, 2, 25000, 'QRIS / E-Wallet', CURDATE()),"
                        + "(3, 3, 70000, 'Cash', CURDATE()),"
                        + "(4, 4, 120000, 'Transfer Bank', CURDATE()),"
                        + "(5, 5, 90000, 'QRIS / E-Wallet', CURDATE()),"
                        + "(6, 6, 75000, 'Cash', CURDATE()),"
                        + "(7, 7, 84000, 'Transfer Bank', CURDATE()),"
                        + "(8, 8, 60000, 'Cash', CURDATE()),"
                        + "(9, 9, 50000, 'QRIS / E-Wallet', CURDATE()),"
                        + "(10, 10, 60000, 'Cash', CURDATE())");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
