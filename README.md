<div align="center">

# CoRi7la 🚗

### Un trajet, plusieurs arrêts, des places à partager.

API REST de covoiturage développée avec **Java et Spring Boot**, dans le cadre d’un brief YouCode.

![Java 17](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.2-6DB33F?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square)
![MapStruct](https://img.shields.io/badge/MapStruct-1.6.3-blue?style=flat-square)

[Installation](#installation) · [Fonctionnalités](#fonctionnalités) · [API](#api) · [Tests](#tests-unitaires) · [Jira](https://edu-team-m91pe19p.atlassian.net/jira/software/projects/CORI/boards/69)

</div>

---

## Présentation

CoRi7la permet à un conducteur de publier un trajet avec son véhicule et un parcours composé de points d’arrêt ordonnés. Un passager recherche un segment de ce parcours, réserve des places et consulte ses réservations.

Chaque arrêt contient une ville, une heure de passage et un prix cumulé depuis le départ. Le premier arrêt définit la ville de départ et le dernier la ville d’arrivée.

> **Branche de travail : `develop`.** Les fonctionnalités sont réalisées sur des branches `feature/*`, puis intégrées dans `develop`. La branche `main` est destinée à la livraison finale.

## Fonctionnalités

| Domaine | Fonctions implémentées |
|---|---|
| Conducteurs | Création, détail, modification et recherche par nom ou email |
| Passagers | Création, détail, modification et recherche par email |
| Véhicules | Ajout, détail, modification, liste par conducteur et suppression conditionnelle |
| Trajets | Publication multi-arrêts, détail avec places restantes, annulation et terminaison |
| Recherche | Villes dans le bon ordre, date de départ, places demandées, prix maximal par place et pagination |
| Réservations | Réservation entre deux arrêts, calcul du montant, historique passager et liste côté conducteur |
| Annulations | Délai de 24 heures pour le passager, réouverture d’un trajet complet et annulation des réservations lors de l’annulation du trajet |
| Erreurs | Réponses `ProblemDetail`, validation des entrées et journalisation SLF4J |

**État des vérifications :** 34 tests unitaires des services trajet et réservation ont été exécutés avec succès. La collection Postman manuelle est préparée ; la validation HTTP complète des dernières fonctionnalités reste à effectuer.

## Stack technique

| Outil | Rôle |
|---|---|
| Java 17 / Spring Boot 4.1.2 | Application et API REST Spring MVC |
| Spring Data JPA / Hibernate | Persistance et requêtes JPQL |
| PostgreSQL 16 | Base de données relationnelle |
| Jakarta Bean Validation | Validation des DTO de requête |
| MapStruct 1.6.3 | Conversion des entités en DTO |
| Maven Wrapper | Compilation et exécution des tests |
| Docker Compose | Démarrage de PostgreSQL |
| JUnit / Mockito / AssertJ | Tests unitaires des services |
| Postman | Requêtes de démonstration et vérification HTTP |

## Installation

### Prérequis

- JDK **17**, avec `java` et `javac` accessibles.
- **Git**.
- **Docker Desktop** démarré, avec Docker Compose.
- **Postman** pour essayer l’API.

Maven est fourni par le wrapper du dépôt.

### 1. Récupérer le projet

```bash
git clone --branch develop https://github.com/Bouchraelqartit/CoRi7la.git
cd CoRi7la
```

### 2. Démarrer PostgreSQL

```bash
docker compose up -d db
docker compose ps
```

| Paramètre local | Valeur |
|---|---|
| Hôte PostgreSQL | `localhost` |
| Port sur le poste | `5434` |
| Port dans le conteneur | `5432` |
| Base / utilisateur | `cori7la` |
| Mot de passe de développement | `cori7la_dev` |

Les données sont conservées dans un volume Docker nommé par Compose à partir de `cori7la_db`.

La configuration se trouve dans `src/main/resources/application.yaml`. L’URL JDBC est `jdbc:postgresql://localhost:5434/cori7la`. Hibernate utilise `ddl-auto: update` en développement et `open-in-view` est désactivé.

La variable `CORI7LA_DB_PASSWORD` permet de remplacer le mot de passe utilisé par l’application ; elle doit correspondre au mot de passe de PostgreSQL. Les valeurs incluses dans Compose sont destinées au développement local.

### 3. Lancer l’application

**Windows — PowerShell :**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS :**

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

L’API est accessible sur **`http://localhost:8080/api`**. Le projet expose une API REST ; aucune page d’accueil n’est prévue à la racine.

### 4. Compiler ou arrêter

```powershell
.\mvnw.cmd clean compile
```

Arrêter l’application avec `Ctrl+C`. Pour arrêter PostgreSQL en conservant les données :

```bash
docker compose stop db
```

## Modèle et architecture

Le modèle comporte six entités : **Conducteur, Passager, Vehicule, Trajet, PointArret et Reservation**.

- Un véhicule appartient à un conducteur.
- Un trajet utilise un véhicule et possède une liste ordonnée de points d’arrêt.
- Une réservation relie un passager, un trajet et ses points de montée et de descente.
- Les villes de départ et d’arrivée sont déduites des arrêts ; elles ne sont pas dupliquées dans `Trajet`.

| Package | Responsabilité |
|---|---|
| `controller` | Routes HTTP, validation des requêtes et codes de réponse |
| `service` / `service.impl` | Contrats et règles métier transactionnelles |
| `repository` | Accès aux données, JPQL et verrouillage |
| `entity` | Entités JPA et statuts |
| `dto` | Records de requête et de réponse |
| `mapper` | Conversions MapStruct |
| `exception` | Exceptions métier et gestion centralisée |

Le package racine est `ma.youcode.CoRi7la`. Les tests sont dans `src/test/java`.

## Règles métier

### Publication

- Le véhicule doit appartenir au conducteur indiqué.
- Le nombre de places proposées doit être compris entre **1 et la capacité du véhicule** ; la capacité autorisée est de 1 à 8 passagers.
- Le parcours contient au moins deux arrêts, sans ville répétée.
- Le départ est futur et les heures de passage sont strictement croissantes.
- Le prix du premier arrêt vaut **0 DH**, puis les prix cumulés sont strictement croissants.

### Réservation et prix

Le trajet doit être `publie` et ne pas avoir commencé. Les deux arrêts doivent lui appartenir, avec la montée avant la descente.

```text
Montant total = (prix cumulé à la descente − prix cumulé à la montée) × places
```

| Parcours d’exemple | Heure | Prix cumulé |
|---|---|---|
| Tanger | 08:00 | 0 DH |
| Tetouan | 09:00 | 50 DH |
| Chefchaouen | 10:00 | 80 DH |

Une place Tanger → Tetouan coûte **50 DH**. Deux places Tanger → Chefchaouen coûtent **160 DH**. Le serveur calcule le montant en `BigDecimal`, avec deux décimales.

Un passager ne peut avoir qu’une réservation `confirmee` par trajet. **Les places sont occupées sur l’ensemble du trajet**, même pour un segment. Les places restantes sont calculées depuis les réservations confirmées.

Le service verrouille le trajet avec `PESSIMISTIC_WRITE` dans une transaction avant de vérifier les places et d’enregistrer la réservation. La dernière place réservée fait passer le trajet à `complet`.

### Cycle de vie

| Statut du trajet | Signification |
|---|---|
| `publie` | Disponible à la réservation si le départ n’est pas passé |
| `complet` | Toutes les places sont réservées |
| `termine` | Terminé par son conducteur après le départ ; statut final |
| `annule` | Annulé par son conducteur ; statut final |

Le passager peut annuler **au plus tard 24 heures avant le départ**. Si une annulation libère une place sur un trajet complet, celui-ci redevient publié.

L’annulation du trajet annule ses réservations confirmées dans la même transaction. Les données restent consultables pour l’historique. Les valeurs de statut des réservations sont `confirmee` et `annule`.

## API

Préfixe commun : **`http://localhost:8080/api`**. Les corps de requête sont envoyés en JSON avec `Content-Type: application/json`.

### Profils et véhicules

| Méthode | Route | Action |
|---|---|---|
| POST | `/conducteurs` | Créer un conducteur |
| GET | `/conducteurs?nom=Amrani&email=hamid@example.test` | Rechercher des conducteurs ; filtres facultatifs |
| GET / PUT | `/conducteurs/{id}` | Consulter / modifier un conducteur |
| POST | `/passagers` | Créer un passager |
| GET | `/passagers?email=bouchra@example.test` | Lister / rechercher les passagers |
| GET / PUT | `/passagers/{id}` | Consulter / modifier un passager |
| POST / GET | `/conducteurs/{conducteurId}/vehicules` | Ajouter / lister les véhicules |
| GET / PUT / DELETE | `/vehicules/{id}` | Consulter / modifier / supprimer un véhicule |

L’email et le permis du conducteur sont uniques, ainsi que l’immatriculation du véhicule. Un véhicule associé à un trajet ne peut pas être supprimé.

### Trajets et réservations

| Méthode | Route | Action |
|---|---|---|
| POST | `/conducteurs/{conducteurId}/trajets` | Publier un trajet |
| GET | `/trajets/{id}` | Consulter le détail |
| GET | `/trajets` | Rechercher avec filtres et pagination |
| PATCH | `/conducteurs/{conducteurId}/trajets/{trajetId}/annulation` | Annuler le trajet et ses réservations confirmées |
| PATCH | `/conducteurs/{conducteurId}/trajets/{trajetId}/terminaison` | Terminer après le départ |
| POST / GET | `/passagers/{passagerId}/reservations` | Réserver / consulter l’historique |
| GET | `/conducteurs/{conducteurId}/trajets/{trajetId}/reservations` | Consulter les réservations de son trajet |
| PATCH | `/passagers/{passagerId}/reservations/{reservationId}/annulation` | Annuler sa réservation |

Les requêtes PATCH d’annulation et de terminaison n’ont pas de corps.

### Exemple : publier un trajet

`POST /api/conducteurs/1/trajets`

```json
{
  "vehiculeId": 2,
  "placesProposees": 3,
  "pointsArret": [
    { "ville": "Tanger", "heurePassage": "2026-12-10T08:00:00", "prixDepuisDepart": 0.00 },
    { "ville": "Tetouan", "heurePassage": "2026-12-10T09:00:00", "prixDepuisDepart": 50.00 },
    { "ville": "Chefchaouen", "heurePassage": "2026-12-10T10:00:00", "prixDepuisDepart": 80.00 }
  ]
}
```

Adapter les identifiants aux données existantes et choisir des dates futures. L’ordre des éléments du tableau définit l’ordre du parcours.

### Exemple : rechercher un segment

```text
GET http://localhost:8080/api/trajets?villeMontee=Tanger&villeDescente=Tetouan&date=2026-12-10&nombrePlaces=1&prixMax=50&page=0&taille=10
```

`date` correspond au **jour du départ du trajet**. `prixMax` est facultatif et s’applique au **prix du segment par place**. La pagination commence à `page=0`, avec une taille de 1 à 100. Les résultats sont triés par départ, puis identifiant.

### Exemple : réserver

`POST /api/passagers/1/reservations`

```json
{
  "trajetId": 1,
  "pointMonteeId": 1,
  "pointDescenteId": 3,
  "nombrePlaces": 2
}
```

Les identifiants des points proviennent du détail du trajet : ce ne sont pas leurs numéros d’ordre.

### Réponses HTTP

| Code | Utilisation |
|---|---|
| `200` | Lecture, modification ou changement de statut réussi |
| `201` | Ressource créée |
| `204` | Véhicule supprimé |
| `400` | Entrée invalide ou parcours incohérent |
| `403` | Ressource appartenant à un autre conducteur / passager indiqué |
| `404` | Ressource introuvable |
| `409` | Conflit métier : doublon, places insuffisantes, délai ou statut incompatible |
| `500` | Erreur interne, avec détail générique côté client |

Les exceptions métier sont exposées sous forme de `ProblemDetail`, avec notamment `status`, `detail`, `type`, `instance` et `timestamp`.

> Les contrôles de propriété actuels comparent les identifiants fournis dans les routes. Le projet ne dispose pas encore d’une authentification reliant ces identifiants à un utilisateur connecté.

## Tests unitaires

**Windows — exécuter les deux services demandés par le brief :**

```powershell
.\mvnw.cmd "-Dtest=ReservationServiceImplTest,TrajetServiceImplTest" test
```

**Linux / macOS :**

```bash
./mvnw '-Dtest=ReservationServiceImplTest,TrajetServiceImplTest' test
```

| Classe | Cas exécutés |
|---|---:|
| `ReservationServiceImplTest` | 14 |
| `TrajetServiceImplTest` | 20 |
| **Total** | **34** |

Ces tests couvrent des cas nominaux et des règles refusées : montant, capacité, doublons, ordre des arrêts, publication, annulations et terminaison. Les repositories sont simulés avec Mockito : PostgreSQL n’est pas nécessaire pour cette commande ciblée.

Pour exécuter toute la suite :

```powershell
.\mvnw.cmd test
```

Le test de chargement du contexte Spring peut nécessiter PostgreSQL démarré. Les tests unitaires seuls ne démontrent pas le verrouillage réel ni le rollback en base. Les rapports Maven sont disponibles dans `target/surefire-reports`.

## Démonstration avec Postman

Une collection **`CoRi7la_Simple.postman_collection.json`** a été préparée séparément : requêtes regroupées par dossier, URL complètes et données fixes, sans script JavaScript ni variable Postman.

1. Démarrer PostgreSQL et l’application.
2. Importer le fichier fourni dans Postman.
3. Envoyer les requêtes une à une et recopier les identifiants retournés dans les requêtes suivantes.
4. Vérifier le statut HTTP, le montant, les places restantes et les statuts métier.

Les exemples ne supposent pas une base vide. Modifier les valeurs uniques déjà utilisées et adapter les dates pour les scénarios des 24 heures et de terminaison.

**Parcours conseillé pour la soutenance :** publier un trajet → rechercher un segment → réserver → montrer le prix et les places restantes → annuler une réservation ou le trajet → consulter l’historique.

## Organisation Git et suivi

- `main` : livraison finale.
- `develop` : intégration du code.
- `feature/*` : fonctionnalités et tests isolés avant intégration.

Le suivi des tâches et des user stories est disponible dans le [tableau Jira CoRi7la](https://edu-team-m91pe19p.atlassian.net/jira/software/projects/CORI/boards/69).

## Autrice

**Bouchra El Qartit** — Projet réalisé dans le cadre de la formation **YouCode**.

[Profil GitHub](https://github.com/Bouchraelqartit) · [Dépôt CoRi7la](https://github.com/Bouchraelqartit/CoRi7la)
