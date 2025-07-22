import java.io.*;
import java.net.*;
import java.util.*;

public class Coordinator {
    private static List<Socket> participants = new ArrayList<>();
    private static List<PrintWriter> outStreams = new ArrayList<>();
    private static List<BufferedReader> inStreams = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter number of participants: ");
        int numParticipants = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Waiting for " + numParticipants + " participants to connect...");

            for (int i = 0; i < numParticipants; i++) {
                Socket socket = serverSocket.accept();
                participants.add(socket);
                outStreams.add(new PrintWriter(socket.getOutputStream(), true));
                inStreams.add(new BufferedReader(new InputStreamReader(socket.getInputStream())));
                System.out.println("Participant " + (i + 1) + " connected.");
            }

            System.out.print("Enter transaction details: ");
            String transaction = scanner.nextLine();

            boolean success = initiateTwoPhaseCommit(transaction, numParticipants);
            if (success) {
                System.out.println("Transaction committed successfully.");
            } else {
                System.out.println("Transaction aborted.");
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            closeConnections();
            scanner.close();
        }
    }

    private static boolean initiateTwoPhaseCommit(String transaction, int numParticipants) {
        try {
            System.out.println("Sending PREPARE to all participants...");
            for (PrintWriter out : outStreams) {
                out.println("PREPARE " + transaction);
            }

            List<String> votes = new ArrayList<>();
            for (BufferedReader in : inStreams) {
                String response = in.readLine();
                votes.add(response);
            }

            System.out.println("\nCollected Votes:");
            for (int i = 0; i < numParticipants; i++) {
                System.out.println("Participant " + (i + 1) + ": " + votes.get(i));
            }

            boolean allAgreed = votes.stream().allMatch(vote -> "YES".equalsIgnoreCase(vote));

            if (allAgreed) {
                System.out.println("All participants voted YES. Sending COMMIT...");
                for (PrintWriter out : outStreams) {
                    out.println("COMMIT");
                }
                return true;
            } else {
                System.out.println("At least one participant voted NO. Sending ABORT...");
                for (PrintWriter out : outStreams) {
                    out.println("ABORT");
                }
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error during commit: " + e.getMessage());
            return false;
        }
    }

    private static void closeConnections() {
        try {
            for (Socket socket : participants) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connections: " + e.getMessage());
        }
    }
}
