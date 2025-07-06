package def_pkg.Controllers.ClientCon;
import def_pkg.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class ClientMenuController {
    @FXML private Label userNameLabel;
    @FXML private Label balanceLabel;
    @FXML private Button accountInfoButton;
    @FXML private Button transferMoneyButton;
    @FXML private Button DepositMoney;
    @FXML private Button WithdrawMoney;
    @FXML private Button signOutButton;
    @FXML private ImageView eye;

    private Client client;
    private Bank_Account account;

    public void setClientData(Client client, Bank_Account account) {
        System.out.println("Client: " + client);
        System.out.println("Account: " + account);
        this.client = client;
        this.account = account;
        updateUI();
        updateBalance();

    }

    @FXML
    private void initialize() {
        setupButtonHoverEffects();
        eye.setImage(new Image("images/eye-closed.png"));
        GaussianBlur blurEffect = new GaussianBlur(20);
        balanceLabel.setEffect(blurEffect);
        // Button actions
        accountInfoButton.setOnAction(e -> openAccountInfo());
        transferMoneyButton.setOnAction(e -> openTransferMoney());
        signOutButton.setOnAction(e -> signOut());
        DepositMoney.setOnAction(e-> openDepositMoney());
        WithdrawMoney.setOnAction(e-> WithdrawMoney());

        eye.setOnMouseClicked(e -> hideBalance());

    }
    private void updateBalance() {
        if (account == null) {
            showAlert("ERROR", "Account is not initialized", Alert.AlertType.ERROR);
            return;
        }
        try (DB_handler db = new DB_handler()) {
            Connection conn = db.getConnection();
            account.updateBalance(conn); // Ensure this method is implemented correctly in Bank_Account
        } catch (SQLException ex) {
            showAlert("ERROR", "Database Error", Alert.AlertType.ERROR);
        }
    }

    private void hideBalance() {
        GaussianBlur blurEffect = new GaussianBlur(20); // Adjust radius as needed

        if (balanceLabel.getEffect() instanceof GaussianBlur) {
            // If GaussianBlur is already applied, remove it
            balanceLabel.setEffect(null); // Remove the blur effect

            // Update the eye icon to the normal eye image
            eye.setImage(new Image("images/eye.png"));
            eye.setFitHeight(40);
            eye.setFitWidth(48);
        } else {
            // Apply Gaussian blur
            balanceLabel.setEffect(blurEffect);

            // Update the eye icon to the closed eye image
            eye.setImage(new Image("images/eye-closed.png"));
            eye.setFitHeight(32);
            eye.setFitWidth(40);
        }
    }

    private void updateUI() {
        userNameLabel.setText("Welcome, " + client.getFName() + " " + client.getLName());
        balanceLabel.setText("$" + String.format("%,.2f", Double.parseDouble(account.getBalance())));
    }

    private void setupButtonHoverEffects() {
        // Generic hover effect for all action buttons
        String normalStyle = "-fx-background-color: #4a6fa5; -fx-text-fill: white;";
        String hoverStyle = "-fx-background-color: #3a5a80; -fx-text-fill: white;";

        setupHoverEffect(accountInfoButton, normalStyle, hoverStyle);
        setupHoverEffect(transferMoneyButton, normalStyle, hoverStyle);

        signOutButton.setOnMouseEntered(e ->
                signOutButton.setStyle(
                        "-fx-border-color: #a8c4ff; " +
                                "-fx-background-color: #e0ebff; " +
                                "-fx-text-fill: #003399; " +
                                "-fx-border-radius: 15px;" +
                                "-fx-background-radius: 15px; "
                )
        );
        signOutButton.setOnMouseExited(e ->
                signOutButton.setStyle(
                        "-fx-border-color: white; " +
                                "-fx-background-color: transparent; " +
                                "-fx-text-fill: white; " +
                                "-fx-border-radius: 15px;" +
                                "-fx-background-radius: 15px; "
                )
        );

    }

    private void setupHoverEffect(Button button, String normalStyle, String hoverStyle) {
        button.setStyle(normalStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
    }


    private void openDepositMoney() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/Client/ClientDeposit.fxml"));
            Parent root = loader.load();

            DepositController controller = loader.getController();
            controller.setClientData(client, account);            Stage stage = (Stage) accountInfoButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open account info", Alert.AlertType.ERROR);
        }
    }


    private void openAccountInfo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/Client/ClientAccountInfo.fxml"));
            Parent root = loader.load();

            AccountInfoController controller = loader.getController();
            controller.setClientData(client, account);

            Stage stage = (Stage) accountInfoButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open account info", Alert.AlertType.ERROR);
        }
    }

    private void openTransferMoney() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/Client/ClientTransferMoney.fxml"));
            Parent root = loader.load();

            TransferMoneyController controller = loader.getController();
            controller.setClientData(client, account);

            Stage stage = (Stage) transferMoneyButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open transfer screen", Alert.AlertType.ERROR);
        }
    }

    private void WithdrawMoney() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/Client/ClientWithdraw.fxml"));
            Parent root = loader.load();

            WithdrawController controller = loader.getController();
            controller.setClientData(client, account);

            Stage stage = (Stage) WithdrawMoney.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open transfer screen", Alert.AlertType.ERROR);
        }
    }



    private void signOut() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../../../GUI_Pages/LoginSignup/Login.fxml"));
            Stage stage = (Stage) signOutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to sign out", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}