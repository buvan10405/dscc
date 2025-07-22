import java.net.*;
import java.io.*;

public class ChatServer implements Runnable {
    private ChatServerThread clients[] = new ChatServerThread[50];
    private ServerSocket server = null;
    private Thread thread = null;
    private int clientCount = 0;

    public ChatServer(int port) {
        try {
            System.out.println("binding to port" + port + ",please wait ...");
            server = new ServerSocket(port);
            System.out.println("server started:" + server);
            thread = new Thread(this);
            thread.start();
        } catch (IOException e) {
            System.out.println("cannot bind to port" + port + ":" + e.getMessage());
        }
    }

    public void run() {
        while (thread != null) {
            try {
                System.out.println("waiting for a client...");
                addThread(server.accept());
            } catch (IOException e) {
                System.out.println("server accept error:" + e);
                if (thread != null) {
                    // It's generally not recommended to stop a thread directly using stop()
                    // A better approach would be to set a flag and let the thread exit gracefully.
                    // For this example, we'll keep the original logic for direct correction.
                    thread = null; 
                }
            }
        }
    }

    public void stop() {
        if (thread != null) {
            // As noted, avoid thread.stop() in production.
            thread = null;
        }
    }

    private int findClient(int ID) {
        for (int i = 0; i < clientCount; i++)
            if (clients[i].getID() == ID)
                return i;
        return -1;
    }

    public synchronized void handle(int ID, String input) {
        if (input.equals("quit")) {
            clients[findClient(ID)].send("quit");
            remove(ID);
        } else {
            System.out.println(ID + ":" + input);
            for (int i = 0; i < clientCount; i++)
                clients[i].send(ID + ":" + input);
        }
    }

    public synchronized void remove(int ID) {
        int pos = findClient(ID);
        if (pos >= 0) {
            ChatServerThread closing = clients[pos];
            System.out.println("removing client thread:" + ID + "at" + pos);
            if (pos < clientCount - 1)
                for (int i = pos + 1; i < clientCount; i++)
                    clients[i - 1] = clients[i];
            clientCount--;
            try {
                closing.close();
            } catch (IOException e) {
                System.out.println("error closing thread;" + e);
            }
            // Avoid thread.stop() in production.
            // A better approach would be to have the thread gracefully exit its run loop.
            // For this example, we'll keep the original logic for direct correction.
        }
    }

    private void addThread(Socket socket) {
        if (clientCount < clients.length) {
            System.out.println("client accepted:" + socket);
            clients[clientCount] = new ChatServerThread(this, socket);
            try {
                clients[clientCount].open();
                clients[clientCount].start();
                clientCount++;
            } catch (IOException e) {
                System.out.println("error opening thread;" + e);
            }
        } else {
            System.out.println("client refused;maximum" + clients.length + "reached");
        }
    }

    public static void main(String a[]) {
        ChatServer server = null;
        if (a.length != 1) {
            System.out.println("Usage:java ChatServer Port");
        } else {
            server = new ChatServer(Integer.parseInt(a[0]));
        }
    }
}