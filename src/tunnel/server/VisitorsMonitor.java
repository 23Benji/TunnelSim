package tunnel.server;


/**
 * Diese Klasse verwaltet die verf�gbaren Besucher, welche eingelassen werden
 * k�nnen
 */
public class VisitorsMonitor {
    /**
     * Maximalanzahl der im Tunnel vorhanden Besucher
     */
    protected final int MAX_VISITORS = 50;
    /**
     * Anzahl der Besucher die in den Tunnel noch eingelassen werden k�nnen
     */
    protected int availableVisitors = MAX_VISITORS;

    /**
     * Fordert count Besucher an und gibt Statusmeldungen an der Serverkonsole
     * aus
     *
     * @param count
     */
    public synchronized void request(int count) throws InterruptedException, IllegalArgumentException {
        if (count <= 0) {
            throw new IllegalArgumentException("Visitor count must be positive!");
        }

        while (availableVisitors < count) {
            try {
                wait();
            }catch (InterruptedException e) {
                throw new InterruptedException();
            }
        }

        availableVisitors -= count;
        System.out.println(count + " visitors received. " + availableVisitors + " visitors available");
    }


    /**
     * Gibt count Besucher an den VisitorsMonitor zur�ck und gibt Statusmeldungen
     * an der Serverkonsole aus
     *
     * @param count
     */
    public synchronized void release(int count) throws IllegalArgumentException {
        if (count <= 0) {
            throw new IllegalArgumentException("Visitor count must be positive!");
        }
        if (availableVisitors + count > MAX_VISITORS) {
            throw new IllegalArgumentException("Cannot release " + count + " visitors: would exceed maximum capacity of " + MAX_VISITORS + ".");
        }

        availableVisitors += count;
        System.out.println(count + " visitors released. " + availableVisitors + " visitors available");
        notifyAll();
    }


    /**
     * Liefert die Anzahl der momentan noch verf�gbaren Besucher zur�ck, die in den
     * Tunnel eingelassen werden k�nnen
     *
     * @return Anzahl der noch in den Tunnel einlassbaren Besucher
     */
    public synchronized int getAvailableVisitors() {
        return availableVisitors;
    }
}
