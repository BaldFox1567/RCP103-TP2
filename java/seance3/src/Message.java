import java.util.Locale;

/**
 * Message.java — L'entité qui circule dans le système.
 *
 * Membres : messageID, source, destination, sendTime, arrivedTime.
 *   - sendTime    : instant d'envoi du message par le client ;
 *   - arrivedTime : instant d'arrivée à la passerelle.
 */
public class Message {

    private int    messageID;    // identifiant unique du message
    private int    source;       // nœud émetteur (client)
    private int    destination;  // nœud destinataire (0 = passerelle)
    private double sendTime;     // instant d'envoi
    private double arrivedTime;  // instant d'arrivée à la passerelle

    /** Constructeur par défaut. */
    public Message() {
        this.messageID   = 0;
        this.source      = 0;
        this.destination = 0;
        this.sendTime    = 0.0;
        this.arrivedTime = 0.0;
    }

    /** Constructeur complet. */
    public Message(int messageID, int source, int destination,
                   double sendTime, double arrivedTime) {
        this.messageID   = messageID;
        this.source      = source;
        this.destination = destination;
        this.sendTime    = sendTime;
        this.arrivedTime = arrivedTime;
    }

    // --- Getters ---
    public int    GetMessageID()   { return messageID; }
    public int    GetSource()      { return source; }
    public int    GetDestination() { return destination; }
    public double GetSendTime()    { return sendTime; }
    public double GetArrivedTime() { return arrivedTime; }

    // --- Setters ---
    public void SetMessageID(int id)         { this.messageID = id; }
    public void SetSource(int source)        { this.source = source; }
    public void SetDestination(int dest)     { this.destination = dest; }
    public void SetSendTime(double t)        { this.sendTime = t; }
    public void SetArrivedTime(double t)     { this.arrivedTime = t; }

    /**
     * Remet les deux temps à zéro. Prévue pour réinitialiser les temps au
     * début du service ; servira dans les séances suivantes.
     */
    public void serviceStart() {
        this.sendTime    = 0.0;
        this.arrivedTime = 0.0;
    }

    /** Affiche tous les champs du message. */
    public void PrintMessage() {
        System.out.printf(Locale.ROOT,
            "Message #%d : source=%d, destination=%d, sendTime=%.3f, arrivedTime=%.3f%n",
            messageID, source, destination, sendTime, arrivedTime);
    }
}
