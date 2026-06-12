import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * La classe principale : le moteur de simulation (seance 3).
 *
 * Comme aux seances precedentes, elle contient le main, la duree de
 * simulation, l'instance du scheduler, la trace stockee en memoire et les
 * methodes de test. La seance 3 ajoute la passerelle (Gateway) et la boucle
 * principale Run() : extraire le prochain evenement, avancer le temps, mettre
 * a jour l'etat du systeme, programmer les evenements suivants, generer la
 * trace et accumuler les metriques.
 *
 * Logique des trois evenements (modele du cours) :
 *   - SEND_MSG : le client envoie ; le message met 1 seconde a arriver
 *     (RECV_MSG programme a t+1) et le prochain envoi du client est
 *     programme a t + inter-arrivee ;
 *   - RECV_MSG : si le systeme est plein le message est rejete ; si un
 *     serveur est libre le service commence (MSG_DEPT programme a
 *     t + temps de service) ; sinon le message est mis en file ;
 *   - MSG_DEPT : fin de traitement ; si la file n'est pas vide le serveur
 *     enchaine avec le message suivant, sinon il se libere.
 */
public class Engine {

    // Parametres de la simulation
    private int nbClients;
    private int nbServers;
    private int capacity;          // capacite K du systeme (0 = infinie)
    private double lambda;         // taux d'arrivee total (msg/s)
    private double mu;             // taux de service par serveur (msg/s)
    private double simulationTime; // duree de la simulation (s)

    // Composants du simulateur
    private Scheduler scheduler;
    private Gateway gateway;
    private ArrayList<Client> clients;
    private ArrayList<String[]> traceList;
    private int traceLimit;        // nb max de lignes conservees (0 = pas de trace)
    private int eventCounter;

    // Accumulateurs de metriques
    private double areaSystem;      // somme des N_systeme * dt
    private double areaQueue;       // somme des N_file * dt
    private double lastEventTime;
    private double sumTimeInSystem; // somme des temps de sejour (arrivee -> depart)
    private double sumWaitingTime;  // somme des temps d'attente en file
    private int nbServed;           // nombre de services commences

    // Couleurs pour l'affichage console
    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String ORANGE = "\u001B[33m";

    public Engine(int nbClients, int nbServers, int capacity,
            double lambda, double mu, double simulationTime) {
        this.nbClients = nbClients;
        this.nbServers = nbServers;
        this.capacity = capacity;
        this.lambda = lambda;
        this.mu = mu;
        this.simulationTime = simulationTime;

        this.scheduler = new Scheduler();
        this.gateway = new Gateway(nbServers, capacity, mu);
        this.clients = new ArrayList<Client>();
        this.traceList = new ArrayList<String[]>();
        this.traceLimit = 0;
        this.eventCounter = 0;

        this.areaSystem = 0.0;
        this.areaQueue = 0.0;
        this.lastEventTime = 0.0;
        this.sumTimeInSystem = 0.0;
        this.sumWaitingTime = 0.0;
        this.nbServed = 0;
    }

    // Identifiant du prochain evenement
    private int NextEventID() {
        this.eventCounter = this.eventCounter + 1;
        return this.eventCounter;
    }

    // Getters / Setters
    public double GetSimulationTime() {
        return this.simulationTime;
    }

    public void SetSimulationTime(double simulationTime) {
        this.simulationTime = simulationTime;
    }

    public Gateway GetGateway() {
        return this.gateway;
    }

    public void SetTraceLimit(int traceLimit) {
        this.traceLimit = traceLimit;
    }

    // Cree les n clients (identifiants 1..n ; 0 est reserve a la passerelle).
    // Les clients se partagent le taux lambda : la superposition de n processus
    // de Poisson de taux lambda/n reste un Poisson de taux lambda.
    public void CreateClients(int n) {
        this.clients.clear();

        for (int i = 1; i <= n; i++) {
            this.clients.add(new Client(i, 0, this.lambda / n));
        }
    }

