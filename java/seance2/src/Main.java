import java.util.Locale;
import java.util.Random;

/**
 * Main.java — Point d'entrée de la Séance 2 (projet cumulatif).
 *
 * Cette séance ajoute les classes Client, Queue et Server. Le Main rappelle
 * d'abord brièvement que les briques de la Séance 1 fonctionnent toujours
 * (Message, Event, Scheduler), puis teste les trois nouvelles classes.
 *
 * Développement incrémental : on teste une classe à la fois, dans l'ordre
 * Client → Queue → Server.
 */
public class Main {

    private static final int NODE_GATEWAY = 0;

    // ===============================================================
    //  RAPPEL SÉANCE 1 (briques de base)
    // ===============================================================
    public static void testRappelSeance1() {
        System.out.println("########################################");
        System.out.println(" RAPPEL SEANCE 1 (briques de base)");
        System.out.println("########################################");

        // Message
        Message m = new Message(1, 1, 0);
        m.setSendTime(1.202);
        m.setArrivedTime(1.916);
        m.printMessage();
        assert m.getMessageID() == 1 && Math.abs(m.getSendTime() - 1.202) < 1e-9;

        // Event
        Event e = new Event(10, EventType.SEND_MSG, 1.202, m);
        e.printEvent();
        assert e.getEventType() == EventType.SEND_MSG;

        // Scheduler : insertion désordonnée -> extraction chronologique
        Scheduler s = new Scheduler();
        s.addEvent(new Event(1, EventType.MSG_DEPT, 4.572, m));
        s.addEvent(new Event(2, EventType.SEND_MSG, 1.202, m));
        s.addEvent(new Event(3, EventType.RECV_MSG, 1.916, m));
        double tPrec = -1.0;
        while (s.hasEvents()) {
            Event ev = s.getEvent();
            assert ev.getEventTime() >= tPrec : "ordre chronologique rompu";
            tPrec = ev.getEventTime();
        }
        System.out.println("→ Briques Seance 1 : OK\n");
    }

    // ===============================================================
    //  testClient : valide la classe Client
    // ===============================================================
    public static void testClient() {
        System.out.println("========================================");
        System.out.println(" testClient()");
        System.out.println("========================================");

        Random rng = new Random(42);
        double lambda = 4.0;                       // taux : 4 msg/s -> moyenne 1/4 = 0,25 s
        Client c = new Client(1, NODE_GATEWAY, lambda, rng);

        // Génération de quelques messages : les IDs doivent s'incrémenter,
        // et le sendTime fourni doit être bien renseigné.
        Message m1 = c.generateMessage(0.500);
        Message m2 = c.generateMessage(1.250);
        System.out.print("1er message : "); m1.printMessage();
        System.out.print("2e  message : "); m2.printMessage();
        assert m1.getMessageID() == 1 && m2.getMessageID() == 2 : "IDs non incrementaux";
        assert m1.getSource() == 1 && m1.getDestination() == 0  : "source/destination KO";
        assert Math.abs(m1.getSendTime() - 0.500) < 1e-9 : "sendTime non renseigne";

        // Vérification de la loi : sur un grand échantillon, la moyenne des
        // inter-arrivées doit approcher 1/lambda = 0,25 s.
        int n = 200_000;
        double somme = 0.0;
        for (int i = 0; i < n; i++) {
            double d = c.nextDelay();
            assert d > 0 : "inter-arrivee negative";
            somme += d;
        }
        double moyenne = somme / n;
        System.out.printf(Locale.ROOT,
                "Moyenne empirique des inter-arrivees : %.4f s (theorie 1/lambda = %.4f s)%n",
                moyenne, 1.0 / lambda);
        assert Math.abs(moyenne - 1.0 / lambda) < 0.01 : "la loi exponentielle ne colle pas";

        c.printClient();
        System.out.println("→ testClient : OK\n");
    }

    // ===============================================================
    //  testQueue : valide la classe Queue (FIFO)
    // ===============================================================
    public static void testQueue() {
        System.out.println("========================================");
        System.out.println(" testQueue()");
        System.out.println("========================================");

        Queue q = new Queue();
        assert q.isEmpty() : "une file neuve doit etre vide";

        // On enfile 3 messages
        q.enqueue(new Message(1, 1, 0));
        q.enqueue(new Message(2, 1, 0));
        q.enqueue(new Message(3, 1, 0));
        q.printQueue();
        assert q.size() == 3 : "taille incorrecte apres 3 enqueue";

        // Ordre FIFO : on doit ressortir 1, puis 2, puis 3
        Message d1 = q.dequeue();
        Message d2 = q.dequeue();
        System.out.println("dequeue -> #" + d1.getMessageID() + " puis #" + d2.getMessageID());
        assert d1.getMessageID() == 1 && d2.getMessageID() == 2 : "ordre FIFO non respecte";
        assert q.size() == 1 : "taille incorrecte apres 2 dequeue";

        q.dequeue();                       // on vide la file
        assert q.isEmpty() : "la file devrait etre vide";
        System.out.println("→ testQueue : OK\n");
    }

    // ===============================================================
    //  testServer : valide la classe Server
    // ===============================================================
    public static void testServer() {
        System.out.println("========================================");
        System.out.println(" testServer()");
        System.out.println("========================================");

        Random rng = new Random(7);
        double mu = 8.0;                           // taux : 8 msg/s -> moyenne 1/8 = 0,125 s
        Server srv = new Server(1, mu, rng);

        // État initial : libre
        srv.printServer();
        assert srv.isFree() : "un serveur neuf doit etre libre";

        // Occupation / libération
        srv.occupy();
        assert !srv.isFree() : "le serveur devrait etre occupe";
        srv.printServer();
        srv.release();
        assert srv.isFree() : "le serveur devrait etre libre";

        // Vérification de la loi : moyenne des temps de service ≈ 1/mu = 0,125 s
        int n = 200_000;
        double somme = 0.0;
        for (int i = 0; i < n; i++) {
            double d = srv.serviceTime();
            assert d > 0 : "temps de service negatif";
            somme += d;
        }
        double moyenne = somme / n;
        System.out.printf(Locale.ROOT,
                "Moyenne empirique des temps de service : %.4f s (theorie 1/mu = %.4f s)%n",
                moyenne, 1.0 / mu);
        assert Math.abs(moyenne - 1.0 / mu) < 0.005 : "la loi exponentielle ne colle pas";

        System.out.println("→ testServer : OK\n");
    }

    // ===============================================================
    //  Main
    // ===============================================================
    public static void main(String[] args) {
        testRappelSeance1();

        System.out.println("########################################");
        System.out.println(" SEANCE 2 : Client, Queue, Server");
        System.out.println("########################################\n");
        testClient();
        testQueue();
        testServer();

        System.out.println("========================================");
        System.out.println(" Seance 2 — tous les tests passes.");
        System.out.println("========================================");
    }
}
