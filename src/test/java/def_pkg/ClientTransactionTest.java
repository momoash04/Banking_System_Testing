package def_pkg;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class ClientTransactionTest {
    private Connection conn;
    private Client c;



    @BeforeEach
    void setUp() throws SQLException {
        conn = establishConnection();

        c = Client.getByCNIC(conn,"0978912");
    }

    private Connection establishConnection() {
        String url = "jdbc:mysql://localhost:3306/bank_schema";
        String username = "root";
        String password = "";

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connection established successfully!");
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to connect to the database. " + e.getMessage());
        }
    }
    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    @Nested
    @Order(1)
    @DisplayName("Deposit Money Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DepositTests {
        @Test
        @DisplayName("Deposit successful")
        @Order(1)
        void testSuccessfulDeposit() throws SQLException {
            int result = c.depositMoney(conn, "500001", 1000.0);
            assertEquals(0, result, "Expected successful deposit (return 0)");
            Bank_Account client_Acc = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(21000, Integer.parseInt(client_Acc.getBalance()));
        }

        @Test
        @DisplayName("Deposit into non-existing account")
        @Order(2)
        void testDepositToNonExistingAccount() throws SQLException {
            int result = c.depositMoney(conn, "999999", 1000.0); // fake acc_num
            assertEquals(1, result, "Expected account not found (return 1)");
            Bank_Account client_Acc = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(21000, Integer.parseInt(client_Acc.getBalance()));
        }

        @Test
        @DisplayName("Deposit with negative amount")
        @Order(3)
        void testNegativeDeposit() throws SQLException {
            int result = c.depositMoney(conn, "500001", -500.0);
            assertEquals(2, result, "Expected invalid amount error (return 2)");
            Bank_Account client_Acc = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(21000, Integer.parseInt(client_Acc.getBalance()));
        }

        @Test
        @DisplayName("Deposit with zero amount")
        @Order(4)
        void testZeroDeposit() throws SQLException {
            int result = c.depositMoney(conn, "500001", 0.0);
            assertEquals(2, result, "Expected invalid amount error (return 2)");
            Bank_Account client_Acc = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(21000, Integer.parseInt(client_Acc.getBalance()));
        }

    }


    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class WithdrawTests {
        @Test
        @Order(1)
        void testSuccessfulWithdraw() throws SQLException {

            int result = c.WithdrawMoney(conn, "500001", 1000);
            assertEquals(0, result);

            Bank_Account updatedAccount = Bank_Account.getByAccountNumber(conn, "500001");

            assertEquals(20000, Double.parseDouble(updatedAccount.getBalance()));


        }


        @Test
        @Order(2)
        void testWithdrawFromNonExistingAccount() throws SQLException {
            Client client = new Client();
            int result = client.WithdrawMoney(conn, "999999", 100);
            assertEquals(1, result);
            Bank_Account updatedAccount = Bank_Account.getByAccountNumber(conn, "500001");

            assertEquals(20000, Double.parseDouble(updatedAccount.getBalance()));
        }

        @Test
        @Order(3)
        void testWithdrawNegativeAmount() throws SQLException {
            Client client = new Client();
            int result = client.WithdrawMoney(conn, "500001" , -500);
            assertEquals(2, result);
            Bank_Account updatedAccount = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(20000, Double.parseDouble(updatedAccount.getBalance()));
        }

        @Test
        @Order(4)
        void testWithdrawMoreThanBalance() throws SQLException {
            Client client = new Client();
            int result = client.WithdrawMoney(conn, "500001", 1000000); // More than current
            assertEquals(3, result);
            Bank_Account updatedAccount = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(20000, Double.parseDouble(updatedAccount.getBalance()));
        }
    }
    @Nested
    @Order(3)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class TransferTests {

        void ResetBalances() throws SQLException {
            c.depositMoney(conn,"500001",300);
            Client recieverc = Client.getById(conn,"10002");
            recieverc.WithdrawMoney(conn,"500002",300);
        }
        @Test
        @Order(1)
        void testSuccessfulTransfer() throws SQLException {
            int result = c.transferMoney(conn, "500002", 300);
            assertEquals(0, result);

            Bank_Account updatedSender = Bank_Account.getByAccountNumber(conn, "500001");
            Bank_Account updatedReceiver = Bank_Account.getByAccountNumber(conn, "500002");

            assertEquals(19700, Integer.parseInt(updatedSender.getBalance()));
            assertEquals(7300, Integer.parseInt(updatedReceiver.getBalance()));
        }

        @Test
        @Order(2)
        void testSenderAccountNotFound() throws SQLException {
            Client fakeSender = new Client( "Fake", "Test", "Father", "Mother", "fake_cnic", "1992-01-01", "1122334455", "fake@example.com", "Fake Address");

            int result = fakeSender.transferMoney(conn, "500002", 100);
            Bank_Account updatedReceiver = Bank_Account.getByAccountNumber(conn, "500002");
            assertEquals(1, result);
            assertEquals(7300, Integer.parseInt(updatedReceiver.getBalance()));
        }

        @Test
        @Order(3)
        void testReceiverAccountNotFound() throws SQLException {
            int result = c.transferMoney(conn, "500003", 100);
            assertEquals(1, result);
            Bank_Account updatedSender = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(19700, Integer.parseInt(updatedSender.getBalance()));


        }

        @Test
        @Order(4)
        void testInsufficientBalance() throws SQLException {
            int result = c.transferMoney(conn, "500002", 500000); // sender only has 19700
            assertEquals(2, result);
            Bank_Account updatedReceiver = Bank_Account.getByAccountNumber(conn, "500002");
            assertEquals(7300, Integer.parseInt(updatedReceiver.getBalance()));
            Bank_Account updatedSender = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(19700, Integer.parseInt(updatedSender.getBalance()));
            ResetBalances();


        }


    }


}

