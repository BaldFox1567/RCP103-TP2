"""
file_attente.py - Classe Queue : la file d'attente (FIFO) de la passerelle.

Les messages qui arrivent alors que tous les serveurs sont occupés patientent
ici, dans l'ordre d'arrivée (premier arrivé, premier servi).
"""

from collections import deque


class Queue:
    def __init__(self):
        self._file = deque()

    def Enfiler(self, msg):
        """Ajoute un message en queue de file."""
        self._file.append(msg)

    def Defiler(self):
        """Retire et retourne le message en tête de file (le plus ancien)."""
        return self._file.popleft()

    def EstVide(self):
        return len(self._file) == 0

    def Taille(self):
        return len(self._file)
