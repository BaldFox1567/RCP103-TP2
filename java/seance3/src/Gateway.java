/**
 * La passerelle IoT, c'est-a-dire le systeme etudie : une file d'attente,
 * un ou plusieurs serveurs, et une capacite maximale K (messages en file +
 * en service). capacity = 0 signifie capacite infinie (M/M/1).
 *
 * La passerelle porte l'etat du systeme et les compteurs. La logique des
 * evenements RECV_MSG et MSG_DEPT est appliquee par l'Engine, qui interroge
 * la passerelle : systeme plein ? serveur libre ? file vide ?
 */
public class Gateway {

    private Queue queue;
    private Server[] servers;
    private int capacity;

    // Compteurs de la passerelle
    private int nbArrivals;
    private int nbAccepted;
    private int nbRejected;
    private int nbDepartures;

    public Gateway(int nbServers, int capacity, double mu) {
        this.queue = new Queue();
        this.capacity = capacity;
        this.servers = new Server[nbServers];

        for (int i = 0; i < nbServers; i++) {
            this.servers[i] = new Server(i + 1, mu);
        }

        this.nbArrivals = 0;
        this.nbAccepted = 0;
        this.nbRejected = 0;
        this.nbDepartures = 0;
    }

    // Nombre de serveurs occupes
    public int GetNbBusyServers() {
        int n = 0;

        for (int i = 0; i < this.servers.length; i++) {
            if (this.servers[i].IsBusy()) {
                n = n + 1;
            }
        }

        return n;
    }

    // Nombre de messages en attente dans la file
    public int GetNbInQueue() {
        return this.queue.GetSize();
    }

    // Nombre de messages dans le systeme = en service + en file
    public int GetNbInSystem() {
        return GetNbBusyServers() + this.queue.GetSize();
    }

    // Vrai si le systeme est plein (uniquement pour les modeles bornes /K)
    public boolean IsFull() {
        return this.capacity > 0 && GetNbInSystem() >= this.capacity;
    }

    // Retourne le premier serveur libre, ou null s'ils sont tous occupes
    public Server GetFreeServer() {
        for (int i = 0; i < this.servers.length; i++) {
            if (!this.servers[i].IsBusy()) {
                return this.servers[i];
            }
        }

        return null;
    }

    // Retourne le premier serveur occupe, ou null (utile a la fin d'un service)
    public Server GetBusyServer() {
        for (int i = 0; i < this.servers.length; i++) {
            if (this.servers[i].IsBusy()) {
                return this.servers[i];
            }
        }

        return null;
    }

    // Getters
    public Queue GetQueue() {
        return this.queue;
    }

    public int GetCapacity() {
        return this.capacity;
    }

    public int GetNbServers() {
        return this.servers.length;
    }

    // Compteurs
    public void MessageArrival() {
        this.nbArrivals = this.nbArrivals + 1;
    }

    public void MessageAccepted() {
        this.nbAccepted = this.nbAccepted + 1;
    }

    public void MessageRejected() {
        this.nbRejected = this.nbRejected + 1;
    }

    public void MessageDeparture() {
        this.nbDepartures = this.nbDepartures + 1;
    }

    public int GetNbArrivals() {
        return this.nbArrivals;
    }

    public int GetNbAccepted() {
        return this.nbAccepted;
    }

    public int GetNbRejected() {
        return this.nbRejected;
    }

    public int GetNbDepartures() {
        return this.nbDepartures;
    }

    // Affichage
    public void PrintGateway() {
        System.out.println("Gateway");
        System.out.println("  serveurs   : " + this.servers.length
                + " (occupes=" + GetNbBusyServers() + ")");
        System.out.println("  file       : " + GetNbInQueue());
        System.out.println("  systeme    : " + GetNbInSystem()
                + (this.capacity > 0 ? " / " + this.capacity : " (capacite infinie)"));
        System.out.println("  arrives    : " + this.nbArrivals);
        System.out.println("  acceptes   : " + this.nbAccepted);
        System.out.println("  rejetes    : " + this.nbRejected);
        System.out.println("  sortis     : " + this.nbDepartures);
    }
}
