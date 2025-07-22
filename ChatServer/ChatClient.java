import java.net.*;
import java.io.*;

public class ChatClient implements Runnable {
    private ChatClientThread client = null;
    private Socket socket = null;
    private DataInputStream console = null;
    private Thread thread = null;
    private PrintStream Out = null;

    public ChatClient(String serverName, int serverPort) {
        System.out.println("Establishing connection, please wait...");
        try {
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + socket);
            console = new DataInputStream(System.in);
            Out = new PrintStream(socket.getOutputStream());
            if (thread == null) {
                client = new ChatClientThread(this, socket);
                thread = new Thread(this);
                thread.start();
            }
        } catch (UnknownHostException e) {
            System.out.println("Host unknown: " + e.getMessage());
        } catch (IOException ioe) {
            System.out.println("Unexpected exception: " + ioe.getMessage());
        }
    }

    @Override
    public void run() {
        while (thread != null) {
            try {
                String line = console.readLine();
                if (line.equals("quit")) {
                    Out.println(line);
                    Out.flush();
                    stop(); // Stop the client thread and close resources
                    break;  // Exit the run loop
                }
                Out.println(line);
                Out.flush();
            } catch (IOException e) {
                System.out.println("Sending error: " + e.getMessage());
                stop();
                break; // Exit the run loop
            }
        }
    }

    public void handle(String msg) {
        if (msg.equals("quit")) {
            System.out.println("Goodbye, press RETURN to exit...");
            stop();
        } else {
            System.out.println(msg);
        }
    }

    public void stop() {
        if (thread != null) {
            // Deprecated thread.stop() usage.
            // A more robust way would be to use a volatile boolean flag.
            thread = null; 
        }
        try {
            if (console != null) console.close();
            if (Out != null) Out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Error closing...");
        }
        if (client != null) {
            client.close();
            // Deprecated thread.stop() usage.
            // A more robust way would be to use a volatile boolean flag.
        }
    }

    public static void main(String args[]) {
        ChatClient client = null;
        if (args.length != 2) {
            System.out.println("Usage: java ChatClient <host> <port>");
        } else {
            client = new ChatClient(args[0], Integer.parseInt(args[1]));
        }
    }
}