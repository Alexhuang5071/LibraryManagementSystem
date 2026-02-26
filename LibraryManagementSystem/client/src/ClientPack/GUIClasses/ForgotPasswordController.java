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
import ClientPack.Reader;
import ClientPack.Writer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;

public class ForgotPasswordController {
    public TextField Email_Field;
    public Button Submit_Button;
    public Label EnterCodeLabel;
    public TextField Code_Field;
    public Button VerifyButton;
    public Button ResendButton;
    public Label CodeResent_Label;
    public Label CodeIncorrect_Label;
    public Label CodeVerified_Label;
    public TextField EnterPassField;
    public Label EnterPasswordLabel;
    public Button ResetButton;
    public Label PasswordResetLabel;
    public Button LoginButton;
    public Label Required_Field;
    public Label EmailNotFound;
    public PasswordField HiddenPasswordFIeld;
    public CheckBox Show_Password;
    private Reader reader;
    private Writer writer;
    private  Client client;

    public void initialize() {
        // Bind the text properties of both fields
        EnterPassField.textProperty().bindBidirectional(HiddenPasswordFIeld.textProperty());

        EnterCodeLabel.setVisible(false);
        Code_Field.setVisible(false);
        VerifyButton.setVisible(false);
        ResendButton.setVisible(false);
        CodeResent_Label.setVisible(false);
        CodeIncorrect_Label.setVisible(false);
        CodeVerified_Label.setVisible(false);
        EnterPassField.setVisible(false);
        EnterPasswordLabel.setVisible(false);
        ResendButton.setVisible(false);
        Required_Field.setVisible(false);
        PasswordResetLabel.setVisible(false);
        LoginButton.setVisible(false);
        ResetButton.setVisible(false);
        EmailNotFound.setVisible(false);
        HiddenPasswordFIeld.setVisible(false);
        Show_Password.setVisible(false);
    }
    public void setReader(Reader reader) {
        this.reader = reader;
        reader.setForgotPasswordController(this);
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    private void Show_Password_Check(ActionEvent event) {
        if (Show_Password.isSelected()) {
            EnterPassField.setVisible(true);
            HiddenPasswordFIeld.setVisible(false);
        } else {
            EnterPassField.setVisible(false);
            HiddenPasswordFIeld.setVisible(true);
        }
    }



    @FXML
    public void EmailValid() {
        Email_Field.setEditable(false);
        String email = Email_Field.getText();
        Document document = new Document("Email", email).append("ForgotPassword", ""); //Send to server saying user needs to reset password
        writer.sendDocument(document);
        EnterCodeLabel.setVisible(true);
        Code_Field.setVisible(true);
        ResendButton.setVisible(true);
        VerifyButton.setVisible(true);
        Email_Field.setEditable(false);
        Submit_Button.setDisable(true);
        EmailNotFound.setVisible(false);
    }

    public void Submit_Button_Click(ActionEvent event) {
        Document document = new Document("Email", Email_Field.getText().toLowerCase()).append("EmailOnlyCheckForPasswordForget", "");;
        writer.sendDocument(document);
    }

    public void InvalidEmail() {
        EmailNotFound.setVisible(true);
    }




    public void Back_Button_Click(javafx.event.ActionEvent event) throws IOException {
        // Load the new FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("GUIClasses/GUI.fxml"));
        Parent root = fxmlLoader.load();

        GUIController controller = fxmlLoader.getController();
        controller.setReader(reader);
        controller.setWriter(writer);
        controller.setClient(client);
        // Create a new scene with the new FXML file
        Scene scene = new Scene(root, 600, 600);

        // Get the stage and set the new scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    public void Verify_Button_Click(ActionEvent actionEvent) {
        Document document = new Document("Email", Email_Field.getText().toLowerCase())
                .append("VerificationCode", Code_Field.getText())
                .append("ForgotPasswordCode", "");
        writer.sendDocument(document);
        Code_Field.setEditable(false);
        VerifyButton.setDisable(true);
    }

    public void Resend_Button_Click(ActionEvent actionEvent) {
        CodeResent_Label.setVisible(true);
        CodeIncorrect_Label.setVisible(false);
        CodeVerified_Label.setVisible(false);
        Submit_Button_Click(actionEvent);
    }
    public void CodeCorrect() { //Password Field only shown if Code is Correct
        EnterPasswordLabel.setVisible(true);
        ResetButton.setVisible(true);
        CodeIncorrect_Label.setVisible(false);
        CodeVerified_Label.setVisible(true);
        CodeResent_Label.setVisible(false);
        ResendButton.setDisable(true);

        EnterPassField.setVisible(false);
        HiddenPasswordFIeld.setVisible(true);
        Show_Password.setVisible(true);
    }
    public void CodeIncorrect() {
        CodeVerified_Label.setVisible(false);
        CodeResent_Label.setVisible(false);
        CodeIncorrect_Label.setVisible(true);
        Code_Field.setEditable(true);
        VerifyButton.setDisable(false);
    }


    public void Reset_Button_Click(ActionEvent actionEvent) {
        if (EnterPassField.getText().isEmpty()) {
            Required_Field.setVisible(true);
        } else {
            Document document = new Document("Email", Email_Field.getText().toLowerCase())
                    .append("Password", EnterPassField.getText())
                    .append("PasswordResetValidated", "");
            writer.sendDocument(document);
            EnterPassField.setEditable(false);
            HiddenPasswordFIeld.setEditable(false);
            ResetButton.setDisable(true);
            Required_Field.setVisible(false);
            PasswordResetLabel.setVisible(true);
            LoginButton.setVisible(true);
            //Send New Password
        }
    }
}
