"""
message.py - Classe Message : un message IoT qui circule dans le système.
"""


class Message:
    """Message échangé entre un client et la passerelle.

    Membres demandés : messageID, source, destination, timestamp.
    On ajoute deux horodatages internes utiles au calcul des métriques :
      - t_arrivee      : instant d'arrivée à la passerelle (événement RECV)
      - t_debut_service: instant de début de service
    Le serveur affecté est mémorisé pour pouvoir le libérer au départ.
    """

    def __init__(self, message_id, source, destination):
        self.messageID = message_id
        self.source = source
        self.destination = destination
        self.timestamp = 0.0          # heure de création (mise à jour à l'émission)

        # Champs internes (pas dans le sujet, mais nécessaires aux métriques)
        self.t_arrivee = None         # arrivée à la passerelle
        self.t_debut_service = None   # début de service
        self.serveur = None           # serveur qui traite ce message

    # --- Getters ---
    def GetMessageID(self):
        return self.messageID

    def GetSource(self):
        return self.source

    def GetDestination(self):
        return self.destination

    def GetTimestamp(self):
        return self.timestamp

    # --- Setters ---
    def SetTimestamp(self, t):
        self.timestamp = t

    # --- Affichage ---
    def PrintMessage(self):
        print(f"Message #{self.messageID} : "
              f"source={self.source} -> destination={self.destination}, "
              f"timestamp={self.timestamp:.3f}")