    // ===== Gestion de la trace (memes principes que la seance 1) =====

    // Ajoute une ligne de trace en memoire
    public void GenerateTrace(Event event, int node) {
        if (this.traceLimit == 0 || this.traceList.size() >= this.traceLimit) {
            return;
        }

        if (event == null) {
            return;
        }

        Message msg = event.GetMessage();

        if (msg == null) {
            return;
        }

        String[] line = new String[6];

        line[0] = String.format(Locale.US, "%.3f", event.GetEventTime());
        line[1] = String.valueOf(node);
        line[2] = event.GetEventType();
        line[3] = String.valueOf(msg.GetSource());
        line[4] = String.valueOf(msg.GetDestination());
        line[5] = String.valueOf(msg.GetMessageID());

        this.traceList.add(line);
    }

    // Retourne une couleur selon le type d'evenement
    private String GetEventColor(String eventType) {
        if (Event.SEND_MSG.equals(eventType)) {
            return BLUE;
        }

        if (Event.RECV_MSG.equals(eventType)) {
            return GREEN;
        }

        if (Event.MSG_DEPT.equals(eventType)) {
            return ORANGE;
        }

        return RESET;
    }

    // Affiche la trace sous forme de tableau
    public void PrintTrace() {
        System.out.println("time     node  event      src  dst  msgID");
        System.out.println("------------------------------------------");

        for (int i = 0; i < this.traceList.size(); i++) {
            String[] line = this.traceList.get(i);
            String color = GetEventColor(line[2]);

            System.out.printf(
                    "%s%-8s %-5s %-10s %-4s %-4s %-5s%s%n",
                    color,
                    line[0],
                    line[1],
                    line[2],
                    line[3],
                    line[4],
                    line[5],
                    RESET);
        }

        System.out.println();
        System.out.println(BLUE + "SEND_MSG" + RESET + " = client envoie");
        System.out.println(GREEN + "RECV_MSG" + RESET + " = gateway recoit");
        System.out.println(ORANGE + "MSG_DEPT" + RESET + " = service termine");
    }

    // Exporte la trace dans un fichier CSV
    public void ExportTraceCSV(String filename) {
        try {
            FileWriter writer = new FileWriter(filename);

            writer.write("time,node,event,src,dst,msgID\n");

            for (int i = 0; i < this.traceList.size(); i++) {
                String[] line = this.traceList.get(i);

                writer.write(line[0] + ","
                        + line[1] + ","
                        + line[2] + ","
                        + line[3] + ","
                        + line[4] + ","
                        + line[5] + "\n");
            }

            writer.close();

            System.out.println("Trace exportee dans le fichier : " + filename);
        } catch (IOException e) {
            System.out.println("Erreur pendant l'export CSV.");
        }
    }

    // ===== Boucle principale de simulation =====

    // Lance la simulation : amorce le premier envoi de chaque client, puis
    // traite les evenements en ordre chronologique jusqu'a la fin de la duree
    public void Run() {
        CreateClients(this.nbClients);

        // Amorcage : premier SEND_MSG de chaque client
        for (int i = 0; i < this.clients.size(); i++) {
            Client client = this.clients.get(i);
            double t0 = client.GetInterArrivalTime();
            this.scheduler.AddEvent(
                    new Event(NextEventID(), Event.SEND_MSG, t0, client.GenerateMessage(t0)));
        }

        while (this.scheduler.HasEvents()) {
            Event event = this.scheduler.GetEvent();
            double t = event.GetEventTime();

            if (t > this.simulationTime) {
                break;
            }

            // Moyennes temporelles : l'etat n'a pas change sur [lastEventTime, t]
            this.areaSystem = this.areaSystem + this.gateway.GetNbInSystem() * (t - this.lastEventTime);
            this.areaQueue = this.areaQueue + this.gateway.GetNbInQueue() * (t - this.lastEventTime);
            this.lastEventTime = t;

            // Mise a jour de l'etat selon le type d'evenement
            if (Event.SEND_MSG.equals(event.GetEventType())) {
                ProcessSendMsg(event, t);
            } else if (Event.RECV_MSG.equals(event.GetEventType())) {
                ProcessRecvMsg(event, t);
            } else if (Event.MSG_DEPT.equals(event.GetEventType())) {
                ProcessMsgDept(event, t);
            }

            // Une ligne de trace apres chaque evenement
            int node = 0;

            if (Event.SEND_MSG.equals(event.GetEventType())) {
                node = event.GetMessage().GetSource();
            } else {
                node = event.GetMessage().GetDestination();
            }

            this.GenerateTrace(event, node);
        }
    }

