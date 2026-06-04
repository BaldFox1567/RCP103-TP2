import java.util.Locale;

/**
 * Main.java — Point d'entrée de la Séance 1.
 *
 * Contient :
 *   - testMessage()   : teste la classe Message
 *   - testEvent()     : teste la classe Event
 *   - testScheduler() : teste le Scheduler (ordre chronologique des événements)
 *   - generateTrace() : affiche une ligne de trace CSV pour un événement donné
 *
 * Ordre d'exécution : Message → Event → Scheduler (développement incrémental).
 */
public class Main {

    // Nœud 0 = passerelle (gateway), N = client N (convention du sujet)
    private static final int NODE_GATEWAY = 0;

    // ---------------------------------------------------------------
    //  Trace CSV
    // ---------------------------------------------------------------

    /**
     * Génère et affiche une ligne de trace pour un événement.
     * Format : time, node, event, source, destination, msgID
     *
     * @param e    l'événement à tracer
     * @param node le nœud concerné (0 = passerelle, N = client N)
     */
    public static void generateTrace(Event e, int node) {
        Message m = e.getMessage();
        int src  = (m != null) ? m.getSource()      : -1;
        int dst  = (m != null) ? m.getDestination() : -1;
        int mid  = (m != null) ? m.getMessageID()   : -1;
        // Locale.ROOT : séparateur décimal = '.' (format CSV portable)
        System.out.printf(Locale.ROOT, "%.3f,%d,%s,%d,%d,%d%n",
                e.getEventTime(), node, e.getEventType(), src, dst, mid);
    }

    // ---------------------------------------------------------------
    //  testMessage : valide la classe Message
    // ---------------------------------------------------------------
    public static void testMessage() {
        System.out.println("========================================");
        System.out.println(" testMessage()");
        System.out.println("========================================");

        // Création et affichage initial
        Message m = new Message(1, 1, 0);
        System.out.print("Avant renseignement : ");
        m.printMessage();

        // Renseignement des instants : envoi (sendTime) puis arrivée (arrivedTime)
        m.setSendTime(1.202);
        m.setArrivedTime(1.916);
        System.out.print("Après renseignement : ");
        m.printMessage();

        // Vérifications
        assert m.getMessageID()   == 1   : "messageID incorrect";
        assert m.getSource()      == 1   : "source incorrecte";
        assert m.getDestination() == 0   : "destination incorrecte";
        assert Math.abs(m.getSendTime()    - 1.202) < 1e-9 : "sendTime incorrect";
        assert Math.abs(m.getArrivedTime() - 1.916) < 1e-9 : "arrivedTime incorrect";

        System.out.println("→ testMessage : OK\n");
    }

    // ---------------------------------------------------------------
    //  testEvent : valide la classe Event
    // ---------------------------------------------------------------
    public static void testEvent() {
        System.out.println("========================================");
        System.out.println(" testEvent()");
        System.out.println("========================================");

        Message m = new Message(1, 1, 0);
        m.setSendTime(1.202);

        Event e = new Event(10, EventType.SEND_MSG, 1.202, m);
        System.out.print("Événement créé : ");
        e.printEvent();

        // Modification du type et du temps
        e.setEventType(EventType.RECV_MSG);
        e.setEventTime(1.916);
        System.out.print("Après set : ");
        e.printEvent();

        // Vérifications
        assert e.getEventID()   == 10                 : "eventID incorrect";
        assert e.getEventType() == EventType.RECV_MSG : "eventType incorrect";
        assert Math.abs(e.getEventTime() - 1.916) < 1e-9 : "eventTime incorrect";

        // Trace CSV de cet événement (node = client 1 pour RECV simulé)
        System.out.println("--- trace CSV ---");
        System.out.println("time,node,event,source,destination,msgID");
        generateTrace(e, 1);

        System.out.println("→ testEvent : OK\n");
    }

    // ---------------------------------------------------------------
    //  testScheduler : valide le Scheduler (ordre chronologique)
    // ---------------------------------------------------------------
    public static void testScheduler() {
        System.out.println("========================================");
        System.out.println(" testScheduler()");
        System.out.println("========================================");

        Scheduler s = new Scheduler();

        // Insertion dans le DÉSORDRE (le Scheduler doit remettre en ordre)
        //   t=4.572 (DEPT msg1), t=1.202 (SEND msg1),
        //   t=2.320 (SEND msg2), t=1.916 (RECV msg1)
        // Ces valeurs reproduisent la trace d'exemple du sujet (diapo 13).
        Message m1 = new Message(1, 1, 0);
        Message m2 = new Message(2, 3, 0);

        s.addEvent(new Event(1, EventType.MSG_DEPT,  4.572, m1));
        s.addEvent(new Event(2, EventType.SEND_MSG,  1.202, m1));
        s.addEvent(new Event(3, EventType.SEND_MSG,  2.320, m2));
        s.addEvent(new Event(4, EventType.RECV_MSG,  1.916, m1));

        System.out.println("Événements insérés dans le désordre. Extraction :");
        System.out.println("time,node,event,source,destination,msgID");

        double tPrecedent = -1.0;
        while (s.hasEvents()) {
            Event e = s.getEvent();
            e.printEvent();

            // Détermine le nœud selon le type (convention sujet : SEND = client, RECV/DEPT = gateway)
            int node = (e.getEventType() == EventType.SEND_MSG) ? e.getMessage().getSource() : NODE_GATEWAY;
            generateTrace(e, node);

            // Vérification de l'ordre chronologique
            assert e.getEventTime() >= tPrecedent
                    : "ERREUR : ordre chronologique non respecté ! t=" + e.getEventTime() + " < " + tPrecedent;
            tPrecedent = e.getEventTime();
        }

        System.out.printf(Locale.ROOT, "Temps courant final du Scheduler : %.3f%n", s.getCurrentTime());
        System.out.println("→ testScheduler : OK (ordre chronologique vérifié)\n");
    }

    // ---------------------------------------------------------------
    //  Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        // Activer les assertions Java (-ea à la JVM)
        Main.class.getClassLoader();  // simple warm-up

        testMessage();
        testEvent();
        testScheduler();

        System.out.println("========================================");
        System.out.println(" Séance 1 — tous les tests passés.");
        System.out.println("========================================");
    }
}
