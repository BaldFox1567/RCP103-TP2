import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Engine.java — La classe principale du simulateur.
 *
 * Elle contient le main, la durée de simulation, une instance du scheduler,
 * la trace stockée en mémoire et les méthodes de test. Pour l'instant, elle ne
 * lance pas de simulation complète : son rôle est d'orchestrer les tests et de
 * gérer la trace. Run() appelle successivement les quatre tests, et main() se
 * contente de créer un Engine et de lancer Run().
 *
 * La trace est stockée en mémoire puis réutilisée pour deux sorties :
 * un affichage dans la console et un export CSV (trace.csv).
 * Chaque ligne contient six champs : time, node, event, src, dst, msgID.
 */
public class Engine {

    private double            simulationDuration; // durée de simulation (servira aux séances suivantes)
    private Scheduler         scheduler;          // l'ordonnanceur d'événements
    private ArrayList<String> trace;              // la trace, stockée en mémoire

    public Engine() {
        this.simulationDuration = 0.0;
        this.scheduler          = new Scheduler();
        this.trace              = new ArrayList<String>();
    }

    // ===================================================================
    //  Gestion de la trace
    // ===================================================================

    /**
     * Génère la ligne de trace d'un événement et la stocke en mémoire.
     * Le nœud concerné est déterminé à partir du type : pour un SEND_MSG,
     * c'est la source (le client) ; sinon, c'est la destination, c'est-à-dire
     * la passerelle (nœud 0).
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
    //  Méthodes de test (une par classe, démarche incrémentale)
    // ===================================================================

    /** Test de la classe Message : création d'un message complet puis affichage. */
    public void Test_Message() {
        System.out.println("=== Test_Message ===");
        Message m = new Message(1, 1, 0, 1.202, 1.916);
        m.PrintMessage();
        System.out.println();
    }

    /** Test de la classe Event : un message associé à un événement SEND_MSG. */
    public void Test_Event() {
        System.out.println("=== Test_Event ===");
        Message m = new Message(1, 1, 0, 1.202, 0.0);
        Event e = new Event(1, Event.SEND_MSG, 1.202, m);
        e.PrintEvent();
        System.out.println();
    }

    /**
     * Test de la classe Scheduler : trois événements SEND_MSG créés avec des
     * dates différentes (1.202, 2.320 et 0.950), ajoutés dans un ordre non
     * chronologique. On affiche le contenu du scheduler, puis on extrait les
     * événements un par un : ils doivent ressortir du plus tôt au plus tard.
     */
    public void Test_Scheduler() {
        System.out.println("=== Test_Scheduler ===");
        Message m1 = new Message(1, 1, 0, 1.202, 0.0);
        Message m2 = new Message(2, 2, 0, 2.320, 0.0);
        Message m3 = new Message(3, 3, 0, 0.950, 0.0);

        // Ajout dans un ordre non chronologique
        scheduler.AddEvent(new Event(1, Event.SEND_MSG, 1.202, m1));
        scheduler.AddEvent(new Event(2, Event.SEND_MSG, 2.320, m2));
        scheduler.AddEvent(new Event(3, Event.SEND_MSG, 0.950, m3));

        System.out.println("Contenu du scheduler (doit etre trie par date) :");
        scheduler.PrintScheduler();

        System.out.println("Extraction des evenements un par un :");
        while (scheduler.HasEvents()) {
            Event e = scheduler.GetEvent();
            System.out.print("   ");
            e.PrintEvent();
        }
        System.out.printf(Locale.ROOT, "currentTime final = %.3f%n%n",
                scheduler.GetCurrentTime());
    }

    /**
     * Test de la trace : deux messages, puis six événements (deux envois, deux
     * réceptions et deux fins de traitement), volontairement ajoutés au
     * scheduler dans un ordre mélangé. Le scheduler les remet dans l'ordre
     * chronologique, et c'est lors de leur extraction que chaque événement est
     * transformé en une ligne de trace. La trace est ensuite affichée dans la
     * console puis exportée en CSV.
     */
    public void Test_Trace() {
        System.out.println("=== Test_Trace ===");
        Message m1 = new Message(1, 1, 0, 1.202, 1.916);
        Message m2 = new Message(2, 3, 0, 2.320, 2.391);

        // Six événements ajoutés dans un ordre mélangé
        scheduler.AddEvent(new Event(5, Event.MSG_DEPT, 4.572, m1));
        scheduler.AddEvent(new Event(3, Event.SEND_MSG, 2.320, m2));
        scheduler.AddEvent(new Event(2, Event.RECV_MSG, 1.916, m1));
        scheduler.AddEvent(new Event(6, Event.MSG_DEPT, 5.916, m2));
        scheduler.AddEvent(new Event(1, Event.SEND_MSG, 1.202, m1));
        scheduler.AddEvent(new Event(4, Event.RECV_MSG, 2.391, m2));

        // Extraction chronologique : chaque événement génère une ligne de trace
        while (scheduler.HasEvents()) {
            Event e = scheduler.GetEvent();
            GenerateTrace(e);
        }

        PrintTrace();
        ExportTrace("trace.csv");
        System.out.println();
    }

    // ===================================================================
    //  Run + main
    // ===================================================================

    /** Appelle successivement les quatre tests. */
    public void Run() {
        Test_Message();
        Test_Event();
        Test_Scheduler();
        Test_Trace();
    }

    public static void main(String[] args) {
        Engine engine = new Engine();
        engine.Run();
    }
}
