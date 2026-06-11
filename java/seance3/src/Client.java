import java.util.Locale;
import java.util.Random;

/**
 * Client.java — Le générateur de messages (dispositif mobile IoT).
 *
 * Le client envoie des messages vers la passerelle. Les inter-arrivées suivent
 * une loi exponentielle : lambda est un taux (messages/s), l'inter-arrivée
 * moyenne vaut donc 1/lambda. Pour lambda = 4, la moyenne est 1/4 = 0.25 s.
 */
public class Client {

    private int    clientID;     // identifiant du client (>= 1 ; 0 = passerelle)
    private int    destination;  // nœud destinataire (0 = passerelle)
    private double lambda;       // taux d'arrivée (messages/s)
    private int    nbMessages;   // nombre de messages créés (sert d'identifiant)
    private Random random;       // générateur aléatoire

    public Client(int clientID, int destination, double lambda) {
        this.clientID    = clientID;
        this.destination = destination;
        this.lambda      = lambda;
        this.nbMessages  = 0;
        this.random      = new Random();
    }

    /**
     * Tire une inter-arrivée exponentielle de moyenne 1/lambda.
     * Transformée inverse : -ln(U)/lambda avec U uniforme sur [0,1).
     */
    public double GetInterArrivalTime() {
        double u = random.nextDouble();
        return -Math.log(1.0 - u) / lambda;
    }

    /**
     * Crée le prochain message à envoyer : identifiant incrémenté,
     * sendTime = instant d'envoi, arrivedTime encore à 0 (posé à la réception).
     */
    public Message GenerateMessage(double sendTime) {
        nbMessages++;
        return new Message(nbMessages, clientID, destination, sendTime, 0.0);
    }

    // --- Getters / Setters ---
    public int    GetClientID()    { return clientID; }
    public int    GetDestination() { return destination; }
    public double GetLambda()      { return lambda; }
    public int    GetNbMessages()  { return nbMessages; }
    public void   SetLambda(double lambda) { this.lambda = lambda; }

    /** Affiche tous les champs du client. */
    public void PrintClient() {
        System.out.printf(Locale.ROOT,
            "Client #%d : destination=%d, lambda=%.1f msg/s, messages crees=%d%n",
            clientID, destination, lambda, nbMessages);
    }
}
