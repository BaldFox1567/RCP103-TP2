"""
figures.py - Génère les graphiques PNG à partir de results/resultats.csv.

Usage : python3 figures.py   (après avoir lancé main.py simu)
"""

import csv
import os

import matplotlib
matplotlib.use("Agg")  # backend sans affichage (sauvegarde fichier)
import matplotlib.pyplot as plt

ICI = os.path.dirname(os.path.abspath(__file__))
RACINE = os.path.dirname(ICI)
DOSSIER_RESULTATS = os.path.join(RACINE, "results")
DOSSIER_FIGURES = os.path.join(RACINE, "figures")


def charger():
    """Charge les résultats en un dict {(modele, lambda): ligne}."""
    chemin = os.path.join(DOSSIER_RESULTATS, "resultats.csv")
    data = {}
    with open(chemin, encoding="utf-8") as f:
        for l in csv.DictReader(f):
            data[(l["modele"], int(l["lambda"]))] = l
    return data


def _f(x):
    """Convertit en float, en gérant 'inf' et les chaînes vides."""
    try:
        return float(x)
    except (ValueError, TypeError):
        return float("inf")


LAMBDAS = [4, 6, 8, 12]
MODELES = ["M/M/1", "M/M/1/4", "M/M/1/8", "M/M/3/8"]


def fig_mm1_vs_theorie(data):
    """M/M/1 : N et W simulés vs théoriques (points stables seulement)."""
    lam_stb = [4, 6]  # seuls les cas stables ont une théorie finie
    N_sim = [_f(data[("M/M/1", l)]["N_systeme"]) for l in lam_stb]
    N_th = [_f(data[("M/M/1", l)]["th_L"]) for l in lam_stb]
    W_sim = [_f(data[("M/M/1", l)]["W_systeme"]) for l in lam_stb]
    W_th = [_f(data[("M/M/1", l)]["th_W"]) for l in lam_stb]

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(10, 4))

    ax1.plot(lam_stb, N_th, "o-", label="Théorie", color="C0")
    ax1.plot(lam_stb, N_sim, "s--", label="Simulation", color="C1")
    ax1.set_xlabel("λ (msg/s)")
    ax1.set_ylabel("N̄ système")
    ax1.set_title("M/M/1 — nombre moyen dans le système")
    ax1.set_xticks(lam_stb)
    ax1.legend()
    ax1.grid(True, alpha=0.3)

    ax2.plot(lam_stb, W_th, "o-", label="Théorie", color="C0")
    ax2.plot(lam_stb, W_sim, "s--", label="Simulation", color="C1")
    ax2.set_xlabel("λ (msg/s)")
    ax2.set_ylabel("W̄ système (s)")
    ax2.set_title("M/M/1 — temps moyen dans le système")
    ax2.set_xticks(lam_stb)
    ax2.legend()
    ax2.grid(True, alpha=0.3)

    fig.tight_layout()
    chemin = os.path.join(DOSSIER_FIGURES, "mm1_sim_vs_theorie.png")
    fig.savefig(chemin, dpi=130)
    plt.close(fig)
    print("  ->", chemin)


def fig_rejets(data):
    """Taux de rejet vs λ pour les modèles bornés."""
    fig, ax = plt.subplots(figsize=(7, 4.5))
    for m in ["M/M/1/4", "M/M/1/8", "M/M/3/8"]:
        y = [_f(data[(m, l)]["taux_rejet"]) * 100 for l in LAMBDAS]
        ax.plot(LAMBDAS, y, "o-", label=m)
    ax.set_xlabel("λ (msg/s)")
    ax.set_ylabel("Taux de rejet (%)")
    ax.set_title("Taux de rejet en fonction de λ (modèles bornés)")
    ax.set_xticks(LAMBDAS)
    ax.legend()
    ax.grid(True, alpha=0.3)
    fig.tight_layout()
    chemin = os.path.join(DOSSIER_FIGURES, "taux_rejet.png")
    fig.savefig(chemin, dpi=130)
    plt.close(fig)
    print("  ->", chemin)


