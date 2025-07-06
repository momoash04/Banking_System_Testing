package def_pkg.Controllers.ManagerCon;
import def_pkg.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javax.swing.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class ManagerDashboardController {

    private Manager manager;

    @FXML private Label welcomeLabel;
    @FXML private Label totalAccountsLabel;
    @FXML private Label totalEmployeesLabel;
    @FXML private Button signOutButton;
    @FXML private Button CreateAccountBtn2;
    @FXML private Button CreateAccountBtn;
    @FXML private Button blockUnblockBtn;
    @FXML private Button blockUnblockBtn2;
    @FXML private Button UpdateAccountBtn;


    @FXML public void setManagerData(Manager manager) {
        System.out.println("Manager is: " + manager.getName());
        welcomeLabel.setText("Welcome " + manager.getName());
        this.manager = manager;
        updateUI(manager);
    }
    @FXML public void updateUI(Manager manager){
        if (manager == null) {
            System.err.println("Manager is not set!");
            return;
        }
        try (DB_handler db = new DB_handler()) {
            Connection conn = db.getConnection();
            System.out.println(welcomeLabel.getText()); welcomeLabel.setText("");
            welcomeLabel.setText("Welcome, " + this.manager.getName());
            int totalAccounts = manager.getTotalAccounts(conn,manager);
            System.out.println("Total Accounts: " + totalAccounts);
            totalAccountsLabel.setText(String.valueOf(totalAccounts));
            totalEmployeesLabel.setText(String.valueOf(manager.getTotalEmployees(conn,manager)));

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load dashboard data: " + e.getMessage());
        }
    }

    @FXML public void initialize() {
        CreateAccountBtn.setOnAction(e->openCreateAccountpage());
        CreateAccountBtn2.setOnAction(e->openCreateAccountpage());
        blockUnblockBtn.setOnAction(e->openBlockAccountpage());
        blockUnblockBtn2.setOnAction(e->openBlockAccountpage());
        UpdateAccountBtn.setOnAction(e->openUpdateAccountpage());
        signOutButton.setOnAction(e->signOut());

        String normalStyle = "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-font-weight: bold;-fx-background-radius: 10;";
        String hoverStyle = "-fx-background-color: #3a5a80; -fx-text-fill: white; -fx-font-weight: bold;-fx-background-radius: 10;";
        setupHoverEffect(CreateAccountBtn,normalStyle,hoverStyle);
        setupHoverEffect(blockUnblockBtn,normalStyle,hoverStyle);
        setupHoverEffect(UpdateAccountBtn,normalStyle,hoverStyle);
        setupHoverEffect(signOutButton,hoverStyle,normalStyle);

    }

    private void setupHoverEffect(Button button, String normalStyle, String hoverStyle) {
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
    }
    private void signOut() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../../../GUI_Pages/LoginSignup/Login.fxml"));
            Stage stage = (Stage) signOutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to sign out");
        }
    }

    public void openCreateAccountpage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/Manager/ManagerCreateAccount.fxml"));
            Parent root = loader.load();

            CreateAccountController  controller = loader.getController();
            controller.setManagerData(manager);
            Stage stage = (Stage) CreateAccountBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open create account page");
        }
    }

    private void openUpdateAccountpage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/Manager/ManagerUpdateAccount.fxml"));
            Parent root = loader.load();

            UpdateAccountController  controller = loader.getController();
            controller.setManagerData(manager);
            Stage stage = (Stage) UpdateAccountBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open update account page");
        }
    }

    private void openBlockAccountpage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../GUI_Pages/Manager/ManagerBlockUnblock.fxml"));
            Parent root = loader.load();

            BlockUnblockController  controller = loader.getController();
            controller.setManagerData(manager);
            Stage stage = (Stage) blockUnblockBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Block/Unblock account page");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}