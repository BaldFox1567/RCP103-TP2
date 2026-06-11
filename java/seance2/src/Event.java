import java.util.Locale;

/**
 * Event.java — Un événement daté de la simulation.
 *
 * Membres : eventID, eventType, eventTime, message associé.
 * Les trois types autorisés sont définis comme constantes :
 * SEND_MSG, RECV_MSG, MSG_DEPT.
 *
 * À la construction comme dans le setter, le type passe par
 * IsValidEventType() : s'il ne correspond à aucun des trois types attendus,
 * il est remplacé par une chaîne vide. Cela évite de se retrouver avec un
 * événement au type incohérent (faute de frappe dans un test, etc.) et
 * fiabilise les ajouts au scheduler.
 */
public class Event {

    // Les trois types d'événements du modèle du cours
    public static final String SEND_MSG = "SEND_MSG";  // un client envoie un message
    public static final String RECV_MSG = "RECV_MSG";  // la passerelle reçoit le message
    public static final String MSG_DEPT = "MSG_DEPT";  // la passerelle termine le traitement

    private int     eventID;    // identifiant unique de l'événement
    private String  eventType;  // SEND_MSG, RECV_MSG ou MSG_DEPT
    private double  eventTime;  // date de l'événement
    private Message message;    // message concerné

    /** Constructeur : Event(id, type, time, msg). Le type est validé. */
    public Event(int eventID, String eventType, double eventTime, Message message) {
        this.eventID   = eventID;
        this.eventType = IsValidEventType(eventType) ? eventType : "";
        this.eventTime = eventTime;
        this.message   = message;
    }

    /** Vrai si le type correspond à l'un des trois types attendus. */
    public boolean IsValidEventType(String type) {
        return SEND_MSG.equals(type) || RECV_MSG.equals(type) || MSG_DEPT.equals(type);
    }

    // --- Getters ---
    public int     GetEventID()   { return eventID; }
    public String  GetEventType() { return eventType; }
    public double  GetEventTime() { return eventTime; }
    public Message GetMessage()   { return message; }

    // --- Setters ---
    public void SetEventID(int id)        { this.eventID = id; }
    public void SetEventTime(double t)    { this.eventTime = t; }
    public void SetMessage(Message m)     { this.message = m; }

    /** Le type est validé : un type inconnu est remplacé par une chaîne vide. */
    public void SetEventType(String type) {
        this.eventType = IsValidEventType(type) ? type : "";
    }

    /** Affiche tous les champs de l'événement. */
    public void PrintEvent() {
        String msgID = (message != null) ? String.valueOf(message.GetMessageID()) : "-";
        System.out.printf(Locale.ROOT,
            "Event #%d : type=%-8s, eventTime=%.3f, messageID=%s%n",
            eventID, eventType, eventTime, msgID);
    }
}
