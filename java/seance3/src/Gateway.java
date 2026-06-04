import java.util.IdentityHashMap;
import java.util.Random;

/**
 * Gateway.java — La passerelle IoT, c'est-à-dire « le système ».
 *
 * Elle regroupe :
 *   - une file d'attente partagée (Queue, classe de la Séance 2) ;
 *   - un ou plusieurs serveurs (Server, Séance 2) ;
 *   - une capacité maximale K (nombre total de messages dans le système =
 *     en file + en service). capacite = null signifie capacité infinie (M/M/1).
 *
 * La Gateway porte l'état observé par les métriques (nombre dans le système,
 * nombre en file) et applique la logique des événements RECV_MSG et MSG_DEPT.
 * L'Engine se charge, lui, de l'ordonnancement des événements et du temps.
 */
public class Gateway {

    /**
     * Petit résultat retourné quand un service démarre : le message concerné
     * et le serveur qui le traite. L'Engine s'en sert pour tirer la durée de
     * service et programmer le MSG_DEPT correspondant.
     */
    public static class ServiceStart {
        public final Message msg;
        public final Server  server;
        public ServiceStart(Message msg, Server server) {
            this.msg = msg;
            this.server = server;
        }
    }

    private final int      nbServeurs;
    private final Integer  capacite;          // K ; null = infini
    private final Queue    file;
    private final Server[] serveurs;

    // Mémorise quel serveur traite quel message (pour le libérer au départ).
    // IdentityHashMap : on compare les messages par identité d'objet.
    private final IdentityHashMap<Message, Server> serveurParMessage;

    // Compteurs globaux de la passerelle
    private long arrivees = 0;   // messages présentés à la passerelle (RECV)
    private long acceptes = 0;   // messages admis (service ou file)
    private long rejets   = 0;   // messages rejetés (système plein)
    private long departs  = 0;   // messages servis et sortis (DEPT)

    public Gateway(int nbServeurs, Integer capacite, double mu, Random rng) {
        this.nbServeurs = nbServeurs;
        this.capacite   = capacite;
        this.file       = new Queue();
        this.serveurs   = new Server[nbServeurs];
        for (int i = 0; i < nbServeurs; i++) {
            this.serveurs[i] = new Server(i + 1, mu, rng);
        }
        this.serveurParMessage = new IdentityHashMap<>();
    }

    // ---------------- État instantané ----------------

    /** Nombre de serveurs occupés. */
    public int nbBusy() {
        int n = 0;
        for (Server s : serveurs) if (!s.isFree()) n++;
        return n;
    }

    /** Messages dans le système = en service + en file. */
    public int nbInSystem() {
        return nbBusy() + file.size();
    }

    /** Messages en attente dans la file. */
    public int nbInQueue() {
        return file.size();
    }

    // ---------------- Décisions ----------------

    /** Vrai si le système est plein (cas borné /K). */
    public boolean isFull() {
        return capacite != null && nbInSystem() >= capacite;
    }

    /** Vrai s'il existe au moins un serveur libre. */
    public boolean hasFreeServer() {
        return nbBusy() < nbServeurs;
    }

    /** Retourne le premier serveur libre (cas multi-serveurs), ou null. */
    public Server selectFreeServer() {
        for (Server s : serveurs) if (s.isFree()) return s;
        return null;
    }

    // ---------------- Traitement des événements ----------------

    /**
     * Traitement d'un RECV_MSG : un message arrive à la passerelle.
     *   - système plein  -> rejet (compteur), retourne null ;
     *   - serveur libre  -> service immédiat, retourne le ServiceStart ;
     *   - sinon          -> mise en file, retourne null.
     */
    public ServiceStart receive(Message msg) {
        arrivees++;
        if (isFull()) {
            rejets++;
            return null;
        }
        acceptes++;
        if (hasFreeServer()) {
            Server srv = selectFreeServer();
            srv.occupy();
            serveurParMessage.put(msg, srv);
            return new ServiceStart(msg, srv);
        } else {
            file.enqueue(msg);
            return null;
        }
    }

    /**
     * Traitement d'un MSG_DEPT : fin de service du message qui termine.
     * Libère son serveur, comptabilise la sortie, puis sert le message suivant
     * de la file s'il y en a un (retourne alors le nouveau ServiceStart),
     * sinon retourne null.
     */
    public ServiceStart depart(Message quiTermine) {
        departs++;
        Server fini = serveurParMessage.remove(quiTermine);
        if (fini != null) fini.release();

        if (!file.isEmpty()) {
            Message suivant = file.dequeue();
            Server srv = selectFreeServer();   // au moins celui qu'on vient de libérer
            srv.occupy();
            serveurParMessage.put(suivant, srv);
            return new ServiceStart(suivant, srv);
        }
        return null;
    }

    // ---------------- Compteurs (lecture) ----------------
    public long getArrivees() { return arrivees; }
    public long getAcceptes() { return acceptes; }
    public long getRejets()   { return rejets; }
    public long getDeparts()  { return departs; }
    public int  getNbServeurs() { return nbServeurs; }
    public Integer getCapacite() { return capacite; }

    /** Affiche l'état courant de la passerelle. */
    public void printGateway() {
        String cap = (capacite == null) ? "∞" : capacite.toString();
        System.out.printf(
            "Gateway : serveurs=%d (occupes=%d), file=%d, systeme=%d/%s | "
            + "arr=%d acc=%d rej=%d dep=%d%n",
            nbServeurs, nbBusy(), nbInQueue(), nbInSystem(), cap,
            arrivees, acceptes, rejets, departs);
    }
}
