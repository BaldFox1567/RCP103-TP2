# TP Simulateur — Séance 1 (Java)

RCP103 / NCA — Cours 09 : Simulateur à événements discrets  
P. B. Velloso & A. Hidouri

## Structure

```
tp_simulateur_java/
├── src/
│   ├── EventType.java   # enum : SEND_MSG, RECV_MSG, MSG_DEPT
│   ├── Message.java     # message IoT (messageID, source, destination, timestamp)
│   ├── Event.java       # événement daté (eventID, eventType, eventTime, message)
│   ├── Scheduler.java   # ordonnanceur (PriorityQueue / tas binaire, sans sort())
│   └── Main.java        # testMessage(), testEvent(), testScheduler(), generateTrace()
└── README.md
```

## Compiler et lancer

```bash
cd src

# Compilation (une commande compile toutes les classes)
javac -encoding UTF-8 *.java

# Exécution avec assertions activées (-ea)
java -ea Main
```

## Ce qui est testé (Séance 1)

| Test | Ce qu'il vérifie |
|------|-----------------|
| `testMessage()` | création, getters, `setTimestamp`, `printMessage` |
| `testEvent()` | constructeur, `setEventType`, `setEventTime`, `printEvent`, trace CSV |
| `testScheduler()` | insertion de 4 événements dans le désordre → extraction chronologique |

## Choix d'implémentation : Scheduler

Le sujet interdit `sort()` à chaque insertion. On utilise `java.util.PriorityQueue`
(tas binaire min-heap) :
- `addEvent` : O(log n) — le tas se réorganise automatiquement ;
- `getEvent` : O(log n) — extrait toujours l'événement le plus proche.

L'ordre est défini par `Event.compareTo()` : chronologique, puis par `eventID`
(départage stable en cas d'égalité de temps).
