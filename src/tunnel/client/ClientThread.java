package tunnel.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Jede Anfrage um Start einer Besichtigung oder Beendigung einer solchen muss in
 * einem eigenen Thread durchgef�hrt werden, da insbesondere bei nicht
 * Vorhandensein eines F�hrers oder bei nicht verf�gbarem Besucherkontingent eine
 * solche Anfrage l�ngere Zeit warten und deshalb das ClientFormu blockiert
 * w�rde.<br>
 * Damit der Thread seine Aufgabe durchf�hren kann, muss er einerseits den
 * GuidesMonitor als Referenz enthalten, um die F�hreranforderung zu stellen.
 * Andererseits muss er ClientForm kennen, um die Ausgaben und Anpassungen an
 * der Benutzerschnittstelle vornehmen zu k�nnen.<br>
 * Der Thread erh�lt die Besucheranzahl. Ist diese positiv, so fordert er zuerst
 * beim GuidesMonitor einen F�hrer f�r die Gruppe an. Erh�lt er diese, so wird
 * �ber eine Netzwerkverbindung mit dem Programm ServerMain Verbindung aufgenommen
 * und um die eingegebene Anzahl von Besuchern angefragt.<br>
 * Ist die Besucheranzahl negativ, so bedeutet dies, dass die F�hrung beendet
 * wird, der F�hrer dem GuidesMonitor zur�ck gegeben und beim Server ebenfalls
 * die Besucheranzahl zur�ck gegeben wird.<br>
 * Ist die Besucheranzahl gleich 0, so wird der Thread anweisen beim Server die
 * Anzahl der verf�gbaren Besucher nachzufragen, die noch im Tunnel Platz haben
 */
public class ClientThread extends Thread
{
	/**
	 * IP-Adresse des Besucherservers
	 */
	protected static final String HOST = "localhost";
	/**
	 * Port
	 */
	protected static final int PORT = 65535;
	/**
	 * Falls positiv: Anzahl der anzufordernden Besucher die eine Besichtigung machen
	 * m�chten<br>
	 * Falls negativ: Anzahl der Besucher die eine Besichtigung beenden m�chten<br>
	 * Falls 0: Besucherserver muss angewiesen werden, die Anzahl der Besucher
	 * zur�ckzuliefern, welche noch in den Tunnel eingelassen werden k�nnen
	 */
	protected int count = 0;
	/**
	 * Referenz auf das ClientForm. Diese ist notwendig, damit der ClientThread
	 * die Benutzerschnittstelle aktualisieren und z. B. Statusmeldungen dort
	 * anzeigen kann
	 */
	protected ClientForm clientForm = null;
	/**
	 * Referent auf den GuidesMonitor. Diese ist notwendig, dass der ClientThread
	 * an diesem einen F�hrer anfordern bzw. nach Beendigung einer F�hrung den
	 * F�hrer zur�ckgeben kann
	 */
	protected GuidesMonitor guidesMonitor = null;

	/**
	 * Konstruktor dem die Anzahl der Besucher, das ClientForm und der
	 * GuidesMonitor �bergeben wird
	 * @param anzahl
	 * @param clientForm
	 * @param guidesMonitor
	 */
	public ClientThread(int anzahl, ClientForm clientForm,
		GuidesMonitor guidesMonitor) {
		this.count = anzahl;
		this.clientForm = clientForm;
		this.guidesMonitor = guidesMonitor;

	}

