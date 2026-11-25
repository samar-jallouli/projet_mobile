# Planification des Sprints & Répartition de l'Équipe — ClubSync v3

Équipe projet : **Youssef** & **Samar**

---

### Sprint 1 : Socle Technique, Sécurité & Rôles
**Objectif** : Mettre en place les fondations techniques, l'authentification et l'aiguillage selon le rôle.
- **Youssef** :
  - Configuration Gradle (Firebase BoM, Guava, CameraX, ML Kit, ZXing, MPAndroidChart, ViewBinding).
  - Modèles métiers Firestore (`AppUser`, `Member`, `Event`, `Registration`, `Attendance`).
  - Implémentation du singleton `FirebaseRepository` (Auth, `getUserRole`, auto-login).
- **Samar** :
  - Définition du thème sombre complet Material3 (couleurs, dimensions, styles verre, badges).
  - Création des barres de navigation arrondie Admin et Membre.
  - Implémentation de `BaseActivity` et de `LoginActivity` (validation et gestion d'erreurs).

---

### Sprint 2 : Gestion des Membres & QR Code
**Objectif** : Offrir le cycle de vie complet des membres avec identification par QR Code.
- **Youssef** :
  - Méthodes CRUD membres dans `FirebaseRepository` et écoute temps réel.
  - Utilitaire `QrUtils` avec ZXing pour la génération des QR Codes.
  - Implémentation de `MemberDetailActivity` (édition, suppression sécurisée, affichage du QR).
- **Samar** :
  - Création de `AddMemberActivity` avec validation granulaire par champ.
  - Création de `MemberListActivity` et `MemberAdapter` (chips de filtres, recherche instantanée).
  - Gestion des permissions d'affichage (bouton `+` et clics réservés à l'Admin).

---

### Sprint 3 : Événements, Dashboards & Inscriptions
**Objectif** : Gérer le planning des réunions et les inscriptions en temps réel.
- **Youssef** :
  - Transactions atomiques d'inscription et désinscription avec vérification de capacité.
  - Implémentation de `EventActivity` (formulaire, DatePicker, TimePicker, liste avec statut).
  - Écouteurs en direct des événements et des inscriptions du membre connecté.
- **Samar** :
  - Implémentation de `AdminDashboardActivity` (cartes KPI en temps réel, prochain événement, raccourcis).
  - Implémentation de `MemberHomeActivity` (salutation, bouton « Je m'inscris », stats).
  - Implémentation de `MemberEventsActivity` (onglets « À venir » / « Mes inscriptions »).

---

### Sprint 4 : Scanner de Présences & Statistiques Avancées
**Objectif** : Déployer le pointage caméra et les visualisations graphiques.
- **Youssef** :
  - Intégration de CameraX et ML Kit Barcode Scanning dans `AttendanceScannerActivity`.
  - Transaction Firestore pour le pointage des présences et la mise à jour des compteurs.
  - Écran `MyQrActivity` pour la présentation du badge numérique à l'entrée.
- **Samar** :
  - Configuration de MPAndroidChart dans `AnalyticsActivity`.
  - Implémentation du Donut Chart (répartition par pôle) et du BarChart (conversion du recrutement).
  - Implémentation de `ProfileActivity` (badges, rang et classement des présences).

---

### Sprint 5 : Intelligence Artificielle & Finalisation
**Objectif** : Intégrer les fonctionnalités Gemini via Firebase AI Logic et sécuriser l'application.
- **Youssef** :
  - Implémentation de `AiService` avec `GenerativeModelFutures` et prompts stricts en français.
  - Intégration de l'analyse et recommandation de recrue dans `RecruitAIActivity` (parsing JSON robuste).
  - Rédaction des règles de sécurité `firestore.rules` et activation de la persistance hors-ligne.
- **Samar** :
  - Implémentation du fil de discussion interactif dans `AiAssistantActivity` avec injection du contexte.
  - Intégration de la génération automatique d'ordre du jour dans `EventActivity`.
  - Rédaction du cahier de tests `docs/TESTS.md` et finalisation du `README.md`.
