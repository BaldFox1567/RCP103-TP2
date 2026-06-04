"""
server.py - Classe Server : un serveur de traitement de la passerelle.

Chaque serveur traite un message à la fois. Le temps de service suit une loi
exponentielle de moyenne 1/mu (mu = taux de service).
"""


class Server:
    def __init__(self, server_id, mu, rng):
        self.serverID = server_id
        self.mu = mu              # taux de service (messages/s)
        self.occupe = False       # True si le serveur traite un message
        self._rng = rng

    def DureeService(self):
        """Tire un temps de service exponentiel de moyenne 1/mu."""
        return self._rng.exponential(1.0 / self.mu)

    def Occuper(self):
        self.occupe = True

    def Liberer(self):
        self.occupe = False

    def EstLibre(self):
        return not self.occupe
