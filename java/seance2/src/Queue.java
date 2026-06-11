import java.util.ArrayList;
import java.util.Locale;

public class Queue {

    private ArrayList<Message> messageList;
    private int capacity;     // 0 = capacite infinie
    private int nbArrivals;   // nombre de requetes presentees a la file
    private int nbRejected;   // nombre de requetes rejetees

    // Constructeur par defaut : capacite infinie
    // (aucune limite sur le nombre de requetes stockees)
    public Queue() {
        this.messageList = new ArrayList<Message>();
        this.capacity = 0;
        this.nbArrivals = 0;
        this.nbRejected = 0;
    }

    // Constructeur avec capacite maximale
    // (servira aux tests des files bornees de la seance 3)
    public Queue(int capacity) {
        this.messageList = new ArrayList<Message>();
        this.capacity = capacity;
        this.nbArrivals = 0;
        this.nbRejected = 0;
    }

    // Indique si la file est vide
    public boolean IsEmpty() {
        return this.messageList.size() == 0;
    }

    // Indique si la file est pleine (jamais pleine si capacite infinie)
    public boolean IsFull() {
        return this.capacity > 0 && this.messageList.size() >= this.capacity;
    }

    // Nombre de messages en attente
    public int GetSize() {
        return this.messageList.size();
    }

    // Getters
    public int GetCapacity() {
        return this.capacity;
    }

    public int GetNbArrivals() {
        return this.nbArrivals;
    }

    public int GetNbRejected() {
        return this.nbRejected;
    }

    // Setter
    public void SetCapacity(int capacity) {
        this.capacity = capacity;
    }

    // Taux de rejet = requetes rejetees / requetes presentees.
    // Toujours nul si la capacite est infinie (aucun rejet possible).
    public double GetRejectionRate() {
        if (this.nbArrivals == 0) {
            return 0.0;
        }

        return (double) this.nbRejected / this.nbArrivals;
    }

    // Ajoute un message en fin de file (FIFO).
    // Si la file est pleine, le message est rejete et comptabilise.
    // Renvoie true si le message a ete ajoute, false sinon.
    public boolean AddMessage(Message message) {
        if (message == null) {
            return false;
        }

        this.nbArrivals = this.nbArrivals + 1;

        if (IsFull()) {
            this.nbRejected = this.nbRejected + 1;
            return false;
        }

        this.messageList.add(message);
        return true;
    }

    // Retire et renvoie le premier message (le plus ancien)
    public Message GetMessage() {
        if (IsEmpty()) {
            return null;
        }

        return this.messageList.remove(0);
    }

    // Affichage du contenu de la file
    public void PrintQueue() {
        System.out.println("Queue");

        if (this.capacity > 0) {
            System.out.println("  capacite   : " + this.capacity);
        } else {
            System.out.println("  capacite   : infinie");
        }

        System.out.println("  nbMessages : " + this.messageList.size());
        System.out.println("  nbArrivals : " + this.nbArrivals);
        System.out.println("  nbRejected : " + this.nbRejected);
        System.out.println("  tauxRejet  : "
                + String.format(Locale.US, "%.2f", GetRejectionRate() * 100.0) + " %");

        for (int i = 0; i < this.messageList.size(); i++) {
            Message message = this.messageList.get(i);
            System.out.println("  [" + i + "] messageID=" + message.GetMessageID());
        }
    }
}
