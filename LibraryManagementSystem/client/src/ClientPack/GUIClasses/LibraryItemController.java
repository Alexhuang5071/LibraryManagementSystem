/*
 * EE422C Final Project submission by
 * Replace <... ...> with your actual data.
 * <Alex Huang>
 * <ah57984>
 * <17185>
 * Spring 2023
 */
package ClientPack.GUIClasses;

import ClientPack.Reader;
import ClientPack.Writer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LibraryItemController {
    @FXML
    public Button Checkout_Button;
    public Label itemCurrentlyCheckedOutBy;
    public Label Pages_Label;
    public Label Authors_Label;
    public Label Hold_List;
    @FXML
    private ImageView itemImage;
    @FXML
    private Hyperlink itemTitle;
    @FXML
    private Label itemType;
    @FXML
    private Label itemAuthors;
    @FXML
    private Label itemPages;
    @FXML
    private Label itemAvailability;
    @FXML
    private Label itemLastCheckedOut;
    @FXML
    private Label itemDescription;
    private ObjectId objectId;
    private Reader reader;
    private Writer writer;
    private String username;
    private Document item;
    private boolean Librarian = false; //Tells Item its librarian accessing it


    public void LibrarianAccess() {
        Librarian = true;
    }
    public void setReader(Reader reader) {
        this.reader = reader;
        reader.setLibraryItemController(this);
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setItemData(Document item) {
        this.item = item;
        objectId = item.getObjectId("_id");
        itemTitle.setText(item.getString("Title"));
        itemType.setText(item.getString("Type"));
        if (item.getString("Type").equals("Movie")) {
            itemPages.setText(item.getString("Runtime"));
            itemAuthors.setText(String.join(", ", item.getList("Directors", String.class)));
            Pages_Label.setText("Runtime: ");
            Authors_Label.setText("Directors: ");
        } else if (item.getString("Type").equals("Book")) {
            itemPages.setText(item.getString("Pages"));
            itemAuthors.setText(String.join(", ", item.getList("Authors", String.class)));
        } else if (item.getString("Type").equals("Game")) {
            itemPages.setText(item.getString("Platform"));
            itemAuthors.setText(String.join(", ", item.getList("Genres", String.class)));
            Pages_Label.setText("Platform: ");
            Authors_Label.setText("Genres: ");
        }
        itemAvailability.setText(item.getBoolean("CheckedOut") ? "Not Available" : "Available");
        String UsersCheckedOut = String.join(", ", item.getList("Checked Out Users", String.class));
        Hold_List.setText(String.join(", ", item.getList("Holds", String.class)));
        if (Hold_List.getText().isEmpty()) {
            Hold_List.setText("None");
        }


        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date lastCheckedOutDate = item.getDate("Last Checked Out"); // Retrieve the Date object directly
        Timestamp lastCheckedOut = new Timestamp(lastCheckedOutDate.getTime()); // Convert Date to Timestamp
        itemLastCheckedOut.setText(sdf.format(lastCheckedOut));
        if (UsersCheckedOut.isEmpty()) {
            itemLastCheckedOut.setText("N/A");
        }

        // Set the "Currently Checked Out by:" field
        String currentCheckedOut = item.getString("Current Checked Out");
        if (currentCheckedOut == null || currentCheckedOut.isEmpty()) {
            Checkout_Button.setText("Checkout");
            itemCurrentlyCheckedOutBy.setText("None");
        } else if (currentCheckedOut.equals(username)) {
            Checkout_Button.setText("Return");
            itemCurrentlyCheckedOutBy.setText(currentCheckedOut);
        } else { //Means book is checked out by different User
            itemCurrentlyCheckedOutBy.setText(currentCheckedOut);
            if (Hold_List.getText().contains(username)) { //Means User Already Held Book
                Checkout_Button.setText("Cancel Hold");
            } else {
                Checkout_Button.setText("Hold");
            }
        }

        if (Librarian) {
            Checkout_Button.setText("Delete");
        }

        itemDescription.setText(item.getString("Description"));

        String imageUrl = item.getString("Image");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            itemImage.setImage(new Image(imageUrl));
        }
    }

    public void showStolenBookAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Oh No! Darnit!");
            alert.setHeaderText(null);
            alert.setContentText(itemTitle.getText() + " was stolen!");
            alert.showAndWait();
        });
    }

    public void showSuccessfulCheckoutAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Yippe Yay! Get to Reading!");
            alert.setHeaderText(null);
            alert.setContentText(itemTitle.getText() + " successfully checked out!");
            alert.showAndWait();
        });
    }

    public void showSuccessfulReturn() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thanks for Reading!");
            alert.setHeaderText(null);
            alert.setContentText(itemTitle.getText() + " successfully returned!");
            alert.showAndWait();
        });
    }

    public void showSuccessfulHold() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("In No Time It'll Be Yours!");
            alert.setHeaderText(null);
            alert.setContentText(itemTitle.getText() + " successfully held!");
            alert.showAndWait();
        });
    }

    public void showSuccessfulCancelHold() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Patience a Virtue :(");
            alert.setHeaderText(null);
            alert.setContentText(itemTitle.getText() + " successfully Un-Held :(");
            alert.showAndWait();
        });
    }
    public void showUnSuccessfulCancelHold() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Hmm seems like you got!");
            alert.setHeaderText(null);
            alert.setContentText(itemTitle.getText() + " unsuccessfully Un-Held. Seems like you got the book!");
            alert.showAndWait();
        });
    }


    public void Checkout_Button_Click(ActionEvent actionEvent) {
        if (Checkout_Button.getText().equals("Checkout")) {
            System.out.println("Checking Out Book");
            Date currentDate = new Date(); // Get the current date
            Document document = new Document("_id", objectId)
                    .append("Username", username)
                    .append("Last Checked Out", currentDate); // Store as Date object

            //Sends id of book and username of loggedIn user and time of checkout
            reader.setLibraryItemController(this); //So reader knows which instance to call
            writer.sendDocument(document);
        } else if (Checkout_Button.getText().equals("Return")) {
            System.out.println("Returning Book");
            Document document = new Document("_id", objectId)
                    .append("Username", username);
            reader.setLibraryItemController(this);
            writer.sendDocument(document);
        } else if (Checkout_Button.getText().equals("Cancel Hold")) {
            System.out.println("Cancelling Hold");
            Document document = new Document("_id", objectId)
                    .append("Username", username)
                    .append("CancelHold", "");
            reader.setLibraryItemController(this);
            writer.sendDocument(document);
        } else if (Checkout_Button.getText().equals("Hold")){ //Else Means button says Hold
            System.out.println("Holding Book");
            Document document = new Document("_id", objectId)
                    .append("Username", username)
                    .append("Holding", "");
            reader.setLibraryItemController(this);
            writer.sendDocument(document);
        } else { //Means it's a librarian account and it says delete
            System.out.println("Deleting Book");
            Document document = new Document("_id", objectId)
                    .append("Username", username)
                    .append("Deleting", "");
            reader.setLibraryItemController(this);
            writer.sendDocument(document);
        }
    }


    public void itemTitleClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LibraryItemDetails.fxml"));
            BorderPane root = loader.load();

            LibraryItemDetailsController detailsController = loader.getController();
            detailsController.setLibrarian(Librarian);
            detailsController.setItemData(item, username);
            detailsController.setLibraryItemController(this);
            // Set the item details using the detailsController methods

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Item Details");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


