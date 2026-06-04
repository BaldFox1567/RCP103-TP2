# Séance 1 (Java) — Message, Event, Scheduler

RCP103 / NCA — Cours 09 : Simulateur à événements discrets
P. B. Velloso & A. Hidouri

Briques de base du simulateur : un message, un événement, et l'ordonnanceur.

## Contenu (`src/`)

| Fichier | Rôle |
|---------|------|
| `EventType.java` | enum : `SEND_MSG`, `RECV_MSG`, `MSG_DEPT` |
| `Message.java` | message IoT (`messageID`, `source`, `destination`, `timestamp`) |
| `Event.java` | événement daté (`eventID`, `eventType`, `eventTime`, `message`) — `Comparable` |
| `Scheduler.java` | ordonnanceur (`PriorityQueue` / tas binaire, sans `sort()`) |
| `Main.java` | `testMessage()`, `testEvent()`, `testScheduler()`, `generateTrace()` |

## Compiler et lancer

```bash
cd src
javac -encoding UTF-8 *.java
java -ea Main          # -ea active les assertions des tests
```

## Choix d'implémentation : Scheduler

Le sujet interdit `sort()` à chaque insertion. On utilise `java.util.PriorityQueue`
(tas binaire) : `addEvent` et `getEvent` en O(log n), ordre chronologique garanti
par `Event.compareTo()` (par temps, puis par `eventID`).
