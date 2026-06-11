import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class Engine {

    private double simulationTime;
    private Scheduler scheduler;
    private ArrayList<String[]> traceList;

    // Couleurs pour l'affichage console
    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String ORANGE = "\u001B[33m";

    public Engine() {
        this.simulationTime = 10.0;
        this.scheduler = new Scheduler();
        this.traceList = new ArrayList<String[]>();
    }

    // Getters / Setters
    public double GetSimulationTime() {
        return this.simulationTime;
    }

    public void SetSimulationTime(double simulationTime) {
        this.simulationTime = simulationTime;
    }

    public Scheduler GetScheduler() {
        return this.scheduler;
    }

    public void SetScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    // Ajoute une ligne de trace en memoire
    public void GenerateTrace(Event event, int node) {
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

    // ===== Tests de la seance 1 (deja valides) =====

    // Test de la classe Message
    public void Test_Message() {
        System.out.println("===== TEST MESSAGE =====");

        Message msg = new Message(1, 1, 0, 1.202, 0);
        msg.PrintMessage();

        System.out.println();
    }

    // Test de la classe Event
    public void Test_Event() {
        System.out.println("===== TEST EVENT =====");

        Message msg = new Message(1, 1, 0, 1.202, 0);

        Event event = new Event(1, Event.SEND_MSG, 1.202, msg);
        event.PrintEvent();

        System.out.println();
    }

    // Test de la classe Scheduler
    public void Test_Scheduler() {
        System.out.println("===== TEST SCHEDULER =====");

        this.scheduler = new Scheduler();

        Message msg1 = new Message(1, 1, 0, 1.202, 0);
        Message msg2 = new Message(2, 3, 0, 2.320, 0);
        Message msg3 = new Message(3, 2, 0, 0.950, 0);

        Event e1 = new Event(1, Event.SEND_MSG, 1.202, msg1);
        Event e2 = new Event(2, Event.SEND_MSG, 2.320, msg2);
        Event e3 = new Event(3, Event.SEND_MSG, 0.950, msg3);

        this.scheduler.AddEvent(e1);
        this.scheduler.AddEvent(e2);
        this.scheduler.AddEvent(e3);

        System.out.println("Liste des evenements apres insertion chronologique :");
        this.scheduler.PrintScheduler();
        System.out.println();

        System.out.println("Extraction des evenements dans l'ordre :");
        while (this.scheduler.HasEvents()) {
            Event event = this.scheduler.GetEvent();

            if (event != null) {
                event.PrintEvent();
            }
        }

        System.out.println();
    }

    // Test de la trace
    public void Test_Trace() {
        System.out.println("===== TEST TRACE =====");

        this.traceList.clear();
        this.scheduler = new Scheduler();

        Message msg1 = new Message(1, 1, 0, 1.202, 2.391);

        Message msg2 = new Message(2, 3, 0, 2.320, 3.391);

        Event e1 = new Event(1, Event.SEND_MSG, 1.202, msg1);
        Event e2 = new Event(2, Event.RECV_MSG, 1.916, msg1);

        Event e3 = new Event(3, Event.SEND_MSG, 2.320, msg2);
        Event e4 = new Event(4, Event.RECV_MSG, 2.391, msg2);

        Event e5 = new Event(5, Event.MSG_DEPT, 4.572, msg1);
        Event e6 = new Event(6, Event.MSG_DEPT, 5.916, msg2);

        this.scheduler.AddEvent(e5);
        this.scheduler.AddEvent(e1);
        this.scheduler.AddEvent(e6);
        this.scheduler.AddEvent(e3);
        this.scheduler.AddEvent(e2);
        this.scheduler.AddEvent(e4);

        while (this.scheduler.HasEvents()) {
            Event event = this.scheduler.GetEvent();

            if (event != null) {
                int node = 0;

                if (Event.SEND_MSG.equals(event.GetEventType())) {
                    node = event.GetMessage().GetSource();
                } else {
                    node = event.GetMessage().GetDestination();
                }

                this.GenerateTrace(event, node);
            }
        }

        this.PrintTrace();
        this.ExportTraceCSV("trace.csv");

        System.out.println();
    }

    // ===== Tests de la seance 2 (Client, Queue, Server) =====

    // Test de la classe Client
    public void Test_Client() {
        System.out.println("===== TEST CLIENT =====");

        Client client = new Client(1, 0, 4.0);
        client.PrintClient();
        System.out.println();

        Message msg1 = client.GenerateMessage(0.500);
        Message msg2 = client.GenerateMessage(1.250);
        msg1.PrintMessage();
        msg2.PrintMessage();
        System.out.println();

        client.PrintClient();

        // Verification de la loi : la moyenne des inter-arrivees
        // doit approcher 1/lambda = 0.25 s
        int n = 100000;
        double somme = 0.0;

        for (int i = 0; i < n; i++) {
            somme = somme + client.GetInterArrivalTime();
        }

        System.out.println("Moyenne des inter-arrivees sur " + n + " tirages : "
                + String.format(Locale.US, "%.4f", somme / n)
                + " (theorie 1/lambda = "
                + String.format(Locale.US, "%.4f", 1.0 / client.GetLambda()) + ")");

        System.out.println();
    }

    // Test de la classe Queue
    public void Test_Queue() {
        System.out.println("===== TEST QUEUE =====");

        // 1) File a capacite infinie (constructeur par defaut) : rien n'est
        //    jamais rejete, l'ordre de sortie est l'ordre d'entree (FIFO)
        System.out.println("-- File a capacite infinie :");

        Queue queue = new Queue();

        queue.AddMessage(new Message(1, 1, 0, 0, 0));
        queue.AddMessage(new Message(2, 1, 0, 0, 0));
        queue.AddMessage(new Message(3, 1, 0, 0, 0));

        queue.PrintQueue();
        System.out.println();

        Message msg1 = queue.GetMessage();
        Message msg2 = queue.GetMessage();

        System.out.println("GetMessage -> messageID=" + msg1.GetMessageID()
                + " puis messageID=" + msg2.GetMessageID() + " (ordre FIFO)");
        System.out.println();

        queue.PrintQueue();
        System.out.println();

        // 2) File a capacite bornee (second constructeur) : le message ajoute
        //    alors que la file est pleine est rejete et comptabilise
        System.out.println("-- File a capacite bornee (capacite = 2) :");

        Queue queueBornee = new Queue(2);

        queueBornee.AddMessage(new Message(1, 1, 0, 0, 0));
        queueBornee.AddMessage(new Message(2, 1, 0, 0, 0));

        boolean ajoute = queueBornee.AddMessage(new Message(3, 1, 0, 0, 0));

        System.out.println("Ajout du message 3 dans une file pleine -> ajoute=" + ajoute);
        queueBornee.PrintQueue();

        System.out.println();
    }

    // Test de la classe Server
    public void Test_Server() {
        System.out.println("===== TEST SERVER =====");

        Server server = new Server(1, 8.0);

        server.PrintServer();

        server.SetBusy(true);
        server.PrintServer();

        server.SetBusy(false);
        server.PrintServer();

        // Verification de la loi : la moyenne des temps de service
        // doit approcher 1/mu = 0.125 s
        int n = 100000;
        double somme = 0.0;

        for (int i = 0; i < n; i++) {
            somme = somme + server.GetServiceTime();
        }

        System.out.println("Moyenne des temps de service sur " + n + " tirages : "
                + String.format(Locale.US, "%.4f", somme / n)
                + " (theorie 1/mu = "
                + String.format(Locale.US, "%.4f", 1.0 / server.GetMu()) + ")");

        System.out.println();
    }

    // Execution des tests de la seance 2
    public void Run() {
        this.Test_Client();
        this.Test_Queue();
        this.Test_Server();
    }

    public static void main(String[] args) {
        Engine engine = new Engine();
        engine.Run();
    }
}
