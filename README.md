# RCP103 / NCA — TP Simulateur à événements discrets

Travaux pratiques du Cours 09 (P. B. Velloso & A. Hidouri, CNAM).
Modèle d'une passerelle IoT : files d'attente M/M/1, M/M/1/K, M/M/c/K.

Le dépôt sépare clairement le **Java** (réalisé séance par séance) du **Python**
(version complète de référence), et les **séances** entre elles.

## Organisation

```
RCP103-TP2/
├── java/                  # implémentation Java, par séance
│   ├── seance1/           # Message, Event, Scheduler + tests
│   │   ├── src/
│   │   └── README.md
│   ├── seance2/           # = S1 + Client, Queue, Server + tests (cumulatif)
│   │   ├── src/
│   │   └── README.md
│   └── seance3/           # Gateway, Engine, Theorie + Main de simulation
│       ├── src/           # réutilise S1/S2 via -sourcepath (pas de copie)
│       ├── results/       # traces CSV + tableau de métriques
│       ├── figures/       # graphiques éventuels
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

**Java — Séance courante (3 : simulateur complet)**
```bash
cd java/seance3
javac -encoding UTF-8 -sourcepath ../seance2/src -d build src/*.java
java -ea -cp build Main          # tests + campagne de simulations
```

**Java — Séances 1 et 2 (autonomes)**
```bash
cd java/seance1/src   # ou seance2/src
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
| 3 | Engine, Gateway + simulations M/M/1, M/M/1/K, M/M/c/K | ✅ |

Les **trois séances Java sont réalisées** : le simulateur est complet et ses
résultats concordent avec la théorie (accord < 1 %, loi de Little vérifiée). La
version **Python** couvre le même périmètre et sert de référence croisée
(notamment pour les graphiques et le rapport).
