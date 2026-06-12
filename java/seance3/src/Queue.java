import java.util.ArrayList;

public class Queue {

    private ArrayList<Message> messageList;
    private int capacity;     // 0 = capacite infinie

    // Constructeur par defaut : capacite infinie
    // (aucune limite sur le nombre de requetes stockees)
    public Queue() {
        this.messageList = new ArrayList<Message>();
        this.capacity = 0;
    }

    // Constructeur avec capacite maximale
    // (servira aux tests des files bornees de la seance 3)
    public Queue(int capacity) {
        this.messageList = new ArrayList<Message>();
        this.capacity = capacity;
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

    // Setter
    public void SetCapacity(int capacity) {
        this.capacity = capacity;
    }

    // Ajoute un message en fin de file (FIFO).
    // La decision d'accepter ou de rejeter (systeme plein) appartient a la
    // passerelle ; ici on ajoute simplement le message si la file peut le
    // contenir. Renvoie true si le message a ete ajoute, false sinon.
    public boolean AddMessage(Message message) {
        if (message == null) {
            return false;
        }

        if (IsFull()) {
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

        for (int i = 0; i < this.messageList.size(); i++) {
            Message message = this.messageList.get(i);
            System.out.println("  [" + i + "] messageID=" + message.GetMessageID());
        }
    }
}
