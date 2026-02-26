/*
 * EE422C Final Project submission by
 * Replace <... ...> with your actual data.
 * <Alex Huang>
 * <ah57984>
 * <17185>
 * Spring 2023
 */
package ClientPack;

import ClientPack.GUIClasses.GUIController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;


public class Client extends Application {
    private static String host = "127.0.0.1";
    static private Socket socket;
    static public FXMLLoader fxmlLoader;
    private Reader reader;
    private Thread readerthread;

    public static void main() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setUpNetworking();
        StartLoginPage(primaryStage);
    }
    public void StartLoginPage(Stage primaryStage) throws IOException {
        fxmlLoader = new FXMLLoader(Client.class.getResource("GUIClasses/GUI.fxml"));
        Parent root = fxmlLoader.load();
        GUIController guiController = fxmlLoader.getController();
        guiController.setReader(reader);
        Writer writer = new Writer(socket);
        guiController.setWriter(writer);
        guiController.setClient(this);
        Scene scene = new Scene(root, 600, 600);
        primaryStage.setTitle("Library Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void setUpNetworking() throws Exception{
        socket = new Socket(host, 4242);
        System.out.println("Connecting to... Server Socket" + socket);
        reader = new Reader(socket);
        readerthread = new Thread(reader);
        readerthread.start();
    }

    public void StopNetworking() throws IOException {
        socket.close();
        readerthread.stop();
    }

    static public Socket getsocket() {
        return socket;
    }


}
