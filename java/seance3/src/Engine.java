import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Engine.java — La classe principale : le moteur de simulation (séance 3).
 *
 * Comme aux séances précédentes, elle contient le main, la durée de
 * simulation, l'instance du scheduler, la trace stockée en mémoire et les
 * méthodes de test. La séance 3 ajoute la passerelle (Gateway) et la boucle
 * principale Run() : extraire le prochain événement, avancer le temps, mettre
 * à jour l'état du système, programmer les événements suivants, générer la
 * trace et accumuler les métriques.
 *
 * Logique des trois événements (modèle du cours) :
 *   - SEND_MSG : le client envoie un message ; il met 1 seconde à arriver,
 *     un RECV_MSG est programmé à t+1, et le prochain envoi du client est
 *     programmé à t + inter-arrivée ;
 *   - RECV_MSG : la passerelle reçoit le message ; si elle est pleine, il est
 *     rejeté ; si un serveur est libre, le service commence et un MSG_DEPT est
 *     programmé à t + temps de service ; sinon le message est mis en file ;
 *   - MSG_DEPT : fin de traitement ; si la file n'est pas vide, le serveur
 *     enchaîne avec le message suivant, sinon il est libéré.
 *
 * Métriques : moyennes temporelles (aires N·dt) pour les nombres moyens dans
 * le système et dans la file, moyennes par message pour les temps. Le temps de
 * séjour est mesuré de l'arrivée à la passerelle (arrivedTime) au départ : le
 * délai de propagation, constant, n'entre pas dans W, qui se compare donc
 * directement à la théorie M/M/1.
 */
public class Engine {

    // --- Paramètres de la simulation ---
    private int    nbClients;           // nombre de clients
    private int    nbServers;           // nombre de serveurs de la passerelle
    private int    capacity;            // capacité K du système (0 = infinie)
    private double lambda;              // taux d'arrivée total (msg/s)
    private double mu;                  // taux de service par serveur (msg/s)
    private double simulationDuration;  // durée de la simulation (s)

    // --- Composants du simulateur ---
    private Scheduler         scheduler;
    private Gateway           gateway;
    private ArrayList<Client> clients;
    private ArrayList<String> trace;      // la trace, stockée en mémoire
    private int               traceLimit; // nb max de lignes conservées (0 = pas de trace)
    private int               eventCounter;

    // --- Accumulateurs de métriques ---
    private double areaSystem;       // somme des N_systeme * dt
    private double areaQueue;        // somme des N_file * dt
    private double lastEventTime;    // date de l'événement précédent
    private double sumTimeInSystem;  // somme des temps de séjour (arrivée -> départ)
    private double sumWaitingTime;   // somme des temps d'attente en file
    private int    nbServed;         // nombre de services commencés

    public Engine(int nbClients, int nbServers, int capacity,
                  double lambda, double mu, double simulationDuration) {
        this.nbClients          = nbClients;
        this.nbServers          = nbServers;
        this.capacity           = capacity;
        this.lambda             = lambda;
        this.mu                 = mu;
        this.simulationDuration = simulationDuration;

        this.scheduler    = new Scheduler();
        this.gateway      = new Gateway(nbServers, capacity, mu);
        this.clients      = new ArrayList<Client>();
        this.trace        = new ArrayList<String>();
        this.traceLimit   = 0;
        this.eventCounter = 0;

        this.areaSystem      = 0.0;
        this.areaQueue       = 0.0;
        this.lastEventTime   = 0.0;
        this.sumTimeInSystem = 0.0;
        this.sumWaitingTime  = 0.0;
        this.nbServed        = 0;
    }

    /** Identifiant du prochain événement. */
    private int NextEventID() {
        eventCounter++;
        return eventCounter;
    }

    /**
     * Crée les n clients (identifiants 1..n ; 0 est réservé à la passerelle).
     * Les clients se partagent le taux lambda : la superposition de n processus
     * de Poisson de taux lambda/n reste un Poisson de taux lambda.
     */
    public void CreateClients(int n) {
        clients.clear();
        for (int i = 1; i <= n; i++) {
            clients.add(new Client(i, 0, lambda / n));
        }
    }

    // ===================================================================
    //  Gestion de la trace (mêmes principes que la séance 1)
    // ===================================================================

    /** Nombre maximal de lignes de trace conservées (0 = pas de trace). */
    public void SetTraceLimit(int limit) {
        this.traceLimit = limit;
    }

