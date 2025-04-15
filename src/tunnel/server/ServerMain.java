package tunnel.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Server main application. Creates a VisitorsMonitor, listens for client
 * connections, and handles them in separate ServerThreads.
 * (Admin console removed).
 */
public class ServerMain {

    protected static final int PORT = 65535;
    protected static VisitorsMonitor visitorsMonitor = null;
    private static volatile boolean serverIsRunning = true;
    private static ServerSocket serverSocket = null;

    /**
     * Initializes monitor, starts the server socket, handles client connections.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        visitorsMonitor = new VisitorsMonitor();


        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("====== Server started on port " + PORT + " =======");
            System.out.println("Waiting for client connections... (Stop with Ctrl+C)");

            while (serverIsRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    if (serverIsRunning) {
                        new ServerThread(clientSocket, visitorsMonitor).start();
                    } else {
                        try {
                            clientSocket.close();
                        } catch (IOException ioex) {
                        }
                        break;
                    }

                } catch (SocketException se) {
                    if (serverIsRunning) {
                        System.err.println("SocketException during accept: " + se.getMessage());
                    } else {
                        System.out.println("Accept loop interrupted, server stopping.");
                        break;
                    }
                } catch (IOException e) {
                    if (serverIsRunning) {
                        behandleException(e);
                    }
                } catch (Exception e) {
                    if (serverIsRunning) {
                        System.err.println("Unexpected error in connection accept loop:");
                        behandleException(e);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("FATAL: Could not listen on port " + PORT);
            behandleException(e);
            serverIsRunning = false;
        } finally {
            System.out.println("Server main loop finished.");
            closeServerSocket();
            System.out.println("Server resources potentially closed.");
        }
    }


    private static synchronized void closeServerSocket() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                System.out.println("Closing server socket...");
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
                e.printStackTrace();
            }
        }
        serverSocket = null;
    }

    /**
     * Methode zur Exceptionbehandlung
     *
     * @param e The exception to handle.
     */
    public static void behandleException(Exception e) {
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
    }
}