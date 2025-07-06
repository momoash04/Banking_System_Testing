package def_pkg;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientWhiteBoxTest {
    private Connection conn;
    private Client c;

    @BeforeEach
    void setUp() {
        conn = establishConnection();

        c = new Client("Omar","Dardir", "Ahmed",
                "Sameha", "30501300106716", "30/01/2005", "0978753",
                "omarahmed3001@gmail.com", "24, Doughnut St, City of Stars, La La Land");
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


    @Test
    @Order(1)
    void getById() throws SQLException {
        Client fetched = Client.getById(conn, "10001");
        assertEquals("Mohammed", fetched.getFName());
        assertEquals("Ahmed", fetched.getLName());
        assertEquals("Bashir", fetched.getFatherName());
        assertEquals("Mum", fetched.getMotherName());
        assertEquals("0978912", fetched.getCNIC());
        assertEquals("2005-01-26", fetched.getDOB());
        assertEquals("01027827193", fetched.getPhone());
        assertEquals("Bashi8@gmail.com", fetched.getEmail());
        assertEquals("In our hearts", fetched.getAddress());

    }
    @Test
    void getByWrongId() throws SQLException {
        Client fetched = Client.getById(conn,"766767");
        assertNull(fetched);

    }
    @Test
    void getByIdSQLException() throws SQLException {
        conn.close(); // Force connection to be invalid

        Client result = Client.getById(conn, "766767");

        assertNull(result); // Since the method catches SQLException and returns null
    }
    @Test
    @Order(2)
    void save() throws SQLException {
        c.save(conn);
        Client fetched = Client.getById(conn, c.getClientID());
        assertEquals("Omar", fetched.getFName());
        assertEquals("Dardir", fetched.getLName());
        assertEquals("Ahmed", fetched.getFatherName());
        assertEquals("Sameha", fetched.getMotherName());
        assertEquals("30501300106716", fetched.getCNIC());
        assertEquals("2005-01-30", fetched.getDOB());
        assertEquals("0978753", fetched.getPhone());
        assertEquals("omarahmed3001@gmail.com", fetched.getEmail());
        assertEquals("24, Doughnut St, City of Stars, La La Land", fetched.getAddress());Client.getById(conn,c.getClientID());



    }

    @Test
    void getAccNumByCNIC() throws SQLException {
        assertEquals("500001", c.getAccNumByCNIC(conn,"0978912"));    }
    @Test
    void getAccNumByWrongCNIC() throws SQLException {
        assertEquals("", c.getAccNumByCNIC(conn,"0978912222"));    }

    @Test
    void getByCNIC() throws SQLException {
        Client result = Client.getByCNIC(conn, "0978912");
        assertEquals("Mohammed", result.getFName());
        assertEquals("Ahmed", result.getLName());
        assertEquals("Bashir", result.getFatherName());
        assertEquals("Mum", result.getMotherName());
        assertEquals("0978912", result.getCNIC());
        assertEquals("2005-01-26", result.getDOB());
        assertEquals("01027827193", result.getPhone());
        assertEquals("Bashi8@gmail.com", result.getEmail());
        assertEquals("In our hearts", result.getAddress());
    }
    @Test
    void getByInvalidCNICReturnsNull() throws SQLException {
        Client result = Client.getByCNIC(conn, "123");
        assertNull(result, "Expected null when CNIC does not exist in database");
    }


        @Test

        @Order(3)
        void testSuccessfulDeposit() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");

            int result = c.depositMoney(conn, "500001", 1000.0);
            assertEquals(0, result, "Expected successful deposit (return 0)");
            Bank_Account client_Acc = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(21000, Integer.parseInt(client_Acc.getBalance()));
        }

        @Test
        @DisplayName("Deposit into non-existing account")
        @Order(4)
        void testDepositToNonExistingAccount() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            int result = c.depositMoney(conn, "999999", 1000.0); // fake acc_num
            assertEquals(1, result, "Expected account not found (return 1)");
            Bank_Account client_Acc = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(21000, Integer.parseInt(client_Acc.getBalance()));
        }

        @Test
        @DisplayName("Deposit with negative amount")
        @Order(5)
        void testNegativeDeposit() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            int result = c.depositMoney(conn, "500001", -500.0);
            assertEquals(2, result, "Expected invalid amount error (return 2)");
            Bank_Account client_Acc = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(21000, Integer.parseInt(client_Acc.getBalance()));
        }

        @Test
        @DisplayName("Deposit with zero amount")
        @Order(6)
        void testZeroDeposit() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            int result = c.depositMoney(conn, "500001", 0.0);
            assertEquals(2, result, "Expected invalid amount error (return 2)");
            Bank_Account client_Acc = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(21000, Integer.parseInt(client_Acc.getBalance()));
        }
        @Test
        void testSQLExceptionRollback() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            assertThrows(SQLException.class, () -> {
                conn.close(); // forcibly close to cause SQLException
                c.depositMoney(conn, "500002", 100);
            });
        }




        @Test
        @Order(7)
        void testSuccessfulWithdraw() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");

            int result = c.WithdrawMoney(conn, "500001", 1000);
            assertEquals(0, result);

            Bank_Account updatedAccount = Bank_Account.getByAccountNumber(conn, "500001");

            assertEquals(20000, Double.parseDouble(updatedAccount.getBalance()));


        }


        @Test
        @Order(8)
        void testWithdrawFromNonExistingAccount() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            Client client = new Client();
            int result = client.WithdrawMoney(conn, "999999", 100);
            assertEquals(1, result);
            Bank_Account updatedAccount = Bank_Account.getByAccountNumber(conn, "500001");

            assertEquals(20000, Double.parseDouble(updatedAccount.getBalance()));
        }

        @Test
        @Order(9)
        void testWithdrawNegativeAmount() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            Client client = new Client();
            int result = client.WithdrawMoney(conn, "500001" , -500);
            assertEquals(2, result);
            Bank_Account updatedAccount = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(20000, Double.parseDouble(updatedAccount.getBalance()));
        }

        @Test
        @Order(10)
        void testWithdrawMoreThanBalance() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            Client client = new Client();
            int result = client.WithdrawMoney(conn, "500001", 1000000); // More than current
            assertEquals(3, result);
            Bank_Account updatedAccount = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(20000, Double.parseDouble(updatedAccount.getBalance()));
        }
        @Test
        void testSQLExceptionRollback2() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            assertThrows(SQLException.class, () -> {
                conn.close(); // forcibly close to cause SQLException
                c.WithdrawMoney(conn, "500002", 100);
            });
        }



        void ResetBalances() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            c.depositMoney(conn,"500001",300);
            Client recieverc = Client.getById(conn,"10002");
            recieverc.WithdrawMoney(conn,"500002",300);
        }
        @Test
        @Order(11)
        void testSuccessfulTransfer() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            int result = c.transferMoney(conn, "500002", 300);
            assertEquals(0, result);

            Bank_Account updatedSender = Bank_Account.getByAccountNumber(conn, "500001");
            Bank_Account updatedReceiver = Bank_Account.getByAccountNumber(conn, "500002");

            assertEquals(19700, Integer.parseInt(updatedSender.getBalance()));
            assertEquals(7300, Integer.parseInt(updatedReceiver.getBalance()));
        }

        @Test
        @Order(12)
        void testSenderAccountNotFound() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            Client fakeSender = new Client( "Fake", "Test", "Father", "Mother", "fake_cnic", "1992-01-01", "1122334455", "fake@example.com", "Fake Address");

            int result = fakeSender.transferMoney(conn, "500002", 100);
            Bank_Account updatedReceiver = Bank_Account.getByAccountNumber(conn, "500002");
            assertEquals(1, result);
            assertEquals(7300, Integer.parseInt(updatedReceiver.getBalance()));
        }

        @Test
        @Order(13)
        void testReceiverAccountNotFound() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            int result = c.transferMoney(conn, "500003", 100);
            assertEquals(1, result);
            Bank_Account updatedSender = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(19700, Integer.parseInt(updatedSender.getBalance()));


        }

        @Test
        @Order(14)
        void testInsufficientBalance() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            int result = c.transferMoney(conn, "500002", 500000); // sender only has 19700
            assertEquals(2, result);
            Bank_Account updatedReceiver = Bank_Account.getByAccountNumber(conn, "500002");
            assertEquals(7300, Integer.parseInt(updatedReceiver.getBalance()));
            Bank_Account updatedSender = Bank_Account.getByAccountNumber(conn, "500001");
            assertEquals(19700, Integer.parseInt(updatedSender.getBalance()));
            ResetBalances();


        }
        @Test
        void testSQLExceptionRollback3() throws SQLException {
            c = Client.getByCNIC(conn,"0978912");
            assertThrows(SQLException.class, () -> {
                conn.close(); // forcibly close to cause SQLException
                c.transferMoney(conn, "500002", 100);
            });
        }



}
