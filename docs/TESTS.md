# Scénarios de Tests Manuels — ClubSync v3

Ce document détaille les scénarios de test pour chacune des 14 activités de ClubSync.

---

### 1. LoginActivity (Lanceur)
- [ ] **TC-01 : Validation champs vides** — Cliquer sur "Se connecter" avec des champs vides affiche le message d'erreur.
- [ ] **TC-02 : Validation format email** — Saisir un email invalide (ex: `test@invalide`) affiche une erreur.
- [ ] **TC-03 : Connexion rôle ADMIN** — Saisir les identifiants d'un Admin redirige immédiatement vers `AdminDashboardActivity`.
- [ ] **TC-04 : Connexion rôle MEMBRE** — Saisir les identifiants d'un Membre redirige immédiatement vers `MemberHomeActivity`.
- [ ] **TC-05 : Auto-Login** — Quitter l'application et la relancer ouvre directement l'espace approprié sans repasser par le formulaire.

---

### 2. AdminDashboardActivity
- [ ] **TC-06 : Compteurs KPI** — Les compteurs « Membres actifs » (cyan) et « Recrues en attente » (ambre) affichent les totaux Firestore.
- [ ] **TC-07 : Prochain événement** — La carte affiche le titre, la date et le nombre d'inscrits du prochain événement planifié.
- [ ] **TC-08 : Tuiles de raccourcis** — Les 4 tuiles ouvrent respectivement l'ajout de membre, les événements, le scanner et l'analyse IA.
- [ ] **TC-09 : Menu Démo & Déconnexion** — Cliquer sur l'avatar permet de générer les données de démo et de se déconnecter.

---

### 3. AddMemberActivity
- [ ] **TC-10 : Contrôles de validation** — Les erreurs spécifiques s'affichent pour chaque champ (nom < 3 car, email invalide, tél non tunisien).
- [ ] **TC-11 : Enregistrement membre** — Le membre est créé avec le statut `EN_ATTENTE`, un UUID généré et la date courante.

---

### 4. MemberListActivity
- [ ] **TC-12 : Recherche en direct** — Saisir des lettres dans le champ recherche filtre instantanément la liste par nom.
- [ ] **TC-13 : Filtrage par pôle** — Cliquer sur Tech, Com, Event ou RH filtre la liste selon le pôle choisi.
- [ ] **TC-14 : Droits selon le rôle** — En tant qu'ADMIN, le bouton `+` est visible et cliquer sur un membre ouvre son détail. En tant que MEMBRE, la liste est en lecture seule.

---

### 5. MemberDetailActivity
- [ ] **TC-15 : Affichage du QR Code** — Le QR Code personnel est généré et rendu avec fond blanc et modules `#0B1020`.
- [ ] **TC-16 : Modification du statut** — Changer le statut en `ACTIF` ou mettre à jour le téléphone enregistre les modifications dans Firestore.
- [ ] **TC-17 : Suppression sécurisée** — Le bouton supprimer affiche une boîte de dialogue de confirmation avant suppression.

---

### 6. EventActivity
- [ ] **TC-18 : Sélecteurs de date et heure** — `DatePickerDialog` et `TimePickerDialog` permettent de définir la date de l'événement.
- [ ] **TC-19 : IA Ordre du jour** — Le bouton « Générer l'ordre du jour avec l'IA » appelle Gemini et préremplit les 5 à 7 points.
- [ ] **TC-20 : Création d'événement** — L'événement est créé avec `registeredCount = 0` et apparaît dans la liste au-dessus.

---

### 7. AttendanceScannerActivity
- [ ] **TC-21 : Permissions Caméra** — L'application demande l'accès caméra au premier lancement.
- [ ] **TC-22 : Pointage par scan** — Scanner le QR Code d'un membre enregistre sa présence pour l'événement sélectionné et affiche la confirmation verte.

---

### 8. AnalyticsActivity
- [ ] **TC-23 : Répartition par pôle** — Le Donut Chart affiche les parts des pôles Tech, Com, Event et RH.
- [ ] **TC-24 : Taux de conversion** — Le BarChart compare les membres ACTIF vs EN_ATTENTE et calcule le pourcentage de conversion.

---

### 9. RecruitAIActivity
- [ ] **TC-25 : Analyse du profil** — Sélectionner une recrue et cliquer sur « Analyser avec l'IA » affiche le pôle recommandé, le score et la justification.
- [ ] **TC-26 : Validation du pôle** — Le bouton « Valider ce pôle » met à jour le département de la recrue après confirmation.

---

### 10. MemberHomeActivity
- [ ] **TC-27 : Salutation personnalisée** — Le prénom du membre connecté et son département s'affichent dans l'en-tête.
- [ ] **TC-28 : Inscription rapide** — Cliquer sur « Je m'inscris » bascule le bouton en « Inscrit ✓ » et met à jour le compteur en temps réel.
- [ ] **TC-29 : Lien Annuaire** — La carte annuaire ouvre la liste des membres en lecture seule.

---

### 11. MemberEventsActivity
- [ ] **TC-30 : Onglets Événements** — La bascule entre « À venir » et « Mes inscriptions » filtre la liste en temps réel.
- [ ] **TC-31 : Gestion des places** — Le nombre de places restantes se décrémente lors d'une inscription et le badge « Complet » apparaît si capacité atteinte.

---

### 12. MyQrActivity
- [ ] **TC-32 : Affichage QR personnel** — Le grand QR Code est affiché avec le nom, le rôle, le département et le badge de statut du membre.

---

### 13. AiAssistantActivity
- [ ] **TC-33 : Réponses contextuelles** — Poser une question sur une réunion répond avec précision à partir des événements Firestore.
- [ ] **TC-34 : Suggestions rapides** — Cliquer sur « Mes présences » ou « Événements cette semaine » envoie la requête immédiatement.

---

### 14. ProfileActivity
- [ ] **TC-35 : Statistiques et Classement** — Le nombre de présences réelles et le classement s'affichent correctement.
- [ ] **TC-36 : Mise en évidence** — La ligne du membre connecté apparaît en cyan dans la liste de classement.
