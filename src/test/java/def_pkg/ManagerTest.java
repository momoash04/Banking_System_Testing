package def_pkg;

import org.junit.jupiter.api.*;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ManagerTest {
    private Connection conn;
    private Manager manager;
    private Client testClient;

    String url = "jdbc:mysql://localhost:3306/bank_schema";
    String username = "root";
    String password = "";

    @BeforeAll
    void setupConnection() {
        manager = new Manager("Abdelrahman");
        testClient = new Client("Abdelrahman", "Sallam", "Mostafa", "mama", "30410180105717", "18/10/2004", "01129908336", "sallam@gmail.com", "cairo");

        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            fail("Unable to connect to the database. " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void getName() {
        assertEquals("Abdelrahman", manager.getName());
    }

    @Nested
    @Order(2)
    @DisplayName("Create Account Test")
    class create_Account{
        @Test
        @Order(1)
        @DisplayName("Create New Bank Account For New Client")
        void createBankAccount () throws SQLException {
            int result = manager.createAccount(conn, testClient, "Saving");
            assertEquals(0, result);

            Client createdClient = Client.getByCNIC(conn,testClient.getCNIC());
            assertNotNull(createdClient);
            assertEquals("Abdelrahman",createdClient.getFName());
            assertEquals("Sallam",createdClient.getLName());
            assertEquals("Mostafa",createdClient.getFatherName());
            assertEquals("mama",createdClient.getMotherName());
            assertEquals("01129908336",createdClient.getPhone());
            assertEquals("sallam@gmail.com",createdClient.getEmail());



            Bank_Account acc = Bank_Account.getByClientId(conn, testClient.getClientID());
            assertNotNull(acc);

            assertEquals("Saving", acc.getType());
            assertEquals("0", acc.getBalance());
            assertEquals("1",acc.getStatus());

        }

        @Test
        @Order(2)
        @DisplayName("Create Bank Account For Existing Client")
        void createBankAccountForSameClient() throws SQLException {
            int result1 = manager.createAccount(conn, testClient, "Saving");
            assertEquals(0, result1);
            int result2 = manager.createAccount(conn, testClient, "Saving");
            assertEquals(1, result2);
        }

        @Test
        @DisplayName("createAccount throws and rolls back on SQL error")
        void createAccountSqlException() throws SQLException {
            // force conn into bad state

            try ( Connection badConn = DriverManager.getConnection(url, username, password) ) {
                badConn.close();  // now it’s closed
                assertThrows(SQLException.class,
                        () -> manager.createAccount(badConn, testClient, "Saving")
                );
            }

        }
    }





    @Nested
    @Order(3)
    @DisplayName("block and unblock account test")
    class Block_Unblock_Account {
        @Test
        @Order(1)
        @DisplayName("Block an existing account")
        void blockAccount() throws SQLException {
            manager.createAccount(conn, testClient, "Saving");
            Bank_Account acc = Bank_Account.getByClientId(conn, testClient.getClientID());

            assert acc != null;
            int blockResult = manager.blockAccount(conn, acc);
            assertEquals(1, blockResult);

            assertEquals("2", acc.getStatus());
        }

        @Test
        @Order(2)
        @DisplayName("Block a non-existing account")
        void blockNonExistingAccount() throws SQLException {
            Bank_Account acc = new Bank_Account();
            int blockResult = manager.blockAccount(conn, acc);
            assertEquals(2, blockResult);
        }

        @Test
        @Order(3)
        @DisplayName("Block a blocked account")
        void blockedAccount() throws SQLException {
            manager.createAccount(conn, testClient, "Saving");
            Bank_Account acc = Bank_Account.getByClientId(conn, testClient.getClientID());
            assert acc != null;
            int blockResult = manager.blockAccount(conn, acc);
            assertEquals(1, blockResult);
            assertEquals("2", acc.getStatus());

            blockResult = manager.blockAccount(conn, acc);
            assertEquals(0, blockResult);
            assertEquals("2", acc.getStatus());
        }


        @Test
        @Order(4)
        @DisplayName("Block then unblock")
        void UnblockAccount() throws SQLException {
            manager.createAccount(conn, testClient, "Saving");
            Bank_Account acc = Bank_Account.getByClientId(conn, testClient.getClientID());
            assert acc != null;
            int blockResult = manager.blockAccount(conn, acc);
            assertEquals(1, blockResult);
            assertEquals("2", acc.getStatus());

            int unblockResult = manager.unblockAccount(conn, acc);
            assertEquals(1, unblockResult);
            assertEquals("1", acc.getStatus());
        }



        @Test
        @Order(5)
        @DisplayName("Unblock a non-existing account")
        void unblockNonExistingAccount() throws SQLException {
            Bank_Account acc = new Bank_Account(); // Not saved in DB
            int unblockResult = manager.unblockAccount(conn, acc);
            assertEquals(2, unblockResult); // Assuming 2 means "not found"
        }

        @Test
        @Order(6)
        @DisplayName("Unblock an already active account")
        void unblockActiveAccount() throws SQLException {
            manager.createAccount(conn, testClient, "Saving");
            Bank_Account acc = Bank_Account.getByClientId(conn, testClient.getClientID());
            assert acc != null;
            assertEquals("1", acc.getStatus()); // Should be active by default

            int unblockResult = manager.unblockAccount(conn, acc);
            assertEquals(0, unblockResult); // Assuming 0 means "already active"

            Bank_Account refreshed = Bank_Account.getByClientId(conn, testClient.getClientID());
            assert refreshed != null;
            assertEquals("1", refreshed.getStatus());
        }

        @Test
        @DisplayName("blockAccount throws on SQL error")
        void blockAccountSqlException() throws SQLException {

            try ( Connection badConn = DriverManager.getConnection(url, username, password) ) {
                badConn.close();  // now it’s closed
                assertThrows(SQLException.class,
                        () -> manager.blockAccount(badConn, new Bank_Account())
                );
            }

        }

        @Test
        @DisplayName("unblockAccount throws on SQL error")
        void unblockAccountSqlException() throws SQLException {
            try ( Connection badConn = DriverManager.getConnection(url, username, password) ) {
                badConn.close();  // now it’s closed
                assertThrows(SQLException.class,
                        () -> manager.unblockAccount(badConn, new Bank_Account())
                );
            }
        }

    }


    @Nested
    @Order(4)
    @DisplayName("Get Client Info Test")
    class Get_Client_Info {
        @Test
        @Order(1)
        @DisplayName("Get test client info")
        void getClientInfo() throws SQLException {
            manager.createAccount(conn, testClient, "Saving");
            Bank_Account acc = Bank_Account.getByClientId(conn, testClient.getClientID());
            assertNotNull(acc);

            Client result = manager.getClientInfo(conn, acc.getAccountNum());
            assertNotNull(result);
            assertEquals(testClient.getCNIC(), result.getCNIC());
        }

        @Test
        @Order(2)
        @DisplayName("send a non exiting account number")
        void WrongAccountNumber() throws SQLException {
            Client client = manager.getClientInfo(conn, "5563672");
            assertNull(client);
        }
    }

    @Test
    @Order(5)
    @DisplayName("Update Client Info")
    void updateClientInfo() throws SQLException {
        manager.createAccount(conn, testClient, "Saving");
        Bank_Account acc = Bank_Account.getByClientId(conn, testClient.getClientID());

        assert acc != null;
        Client client = manager.getClientInfo(conn, acc.getAccountNum());

        manager.updateClientInfo(conn, client.getClientID(), "01000000000", "updated@email.com", "new address");

        Client updated = manager.getClientInfo(conn, acc.getAccountNum());
        assertEquals("01000000000", updated.getPhone());
        assertEquals("updated@email.com", updated.getEmail());
        assertEquals("new address", updated.getAddress());
    }

    @Test
    @Order(6)
    void getTotalAccounts() throws SQLException {
        int count = manager.getTotalAccounts(conn,manager);
        manager.createAccount(conn, testClient, "Saving");
        assertEquals(count+1, manager.getTotalAccounts(conn,manager));
    }

    @Test
    @Order(7)
    void getTotalEmployees() throws SQLException {
        assertEquals(1, manager.getTotalEmployees(conn,manager));
    }

//    @Nested
//    @Order(9)
//    @DisplayName("White box testing")
//    class White_Box {
//        @Test
//        @Order(1)
//        @DisplayName("database connection cut in get total employees")
//        void closeConnection() throws SQLException {
//            assertThrows(SQLException.class, () -> {
//                conn.close();
//                manager.createAccount(conn, testClient, "Saving");
//            });
//        }
//
//
//
//    }

    @Test
    @DisplayName("getTotalEmployees wraps SQLException into RuntimeException")
    void getTotalEmployeesRuntimeException() throws SQLException {

        try ( Connection badConn = DriverManager.getConnection(url, username, password) ) {
            badConn.close();  // now it’s closed
            assertThrows(RuntimeException.class,
                    () -> manager.getTotalEmployees(badConn, manager)
            );
        }

    }


////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Nested
    @Order(8)
    @DisplayName("Exceptional Rollback & Error Paths")
    class ExceptionalPaths {

        @Test
        @DisplayName("createAccount rolls back on SQL error (no partial insert)")
        void createAccountRollback() throws SQLException {
            // open+close a throwaway conn to cause the error
            try ( Connection badConn = DriverManager.getConnection(url, username, password) ) {
                badConn.setAutoCommit(false);
                badConn.close();

                // attempt to create → SQLException
                assertThrows(SQLException.class,
                        () -> manager.createAccount(badConn, testClient, "Saving")
                );

                // on real conn, verify no client or account exists
                assertNull(Client.getByCNIC(conn, testClient.getCNIC()));
            }
        }

        @Test
        @DisplayName("getClientInfo throws on SQL error")
        void getClientInfoSqlException() throws SQLException {
            try ( Connection badConn = DriverManager.getConnection(url, username, password) ) {
                badConn.close();
                assertThrows(SQLException.class,
                        () -> manager.getClientInfo(badConn, "12345")
                );
            }
        }

        @Test
        @DisplayName("updateClientInfo throws on SQL error")
        void updateClientInfoSqlException() throws SQLException {
            // need an existing client so UPDATE SQL compiles—but we’re using closed conn anyway
            manager.createAccount(conn, testClient, "Saving");
            Bank_Account acc = Bank_Account.getByClientId(conn, testClient.getClientID());
            assertNotNull(acc);

            try ( Connection badConn = DriverManager.getConnection(url, username, password) ) {
                badConn.close();
                assertThrows(SQLException.class,
                        () -> manager.updateClientInfo(badConn, testClient.getClientID(),
                                "000", "e@e.com", "addr")
                );
            }
        }

        @Test
        @DisplayName("getTotalAccounts throws on SQL error")
        void getTotalAccountsSqlException() throws SQLException {
            try ( Connection badConn = DriverManager.getConnection(url, username, password) ) {
                badConn.close();
                assertThrows(SQLException.class,
                        () -> manager.getTotalAccounts(badConn, manager)
                );
            }
        }
    }







    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            String sql = "DELETE FROM bank_account where client_id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, testClient.getClientID());
                pstmt.executeUpdate();
            }
            String sql2 = "DELETE FROM client where client_id=?";
            try (PreparedStatement pstmt2 = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS)) {
                pstmt2.setString(1, testClient.getClientID());
                pstmt2.executeUpdate();
            }
        }

    }

}