	/**
	 * In dieser Methode wird die eigentliche Arbeit des Threads erledigt. Ausgehend
	 * vom Wert der Variable count wird folgendes erledigt:<br><br>
	 * <b>count > 0: Eine neue Besichtigung soll durchgef�hrt werden</b><br>
	 * In einem ersten Schritt wird am GuidesMonitor ein F�hrer angefordert. War
	 * dies erfolgreich, so wird in einem zweiten Schritt eine Socket-Verbindung
	 * mit dem Server aufgebaut. Konnte die Verbindung nicht aufgebaut werden, so
	 * wird der F�hrer wieder zur�ck gegeben. Bei aufrechter Verbindung wird die
	 * Anzahl �bermittelt. Dann wartet der Thread auf die Antwort des Servers. Da
	 * der Thread neben anderen Threads eigenst�ndig wartet, werden alle anderen
	 * Aktivit�ten nicht blockiert. Nachdem die Antwort des Servers da ist, muss
	 * auch noch die JList mit einem neuen Eintrag erg�nzt werden.Bei diesem Vorgang
	 * m�ssen �nderungen und Ausgaben von Statusmeldungen am ClientFormular erfolgen<br><br>
	 * <b>count < 0: Eine Besichtigung soll beendet werden</b><br>
	 * Zuerst wird der F�hrer dem GuidesMonitor zur�ck gegeben. Dann wird eine
	 * Verbindung zum Server aufgebaut und diesem die Anzahl �bergeben. Dann wird
	 * auf die Antwort des Servers gewartet und danach die JListeintr�ge aktualisiert.
	 * W�hrend dieses Vorganges werden die Inhalte von ClienForm angepasst<br><br>
	 * <b>count == 0: Eine Anfrage an den Server soll ermitteln, wie viele
	 * Besucher noch im Tunnel Platz finden</b><br>
	 * Der Thread erstellt eine Socketverbindung mit dem Server, und schickt diesem
	 * eine 0. Der Server - falls aktiv - antwortet mit der aktuellen Besucheranzahl
	 * die noch in den Tunnel einglassen werden d�rfen. Diese Anzahl wird im
	 * ClientForm ausgegeben
	 */
	@Override
	public void run() {
		// Renamed guideAcquired to monitorLockObtained for clarity
		boolean monitorLockObtained = false;
		try {
			// --- Case: Check Capacity (count == 0) ---
			// Moved this check to the beginning
			if (count == 0) {
				try (Socket checkSocket = new Socket(HOST, PORT); // Renamed socket
					 DataOutputStream checkOut = new DataOutputStream(checkSocket.getOutputStream()); // Renamed out
					 DataInputStream checkIn = new DataInputStream(checkSocket.getInputStream())) { // Renamed in

					checkOut.writeInt(0); // Send 0 to indicate capacity check
					checkOut.flush();
					int currentCapacity = checkIn.readInt(); // Renamed available
					clientForm.updateAvailableCapacity(currentCapacity);

				} catch (IOException e) {
					// Original code had an empty catch block here for capacity check failures.
					// Consider adding logging if this failure is important:
					// clientForm.logError("Network error checking capacity: " + e.getMessage());
				}
			}
			// --- Case: End Visit (count < 0) ---
			// Moved this check to be second
			else if (count < 0) {
				int numVisitorsExiting = -count; // Renamed visitorsEnding

				// Release the guide *before* contacting the server for end confirmation
				guidesMonitor.release();

				try (Socket endSocket = new Socket(HOST, PORT); // Renamed socket
					 DataOutputStream endOut = new DataOutputStream(endSocket.getOutputStream()); // Renamed out
					 DataInputStream endIn = new DataInputStream(endSocket.getInputStream())) { // Renamed in

					endOut.writeInt(count); // Send the negative count to signal end
					endOut.flush();
					int endResponseCode = endIn.readInt(); // Renamed response

					if (endResponseCode == numVisitorsExiting) {
						clientForm.updateStatus("Visit with " + numVisitorsExiting + " visitors finished");
					} else {
						// Simplified original comment
						clientForm.updateStatus("Err:Server did not confirm end correctly (Code: " + endResponseCode +")");
					}
				} catch (UnknownHostException e) {
					clientForm.updateStatus("Err:Server not found (end request).");
				} catch (IOException e) {
					clientForm.updateStatus("Err:Network error (end): " + e.getMessage());
				}
			}
			// --- Case: Start Visit (count > 0) ---
			// This case is now last
			else { // count > 0 must be true here
				guidesMonitor.request(); // Request a guide first
				monitorLockObtained = true; // Mark that we've acquired the monitor lock

				try (Socket startSocket = new Socket(HOST, PORT); // Renamed socket
					 DataOutputStream startOut = new DataOutputStream(startSocket.getOutputStream()); // Renamed out
					 DataInputStream startIn = new DataInputStream(startSocket.getInputStream())) { // Renamed in

					clientForm.updateStatus("Visit with " + count + " visitors requested...");
					startOut.writeInt(count); // Send the positive visitor count
					startOut.flush();
					int startResponseCode = startIn.readInt(); // Renamed response

					if (startResponseCode == count) {
						clientForm.updateStatus("Visit with " + count + " visitors enter the tunnel");
						clientForm.addActiveVisitEntry(count);
						// If successful, the monitor lock is conceptually "held" by the visit now.
						// This thread is no longer responsible for releasing it *unless* an error occurred below.
						// Setting the flag false signifies this thread doesn't need to release in normal exit.
						monitorLockObtained = false;
					} else if (startResponseCode == -1) {
						clientForm.updateStatus("Err:Server error during start request.");
						guidesMonitor.release(); // Release monitor due to server error
						monitorLockObtained = false; // Mark lock as released
					} else {
						clientForm.updateStatus("Err:Server denied start request (Code: " + startResponseCode + ")");
						guidesMonitor.release(); // Release monitor due to denial
						monitorLockObtained = false; // Mark lock as released
					}
				} catch (UnknownHostException e) {
					clientForm.updateStatus("Err:Server not found: " + HOST + ":" + PORT);
					// Clean up monitor lock if acquired before the network error
					if (monitorLockObtained) {
						guidesMonitor.release();
						monitorLockObtained = false;
					}
				} catch (IOException e) {
					clientForm.updateStatus("Err:Network error (start): " + e.getMessage());
					// Clean up monitor lock if acquired before the network error
					if (monitorLockObtained) {
						guidesMonitor.release();
						monitorLockObtained = false;
					}
				}
				// If we reach here successfully (startResponseCode == count), monitorLockObtained is already false.
				// If we reach here after an error/exception inside the try, monitorLockObtained should also be false
				// because it was explicitly released and set.
			}
		} catch (InterruptedException e) {
			clientForm.updateStatus("Err:Operation interrupted (waiting for guide?).");
			Thread.currentThread().interrupt(); // Preserve interrupt status
			// Ensure monitor is released if interrupted while holding the lock
			if (monitorLockObtained) {
				guidesMonitor.release();
			}
		} catch (Exception e) {
			clientForm.updateStatus("Err:Unexpected client thread error: " + e.getMessage());
			// Ensure monitor is released if an unexpected error occurs while holding the lock
			if (monitorLockObtained) {
				guidesMonitor.release();
			}
		}
		// The 'monitorLockObtained' flag ensures guidesMonitor.release() is only called
		// when it's appropriate (i.e., when the lock was acquired by this thread
		// and not successfully transferred to an active visit or already released due to an error).
	}

	/**
	 * Methode zur Behandlung der Netzwerkexceptions
	 * @param e
	 */
	public void behandleException(Exception e) {
	}
}