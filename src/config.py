"""
config.py - Constantes et paramètres globaux du simulateur.

On regroupe ici tout ce qui est « réglable » pour ne pas avoir de valeurs
magiques dispersées dans le code.
"""

# --- Types d'événements (les 3 du sujet) ---
SEND_MSG = "SEND"   # le client émet un message
RECV_MSG = "RECV"   # la passerelle reçoit le message
MSG_DEPT = "DEPT"   # fin de service (départ du message)

# --- Identifiants de nœuds pour la trace ---
# Convention du sujet : 0 = passerelle (gateway), N = client.
NODE_GATEWAY = 0

# --- Délai de propagation client -> passerelle ---
# Le sujet annonce 1 s dans le texte, mais sa trace d'exemple montre
# un Δt ≈ 0.714 s. On le rend paramétrable ; il décale seulement les
# horodatages de la trace et n'affecte PAS les métriques de file
# (mesurées à partir de l'arrivée à la passerelle). Voir le rapport.
DELAI_PROPAGATION = 1.0  # secondes

# --- Graine aléatoire par défaut (reproductibilité) ---
GRAINE_DEFAUT = 12345
