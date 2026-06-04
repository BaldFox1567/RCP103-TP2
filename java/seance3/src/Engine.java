import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Engine.java — Le moteur de la simulation à événements discrets.
 *
 * Il assemble toutes les briques : il crée les clients (Séance 2), construit la
 * passerelle (Gateway, Séance 3), pilote le Scheduler (Séance 1) et exécute la
 * boucle principale : extraire l'événement suivant -> avancer t -> mettre à jour
 * l'état -> programmer les prochains événements -> accumuler les métriques.
 *
 * Modèle mesuré : le « système » est la passerelle (file + serveurs). Les
 * nombres N_systeme et N_file changent aux instants RECV (arrivée admise) et
 * DEPT (départ). Chaque message porte deux instants : sendTime (envoi par le
 * client) et arrivedTime (réception à la passerelle). Le temps de séjour est
 * mesuré de l'arrivée à la passerelle au départ (sortie - arrivedTime) : le
 * délai de propagation, constant, décale seulement les arrivées et n'entre donc
 * pas dans W — c'est pourquoi W se compare directement à la théorie M/M/c.
 */
public class Engine {

    /** Délai de propagation SEND->RECV par défaut (le sujet dit 1 s). */
    public static final double DELAI_PROPAGATION_DEFAUT = 1.0;

    // --- Paramètres ---
    private final double  lam;          // taux d'arrivée global (msg/s)
    private final double  mu;           // taux de service par serveur (msg/s)
    private final double  duree;        // horizon de simulation T (s)
    private final int     nbServeurs;
    private final Integer capacite;     // K ; null = infini
    private final int     nbClients;
    private final double  delaiPropagation;
    private final long    graine;

    // --- Objets de simulation ---
    private final Random    rng;
    private final Scheduler scheduler;
    private final Gateway   gateway;
    private final Map<Integer, Client> clients = new HashMap<>();
    private int nextEventId = 0;

    // --- Trace ---
    private BufferedWriter traceWriter = null;
    private long traceMax = 0;
    private long traceLignes = 0;

    public Engine(double lam, double mu, double duree, int nbServeurs,
                  Integer capacite, int nbClients,
                  double delaiPropagation, long graine) {
        this.lam = lam;
        this.mu = mu;
        this.duree = duree;
        this.nbServeurs = nbServeurs;
        this.capacite = capacite;
        this.nbClients = nbClients;
        this.delaiPropagation = delaiPropagation;
        this.graine = graine;
        this.rng = new Random(graine);
        this.scheduler = new Scheduler();
        this.gateway = new Gateway(nbServeurs, capacite, mu, rng);
    }

    /** Constructeur court avec valeurs par défaut (1 client, propagation 1 s). */
    public Engine(double lam, double mu, double duree, int nbServeurs,
                  Integer capacite, long graine) {
        this(lam, mu, duree, nbServeurs, capacite, 1,
             DELAI_PROPAGATION_DEFAUT, graine);
    }

    private int nextEventId() { return ++nextEventId; }

    // ------------------------------------------------------------------
    //  Création des clients
    // ------------------------------------------------------------------
    /**
     * Crée n clients qui se partagent également le taux lambda. La superposition
     * de n processus de Poisson de taux lambda/n reste un Poisson de taux lambda.
     */
    public void createClients(int n) {
        clients.clear();
        double lamParClient = lam / n;
        for (int i = 0; i < n; i++) {
            // clientID >= 1 (0 est réservé à la passerelle dans la trace)
            clients.put(i + 1, new Client(i + 1, 0, lamParClient, rng));
        }
    }

