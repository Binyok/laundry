CREATE DATABASE IF NOT EXISTS `laundry_db`;
USE `laundry_db`;

CREATE TABLE IF NOT EXISTS `pelanggan` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `nama` VARCHAR(100) NOT NULL,
  `alamat` VARCHAR(255) NULL,
  `telepon` VARCHAR(30) NULL,
  `email` VARCHAR(100) NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `karyawan` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `nama` VARCHAR(100) NOT NULL,
  `jabatan` VARCHAR(50) NULL,
  `telepon` VARCHAR(30) NULL,
  `alamat` VARCHAR(255) NULL,
  `gaji` DOUBLE DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `layanan` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `nama` VARCHAR(100) NOT NULL,
  `satuan` VARCHAR(20) NULL,
  `harga` DOUBLE NOT NULL,
  `estimasi_hari` INT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pembelian` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `nama_barang` VARCHAR(100) NOT NULL,
  `jumlah` INT DEFAULT 1,
  `harga_satuan` DOUBLE NOT NULL,
  `total` DOUBLE NOT NULL,
  `tempat_beli` VARCHAR(100) NULL,
  `tanggal` DATE DEFAULT (CURRENT_DATE),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `transaksi` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `kode_transaksi` VARCHAR(50) NOT NULL UNIQUE,
  `pelanggan_id` INT NULL,
  `karyawan_id` INT NULL,
  `tanggal` DATE DEFAULT (CURRENT_DATE),
  `total` DOUBLE DEFAULT 0,
  `status` VARCHAR(30) DEFAULT 'Proses',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX (`pelanggan_id`),
  INDEX (`karyawan_id`),
  CONSTRAINT `fk_transaksi_pelanggan` FOREIGN KEY (`pelanggan_id`) REFERENCES `pelanggan` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_transaksi_karyawan` FOREIGN KEY (`karyawan_id`) REFERENCES `karyawan` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `detail_transaksi` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `transaksi_id` INT NOT NULL,
  `layanan_id` INT NOT NULL,
  `qty` DOUBLE DEFAULT 1,
  `harga` DOUBLE NOT NULL,
  `subtotal` DOUBLE NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX (`transaksi_id`),
  INDEX (`layanan_id`),
  CONSTRAINT `fk_detail_transaksi` FOREIGN KEY (`transaksi_id`) REFERENCES `transaksi` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_detail_layanan` FOREIGN KEY (`layanan_id`) REFERENCES `layanan` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pembayaran` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `transaksi_id` INT NOT NULL,
  `jumlah_bayar` DOUBLE NOT NULL,
  `metode` VARCHAR(50) DEFAULT 'Cash',
  `tanggal` DATE DEFAULT (CURRENT_DATE),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX (`transaksi_id`),
  CONSTRAINT `fk_pembayaran_transaksi` FOREIGN KEY (`transaksi_id`) REFERENCES `transaksi` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Data Dummy Awal
INSERT IGNORE INTO `pelanggan` (`id`, `nama`, `alamat`, `telepon`, `email`) VALUES
(1, 'Ahmad Yani', 'Jl. Merdeka No. 12', '081234567890', 'ahmad@gmail.com'),
(2, 'Siti Nurhaliza', 'Jl. Sudirman No. 45', '081398765432', 'siti@yahoo.com'),
(3, 'Budi Santoso', 'Jl. Diponegoro No. 8', '085712345678', 'budi@gmail.com');

INSERT IGNORE INTO `karyawan` (`id`, `nama`, `jabatan`, `telepon`, `alamat`, `gaji`) VALUES
(1, 'Dewi Lestari', 'Kasir', '082111222333', 'Jl. Kenanga No. 3', 3000000),
(2, 'Rian Hidayat', 'Operasional', '082222333444', 'Jl. Mawar No. 7', 2800000);

