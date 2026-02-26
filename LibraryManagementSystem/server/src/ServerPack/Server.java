/*
 * EE422C Final Project submission by
 * Replace <... ...> with your actual data.
 * <Alex Huang>
 * <ah57984>
 * <17185>
 * Spring 2023
 */
package ServerPack;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Server extends Observable {
    public static void main(String[] args) {
        new Server().runServer();
    }

    private void runServer() {
        try {
            SetUpNetworking();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void SetUpNetworking() throws IOException {
        ServerSocket serverSock;
        try {
            serverSock = new ServerSocket(4242);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            Socket clientSocket = serverSock.accept();
            System.out.println("Connecting to..." + clientSocket);

            Reader reader = new Reader(clientSocket);

            Thread r = new Thread(reader);
            r.start();
        }
    }


}