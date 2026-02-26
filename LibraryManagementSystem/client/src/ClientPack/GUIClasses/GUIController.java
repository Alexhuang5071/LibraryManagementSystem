/*
 * EE422C Final Project submission by
 * Replace <... ...> with your actual data.
 * <Alex Huang>
 * <ah57984>
 * <17185>
 * Spring 2023
 */
package ClientPack.GUIClasses;

import ClientPack.Client;
import Common.LoginMessage;
import ClientPack.Reader;
import ClientPack.Writer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class GUIController {

    public Text Wrong_Login;
    public CheckBox Show_Password;
    public TextField Visible_Password_Field;
    public Hyperlink Forgot_Password;
    @FXML
    private TextField Username_Field;
    @FXML
    private PasswordField Password_Field;
    private String LoggedInUsername;
    private String LoggedInPassword;
    private static Reader reader;
    private static Writer writer;
    private Client client;
    public void setClient(Client client) {
        this.client = client;
    }

    public void setWriter(Writer writer1) {
        writer = writer1;
    }
    public void setReader(Reader reader1) {
        reader = reader1;
        reader.setGUIController(this);
    }
    public void initialize() {
        // Bind the text properties of both fields
        Visible_Password_Field.textProperty().bindBidirectional(Password_Field.textProperty());
        Visible_Password_Field.setVisible(false);
        Password_Field.setVisible(true);
    }
    public void Login_ButtonClick(javafx.event.ActionEvent actionEvent) {
        String Username = Username_Field.getText();
        String Password = Password_Field.getText();
        LoginMessage send = new LoginMessage(Username, Password);
        try {
            writer.sendLoginMessage(send);
        } catch (IOException e) {
            System.out.println("ClientPack.Controller Error");
            throw new RuntimeException(e);
        }
    }

    public void Login_Failed() {
        Wrong_Login.setVisible(true);
    }

    public void Login_Success() {
        Wrong_Login.setVisible(false);
        LoggedInUsername = Username_Field.getText(); //Saves the current username of the account logged
        LoggedInPassword = Password_Field.getText();
        Platform.runLater(() -> LoadLoggedIn(Username_Field));
    }

    public void LibrarianLoginSuccess() {
        Wrong_Login.setVisible(false);
        LoggedInUsername = Username_Field.getText(); //Saves the current username of the account logged
        LoggedInPassword = Password_Field.getText();
        Platform.runLater(() -> LoadLibrarianLoggedIn(Username_Field));
    }

    public void LoadLibrarianLoggedIn(Node node) {
        try {
            // Load the LibrarianLoggedIn.fxml file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ClientPack/GUIClasses/LibrarianLoggedIn.fxml"));
            Parent root = fxmlLoader.load();

            LibrarianLoggedInController controller = fxmlLoader.getController();
            controller.setReader(reader);
            controller.setWriter(writer);
            controller.setClient(client);
            controller.setLogin(LoggedInUsername);

            Scene scene = new Scene(root, 800, 800);

            // Get the stage and set the new scene
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void LoadLoggedIn(Node node) {
        try {
            // Load the LoggedIn.fxml file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ClientPack/GUIClasses/LoggedIn.fxml"));
            Parent root = fxmlLoader.load();

            LoggedInController controller = fxmlLoader.getController();
            controller.setReader(reader);
            controller.setWriter(writer);
            controller.setClient(client);
            controller.setLogin(LoggedInUsername, LoggedInPassword);

            Scene scene = new Scene(root, 800, 800);

            // Get the stage and set the new scene
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void SignUp_ButtonClick(javafx.event.ActionEvent event) throws IOException {
        // Load the new FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("GUIClasses/SignUp.fxml"));
        Parent root = fxmlLoader.load();

        SignUpController controller = fxmlLoader.getController();
        controller.setReader(reader);
        controller.setWriter(writer);
        controller.setClient(client);
        // Create a new scene with the new FXML file
        Scene scene = new Scene(root, 600, 600);

        // Get the stage and set the new scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }


    public void Show_Password_Check(ActionEvent actionEvent) {
        if (Show_Password.isSelected()) {
            // Show the TextField and hide the PasswordField
            Visible_Password_Field.setVisible(true);
            Password_Field.setVisible(false);
        } else {
            // Hide the TextField and show the PasswordField
            Visible_Password_Field.setVisible(false);
            Password_Field.setVisible(true);
        }
    }

    public void Quit_Button_Handler(ActionEvent actionEvent) {
        try {
            client.StopNetworking();
            Platform.exit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Forgot_Password_Click(ActionEvent actionEvent) throws IOException {
        // Load the new FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("GUIClasses/ForgotPassword.fxml"));
        Parent root = fxmlLoader.load();

        ForgotPasswordController controller = fxmlLoader.getController();
        controller.setReader(reader);
        controller.setWriter(writer);
        controller.setClient(client);
        // Create a new scene with the new FXML file
        Scene scene = new Scene(root, 500, 500);

        // Get the stage and set the new scene
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }
}
