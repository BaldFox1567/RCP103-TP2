import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Engine.java — La classe principale du simulateur (séance 2).
 *
 * Comme à la séance 1, elle contient le main, la durée de simulation, une
 * instance du scheduler, la trace stockée en mémoire et les méthodes de test.
 * La séance 2 ajoute les classes Client, Queue et Server : Run() appelle leurs
 * trois tests (les tests de la séance 1, déjà validés, restent disponibles).
 * Le simulateur ne tourne pas encore : la boucle viendra à la séance 3.
 */
public class Engine {

    private double            simulationDuration; // durée de simulation (servira à la séance 3)
    private Scheduler         scheduler;          // l'ordonnanceur d'événements
    private ArrayList<String> trace;              // la trace, stockée en mémoire

    public Engine() {
        this.simulationDuration = 0.0;
        this.scheduler          = new Scheduler();
        this.trace              = new ArrayList<String>();
    }

    // ===================================================================
    //  Gestion de la trace (séance 1)
    // ===================================================================

    /**
     * Génère la ligne de trace d'un événement et la stocke en mémoire.
     * Nœud concerné : la source pour un SEND_MSG, sinon la destination
     * (la passerelle, nœud 0).
     */
    public void GenerateTrace(Event e) {
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
    //  Tests de la séance 1 (déjà validés, conservés)
    // ===================================================================

    public void Test_Message() {
        System.out.println("=== Test_Message ===");
        Message m = new Message(1, 1, 0, 1.202, 1.916);
        m.PrintMessage();
        System.out.println();
    }

    public void Test_Event() {
        System.out.println("=== Test_Event ===");
        Message m = new Message(1, 1, 0, 1.202, 0.0);
        Event e = new Event(1, Event.SEND_MSG, 1.202, m);
        e.PrintEvent();
        System.out.println();
    }

    public void Test_Scheduler() {
        System.out.println("=== Test_Scheduler ===");
        Message m1 = new Message(1, 1, 0, 1.202, 0.0);
        Message m2 = new Message(2, 2, 0, 2.320, 0.0);
        Message m3 = new Message(3, 3, 0, 0.950, 0.0);

        scheduler.AddEvent(new Event(1, Event.SEND_MSG, 1.202, m1));
        scheduler.AddEvent(new Event(2, Event.SEND_MSG, 2.320, m2));
        scheduler.AddEvent(new Event(3, Event.SEND_MSG, 0.950, m3));

        scheduler.PrintScheduler();
        while (scheduler.HasEvents()) {
            scheduler.GetEvent().PrintEvent();
        }
        System.out.println();
    }

    public void Test_Trace() {
        System.out.println("=== Test_Trace ===");
        Message m1 = new Message(1, 1, 0, 1.202, 1.916);
        Message m2 = new Message(2, 3, 0, 2.320, 2.391);

        scheduler.AddEvent(new Event(5, Event.MSG_DEPT, 4.572, m1));
        scheduler.AddEvent(new Event(3, Event.SEND_MSG, 2.320, m2));
        scheduler.AddEvent(new Event(2, Event.RECV_MSG, 1.916, m1));
        scheduler.AddEvent(new Event(6, Event.MSG_DEPT, 5.916, m2));
        scheduler.AddEvent(new Event(1, Event.SEND_MSG, 1.202, m1));
        scheduler.AddEvent(new Event(4, Event.RECV_MSG, 2.391, m2));

        while (scheduler.HasEvents()) {
            GenerateTrace(scheduler.GetEvent());
        }
        PrintTrace();
        ExportTrace("trace.csv");
        System.out.println();
    }

    // ===================================================================
    //  Tests de la séance 2 (Client, Queue, Server)
    // ===================================================================

    /**
     * Test de la classe Client : création, affichage, génération de deux
     * messages, puis vérification de la loi des inter-arrivées : sur un grand
     * échantillon, la moyenne doit approcher 1/lambda.
     */
    public void Test_Client() {
        System.out.println("=== Test_Client ===");
        Client client = new Client(1, 0, 4.0);   // lambda = 4 msg/s
        client.PrintClient();

        // Génération de deux messages : identifiants incrémentés, sendTime posé
        Message m1 = client.GenerateMessage(0.500);
        Message m2 = client.GenerateMessage(1.250);
        m1.PrintMessage();
        m2.PrintMessage();
        client.PrintClient();

        // Vérification de la loi exponentielle des inter-arrivées
        int n = 100000;
        double somme = 0.0;
        for (int i = 0; i < n; i++) {
            somme += client.GetInterArrivalTime();
        }
        System.out.printf(Locale.ROOT,
            "Moyenne des inter-arrivees sur %d tirages : %.4f s (theorie 1/lambda = %.4f s)%n%n",
            n, somme / n, 1.0 / client.GetLambda());
    }

    /**
     * Test de la classe Queue : trois messages enfilés, puis retirés.
     * L'ordre de sortie doit être l'ordre d'entrée (FIFO).
     */
    public void Test_Queue() {
        System.out.println("=== Test_Queue ===");
        Queue queue = new Queue();
        queue.AddMessage(new Message(1, 1, 0, 0.0, 0.0));
        queue.AddMessage(new Message(2, 1, 0, 0.0, 0.0));
        queue.AddMessage(new Message(3, 1, 0, 0.0, 0.0));
        queue.PrintQueue();

        Message m1 = queue.GetMessage();
        Message m2 = queue.GetMessage();
        System.out.println("GetMessage -> #" + m1.GetMessageID()
                         + " puis #" + m2.GetMessageID() + " (ordre FIFO)");
        queue.PrintQueue();
        System.out.println();
    }

    /**
     * Test de la classe Server : état libre/occupé, puis vérification de la
     * loi des temps de service : la moyenne doit approcher 1/mu.
     */
    public void Test_Server() {
        System.out.println("=== Test_Server ===");
        Server server = new Server(1, 8.0);      // mu = 8 msg/s
        server.PrintServer();
        server.SetBusy(true);
        server.PrintServer();
        server.SetBusy(false);
        server.PrintServer();

        int n = 100000;
        double somme = 0.0;
        for (int i = 0; i < n; i++) {
            somme += server.GetServiceTime();
        }
        System.out.printf(Locale.ROOT,
            "Moyenne des temps de service sur %d tirages : %.4f s (theorie 1/mu = %.4f s)%n%n",
            n, somme / n, 1.0 / server.GetMu());
    }

    // ===================================================================
    //  Run + main
    // ===================================================================

    /** Appelle les tests des trois nouvelles classes de la séance 2. */
    public void Run() {
        Test_Client();
        Test_Queue();
        Test_Server();
    }

    public static void main(String[] args) {
        Engine engine = new Engine();
        engine.Run();
    }
}
