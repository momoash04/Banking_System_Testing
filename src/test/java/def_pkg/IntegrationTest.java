package def_pkg;

import javafx.util.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegrationTest {
    private Connection conn;
    Client client1;
    Login_Account Login;
    @BeforeEach
    void setUp() {
        conn = establishConnection();


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

        String sql = "DELETE FROM bank_account where client_id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, client1.getClientID());
            pstmt.executeUpdate();
        }

        String sql2 = "DELETE FROM client where client_id=?";
        try (PreparedStatement pstmt2 = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS)) {
            pstmt2.setString(1, client1.getClientID());
            pstmt2.executeUpdate();
        }

        String sql3 = "DELETE FROM login_account where username=?";
        try (PreparedStatement pstmt3 = conn.prepareStatement(sql3, Statement.RETURN_GENERATED_KEYS)) {
            pstmt3.setString(1, Login.getUsername());
            pstmt3.executeUpdate();
        }



        if (conn != null && !conn.isClosed()) {
            conn.close();
        }

    }
    //scenario 1
    @Test
    @Order(1)
    void Test1() throws SQLException {
        Login_Account manager_login_acc = Login_Account.signIn(conn, "Abdelrahman20", "Sallam").getKey();
        assertEquals("60000", manager_login_acc.getLoginId());
        assertEquals("Abdelrahman20", manager_login_acc.getUsername());
        assertEquals("Manager", manager_login_acc.getType());
        Manager manager = new Manager(Login_Account.getEmployeeName(conn, manager_login_acc.getLoginId()));

        client1 = new Client("Andrew", "Basilly", "Ramy",
                "Mom", "30501316106716", "30/06/2004", "01026065412",
                "andresramesbaseles@gmail.com", "Almaza");
        manager.createAccount(conn, client1, "Current");
        Client fetchClient = Client.getByCNIC(conn, "30501316106716");
        assertEquals("Andrew",fetchClient.getFName());
        assertEquals("Almaza",fetchClient.getAddress());

        Bank_Account fetchBankAccount = Bank_Account.getByClientId(conn,fetchClient.getClientID());

        // scenariooo 2
        Login_Account.signUp(conn, "besela", "dodo", "dodo", fetchBankAccount.getAccountNum());
        Pair<Login_Account,Integer> SignInAcc = Login_Account.signIn(conn, "besela", "dodo");
        Login = SignInAcc.getKey();
        assert Login != null;
        int Status = SignInAcc.getValue();

        assertEquals("besela",Login.getUsername());
        assertEquals(1,Status);

        client1.depositMoney(conn,fetchBankAccount.getAccountNum(),2000);
        fetchBankAccount = Bank_Account.getByClientId(conn,fetchClient.getClientID());
        assertEquals("2000",fetchBankAccount.getBalance());
        client1.transferMoney(conn,"500000",500);
        fetchBankAccount = Bank_Account.getByClientId(conn,fetchClient.getClientID());
        assertEquals("1500",fetchBankAccount.getBalance());



        //manager blocks the account
        int blockResult = manager.blockAccount(conn, fetchBankAccount);
        assertEquals(1,blockResult);
        assertEquals("2",fetchBankAccount.getStatus());

        //client is trying to login again
        SignInAcc = Login_Account.signIn(conn,"besela", "dodo");
        assertEquals(2, SignInAcc.getValue());

    }


}

