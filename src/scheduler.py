"""
scheduler.py - Classe Scheduler : l'ordonnanceur d'événements.

Le scheduler maintient les événements futurs en ordre chronologique.
Le sujet interdit d'appeler sort() à chaque insertion. On utilise un tas
binaire (heapq) : AddEvent insère en O(log n) et GetEvent extrait le
minimum (le prochain événement) en O(log n), sans jamais re-trier la liste
entière. L'ordre chronologique est donc garanti par construction.
"""

import heapq


class Scheduler:
    def __init__(self):
        self._tas = []            # tas binaire d'objets Event
        self._temps_courant = 0.0  # temps de l'événement en cours

    def AddEvent(self, e):
        """Insère un événement au bon endroit (ordre chronologique maintenu)."""
        heapq.heappush(self._tas, e)

    def GetEvent(self):
        """Extrait et retourne le prochain événement (le plus tôt)."""
        e = heapq.heappop(self._tas)
        self._temps_courant = e.GetEventTime()
        return e

    def GetCurrentTime(self):
        """Retourne le temps courant (celui du dernier événement extrait)."""
        return self._temps_courant

    def HasEvents(self):
        """Indique s'il reste des événements à traiter."""
        return len(self._tas) > 0
