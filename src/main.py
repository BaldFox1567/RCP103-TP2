"""
main.py - Point d'entrée du simulateur.

Usage :
    python3 main.py test      # lance les tests de chaque classe
    python3 main.py trace     # génère une trace d'exemple (CSV) courte
    python3 main.py simu       # lance toutes les simulations -> results/
    python3 main.py all        # tout (test + trace + simu)   [defaut]
"""

import csv
import os
import sys

from engine import Engine
from config import SEND_MSG, RECV_MSG, MSG_DEPT
import theorie

# Dossiers (relatifs à la racine du projet, main.py étant dans src/)
ICI = os.path.dirname(os.path.abspath(__file__))
RACINE = os.path.dirname(ICI)
DOSSIER_RESULTATS = os.path.join(RACINE, "results")

# Plan d'expériences
LAMBDAS = [4, 6, 8, 12]
MU = 8
# Configurations : (nom, nb_serveurs, capacite)
MODELES = [
    ("M/M/1",   1, None),
    ("M/M/1/4", 1, 4),
    ("M/M/1/8", 1, 8),
    ("M/M/3/8", 3, 8),
]

# Durée de simulation et chauffe (justifiées dans le rapport)
DUREE = 100000.0    # T : horizon simulé (s)
RAMPE = 1000.0      # warm-up exclu des statistiques (s)
GRAINE = 12345


# ----------------------------------------------------------------------
#  1) Tests par classe
# ----------------------------------------------------------------------
def lancer_tests():
    print("=" * 60)
    print(" TESTS UNITAIRES PAR CLASSE (developpement incremental)")
    print("=" * 60)
    Engine.Test_Message()
    Engine.Test_Event()
    Engine.Test_Scheduler()
    Engine.Test_Client()
    Engine.Test_Queue()
    Engine.Test_Server()
    print("Tous les tests de classes sont passes.\n")


# ----------------------------------------------------------------------
#  2) Trace d'exemple (courte, pour le rapport)
# ----------------------------------------------------------------------
def generer_trace_exemple():
    print("Generation d'une trace d'exemple (M/M/1, lambda=4)...")
    chemin = os.path.join(DOSSIER_RESULTATS, "trace_exemple.csv")
    eng = Engine(lam=4, mu=MU, duree=10.0, nb_serveurs=1, capacite=None,
                 graine=7)
    eng.Run(chemin_trace=chemin, trace_max=20)
    # Vérification de l'ordre chronologique de la trace
    verifier_ordre_trace(chemin)
    print(f"  -> {chemin} (20 premieres lignes)\n")


def verifier_ordre_trace(chemin):
    with open(chemin, encoding="utf-8") as f:
        lignes = list(csv.DictReader(f))
    temps = [float(l["time"]) for l in lignes]
    assert temps == sorted(temps), "ERREUR : trace non chronologique !"
    print(f"  trace chronologique OK ({len(lignes)} lignes verifiees)")


# ----------------------------------------------------------------------
#  3) Toutes les simulations
# ----------------------------------------------------------------------
def lancer_simulations():
    print("=" * 60)
    print(" SIMULATIONS")
    print("=" * 60)
    lignes = []
    for nom, c, K in MODELES:
        for lam in LAMBDAS:
            eng = Engine(lam=lam, mu=MU, duree=DUREE, nb_serveurs=c,
                         capacite=K, graine=GRAINE)
            res = eng.Run(rampe=RAMPE)

            # Valeurs théoriques de référence
            th = valeurs_theoriques(nom, lam, MU, c, K)
            res.update(th)

            lignes.append(res)
            print(f"{nom:8s} lambda={lam:2d} | "
                  f"rho={res['rho']:.3f} | "
                  f"N_sys={res['N_systeme']:7.3f} | "
                  f"W_sys={res['W_systeme']:.4f} | "
                  f"rejet={res['taux_rejet']*100:5.2f}% | "
                  f"L_th={th.get('th_L', float('nan'))}")
    ecrire_resultats_csv(lignes)
    print(f"\nResultats ecrits dans {DOSSIER_RESULTATS}/resultats.csv")
    return lignes


def valeurs_theoriques(nom, lam, mu, c, K):
    """Retourne les valeurs théoriques (préfixées th_) pour le modèle."""
    if K is None:
        r = theorie.mm1(lam, mu)
        if r is None:
            return {"th_rho": lam / mu, "th_L": "inf", "th_W": "inf",
                    "th_Lq": "inf", "th_Wq": "inf", "th_rejet": 0.0}
        return {"th_rho": r["rho"], "th_L": round(r["L"], 4),
                "th_W": round(r["W"], 4), "th_Lq": round(r["Lq"], 4),
                "th_Wq": round(r["Wq"], 4), "th_rejet": 0.0}
    else:
        r = theorie.mmck(lam, mu, c, K)
        return {"th_rho": round(r["rho"], 4), "th_L": round(r["L"], 4),
                "th_W": round(r["W"], 4), "th_Lq": round(r["Lq"], 4),
                "th_Wq": round(r["Wq"], 4), "th_rejet": round(r["P_rejet"], 4)}


def ecrire_resultats_csv(lignes):
    chemin = os.path.join(DOSSIER_RESULTATS, "resultats.csv")
    colonnes = [
        "modele", "lambda", "mu", "serveurs", "capacite", "T", "rho",
        "arrivees", "acceptes", "rejets", "departs", "taux_rejet",
        "lambda_eff", "N_systeme", "N_file", "W_systeme", "W_file",
        "little_L", "little_Lq",
        "th_rho", "th_L", "th_Lq", "th_W", "th_Wq", "th_rejet",
    ]
    with open(chemin, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=colonnes)
        w.writeheader()
        for l in lignes:
            w.writerow({k: l.get(k, "") for k in colonnes})


# ----------------------------------------------------------------------
#  Programme principal
# ----------------------------------------------------------------------
def main():
    mode = sys.argv[1] if len(sys.argv) > 1 else "all"
    os.makedirs(DOSSIER_RESULTATS, exist_ok=True)

    if mode in ("test", "all"):
        lancer_tests()
    if mode in ("trace", "all"):
        generer_trace_exemple()
    if mode in ("simu", "all"):
        lancer_simulations()


if __name__ == "__main__":
    main()
