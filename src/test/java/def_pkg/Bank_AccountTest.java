package def_pkg;

import com.mysql.cj.log.Log;
import org.junit.jupiter.api.*;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Bank_AccountTest {

    private Connection conn;
    private Manager TestManager;
    private Client TestClient;

    @BeforeEach
    public void create_Bank_Account() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/bank_schema";
        String username = "root";
        String password = "";
        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection established successfully!");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to connect to the database. " + e.getMessage());
        }

        TestManager = new Manager("");
        TestClient = new Client("Test_f_name", "Test_l_name", "Test_father_name",
                "Test_mother_name", "12345", "1/2/2000", "01000000000",
                "test@gmail.com", "testAddress");
        TestManager.createAccount(conn, TestClient, "Current");
    }

    @Nested
    class getAccountBy_Tests {

        @Test
        @Order(1)
        void getByValidClientId() throws SQLException {
            Bank_Account bank_accountTest = Bank_Account.getByClientId(conn, TestClient.getClientID());
            assertNotNull(bank_accountTest);

            assertEquals(Bank_Account.getByClientId(conn, TestClient.getClientID()).getAccountNum(),
                    bank_accountTest.getAccountNum());
            assertEquals(TestClient.getClientID(), bank_accountTest.getClientId());
            assertEquals(null, bank_accountTest.getLoginId());
            assertEquals("Current", bank_accountTest.getType());
            assertEquals("0", bank_accountTest.getBalance());
            assertEquals("1", bank_accountTest.getStatus());
        }

        @Test
        void getByInvalidClientId() throws SQLException {
            Bank_Account bank_accountTest = Bank_Account.getByClientId(conn, "0");

            assertNull(bank_accountTest);
        }

        @Test
        @Order(2)  //Must run after we have successfully tested getByClientId function
        void getByValidAccountNumber() throws SQLException {
            Bank_Account bank_accountTest;
            bank_accountTest = Bank_Account.getByAccountNumber
                    (conn, Bank_Account.getByClientId(conn, TestClient.getClientID()).getAccountNum());
            assertNotNull(bank_accountTest);

            assertEquals(Bank_Account.getByClientId(conn, TestClient.getClientID()).getAccountNum(),
                    bank_accountTest.getAccountNum());
            assertEquals(TestClient.getClientID(), bank_accountTest.getClientId());
            assertEquals(null, bank_accountTest.getLoginId());
            assertEquals("Current", bank_accountTest.getType());
            assertEquals("0", bank_accountTest.getBalance());
            assertEquals("1", bank_accountTest.getStatus());
        }

        @Test
        void getByInvalidAccountNumber() throws SQLException {
            Bank_Account bank_accountTest;
            bank_accountTest = Bank_Account.getByAccountNumber
                    (conn, "0");

            assertNull(bank_accountTest);
        }
    }

    @Nested
    class BalanceTests {

        @Test
        @Order(2) //Must run after we have successfully tested getByClientId function
        void updateBalance() throws SQLException {
            Bank_Account bank_accountTest = Bank_Account.getByClientId(conn, TestClient.getClientID());
            assertNotNull(bank_accountTest);
            Client SenderClient = Client.getById(conn, "10000");
            assertNotNull(SenderClient);

            assertEquals("0", bank_accountTest.getBalance());
            SenderClient.transferMoney(conn, bank_accountTest.getAccountNum(), 100);
            assertEquals("0", bank_accountTest.getBalance());
            bank_accountTest.updateBalance(conn);
            assertEquals("100", bank_accountTest.getBalance());

            // Reset the balance back to original owner after test
            TestClient.transferMoney(conn, Client.getAccNumByCNIC(conn,SenderClient.getCNIC()), 100);
        }

        @Test
        void updateBalance_InvalidAccount() throws SQLException {
            Bank_Account bank_accountTest = new Bank_Account();

            bank_accountTest.updateBalance(conn);
            assertNull(bank_accountTest.getBalance());
        }
    }

    @Nested
    class LoginIdTests {

        @Test
        @Order(2) //Must run after we have successfully tested getByClientId function
        void getByValidLoginId() throws SQLException {
            Bank_Account bank_accountTest;
            bank_accountTest = Bank_Account.getByClientId(conn, TestClient.getClientID());
            Login_Account.signUp(conn, "TestUser", "TestPass", "TestPass", bank_accountTest.getAccountNum());
            bank_accountTest = Bank_Account.getByClientId(conn, TestClient.getClientID());
            bank_accountTest = Bank_Account.getByLoginId(conn, bank_accountTest.getLoginId());

            assertEquals(Bank_Account.getByClientId(conn, TestClient.getClientID()).getAccountNum(),
                    bank_accountTest.getAccountNum());
            assertEquals(TestClient.getClientID(), bank_accountTest.getClientId());
            assertEquals(Login_Account.getByUsername(conn, "TestUser").getLoginId(), bank_accountTest.getLoginId());
            assertEquals("Current", bank_accountTest.getType());
            assertEquals("0", bank_accountTest.getBalance());
            assertEquals("1", bank_accountTest.getStatus());
        }

        @Test
        @Order(2) //Must run after we have successfully tested getByClientId function
        void getByInvalidLoginId() throws SQLException {
            Bank_Account bank_accountTest;
            bank_accountTest = Bank_Account.getByClientId(conn, TestClient.getClientID());
            Login_Account.signUp(conn, "TestUser", "TestPass", "TestPass", bank_accountTest.getAccountNum());
            bank_accountTest = Bank_Account.getByClientId(conn, TestClient.getClientID());
            bank_accountTest = Bank_Account.getByLoginId(conn, "0");

            assertNull(bank_accountTest);
        }

    }

    @AfterEach
    void tearDown() throws SQLException {
        String deleteLoginId = Bank_Account.getByClientId(conn, TestClient.getClientID()).getLoginId();
        String sql = "DELETE FROM bank_account where client_id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, TestClient.getClientID());
            pstmt.executeUpdate();
        }
        String sql2 = "DELETE FROM login_account where login_id=?";
        try (PreparedStatement pstmt2 = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS)) {
            pstmt2.setString(1, deleteLoginId);
            pstmt2.executeUpdate();
        }
        String sql3 = "DELETE FROM client where client_id=?";
        try (PreparedStatement pstmt3 = conn.prepareStatement(sql3, Statement.RETURN_GENERATED_KEYS)) {
            pstmt3.setString(1, TestClient.getClientID());
            pstmt3.executeUpdate();
        }
    }

}
