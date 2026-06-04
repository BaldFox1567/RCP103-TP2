import java.util.Locale;

/**
 * Message.java — Un message IoT circulant dans le simulateur.
 *
 * Membres demandés : messageID, source, destination, timestamp.
 * Le timestamp est l'heure de création du message (mis à jour par setTimestamp).
 */
public class Message {

    private int    messageID;    // identifiant unique du message
    private int    source;       // nœud émetteur (clientID)
    private int    destination;  // nœud destinataire (0 = passerelle)
    private double timestamp;    // heure de création

    // Constructeur : timestamp initialisé à 0, mis à jour à l'émission
    public Message(int id, int src, int dst) {
        this.messageID   = id;
        this.source      = src;
        this.destination = dst;
        this.timestamp   = 0.0;
    }

    // --- Getters ---
    public int    getMessageID()   { return messageID; }
    public int    getSource()      { return source; }
    public int    getDestination() { return destination; }
    public double getTimestamp()   { return timestamp; }

    // --- Setters ---
    public void setTimestamp(double t) { this.timestamp = t; }

    /** Affiche tous les membres du message. */
    public void printMessage() {
        System.out.printf(Locale.ROOT, "Message #%d : source=%d -> destination=%d, timestamp=%.3f%n",
                messageID, source, destination, timestamp);
    }
}
