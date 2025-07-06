package def_pkg;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class BM_System extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("../GUI_Pages/LoginSignup/Login.fxml"));
		primaryStage.setTitle("Banking System");
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("../images/bank-ICON.png")));
		primaryStage.setScene(new Scene(root, 1000, 800));
		primaryStage.setResizable(false);
		primaryStage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}
}

