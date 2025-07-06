package def_pkg.Controllers.ClientCon;

import def_pkg.Bank_Account;
import def_pkg.Client;
import def_pkg.DB_handler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DepositController {
    @FXML private Label headernamefield;
    @FXML private TextField accountNumberField;
    @FXML private TextField currentBalanceField;
    @FXML private TextField depositAmountField;
    @FXML private Label statusMessage;
    @FXML private Button depositButton;
    @FXML private Button backButton;
    @FXML private Button signOutButton;

    private Client client;
    private Bank_Account account;

    public void setClientData(Client client, Bank_Account account) {
        this.client = client;
        this.account = account;

        headernamefield.setText(client.getFName()+ " " + client.getLName());
        accountNumberField.setText(account.getAccountNum());
        currentBalanceField.setText(account.getBalance());
    }

    @FXML private void initialize() {
        // Initialize fields with account data
        // Button actions
        backButton.setOnAction(e -> openClientMenu());
        signOutButton.setOnAction(e -> signOut());
        depositButton.setOnAction(e -> handleDeposit());

    }

    @FXML private void handleDeposit() {
        String accountNum = accountNumberField.getText();
        double balance = Double.parseDouble(currentBalanceField.getText());
        try {
            double amount = Double.parseDouble(depositAmountField.getText());
            if (amount <= 0) {
                statusMessage.setText("Deposit amount must be positive.");
                statusMessage.setStyle("-fx-text-fill:white;-fx-background-color:  #d32f2f;");
                return;
            }


            try (DB_handler db = new DB_handler()) {
                Connection conn = db.getConnection();

                int result = client.depositMoney(conn, accountNum, amount);
                switch (result) {
                    case 1:
                        showAlert(Alert.AlertType.ERROR, "Error", "Account not found");
                        break;
                    case 2:
                        showAlert(Alert.AlertType.ERROR, "Error", "Invalid deposit amount");
                        break;
                    default:
                        double newBalance = balance + amount;
                        currentBalanceField.setText(String.valueOf(newBalance));
                        account.updateBalance(conn);
                        statusMessage.setText("Deposit successful! Your funds have been added to your account.");
                        statusMessage.setStyle("-fx-text-fill:white;-fx-background-color:  #2e7d32;");
                        depositAmountField.clear();                }
            }
            catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            }


        } catch (NumberFormatException e) {
            statusMessage.setText("Please enter a valid amount.");
            statusMessage.setStyle("-fx-text-fill:white;-fx-background-color:  #d32f2f;");
        }
    }

    @FXML
    private void openClientMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/Client/ClientDashboard.fxml"));
            Parent root = loader.load();

            ClientMenuController controller = loader.getController();
            controller.setClientData(client, account);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void signOut() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../../../GUI_Pages/LoginSignup/Login.fxml"));
            Stage stage = (Stage) signOutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}