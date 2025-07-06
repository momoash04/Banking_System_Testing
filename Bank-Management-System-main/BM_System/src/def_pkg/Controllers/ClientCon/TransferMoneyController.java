package def_pkg.Controllers.ClientCon;

import def_pkg.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class TransferMoneyController {
    @FXML private TextField receiverAccountField;
    @FXML private TextField amountField;
    @FXML private Button transferButton;
    @FXML private Button mainMenuButton;
    @FXML private Button signOutButton;
    @FXML private Label currentBalanceLabel;

    private Client client;
    private Bank_Account account;

    public void setClientData(Client client, Bank_Account account) {
        this.client = client;
        this.account = account;
        updateUI();
    }
    private void updateUI() {
        currentBalanceLabel.setText("$" + String.format("%,.2f", Double.parseDouble(account.getBalance())));
    }
    @FXML
    private void initialize() {
        // Button hover effects
        setupButtonHover(transferButton);
        setupButtonHover(mainMenuButton);
        setupButtonHover(signOutButton);
        // Button actions
        transferButton.setOnAction(e -> handleTransfer());
        mainMenuButton.setOnAction(e -> openClientMenu());
        signOutButton.setOnAction(e -> signOut());
    }

    private void setupButtonHover(Button button) {
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #5990b6; -fx-text-fill: white;-fx-background-radius: 20;-fx-font-weight: bold;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #4a6fa5; -fx-text-fill: white;-fx-background-radius: 20;-fx-font-weight: bold;"));
    }

    private void handleTransfer() {
        String receiverAcc = receiverAccountField.getText();
        String amountStr = amountField.getText();


        if (receiverAcc.isEmpty() || amountStr.isEmpty()) {
            showAlert(AlertType.ERROR, "Error", "Please fill all fields");
            return;
        }

        if (receiverAcc.equals(account.getAccountNum())) {
            showAlert(AlertType.ERROR, "Error", "Cannot transfer to yourself");
            return;
        }

        if (Integer.parseInt(amountStr) <= 0) {
            showAlert(AlertType.ERROR,"Error","Transfer amount must be positive.");
            return;
        }
        try (DB_handler db = new DB_handler()) {
            Connection conn = db.getConnection();
            int amount = Integer.parseInt(amountStr);
            int result = client.transferMoney(conn, receiverAcc, amount);

            switch (result) {
                case 0:
                    showAlert(AlertType.INFORMATION, "Success", "Transfer successful");
                    account.updateBalance(conn);
                    openClientMenu();
                    break;
                case 1:
                    showAlert(AlertType.ERROR, "Error", "Account not found");
                    break;
                case 2:
                    showAlert(AlertType.ERROR, "Error", "Insufficient balance");
                    break;
                default:
                    showAlert(AlertType.ERROR, "Error", "Transfer failed");
            }
        } catch (NumberFormatException ex) {
            showAlert(AlertType.ERROR, "Error", "Invalid amount format");
        } catch (SQLException ex) {
            showAlert(AlertType.ERROR, "Database Error", ex.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openClientMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/Client/ClientDashboard.fxml"));
            Parent root = loader.load();

            ClientMenuController controller = loader.getController();
            controller.setClientData(client, account);

            Stage stage = (Stage) mainMenuButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void signOut() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../../../GUI_Pages/LoginSignup/Login.fxml"));
            Stage stage = (Stage) signOutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}