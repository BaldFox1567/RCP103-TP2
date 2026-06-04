import java.util.Locale;

/**
 * Event.java — Un événement daté de la simulation à événements discrets.
 *
 * Membres demandés : eventID, message associé, eventType (enum EventType), eventTime.
 * Constructeur : Event(id, type, time, msg).
 *
 * Comparable<Event> : permet au Scheduler (PriorityQueue) de trier les événements
 * par ordre chronologique sans appeler sort(). En cas d'égalité de temps, on
 * départage par eventID pour un ordre stable.
 */
public class Event implements Comparable<Event> {

    private int       eventID;    // identifiant unique de l'événement
    private Message   message;    // message concerné (peut être null pour un DEPT sans message)
    private EventType eventType;  // SEND_MSG, RECV_MSG ou MSG_DEPT
    private double    eventTime;  // horodatage de l'événement

    // Constructeur conforme au sujet : Event(id, type, time, msg)
    public Event(int id, EventType type, double time, Message msg) {
        this.eventID   = id;
        this.eventType = type;
        this.eventTime = time;
        this.message   = msg;
    }

    // --- Accès au temps ---
    public double    getEventTime()        { return eventTime; }
    public void      setEventTime(double t){ this.eventTime = t; }

    // --- Accès au type ---
    public EventType getEventType()            { return eventType; }
    public void      setEventType(EventType t) { this.eventType = t; }

    // --- Autres getters ---
    public int     getEventID() { return eventID; }
    public Message getMessage() { return message; }

    /** Affiche tous les membres de l'événement. */
    public void printEvent() {
        String msgId = (message != null) ? String.valueOf(message.getMessageID()) : "-";
        System.out.printf(Locale.ROOT, "Event #%d : type=%-10s t=%.3f  msg=%s%n",
                eventID, eventType, eventTime, msgId);
    }

    /**
     * Comparaison par ordre chronologique.
     * En cas d'égalité de temps, on départage par eventID (ordre stable).
     * Utilisé par PriorityQueue dans le Scheduler — pas besoin de sort().
     */
    @Override
    public int compareTo(Event autre) {
        int cmp = Double.compare(this.eventTime, autre.eventTime);
        if (cmp != 0) return cmp;
        return Integer.compare(this.eventID, autre.eventID);
    }
}
