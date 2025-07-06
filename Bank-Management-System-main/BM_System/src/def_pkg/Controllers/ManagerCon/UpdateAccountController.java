package def_pkg.Controllers.ManagerCon;

import def_pkg.Client;
import def_pkg.DB_handler;
import def_pkg.Manager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

public class UpdateAccountController {
    private Manager manager;

    @FXML private Button backButton;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private GridPane clientDetailsPane;
    @FXML private Label clientIDLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField fatherNameField;
    @FXML private TextField motherNameField;
    @FXML private TextField cnicField;
    @FXML private TextField dobField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private Button updateButton;
    @FXML private Label messageLabel;

    public void setManagerData(Manager manager) {
        System.out.println("Manager is: " + manager.getName());
        this.manager = manager;
    }

    @FXML
    private void initialize() {
        // Initialize any default settings
        clientDetailsPane.setVisible(false);
        messageLabel.setVisible(false);
        backButton.setOnAction(e->handleBackButtonAction());
        searchButton.setOnAction(e->handleSearchButtonAction());
        updateButton.setOnAction(e->handleUpdateButtonAction());

    }

    @FXML private void handleBackButtonAction() {
        try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/Manager/ManagerDashboard.fxml"));
                Parent root = loader.load();
                ManagerDashboardController controller = loader.getController();
                controller.setManagerData(manager);
                Scene scene = new Scene(root);
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.setScene(scene);
        } catch (IOException e) {
                e.printStackTrace();
        }

    }

    @FXML
    private void handleSearchButtonAction() {
        String AccNum= searchField.getText();
        if (AccNum.isEmpty()) {
            showMessage("Please enter an Account number to search");
            messageLabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-font-size: 18;");
            clientIDLabel.setVisible(false);
            clientDetailsPane.setVisible(false);
            return;
        }
        try(DB_handler db = new DB_handler()) {
            Connection con = db.getConnection();
            Client client = manager.getClientInfo(con,AccNum);
            loadClientData(client);
            clientDetailsPane.setVisible(true);
            messageLabel.setVisible(false);
        } catch (Exception e) {
            showMessage("Client not found");
            messageLabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-font-size: 18;");
            clientDetailsPane.setVisible(false);
        }
    }

    @FXML private void handleUpdateButtonAction() {
        String AccNum= searchField.getText();

        // Validate editable fields
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();

        if (phone.isEmpty() || email.isEmpty() || address.isEmpty()) {
            showMessage("Please fill all fields");
            messageLabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-font-size: 18;");
            return;
        }
        try(DB_handler db = new DB_handler()) {
            Connection con = db.getConnection();
            Client client = manager.getClientInfo(con,AccNum);
            String clientId = client.getClientID();
            manager.updateClientInfo(con,clientId,phone,email,address);
            showMessage("Client information updated successfully");
            messageLabel.setStyle("-fx-text-fill: white; -fx-background-color: green; -fx-font-weight: bold; -fx-font-size: 18;");

        } catch (Exception e) {
            showMessage("Error updating client: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-font-size: 18;");

        }
    }

    // Updated to use Client object
    private void loadClientData(Client client) {
        clientIDLabel.setText(client.getClientID());
        firstNameField.setText(client.getFName());
        lastNameField.setText(client.getLName());
        fatherNameField.setText(client.getFatherName());
        motherNameField.setText(client.getMotherName());
        cnicField.setText(client.getCNIC());
        dobField.setText(client.getDOB());
        phoneField.setText(client.getPhone());
        emailField.setText(client.getEmail());
        addressField.setText(client.getAddress());
    }
    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
    }
}