INSERT IGNORE INTO `layanan` (`id`, `nama`, `satuan`, `harga`, `estimasi_hari`) VALUES
(1, 'Cuci Komplit Reguler', 'Kg', 7000, 2),
(2, 'Cuci Komplit Kilat', 'Kg', 12000, 1),
(3, 'Cuci Kering Saja', 'Kg', 5000, 1),
(4, 'Setrika Saja', 'Kg', 4000, 1),
(5, 'Cuci Bed Cover Besar', 'Pcs', 25000, 3),
(6, 'Cuci Sepatu', 'Pasang', 30000, 2);

INSERT IGNORE INTO `pembelian` (`id`, `nama_barang`, `jumlah`, `harga_satuan`, `total`, `tempat_beli`, `tanggal`) VALUES
(1, 'Deterjen Cair 20L', 2, 120000, 240000, 'CV Berkah Sabun', CURDATE()),
(2, 'Parfum Laundry Sakura 5L', 1, 95000, 95000, 'Toko Wangi Parfum', CURDATE()),
(3, 'Deterjen Bubuk Konsentrat', 1, 50000, 50000, 'Toko Sabun Bersih', CURDATE()),
(4, 'Plastik Packing Laundry', 2, 25000, 50000, 'Toko Plastik Makmur', CURDATE());

INSERT IGNORE INTO `transaksi` (`id`, `kode_transaksi`, `pelanggan_id`, `karyawan_id`, `tanggal`, `total`, `status`) VALUES
(1, 'TRX-1001', 1, 1, CURDATE(), 35000, 'Proses'),
(2, 'TRX-1002', 2, 1, CURDATE(), 25000, 'Selesai'),
(3, 'TRX-1003', 3, 1, CURDATE(), 70000, 'Selesai'),
(4, 'TRX-1004', 1, 2, CURDATE(), 120000, 'Selesai'),
(5, 'TRX-1005', 2, 1, CURDATE(), 90000, 'Selesai'),
(6, 'TRX-1006', 3, 2, CURDATE(), 75000, 'Selesai'),
(7, 'TRX-1007', 1, 1, CURDATE(), 84000, 'Selesai'),
(8, 'TRX-1008', 2, 2, CURDATE(), 60000, 'Selesai'),
(9, 'TRX-1009', 3, 1, CURDATE(), 50000, 'Selesai'),
(10, 'TRX-1010', 1, 2, CURDATE(), 60000, 'Selesai');

INSERT IGNORE INTO `detail_transaksi` (`id`, `transaksi_id`, `layanan_id`, `qty`, `harga`, `subtotal`) VALUES
(1, 1, 1, 5, 7000, 35000),
(2, 2, 5, 1, 25000, 25000),
(3, 3, 1, 10, 7000, 70000),
(4, 4, 2, 10, 12000, 120000),
(5, 5, 6, 3, 30000, 90000),
(6, 6, 5, 3, 25000, 75000),
(7, 7, 1, 12, 7000, 84000),
(8, 8, 4, 15, 4000, 60000),
(9, 9, 3, 10, 5000, 50000),
(10, 10, 6, 2, 30000, 60000);

INSERT IGNORE INTO `pembayaran` (`id`, `transaksi_id`, `jumlah_bayar`, `metode`, `tanggal`) VALUES
(1, 1, 35000, 'Cash', CURDATE()),
(2, 2, 25000, 'QRIS / E-Wallet', CURDATE()),
(3, 3, 70000, 'Cash', CURDATE()),
(4, 4, 120000, 'Transfer Bank', CURDATE()),
(5, 5, 90000, 'QRIS / E-Wallet', CURDATE()),
(6, 6, 75000, 'Cash', CURDATE()),
(7, 7, 84000, 'Transfer Bank', CURDATE()),
(8, 8, 60000, 'Cash', CURDATE()),
(9, 9, 50000, 'QRIS / E-Wallet', CURDATE()),
(10, 10, 60000, 'Cash', CURDATE());
