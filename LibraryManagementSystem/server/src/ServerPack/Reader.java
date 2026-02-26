package ServerPack;
/*
 * EE422C Final Project submission by
 * Replace <... ...> with your actual data.
 * <Alex Huang>
 * <ah57984>
 * <17185>
 * Spring 2023
 */
import Common.LoginMessage;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Reader implements Runnable {
    private Socket socket;
    private Writer writer;
    private MongoDB mongoDB;
    private MongoDB LibraryCatalog;
    private static final Object mongoDBLock = new Object(); //Ensure only 1 collection is changed at once
    private static final Object libraryCatalogLock = new Object();

    private Map<String, String> VerificationCode = new HashMap<>(); //Contains Username as key and Verification code as value

    public Reader(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            writer = new Writer(socket);
            mongoDB = new MongoDB("Library", "Logins");
            LibraryCatalog = new MongoDB("Library", "E-Library");

            // Read objects from the input stream
            while (true) {
                Object object = inputStream.readObject();
                System.out.println(object);

                // Check if the object is a LoginMessage
                if (object instanceof LoginMessage) {
                    LoginMessage loginMessage = (LoginMessage) object;
                    System.out.println("Received login message: " + loginMessage);

                    // Create a MongoDB document from the login message
                    Document document = new Document("Username", loginMessage.getUsername());
                    System.out.println(document);

                    // Check if the user exists in the database and send the result to the client
                    Document user;
                    synchronized (mongoDBLock) {
                        user = mongoDB.findUser(document); //user will contain hashedpassword
                    }
                    boolean loginSuccess = false;
                    if (user == null) {
                        writer.sendString("Login Failed");
                    } else {
                        if (verifyPassword(loginMessage.getPassword(), user.getString("Password"))) {
                            if (user.getBoolean("Librarian")) {
                                writer.sendString("Librarian Logged In");
                            } else {
                                writer.sendString("Login Success");
                            }
                            loginSuccess = true;
                        } else {
                            writer.sendString("Login Failed");
                        }
                    }
                    // If the login was successful, send the library catalog to the client
                    if (loginSuccess) {
                        synchronized (libraryCatalogLock) {
                            FindIterable<Document> catalog = LibraryCatalog.FindCatalog();
                            writer.SendLibrary(catalog);
                        }
                    }
                }
                // Check if the object is a MongoDB Document
                else if (object instanceof Document) {
                    Document document = (Document) object;
                    if (EmailOnly(document)) {
                        System.out.println("Received document: " + document); //Document Contains just Email
                        if (mongoDB.IfUserExists(document)) { //Synchronized in Method
                            writer.sendString("Email Exists");
                        } else {
                            writer.sendString("Email Free");
                        }
                    } else if (EmailOnlyButForResettting(document)) {
                        System.out.println("Received document: " + document);
                        Document temp = new Document("Email", document.getString("Email"));
                        if (mongoDB.IfUserExists(temp)) {
                            writer.sendString("Email Exists for Password Resetting");
                        } else {
                            writer.sendString("Email Invalid for Password Resetting");
                        }
                    } else if (Usernameonly(document)) {
                        System.out.println("Received document: " + document);
                        if(mongoDB.IfUserExists(document)) {
                            writer.sendString("Username Exists");
                        } else {
                            writer.sendString("Username Free");
                        }
                    } else if (NewAccountCheck(document)) { //First Instance of Account Creation
                        //String Code = "12345";
                        String Code = RandomCodeGenerator(); //
                        VerificationCode.put(document.getString("Email"), Code); //Puts verification code in map
                        String email = document.getString("Email");
                        SendMail.sendEmail(email, "Account Verification", "Please enter " + Code + "."); //
                    } else if (NewAccountWithCode(document)) { //Client Passes back account details with code
                        if (document.getString("VerificationCode").equals(VerificationCode.get(document.getString("Email")))) { //Checks map to see if verification code matches
                            //Password Hashing
                            String oldPassword = document.getString("Password");
                            String hashedPassword = BCrypt.withDefaults().hashToString(12, oldPassword.toCharArray());
                            document.put("Password", hashedPassword);
                            System.out.println("Hashed Password: " + hashedPassword);
                            synchronized (mongoDBLock) {
                                mongoDB.CreateAccount(document);
                            }
                            System.out.println("Account Created " + document);
                            writer.sendString("Account Created");
                        } else {
                            writer.sendString("Verification Incorrect");
                        }
                    } else if (CheckingOut(document)) { // Contains _id and Username and Date
                        String email;
                        synchronized (mongoDBLock) {
                            email = mongoDB.getEmail(document);
                        }
                        String title = LibraryCatalog.getBookbyId(document);
                        ObjectId objectId = document.getObjectId("_id");
                        String Username = document.getString("Username");
                        Date lastCheckedOutDate = document.getDate("Last Checked Out"); // Get the Date object
                        Timestamp timestamp = new Timestamp(lastCheckedOutDate.getTime()); // Convert Date to java.sql.Timestamp

                        Document documentwithId = new Document("_id", objectId); //ID of book
                        Document documentwithUsername = new Document("Username", Username); //Username of account
                        synchronized (libraryCatalogLock) {
                            if (LibraryCatalog.CheckOutBookCheck(documentwithId)) { //If true, book's been checked out
                                writer.sendString("Book already checked out");
                            } else {
                                synchronized (mongoDBLock) {
                                    mongoDB.AddBookToUser(documentwithId, documentwithUsername); //Add's the book to the User's list of checkedoutbooks
                                }
                                LibraryCatalog.AddUserToBook(documentwithUsername, documentwithId, timestamp);
                                writer.sendString("Book successfully checked out");
                                //Comment Out Later
                                SendMail.sendEmail(email, "Get to Reading!", "You've successfully checked out " + title + "!");
                            }
                        }
                        System.out.println("Checking Out Book: " + document);
                    } else if (Returning(document)) {
                        synchronized (libraryCatalogLock) {
                            LibraryCatalog.RemoveUserFromBook(document);
                        }
                        synchronized (mongoDBLock) {
                            mongoDB.RemoveBookFromUser(document);
                        }
                        writer.sendString("Book Returned");
                    } else if (Holding(document)) {
                        synchronized (libraryCatalogLock) {
                            LibraryCatalog.AddUserToHoldBook(document);
                        }
                        synchronized (mongoDBLock) {
                            mongoDB.AddBookToUserHoldList(document);
                        }
                        writer.sendString("Book Successfully Held");
                    } else if (CancelingHold(document)) {
                        if (LibraryCatalog.CheckHoldList(document)) {
                            synchronized (libraryCatalogLock) {
                                LibraryCatalog.RemoveUserToHoldBook(document);
                            }
                            synchronized (mongoDBLock) {
                                mongoDB.RemoveBookToUserHoldList(document);
                            }
                            writer.sendString("Book Successfully UnHeld");
                        } else { //Meaning User Checked Out Book Because Someone else returned it
                            writer.sendString("Book Unsuccessfully UnHeld");
                        }
                    } else if (ForgotPassword(document)) { //Document Contains Email and ForgotPassword key
                        //String Code = "12345";
                        //Comment Out
                        String Code = RandomCodeGenerator();
                        VerificationCode.put(document.getString("Email").toLowerCase(), Code); //Puts verification code in map
                        String email = document.getString("Email");
                        SendMail.sendEmail(email, "Account Verification", "Please enter " + Code + ".");
                    } else if (ForgotPasswordWithCode(document)) { //Sends back document with email and verification code
                        if (document.getString("VerificationCode").equals(VerificationCode.get(document.getString("Email")).toLowerCase())) { //Checks map to see if verification code matches
                            writer.sendString("PasswordForgotCodeCorrect");
                        } else {
                            writer.sendString("PasswordForgotCodeIncorrect");
                        }
                    } else if (ResettingPasswordValidated(document)) { //Document contains Email and new Password and been validated
                        String oldPassword = document.getString("Password");
                        String hashedPassword = BCrypt.withDefaults().hashToString(12, oldPassword.toCharArray());
                        document.put("Password", hashedPassword);
                        System.out.println("Hashed Password: " + hashedPassword);
                        synchronized (mongoDBLock) {
                            mongoDB.ChangePassword(document);
                        }
                        System.out.println("Password Changed " + document);
                    } else if (ChangePassword(document)) {
                        String oldPassword = document.getString("Password");
                        String hashedPassword = BCrypt.withDefaults().hashToString(12, oldPassword.toCharArray());
                        document.put("Password", hashedPassword);
                        System.out.println("Hashed Password: " + hashedPassword);
                        synchronized (mongoDBLock) {
                            mongoDB.ChangePasswordWithUsername(document);
                        }
                        System.out.println("Password Changed " + document);
                    } else if (Deleting(document)) {
                        synchronized (libraryCatalogLock) {
                            if (LibraryCatalog.BookExistsWithId(document)) {
                                LibraryCatalog.DeletingItem(document);
                                writer.sendString("Deleted Item");
                            } else {
                                writer.sendString("Book Already Deleted");
                            }

                        }
                    } else if (AddingBook(document)) {
                        synchronized (libraryCatalogLock) {
                            LibraryCatalog.AddItem(document);
                        }
                    }
                } else if (object instanceof String) {
                    String message = (String) object;
                    if (message.equals("Refresh List")) {
                        FindIterable<Document> catalog = LibraryCatalog.FindCatalog();
                        writer.SendLibrary(catalog);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean AddingBook(Document document) {
        return document.containsKey("Type") && document.containsKey("Title") && document.containsKey("Image");
    }

    private boolean Deleting(Document document) {
        return document.containsKey("_id") && document.containsKey("Username") && document.containsKey("Deleting");
    }
    private boolean ResettingPasswordValidated(Document document) {
        return document.containsKey("Email") && document.containsKey("Password") && document.containsKey("PasswordResetValidated");
    }
    private boolean ForgotPassword(Document document) {
        return document.containsKey("Email") && document.containsKey("ForgotPassword");
    }

    private boolean ForgotPasswordWithCode(Document document) {
        return document.containsKey("Email") && document.containsKey("ForgotPasswordCode");
    }

    private String RandomCodeGenerator() {
        SecureRandom secureRandom = new SecureRandom();
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
        return result.verified;
    }

    private boolean NewAccountCheck(Document document) {
        return document.containsKey("Username") && document.containsKey("Email") && document.containsKey("Password")
                && document.containsKey("First Name") && document.containsKey("Last Name") && document.containsKey("NeedToVerify");
    }

    private boolean ChangePassword(Document document) {
        return document.containsKey("Username") && document.containsKey("Password");
    }

    private boolean NewAccountWithCode(Document document) {
        return document.containsKey("Username") && document.containsKey("Email") && document.containsKey("Password")
                && document.containsKey("First Name") && document.containsKey("Last Name") && document.containsKey("VerificationCode");
    }

    private boolean EmailOnly(Document document) {
        if (document.size() == 1) {
            return !document.containsKey("Username") && document.containsKey("Email") && !document.containsKey("Password")
                    && !document.containsKey("First Name") && !document.containsKey("Last Name") && !document.containsKey("ForgotPassword")
                    && !document.containsKey("ForgotPasswordCode") && !document.containsKey("PasswordResetValidated") && !document.containsKey("EmailOnlyCheckForPasswordForget");
        }
        return false;
    }

    private boolean EmailOnlyButForResettting(Document document) {
        if (document.size() == 2) {
            return document.containsKey("Email") && document.containsKey("EmailOnlyCheckForPasswordForget");
        }
        return false;
    }

    private boolean Usernameonly(Document document) {
        if (document.size() == 1) {
            return document.containsKey("Username") && !document.containsKey("Email") && !document.containsKey("Password")
                    && !document.containsKey("First Name") && !document.containsKey("Last Name");
        }
        return false;
    }

    private boolean CheckingOut(Document document) {
        return document.containsKey("_id") && document.containsKey("Username") && document.containsKey("Last Checked Out");
    }

    private boolean Returning(Document document) {
        return document.containsKey("_id") && document.containsKey("Username") && !document.containsKey("Deleting") && !document.containsKey("Last Checked Out") && !document.containsKey("Holding") && !document.containsKey("CancelHold");
    }

    private boolean Holding(Document document) {
        return document.containsKey("_id") && document.containsKey("Username") && document.containsKey("Holding"); //Holding is Key
    }

    private boolean CancelingHold(Document document) {
        return document.containsKey("_id") && document.containsKey("Username") && document.containsKey("CancelHold"); //Holding is Key
    }
}



