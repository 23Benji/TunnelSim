package tunnel.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Der Thread liest vom Socket die Anzahl, und dabei werden die drei Fälle
 * (größer 0, kleiner 0 oder gleich 0) unterschieden und entsprechend am
 * VisitorsMonitor die Anfragen gestellt. Das Ergebnis wird an den Client
 * zurückgeschickt. Der ServerThread erhält den Socket des Clients und eine
 * Referenz auf VisitorsMonitor.
 */
public class ServerThread extends Thread {
    /**
     * Der Clientsocket, von welchem die Besucheranzahl gelesen werden kann
     */
    protected Socket client;
    /**
     * VisitorsMonitor, an dem die Anfrage nach Besuchern bzw. die Rückgabe
     * der Besucher nach Beendigung einer Besichtigung gestellt werden kann
     */
    protected VisitorsMonitor visitorsMonitor;

    /**
     * Konstruktor erhält den Clientsocket und den VisitorsMonitor als
     * Referenz. Als Threadname wird die IP-Adresse des Clients gesetzt.
     * Die IP-Adresse kann über den Clientsocket durch die Methode
     * getInetAddress() erfragt werden.
     *
     * @param client
     * @param visitorsMonitor
     */
    public ServerThread(Socket client, VisitorsMonitor visitorsMonitor) {
        if (client == null || visitorsMonitor == null) {
            throw new IllegalArgumentException("Socket/Monitor is null");
        }
        this.client = client;
        this.visitorsMonitor = visitorsMonitor;
    }

    /**
     * Diese Methode liest zuerst vom Clientsocket die Anzahl. Je nachdem,
     * welche Werte in anzahl stehen, werden folgende Aufgaben erledigt:
     *
     * <b>anzahl == 0</b>
     * Es wird die Anzahl der am VisitorsMonitor momentan verfügbaren Benutzer
     * abgefragt und an den Client zurückgeschickt.
     *
     * <b>anzahl > 0</b>
     * Es werden am VisitorsMonitor die Benutzer angefordert.
     *
     * <b>anzahl < 0</b>
     * Es werden dem VisitorsMonitor die Anzahl an Benutzer zurückgegeben.
     */
    @Override
    public void run() {
        int responseCode = -1;

        try (DataInputStream inputDataStream = new DataInputStream(client.getInputStream());
             DataOutputStream outputDataStream = new DataOutputStream(client.getOutputStream())) {

            int clientRequestCode = inputDataStream.readInt();

            try {
                if (clientRequestCode > 0) {
                    visitorsMonitor.request(clientRequestCode);
                    responseCode = clientRequestCode;

                } else if (clientRequestCode < 0) {
                    int numLeaving = -clientRequestCode;
                    visitorsMonitor.release(numLeaving);
                    responseCode = numLeaving;

                } else {
                    int availableSlots = visitorsMonitor.getAvailableVisitors();
                    responseCode = availableSlots;
                }
            } catch (InterruptedException interruptEx) {
                Thread.currentThread().interrupt();
                responseCode = -1;
            } catch (IllegalArgumentException argEx) {
                responseCode = -1;
            }

            outputDataStream.writeInt(responseCode);
            outputDataStream.flush();

        } catch (EOFException | SocketException networkCloseEx) {

        } catch (IOException ioEx) {

        } catch (Exception generalEx) {

        } finally {
            try {
                if (client != null && !client.isClosed()) {
                    client.close();
                }
            } catch (IOException closeEx) {

            }
        }
    }
}
