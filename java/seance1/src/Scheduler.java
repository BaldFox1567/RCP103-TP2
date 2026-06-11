import java.util.ArrayList;

public class Scheduler {

    private ArrayList<Event> eventList;
    private double currentTime;

    public Scheduler() {
        this.eventList = new ArrayList<Event>();
        this.currentTime = 0.0;
    }

    public double GetCurrentTime() {
        return this.currentTime;
    }

    // Indique s'il reste des evenements dans la liste
    public boolean HasEvents() {
        return this.eventList.size() > 0;
    }

    public void AddEvent(Event event) {
        if (event == null) {
            return;
        }

        if (this.eventList.size() == 0) {
            this.eventList.add(event);
            return;
        }

        boolean inserted = false;

        for (int i = 0; i < this.eventList.size(); i++) {
            if (event.GetEventTime() < this.eventList.get(i).GetEventTime()) {
                this.eventList.add(i, event);
                inserted = true;
                break;
            }
        }

        if (!inserted) {
            this.eventList.add(event);
        }
    }

    // Extrait le prochain evenement de la liste
    public Event GetEvent() {
        if (!HasEvents()) {
            return null;
        }

        Event event = this.eventList.remove(0);
        this.currentTime = event.GetEventTime();
        return event;
    }

    // Affichage du contenu du scheduler
    public void PrintScheduler() {
        System.out.println("Scheduler");
        System.out.println("  currentTime : " + this.currentTime);
        System.out.println("  nbEvents    : " + this.eventList.size());

        for (int i = 0; i < this.eventList.size(); i++) {
            Event event = this.eventList.get(i);
            System.out.println(
                    "  [" + i + "] time=" + event.GetEventTime()
                            + " type=" + event.GetEventType()
                            + " id=" + event.GetEventID());
        }
    }
}
