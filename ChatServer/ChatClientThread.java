import java.io.*;
import java.net.*;

public class ChatClientThread extends Thread {
    private Socket socket = null;
    private ChatClient client = null;
    private DataInputStream streamIn = null;
    private volatile boolean running = true; // Use a volatile flag for graceful shutdown

    public ChatClientThread(ChatClient client, Socket socket) {
        this.client = client;
        this.socket = socket;
        open();
        start();
    }

    public void open() {
        try {
            streamIn = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Error getting input stream: " + e);
            client.stop();
        }
    }

    public void close() {
        try {
            running = false; // Set flag to stop the run loop
            if (streamIn != null) streamIn.close();
        } catch (IOException e) {
            System.out.println("Error closing input stream: " + e);
        }
    }

    @Override
    public void run() {
        while (running) { // Use the flag to control the loop
            try {
                String message = streamIn.readLine();
                if (message == null) { // Check for null to detect stream closure
                    System.out.println("Server disconnected.");
                    client.stop();
                    break; // Exit the loop
                }
                client.handle(message);
            } catch (IOException e) {
                System.out.println("Listening error: " + e.getMessage());
                client.stop();
                break; // Exit the loop on error
            }
        }
    }
}