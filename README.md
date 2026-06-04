# RCP103 / NCA — TP Simulateur à événements discrets

Travaux pratiques du Cours 09 (P. B. Velloso & A. Hidouri, CNAM).
Modèle d'une passerelle IoT : files d'attente M/M/1, M/M/1/K, M/M/c/K.

Le dépôt sépare clairement le **Java** (réalisé séance par séance) du **Python**
(version complète de référence), et les **séances** entre elles.

## Organisation

```
RCP103-TP2/
├── java/                  # implémentation Java, par séance (cumulative)
│   ├── seance1/           # Message, Event, Scheduler + tests
│   │   ├── src/
│   │   └── README.md
│   └── seance2/           # = S1 + Client, Queue, Server + tests
│       ├── src/
│       └── README.md
│
├── python/                # projet Python complet (TP de référence)
│   ├── src/               # une classe par fichier + main + figures
│   ├── results/           # traces CSV + tableau de métriques
│   ├── figures/           # graphiques PNG
│   ├── report/            # rapport LaTeX + PDF compilé
│   └── README.md
│
├── docs/                  # documents pédagogiques
│   ├── Seance1_Pedagogique.docx
│   └── make_doc.py        # script de génération du .docx
│
└── README.md              # ce fichier
```

> **Séances cumulatives** : chaque dossier `java/seanceN/` est autonome et
> compilable seul. La séance 2 reprend les classes de la séance 1 (inchangées)
> et y ajoute les nouvelles. On voit ainsi l'avancement séance par séance.

## Démarrage rapide

**Java — Séance courante (2)**
```bash
cd java/seance2/src
javac -encoding UTF-8 *.java
java -ea Main
```

**Python — projet complet**
```bash
cd python/src
python3 main.py all      # tests + trace + simulations
python3 figures.py       # graphiques
```

## Avancement

| Séance | Classes | État |
|:------:|---------|:----:|
| 1 | Message, Event, Scheduler (+ Main/trace/tests) | ✅ |
| 2 | Client, Queue, Server (+ tests) | ✅ |
| 3 | Engine, Gateway + simulations M/M/1, M/M/1/K, M/M/c/K | à faire (Java) |

La version **Python** couvre déjà l'intégralité du TP (toutes les séances et les
simulations), elle sert de référence ; la version **Java** est construite
progressivement séance par séance.
