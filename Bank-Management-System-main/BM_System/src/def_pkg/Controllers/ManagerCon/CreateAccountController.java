package def_pkg.Controllers.ManagerCon;

import def_pkg.Bank_Account;
import def_pkg.Client;
import def_pkg.DB_handler;
import def_pkg.Manager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CreateAccountController {
    // Form Fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField cnicField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> accountTypeCombo;
    @FXML private TextField fathernameField;
    @FXML private TextField mothernamefield;
    @FXML private DatePicker datepicker;
    @FXML private TextField addressfield;

    // Buttons and Labels
    @FXML private Button createButton;
    @FXML private Button backButton;
    @FXML private Label createlabel;

    private Manager manager;
    @FXML public void setManagerData(Manager manager) {
        this.manager = manager;
    }

    @FXML
    public void initialize() {
        createButton.setOnAction(e -> handleCreateAccount());
        backButton.setOnAction(e-> handleBack());

        // Initialize account type dropdown
        accountTypeCombo.getItems().addAll("Savings", "Current");
        // Set default date to today
        datepicker.setValue(LocalDate.now());

        // Initialize manager and connection

    }
    @FXML private void handleCreateAccount() {
        try(DB_handler db = new DB_handler()) {
            Connection conn = db.getConnection();
            if (!validateInputs()) {return;}
            String dob = datepicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Client client = new Client(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    fathernameField.getText(),
                    mothernamefield.getText(),
                    cnicField.getText(),
                    dob,
                    phoneField.getText(),
                    emailField.getText(),
                    addressfield.getText()
            );
            int isvalid = manager.createAccount(conn, client, accountTypeCombo.getValue());
            if (isvalid == 1) {
                showError("Account with corresponding CNIC already exists!") ;
                cnicField.setStyle("-fx-background-color: pink; -fx-border-color: red; -fx-text-fill: red;");
                return;
            }
            else{
            createlabel.setText("Account Created Successfully!");
            createlabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-background-color: green;-fx-font-weight: bold;");
            clearForm();
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }


    @FXML
    private void handleBack(){
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

    private boolean validateInputs() {
        // Check required fields
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                cnicField.getText().isEmpty() || phoneField.getText().isEmpty() ||
                emailField.getText().isEmpty() || accountTypeCombo.getValue() == null ||
                fathernameField.getText().isEmpty() || mothernamefield.getText().isEmpty() ||
                addressfield.getText().isEmpty()) {

            showError("Please fill in all required fields");
            cnicField.setStyle("");
            return false;
        }
        return true;
    }


    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        cnicField.clear();
        phoneField.clear();
        emailField.clear();
        accountTypeCombo.getSelectionModel().clearSelection();
        fathernameField.clear();
        mothernamefield.clear();
        addressfield.clear();
        datepicker.setValue(LocalDate.now());
    }

    private void showError(String message) {
        createlabel.setText(message);
        createlabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-font-size: 18;");
    }

    private void showSuccess(String message) {
        createlabel.setText(message);
        createlabel.setStyle("-fx-text-fill: white; -fx-background-color: green; -fx-font-weight: bold; -fx-font-size: 18;");
    }
}