package ServerPack;
/*
 * EE422C Final Project submission by
 * Replace <... ...> with your actual data.
 * <Alex Huang>
 * <ah57984>
 * <17185>
 * Spring 2023
 */
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MongoDB {
    private MongoClient mongoClient;
    private static MongoClient mongo;
    private MongoDatabase database;
    private MongoCollection<Document> login_info;
    private static final Object key = new Object();

    private static final String URI = "mongodb://alexhuang5071:12345@ac-izknbsn-shard-00-00.nf7norw.mongodb.net:27017,ac-izknbsn-shard-00-01.nf7norw.mongodb.net:27017,ac-izknbsn-shard-00-02.nf7norw.mongodb.net:27017/?ssl=true&replicaSet=atlas-c7lsdb-shard-0&authSource=admin&retryWrites=true&w=majority";

    public MongoDB(String databaseName, String collectionName) {


        // Connect to MongoDB server

        mongo = MongoClients.create(URI);


        // Get database and collection
        database = mongo.getDatabase(databaseName);
        login_info = database.getCollection(collectionName);
    }

    public void DeletingItem(Document document) { //Document contains _id
        login_info.deleteOne(Filters.eq("_id", document.getObjectId("_id")));
    }

    public boolean BookExistsWithId(Document document) {
        ObjectId objectId = document.getObjectId("_id");
        Document search = new Document("_id", objectId);
        Document document1 = login_info.find(search).first();
        if (document1 != null) {
            boolean CheckedOut = document1.getBoolean("CheckedOut");
            return !CheckedOut;
        }
        return false;
    }


    public FindIterable<Document> FindCatalog() {
        return login_info.find();
    }

    public Document findUser(Document document) {
        return login_info.find(document).first();
    }

    public Boolean IfUserExists(Document document) {
        synchronized (key) {
            Document result = login_info.find(document).first();
            return result != null;
        }
    }

    public void AddItem(Document document) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        login_info.insertOne(document.append("Last Checked Out", timestamp));
    }


    public void CreateAccount(Document document) {
        document.append("CheckedOutBooks", Collections.emptyList())
                .append("Librarian", false)
                .append("Hold List", Collections.emptyList());
        String email = document.getString("Email").toLowerCase();
        document.put("Email", email); //Ensures Emails stored on database is lowercase
        login_info.insertOne(document);
    }

    public String getEmail(Document document) {
        Document username = new Document("Username", document.getString("Username"));
        return login_info.find(username).first().getString("Email");
    }

    public String getBookbyId(Document document1) {
        ObjectId bookId = document1.getObjectId("_id");
        Document document = new Document("_id", bookId);
        return login_info.find(document).first().getString("Title");
    }

    public void AddBookToUser(Document documentwithId, Document documentwithUsername) {
        ObjectId bookId = documentwithId.getObjectId("_id");
        String username = documentwithUsername.getString("Username");

        login_info.updateOne(
                Filters.eq("Username", username),
                Updates.push("CheckedOutBooks", bookId)
        );
    }

    public void AddUserToBook(Document documentwithUsername, Document documentwithId, Timestamp timestamp) {
        ObjectId bookId = documentwithId.getObjectId("_id");
        String username = documentwithUsername.getString("Username");

        Date lastCheckedOutDate = new Date(timestamp.getTime()); // Convert Timestamp to Date

        login_info.updateOne(
                Filters.eq("_id", bookId),
                Updates.combine(
                        Updates.push("Checked Out Users", username),
                        Updates.set("Last Checked Out", lastCheckedOutDate), // Update with Date object
                        Updates.set("CheckedOut", true),
                        Updates.set("Current Checked Out", username)
                )
        );
    }

    public void RemoveUserFromBook(Document document) { //Passed in Document contains ObjectId first, then Username
        ObjectId bookId = document.getObjectId("_id");

        login_info.updateOne(
                Filters.eq("_id", bookId),
                Updates.combine(
                        Updates.set("CheckedOut", false),
                        Updates.set("Current Checked Out", "")
                )
        ); //User has been removed from book

        Document bookDocument = login_info.find(Filters.eq("_id", bookId)).first();
        //Check if the document has the "Holds" field and if it has any elements
        if (bookDocument != null) {
            List<String> holds = bookDocument.getList("Holds", String.class);
            if (holds != null && !holds.isEmpty()) {
                //Get the first hold
                String firstHold = holds.get(0); //Username of person to get book next
                System.out.println("First hold: " + firstHold);

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                Document documentwithname = new Document("Username", firstHold);
                Document documentwithId = new Document("_id", bookId);
                AddUserToBook(documentwithname, documentwithId, timestamp); //Adds new user to the book

                //Now we need to add book to user
                MongoCollection<Document> temp = database.getCollection("Logins");
                String email = temp.find(documentwithname).first().getString("Email"); //Returns User's Email
                String title = login_info.find(Filters.eq("_id", bookId)).first().getString("Title"); //Gets Title of Book
                SendMail.sendEmail(email, "Waiting has paid off!", "You have now checked out " + title + "! You were able to check out the item off of the hold list. Log in to access your new content. Told you patience is a virtue. :)");
                temp.updateOne(
                        Filters.eq("Username", firstHold),
                        Updates.combine(
                                Updates.push("CheckedOutBooks", bookId), //Adds book to user
                                Updates.pullByFilter(Filters.eq("Hold List", bookId))  //Book off user hold list
                        )
                );

                // Remove the firstHold from the "Holds" array in the database
                login_info.updateOne(
                        Filters.eq("_id", bookId),
                        Updates.pullByFilter(Filters.eq("Holds", firstHold))
                );
            }
        }
    }

    public void ChangePassword(Document document) { //Document contains Email and New Password
        String email = document.getString("Email");
        String pass = document.getString("Password");
        login_info.updateOne(
                Filters.eq("Email", email),
                Updates.set("Password", pass)
        );
    }

    public void ChangePasswordWithUsername(Document document) {
        String Username = document.getString("Username");
        String pass = document.getString("Password");
        login_info.updateOne(
                Filters.eq("Username", Username),
                Updates.set("Password", pass)
        );
    }

    public void RemoveUserToHoldBook(Document document) {
        ObjectId bookId = document.getObjectId("_id");
        String Username = document.getString("Username");

        login_info.updateOne(
                Filters.eq("_id", bookId),
                Updates.combine(
                        Updates.pull("Holds", Username)
                )
        );
    }

    public boolean CheckHoldList(Document document) {
        ObjectId bookId = document.getObjectId("_id");
        String username = document.getString("Username");
        Document bookDocument = login_info.find(Filters.eq("_id", bookId)).first();
        //Check if the document has the "Holds" field and if it has any elements
        if (bookDocument != null) {
            List<String> holds = bookDocument.getList("Holds", String.class); //Get Hold list of books
            return holds.contains(username);
        }
        return false;
    }

    public void AddUserToHoldBook(Document document) { //Passed in Document contains ObjectId first, Username, then Hold List
        ObjectId bookId = document.getObjectId("_id");
        String Username = document.getString("Username");

        login_info.updateOne(
                Filters.eq("_id", bookId),
                Updates.combine(
                        Updates.push("Holds", Username)
                )
        );
    }

    public void AddBookToUserHoldList(Document document) {
        ObjectId bookId = document.getObjectId("_id");
        String Username = document.getString("Username");

        login_info.updateOne(
                Filters.eq("Username", Username),
                Updates.combine(
                        Updates.push("Hold List", bookId)
                )
        );
    }

    public void RemoveBookToUserHoldList(Document document) {
        ObjectId bookId = document.getObjectId("_id");
        String Username = document.getString("Username");

        login_info.updateOne(
                Filters.eq("Username", Username),
                Updates.combine(
                        Updates.pull("Hold List", bookId)
                )
        );
    }

    public void RemoveBookFromUser(Document document) {
        ObjectId bookId = document.getObjectId("_id");
        String username = document.getString("Username");

        login_info.updateOne(
                Filters.eq("Username", username),
                Updates.pull("CheckedOutBooks", bookId)
        );


    }


    public boolean CheckOutBookCheck(Document document) {
        Document book = login_info.find(document).first(); //Book
        return book.getBoolean("CheckedOut");
    }

    public boolean DuplicateAccountCheck(Document document) { //Returns true if account already exists
        Document result = login_info.find(document).first();
        return result != null;
    }

    public void closeConnection() {
        // Close connection to MongoDB server
        mongoClient.close();
    }
}
