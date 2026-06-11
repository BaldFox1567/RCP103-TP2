import java.util.LinkedList;

/**
 * Queue.java — La file d'attente de la passerelle (FIFO).
 *
 * Les messages qui arrivent alors que le serveur est occupé patientent ici,
 * dans l'ordre d'arrivée : premier arrivé, premier servi. Mêmes principes que
 * le Scheduler : AddMessage() ajoute (en fin de file, pas de tri nécessaire),
 * GetMessage() retire et renvoie le premier élément.
 */
public class Queue {

    private LinkedList<Message> messageList;  // la file : ajout en queue, retrait en tête

    public Queue() {
        this.messageList = new LinkedList<Message>();
    }

    /** Ajoute un message en fin de file (un message nul est ignoré). */
    public void AddMessage(Message m) {
        if (m == null) {
            return;
        }
        messageList.addLast(m);
    }

    /** Retire et renvoie le premier message (le plus ancien). null si vide. */
    public Message GetMessage() {
        if (messageList.isEmpty()) {
            return null;
        }
        return messageList.removeFirst();
    }

    /** Indique si la file est vide. */
    public boolean IsEmpty() {
        return messageList.isEmpty();
    }

    /** Nombre de messages actuellement en attente. */
    public int GetSize() {
        return messageList.size();
    }

    /** Affiche le contenu de la file. */
    public void PrintQueue() {
        System.out.print("Queue : " + messageList.size() + " message(s) [ ");
        for (Message m : messageList) {
            System.out.print("#" + m.GetMessageID() + " ");
        }
        System.out.println("]");
    }
}
