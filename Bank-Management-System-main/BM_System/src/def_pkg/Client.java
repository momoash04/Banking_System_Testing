package def_pkg;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Client {
	private String client_id;
	private String f_name;
	private String l_name;
	private String father_name;
	private String mother_name;
	private String CNIC;
	private String DOB;
	private String phone;
	private String email;
	private String address;

	public Client() {
		client_id = "";
		f_name = "";
		l_name = "";
		father_name = "";
		mother_name = "";
		CNIC = "";
		DOB = "";
		phone = "";
		email = "";
		address = "";
	}

	public Client(String f_name, String l_name, String father_name, String mother_name,
				  String CNIC, String DOB, String phone, String email, String address) {
		this.client_id = "";
		this.f_name = f_name;
		this.l_name = l_name;
		this.father_name = father_name;
		this.mother_name = mother_name;
		this.CNIC = CNIC;
		this.DOB = DOB;
		this.phone = phone;
		this.email = email;
		this.address = address;
	}

	public Client(String client_id, String f_name, String l_name, String father_name,
				  String mother_name, String CNIC, String DOB, String phone,
				  String email, String address) {
		this.client_id = client_id;
		this.f_name = f_name;
		this.l_name = l_name;
		this.father_name = father_name;
		this.mother_name = mother_name;
		this.CNIC = CNIC;
		this.DOB = DOB;
		this.phone = phone;
		this.email = email;
		this.address = address;
	}

	public String getClientID() { return client_id; }
	public String getFName() { return f_name; }
	public String getLName() { return l_name; }
	public String getFatherName() { return father_name; }
	public String getMotherName() { return mother_name; }
	public String getCNIC() { return CNIC; }
	public String getDOB() { return DOB; }
	public String getPhone() { return phone; }
	public String getEmail() { return email; }
	public String getAddress() { return address; }


	public void save(Connection conn) throws SQLException {
		String sql = "INSERT INTO client (f_name, l_name, father_name, mother_name, CNIC, DOB, phone, email, address) "
				+ "VALUES (?, ?, ?, ?, ?, STR_TO_DATE(?, '%d/%m/%Y'), ?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, f_name);
			pstmt.setString(2, l_name);
			pstmt.setString(3, father_name);
			pstmt.setString(4, mother_name);
			pstmt.setString(5, CNIC);
			pstmt.setString(6, DOB);
			pstmt.setString(7, phone);
			pstmt.setString(8, email);
			pstmt.setString(9, address);
			pstmt.executeUpdate();

			try (ResultSet rs = pstmt.getGeneratedKeys()) {
				if (rs.next()) {
					client_id = rs.getString(1);
				}
			}
		}
	}

	public static Client getById(Connection conn, String clientId) {
		String sql = "SELECT * FROM client WHERE client_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, clientId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return new Client(
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
			}
		} catch (SQLException e) {
			System.err.println("SQL Error in getById: " + e.getMessage());

		}
		return null;
	}


	public int transferMoney(Connection conn, String recvAccNum, int amount) throws SQLException {
		try {
			conn.setAutoCommit(false);
			Bank_Account senderAccount = Bank_Account.getByClientId(conn, client_id);
			Bank_Account receiverAccount = Bank_Account.getByAccountNumber(conn, recvAccNum);

			if (senderAccount == null || receiverAccount == null) {
				return 1; // Account not found
			}

			if (Integer.parseInt(senderAccount.getBalance()) < amount) {
				return 2; // Insufficient balance
			}

			// Update sender balance
			String updateSender = "UPDATE bank_account SET balance = balance - ? WHERE acc_num = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(updateSender)) {
				pstmt.setInt(1, amount);
				pstmt.setString(2, senderAccount.getAccountNum());
				pstmt.executeUpdate();
			}

			// Update receiver balance
			String updateReceiver = "UPDATE bank_account SET balance = balance + ? WHERE acc_num = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(updateReceiver)) {
				pstmt.setInt(1, amount);
				pstmt.setString(2, recvAccNum);
				pstmt.executeUpdate();
			}

			conn.commit();
			return 0;
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} finally {
			conn.setAutoCommit(true);
		}
	}

	public int depositMoney(Connection conn, String accountNumber, double amount) throws SQLException {
		try {
			conn.setAutoCommit(false);

			// 1. Validate the account exists
			Bank_Account account = Bank_Account.getByAccountNumber(conn, accountNumber);
			if (account == null) {
				return 1; // Account not found
			}

			// 2. Validate the deposit amount is positive
			if (amount <= 0) {
				return 2; // Invalid deposit amount
			}


			// 3. Update the account balance
			String updateBalance = "UPDATE bank_account SET balance = balance + ? WHERE acc_num = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(updateBalance)) {
				pstmt.setDouble(1, amount);
				pstmt.setString(2, accountNumber);
				pstmt.executeUpdate();
			}

			conn.commit();
			return 0; // Success
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} finally {
			conn.setAutoCommit(true);
		}
	}

	public int WithdrawMoney(Connection conn, String accountNumber, double amount) throws SQLException {
		try {
			conn.setAutoCommit(false);

			// 1. Validate the account exists
			Bank_Account account = Bank_Account.getByAccountNumber(conn, accountNumber);
			if (account == null) {
				return 1; // Account not found
			}

			// 2. Validate the withdraw amount is positive
			if (amount <= 0) {
				return 2; // Invalid withdraw amount
			}
			int currentbalance= Integer.parseInt(account.getBalance());
			if(currentbalance<amount){
				return 3; //Insufficient funds
			}


			// 3. Update the account balance
			String updateBalance = "UPDATE bank_account SET balance = balance - ? WHERE acc_num = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(updateBalance)) {
				pstmt.setDouble(1, amount);
				pstmt.setString(2, accountNumber);
				pstmt.executeUpdate();
			}

			conn.commit();
			return 0; // Success
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} finally {
			conn.setAutoCommit(true);
		}
	}

	public static String getAccNumByCNIC(Connection conn, String CNIC) throws SQLException {
		String sql = "SELECT ba.acc_num FROM bank_account ba "
				+ "JOIN client c ON ba.client_id = c.client_id "
				+ "WHERE c.CNIC = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, CNIC);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() ? rs.getString("acc_num") : "";
			}
		}
	}


	public static Client getByCNIC(Connection conn, String CNIC) throws SQLException {
		String sql = "SELECT * FROM client WHERE CNIC = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, CNIC);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return new Client(
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
			}
		}
		return null;
	}


}