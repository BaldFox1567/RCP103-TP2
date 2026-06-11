# Séance 3 (Java) — Gateway, Engine + simulations

RCP103 / NCA — Cours 09 : Simulateur à événements discrets
P. B. Velloso & A. Hidouri

Séance d'assemblage : la passerelle (Gateway) relie la file et les serveurs,
et l'Engine exécute la boucle de simulation complète.

## Contenu (`src/`) — nouveautés

| Fichier | Rôle |
|---------|------|
| `Gateway.java` | la passerelle (= le système) : file + serveurs + capacité K (0 = infinie) ; état (`GetNbInSystem`, `IsFull`, `GetFreeServer`…) et compteurs (arrivés, acceptés, rejetés, sortis) |
| `Engine.java` | classe principale : `main`, `CreateClients()`, boucle `Run()` (SEND → RECV → DEPT), `GenerateTrace()` + export CSV, métriques, `Test_Gateway`, `Test_Engine`, `Simulations()` |
| `Theorie.java` | formules M/M/1 et M/M/c/K pour la comparaison |

(+ copies inchangées des séances 1 et 2 : `Message`, `Event`, `Scheduler`,
`Client`, `Queue`, `Server` — le dossier est autonome.)

## Logique des événements (modèle du cours)

- **SEND_MSG** : le client envoie ; le message met 1 s à arriver (RECV_MSG
  programmé à t+1) et le prochain envoi est programmé à t + inter-arrivée ;
- **RECV_MSG** : système plein → rejet ; serveur libre → service (MSG_DEPT
  programmé à t + temps de service) ; sinon → mise en file ;
- **MSG_DEPT** : sortie du message ; si la file n'est pas vide, le serveur
  enchaîne avec le suivant, sinon il se libère.

Après chaque événement, une ligne de trace est générée (mêmes six champs que
la séance 1), stockée en mémoire puis affichée/exportée.

## Compiler et lancer

Depuis ce dossier (`java/seance3/`) :

```bash
javac -encoding UTF-8 src/*.java
java -cp src Engine
```

L'exécution enchaîne `Test_Gateway`, `Test_Engine` (simulation courte M/M/1
avec trace → `trace.csv`, métriques, vérification de la loi de Little) puis la
campagne `Simulations()` → `results/resultats.csv`.

## Modèles simulés

λ ∈ {4, 6, 8, 12} msg/s, µ = 8 msg/s, T = 100 000 s :

| Modèle | Serveurs | Capacité K |
|--------|:--------:|:----------:|
| M/M/1   | 1 | ∞ |
| M/M/1/4 | 1 | 4 |
| M/M/1/8 | 1 | 8 |
| M/M/3/8 | 3 | 8 |

Le temps de séjour W est mesuré de l'arrivée à la passerelle (`arrivedTime`)
au départ : le délai de propagation (1 s, constant) n'y entre pas, ce qui
permet la comparaison directe avec la théorie.

## Résultats obtenus (extrait)

Accord simulation/théorie à mieux que 1 %, loi de Little vérifiée :

| Modèle | λ | N_sim | L_th | W_sim | W_th | rejet sim/th |
|--------|---|------:|-----:|------:|-----:|:---:|
| M/M/1   | 4 | 1.000 | 1.000 | 0.250 | 0.250 | — |
| M/M/1   | 6 | 3.025 | 3.000 | 0.503 | 0.500 | — |
| M/M/1   | 8 | — | ∞ | — | ∞ | *(instable, ρ=1)* |
| M/M/1/4 | 8 | 2.001 | 2.000 | 0.313 | 0.313 | 19.9 % / 20.0 % |
| M/M/1/8 | 12 | 6.236 | 6.240 | 0.790 | 0.791 | 34.2 % / 34.2 % |
| M/M/3/8 | 12 | 1.704 | 1.706 | 0.143 | 0.143 | 0.37 % / 0.37 % |

À λ égal, M/M/3/8 rejette nettement moins que les mono-serveurs (capacité de
traitement triplée) et le temps d'attente y est quasi nul.
