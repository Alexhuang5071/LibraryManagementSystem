package ClientPack;
/*
 * EE422C Final Project submission by
 * Replace <... ...> with your actual data.
 * <Alex Huang>
 * <ah57984>
 * <17185>
 * Spring 2023
 */
import Common.LoginMessage;
import org.bson.Document;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Writer {
    private Socket socket;
    private ObjectOutputStream outputStream;

    public Writer(Socket socket) throws IOException {
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    public void sendLoginMessage(LoginMessage message) throws IOException {
        outputStream.writeObject(message);
        outputStream.flush();

        System.out.println("Sending Login Message " + message.toString());
    }


    public void sendDocument(Document document) {
        try {
            outputStream.writeObject(document);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Sending Document: " + document);
    }

    public void sendString(String message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Sending String: " + message);
    }

}

