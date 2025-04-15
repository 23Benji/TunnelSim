package tunnel.client;

/**
 * An ihm kann ein Führer angefordert aber auch ein solcher zurückgegeben
 * werden. Dieser muss eine Referenz auf ClientForm haben, damit die
 * Statusmeldungen dort angezeigt werden können.
 */
public class GuidesMonitor {
    /**
     * Maximalanzahl der am Eingang vorhandenen Führer
     */
    protected static final int MAX_GUIDES = 4;
    /**
     * Anzahl der momentan verfügbaren Führer
     */
    protected int availableGuides = MAX_GUIDES;
    /**
     * Referenz auf das ClientForm, um Statustexte auszugeben
     */
    protected ClientForm clientForm;

    /**
     * Konstruktor, dem eine Referenz auf das ClientForm übergeben wird
     *
     * @param clientForm Referenz auf das ClientForm
     */
    public GuidesMonitor(ClientForm clientForm) {
        if (clientForm == null) {
            throw new IllegalArgumentException("clientForm is null");
        }
        this.clientForm = clientForm;
    }

    /**
     * Ein Führer wird angefordert, gleichzeitig werden die Statusmeldungen im
     * ClientForm ausgegeben und die Benutzerschnittstelle angepasst.
     */
    public synchronized void request() throws InterruptedException {
        if (availableGuides > 0) {
            availableGuides--;
            clientForm.updateAvailableGuides(availableGuides);
        } else {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new InterruptedException();
            }
        }
    }

    /**
     * Führer wird bei Beendigung einer Führung zurückgegeben. Statusmeldungen
     * werden ausgegeben und die Benutzerschnittstelle angepasst.
     */
    public synchronized void release() {
        availableGuides++;
        notifyAll();
        clientForm.updateAvailableGuides(availableGuides);
    }

    /**
     * Die Anzahl der momentan verfügbaren Führer wird zurückgeliefert
     *
     * @return Anzahl der momentan verfügbaren Führer
     */
    public synchronized int getAvailableGuides() {
        return availableGuides;
    }

}
