# Séance 2 (Java) — Client, Queue, Server

RCP103 / NCA — Cours 09 : Simulateur à événements discrets
P. B. Velloso & A. Hidouri

Séance cumulative : reprend les classes de la séance 1 (inchangées) et ajoute
les trois composants du système. Le simulateur ne tourne toujours pas : la
boucle viendra à la séance 3.

## Contenu (`src/`) — nouveautés

| Fichier | Rôle |
|---------|------|
| `Client.java` | générateur de messages : `clientID`, `destination`, `lambda` ; inter-arrivées exponentielles `GetInterArrivalTime()` (moyenne 1/λ), `GenerateMessage()` |
| `Queue.java` | file FIFO de la passerelle : deux constructeurs (`Queue()` = capacité infinie, `Queue(capacity)` = bornée, pour les tests de la séance 3), compteurs `nbArrivals`/`nbRejected` et taux de rejet `GetRejectionRate()` (rejetées/présentées, nul si capacité infinie), `AddMessage()`, `GetMessage()`, `IsEmpty()`, `IsFull()`, `GetSize()`, `PrintQueue()` |
| `Server.java` | serveur de traitement : `serverID`, `mu`, `busy` ; temps de service exponentiel `GetServiceTime()` (moyenne 1/µ) |
| `Engine.java` | classe principale : trace + tests des séances 1 et 2 ; `Run()` appelle `Test_Client`, `Test_Queue`, `Test_Server` |

(+ copies inchangées de `Message.java`, `Event.java`, `Scheduler.java`.)

## Compiler et lancer

```bash
cd src
javac -encoding UTF-8 *.java
java Engine
```

## Remarque sur λ et µ

λ et µ sont des **taux** (messages/s) : l'inter-arrivée moyenne vaut 1/λ et le
temps de service moyen 1/µ (transformée inverse : `-ln(U)/taux`). Les tests
vérifient ces moyennes sur un grand échantillon.
