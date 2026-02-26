package ServerPack;
/*
 * EE422C Final Project submission by
 * Replace <... ...> with your actual data.
 * <Alex Huang>
 * <ah57984>
 * <17185>
 * Spring 2023
 */
import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Writer {
    private Socket socket;
    private ObjectOutputStream outputStream;

    public Writer(Socket socket) throws IOException {
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }


    public void sendString(String message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void SendLibrary(FindIterable<Document> documents) {
        try {
            // Convert FindIterable<Document> to List<Document>
            List<Document> documentList = new ArrayList<>();
            for (Document doc : documents) {
                // Retrieve the Date field
                Date lastCheckedOutDate = doc.getDate("Last Checked Out");
                if (lastCheckedOutDate != null) {
                    java.sql.Timestamp timestamp = new java.sql.Timestamp(lastCheckedOutDate.getTime()); // Convert to java.sql.Timestamp
                    doc.put("Last Checked Out", timestamp); // Replace Date with java.sql.Timestamp
                }

                documentList.add(doc);
            }

            // Send the list of documents
            outputStream.writeObject(documentList);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

