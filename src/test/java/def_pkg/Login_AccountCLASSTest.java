package def_pkg;
import javafx.util.Pair;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;


class Login_AccountCLASStest {

    static Connection conn;


    @BeforeAll
    static void connectToDB() {
        try {
            // Replace with your actual DB credentials
            String url = "jdbc:mysql://localhost:3306/bank_schema";
            String user = "root";
            String password = "";
            conn = DriverManager.getConnection(url, user, password);
            assertNotNull(conn);
        } catch (SQLException e) {
            fail("Database connection failed: " + e.getMessage());
        }
    }

    @AfterAll
    static void closeDB() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    private static Bank_Account bankAccount;
    private static Client client;
    private static Client clientB;
    Manager manager;

    @BeforeEach
    public void setUp() throws SQLException {
        // Setup test data
        client = new Client("John", "Doe", "Michael", "Sarah",
                "12345-6789012-3", "1/1/2004", "0123456789",
                "john@example.com", "123 Street");
        clientB = new Client("mablook", "metblk", "mablook", "mablooka",
                "1234", "1/1/2004", "0123456789",
                "jdj.com", "123 Street");
        manager = new Manager("sallam");

        bankAccount = new Bank_Account("5", "50", null, "Saving",
                "1000", "Active", "2024-01-01");
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        // Clean up references
        bankAccount = null;
        client = null;

    }

    @Test
    public void testAccountCreation() {
        // Sample test case
        assert bankAccount != null;
    }

    @DisplayName("loginid getter")
    @Test
    void getLoginId() {
        Login_Account account = new Login_Account("123", "medhat", "pass", "C");
        assertEquals("123", account.getLoginId());
    }

    @DisplayName("gettype getter")
    @Test
    void getType() {
        Login_Account client = new Login_Account("101", "client test", "pass", "C");
        Login_Account manager = new Login_Account("102", "manager test", "pass", "M");
        Login_Account unknown = new Login_Account("103", "unknown test", "pass", "X");

        assertEquals("Client", client.getType());
        assertEquals("Manager", manager.getType());
        assertEquals("Unknown", unknown.getType());
    }

    @DisplayName("signup with valid client accNm")
    @Test
    void signUpSuccess() throws SQLException {
//        try {
        manager.createAccount(conn,client,"Saving");
        Bank_Account acc = Bank_Account.getByClientId(conn,client.getClientID());


        String username = "mohamed";
        String pass = "2004";
        assert acc != null;
        String accNum = acc.getAccountNum(); // existing bank_account

        int result = Login_Account.signUp(conn, username, pass, pass, accNum);

        // Assert success (0) for valid signup
        assertEquals(0, result, "Signup should succeed with a valid account number");


        String sql = "DELETE FROM bank_account where client_id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, client.getClientID());
            pstmt.executeUpdate();
        }

        String sql2 = "DELETE FROM client where client_id=?";
        try (PreparedStatement pstmt2 = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS)) {
            pstmt2.setString(1, client.getClientID());
            pstmt2.executeUpdate();
        }

        String sql3 = "DELETE FROM login_account where username=?";
        try (PreparedStatement pstmt3 = conn.prepareStatement(sql3, Statement.RETURN_GENERATED_KEYS)) {
            pstmt3.setString(1, username);
            pstmt3.executeUpdate();
        }

    }

    @Test
    void signInSuccess() {
        try {
            Pair<Login_Account, Integer> result = Login_Account.signIn(conn, "MohAshraf", "Ashraf");

            assertNotNull(result.getKey(), "Expected non-null account for valid credentials");
            assertEquals("MohAshraf", result.getKey().getUsername(), "Username does not match");
            assertEquals(1, result.getValue(), "Expected login status value 1 for successful login");
        } catch (SQLException e) {
            fail("SQLException occurred: " + e.getMessage());
        }
    }

    @DisplayName("verify Account Success")
    @Test
    void verifyAccount_success() {
        try {
            boolean result = Login_Account.verifyAccount(conn, "500000", "7853257");
            assertTrue(result); // assuming correct data
        } catch (SQLException e) {
            fail("SQLException occurred: " + e.getMessage());
        }
    }

    @DisplayName("Get employee name Success")
    @Test
    void getEmployeeNameSuccess() {
        try {
            String loginId = "60000"; //  existing login_id from the employee table
            String name = Login_Account.getEmployeeName(conn, loginId);
            assertNotNull(name);
            assertFalse(name.isEmpty());
        } catch (SQLException e) {
            fail("SQLException occurred: " + e.getMessage());
        }
    }

    @DisplayName("getByUsername - existing user")
    @Test
    void getByUsernameExists() {
        try {
            // Assume this user exists in your DB with a known login_id and type
            Login_Account account = Login_Account.getByUsername(conn, "MohAshraf");

            assertNotNull(account, "Expected a valid Login_Account object");
            assertEquals("MohAshraf", account.getUsername(), "Username mismatch");
            assertNotNull(account.getType(), "Type should not be null");
        } catch (SQLException e) {
            fail("SQLException occurred: " + e.getMessage());
        }
    }


}

