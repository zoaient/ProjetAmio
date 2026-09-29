# LightWatch — état à l'issue du T.P. 1

Projet fil rouge du module AMIO (Applications mobiles et Internet des Objets),
troisième année. Ce dépôt correspond au **livrable de fin de T.P. 1** : l'écran
principal affiche une liste de capteurs alimentée par une source de données
simulée.

## Configuration

| | |
| --- | --- |
| `minSdk` | 26 (Android 8.0) |
| `targetSdk` | 29 (Android 10) |
| `compileSdk` | 33 |
| Android Gradle Plugin | 7.4.2 |
| Gradle | 7.5 |
| Langage | Java 11 |

## Import dans Android Studio

1. *File → Open*, sélectionner le répertoire `LightWatch/` (celui qui contient
   `settings.gradle`), puis accepter la fenêtre *Trust Project*.
2. **Le binaire `gradle/wrapper/gradle-wrapper.jar` n'est pas inclus** (c'est un
   fichier binaire, volontairement absent de l'archive). Au premier
   *Gradle Sync*, Android Studio propose de le régénérer et télécharge la
   distribution indiquée dans `gradle-wrapper.properties` ; accepter. En cas de
   refus, deux solutions :
   - *File → Settings → Build, Execution, Deployment → Build Tools → Gradle*,
     choisir `Use Gradle from: 'gradle-wrapper.properties' file` puis relancer la
     synchronisation ;
   - ou, depuis un terminal avec Gradle installé : `gradle wrapper --gradle-version 7.5`.
3. La première synchronisation télécharge les dépendances AndroidX : prévoir une
   connexion réseau.
4. Créer un AVD d'**API 29** (voir l'énoncé du T.P. 1, exercice 1 question 3),
   puis *Run*.

## Ce qui est implémenté

- `model/MoteReading` — relevé immuable d'un capteur (`java.time.Instant`).
- `data/SensorDataSource` — contrat d'accès aux relevés.
- `data/FakeSensorDataSource` — 8 capteurs simulés, latence de 500 ms.
- `ui/MoteAdapter` — adaptateur `RecyclerView` avec `ViewHolder` classique
  (`findViewById` depuis `itemView`).
- `ui/MainActivity` — barre d'outils, carte d'état, interrupteur de
  surveillance, puces de synthèse, liste, *pull-to-refresh*, état vide,
  `ExecutorService` + `Handler` pour ne pas bloquer le thread de l'interface.
- Les vues sont récupérées par `findViewById()` : `viewBinding` est à `false`,
  conformément à l'énoncé du T.P. 1 (exercice 3, question 3).
- Thème Material `DayNight`, toutes les chaînes externalisées, icône adaptative.

Les questions facultatives de l'exercice 4 (tri par luminosité décroissante,
puces de synthèse) sont incluses.

## Ce qui reste à faire (repères `TODO` dans le code)

- **T.P. 2** — `IotLabSensorDataSource` (HttpURLConnection, JSON),
  `network_security_config`, classe `LightDetector` et calibrage du seuil, qui
  remplacera `MoteAdapter.PROVISIONAL_THRESHOLD`.
- **T.P. 3** — `MonitoringService` de premier plan branché sur l'interrupteur,
  canaux et notifications, `AlertPolicy`, remontée des résultats vers l'activité.
- **T.P. 4** — `SettingsActivity` branchée sur l'entrée « Réglages » du menu,
  alerte e-mail, vibreur, `WorkManager`.

## Limite connue

La rotation de l'écran relance un relevé au lieu de restaurer la liste : c'est
l'objet de la question facultative 4 de l'exercice 4 (et la persistance du T.P. 2
la traitera plus proprement).
