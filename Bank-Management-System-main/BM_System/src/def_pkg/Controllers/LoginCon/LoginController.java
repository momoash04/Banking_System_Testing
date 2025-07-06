package def_pkg.Controllers.LoginCon;
import def_pkg.*;
import def_pkg.Controllers.ClientCon.ClientMenuController;
import def_pkg.Controllers.ManagerCon.ManagerDashboardController;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javafx.util.Pair;

public class LoginController {
    @FXML public Label loginerrorlabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;
    @FXML private Pane loginPanel;


    @FXML
    private void initialize() {
        // Hover effects for buttons
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #005f8c; -fx-text-fill: white; -fx-background-radius: 5;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #0077b6; -fx-text-fill: white; -fx-background-radius: 5;"));

        signupButton.setOnMouseEntered(e -> signupButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #0077b6; -fx-underline: true;"));
        signupButton.setOnMouseExited(e -> signupButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #0077b6; -fx-underline: false;"));
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if(username.isEmpty() || pass.isEmpty()){
            shakePanel(loginPanel);
            loginerrorlabel.setText("Please enter all fields!");
            loginerrorlabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-background-radius: 5;");
            loginerrorlabel.setVisible(true);
            showError("Please enter all fields!");
            return;
        }

        try (DB_handler db = new DB_handler()) {
            Connection conn = db.getConnection();
            Pair<Login_Account,Integer> loggedInUser = Login_Account.signIn(conn,
                    username,
                    pass
            );
            Login_Account loginAccount = loggedInUser.getKey();
            int blockedStatus = loggedInUser.getValue();

            if (loginAccount != null) {
                String userType = loginAccount.getType();
                switch(userType) {
                    case "Client":
                        Bank_Account account = Bank_Account.getByLoginId(conn, loginAccount.getLoginId());
                        System.out.println("Searching for login ID: " + loginAccount.getLoginId());
                        Client client = Client.getById(conn, account.getClientId());
                        if (blockedStatus == 1) {
                            System.out.println("Mr."+client.getFatherName()+" has entered the app");
                            openClientMenu(client, account);
                        } else {
                            loginerrorlabel.setText("Account not active");
                            loginerrorlabel.setVisible(true);
                            loginerrorlabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-background-radius: 5;");                            showError("Account not active");
                        }
                        break;
                    case "Manager":
                        Manager manager = new Manager(Login_Account.getEmployeeName(conn, loginAccount.getLoginId()));
                        System.out.println("Mr."+manager.getName()+" has entered the app");
                        openManagerMenu(manager);
                        break;
                }
            } else {
                shakePanel(loginPanel);
                loginerrorlabel.setText("Invalid username or password");
                loginerrorlabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-background-radius: 5;");
                loginerrorlabel.setVisible(true);
                showError("Invalid credentials");
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    @FXML
    private void handleSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/LoginSignup/Signup1.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shakePanel(Pane panel) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(70), panel);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    private void showError(String message) {
        System.err.println(message);
    }

    private void openClientMenu(Client client, Bank_Account account) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/Client/ClientDashboard.fxml"));
            Parent root = loader.load();

            ClientMenuController controller = loader.getController();
            controller.setClientData(client, account);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error");
        }
    }

    private void openManagerMenu(Manager manager) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/Manager/ManagerDashboard.fxml"));
            Parent root = loader.load();

            ManagerDashboardController controller = loader.getController();
            controller.setManagerData(manager);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error");
        }    }
}