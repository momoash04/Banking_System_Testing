package def_pkg.Controllers.LoginCon;

import def_pkg.Login_Account;
import def_pkg.DB_handler;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Signup1Controller {
    @FXML private TextField accountNumberField;
    @FXML private TextField cnicField;
    @FXML private Button verifyButton;
    @FXML private Button backButton;
    @FXML private Pane signupPanel;
    @FXML public Label errorLabel;

    @FXML
    private void initialize() {
        verifyButton.setOnMouseEntered(e -> verifyButton.setStyle("-fx-background-color: #005f8c; -fx-text-fill: white; -fx-background-radius: 5;"));
        verifyButton.setOnMouseExited(e -> verifyButton.setStyle("-fx-background-color: #0077b6; -fx-text-fill: white; -fx-background-radius: 5;"));
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/LoginSignup/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVerify() {
        String acc = accountNumberField.getText().trim();
        String cnic = cnicField.getText().trim();

        if(acc.isEmpty() || cnic.isEmpty()){
            shakePanel(signupPanel);
            errorLabel.setText("Please enter all fields!");
            errorLabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-background-radius: 5;");
            errorLabel.setVisible(true);
            showError("Please enter all fields!");
            return;
        }

        try (DB_handler db = new DB_handler()) {
            Connection conn = db.getConnection();
            boolean isValid = Login_Account.verifyAccount(conn,
                    accountNumberField.getText(),
                    cnicField.getText()
            );

            if (isValid) {
                openSignupForm2(accountNumberField.getText());
            } else {
                shakePanel(signupPanel);
                errorLabel.setVisible(true);
                errorLabel.setText("Invalid Account Number or CNIC");
                errorLabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-background-radius: 5;");
                showError("Invalid account or CNIC");
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void openSignupForm2(String accountNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/LoginSignup/Signup2.fxml"));
            Parent root = loader.load();

            Signup2Controller controller = loader.getController();
            controller.setAccountNumber(accountNumber);

            Stage stage = (Stage) verifyButton.getScene().getWindow();
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
        // You can replace this with a proper JavaFX Alert dialog
        System.err.println(message);
    }
}