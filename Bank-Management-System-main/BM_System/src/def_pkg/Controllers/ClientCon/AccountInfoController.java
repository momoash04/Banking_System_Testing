package def_pkg.Controllers.ClientCon;

import def_pkg.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AccountInfoController {
    @FXML private TextField nameField;
    @FXML private Label  headernamefield;
    @FXML private TextField accountNumberField;
    @FXML private TextField accountTypeField;
    @FXML private Label balanceField;
    @FXML private TextField openingDateField;
    @FXML private Button backButton;
    @FXML private Button signOutButton;

    private Client client;
    private Bank_Account account;

    public void setClientData(Client client, Bank_Account account) {
        this.client = client;
        this.account = account;

        // Populate fields
        nameField.setText(client.getFName() + " " + client.getLName());
        headernamefield.setText(client.getFName() + " " + client.getLName());
        accountNumberField.setText(account.getAccountNum());
        accountTypeField.setText(account.getType());
        balanceField.setText(account.getBalance());
        openingDateField.setText(account.getOpeningDate());
    }

    @FXML
    private void initialize() {
        // Button hover effects
        setupButtonHover(backButton);
        setupButtonHover(signOutButton);

        // Button actions
        backButton.setOnAction(e -> openClientMenu());
        signOutButton.setOnAction(e -> signOut());
    }

    private void setupButtonHover(Button button) {
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #000fa5; -fx-text-fill: white;-fx-background-radius: 20;-fx-border-color: white;-fx-border-radius: 20;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #4a6fa5; -fx-text-fill: white;-fx-background-radius: 20;-fx-border-color: white;-fx-border-radius: 20;"));
    }

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