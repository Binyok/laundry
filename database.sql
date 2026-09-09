
CREATE DATABASE IF NOT EXISTS laundry;
USE laundry;

-- Table for Admin Users
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin') DEFAULT 'admin'
);

-- Table for Customers
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
    address TEXT
);

-- Table for Laundry Services
CREATE TABLE IF NOT EXISTS services (
    id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL UNIQUE,
    price_per_kg DECIMAL(10, 2) NOT NULL
);

-- Table for Laundry Transactions
CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_weight DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    due_date DATETIME,
    status ENUM('Diterima', 'Diproses', 'Selesai', 'Sudah Diambil') DEFAULT 'Diterima',
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Table for Transaction Details (to link services to transactions)
CREATE TABLE IF NOT EXISTS transaction_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT,
    service_id INT,
    weight DECIMAL(10, 2) NOT NULL,
    price_per_kg DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    FOREIGN KEY (service_id) REFERENCES services(id)
);

-- Optional: Add some initial data for services
INSERT IGNORE INTO services (service_name, price_per_kg) VALUES
('Cuci Kering', 7000.00),
('Cuci Setrika', 10000.00),
('Setrika', 5000.00);

-- Optional: Add a default admin user (password 'admin123')
-- IMPORTANT: In a real application, never store plain text passwords. Use hashing!
INSERT IGNORE INTO users (username, password, role) VALUES
('admin', 'admin123', 'admin');
