package def_pkg;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClientBasicTest {
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
    @AfterAll
    static void cleanUp() throws SQLException {


    }

}