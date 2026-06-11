import java.util.Random;

public class Server {

    private int serverID;
    private double mu;
    private boolean busy;
    private Random random;

    public Server() {
        this.serverID = 0;
        this.mu = 0.0;
        this.busy = false;
        this.random = new Random();
    }

    public Server(int serverID, double mu) {
        this.serverID = serverID;
        this.mu = mu;
        this.busy = false;
        this.random = new Random();
    }

    // Tire un temps de service exponentiel : mu est un taux (msg/s),
    // la moyenne vaut donc 1/mu (transformee inverse : -ln(U)/mu)
    public double GetServiceTime() {
        double u = this.random.nextDouble();
        return -Math.log(1.0 - u) / this.mu;
    }

    // Getters
    public int GetServerID() {
        return this.serverID;
    }

    public double GetMu() {
        return this.mu;
    }

    public boolean IsBusy() {
        return this.busy;
    }

    // Setters
    public void SetServerID(int serverID) {
        this.serverID = serverID;
    }

    public void SetMu(double mu) {
        this.mu = mu;
    }

    public void SetBusy(boolean busy) {
        this.busy = busy;
    }

    // Affichage
    public void PrintServer() {
        System.out.println("Server");
        System.out.println("  serverID : " + this.serverID);
        System.out.println("  mu       : " + this.mu);
        System.out.println("  busy     : " + this.busy);
    }
}
