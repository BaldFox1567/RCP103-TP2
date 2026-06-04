"""
engine.py - Classe Engine : le moteur de la simulation à événements discrets.

Rôle :
  - stocke les paramètres (lambda, mu, nb serveurs, capacité, durée) ;
  - crée les clients (CreateClients) et la passerelle ;
  - exécute la boucle principale (Run) : extraire l'événement suivant,
    mettre à jour le temps et l'état, programmer les prochains événements ;
  - génère la trace CSV (GenerateTrace) et calcule les métriques ;
  - fournit des fonctions de test par classe (développement incrémental).

Modèle de file. Le « système » mesuré est la passerelle (file + serveurs).
Les nombres N_systeme et N_file changent aux instants RECV (arrivée) et DEPT
(départ). On accumule N·Δt à chaque événement pour obtenir les moyennes
temporelles. Le temps de séjour est mesuré de l'arrivée à la passerelle
(RECV) au départ (DEPT) : c'est ce qui se compare à la théorie M/M/c.
Le délai de propagation ne fait que décaler les arrivées et n'entre donc pas
dans W (voir le rapport).
"""

import numpy as np

from config import (SEND_MSG, RECV_MSG, MSG_DEPT, NODE_GATEWAY,
                    DELAI_PROPAGATION, GRAINE_DEFAUT)
from event import Event
from scheduler import Scheduler
from client import Client
from gateway import Gateway


