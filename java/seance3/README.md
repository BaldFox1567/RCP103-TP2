# Séance 3 (Java) — Engine, Gateway + simulations

RCP103 / NCA — Cours 09 : Simulateur à événements discrets
P. B. Velloso & A. Hidouri

Séance d'assemblage : on relie les briques des Séances 1 et 2 dans un moteur
complet, et on lance réellement les simulations des 4 modèles.

## Contenu (`src/`) — uniquement les nouveautés S3

| Fichier | Rôle |
|---------|------|
| `Gateway.java` | la passerelle (= le système) : file + serveurs + capacité K ; logique RECV/DEPT (service / mise en file / rejet / service du suivant) |
| `Engine.java` | moteur : `createClients`, `run` (boucle à événements), `generateTrace`, calcul des métriques |
| `Theorie.java` | formules M/M/1 et M/M/c/K (comparaison) |
| `Main.java` | `testGateway`, `testEngine`, campagne de simulations + écriture CSV |

Les classes des Séances 1 et 2 (`Message`, `Event`, `EventType`, `Scheduler`,
`Client`, `Queue`, `Server`) **ne sont pas recopiées** ici.

## Réutilisation des Séances 1 et 2 (choix de build)

On réutilise les classes précédentes via le **`-sourcepath` de `javac`**, qui
pointe sur `../seance2/src` (lequel contient déjà, cumulativement, toutes les
classes des Séances 1 et 2). `javac` ne compile que les classes réellement
utilisées et les place dans `build/`. Avantages : aucune duplication de code,
aucune modification des séances précédentes, séances restées séparées.

> Toutes les classes sont dans le *package par défaut* (comme aux séances 1-2),
> d'où l'usage de `-sourcepath` plutôt que d'un package nommé.

## Compiler et lancer

Depuis ce dossier (`java/seance3/`) :

```bash
javac -encoding UTF-8 -sourcepath ../seance2/src -d build src/*.java
java -ea -cp build Main
```

`-ea` active les assertions des tests. L'exécution lance les tests puis la
campagne complète (~3 s) et écrit les résultats dans `results/`.

## Sorties (`results/`)

| Fichier | Contenu |
|---------|---------|
| `resultats.csv` | tableau des métriques des 16 runs + valeurs théoriques |
| `trace_exemple.csv` | trace courte (20 lignes), ordre chronologique vérifié |
| `trace_test.csv` | trace générée par `testEngine` |

`figures/` est prévu pour d'éventuels PNG (non générés en Java ; voir la chaîne
Python `../../python/` qui produit les graphiques avec matplotlib).

## Modèles simulés

λ ∈ {4, 6, 8, 12} msg/s, µ = 8 msg/s, horizon **T = 100 000 s** (assez long pour
converger : 0,4 à 1,2 million d'arrivées par run ; l'accord avec la théorie le
confirme). Délai de propagation SEND→RECV = **1,0 s** (paramétrable, cf.
`Engine.DELAI_PROPAGATION_DEFAUT`). Comme ce délai est constant, il décale les
arrivées mais n'entre pas dans W (mesuré de l'arrivée à la passerelle au départ),
qui se compare donc directement à la théorie M/M/c.

| Modèle | Serveurs | Capacité K |
|--------|:--------:|:----------:|
| M/M/1   | 1 | ∞ |
| M/M/1/4 | 1 | 4 |
| M/M/1/8 | 1 | 8 |
| M/M/3/8 | 3 | 8 |

## Résultats obtenus (extrait)

Accord simulation/théorie à mieux qu'un pour-cent, loi de Little vérifiée
(`little_L ≈ N_systeme`) :

| Modèle | λ | N_sim | L_th | W_sim | W_th | rejet sim/th |
|--------|---|------:|-----:|------:|-----:|:---:|
| M/M/1   | 4 | 0.989 | 1.000 | 0.248 | 0.250 | — |
| M/M/1   | 6 | 2.990 | 3.000 | 0.498 | 0.500 | — |
| M/M/1   | 8 | 466 | ∞ | 58.3 | ∞ | — *(instable, ρ=1)* |
| M/M/1/4 | 8 | 2.000 | 2.000 | 0.313 | 0.313 | 19.9 % / 20.0 % |
| M/M/1/8 | 12 | 6.230 | 6.240 | 0.788 | 0.791 | 34.0 % / 34.2 % |
| M/M/3/8 | 12 | 1.699 | 1.706 | 0.142 | 0.143 | 0.37 % / 0.37 % |

À λ égal, M/M/3/8 rejette nettement moins que les mono-serveur (capacité de
traitement triplée), avec un temps d'attente quasi nul.