    /**
     * Génère la ligne de trace d'un événement et la stocke en mémoire.
     * Nœud concerné : la source pour un SEND_MSG, sinon la destination
     * (la passerelle, nœud 0).
     */
    public void GenerateTrace(Event e) {
        if (traceLimit == 0 || trace.size() >= traceLimit) {
            return;
        }
        if (e == null || e.GetMessage() == null) {
            return;
        }
        Message m = e.GetMessage();
        int node = Event.SEND_MSG.equals(e.GetEventType())
                 ? m.GetSource()
                 : m.GetDestination();
        String ligne = String.format(Locale.ROOT, "%.3f,%d,%s,%d,%d,%d",
                e.GetEventTime(), node, e.GetEventType(),
                m.GetSource(), m.GetDestination(), m.GetMessageID());
        trace.add(ligne);
    }

    /** Affiche la trace dans la console. */
    public void PrintTrace() {
        System.out.println("time,node,event,src,dst,msgID");
        for (String ligne : trace) {
            System.out.println(ligne);
        }
    }

    /** Exporte la trace dans un fichier CSV. */
    public void ExportTrace(String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("time,node,event,src,dst,msgID");
            for (String ligne : trace) {
                writer.println(ligne);
            }
            System.out.println("Trace exportee dans " + fileName);
        } catch (IOException ex) {
            System.out.println("Erreur d'ecriture de la trace : " + ex.getMessage());
        }
    }

    // ===================================================================
    //  Boucle principale de simulation
    // ===================================================================

    /**
     * Lance la simulation : amorce le premier envoi de chaque client, puis
     * traite les événements en ordre chronologique jusqu'à la fin de la durée
     * de simulation.
     */
    public void Run() {
        CreateClients(nbClients);

        // Amorçage : premier SEND_MSG de chaque client
        for (Client c : clients) {
            double t0 = c.GetInterArrivalTime();
            scheduler.AddEvent(new Event(NextEventID(), Event.SEND_MSG, t0,
                                         c.GenerateMessage(t0)));
        }

        while (scheduler.HasEvents()) {
            Event e = scheduler.GetEvent();
            double t = e.GetEventTime();
            if (t > simulationDuration) {
                break;
            }

            // Moyennes temporelles : l'état n'a pas changé sur [lastEventTime, t]
            areaSystem += gateway.GetNbInSystem() * (t - lastEventTime);
            areaQueue  += gateway.GetNbInQueue()  * (t - lastEventTime);
            lastEventTime = t;

            // Mise à jour de l'état selon le type d'événement
            if (Event.SEND_MSG.equals(e.GetEventType())) {
                ProcessSendMsg(e, t);
            } else if (Event.RECV_MSG.equals(e.GetEventType())) {
                ProcessRecvMsg(e, t);
            } else if (Event.MSG_DEPT.equals(e.GetEventType())) {
                ProcessMsgDept(e, t);
            }

            // Une ligne de trace après chaque événement
            GenerateTrace(e);
        }
    }

    /** SEND_MSG : le message part vers la passerelle, le client prépare le suivant. */
    private void ProcessSendMsg(Event e, double t) {
        Message m = e.GetMessage();
        // Le message met 1 seconde à arriver : RECV_MSG programmé à t+1
        scheduler.AddEvent(new Event(NextEventID(), Event.RECV_MSG, t + 1.0, m));
        // Prochain envoi du même client
        Client c = clients.get(m.GetSource() - 1);
        double tNext = t + c.GetInterArrivalTime();
        scheduler.AddEvent(new Event(NextEventID(), Event.SEND_MSG, tNext,
                                     c.GenerateMessage(tNext)));
    }

    /** RECV_MSG : rejet si plein, service si serveur libre, sinon mise en file. */
    private void ProcessRecvMsg(Event e, double t) {
        Message m = e.GetMessage();
        m.SetArrivedTime(t);
        gateway.MessageArrival();

        if (gateway.IsFull()) {
            gateway.MessageRejected();
            return;
        }
        gateway.MessageAccepted();

        Server s = gateway.GetFreeServer();
        if (s != null) {
            // Service immédiat : attente nulle
            s.SetBusy(true);
            nbServed++;
            scheduler.AddEvent(new Event(NextEventID(), Event.MSG_DEPT,
                                         t + s.GetServiceTime(), m));
        } else {
            gateway.GetQueue().AddMessage(m);
        }
    }

    /** MSG_DEPT : le message sort ; le serveur enchaîne avec le suivant ou se libère. */
    private void ProcessMsgDept(Event e, double t) {
        Message m = e.GetMessage();
        gateway.MessageDeparture();
        // Temps de séjour dans le système : de l'arrivée à la passerelle au départ
        sumTimeInSystem += t - m.GetArrivedTime();

        if (!gateway.GetQueue().IsEmpty()) {
            // Le serveur libéré enchaîne immédiatement avec le premier de la file
            Message next = gateway.GetQueue().GetMessage();
            sumWaitingTime += t - next.GetArrivedTime();
            nbServed++;
            Server s = gateway.GetBusyServer();   // il reste occupé
            scheduler.AddEvent(new Event(NextEventID(), Event.MSG_DEPT,
                                         t + s.GetServiceTime(), next));
        } else {
            gateway.GetBusyServer().SetBusy(false);
        }
    }

    // ===================================================================
    //  Métriques
    // ===================================================================

    /** Nombre moyen de messages dans le système (moyenne pondérée par le temps). */
    public double GetAvgNbInSystem() {
        return areaSystem / simulationDuration;
    }

    /** Nombre moyen de messages dans la file (moyenne pondérée par le temps). */
    public double GetAvgNbInQueue() {
        return areaQueue / simulationDuration;
    }

    /** Temps moyen dans le système (arrivée à la passerelle -> départ). */
    public double GetAvgTimeInSystem() {
        return (gateway.GetNbDepartures() > 0)
             ? sumTimeInSystem / gateway.GetNbDepartures() : 0.0;
    }

    /** Temps moyen d'attente en file. */
    public double GetAvgWaitingTime() {
        return (nbServed > 0) ? sumWaitingTime / nbServed : 0.0;
    }

    /** Taux de rejet = messages rejetés / messages arrivés. */
    public double GetRejectionRate() {
        return (gateway.GetNbArrivals() > 0)
             ? (double) gateway.GetNbRejected() / gateway.GetNbArrivals() : 0.0;
    }

    public Gateway GetGateway() { return gateway; }

    /** Nom du modèle simulé : M/M/c ou M/M/c/K. */
    public String GetModelName() {
        return (capacity > 0) ? "M/M/" + nbServers + "/" + capacity
                              : "M/M/" + nbServers;
    }

    /** Affiche les métriques de la simulation, avec vérification de Little. */
    public void PrintResults() {
        double lambdaEff = gateway.GetNbAccepted() / simulationDuration;
        System.out.printf(Locale.ROOT, "Modele %s : lambda=%.1f, mu=%.1f, T=%.0f s%n",
                GetModelName(), lambda, mu, simulationDuration);
        System.out.println("  arrives=" + gateway.GetNbArrivals()
                + "  acceptes=" + gateway.GetNbAccepted()
                + "  rejetes=" + gateway.GetNbRejected()
                + "  sortis=" + gateway.GetNbDepartures());
        System.out.printf(Locale.ROOT,
                "  N_systeme=%.3f  N_file=%.3f  W_systeme=%.4f s  W_file=%.4f s  taux_rejet=%.2f%%%n",
                GetAvgNbInSystem(), GetAvgNbInQueue(),
                GetAvgTimeInSystem(), GetAvgWaitingTime(), GetRejectionRate() * 100.0);
        // Loi de Little : L = lambda_eff * W (vérification du simulateur)
        System.out.printf(Locale.ROOT,
                "  Little : lambda_eff*W_systeme=%.3f (doit valoir N_systeme=%.3f)%n",
                lambdaEff * GetAvgTimeInSystem(), GetAvgNbInSystem());
    }

    // ===================================================================
    //  Méthodes de test
    // ===================================================================

    /**
     * Test de la classe Gateway : un serveur, capacité K=2. On rejoue à la main
     * la logique RECV/DEPT : service immédiat, mise en file, rejet, puis
     * service du suivant à la fin du premier service.
     */
    public static void Test_Gateway() {
        System.out.println("=== Test_Gateway ===");
        Gateway g = new Gateway(1, 2, 8.0);
        Message m1 = new Message(1, 1, 0, 0.0, 1.0);
        Message m2 = new Message(2, 1, 0, 0.0, 2.0);
        Message m3 = new Message(3, 1, 0, 0.0, 3.0);

        // m1 arrive : serveur libre -> service immédiat
        g.MessageArrival();
        g.MessageAccepted();
        g.GetFreeServer().SetBusy(true);
        System.out.print("m1 servi      -> "); g.PrintGateway();

        // m2 arrive : serveur occupé, système non plein -> mise en file
        g.MessageArrival();
        g.MessageAccepted();
        g.GetQueue().AddMessage(m2);
        System.out.print("m2 en file    -> "); g.PrintGateway();

        // m3 arrive : système plein (2/2) -> rejet
        g.MessageArrival();
        if (g.IsFull()) {
            g.MessageRejected();
        }
        System.out.print("m3 rejete     -> "); g.PrintGateway();

        // Fin de service de m1 : le serveur enchaîne avec m2 (sorti de la file)
        g.MessageDeparture();
        Message next = g.GetQueue().GetMessage();
        System.out.print("m1 sort, m" + next.GetMessageID() + " servi -> ");
        g.PrintGateway();

        // Fin de service de m2 : file vide, le serveur se libère
        g.MessageDeparture();
        g.GetBusyServer().SetBusy(false);
        System.out.print("m2 sort       -> "); g.PrintGateway();
        System.out.println();
    }

    /**
     * Test de la classe Engine : courte simulation M/M/1 (lambda=4, mu=8,
     * T=1000 s) avec trace. On vérifie la boucle, la trace et les métriques.
     */
    public static void Test_Engine() {
        System.out.println("=== Test_Engine (M/M/1, lambda=4, mu=8, T=1000 s) ===");
        Engine engine = new Engine(1, 1, 0, 4.0, 8.0, 1000.0);
        engine.SetTraceLimit(20);   // on conserve les 20 premieres lignes
        engine.Run();
        engine.PrintTrace();
        engine.ExportTrace("trace.csv");
        engine.PrintResults();
        System.out.println();
    }

    // ===================================================================
    //  Simulations et résultats
    // ===================================================================

    /**
     * Campagne de simulations du sujet : M/M/1, M/M/1/4, M/M/1/8 et M/M/3/8
     * pour lambda dans {4, 6, 8, 12} et mu = 8. Affiche la comparaison avec la
     * théorie et écrit les résultats dans results/resultats.csv.
     */
    public static void Simulations() {
        double[] lambdas  = {4.0, 6.0, 8.0, 12.0};
        double   mu       = 8.0;
        double   duration = 100000.0;
        // {nb serveurs, capacite} ; capacite 0 = infinie
        int[][] modeles = { {1, 0}, {1, 4}, {1, 8}, {3, 8} };

        System.out.println("=== Simulations (T=" + (long) duration + " s) ===");
        System.out.println("modele    lambda |  N_sim     L_th |   W_sim     W_th | rejet_sim rejet_th");
        System.out.println("-------------------------------------------------------------------------");

        ArrayList<String> lignesCsv = new ArrayList<String>();
        lignesCsv.add("modele,lambda,mu,serveurs,capacite,rho,arrives,rejetes,sortis,"
                    + "N_systeme,N_file,W_systeme,W_file,taux_rejet,th_L,th_W,th_rejet");

        for (int[] modele : modeles) {
            int c = modele[0];
            int k = modele[1];
            for (double lam : lambdas) {
                Engine engine = new Engine(1, c, k, lam, mu, duration);
                engine.Run();

                // Valeurs théoriques pour comparaison
                String thL, thW, thRejet;
                if (k == 0) {
                    Theorie.Resultat th = Theorie.mm1(lam, mu);
                    thL = th.stable ? String.format(Locale.ROOT, "%.3f", th.L) : "inf";
                    thW = th.stable ? String.format(Locale.ROOT, "%.4f", th.W) : "inf";
                    thRejet = "0.00";
                } else {
                    Theorie.Resultat th = Theorie.mmck(lam, mu, c, k);
                    thL = String.format(Locale.ROOT, "%.3f", th.L);
                    thW = String.format(Locale.ROOT, "%.4f", th.W);
                    thRejet = String.format(Locale.ROOT, "%.2f", th.pRejet * 100.0);
                }

                System.out.printf(Locale.ROOT,
                    "%-9s %6.0f | %6.3f %8s | %7.4f %8s | %7.2f%% %7s%%%n",
                    engine.GetModelName(), lam,
                    engine.GetAvgNbInSystem(), thL,
                    engine.GetAvgTimeInSystem(), thW,
                    engine.GetRejectionRate() * 100.0, thRejet);

                lignesCsv.add(String.format(Locale.ROOT,
                    "%s,%.0f,%.0f,%d,%s,%.4f,%d,%d,%d,%.4f,%.4f,%.6f,%.6f,%.6f,%s,%s,%s",
                    engine.GetModelName(), lam, mu, c, (k > 0 ? String.valueOf(k) : "inf"),
                    lam / mu / c,
                    engine.GetGateway().GetNbArrivals(),
                    engine.GetGateway().GetNbRejected(),
                    engine.GetGateway().GetNbDepartures(),
                    engine.GetAvgNbInSystem(), engine.GetAvgNbInQueue(),
                    engine.GetAvgTimeInSystem(), engine.GetAvgWaitingTime(),
                    engine.GetRejectionRate(), thL, thW, thRejet));
            }
        }

        // Écriture du tableau de résultats
        new File("results").mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter("results/resultats.csv"))) {
            for (String ligne : lignesCsv) {
                writer.println(ligne);
            }
            System.out.println("\nResultats ecrits dans results/resultats.csv");
        } catch (IOException ex) {
            System.out.println("Erreur d'ecriture des resultats : " + ex.getMessage());
        }
    }

    // ===================================================================
    //  main
    // ===================================================================

    public static void main(String[] args) {
        Test_Gateway();
        Test_Engine();
        Simulations();
    }
}
