package tunnel.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * In dieser Konsolenanwendung wird zuerst ein VisitorsMonitor angelegt, und dann
 * wartet das Programm in einer Endlosschleife auf Clientanfragen. Erreicht ihm
 * eine solche, so wird diese in einem Thread vom Typ ServerThread abgearbeitet.
 * Dadurch dass jede Anfrage in einem eigenen Thread abgearbeitet wird,
 * können mehrere Anfragen gleichzeitig bearbeitet werden.
 */
public class ServerMain {
	/**
	 * Port an welchem der Server arbeitet
	 */
	protected static final int PORT = 65535;
	protected static VisitorsMonitor visitorsMonitor = null;

	/**
	 * Besuchermonitor wird angelegt, und in einer Endlosschleife wird auf
	 * Clientanfragen gewartet, welche alle über einzelne ServerThreads abgearbeitet
	 * werden. Dadurch dass jede Anfrage in einem eigenen Thread abgearbeitet wird,
	 * können mehrere Anfragen gleichzeitig bearbeitet werden.
	 * @param args
	 */
	public static void main(String[] args) {
		visitorsMonitor = new VisitorsMonitor();

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Server gestartet. Wartet auf Verbindungen...");

			while (true) {
				try {
					Socket clientSocket = serverSocket.accept();
					new ServerThread(clientSocket, visitorsMonitor).start();
				} catch (IOException e) {
					behandleException(e);
				}
			}
		} catch (IOException e) {
			behandleException(e);
		}
	}

	/**
	 * Methode zur Exceptionbehandlung
	 * @param e
	 */
	public static void behandleException(Exception e) {
		System.err.println("Fehler: " + e.getMessage());
		e.printStackTrace();
	}
}