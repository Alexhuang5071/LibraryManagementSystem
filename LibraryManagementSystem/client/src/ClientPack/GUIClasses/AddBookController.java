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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class AddBookController {
    public Text AllFieldsRequiredText;
    public TextField TitleField;
    public TextField PagesField;
    public TextField AuthorsField;
    public TextField ImageLink;
    public TextArea DescriptionField;
    @FXML
    private ComboBox<String> itemTypeComboBox;
    @FXML
    private Label labelAuthorDirectorGenre;
    @FXML
    private Label labelPagesRuntimePlatform;
    private Reader reader;
    private Writer writer;
    private Client client;
    String Username;



    public void setLogin(String Username) {
        this.Username = Username;
    }


    public void initialize() {
        AllFieldsRequiredText.setVisible(false);
        itemTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateLabels(newValue));
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
        reader.setAddBookController(this);
    }
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    private void updateLabels(String selectedItem) {
        switch (selectedItem) {
            case "Movie":
                labelAuthorDirectorGenre.setText("Directors:");
                labelPagesRuntimePlatform.setText("Runtime:");
                break;
            case "Game":
                labelAuthorDirectorGenre.setText("Genres:");
                labelPagesRuntimePlatform.setText("Platform:");
                break;
            default: // "Book" or any other case
                labelAuthorDirectorGenre.setText("Authors:");
                labelPagesRuntimePlatform.setText("Pages:");
                break;
        }
    }

    public void AddItemHandler(ActionEvent actionEvent) throws IOException {
        if (TitleField.getText().isEmpty() || ImageLink.getText().isEmpty() || DescriptionField.getText().isEmpty() ||
        AuthorsField.getText().isEmpty() || PagesField.getText().isEmpty() || itemTypeComboBox.getValue() == null) {
            AllFieldsRequiredText.setVisible(true);
        } else {
            AllFieldsRequiredText.setVisible(false);
            String Type = itemTypeComboBox.getValue();
            String authors = AuthorsField.getText();
            String[] authorsarray = authors.split("\\s*,\\s*");
            String Title = TitleField.getText();
            String Image = ImageLink.getText();
            String Description = DescriptionField.getText();
            String Pages = PagesField.getText();
            Document document = new Document("Title", Title)
                    .append(labelAuthorDirectorGenre.getText().replace(":",""), Arrays.asList(authorsarray))
                    .append("Image", Image)
                    .append("Description", Description)
                    .append("Type", Type)
                    .append(labelPagesRuntimePlatform.getText().replace(":", ""), Pages)
                    .append("CheckedOut", false)
                    .append("Checked Out Users", Collections.emptyList())
                    .append("Holds", Collections.emptyList())
                    .append("Current Checked Out", "");
            writer.sendDocument(document);
            Back_Button_Click(actionEvent);
        }
    }

    public void Back_Button_Click(javafx.event.ActionEvent event) throws IOException {
        // Load the new FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("GUIClasses/LibrarianLoggedIn.fxml"));
        Parent root = fxmlLoader.load();

        LibrarianLoggedInController controller = fxmlLoader.getController();
        controller.setReader(reader);
        controller.setWriter(writer);
        controller.setClient(client);
        controller.setLogin(Username);
        controller.Refresh();
        // Create a new scene with the new FXML file
        Scene scene = new Scene(root, 800, 800);

        // Get the stage and set the new scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }
}
