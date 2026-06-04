# Séance 2 (Java) — Client, Queue, Server

RCP103 / NCA — Cours 09 : Simulateur à événements discrets
P. B. Velloso & A. Hidouri

Projet **cumulatif** : ce dossier contient toutes les classes de la Séance 1
(inchangées) **plus** les trois nouvelles classes de la Séance 2. Il compile et
s'exécute donc tout seul.

## Contenu (`src/`)

| Fichier | Séance | Rôle |
|---------|:------:|------|
| `EventType.java`, `Message.java`, `Event.java`, `Scheduler.java` | S1 | briques de base (rappel) |
| `Client.java` | **S2** | source de messages ; inter-arrivées exp. de moyenne 1/λ |
| `Queue.java` | **S2** | file d'attente FIFO (`ArrayDeque`) |
| `Server.java` | **S2** | serveur ; temps de service exp. de moyenne 1/μ |
| `Main.java` | **S2** | rappel S1 + `testClient()`, `testQueue()`, `testServer()` |

## Compiler et lancer

```bash
cd src
javac -encoding UTF-8 *.java
java -ea Main
```

## Ce que les tests vérifient

- **Client** : IDs de messages incrémentaux ; sur 200 000 tirages, la moyenne des
  inter-arrivées ≈ 1/λ = 0,25 s (λ=4). Confirme que λ est bien un **taux**.
- **Queue** : ordre FIFO (premier entré, premier sorti), `size`, `isEmpty`.
- **Server** : états libre/occupé ; moyenne des temps de service ≈ 1/μ = 0,125 s (μ=8).

## Note sur l'aléatoire

Pas de numpy en Java : les lois exponentielles sont générées par transformée
inverse `-ln(1-U)/taux` (moyenne = 1/taux), avec `java.util.Random` graine fixée
pour la reproductibilité.

## À venir (Séance 3)

Classes `Gateway` et `Engine`, boucle de simulation complète et mesures M/M/1,
M/M/1/K, M/M/c/K (déjà réalisées dans la version Python, voir `../../python/`).
