# RCP103 / NCA — TP Simulateur à événements discrets

Travaux pratiques du Cours 09 (P. B. Velloso & A. Hidouri, CNAM).
Modèle d'une passerelle IoT : files d'attente M/M/1, M/M/1/K, M/M/c/K.

## Organisation

```
RCP103-TP2/
├── java/                  # implémentation Java, par séance
│   ├── seance1/           # Message, Event, Scheduler, Engine (main+trace+tests)
│   │   ├── src/
│   │   └── README.md
│   ├── seance2/           # = S1 + Client, Queue, Server + tests (cumulatif)
│   │   ├── src/
│   │   └── README.md
│   └── seance3/           # = S2 + Gateway, Engine (boucle), Theorie + simulations
│       ├── src/           # dossier autonome (copies des classes précédentes)
│       ├── results/       # tableau de métriques (resultats.csv)
│       ├── figures/       # graphiques éventuels
│       └── README.md
│
├── docs/                  # documents pédagogiques
│   ├── Seance1_Pedagogique.docx
│   └── Seance2-3_Pedagogique.docx
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
javac -encoding UTF-8 src/*.java
java -cp src Engine              # tests + campagne de simulations
```

**Java — Séances 1 et 2 (autonomes)**
```bash
cd java/seance1/src   # ou seance2/src
javac -encoding UTF-8 *.java
java Engine
```

## Avancement

| Séance | Classes | État |
|:------:|---------|:----:|
| 1 | Message, Event, Scheduler, Engine (main + trace + tests) | ✅ |
| 2 | Client, Queue, Server (+ tests) | ✅ |
| 3 | Gateway, Engine (boucle) + simulations M/M/1, M/M/1/K, M/M/c/K | ✅ |

Les **trois séances Java sont réalisées** : le simulateur est complet et ses
résultats concordent avec la théorie (accord < 1 %, loi de Little vérifiée).
