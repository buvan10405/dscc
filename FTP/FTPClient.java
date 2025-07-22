import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FTPClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;
    private static Socket socket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Connected to FTP Server!");

            while (true) {
                showMenu();
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice == 7) {
                    out.writeUTF("exit");
                    System.out.println("Exiting FTP Client...");
                    break;
                }
                handleChoice(choice);
            }
            socket.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void showMenu() {
        System.out.println("\n--- FTP Client Menu ---");
        System.out.println("1. List Files");
        System.out.println("2. Show Current Directory");
        System.out.println("3. Read File");
        System.out.println("4. Write File");
        System.out.println("5. Change Directory");
        System.out.println("6. Create Directory");
        System.out.println("7. Exit");
        System.out.print("Enter choice: ");
    }

    private static void handleChoice(int choice) throws IOException {
        switch (choice) {
            case 1:
                out.writeUTF("LIST");
                System.out.println("Files on Server:\n" + in.readUTF());
                break;
            case 2:
                out.writeUTF("PWD");
                System.out.println("Current Directory: " + in.readUTF());
                break;
            case 3:
                readFile();
                break;
            case 4:
                writeFile();
                break;
            case 5:
                changeDirectory();
                break;
            case 6:
                createDirectory();
                break;
            default:
                System.out.println("Invalid choice! Please try again.");
        }
    }

    private static void readFile() throws IOException {
        System.out.print("Enter file to read: ");
        String fileName = scanner.nextLine();
        out.writeUTF("READ " + fileName);
        System.out.println("File Contents:\n" + in.readUTF());
    }

    private static void writeFile() throws IOException {
        System.out.print("Enter file to write: ");
        String fileName = scanner.nextLine();
        out.writeUTF("WRITE " + fileName);

        System.out.println("Enter content (type 'EOF' on a new line to stop):");
        while (true) {
            String content = scanner.nextLine();
            out.writeUTF(content);
            if (content.equals("EOF")) break;
        }

        System.out.println("Write operation response: " + in.readUTF());
    }

    private static void changeDirectory() throws IOException {
        System.out.print("Enter directory to change to: ");
        String dirName = scanner.nextLine();
        out.writeUTF("CD " + dirName);
        System.out.println("Change Directory Response: " + in.readUTF());
    }

    private static void createDirectory() throws IOException {
        System.out.print("Enter directory name to create: ");
        String dirName = scanner.nextLine();
        out.writeUTF("MKDIR " + dirName);
        System.out.println("Directory Creation Response: " + in.readUTF());
    }
}

