"""
gateway.py - Classe Gateway : la passerelle IoT (le « système »).

La passerelle regroupe :
  - une file d'attente partagée (Queue),
  - un ou plusieurs serveurs (Server),
  - une capacité maximale K (nombre total de messages dans le système :
    en file + en service). K = None signifie capacité infinie (M/M/1).

C'est l'objet qui porte l'état observé par les métriques (nombre dans le
système, nombre en file). La logique d'accept/rejet/affectation est exposée
via des méthodes simples ; l'Engine les orchestre dans la boucle.
"""

from file_attente import Queue
from server import Server


class Gateway:
    def __init__(self, nb_serveurs, capacite, mu, rng):
        self.nb_serveurs = nb_serveurs
        self.capacite = capacite          # K (None = infini)
        self.file = Queue()
        self.serveurs = [Server(i + 1, mu, rng) for i in range(nb_serveurs)]

    # --- État instantané ---
    def NbOccupes(self):
        """Nombre de serveurs occupés."""
        return sum(1 for s in self.serveurs if s.occupe)

    def NbDansSysteme(self):
        """Messages dans le système = en service + en file."""
        return self.NbOccupes() + self.file.Taille()

    def NbDansFile(self):
        return self.file.Taille()

    # --- Décisions ---
    def SystemePlein(self):
        """Vrai si le système est plein (cas borné /K)."""
        if self.capacite is None:
            return False
        return self.NbDansSysteme() >= self.capacite

    def ServeurLibre(self):
        return any(s.EstLibre() for s in self.serveurs)

    def OccuperServeur(self):
        """Affecte le premier serveur libre et le retourne."""
        for s in self.serveurs:
            if s.EstLibre():
                s.Occuper()
                return s
        return None  # ne devrait pas arriver si ServeurLibre() a été vérifié

    def LibererServeur(self, serveur):
        serveur.Liberer()
