import java.util.Locale;
import java.util.Random;

/**
 * Server.java — Un serveur de traitement de la passerelle.
 *
 * Le serveur traite un message à la fois : l'attribut busy indique s'il est
 * occupé. Le temps de service suit une loi exponentielle : mu est un taux
 * (messages/s), le temps de service moyen vaut donc 1/mu.
 * Pour mu = 8, la moyenne est 1/8 = 0.125 s.
 */
public class Server {

    private int     serverID;  // identifiant du serveur
    private double  mu;        // taux de service (messages/s)
    private boolean busy;      // true si le serveur traite un message
    private Random  random;    // générateur aléatoire

    public Server(int serverID, double mu) {
        this.serverID = serverID;
        this.mu       = mu;
        this.busy     = false;
        this.random   = new Random();
    }

    /**
     * Tire un temps de service exponentiel de moyenne 1/mu.
     * Transformée inverse : -ln(U)/mu avec U uniforme sur [0,1).
     */
    public double GetServiceTime() {
        double u = random.nextDouble();
        return -Math.log(1.0 - u) / mu;
    }

    // --- Getters / Setters ---
    public int     GetServerID() { return serverID; }
    public double  GetMu()       { return mu; }
    public boolean IsBusy()      { return busy; }
    public void    SetBusy(boolean busy) { this.busy = busy; }
    public void    SetMu(double mu)      { this.mu = mu; }

    /** Affiche tous les champs du serveur. */
    public void PrintServer() {
        System.out.printf(Locale.ROOT,
            "Server #%d : mu=%.1f msg/s, etat=%s%n",
            serverID, mu, (busy ? "OCCUPE" : "LIBRE"));
    }
}
