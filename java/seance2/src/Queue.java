import java.util.ArrayDeque;

/**
 * Queue.java — La file d'attente (FIFO) de la passerelle.
 *
 * Les messages qui arrivent alors que tous les serveurs sont occupés patientent
 * ici, dans l'ordre d'arrivée : premier arrivé, premier servi (FIFO).
 *
 * Choix d'implémentation : ArrayDeque, qui offre l'ajout en queue et le retrait
 * en tête en temps constant O(1). (On n'importe volontairement pas
 * java.util.Queue pour ne pas masquer notre propre classe Queue.)
 */
public class Queue {

    // File interne : on ajoute en queue, on retire en tête
    private ArrayDeque<Message> file;

    public Queue() {
        this.file = new ArrayDeque<>();
    }

    /** Ajoute un message en queue de file. */
    public void enqueue(Message msg) {
        file.addLast(msg);
    }

    /** Retire et retourne le message en tête (le plus ancien). null si vide. */
    public Message dequeue() {
        return file.pollFirst();
    }

    /** Indique si la file est vide. */
    public boolean isEmpty() {
        return file.isEmpty();
    }

    /** Nombre de messages actuellement en attente. */
    public int size() {
        return file.size();
    }

    /** Affiche le contenu de la file (utile pour déboguer). */
    public void printQueue() {
        System.out.print("Queue (" + file.size() + " msg) : [ ");
        for (Message m : file) {
            System.out.print("#" + m.getMessageID() + " ");
        }
        System.out.println("]");
    }
}
