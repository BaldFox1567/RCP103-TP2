# TP — Simulateur à événements discrets (RCP103 / NCA, CNAM)

Simulateur de files d'attente (M/M/1, M/M/1/K, M/M/c/K) modélisant une
passerelle IoT, écrit en Python orienté objet (une classe par fichier).
Cours 09 — *Simulateur* — P. B. Velloso & A. Hidouri.

## Structure

```
tp_simulateur/
├── src/            # code source (une classe = un fichier)
│   ├── config.py        # constantes (types d'événements, délai de propagation…)
│   ├── message.py       # Message
│   ├── event.py         # Event
│   ├── scheduler.py     # Scheduler (tas binaire, pas de sort())
│   ├── client.py        # Client (source de messages)
│   ├── file_attente.py  # Queue (FIFO)
│   ├── server.py        # Server
│   ├── gateway.py       # Gateway (file + serveurs + capacité K)
│   ├── engine.py        # Engine : boucle Run(), trace, métriques, tests
│   ├── theorie.py       # formules M/M/1 et M/M/c/K (comparaison)
│   ├── main.py          # point d'entrée (tests + simulations)
│   └── figures.py       # génération des graphiques
├── results/        # trace CSV + tableau de métriques (resultats.csv)
├── figures/        # graphiques PNG
├── report/         # rapport LaTeX + PDF compilé
└── README.md
```

## Prérequis

- Python 3 avec **numpy** et **matplotlib**.

```bash
pip install numpy matplotlib
```

## Reproduire les résultats

Toutes les commandes se lancent depuis `src/`.

```bash
cd src

# 1) Tests unitaires de chaque classe (développement incrémental)
python3 main.py test

# 2) Trace d'exemple courte -> results/trace_exemple.csv (ordre vérifié)
python3 main.py trace

# 3) Toutes les simulations -> results/resultats.csv
python3 main.py simu     # ~1 min 30

# (ou tout d'un coup)
python3 main.py all

# 4) Graphiques -> figures/*.png  (après l'étape 3)
python3 figures.py
```

## Paramètres

Définis en haut de `src/main.py` :

- `LAMBDAS = [4, 6, 8, 12]`, `MU = 8` (taux en msg/s) ;
- `MODELES` : M/M/1, M/M/1/4, M/M/1/8, M/M/3/8 ;
- `DUREE = 100000` s, `RAMPE = 1000` s (chauffe exclue des stats) ;
- `GRAINE = 12345` (reproductibilité).

Le délai de propagation est dans `src/config.py`
(`DELAI_PROPAGATION = 1.0` s, paramétrable).

## Résultats principaux

Les mesures simulées coïncident avec la théorie à mieux qu'un pour-cent et la
loi de Little (`L = λ·W`) est vérifiée sur tous les runs. Détails et analyse
dans `report/rapport.pdf`.

## Compiler le rapport

```bash
cd report
pdflatex rapport.tex   # (ou : tectonic rapport.tex)
```

Le PDF compilé est déjà fourni : `report/rapport.pdf`.
