"""
event.py - Classe Event : un événement daté de la simulation.
"""


class Event:
    """Événement de la simulation à événements discrets.

    Membres : EventID, Message associé, EventType, EventTime.
    Constructeur conforme au sujet : Event(id, type, time, msg).
    """

    def __init__(self, event_id, event_type, event_time, message):
        self.EventID = event_id
        self.EventType = event_type      # SEND_MSG, RECV_MSG ou MSG_DEPT
        self.EventTime = event_time      # horodatage de l'événement
        self.Message = message           # message concerné

    # --- Accès au temps ---
    def SetEventTime(self, t):
        self.EventTime = t

    def GetEventTime(self):
        return self.EventTime

    # --- Accès au type ---
    def SetEventType(self, t):
        self.EventType = t

    def GetEventType(self):
        return self.EventType

    # --- Affichage ---
    def PrintEvent(self):
        mid = self.Message.messageID if self.Message is not None else "-"
        print(f"Event #{self.EventID} : type={self.EventType}, "
              f"t={self.EventTime:.3f}, msg={mid}")

    # Comparaison par le temps : permet à heapq de trier les événements.
    # En cas d'égalité de temps, on départage par EventID (ordre stable).
    def __lt__(self, autre):
        if self.EventTime == autre.EventTime:
            return self.EventID < autre.EventID
        return self.EventTime < autre.EventTime
