import java.util.Locale;
import java.util.Random;

/**
 * Server.java — Un serveur de traitement de la passerelle.
 *
 * Chaque serveur traite un message à la fois. Le temps de service suit une loi
 * exponentielle de moyenne 1/mu (mu = taux de service, en messages/s).
 *
 * Rappel important (vérifié dans le TP) : mu est un TAUX. Le temps de service
 * moyen vaut donc 1/mu. Pour mu = 8, la moyenne est 1/8 = 0,125 s.
 */
public class Server {

    private int     serverID;  // identifiant du serveur
    private double  mu;        // taux de service (messages/s)
    private boolean busy;      // true si le serveur traite un message
    private Random  rng;       // générateur aléatoire

    public Server(int serverID, double mu, Random rng) {
        this.serverID = serverID;
        this.mu       = mu;
        this.busy     = false;
        this.rng      = rng;
    }

    /**
     * Tire un temps de service exponentiel de moyenne 1/mu.
     * Méthode de la transformée inverse : si U ~ Uniforme(0,1),
     * alors -ln(1-U)/mu suit une exponentielle de moyenne 1/mu.
     */
    public double serviceTime() {
        double u = rng.nextDouble();          // U dans [0, 1)
        return -Math.log(1.0 - u) / mu;       // moyenne = 1/mu
    }

    /** Marque le serveur comme occupé. */
    public void occupy() {
        this.busy = true;
    }

    /** Libère le serveur (fin de service). */
    public void release() {
        this.busy = false;
    }

    /** Indique si le serveur est libre. */
    public boolean isFree() {
        return !busy;
    }

    public int getServerID() { return serverID; }

    /** Affiche l'état du serveur. */
    public void printServer() {
        System.out.printf(Locale.ROOT, "Server #%d : mu=%.1f, etat=%s%n",
                serverID, mu, (busy ? "OCCUPE" : "LIBRE"));
    }
}
