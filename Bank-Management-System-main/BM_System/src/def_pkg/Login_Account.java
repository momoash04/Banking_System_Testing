package def_pkg;

import java.sql.*;
import java.util.Objects;
import javafx.util.Pair;

public class Login_Account {
	private String login_id;
	private String username;
	private String password;
	private String type;

	public Login_Account(String login_id, String username, String password, String type) {
		this.login_id = login_id;
		this.username = username;
		this.password = password;
		this.type = type;
	}

	// Getters
	public String getLoginId() { return login_id; }
	public String getUsername() { return username; }
	public String getType() {
		switch (type.toUpperCase()) {
			case "C": return "Client";
			case "M": return "Manager";
			default: return "Unknown";
		}
	}

	// Authentication methods
	public static Pair<Login_Account, Integer> signIn(Connection conn, String username, String password) throws SQLException {
		String sql = "SELECT login_id, type FROM login_account WHERE username = ? AND password = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, username);
		pstmt.setString(2, password);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next() && !rs.wasNull()) {
			String type = rs.getString("type");
			String loginId = rs.getString("login_id");

			if (Objects.equals(type, "M")) {
				return new Pair<>(new Login_Account(loginId, username, "", type), 1);
			} else {
				String sql2 = "SELECT status FROM bank_account WHERE login_id = ?";
				PreparedStatement pstmt2 = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS);
				pstmt2.setString(1, loginId);
				ResultSet rs2 = pstmt2.executeQuery();

				if (rs2.next()) {
					int status = rs2.getInt("status");
					if (status == 1) {
						return new Pair<>(new Login_Account(loginId, username, "", type), 1);
					} else if (status == 2) {
						return new Pair<>(new Login_Account(loginId, username, "", type), 2);
					}
				}
			}
		}

		return new Pair<>(null, 0);
	}

	public static boolean verifyAccount(Connection conn, String accNum, String cnic) throws SQLException {
		String sql = "SELECT c.CNIC FROM client c " +
				"JOIN bank_account ba ON c.client_id = ba.client_id " +
				"WHERE ba.acc_num = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, accNum);
		ResultSet rs = pstmt.executeQuery();

		return rs.next() && rs.getString("CNIC").equals(cnic);
	}

	public static int signUp(Connection conn, String username, String pass1, String pass2, String accNum) throws SQLException {
		if (!pass1.equals(pass2)) return -1;

		conn.setAutoCommit(false);

		String sqlselect = "SELECT login_id FROM bank_account WHERE acc_num = ?";
		PreparedStatement pstmt = conn.prepareStatement(sqlselect);
		pstmt.setString(1, accNum);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			int currentLoginId = rs.getInt("login_id");
			if (rs.wasNull()) {
				String sql = "INSERT INTO login_account (username, password, type) VALUES (?, ?, 'C')";
				PreparedStatement pstmt2 = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				pstmt2.setString(1, username);
				pstmt2.setString(2, pass1);
				pstmt2.executeUpdate();

				ResultSet rs2 = pstmt2.getGeneratedKeys();
				if (rs2.next()) {
					int loginId = rs2.getInt(1);
					String updateSql = "UPDATE bank_account SET login_id = ? WHERE acc_num = ?";
					PreparedStatement updateStmt = conn.prepareStatement(updateSql);
					updateStmt.setInt(1, loginId);
					updateStmt.setString(2, accNum);
					updateStmt.executeUpdate();
				}
			} else {
				conn.setAutoCommit(true);
				return -2;
			}
		}

		conn.commit();
		conn.setAutoCommit(true);
		return 0;
	}

	public static String getEmployeeName(Connection conn, String loginId) throws SQLException {
		String sql = "SELECT f_name, l_name FROM employee WHERE login_id = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, loginId);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			return rs.getString("f_name") + " " + rs.getString("l_name");
		}
		return "";
	}

	public static Login_Account getByUsername(Connection conn, String username) throws SQLException {
		String sql = "SELECT login_id, type FROM login_account WHERE username = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, username);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			return new Login_Account(
					rs.getString("login_id"),
					username,
					"", // Empty password for security
					rs.getString("type")
			);
		}
		return null;
	}
}
