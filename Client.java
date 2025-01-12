import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter server IP address: ");
        String serverAddress = scanner.nextLine();

        System.out.print("Enter server port: ");
        int port = Integer.parseInt(scanner.nextLine());

        while (true) {
            System.out.println("\nSelect an operation to request:");
            System.out.println("1 = Date and Time");
            System.out.println("2 = Uptime");
            System.out.println("3 = Memory Usage");
            System.out.println("4 = Netstat");
            System.out.println("5 = Current Users");
            System.out.println("6 = Running Processes");
            System.out.println("7 = Exit");

            String operation = scanner.nextLine();
            if ("7".equals(operation)) {
                System.out.println("Exiting client...");
                break;
            }

            System.out.print("Enter the number of requests to generate (1, 5, 10, 15, 20, 25, 100): ");
            int numRequests = Integer.parseInt(scanner.nextLine());

            List<Long> turnAroundTimes = new ArrayList<>();
            List<Thread> threads = new ArrayList<>();

            for (int i = 0; i < numRequests; i++) {
                Thread thread = new Thread(() -> {
                    long startTime = System.currentTimeMillis();
                    try (Socket socket = new Socket(serverAddress, port);
                         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                        out.println(operation); // Send the operation to the server
                        StringBuilder response = new StringBuilder();

                        String line;
                        while ((line = in.readLine()) != null) {
                            response.append(line).append("\n");
                        }

                        long endTime = System.currentTimeMillis();
                        long turnAroundTime = endTime - startTime;

                        synchronized (turnAroundTimes) {
                            turnAroundTimes.add(turnAroundTime);
                        }

                        System.out.println("Response from server:\n" + response);
                        System.out.println("Turn-around Time: " + turnAroundTime + " ms");

                    } catch (IOException e) {
                        System.err.println("Error communicating with server: " + e.getMessage());
                    }
                });

                threads.add(thread);
                thread.start();
            }

            // Wait for all threads to finish
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    System.err.println("Thread interrupted: " + e.getMessage());
                }
            }

            // Calculate total and average turn-around times
            long totalTurnAroundTime = turnAroundTimes.stream().mapToLong(Long::longValue).sum();
            double averageTurnAroundTime = totalTurnAroundTime / (double) numRequests;

            System.out.println("\n--- Summary ---");
            System.out.println("Total Turn-around Time: " + totalTurnAroundTime + " ms");
            System.out.println("Average Turn-around Time: " + averageTurnAroundTime + " ms");
        }

        scanner.close();
    }
}