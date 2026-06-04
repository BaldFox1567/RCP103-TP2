/**
 * EventType.java — Enumération des types d'événements du simulateur.
 *
 * SEND_MSG : le client envoie un message à la passerelle.
 * RECV_MSG : la passerelle reçoit le message.
 * MSG_DEPT : fin de service (départ du message du système).
 */
public enum EventType {
    SEND_MSG,
    RECV_MSG,
    MSG_DEPT
}
