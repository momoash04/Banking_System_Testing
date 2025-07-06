package def_pkg;

import java.sql.*;

public class Bank_Account {
	private String acc_num;
	private String client_id;
	private String login_id;
	private String type;
	private String balance;
	private String status;
	private String opening_date;

	public Bank_Account() {}

	public Bank_Account(String acc_num, String client_id, String login_id, String type,
						String balance, String status, String opening_date) {
		this.acc_num = acc_num;
		this.client_id = client_id;
		this.login_id = login_id;
		this.type = type;
		this.balance = balance;
		this.status = status;
		this.opening_date = opening_date;
	}

	// Getters
	public String getAccountNum() { return acc_num; }
	public String getClientId() { return client_id; }
	public String getLoginId() { return login_id; }
	public String getType() { return type; }
	public String getBalance() { return balance; }
	public String getStatus() { return status; }
	public String getOpeningDate() { return opening_date; }
	public void setStatus(String status) { this.status = status; }

	// Database operations
	public static Bank_Account getByAccountNumber(Connection conn, String accountNum) throws SQLException {
		String sql = "SELECT * FROM bank_account WHERE acc_num = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, accountNum);
		ResultSet rs = pstmt.executeQuery();
		Bank_Account result = null;
		if (rs.next()) {
			result = new Bank_Account(
					rs.getString("acc_num"),
					rs.getString("client_id"),
					rs.getString("login_id"),
					rs.getString("type"),
					rs.getString("balance"),
					rs.getString("status"),
					rs.getString("opening_date")
			);
		}
		rs.close();
		pstmt.close();
		return result;
	}

	public static Bank_Account getByClientId(Connection conn, String clientId) throws SQLException {
		String sql = "SELECT * FROM bank_account WHERE client_id = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, clientId);
		ResultSet rs = pstmt.executeQuery();
		Bank_Account result = null;
		if (rs.next()) {
			result = new Bank_Account(
					rs.getString("acc_num"),
					rs.getString("client_id"),
					rs.getString("login_id"),
					rs.getString("type"),
					rs.getString("balance"),
					rs.getString("status"),
					rs.getString("opening_date")
			);
		}
		rs.close();
		pstmt.close();
		return result;
	}

	public void updateBalance(Connection conn) throws SQLException {
		String sql = "SELECT balance FROM bank_account WHERE acc_num = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, acc_num);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			balance = rs.getString("balance");
		}
		rs.close();
		pstmt.close();
	}

	public static Bank_Account getByLoginId(Connection conn, String loginId) throws SQLException {
		String sql = "SELECT * FROM bank_account WHERE login_id = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, loginId);
		ResultSet rs = pstmt.executeQuery();
		Bank_Account result = null;
		if (rs.next()) {
			result = new Bank_Account(
					rs.getString("acc_num"),
					rs.getString("client_id"),
					rs.getString("login_id"),
					rs.getString("type"),
					rs.getString("balance"),
					rs.getString("status"),
					rs.getString("opening_date")
			);
		}
		rs.close();
		pstmt.close();
		return result;
	}
}
