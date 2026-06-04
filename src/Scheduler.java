import java.util.PriorityQueue;

/**
 * Scheduler.java — Ordonnanceur d'événements (liste chronologique).
 *
 * Choix d'implémentation : PriorityQueue (tas binaire min-heap de Java).
 *   - addEvent  : insertion en O(log n) — le tas se réorganise seul, sans sort().
 *   - getEvent  : extraction du minimum (= l'événement le plus proche) en O(log n).
 * C'est exactement ce que le sujet demande : "insertion directe au bon endroit,
 * interdit d'appeler sort() à chaque insertion".
 *
 * L'ordre est défini par Event.compareTo() : chronologique, puis par eventID.
 */
public class Scheduler {

    // Tas binaire : le premier élément est toujours l'événement le plus tôt
    private PriorityQueue<Event> tas;
    private double tempsCourant;  // temps du dernier événement extrait

    public Scheduler() {
        this.tas          = new PriorityQueue<>();
        this.tempsCourant = 0.0;
    }

    /**
     * Insère un événement au bon endroit dans le tas (O(log n)).
     * La PriorityQueue garantit l'ordre — pas de sort() appelé.
     */
    public void addEvent(Event e) {
        tas.offer(e);
    }

    /**
     * Extrait et retourne le prochain événement (le plus tôt).
     * Met à jour le temps courant.
     * Retourne null si la liste est vide.
     */
    public Event getEvent() {
        Event e = tas.poll();
        if (e != null) {
            tempsCourant = e.getEventTime();
        }
        return e;
    }

    /** Retourne le temps courant (celui du dernier événement extrait). */
    public double getCurrentTime() {
        return tempsCourant;
    }

    /** Indique s'il reste des événements à traiter. */
    public boolean hasEvents() {
        return !tas.isEmpty();
    }
}