    // SEND_MSG : le message part vers la passerelle, le client prepare le suivant
    private void ProcessSendMsg(Event event, double t) {
        Message msg = event.GetMessage();

        // Le message met 1 seconde a arriver : RECV_MSG programme a t+1
        this.scheduler.AddEvent(new Event(NextEventID(), Event.RECV_MSG, t + 1.0, msg));

        // Prochain envoi du meme client
        Client client = this.clients.get(msg.GetSource() - 1);
        double tNext = t + client.GetInterArrivalTime();
        this.scheduler.AddEvent(
                new Event(NextEventID(), Event.SEND_MSG, tNext, client.GenerateMessage(tNext)));
    }

    // RECV_MSG : rejet si plein, service si serveur libre, sinon mise en file
    private void ProcessRecvMsg(Event event, double t) {
        Message msg = event.GetMessage();
        msg.SetArrivedTime(t);
        this.gateway.MessageArrival();

        if (this.gateway.IsFull()) {
            this.gateway.MessageRejected();
            return;
        }

        this.gateway.MessageAccepted();

        Server server = this.gateway.GetFreeServer();

        if (server != null) {
            // Service immediat : attente nulle
            server.SetBusy(true);
            this.nbServed = this.nbServed + 1;
            this.scheduler.AddEvent(
                    new Event(NextEventID(), Event.MSG_DEPT, t + server.GetServiceTime(), msg));
        } else {
            this.gateway.GetQueue().AddMessage(msg);
        }
    }

    // MSG_DEPT : le message sort ; le serveur enchaine avec le suivant ou se libere
    private void ProcessMsgDept(Event event, double t) {
        Message msg = event.GetMessage();
        this.gateway.MessageDeparture();

        // Temps de sejour dans le systeme : de l'arrivee a la passerelle au depart
        this.sumTimeInSystem = this.sumTimeInSystem + (t - msg.GetArrivedTime());

        if (!this.gateway.GetQueue().IsEmpty()) {
            // Le serveur libere enchaine immediatement avec le premier de la file
            Message next = this.gateway.GetQueue().GetMessage();
            this.sumWaitingTime = this.sumWaitingTime + (t - next.GetArrivedTime());
            this.nbServed = this.nbServed + 1;

            Server server = this.gateway.GetBusyServer();   // il reste occupe
            this.scheduler.AddEvent(
                    new Event(NextEventID(), Event.MSG_DEPT, t + server.GetServiceTime(), next));
        } else {
            this.gateway.GetBusyServer().SetBusy(false);
        }
    }

    // ===== Metriques =====

    // Nombre moyen de messages dans le systeme (moyenne ponderee par le temps)
    public double GetAvgNbInSystem() {
        return this.areaSystem / this.simulationTime;
    }

    // Nombre moyen de messages dans la file
    public double GetAvgNbInQueue() {
        return this.areaQueue / this.simulationTime;
    }

    // Temps moyen dans le systeme (arrivee a la passerelle -> depart)
    public double GetAvgTimeInSystem() {
        if (this.gateway.GetNbDepartures() == 0) {
            return 0.0;
        }

        return this.sumTimeInSystem / this.gateway.GetNbDepartures();
    }

    // Temps moyen d'attente en file
    public double GetAvgWaitingTime() {
        if (this.nbServed == 0) {
            return 0.0;
        }

        return this.sumWaitingTime / this.nbServed;
    }

