import java.util.Random;

public class Client {

    private int clientID;
    private int destination;
    private double lambda;
    private int nbMessages;
    private Random random;

    public Client() {
        this.clientID = 0;
        this.destination = 0;
        this.lambda = 0.0;
        this.nbMessages = 0;
        this.random = new Random();
    }

    public Client(int clientID, int destination, double lambda) {
        this.clientID = clientID;
        this.destination = destination;
        this.lambda = lambda;
        this.nbMessages = 0;
        this.random = new Random();
    }

    // Tire une inter-arrivee exponentielle : lambda est un taux (msg/s),
    // la moyenne vaut donc 1/lambda (transformee inverse : -ln(U)/lambda)
    public double GetInterArrivalTime() {
        double u = this.random.nextDouble();
        return -Math.log(1.0 - u) / this.lambda;
    }

    // Cree le prochain message a envoyer (identifiant incremente,
    // sendTime = instant d'envoi, arrivedTime pose a la reception)
    public Message GenerateMessage(double sendTime) {
        this.nbMessages = this.nbMessages + 1;
        return new Message(this.nbMessages, this.clientID, this.destination, sendTime, 0.0);
    }

    // Getters
    public int GetClientID() {
        return this.clientID;
    }

    public int GetDestination() {
        return this.destination;
    }

    public double GetLambda() {
        return this.lambda;
    }

    public int GetNbMessages() {
        return this.nbMessages;
    }

    // Setters
    public void SetClientID(int clientID) {
        this.clientID = clientID;
    }

    public void SetDestination(int destination) {
        this.destination = destination;
    }

    public void SetLambda(double lambda) {
        this.lambda = lambda;
    }

    // Affichage
    public void PrintClient() {
        System.out.println("Client");
        System.out.println("  clientID   : " + this.clientID);
        System.out.println("  destination: " + this.destination);
        System.out.println("  lambda     : " + this.lambda);
        System.out.println("  nbMessages : " + this.nbMessages);
    }
}
