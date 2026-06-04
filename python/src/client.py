"""
client.py - Classe Client : source de messages (dispositif mobile IoT).

Le client génère un flot de messages vers la passerelle. Les inter-arrivées
suivent une loi exponentielle de moyenne 1/lambda (lambda = taux d'arrivée).
"""


class Client:
    def __init__(self, client_id, destination, lam, rng):
        self.clientID = client_id
        self.destination = destination   # nœud destinataire (la passerelle = 0)
        self.lam = lam                   # taux d'arrivée (messages/s)
        self._rng = rng                  # générateur aléatoire numpy
        self._compteur_msg = 0           # pour numéroter les messages

    def ProchainDelai(self):
        """Tire une inter-arrivée exponentielle de moyenne 1/lambda.

        Attention au scale numpy : exponential(scale=moyenne) = exponential(1/lambda).
        """
        return self._rng.exponential(1.0 / self.lam)

    def GenererMessage(self):
        """Crée le prochain message à émettre (timestamp posé à l'émission)."""
        # Import local pour éviter toute dépendance circulaire au chargement.
        from message import Message
        self._compteur_msg += 1
        msg = Message(self._compteur_msg, self.clientID, self.destination)
        return msg
