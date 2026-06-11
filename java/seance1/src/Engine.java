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

    // Execution des tests
    public void Run() {
        this.Test_Message();
        this.Test_Event();
        this.Test_Scheduler();
        this.Test_Trace();
    }

    public static void main(String[] args) {
        Engine engine = new Engine();
        engine.Run();
    }
}