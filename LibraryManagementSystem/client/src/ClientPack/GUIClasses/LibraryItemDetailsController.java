/*
 * EE422C Final Project submission by
 * Replace <... ...> with your actual data.
 * <Alex Huang>
 * <ah57984>
 * <17185>
 * Spring 2023
 */
package ClientPack.GUIClasses;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.bson.Document;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LibraryItemDetailsController {
    public ImageView itemImage;
    public Label itemTitle;
    public Label itemType;
    public Label Authors_Label;
    public Label itemAuthors;
    public Label Pages_Label;
    public Label itemPages;
    public Label itemAvailability;
    public Label itemDescription;
    public Label itemLastCheckedOut;
    public Label itemCheckedOutUsers;
    public Label itemCurrentlyCheckedOutBy;
    public Button Checkout_Button;
    public Button Close_Button;
    public Label Holds_List;
    private LibraryItemController libraryItemController;
    private String username;
    private boolean Librarian = false;

    public void setLibraryItemController(LibraryItemController libraryItemController) {
        this.libraryItemController = libraryItemController;
    }
    public void setLibrarian(boolean librarian) {
        this.Librarian = librarian;
    }


    public void setItemData(Document item, String username) {
        this.username = username;
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
        itemCheckedOutUsers.setText(String.join(", ", item.getList("Checked Out Users", String.class)));


        Holds_List.setText(String.join(", ", item.getList("Holds", String.class)));
        if (Holds_List.getText().isEmpty()) {
            Holds_List.setText("None");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date lastCheckedOutDate = item.getDate("Last Checked Out"); // Retrieve the Date object directly
        Timestamp lastCheckedOut = new Timestamp(lastCheckedOutDate.getTime()); // Convert Date to Timestamp
        itemLastCheckedOut.setText(sdf.format(lastCheckedOut));
        if (itemCheckedOutUsers.getText().isEmpty()) {
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
            if (Holds_List.getText().contains(username)) { //Means User Already Held Book
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


    public void Checkout_Button_Click(ActionEvent actionEvent) {
        Stage stage = (Stage) Close_Button.getScene().getWindow();
        libraryItemController.Checkout_Button_Click(actionEvent);
        stage.close();
    }

    @FXML
    private void Close_Button_Click(ActionEvent event) {
        Stage stage = (Stage) Close_Button.getScene().getWindow();
        stage.close();
    }

}
