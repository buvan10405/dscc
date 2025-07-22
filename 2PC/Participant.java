import java.io.*;
import java.net.*;
import java.util.*;

public class Participant {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Participant <participant_id>");
            return;
        }

        int participantId = Integer.parseInt(args[0]);

        while (true) {
            try (Socket socket = new Socket("localhost", 5000);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 Scanner scanner = new Scanner(System.in)) {

                System.out.println("Participant " + participantId + " connected.");

                while (true) {
                    String message = in.readLine();
                    if (message == null) {
                        System.out.println("Connection closed by Coordinator.");
                        break;
                    }

                    if (message.startsWith("PREPARE")) {
                        System.out.print("Coordinator sent PREPARE for transaction: " + message.substring(8) + ". Vote YES/NO: ");
                        String vote = scanner.nextLine().toUpperCase();
                        out.println(vote);
                    } else if ("COMMIT".equals(message)) {
                        System.out.println("Transaction COMMITTED.");
                    } else if ("ABORT".equals(message)) {
                        System.out.println("Transaction ABORTED.");
                    }
                }
                break;
            } catch (IOException e) {
                System.err.println("Error connecting to Coordinator. Retrying in 5 seconds...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
