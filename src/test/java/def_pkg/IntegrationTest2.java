package def_pkg;

import javafx.util.Pair;
import org.junit.jupiter.api.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BankSystemIntegrationTest {
    private static Connection conn;
    private Manager testManager;
    private static Client testClient;



    @BeforeEach
    void setUp() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/bank_schema";
        String username = "root";
        String password = "";
        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection established successfully!");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to connect to the database. " + e.getMessage());
        }
        // Create base test client
        testManager = new Manager();
        testClient = new Client("Test_f_name", "Test_l_name", "Test_father_name",
                "Test_mother_name", "12345", "1/2/2000", "01000000000",
                "test@gmail.com", "testAddress");
        try (PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE bank_account SET status = 1 WHERE acc_num = '500001'")) {
            pstmt.executeUpdate();
        }
    }

    @Test
    @Order(1)
    void testManagerOperations() throws SQLException {
        // 1. Test invalid login
        Pair<Login_Account, Integer> loginResult = Login_Account.signIn(conn, "wrongUser", "wrongPass");
        assertNull(loginResult.getKey());
        assertEquals(0, loginResult.getValue());

        // 2. Valid manager login
        loginResult = Login_Account.signIn(conn, "Abdelrahman20", "Sallam");
        assertNotNull(loginResult.getKey());
        assertEquals(1, (int) loginResult.getValue());
        assertEquals("Manager", loginResult.getKey().getType());

        // 3. Account created
        int createResult = testManager.createAccount(conn, testClient, "Savings");
        assertEquals(0, createResult);

        // 4. Account already exists
        int failedResult = testManager.createAccount(conn, testClient, "Savings");
        assertEquals(1, failedResult);

        Bank_Account nullAcc = new Bank_Account();
        int nullResult = testManager.blockAccount(conn, nullAcc);
        assertEquals(2, nullResult);

        // 4. Block account operations
        Bank_Account acc500001 = Bank_Account.getByAccountNumber(conn, "500001");
        int blockResult = testManager.blockAccount(conn, acc500001);
        assertEquals(1, blockResult);

        int reBlockResult = testManager.blockAccount(conn, acc500001);
        assertEquals(0, reBlockResult);
    }

    @Test
    @Order(2)
    void testClientManagement() throws SQLException {
        // Update client info
        Client nullClient = testManager.getClientInfo(conn, null);
        assertNull(nullClient);

        Client client = testManager.getClientInfo(conn, "500002");
        assertNotNull(client);
        testManager.updateClientInfo(conn, client.getClientID(), "9876543", "new@email.com", "New Address");

        Client updatedClient = testManager.getClientInfo(conn, "500002");
        assertEquals("9876543", updatedClient.getPhone());
    }

    @Test
    @Order(3)
    void testAccountOperations() throws SQLException {
        // Signup and login
        int signupResult = Login_Account.signUp(conn, "new_user", "pass123", "pass123", Client.getAccNumByCNIC(conn, "12345"));
        assertEquals(0, signupResult);

        Pair<Login_Account, Integer> loginResult = Login_Account.signIn(conn, "new_user", "pass123");
        assertNotNull(loginResult.getKey());
        assertEquals(1, loginResult.getValue());

        // Money operations
        Client client = Client.getByCNIC(conn, "12345");
        int depositResult = client.depositMoney(conn, "500000", 5000);
        assertEquals(0, depositResult);

        int withdrawResult = client.WithdrawMoney(conn, "500000", 5000);
        assertEquals(0, withdrawResult);
    }

    @Test
    @Order(4)
    void testAccountStatusTransitions() throws SQLException {
        // Block/unblock workflow
        Bank_Account acc500001 = Bank_Account.getByAccountNumber(conn, "500001");
        testManager.blockAccount(conn, acc500001);

        Pair<Login_Account, Integer> loginResult = Login_Account.signIn(conn, "MBesheer", "Besheer");
        assertEquals(2, loginResult.getValue());

        int unblockResult = testManager.unblockAccount(conn, acc500001);
        assertEquals(1, unblockResult);

        Pair<Login_Account, Integer> unblockLoginResult = Login_Account.signIn(conn, "Mbesheer", "Besheer");
        assertNotNull(unblockLoginResult.getKey());
        assertEquals(1, unblockLoginResult.getValue());
    }

    @AfterAll
    static void tearDown() throws SQLException {
        // 1) disable foreign key checks so we can delete in any order
        try (Statement s = conn.createStatement()) {
            s.execute("SET FOREIGN_KEY_CHECKS = 0");
        }

        // 2) delete any leftover rows from prior runs
        //    - bank_account for clients using the test CNIC
        try (PreparedStatement p = conn.prepareStatement(
                "DELETE b FROM bank_account b JOIN client c ON b.client_id=c.client_id WHERE c.cnic=?")) {
            p.setString(1, "12345");
            p.executeUpdate();
        }
        //    - login_account for our test username
        try (PreparedStatement p = conn.prepareStatement(
                "DELETE FROM login_account WHERE username IN ('new_user')")) {
            p.executeUpdate();
        }
        //    - client for our test CNIC
        try (PreparedStatement p = conn.prepareStatement(
                "DELETE FROM client WHERE cnic = ?")) {
            p.setString(1, "12345");
            p.executeUpdate();
        }

        // 4) re-enable foreign key checks
        try (Statement s = conn.createStatement()) {
            s.execute("SET FOREIGN_KEY_CHECKS = 1");
        }

        conn.close();
    }

}