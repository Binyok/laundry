package laundry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String URL = "jdbc:sqlite:laundry.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found.");
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Enable Foreign Keys
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username TEXT NOT NULL UNIQUE,"
                    + "password TEXT NOT NULL,"
                    + "role TEXT DEFAULT 'admin'"
                    + ");");

            // Customers table
            stmt.execute("CREATE TABLE IF NOT EXISTS customers ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT NOT NULL,"
                    + "phone_number TEXT UNIQUE,"
                    + "address TEXT"
                    + ");");

            // Services table
            stmt.execute("CREATE TABLE IF NOT EXISTS services ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "service_name TEXT NOT NULL UNIQUE,"
                    + "price_per_kg REAL NOT NULL"
                    + ");");

            // Transactions table
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "customer_id INTEGER,"
                    + "transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "total_weight REAL NOT NULL,"
                    + "total_amount REAL NOT NULL,"
                    + "due_date DATETIME,"
                    + "status TEXT CHECK(status IN ('Diterima', 'Diproses', 'Selesai', 'Sudah Diambil')) DEFAULT 'Diterima',"
                    + "FOREIGN KEY (customer_id) REFERENCES customers(id)"
                    + ");");

            // Transaction Details table
            stmt.execute("CREATE TABLE IF NOT EXISTS transaction_details ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "transaction_id INTEGER,"
                    + "service_id INTEGER,"
                    + "weight REAL NOT NULL,"
                    + "price_per_kg REAL NOT NULL,"
                    + "subtotal REAL NOT NULL,"
                    + "FOREIGN KEY (transaction_id) REFERENCES transactions(id),"
                    + "FOREIGN KEY (service_id) REFERENCES services(id)"
                    + ");");

            // Insert default service data if empty
            stmt.execute("INSERT OR IGNORE INTO services (id, service_name, price_per_kg) VALUES "
                    + "(1, 'Cuci Kering', 7000.00),"
                    + "(2, 'Cuci Setrika', 10000.00),"
                    + "(3, 'Setrika', 5000.00);");

            // Insert default admin user if empty
            stmt.execute("INSERT OR IGNORE INTO users (id, username, password, role) VALUES "
                    + "(1, 'admin', 'admin123', 'admin');");

            System.out.println("Database successfully initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}