    // ------------------------------------------------------------------
    //  Trace CSV
    // ------------------------------------------------------------------
    private void ouvrirTrace(Path chemin, long maxLignes) {
        try {
            Files.createDirectories(chemin.getParent());
            traceWriter = Files.newBufferedWriter(chemin);
            traceWriter.write("time,node,event,source,destination,msgID\n");
            traceMax = maxLignes;
            traceLignes = 0;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Écrit une ligne de trace pour l'événement e (node : 0=gateway, N=client). */
    public void generateTrace(Event e, int node) {
        if (traceWriter == null) return;
        if (traceMax > 0 && traceLignes >= traceMax) return;
        Message m = e.getMessage();
        try {
            traceWriter.write(String.format(Locale.ROOT, "%.3f,%d,%s,%d,%d,%d%n",
                    e.getEventTime(), node, e.getEventType(),
                    m.getSource(), m.getDestination(), m.getMessageID()));
            traceLignes++;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void fermerTrace() {
        if (traceWriter != null) {
            try { traceWriter.close(); } catch (IOException ignored) {}
            traceWriter = null;
        }
    }

    // ------------------------------------------------------------------
    //  Boucle principale
    // ------------------------------------------------------------------
    /** Lance la simulation sans trace. */
    public Map<String, Object> run() {
        return run(null, 0);
    }

    /**
     * Lance la simulation. Si cheminTrace != null, écrit la trace CSV (limitée
     * à traceMax lignes ; 0 = illimité). Retourne les métriques calculées.
     */
    public Map<String, Object> run(Path cheminTrace, long traceMaxLignes) {
        if (cheminTrace != null) ouvrirTrace(cheminTrace, traceMaxLignes);
        if (clients.isEmpty()) createClients(nbClients);

        final double D = delaiPropagation;

        // Accumulateurs temporels (aire N·Δt)
        double aireSysteme = 0.0, aireFile = 0.0, tPrec = 0.0;
        // Sommes pour les temps d'attente / séjour
        double sommeSejour = 0.0;  long nbSejour = 0;
        double sommeAttente = 0.0; long nbAttente = 0;

        // Amorçage : premier SEND de chaque client (sendTime = instant d'envoi)
        for (Client c : clients.values()) {
            double t0 = c.nextDelay();
            Message m = c.generateMessage(t0);
            scheduler.addEvent(new Event(nextEventId(), EventType.SEND_MSG, t0, m));
        }

        while (scheduler.hasEvents()) {
            Event e = scheduler.getEvent();
            double t = e.getEventTime();
            if (t > duree) break;

            // Accumulation des aires sur [tPrec, t]
            double dt = t - tPrec;
            aireSysteme += gateway.nbInSystem() * dt;
            aireFile    += gateway.nbInQueue()  * dt;
            tPrec = t;

            switch (e.getEventType()) {

                case SEND_MSG: {
                    Message m = e.getMessage();
                    // Réception à la passerelle après le délai de propagation
                    scheduler.addEvent(new Event(nextEventId(), EventType.RECV_MSG, t + D, m));
                    // Prochain SEND du même client (sendTime = instant d'envoi)
                    Client c = clients.get(m.getSource());
                    double tn = t + c.nextDelay();
                    Message mn = c.generateMessage(tn);
                    scheduler.addEvent(new Event(nextEventId(), EventType.SEND_MSG, tn, mn));
                    generateTrace(e, m.getSource());
                    break;
                }

                case RECV_MSG: {
                    Message m = e.getMessage();
                    // La passerelle renseigne arrivedTime = t et applique la logique
                    Gateway.ServiceStart ss = gateway.receive(m, t);
                    if (ss != null) {                 // service immédiat
                        sommeAttente += 0.0; nbAttente++;   // attente nulle
                        double st = ss.server.serviceTime();
                        scheduler.addEvent(new Event(nextEventId(), EventType.MSG_DEPT, t + st, ss.msg));
                    }
                    // sinon : mis en file ou rejeté (compté dans la Gateway)
                    generateTrace(e, 0);
                    break;
                }

                case MSG_DEPT: {
                    Message m = e.getMessage();
                    // Temps de séjour dans le système = sortie - arrivée à la passerelle
                    sommeSejour += (t - m.getArrivedTime()); nbSejour++;
                    Gateway.ServiceStart ss = gateway.depart(m);
                    if (ss != null) {                 // on sert le suivant de la file
                        double w = t - ss.msg.getArrivedTime();   // attente en file
                        sommeAttente += w; nbAttente++;
                        double st = ss.server.serviceTime();
                        scheduler.addEvent(new Event(nextEventId(), EventType.MSG_DEPT, t + st, ss.msg));
                    }
                    generateTrace(e, 0);
                    break;
                }
            }
        }
        fermerTrace();

        // --- Métriques ---
        double T = duree;
        double Nsys  = aireSysteme / T;
        double Nfile = aireFile / T;
        double Wsys  = nbSejour  > 0 ? sommeSejour  / nbSejour  : 0.0;
        double Wfile = nbAttente > 0 ? sommeAttente / nbAttente : 0.0;
        long arr = gateway.getArrivees(), acc = gateway.getAcceptes();
        long rej = gateway.getRejets(),   dep = gateway.getDeparts();
        double tauxRejet = arr > 0 ? (double) rej / arr : 0.0;
        double lamEff = acc / T;
        double rho = (lam / mu) / nbServeurs;

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("modele", nomModele());
        r.put("lambda", lam);
        r.put("mu", mu);
        r.put("serveurs", nbServeurs);
        r.put("capacite", capacite == null ? "inf" : capacite);
        r.put("T", T);
        r.put("rho", rho);
        r.put("arrivees", arr);
        r.put("acceptes", acc);
        r.put("rejets", rej);
        r.put("departs", dep);
        r.put("taux_rejet", tauxRejet);
        r.put("lambda_eff", lamEff);
        r.put("N_systeme", Nsys);
        r.put("N_file", Nfile);
        r.put("W_systeme", Wsys);
        r.put("W_file", Wfile);
        r.put("little_L", lamEff * Wsys);     // doit valoir N_systeme
        r.put("little_Lq", lamEff * Wfile);   // doit valoir N_file
        return r;
    }

    private String nomModele() {
        if (capacite == null) return "M/M/" + nbServeurs;
        return "M/M/" + nbServeurs + "/" + capacite;
    }

    public Gateway getGateway() { return gateway; }
}
