"""
theorie.py - Formules analytiques des files d'attente (pour comparaison).

On compare les résultats simulés aux valeurs théoriques :
  - M/M/1 (file infinie) : formules fermées classiques,
  - M/M/c/K (c serveurs, capacité K) : formule générale de naissance-mort,
    qui couvre aussi M/M/1/K (c=1).
"""

import math


def mm1(lam, mu):
    """M/M/1 file infinie. Retourne (rho, L, Lq, W, Wq) ou None si instable."""
    rho = lam / mu
    if rho >= 1.0:
        return None  # système instable : pas de régime stationnaire
    L = rho / (1 - rho)
    Lq = rho * rho / (1 - rho)
    W = 1.0 / (mu - lam)
    Wq = rho / (mu - lam)
    return {"rho": rho, "L": L, "Lq": Lq, "W": W, "Wq": Wq}


def mmck(lam, mu, c, K):
    """M/M/c/K : c serveurs, capacité totale K (file + service).

    Modèle de naissance-mort fini, toujours stable (états bornés).
    Retourne un dict avec rho (par serveur), L, Lq, W, Wq, P_rejet, lambda_eff.
    """
    a = lam / mu  # charge offerte (intensité de trafic)

    # Probabilités non normalisées des états n = 0..K
    termes = []
    for n in range(K + 1):
        if n < c:
            termes.append(a ** n / math.factorial(n))
        else:
            termes.append(a ** n / (math.factorial(c) * c ** (n - c)))
    somme = sum(termes)
    P = [t / somme for t in termes]  # distribution stationnaire

    P_rejet = P[K]                    # proba que le système soit plein
    lam_eff = lam * (1 - P_rejet)     # taux réellement accepté

    L = sum(n * P[n] for n in range(K + 1))
    # Serveurs occupés en moyenne = lam_eff / mu  ->  Lq = L - serveurs occupés
    Lq = L - lam_eff / mu
    W = L / lam_eff
    Wq = Lq / lam_eff
    rho = a / c                        # taux d'utilisation par serveur

    return {"rho": rho, "L": L, "Lq": Lq, "W": W, "Wq": Wq,
            "P_rejet": P_rejet, "lambda_eff": lam_eff}
