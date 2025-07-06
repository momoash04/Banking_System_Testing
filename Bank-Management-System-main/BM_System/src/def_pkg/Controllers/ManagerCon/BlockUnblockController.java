package def_pkg.Controllers.ManagerCon;

import def_pkg.Bank_Account;
import def_pkg.Client;
import def_pkg.DB_handler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import def_pkg.Manager;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class BlockUnblockController {
    @FXML private TextField accountNumberField;
    @FXML private ComboBox<String> actionCombo;
    @FXML private Button submitButton;
    @FXML private Button backButton;
    @FXML private Label messagelabel;

    private Manager manager;

    public void setManagerData(Manager manager) {
        System.out.println("Manager is: " + manager.getName());
        this.manager = manager;
    }
    public void initialize() {
        actionCombo.getItems().addAll("Block", "Unblock");
        backButton.setOnAction(e->handleBack());
        submitButton.setOnAction(e->handleSubmit());
    }

    @FXML
    private void handleSubmit() {
         String accnum = accountNumberField.getText().trim();
         if (accnum.isEmpty()) {
             messagelabel.setText("Please enter account number");
             messagelabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-font-size: 18;");
             return;
         }
         String action = actionCombo.getValue();
         if(action==null){
             messagelabel.setText("Please enter action");
             messagelabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-font-size: 18;");
            return;
         }
         try(DB_handler db = new DB_handler()){
             Connection conn = db.getConnection();
             Client client = manager.getClientInfo(conn,accnum);
             if(client==null){
                 messagelabel.setText("Account not found");
                 messagelabel.setStyle("-fx-text-fill: white; -fx-background-color: red; -fx-font-weight: bold; -fx-font-size: 18;");
                 return;
             }

             Bank_Account acc = Bank_Account.getByAccountNumber(conn,accnum);

             if (action.equals("Block")) {
                 int blockingStatus = manager.blockAccount(conn, acc);
                 if (blockingStatus == 0)
                 {
                     messagelabel.setText("Account already blocked");
                     messagelabel.setStyle("-fx-text-fill: white; -fx-background-color: orange; -fx-font-weight: bold; -fx-font-size: 18;");
                 }
                 else if (blockingStatus == 1)
                 {
                     messagelabel.setText("Account blocked successfully!");
                     messagelabel.setStyle("-fx-text-fill: white; -fx-background-color: green; -fx-font-weight: bold; -fx-font-size: 18;");
                 }
                 else if (blockingStatus == 2)
                 {

                 }
             }
             else if (action.equals("Unblock"))
             {
                 int blockingStatus = manager.unblockAccount(conn, acc);
                 if (blockingStatus == 0)
                 {
                     messagelabel.setText("Account already unblocked");
                     messagelabel.setStyle("-fx-text-fill: white; -fx-background-color: orange; -fx-font-weight: bold; -fx-font-size: 18;");
                 }
                 else if (blockingStatus == 1)
                 {
                     messagelabel.setText("Account unblocked successfully!");
                     messagelabel.setStyle("-fx-text-fill: white; -fx-background-color: green; -fx-font-weight: bold; -fx-font-size: 18;");
                 }
                 else if (blockingStatus == 2)
                 {

                 }
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
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
}