    // Taux de rejet : calcule et porte par la passerelle
    public double GetRejectionRate() {
        return this.gateway.GetRejectionRate();
    }

    // Nom du modele simule : M/M/c ou M/M/c/K
    public String GetModelName() {
        if (this.capacity > 0) {
            return "M/M/" + this.nbServers + "/" + this.capacity;
        }

        return "M/M/" + this.nbServers;
    }

    // Affiche les metriques de la simulation, avec verification de Little
    public void PrintResults() {
        double lambdaEff = this.gateway.GetNbAccepted() / this.simulationTime;

        System.out.println("Resultats " + GetModelName()
                + " (lambda=" + this.lambda + ", mu=" + this.mu
                + ", T=" + this.simulationTime + " s)");
        System.out.println("  arrives    : " + this.gateway.GetNbArrivals());
        System.out.println("  acceptes   : " + this.gateway.GetNbAccepted());
        System.out.println("  rejetes    : " + this.gateway.GetNbRejected());
        System.out.println("  sortis     : " + this.gateway.GetNbDepartures());
        System.out.println("  N_systeme  : " + String.format(Locale.US, "%.3f", GetAvgNbInSystem()));
        System.out.println("  N_file     : " + String.format(Locale.US, "%.3f", GetAvgNbInQueue()));
        System.out.println("  W_systeme  : " + String.format(Locale.US, "%.4f", GetAvgTimeInSystem()) + " s");
        System.out.println("  W_file     : " + String.format(Locale.US, "%.4f", GetAvgWaitingTime()) + " s");
        System.out.println("  taux_rejet : " + String.format(Locale.US, "%.2f", GetRejectionRate() * 100.0) + " %");
        System.out.println("  Little     : lambda_eff * W_systeme = "
                + String.format(Locale.US, "%.3f", lambdaEff * GetAvgTimeInSystem())
                + " (doit valoir N_systeme = "
                + String.format(Locale.US, "%.3f", GetAvgNbInSystem()) + ")");
    }

    // ===== Methodes de test =====

    // Test de la classe Gateway : un serveur, capacite K=2. On rejoue a la main
    // la logique RECV/DEPT : service immediat, mise en file, rejet, puis
    // service du suivant a la fin du premier service.
    public static void Test_Gateway() {
        System.out.println("===== TEST GATEWAY =====");

        Gateway gateway = new Gateway(1, 2, 8.0);

        Message msg2 = new Message(2, 1, 0, 0, 2.0);

        // m1 arrive : serveur libre -> service immediat
        gateway.MessageArrival();
        gateway.MessageAccepted();
        gateway.GetFreeServer().SetBusy(true);
        System.out.println("-- m1 arrive : service immediat");
        gateway.PrintGateway();

        // m2 arrive : serveur occupe, systeme non plein -> mise en file
        gateway.MessageArrival();
        gateway.MessageAccepted();
        gateway.GetQueue().AddMessage(msg2);
        System.out.println("-- m2 arrive : mise en file");
        gateway.PrintGateway();

        // m3 arrive : systeme plein (2/2) -> rejet
        gateway.MessageArrival();

        if (gateway.IsFull()) {
            gateway.MessageRejected();
        }

        System.out.println("-- m3 arrive : rejete (systeme plein)");
        gateway.PrintGateway();

        // Fin de service de m1 : le serveur enchaine avec m2 (sorti de la file)
        gateway.MessageDeparture();
        Message next = gateway.GetQueue().GetMessage();
        System.out.println("-- m1 sort : le serveur enchaine avec m" + next.GetMessageID());
        gateway.PrintGateway();

        // Fin de service de m2 : file vide, le serveur se libere
        gateway.MessageDeparture();
        gateway.GetBusyServer().SetBusy(false);
        System.out.println("-- m2 sort : serveur libere");
        gateway.PrintGateway();

        System.out.println();
    }

