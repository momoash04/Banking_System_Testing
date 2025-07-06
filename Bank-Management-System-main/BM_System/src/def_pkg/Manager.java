package def_pkg;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Manager {
	private String name;

	public Manager() {
		this.name = "";
	}

	public Manager(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	// Account management operations
	public int createAccount(Connection conn, Client newClient, String type) throws SQLException {
		conn.setAutoCommit(false);

		Client existingClient = Client.getByCNIC(conn, newClient.getCNIC());
		if (existingClient == null) {
			newClient.save(conn);
			existingClient = newClient;

			String sql = "INSERT INTO bank_account (client_id, type, balance, status, opening_date) "
					+ "VALUES (?, ?, 0, 1, CURDATE())";
			PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, existingClient.getClientID());
			pstmt.setString(2, type);
			pstmt.executeUpdate();
			pstmt.close();

			conn.commit();
			conn.setAutoCommit(true);
			return 0;
		} else {
			conn.setAutoCommit(true);
			return 1;
		}
	}

	public int blockAccount(Connection conn, Bank_Account acc) throws SQLException {
		String sql2 = "SELECT status FROM bank_account WHERE acc_num = ?";
		PreparedStatement pstmt2 = conn.prepareStatement(sql2);
		pstmt2.setString(1, acc.getAccountNum());
		ResultSet rs2 = pstmt2.executeQuery();
		if (rs2.next()) {
			if (rs2.getInt("status") == 1) {
				String sql = "UPDATE bank_account SET status = 2 WHERE acc_num = ?";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, Integer.parseInt(acc.getAccountNum()));
				int affected = pstmt.executeUpdate();
				pstmt.close();
				acc.setStatus("2");
				rs2.close();
				pstmt2.close();
				return 1;
			} else {
				rs2.close();
				pstmt2.close();
				return 0;
			}
		} else {
			rs2.close();
			pstmt2.close();
			return 2;
		}
	}

	public int unblockAccount(Connection conn, Bank_Account acc) throws SQLException {
		String sql2 = "SELECT status FROM bank_account WHERE acc_num = ?";
		PreparedStatement pstmt2 = conn.prepareStatement(sql2);
		pstmt2.setString(1, acc.getAccountNum());
		ResultSet rs2 = pstmt2.executeQuery();
		if (rs2.next()) {
			if (rs2.getInt("status") == 2) {
				String sql = "UPDATE bank_account SET status = 1 WHERE acc_num = ?";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, Integer.parseInt(acc.getAccountNum()));
				int affected = pstmt.executeUpdate();
				pstmt.close();
				acc.setStatus("1");
				rs2.close();
				pstmt2.close();
				return 1;
			} else {
				rs2.close();
				pstmt2.close();
				return 0;
			}
		} else {
			rs2.close();
			pstmt2.close();
			return 2;
		}
	}

	// Information retrieval methods
	public Client getClientInfo(Connection conn, String accNum) throws SQLException {
		String sql = "SELECT c.* FROM client c JOIN bank_account ba ON c.client_id = ba.client_id WHERE ba.acc_num = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, accNum);
		ResultSet rs = pstmt.executeQuery();
		Client result = null;
		if (rs.next()) {
			result = new Client(
					rs.getString("client_id"),
					rs.getString("f_name"),
					rs.getString("l_name"),
					rs.getString("father_name"),
					rs.getString("mother_name"),
					rs.getString("CNIC"),
					rs.getString("DOB"),
					rs.getString("phone"),
					rs.getString("email"),
					rs.getString("address")
			);
		}
		rs.close();
		pstmt.close();
		return result;
	}

	public void updateClientInfo(Connection conn, String clientId, String phone, String email, String address) throws SQLException {
		String sql = "UPDATE client SET phone = ?, email = ?, address = ? WHERE client_id = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, phone);
		pstmt.setString(2, email);
		pstmt.setString(3, address);
		pstmt.setString(4, clientId);
		pstmt.executeUpdate();
		pstmt.close();
	}

	public int getTotalAccounts(Connection conn, Manager manager) throws SQLException {
		String sql = "SELECT COUNT(*) AS total_accounts FROM bank_account";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();
		int total = 0;
		if (rs.next()) {
			total = rs.getInt("total_accounts");
		}
		rs.close();
		pstmt.close();
		return total;
	}

	public int getTotalEmployees(Connection conn, Manager manager) {
		String sql = "SELECT COUNT(*) AS total_employees FROM employee";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			int total = 0;
			if (rs.next()) {
				total = rs.getInt("total_employees");
			}
			rs.close();
			pstmt.close();
			return total;
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