def fig_comparaison_modeles(data):
    """Comparaison des 4 modèles : N̄ système et W̄ système vs λ.

    M/M/1 instable (λ=8,12) tronqué pour rester lisible.
    """
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(11, 4.5))

    for m in MODELES:
        N = [_f(data[(m, l)]["N_systeme"]) for l in LAMBDAS]
        W = [_f(data[(m, l)]["W_systeme"]) for l in LAMBDAS]
        ax1.plot(LAMBDAS, N, "o-", label=m)
        ax2.plot(LAMBDAS, W, "o-", label=m)

    ax1.set_xlabel("λ (msg/s)")
    ax1.set_ylabel("N̄ système")
    ax1.set_title("Nombre moyen dans le système")
    ax1.set_ylim(0, 8)  # tronque l'explosion M/M/1 instable
    ax1.set_xticks(LAMBDAS)
    ax1.legend()
    ax1.grid(True, alpha=0.3)

    ax2.set_xlabel("λ (msg/s)")
    ax2.set_ylabel("W̄ système (s)")
    ax2.set_title("Temps moyen dans le système")
    ax2.set_ylim(0, 1.2)
    ax2.set_xticks(LAMBDAS)
    ax2.legend()
    ax2.grid(True, alpha=0.3)

    fig.suptitle("Comparaison des 4 modèles (M/M/1 instable tronqué)")
    fig.tight_layout()
    chemin = os.path.join(DOSSIER_FIGURES, "comparaison_modeles.png")
    fig.savefig(chemin, dpi=130)
    plt.close(fig)
    print("  ->", chemin)


def fig_mm1_LW(data):
    """M/M/1 : L et W simulés vs λ avec courbes théoriques continues."""
    import numpy as np
    mu = 8
    lam_cont = np.linspace(0.5, 7.5, 200)
    L_cont = (lam_cont / mu) / (1 - lam_cont / mu)
    W_cont = 1.0 / (mu - lam_cont)

    lam_stb = [4, 6]
    N_sim = [_f(data[("M/M/1", l)]["N_systeme"]) for l in lam_stb]
    W_sim = [_f(data[("M/M/1", l)]["W_systeme"]) for l in lam_stb]

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(10, 4))
    ax1.plot(lam_cont, L_cont, "-", color="C0", label="L théorique")
    ax1.plot(lam_stb, N_sim, "s", color="C1", markersize=9, label="Simulation")
    ax1.axvline(8, color="red", ls=":", label="λ=µ (instable)")
    ax1.set_xlabel("λ (msg/s)"); ax1.set_ylabel("L")
    ax1.set_title("M/M/1 — L = ρ/(1−ρ)")
    ax1.set_ylim(0, 10); ax1.legend(); ax1.grid(True, alpha=0.3)

    ax2.plot(lam_cont, W_cont, "-", color="C0", label="W théorique")
    ax2.plot(lam_stb, W_sim, "s", color="C1", markersize=9, label="Simulation")
    ax2.axvline(8, color="red", ls=":", label="λ=µ (instable)")
    ax2.set_xlabel("λ (msg/s)"); ax2.set_ylabel("W (s)")
    ax2.set_title("M/M/1 — W = 1/(µ−λ)")
    ax2.set_ylim(0, 1.5); ax2.legend(); ax2.grid(True, alpha=0.3)

    fig.tight_layout()
    chemin = os.path.join(DOSSIER_FIGURES, "mm1_LW_lambda.png")
    fig.savefig(chemin, dpi=130)
    plt.close(fig)
    print("  ->", chemin)


def main():
    os.makedirs(DOSSIER_FIGURES, exist_ok=True)
    data = charger()
    print("Generation des figures...")
    fig_mm1_vs_theorie(data)
    fig_mm1_LW(data)
    fig_rejets(data)
    fig_comparaison_modeles(data)
    print("Figures generees dans", DOSSIER_FIGURES)


if __name__ == "__main__":
    main()