    // Test de la classe Engine : courte simulation M/M/1 (lambda=4, mu=8,
    // T=1000 s) avec trace. On verifie la boucle, la trace et les metriques.
    public static void Test_Engine() {
        System.out.println("===== TEST ENGINE (M/M/1, lambda=4, mu=8, T=1000 s) =====");

        Engine engine = new Engine(1, 1, 0, 4.0, 8.0, 1000.0);
        engine.SetTraceLimit(20);   // on conserve les 20 premieres lignes
        engine.Run();

        engine.PrintTrace();
        engine.ExportTraceCSV("trace.csv");
        System.out.println();

        engine.PrintResults();
        System.out.println();
    }

    // ===== Simulations et resultats =====

    // Campagne du sujet : M/M/1, M/M/1/4, M/M/1/8 et M/M/3/8 pour lambda dans
    // {4, 6, 8, 12} et mu = 8. Affiche la comparaison avec la theorie et ecrit
    // les resultats dans results/resultats.csv.
    public static void Simulations() {
        double[] lambdas = { 4.0, 6.0, 8.0, 12.0 };
        double mu = 8.0;
        double duration = 100000.0;

        // {nb serveurs, capacite} ; capacite 0 = infinie
        int[][] modeles = { { 1, 0 }, { 1, 4 }, { 1, 8 }, { 3, 8 } };

        System.out.println("===== SIMULATIONS (T=" + (long) duration + " s) =====");
        System.out.println("modele    lambda |  N_sim     L_th |   W_sim     W_th | rejet_sim rejet_th");
        System.out.println("-------------------------------------------------------------------------");

        ArrayList<String> lignesCsv = new ArrayList<String>();
        lignesCsv.add("modele,lambda,mu,serveurs,capacite,rho,arrives,rejetes,sortis,"
                + "N_systeme,N_file,W_systeme,W_file,taux_rejet,th_L,th_W,th_rejet");

        for (int m = 0; m < modeles.length; m++) {
            int c = modeles[m][0];
            int k = modeles[m][1];

            for (int l = 0; l < lambdas.length; l++) {
                double lam = lambdas[l];

                Engine engine = new Engine(1, c, k, lam, mu, duration);
                engine.Run();

                // Valeurs theoriques pour comparaison
                String thL;
                String thW;
                String thRejet;

                if (k == 0) {
                    Theorie.Resultat th = Theorie.mm1(lam, mu);
                    thL = th.stable ? String.format(Locale.US, "%.3f", th.L) : "inf";
                    thW = th.stable ? String.format(Locale.US, "%.4f", th.W) : "inf";
                    thRejet = "0.00";
                } else {
                    Theorie.Resultat th = Theorie.mmck(lam, mu, c, k);
                    thL = String.format(Locale.US, "%.3f", th.L);
                    thW = String.format(Locale.US, "%.4f", th.W);
                    thRejet = String.format(Locale.US, "%.2f", th.pRejet * 100.0);
                }

                System.out.printf(Locale.US,
                        "%-9s %6.0f | %6.3f %8s | %7.4f %8s | %7.2f%% %7s%%%n",
                        engine.GetModelName(), lam,
                        engine.GetAvgNbInSystem(), thL,
                        engine.GetAvgTimeInSystem(), thW,
                        engine.GetRejectionRate() * 100.0, thRejet);

                lignesCsv.add(String.format(Locale.US,
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

        // Ecriture du tableau de resultats
        new File("results").mkdirs();

        try {
            FileWriter writer = new FileWriter("results/resultats.csv");

            for (int i = 0; i < lignesCsv.size(); i++) {
                writer.write(lignesCsv.get(i) + "\n");
            }

            writer.close();

            System.out.println();
            System.out.println("Resultats ecrits dans results/resultats.csv");
        } catch (IOException e) {
            System.out.println("Erreur pendant l'ecriture des resultats.");
        }
    }

    // ===== main =====

    public static void main(String[] args) {
        Test_Gateway();
        Test_Engine();
        Simulations();
    }
}
