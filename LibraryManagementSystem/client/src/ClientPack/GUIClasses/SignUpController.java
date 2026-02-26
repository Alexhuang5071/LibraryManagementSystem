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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.util.regex.Pattern;

public class SignUpController {
    public Text Username_Taken;
    public Text Email_Taken;
    public Button Go_Back_Button;
    public Text Account_Created_Text;
    public Button Login_Button;
    @FXML
    public PasswordField Password_Hidden_Field;
    public CheckBox Show_Password;
    public Text Email_NotValid;
    public Text AllFieldsRequired;
    public Label VerificationLabel;
    public Button VerifyButton;
    public Text IncorrectCode_Label;
    public TextField VerificationCodeField;
    public Button Create_Account_Button;
    public Button ResendCodeButton;
    public Text AccountVerifiedText;
    public Text ResentCode_Label;
    @FXML
    private TextField Username_Field;
    @FXML
    private TextField Password_Field;
    @FXML
    private TextField First_Name_Field;
    @FXML
    private TextField Last_Name_Field;
    @FXML
    private TextField Email_Field;
    private Writer writer;
    private Reader reader;
    private Client client;

    public void setReader(Reader reader) {
        this.reader = reader;
        reader.setSignUpController(this);
    }
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public void setClient(Client client) {
        this.client = client;
    }
    public void initialize() {
        // Bind the text properties of both fields
        Password_Field.textProperty().bindBidirectional(Password_Hidden_Field.textProperty());

        // Set the initial visibility of the fields
        Password_Field.setVisible(false);
        Password_Hidden_Field.setVisible(true);

        //Disable all the Verification Stuff
        VerifyButton.setVisible(false);
        VerificationLabel.setVisible(false);
        VerificationCodeField.setVisible(false);
        IncorrectCode_Label.setVisible(false);
        ResendCodeButton.setVisible(false);
        AccountVerifiedText.setVisible(false);
        Account_Created_Text.setVisible(false);
        ResentCode_Label.setVisible(false);
    }

    @FXML
    private void Show_Password_Check(ActionEvent event) {
        if (Show_Password.isSelected()) {
            Password_Field.setVisible(true);
            Password_Hidden_Field.setVisible(false);
        } else {
            Password_Field.setVisible(false);
            Password_Hidden_Field.setVisible(true);
        }
    }

    private boolean isValid(String email) {
        String test = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(test);
        return pattern.matcher(email).matches();
    }

    private boolean FieldsCheck() {
        return !Username_Field.getText().isEmpty() && !Password_Field.getText().isEmpty() && !First_Name_Field.getText().isEmpty()
                && !Last_Name_Field.getText().isEmpty() && !Email_Field.getText().isEmpty();
    }


    public void Create_Account_Button_Click(javafx.event.ActionEvent actionEvent) {
        if (!FieldsCheck()) {
            AllFieldsRequired.setVisible(true);
            Username_Taken.setVisible(false);
            Email_Taken.setVisible(false);
            Email_NotValid.setVisible(false);
        } else if (isValid(Email_Field.getText())) {
            DisableFields();
            AllFieldsRequired.setVisible(false);
            Email_NotValid.setVisible(false);
            Document Emailcheck = new Document("Email", Email_Field.getText());
            writer.sendDocument(Emailcheck);
        } else { //Email is not valid
            EnableFields();
            AllFieldsRequired.setVisible(false);
            Username_Taken.setVisible(false);
            Email_Taken.setVisible(false);
            Email_NotValid.setVisible(true);
        }
    }

    public void Email_Free() { //Only Called if Email is Free
        Email_Taken.setVisible(false);
        Document UsernameCheck = new Document("Username", Username_Field.getText());
        writer.sendDocument(UsernameCheck);
    }

    public void Email_Taken_Handle() {
        Email_Taken.setVisible(true);
        Username_Taken.setVisible(false);
        Email_NotValid.setVisible(false);
        EnableFields();
    }

    public void Username_Free() { //Only Called if Username is Free, Can successfully create account
        Username_Taken.setVisible(false);
        Document UsernameCheck = new Document("Username", Username_Field.getText())
                .append("Password", Password_Field.getText())
                .append("Email", Email_Field.getText().toLowerCase())
                .append("First Name", First_Name_Field.getText())
                .append("Last Name", Last_Name_Field.getText())
                .append("NeedToVerify", ""); //Key to say that still needs verification
        writer.sendDocument(UsernameCheck); //Here, we need to open GUI for verification code
        ResendCodeButton.setVisible(true);
        Create_Account_Button.setDisable(true);
        VerificationLabel.setVisible(true);
        VerificationCodeField.setVisible(true);
        VerifyButton.setVisible(true);
        ResentCode_Label.setVisible(false);
    }

    public void VerificationIncorrect() {
        IncorrectCode_Label.setVisible(true);
        VerificationCodeField.setEditable(true);
        VerifyButton.setDisable(false);
        ResendCodeButton.setDisable(false);
        ResentCode_Label.setVisible(false);
    }

    public void Username_Taken_Handle() {
        Username_Taken.setVisible(true);
        Email_Taken.setVisible(false);
        Email_NotValid.setVisible(false);
        EnableFields();
    }

    public void AccountCreated() {
        VerifyButton.setDisable(true);
        Account_Created_Text.setVisible(true);
        Login_Button.setVisible(true);
        IncorrectCode_Label.setVisible(false);
        ResendCodeButton.setVisible(false);
        AccountVerifiedText.setVisible(true);
        ResentCode_Label.setVisible(false);
        DisableFields();
    }

    public void VerifyButtonClick(ActionEvent actionEvent) {
        String code = VerificationCodeField.getText();
        VerificationCodeField.setEditable(false);
        Document VerifyCheck = new Document("Username", Username_Field.getText())
                .append("Password", Password_Field.getText())
                .append("Email", Email_Field.getText().toLowerCase())
                .append("First Name", First_Name_Field.getText())
                .append("Last Name", Last_Name_Field.getText())
                .append("VerificationCode", code); //Key to say that doc contains code
        writer.sendDocument(VerifyCheck);
        VerificationCodeField.setEditable(false);
        VerifyButton.setDisable(true);
        ResendCodeButton.setDisable(true);
        ResentCode_Label.setVisible(false);
    }

    public void ResendCodeButtonClick(ActionEvent actionEvent) {
        ResentCode_Label.setVisible(true);
        Username_Free();
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




    public void DisableFields() {
        Username_Field.setEditable(false);
        Password_Field.setEditable(false);
        First_Name_Field.setEditable(false);
        Last_Name_Field.setEditable(false);
        Email_Field.setEditable(false);
        Password_Hidden_Field.setEditable(false);
    }

    public void EnableFields() {
        Username_Field.setEditable(true);
        Password_Field.setEditable(true);
        First_Name_Field.setEditable(true);
        Last_Name_Field.setEditable(true);
        Email_Field.setEditable(true);
        Password_Hidden_Field.setEditable(true);
    }


}