class Engine:
    def __init__(self, lam, mu, duree, nb_serveurs=1, capacite=None,
                 nb_clients=1, delai_propagation=DELAI_PROPAGATION,
                 graine=GRAINE_DEFAUT):
        # --- Paramètres de simulation ---
        self.lam = lam                      # taux d'arrivée global (msg/s)
        self.mu = mu                        # taux de service par serveur (msg/s)
        self.duree = duree                  # durée simulée T (s)
        self.nb_serveurs = nb_serveurs
        self.capacite = capacite            # K (None = infini)
        self.nb_clients = nb_clients
        self.delai_propagation = delai_propagation
        self.graine = graine

        # --- Objets de la simulation ---
        self._rng = np.random.default_rng(graine)
        self.scheduler = Scheduler()
        self.gateway = Gateway(nb_serveurs, capacite, mu, self._rng)
        self.clients = []
        self._prochain_event_id = 0

        # --- Trace ---
        self._trace_active = False
        self._trace_fichier = None
        self._trace_max = None
        self._trace_lignes = 0

    # ------------------------------------------------------------------
    #  Création des clients
    # ------------------------------------------------------------------
    def CreateClients(self, n):
        """Crée n clients. Un seul client génère le flot global de taux lambda.

        Avec un client unique (cas du sujet), ce client porte tout le taux
        lambda. Si n > 1, on répartit lambda également entre les clients : la
        superposition de n processus de Poisson de taux lambda/n reste un
        processus de Poisson de taux lambda.
        """
        self.clients = []
        lam_par_client = self.lam / n
        for i in range(n):
            # clientID >= 1 (0 est réservé à la passerelle dans la trace)
            self.clients.append(
                Client(i + 1, NODE_GATEWAY, lam_par_client, self._rng))
        return self.clients

    # ------------------------------------------------------------------
    #  Trace CSV
    # ------------------------------------------------------------------
    def OuvrirTrace(self, chemin, max_lignes=None):
        """Active l'écriture de la trace CSV dans un fichier."""
        self._trace_fichier = open(chemin, "w", encoding="utf-8")
        self._trace_fichier.write("time,node,event,source,destination,msgID\n")
        self._trace_active = True
        self._trace_max = max_lignes
        self._trace_lignes = 0

    def GenerateTrace(self, e, node):
        """Écrit une ligne de trace après le traitement d'un événement.

        Colonnes : time, node, event, source, destination, msgID.
        node : 0 = passerelle, N = client.
        """
        if not self._trace_active:
            return
        if self._trace_max is not None and self._trace_lignes >= self._trace_max:
            return
        m = e.Message
        self._trace_fichier.write(
            f"{e.GetEventTime():.3f},{node},{e.GetEventType()},"
            f"{m.source},{m.destination},{m.messageID}\n")
        self._trace_lignes += 1

    def FermerTrace(self):
        if self._trace_fichier is not None:
            self._trace_fichier.close()
            self._trace_fichier = None
            self._trace_active = False

    def _nouvel_id_event(self):
        self._prochain_event_id += 1
        return self._prochain_event_id

    # ------------------------------------------------------------------
    #  Boucle principale
    # ------------------------------------------------------------------
    def Run(self, chemin_trace=None, trace_max=None, rampe=0.0):
        """Exécute la simulation et retourne le dictionnaire des métriques.

        chemin_trace : si fourni, écrit la trace CSV (limitée à trace_max lignes).
        rampe        : durée de chauffe (warm-up) en secondes, exclue des
                       statistiques pour réduire le biais de démarrage.
        """
        if chemin_trace is not None:
            self.OuvrirTrace(chemin_trace, trace_max)

        # Clients (par défaut : nb_clients du constructeur)
        if not self.clients:
            self.CreateClients(self.nb_clients)

        g = self.gateway

        # --- Compteurs ---
        nb_arrivees = 0      # arrivées à la passerelle (RECV)
        nb_acceptes = 0      # messages admis dans le système
        nb_rejets = 0        # messages rejetés (système plein)
        nb_departs = 0       # messages servis et partis (DEPT)

        # --- Accumulateurs temporels (aire N·Δt) ---
        aire_systeme = 0.0
        aire_file = 0.0
        t_prec = 0.0

        # --- Sommes pour les temps d'attente / séjour ---
        somme_sejour = 0.0   # sum (t_depart - t_arrivee)
        nb_sejour = 0
        somme_attente = 0.0  # sum (t_debut_service - t_arrivee)
        nb_attente = 0

        # Bornes de la fenêtre de mesure (après chauffe)
        t_debut_mesure = rampe
        t_prec = t_debut_mesure

        # --- Amorçage : premier SEND de chaque client ---
        for c in self.clients:
            msg = c.GenererMessage()
            t0 = c.ProchainDelai()
            msg.SetTimestamp(t0)
            ev = Event(self._nouvel_id_event(), SEND_MSG, t0, msg)
            ev.Message.source = c.clientID
            self.scheduler.AddEvent(ev)

        # Pour retrouver le client d'un message (afin de programmer son SEND suivant)
        client_par_id = {c.clientID: c for c in self.clients}

        # --- Boucle d'événements ---
        while self.scheduler.HasEvents():
            e = self.scheduler.GetEvent()
            t = e.GetEventTime()
            if t > self.duree:
                break  # fin de l'horizon de simulation

            # Accumulation des aires sur [t_prec, t] (uniquement après la chauffe)
            if t >= t_debut_mesure:
                dt = t - t_prec
                aire_systeme += g.NbDansSysteme() * dt
                aire_file += g.NbDansFile() * dt
                t_prec = t

            type_ev = e.GetEventType()

            # ---------------- SEND_MSG ----------------
            if type_ev == SEND_MSG:
                msg = e.Message
                cid = msg.source
                # Programmer la réception à la passerelle après propagation
                ev_recv = Event(self._nouvel_id_event(), RECV_MSG,
                                t + self.delai_propagation, msg)
                self.scheduler.AddEvent(ev_recv)
                # Programmer le prochain SEND du même client
                c = client_par_id[cid]
                msg_suivant = c.GenererMessage()
                t_suivant = t + c.ProchainDelai()
                msg_suivant.SetTimestamp(t_suivant)
                msg_suivant.source = cid
                ev_send = Event(self._nouvel_id_event(), SEND_MSG,
                                t_suivant, msg_suivant)
                self.scheduler.AddEvent(ev_send)
                self.GenerateTrace(e, node=cid)

            # ---------------- RECV_MSG ----------------
            elif type_ev == RECV_MSG:
                msg = e.Message
                msg.t_arrivee = t
                if t >= t_debut_mesure:
                    nb_arrivees += 1
                if g.SystemePlein():
                    # Système plein -> rejet (perte du message)
                    if t >= t_debut_mesure:
                        nb_rejets += 1
                else:
                    if t >= t_debut_mesure:
                        nb_acceptes += 1
                    if g.ServeurLibre():
                        # Service immédiat
                        srv = g.OccuperServeur()
                        msg.serveur = srv
                        msg.t_debut_service = t
                        if t >= t_debut_mesure:
                            somme_attente += 0.0  # attente nulle
                            nb_attente += 1
                        duree_service = srv.DureeService()
                        ev_dept = Event(self._nouvel_id_event(), MSG_DEPT,
                                        t + duree_service, msg)
                        self.scheduler.AddEvent(ev_dept)
                    else:
                        # Tous les serveurs occupés -> mise en file
                        g.file.Enfiler(msg)
                self.GenerateTrace(e, node=NODE_GATEWAY)

            # ---------------- MSG_DEPT ----------------
            elif type_ev == MSG_DEPT:
                msg = e.Message
                g.LibererServeur(msg.serveur)
                if t >= t_debut_mesure and msg.t_arrivee >= t_debut_mesure:
                    somme_sejour += (t - msg.t_arrivee)
                    nb_sejour += 1
                    nb_departs += 1
                # S'il y a des messages en attente, servir le suivant
                if not g.file.EstVide():
                    suivant = g.file.Defiler()
                    srv = g.OccuperServeur()
                    suivant.serveur = srv
                    suivant.t_debut_service = t
                    if t >= t_debut_mesure and suivant.t_arrivee >= t_debut_mesure:
                        somme_attente += (t - suivant.t_arrivee)
                        nb_attente += 1
                    duree_service = srv.DureeService()
                    ev_dept = Event(self._nouvel_id_event(), MSG_DEPT,
                                    t + duree_service, suivant)
                    self.scheduler.AddEvent(ev_dept)
                self.GenerateTrace(e, node=NODE_GATEWAY)

        self.FermerTrace()

        # --- Calcul des métriques ---
        T_mesure = self.duree - rampe
        N_systeme = aire_systeme / T_mesure        # nombre moyen dans le système
        N_file = aire_file / T_mesure              # nombre moyen en file
        W_systeme = somme_sejour / nb_sejour if nb_sejour else 0.0
        W_file = somme_attente / nb_attente if nb_attente else 0.0
        taux_rejet = nb_rejets / nb_arrivees if nb_arrivees else 0.0
        lam_eff = nb_acceptes / T_mesure           # taux effectivement admis
        rho = (self.lam / self.mu) / self.nb_serveurs

        # Vérification de la loi de Little : L doit valoir lambda_eff * W
        little_L = lam_eff * W_systeme
        little_Lq = lam_eff * W_file

        return {
            "modele": self._nom_modele(),
            "lambda": self.lam,
            "mu": self.mu,
            "serveurs": self.nb_serveurs,
            "capacite": self.capacite,
            "T": self.duree,
            "rho": rho,
            "arrivees": nb_arrivees,
            "acceptes": nb_acceptes,
            "rejets": nb_rejets,
            "departs": nb_departs,
            "taux_rejet": taux_rejet,
            "lambda_eff": lam_eff,
            "N_systeme": N_systeme,
            "N_file": N_file,
            "W_systeme": W_systeme,
            "W_file": W_file,
            "little_L": little_L,
            "little_Lq": little_Lq,
        }

    def _nom_modele(self):
        if self.capacite is None:
            return f"M/M/{self.nb_serveurs}"
        return f"M/M/{self.nb_serveurs}/{self.capacite}"

    # ------------------------------------------------------------------
    #  Fonctions de test par classe (développement incrémental)
    # ------------------------------------------------------------------
    @staticmethod
    def Test_Message():
        from message import Message
        print("--- Test_Message ---")
        m = Message(1, source=1, destination=0)
        m.SetTimestamp(1.202)
        m.PrintMessage()
        assert m.GetMessageID() == 1
        assert m.GetSource() == 1 and m.GetDestination() == 0
        assert abs(m.GetTimestamp() - 1.202) < 1e-9
        print("OK\n")

    @staticmethod
    def Test_Event():
        from message import Message
        print("--- Test_Event ---")
        m = Message(1, 1, 0)
        e = Event(10, SEND_MSG, 1.202, m)
        e.PrintEvent()
        assert e.GetEventType() == SEND_MSG
        e.SetEventTime(2.0)
        assert abs(e.GetEventTime() - 2.0) < 1e-9
        print("OK\n")

    @staticmethod
    def Test_Scheduler():
        from message import Message
        print("--- Test_Scheduler ---")
        s = Scheduler()
        # On insère dans le désordre ; le scheduler doit ressortir en ordre.
        for (eid, t) in [(1, 4.572), (2, 1.202), (3, 2.320), (4, 1.916)]:
            s.AddEvent(Event(eid, RECV_MSG, t, Message(eid, 1, 0)))
        temps = []
        while s.HasEvents():
            e = s.GetEvent()
            temps.append(e.GetEventTime())
        print("ordre extrait :", temps)
        assert temps == sorted(temps), "le scheduler doit extraire en ordre chronologique"
        print("OK\n")

    @staticmethod
    def Test_Client():
        print("--- Test_Client ---")
        rng = np.random.default_rng(0)
        c = Client(1, NODE_GATEWAY, lam=4.0, rng=rng)
        m = c.GenererMessage()
        m.PrintMessage()
        d = c.ProchainDelai()
        print(f"inter-arrivee tiree : {d:.3f} s (moyenne attendue 1/4 = 0.25)")
        assert d > 0
        print("OK\n")

    @staticmethod
    def Test_Queue():
        from file_attente import Queue
        from message import Message
        print("--- Test_Queue ---")
        q = Queue()
        assert q.EstVide()
        q.Enfiler(Message(1, 1, 0))
        q.Enfiler(Message(2, 1, 0))
        assert q.Taille() == 2
        assert q.Defiler().messageID == 1  # FIFO
        print("OK\n")

    @staticmethod
    def Test_Server():
        from server import Server
        print("--- Test_Server ---")
        rng = np.random.default_rng(0)
        s = Server(1, mu=8.0, rng=rng)
        assert s.EstLibre()
        s.Occuper()
        assert not s.EstLibre()
        s.Liberer()
        assert s.EstLibre()
        d = s.DureeService()
        print(f"duree de service tiree : {d:.3f} s (moyenne attendue 1/8 = 0.125)")
        print("OK\n")
