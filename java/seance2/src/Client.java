import java.util.Locale;
import java.util.Random;

/**
 * Client.java — La source de messages (dispositif mobile IoT).
 *
 * Le client génère un flot de messages vers la passerelle. Les inter-arrivées
 * suivent une loi exponentielle de moyenne 1/lambda (lambda = taux d'arrivée,
 * en messages/s) : c'est ce qui produit un processus de Poisson.
 *
 * Rappel (vérifié dans le TP) : lambda est un TAUX. L'inter-arrivée moyenne
 * vaut donc 1/lambda. Pour lambda = 4, la moyenne est 1/4 = 0,25 s.
 */
public class Client {

    private int    clientID;     // identifiant du client (>= 1 ; 0 = passerelle)
    private int    destination;  // nœud destinataire (0 = passerelle)
    private double lambda;       // taux d'arrivée (messages/s)
    private Random rng;          // générateur aléatoire
    private int    compteurMsg;  // pour numéroter les messages créés

    public Client(int clientID, int destination, double lambda, Random rng) {
        this.clientID    = clientID;
        this.destination = destination;
        this.lambda      = lambda;
        this.rng         = rng;
        this.compteurMsg = 0;
    }

    /**
     * Tire une inter-arrivée exponentielle de moyenne 1/lambda.
     * Transformée inverse : -ln(1-U)/lambda avec U ~ Uniforme(0,1).
     */
    public double nextDelay() {
        double u = rng.nextDouble();
        return -Math.log(1.0 - u) / lambda;   // moyenne = 1/lambda
    }

    /**
     * Crée le prochain message à émettre. Le timestamp (heure de création)
     * sera posé par l'appelant au moment de l'émission.
     */
    public Message generateMessage() {
        compteurMsg++;
        return new Message(compteurMsg, clientID, destination);
    }

    public int getClientID() { return clientID; }

    /** Affiche la configuration du client. */
    public void printClient() {
        System.out.printf(Locale.ROOT, "Client #%d : lambda=%.1f msg/s, destination=%d, messages crees=%d%n",
                clientID, lambda, destination, compteurMsg);
    }
}
