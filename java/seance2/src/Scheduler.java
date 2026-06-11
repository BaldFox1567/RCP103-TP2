import java.util.ArrayList;
import java.util.Locale;

/**
 * Scheduler.java — L'ordonnanceur : maintient la liste des événements futurs,
 * triée par date croissante.
 *
 * Il ne faut pas re-trier toute la liste à chaque ajout : AddEvent() fait une
 * insertion ordonnée directe. La liste reste ainsi triée en permanence, sans
 * aucun appel à sort(). Comme la comparaison se fait directement sur les dates
 * dans AddEvent(), la classe Event n'a pas besoin d'être comparable.
 */
public class Scheduler {

    private ArrayList<Event> eventList;   // liste des événements, toujours triée
    private double           currentTime; // temps courant de la simulation

    public Scheduler() {
        this.eventList   = new ArrayList<Event>();
        this.currentTime = 0.0;
    }

    /**
     * Insertion ordonnée directe :
     *   - si l'événement est nul, il est ignoré ;
     *   - si la liste est vide, il est ajouté tel quel ;
     *   - sinon, on parcourt la liste jusqu'au premier événement dont la date
     *     est plus grande et on insère juste avant ;
     *   - si aucun événement plus tardif n'est trouvé, il est placé en fin de liste.
     */
    public void AddEvent(Event e) {
        if (e == null) {
            return;
        }
        if (eventList.isEmpty()) {
            eventList.add(e);
            return;
        }
        for (int i = 0; i < eventList.size(); i++) {
            if (eventList.get(i).GetEventTime() > e.GetEventTime()) {
                eventList.add(i, e);   // insertion juste avant le premier plus tardif
                return;
            }
        }
        eventList.add(e);              // aucun plus tardif : en fin de liste
    }

    /**
     * Retire et renvoie le premier élément (le plus tôt),
     * puis met à jour currentTime. Renvoie null si la liste est vide.
     */
    public Event GetEvent() {
        if (eventList.isEmpty()) {
            return null;
        }
        Event e = eventList.remove(0);
        currentTime = e.GetEventTime();
        return e;
    }

    /** Retourne le temps courant. */
    public double GetCurrentTime() {
        return currentTime;
    }

    /** Indique s'il reste des événements. */
    public boolean HasEvents() {
        return !eventList.isEmpty();
    }

    /** Affiche le contenu de la liste. */
    public void PrintScheduler() {
        System.out.printf(Locale.ROOT,
            "Scheduler : %d evenement(s), currentTime=%.3f%n",
            eventList.size(), currentTime);
        for (Event e : eventList) {
            System.out.print("   ");
            e.PrintEvent();
        }
    }
}
