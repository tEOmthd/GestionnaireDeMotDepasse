# Gestionnaire de Mots de Passe

Application desktop Java pour stocker et gérer des credentials chiffrés localement.

## Stack technique

- **Java 21** + **Swing** — interface graphique desktop
- **SQLite** via `sqlite-jdbc` — base de données locale
- **AES** via `javax.crypto` — chiffrement des données sensibles
- **Maven** — gestion des dépendances et build

## Prérequis

- Java 21+
- Maven 3.6+

## Installation et lancement

```bash
# Compiler et packager
mvn clean package

# Lancer l'application
java -jar target/GestionnaireDeMotDepasse.jar
```

## Fonctionnalités

- **CRUD complet** — ajout, consultation, modification et suppression de credentials
- **Chiffrement AES** — les mots de passe et données sensibles sont chiffrés en base
- **Filtre par date d'expiration** — repérez rapidement les credentials arrivant à expiration
- **Interface graphique Swing** — application desktop autonome, aucune dépendance réseau

## Contexte

Projet universitaire réalisé dans le cadre d'un cursus IUT.
