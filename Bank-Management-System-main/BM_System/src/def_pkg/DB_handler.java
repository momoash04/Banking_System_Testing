package def_pkg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DB_handler implements AutoCloseable {
	private static final String URL = "jdbc:mysql://localhost:3306/bank_schema";
	private static final String USER = "root";
	private static final String PASS = "";

	private Connection connection;

	public DB_handler() throws SQLException {
		try {
			this.connection = DriverManager.getConnection(URL, USER, PASS);
			System.out.println("Database connection established successfully!");
		} catch (SQLException e) {
			System.err.println("Database connection failed: " + e.getMessage());
			throw e;
		}
	}

	public Connection getConnection() {
		return this.connection;
	}

	public void closeConnection() {
		try {
			if (this.connection != null && !this.connection.isClosed()) {
				this.connection.close();
				System.out.println("Database connection closed successfully!");
			}
		} catch (SQLException e) {
			System.err.println("Error closing connection: " + e.getMessage());
		}
	}
	@Override
	public void close() {
		closeConnection();
	}

	// Database initialization (should only be called once during setup)
	public static void initializeDatabase() throws SQLException {
		try (Connection conn = new DB_handler().getConnection()) {
			createTables(conn);
			System.out.println("Database initialized successfully");
		}
	}

	private static void createTables(Connection conn) throws SQLException {
		executeSQL(conn,
				"CREATE TABLE IF NOT EXISTS client (" +
						"  client_id INT PRIMARY KEY AUTO_INCREMENT," +
						"  f_name VARCHAR(50) NOT NULL," +
						"  l_name VARCHAR(50) NOT NULL," +
						"  father_name VARCHAR(50) NOT NULL," +
						"  mother_name VARCHAR(50) NOT NULL," +
						"  CNIC VARCHAR(15) UNIQUE NOT NULL," +
						"  DOB DATE NOT NULL," +
						"  phone VARCHAR(15) NOT NULL," +
						"  email VARCHAR(50) NOT NULL," +
						"  address VARCHAR(100) NOT NULL" +
						")");

		executeSQL(conn,
				"CREATE TABLE IF NOT EXISTS bank_account (" +
						"  acc_num INT PRIMARY KEY AUTO_INCREMENT," +
						"  client_id INT NOT NULL," +
						"  login_id INT," +
						"  type VARCHAR(20) NOT NULL," +
						"  balance DECIMAL(15,2) DEFAULT 0," +
						"  status TINYINT DEFAULT 1," +  // 0=closed, 1=active, 2=blocked
						"  opening_date DATE NOT NULL," +
						"  FOREIGN KEY (client_id) REFERENCES client(client_id)" +
						")");

		// Add other table creation statements as needed...
	}

	private static void executeSQL(Connection conn, String sql) throws SQLException {
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		}
	}

	// Simple connection health check
	public boolean isConnectionValid() {
		try {
			return this.connection != null
					&& !this.connection.isClosed()
					&& this.connection.isValid(2);
		} catch (SQLException e) {
			return false;
		}
	}
}