/*
 * EE422C Final Project submission by
 * Replace <... ...> with your actual data.
 * <Alex Huang>
 * <ah57984>
 * <17185>
 * Spring 2023
 */
package ClientPack;

import ClientPack.GUIClasses.*;
import org.bson.Document;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;

public class Reader implements Runnable {
    Socket socket;
    SignUpController signUpController;
    GUIController guiController;
    LoggedInController loggedInController;
    LibraryItemController libraryItemController;
    ForgotPasswordController forgotPasswordController;
    LibrarianLoggedInController librarianLoggedInController;
    AddBookController addBookController;


    public Reader(Socket socket) {
        this.socket = socket;
    }
    public void setLibrarianLoggedInController(LibrarianLoggedInController librarianLoggedInController) {
        this.librarianLoggedInController = librarianLoggedInController;
    }

    public void setSignUpController(SignUpController signUpController) {
        this.signUpController = signUpController;
    }

    public void setGUIController(GUIController guiController) {
        this.guiController = guiController;
    }

    public void setLoggedInController(LoggedInController loggedInController) {
        this.loggedInController = loggedInController;
    }
    public void setForgotPasswordController(ForgotPasswordController forgotPasswordController) {
        this.forgotPasswordController = forgotPasswordController;
    }

    public void setLibraryItemController(LibraryItemController libraryItemController) {
        this.libraryItemController = libraryItemController;
    }

    public void ClearLoggedInController() {
        loggedInController = null;
    }

    public void ClearLibrarianLoggedInController() {
        librarianLoggedInController = null;
    }

    public void setAddBookController(AddBookController addBookController) {
        this.addBookController = addBookController;
    }

    public void run() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            // Read objects from the input stream
            while (true) {
                Object data = inputStream.readObject();

                if (data instanceof List) {
                    List<Document> documents = (List<Document>) data;
                    if (librarianLoggedInController != null) {
                        librarianLoggedInController.setItems(documents);
                    }
                    if (loggedInController != null) {
                        loggedInController.setItems(documents);
                    }
                    // Iterate over the documents and call the addItem method
//                    for (Document doc : documents) {
//                        loggedInController.addItem(doc);
//                    }


                    System.out.println(documents);
                } else if (data instanceof String) {
                    String string = (String) data;
                    if (string.equals("Email Exists")) {
                        System.out.println(string);
                        signUpController.Email_Taken_Handle();
                    } else if (string.equals("Email Free")) {
                        System.out.println(string);
                        signUpController.Email_Free();
                    } else if (string.equals("Username Exists")) {
                        System.out.println(string);
                        signUpController.Username_Taken_Handle();
                    } else if (string.equals("Username Free")) {
                        System.out.println(string);
                        signUpController.Username_Free();
                    } else if (string.equals("Verification Incorrect")) {
                        System.out.println(string);
                        signUpController.VerificationIncorrect();
                    } else if (string.equals("Account Created")) {
                        System.out.println(string);
                        signUpController.AccountCreated();
                    } else if (string.equals("Login Success")) {
                        System.out.println(string);
                        guiController.Login_Success();
                    } else if (string.equals("Login Failed")) {
                        System.out.println(string);
                        guiController.Login_Failed();
                    } else if (string.equals("Book already checked out")) {
                        System.out.println(string);
                        libraryItemController.showStolenBookAlert();
                        loggedInController.Refresh();
                    } else if (string.equals("Book successfully checked out")) {
                        System.out.println(string);
                        libraryItemController.showSuccessfulCheckoutAlert();
                        loggedInController.Refresh();
                    } else if (string.equals("Book Returned")) {
                        System.out.println(string);
                        libraryItemController.showSuccessfulReturn();
                        loggedInController.Refresh();
                    } else if (string.equals("Book Successfully Held")) {
                        System.out.println(string);
                        libraryItemController.showSuccessfulHold();
                        loggedInController.Refresh();
                    } else if (string.equals("Book Successfully UnHeld")) {
                        System.out.println(string);
                        libraryItemController.showSuccessfulCancelHold();
                        loggedInController.Refresh();
                    } else if (string.equals("Book Unsuccessfully UnHeld")) {
                        System.out.println(string);
                        libraryItemController.showUnSuccessfulCancelHold();
                        loggedInController.Refresh();
                    } else if (string.equals("PasswordForgotCodeIncorrect")) {
                        System.out.println(string);
                        forgotPasswordController.CodeIncorrect();
                    } else if (string.equals("PasswordForgotCodeCorrect")) {
                        System.out.println(string);
                        forgotPasswordController.CodeCorrect();
                    } else if (string.equals("Email Exists for Password Resetting")) {
                        System.out.println(string);
                        forgotPasswordController.EmailValid();
                    } else if (string.equals("Email Invalid for Password Resetting")) {
                        System.out.println(string);
                        forgotPasswordController.InvalidEmail();
                    } else if(string.equals("Librarian Logged In")) {
                        System.out.println(string);
                        guiController.LibrarianLoginSuccess();
                    } else if (string.equals("Deleted Item")) {
                        System.out.println(string);
                        librarianLoggedInController.Refresh();
                    } else if (string.equals("Book Already Deleted")) {
                        System.out.println(string);
                        librarianLoggedInController.FailedDelete();
                        librarianLoggedInController.Refresh();
                    }
                } else {
                    System.out.println("Received data: " + data);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

