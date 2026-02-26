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
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoggedInController {

    public VBox libraryContainer;
    public Label userLabel;
    public Button Refresh_Button;
    public Button CheckedOutBooks_Button;
    public TextField Search_Bar;
    public Button My_Holds;
    private Writer writer;
    private Reader reader;
    private String Username;
    private String Password;
    private Client client;
    private List<Document> itemsList = new ArrayList<>();


    public void setClient(Client client) {
        this.client = client;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
        reader.setLoggedInController(this);
    }
    public void setWriter(Writer writer) {
        this.writer = writer;
    }
    public void setLogin(String Username, String Password) {
        this.Username = Username;
        this.Password = Password;
        userLabel.setText(Username);
    }



    public void setItems(List<Document> items) {
        Platform.runLater(() -> libraryContainer.getChildren().clear());
        itemsList = items;
        for (Document item : items) {
            String currentCheckedOutUser = item.getString("Current Checked Out");
            List<String> holds = item.getList("Holds", String.class);

            // Skip the item if it is checked out by the current user or held by the current user
            if ((currentCheckedOutUser != null && currentCheckedOutUser.equals(Username)) || (holds != null && holds.contains(Username))) {
                continue;
            }
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("LibraryItem.fxml"));
                HBox libraryItem = loader.load();
                LibraryItemController libraryItemController = loader.getController();
                libraryItemController.setReader(reader);
                libraryItemController.setWriter(writer);
                libraryItemController.setUsername(Username);
                libraryItemController.setItemData(item);

                Platform.runLater(() -> libraryContainer.getChildren().add(libraryItem));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //Method Only Displays Books the user has checked out
    public void filterCheckedOutBooks() {
        Platform.runLater(() -> libraryContainer.getChildren().clear());
        for (Document item : itemsList) {
            if (item.getString("Current Checked Out").equals(Username)) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("LibraryItem.fxml"));
                    HBox libraryItem = loader.load();
                    LibraryItemController libraryItemController = loader.getController();
                    libraryItemController.setReader(reader);
                    libraryItemController.setWriter(writer);
                    libraryItemController.setUsername(Username);
                    libraryItemController.setItemData(item);

                    Platform.runLater(() -> libraryContainer.getChildren().add(libraryItem));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void filterHeldBooks() {
        Platform.runLater(() -> libraryContainer.getChildren().clear());
        for (Document item : itemsList) {
            List<String> holds = item.getList("Holds", String.class);
            if (holds != null && holds.contains(Username)) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("LibraryItem.fxml"));
                    HBox libraryItem = loader.load();
                    LibraryItemController libraryItemController = loader.getController();
                    libraryItemController.setReader(reader);
                    libraryItemController.setWriter(writer);
                    libraryItemController.setUsername(Username);
                    libraryItemController.setItemData(item);

                    Platform.runLater(() -> libraryContainer.getChildren().add(libraryItem));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void CheckedOutBooks_Button_Click(ActionEvent actionEvent) {
        if (CheckedOutBooks_Button.getText().equals("Personal Library")) {
            CheckedOutBooks_Button.setText("Back to Catalog");
            My_Holds.setText("My Holds");
            filterCheckedOutBooks();
        } else { //Meaning button says Back to Catalog
            CheckedOutBooks_Button.setText("Personal Library");
            My_Holds.setText("My Holds");
            setItems(itemsList);
        }
    }

    public void Refresh_Button_Click(ActionEvent actionEvent) {
        Platform.runLater(() -> libraryContainer.getChildren().clear());
        writer.sendString("Refresh List");
        My_Holds.setText("My Holds");
        CheckedOutBooks_Button.setText("Personal Library");
    }

    public void Refresh() {
        Platform.runLater(() -> libraryContainer.getChildren().clear());
        writer.sendString("Refresh List");
        My_Holds.setText("My Holds");
        CheckedOutBooks_Button.setText("Personal Library");
    }

    public void Search_Bar_KeyReleased(KeyEvent keyEvent) {
        filterBySearch();
    }

    public void filterBySearch() {
        String searchText = Search_Bar.getText().toLowerCase();
        Platform.runLater(() -> libraryContainer.getChildren().clear());
        for (Document item : itemsList) {
            String title = item.getString("Title").toLowerCase();
            if (title.contains(searchText)) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("LibraryItem.fxml"));
                    HBox libraryItem = loader.load();
                    LibraryItemController libraryItemController = loader.getController();
                    libraryItemController.setReader(reader);
                    libraryItemController.setWriter(writer);
                    libraryItemController.setUsername(Username);
                    libraryItemController.setItemData(item);

                    Platform.runLater(() -> libraryContainer.getChildren().add(libraryItem));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void Logout_Button_Click(ActionEvent event) throws IOException {
        Platform.runLater(() -> libraryContainer.getChildren().clear());
        // Load the new FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("GUIClasses/GUI.fxml"));
        Parent root = fxmlLoader.load();
        reader.ClearLoggedInController();

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

    public void My_Holds_Click(ActionEvent actionEvent) {
        if (My_Holds.getText().equals("My Holds")) {
            My_Holds.setText("Back to Catalog");
            CheckedOutBooks_Button.setText("Personal Library");
            filterHeldBooks();
        } else { //Meaning button says Back to Catalog
            CheckedOutBooks_Button.setText("Personal Library");
            My_Holds.setText("My Holds");
            setItems(itemsList);
        }
    }


    public void ChangePassword_MenuItem_Click(ActionEvent event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your new password:");

        DialogPane dialogPane = dialog.getDialogPane();

        // Customize the button text
        ButtonType changePasswordButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(changePasswordButtonType, ButtonType.CANCEL);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("New Password:"), 0, 0);
        gridPane.add(newPasswordField, 1, 0);

        dialogPane.setContent(gridPane);

        //Disable the Change Password button by default
        Node changePasswordButton = dialogPane.lookupButton(changePasswordButtonType);
        changePasswordButton.setDisable(true);

        //Enable the Change Password button if the user enters text
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> changePasswordButton.setDisable(newValue.trim().isEmpty()));

        dialog.setResultConverter(buttonType -> {
            if (buttonType == changePasswordButtonType) {
                return newPasswordField.getText();
            }
            return null;
        });

        Optional<String> newPassword = dialog.showAndWait();

        newPassword.ifPresent(pass -> {
            Document document = new Document("Password", newPasswordField.getText())
                    .append("Username", userLabel.getText());
            writer.sendDocument(document);
        });
    }

}
