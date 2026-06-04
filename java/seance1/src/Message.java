import java.util.Locale;

/**
 * Message.java — Un message IoT circulant dans le simulateur.
 *
 * Membres : messageID, source, destination, sendTime, arrivedTime.
 *   - sendTime    : instant où le message est envoyé / créé par le client.
 *   - arrivedTime : instant où le message arrive (réception à la passerelle).
 */
public class Message {

    private int    messageID;    // identifiant unique du message
    private int    source;       // nœud émetteur (clientID)
    private int    destination;  // nœud destinataire (0 = passerelle)
    private double sendTime;     // instant d'envoi / de création (côté client)
    private double arrivedTime;  // instant d'arrivée à la passerelle (RECV)

    // Constructeur : les deux instants sont initialisés à 0, puis renseignés
    // à l'émission (sendTime) et à la réception (arrivedTime).
    public Message(int id, int src, int dst) {
        this.messageID   = id;
        this.source      = src;
        this.destination = dst;
        this.sendTime    = 0.0;
        this.arrivedTime = 0.0;
    }

    // --- Getters ---
    public int    getMessageID()   { return messageID; }
    public int    getSource()      { return source; }
    public int    getDestination() { return destination; }
    public double getSendTime()    { return sendTime; }
    public double getArrivedTime() { return arrivedTime; }

    // --- Setters ---
    public void setSendTime(double t)    { this.sendTime = t; }
    public void setArrivedTime(double t) { this.arrivedTime = t; }

    /** Affiche tous les membres du message. */
    public void printMessage() {
        System.out.printf(Locale.ROOT,
            "Message #%d : source=%d -> destination=%d, sendTime=%.3f, arrivedTime=%.3f%n",
            messageID, source, destination, sendTime, arrivedTime);
    }
}
