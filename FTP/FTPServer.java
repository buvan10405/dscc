import java.io.*;
import java.net.*;

public class FTPServer {
    private static final int SERVER_PORT = 5000;
    private static File currentDirectory = new File(System.getProperty("user.dir"));

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("FTP Server started. Waiting for client...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected.");
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                while (true) {
                    String command = in.readUTF();
                    if (command.equalsIgnoreCase("exit")) {
                        System.out.println("Client disconnected.");
                        break;
                    }
                    handleCommand(command);
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("Connection lost.");
            }
        }

        private void handleCommand(String command) throws IOException {
            if (command.equalsIgnoreCase("LIST")) {
                listFiles();
            } else if (command.equalsIgnoreCase("PWD")) {
                out.writeUTF(currentDirectory.getAbsolutePath());
            } else if (command.startsWith("READ ")) {
                readFile(command.substring(5));
            } else if (command.startsWith("WRITE ")) {
                writeFile(command.substring(6));
            } else if (command.startsWith("CD ")) {
                changeDirectory(command.substring(3));
            } else if (command.startsWith("MKDIR ")) {
                createDirectory(command.substring(6));
            } else {
                out.writeUTF("Invalid command!");
            }
        }

        private void listFiles() throws IOException {
            File[] files = currentDirectory.listFiles();
            if (files == null || files.length == 0) {
                out.writeUTF("No files found.");
                return;
            }
            StringBuilder fileList = new StringBuilder();
            for (File file : files) {
                fileList.append(file.getName()).append("\n");
            }
            out.writeUTF(fileList.toString());
        }

        private void readFile(String fileName) throws IOException {
            File file = new File(currentDirectory, fileName);
            if (!file.exists() || !file.isFile()) {
                out.writeUTF("Error: File not found.");
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            out.writeUTF(content.toString());
        }

        private void writeFile(String fileName) throws IOException {
            File file = new File(currentDirectory, fileName);

            // Ensure the file exists
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(file, true); // Appending to file
            while (true) {
                String line = in.readUTF();
                if (line.equals("EOF")) break;
                writer.write(line + "\n");
            }
            writer.close();
            out.writeUTF("Write successful! File created/updated: " + fileName);
        }

        private void changeDirectory(String dirName) throws IOException {
            File newDir = new File(currentDirectory, dirName);
            if (!newDir.exists() || !newDir.isDirectory()) {
                out.writeUTF("Error: Directory not found.");
                return;
            }
            currentDirectory = newDir;
            out.writeUTF("Directory changed to: " + currentDirectory.getAbsolutePath());
        }

        private void createDirectory(String dirName) throws IOException {
            File newDir = new File(currentDirectory, dirName);
            if (newDir.exists()) {
                out.writeUTF("Error: Directory already exists.");
                return;
            }
            if (newDir.mkdir()) {
                out.writeUTF("Directory created successfully!");
            } else {
                out.writeUTF("Error: Failed to create directory.");
            }
        }
    }
}

