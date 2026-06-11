# Séance 1 (Java) — Message, Event, Scheduler, Engine

RCP103 / NCA — Cours 09 : Simulateur à événements discrets
P. B. Velloso & A. Hidouri

Briques de base du simulateur. À ce stade, le simulateur ne tourne pas encore :
l'objectif est de valider les structures de base et leur enchaînement.

## Contenu (`src/`) — quatre fichiers, une classe par fichier

| Fichier | Rôle |
|---------|------|
| `Message.java` | l'entité qui circule : `messageID`, `source`, `destination`, `sendTime`, `arrivedTime` ; `PrintMessage()`, `serviceStart()` |
| `Event.java` | événement daté : `eventID`, `eventType`, `eventTime`, `message` ; types en constantes (`SEND_MSG`, `RECV_MSG`, `MSG_DEPT`) validés par `IsValidEventType()` |
| `Scheduler.java` | liste d'événements triée par date : insertion ordonnée directe dans `AddEvent()` (aucun `sort()`), `GetEvent()`, `HasEvents()`, `PrintScheduler()` |
| `Engine.java` | classe principale : `main`, durée de simulation, scheduler, trace en mémoire, `GenerateTrace()`, les quatre tests et `Run()` |

## Compiler et lancer

```bash
cd src
javac -encoding UTF-8 *.java
java Engine
```

`Run()` enchaîne `Test_Message`, `Test_Event`, `Test_Scheduler` (trois SEND_MSG
aux dates 1.202, 2.320, 0.950 insérés dans le désordre, ressortis triés) et
`Test_Trace` (2 messages, 6 événements mélangés → trace console + `trace.csv`).

## Gestion de la trace

La trace est **stockée en mémoire** puis réutilisée pour deux sorties :
affichage console et export CSV. Six champs : `time, node, event, src, dst,
msgID`. Le nœud concerné : la source pour un `SEND_MSG` (le client), sinon la
destination, c'est-à-dire la passerelle (nœud 0).

## Choix d'implémentation : Scheduler

Insertion ordonnée directe : événement nul ignoré ; liste vide → ajout simple ;
sinon on parcourt jusqu'au premier événement plus tardif et on insère juste
avant ; à défaut, ajout en fin de liste. La liste reste triée en permanence,
sans `sort()` et sans rendre `Event` comparable.
