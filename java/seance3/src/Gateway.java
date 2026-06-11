/**
 * Gateway.java — La passerelle IoT, c'est-à-dire le système étudié.
 *
 * Elle regroupe la file d'attente (Queue), un ou plusieurs serveurs (Server)
 * et une capacité maximale K (messages en file + en service). capacity = 0
 * signifie capacité infinie (M/M/1).
 *
 * La passerelle porte l'état du système (serveurs occupés, taille de la file)
 * et les compteurs (arrivées, acceptés, rejetés, départs). La logique des
 * événements RECV_MSG et MSG_DEPT est appliquée par l'Engine, qui interroge
 * la passerelle : système plein ? serveur libre ? file vide ?
 */
public class Gateway {

    private Queue    queue;        // la file d'attente partagée
    private Server[] servers;      // le ou les serveurs
    private int      capacity;     // capacité K du système (0 = infinie)

    // Compteurs de la passerelle
    private int nbArrivals;        // messages arrivés (RECV_MSG)
    private int nbAccepted;        // messages admis (service ou file)
    private int nbRejected;        // messages rejetés (système plein)
    private int nbDepartures;      // messages servis et sortis (MSG_DEPT)

    public Gateway(int nbServers, int capacity, double mu) {
        this.queue    = new Queue();
        this.capacity = capacity;
        this.servers  = new Server[nbServers];
        for (int i = 0; i < nbServers; i++) {
            this.servers[i] = new Server(i + 1, mu);
        }
        this.nbArrivals   = 0;
        this.nbAccepted   = 0;
        this.nbRejected   = 0;
        this.nbDepartures = 0;
    }

    // --- État instantané du système ---

    /** Nombre de serveurs occupés. */
    public int GetNbBusyServers() {
        int n = 0;
        for (Server s : servers) {
            if (s.IsBusy()) {
                n++;
            }
        }
        return n;
    }

    /** Nombre de messages en attente dans la file. */
    public int GetNbInQueue() {
        return queue.GetSize();
    }

    /** Nombre de messages dans le système = en service + en file. */
    public int GetNbInSystem() {
        return GetNbBusyServers() + queue.GetSize();
    }

    /** Vrai si le système est plein (uniquement pour les modèles bornés /K). */
    public boolean IsFull() {
        return capacity > 0 && GetNbInSystem() >= capacity;
    }

    /** Retourne le premier serveur libre, ou null s'ils sont tous occupés. */
    public Server GetFreeServer() {
        for (Server s : servers) {
            if (!s.IsBusy()) {
                return s;
            }
        }
        return null;
    }

    /** Retourne le premier serveur occupé, ou null (utile à la fin d'un service). */
    public Server GetBusyServer() {
        for (Server s : servers) {
            if (s.IsBusy()) {
                return s;
            }
        }
        return null;
    }

    public Queue GetQueue()    { return queue; }
    public int   GetCapacity() { return capacity; }
    public int   GetNbServers(){ return servers.length; }

    // --- Compteurs ---
    public void MessageArrival()   { nbArrivals++; }
    public void MessageAccepted()  { nbAccepted++; }
    public void MessageRejected()  { nbRejected++; }
    public void MessageDeparture() { nbDepartures++; }

    public int GetNbArrivals()   { return nbArrivals; }
    public int GetNbAccepted()   { return nbAccepted; }
    public int GetNbRejected()   { return nbRejected; }
    public int GetNbDepartures() { return nbDepartures; }

    /** Affiche l'état courant de la passerelle. */
    public void PrintGateway() {
        String cap = (capacity > 0) ? String.valueOf(capacity) : "infinie";
        System.out.println("Gateway : serveurs=" + servers.length
                + " (occupes=" + GetNbBusyServers() + "), file=" + GetNbInQueue()
                + ", systeme=" + GetNbInSystem() + ", capacite=" + cap
                + " | arrives=" + nbArrivals + " acceptes=" + nbAccepted
                + " rejetes=" + nbRejected + " sortis=" + nbDepartures);
    }
}
