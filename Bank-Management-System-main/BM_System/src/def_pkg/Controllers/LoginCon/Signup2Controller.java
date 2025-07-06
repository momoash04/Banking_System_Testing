package def_pkg.Controllers.LoginCon;

import def_pkg.Login_Account;
import def_pkg.DB_handler;
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

public class Signup2Controller {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ProgressBar strengthBar;
    @FXML private Button createAccountButton;
    @FXML private Button backButton;
    @FXML private Pane createPanel;
    @FXML public Label usererrorLabel;

    private String accountNumber;

    @FXML
    private void initialize() {
        createAccountButton.setOnMouseEntered(e -> createAccountButton.setStyle("-fx-background-color: #005f8c; -fx-text-fill: white; -fx-background-radius: 5;"));
        createAccountButton.setOnMouseExited(e -> createAccountButton.setStyle("-fx-background-color: #0077b6; -fx-text-fill: white; -fx-background-radius: 5;"));

        // Password strength listener
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            int strength = calculatePasswordStrength(newValue);
            strengthBar.setProgress(strength / 100.0);

            // Change color based on strength
            if (strength < 30) strengthBar.setStyle("-fx-accent: red;");
            else if (strength < 70) strengthBar.setStyle("-fx-accent: orange;");
            else strengthBar.setStyle("-fx-accent: green;");
        });
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/LoginSignup/Signup1.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateAccount() {
        String pass1 = passwordField.getText();
        String pass2 = confirmPasswordField.getText();
        String newusername = usernameField.getText();

        if(pass1.isEmpty() || newusername.isEmpty()){
            bouncePanel(createPanel);
            usererrorLabel.setText("Missing fields required!");
            usererrorLabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-background-radius: 5;");
            usererrorLabel.setVisible(true);
            return;
        }
        if (strengthBar.getProgress() < 0.7) { // 70% strength threshold
            bouncePanel(createPanel);
            usererrorLabel.setVisible(true);
            usererrorLabel.setText("Password too weak!");
            usererrorLabel.setStyle("-fx-text-fill: white; -fx-background-color: orange; -fx-background-radius: 5;");
            return;
        }
        if(pass2.isEmpty()){
            bouncePanel(createPanel);
            usererrorLabel.setText("Please confirm your password!");
            usererrorLabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-background-radius: 5;");
            usererrorLabel.setVisible(true);
            return;
        }

        try (DB_handler db = new DB_handler()) {
            Connection conn = db.getConnection();
            int result = Login_Account.signUp(conn, newusername, pass1, pass2, accountNumber);

            if (result == 0) {
                showSuccess("Account created successfully!");
                wait(1000);
                openLoginForm();
            }
            else {
                showError(getSignupError(result));
            }
        } catch (SQLException ex) {
            System.out.println("Database error: " + ex.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void openLoginForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/LoginSignup/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) createAccountButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 8) strength += 20;
        if (password.matches(".*[A-Z].*")) strength += 20;
        if (password.matches(".*[a-z].*")) strength += 20;
        if (password.matches(".*[0-9].*")) strength += 20;
        if (password.matches(".*[!@#$%^&*()].*")) strength += 20;
        return Math.min(strength, 100);
    }

    private String getSignupError(int code) {
        return switch (code) {
            case -1 -> "Passwords mismatch";
            case -2 -> "Account already exists!";
            default -> "Unknown error";
        };
    }

    private void bouncePanel(Pane panel) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), panel);
        tt.setByY(10);
        tt.setCycleCount(10);
        tt.setAutoReverse(true);
        tt.play();
    }

    private void showError(String message) {
        bouncePanel(createPanel);
        usererrorLabel.setVisible(true);
        usererrorLabel.setText(message);
        usererrorLabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-background-radius: 5;");
    }

    private void showSuccess(String message) {
        bouncePanel(createPanel);
        usererrorLabel.setVisible(true);
        usererrorLabel.setText(message);
        usererrorLabel.setStyle("-fx-text-fill: white; -fx-background-color: green; -fx-background-radius: 5;");
    }
}