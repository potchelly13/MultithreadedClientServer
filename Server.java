import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter Port Number: ");
        int portNum = in.nextInt();

        try (ServerSocket serverSocket = new ServerSocket(portNum)) { // Default port
            System.out.println("Server is listening on port " + portNum);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept client connections
                System.out.println("New client connected");

                // Handle client requests in a new thread
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }
}

// Handles individual client connections
class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = in.readLine(); // Read client request
            System.out.println("Processing request: " + request);

            String response = handleRequest(request);
            out.println(response); // Send response to client

        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close(); // Close client socket
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    // Processes the client request and returns the appropriate response
    private String handleRequest(String request) {
        switch (request) {
            case "1":
                return getCurrentDateAndTime();
            case "2":
                return executeCommand("uptime");
            case "3":
                return executeCommand("free -h");
            case "4":
                return executeCommand("netstat");
            case "5":
                return executeCommand("who");
            case "6":
                return executeCommand("ps -aux");
            default:
                return "Unknown command: " + request;
        }
    }

    // Returns the current date and time
    private String getCurrentDateAndTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "Date and Time: " + formatter.format(new Date());
    }

    // Executes the given system command and returns its output
    private String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            return "Error executing command: " + e.getMessage();
        }
        return output.toString();
    }
}