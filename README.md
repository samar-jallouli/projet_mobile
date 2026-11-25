# ClubSync v3 🚀

ClubSync est une application Android moderne dédiée à la gestion complète d'un club étudiant : gestion des membres et recrues, planification des événements, pointage des présences par QR Code et fonctionnalités d'intelligence artificielle avec Firebase AI Logic (Gemini).

---

## 🛠️ Stack Technique

- **Langage & UI** : Java, Android XML, Material Components 3 (Thème sombre sur mesure), ViewBinding.
- **Backend & Données** : Firebase Authentication, Cloud Firestore (avec persistance hors-ligne et règles de sécurité).
- **Intelligence Artificielle** : Firebase AI Logic (Gemini Developer API, `com.google.firebase:firebase-ai`, modèle `gemini-3.5-flash`).
- **Vision & QR Code** : CameraX, ML Kit Barcode Scanning, ZXing Core (génération des QR Codes).
- **Visualisation de Données** : MPAndroidChart (JitPack).

---

## 🏗️ Architecture en Couches

L'application respecte une séparation stricte des responsabilités sans aucune logique Firebase ou Gemini dans les contrôleurs d'interface :

```
com.clubsync
├── ClubSyncApplication.java      # Initialisation Firebase, persistance hors-ligne et App Check Debug
├── model/                        # Modèles métiers (constructeurs Firestore, getters/setters, toMap)
│   ├── AppUser.java              # Utilisateur authentifié & rôle
│   ├── Member.java               # Profil membre du club & token QR
│   ├── Event.java                # Événement & capacité
│   ├── Registration.java         # Inscription d'un membre à un événement
│   ├── Attendance.java           # Pointage de présence vérifié
│   └── ChatMessage.java          # Message pour l'assistant IA
├── data/                         # Couche d'accès aux données (Singletons)
│   ├── Callback.java             # Interface générique asynchrone onSuccess / onError
│   ├── FirebaseRepository.java   # Unique point d'accès Firestore & Auth
│   └── AiService.java            # Unique point d'accès Gemini (prompts stricts & GenerativeModelFutures)
├── ui/                           # Interface utilisateur (14 activités réparties par rôle)
│   ├── auth/                     # LoginActivity (Launcher, validation, auto-login)
│   ├── admin/                    # AdminDashboard, AddMember, MemberDetail, Event, AttendanceScanner, Analytics, RecruitAI
│   ├── member/                   # MemberHome, MemberEvents, MyQr, AiAssistant, Profile
│   └── common/                   # BaseActivity (navigation bottom nav), MemberList, Adapters
└── util/                         # Classes utilitaires
    ├── Constants.java            # Rôles, statuts, départements, modèle IA ("gemini-3.5-flash")
    ├── DateUtils.java            # Formatage des dates et heures (Locale FR)
    ├── Validators.java           # Règles de validation (nom ≥ 3, email, tél tunisien 8 chiffres, mdp ≥ 6)
    └── QrUtils.java              # Génération bitmap QR Code via ZXing
```

---

## 📊 Schéma des 5 Collections Firestore

1. **`users/{uid}`** :
   - `fullName` (String), `email` (String), `role` (`"ADMIN"` | `"MEMBRE"`), `memberId` (String), `createdAt` (Timestamp)
2. **`members/{autoId}`** :
   - `fullName` (String), `email` (String), `phone` (String), `role` (String), `department` (`"Tech"` | `"Com"` | `"Event"` | `"RH"`), `status` (`"ACTIF"` | `"EN_ATTENTE"`), `qrToken` (UUID unique), `joinedAt` (Timestamp)
3. **`events/{autoId}`** :
   - `title` (String), `description` (String), `location` (String), `eventDate` (Timestamp), `capacity` (int), `registeredCount` (int), `attendeesCount` (int), `createdBy` (String)
4. **`registrations/{eventId_memberId}`** :
   - `eventId` (String), `memberId` (String), `memberName` (String), `registeredAt` (Timestamp)
5. **`attendances/{eventId_memberId}`** :
   - `eventId` (String), `memberId` (String), `memberName` (String), `scannedAt` (Timestamp), `verifiedBy` (String)

---

## ⚙️ Installation & Configuration

1. **Configuration Firebase** :
   - Placer le fichier `google-services.json` dans le dossier `app/`.
   - Activer **Firebase Authentication** (fournisseur Email / Mot de passe).
   - Activer **Cloud Firestore** en mode natif.
   - Coller les règles de sécurité du fichier `firestore.rules` dans l'onglet **Règles Firestore**.
2. **Configuration Firebase AI Logic & App Check** :
   - Dans la console Firebase, activer l'API Gemini / Firebase AI Logic.
   - En mode Debug, le jeton App Check s'affiche dans Logcat au lancement :
     `D/DebugAppCheckProvider: Enter this debug secret into the allow list in the Firebase Console...`
   - Copier ce jeton et l'ajouter dans la console : **App Check > Apps > Manage debug tokens**.
3. **Compilation & Déploiement** :
   ```bash
   ./gradlew assembleDebug
   ```
   L'APK de débogage est produit dans `app/build/outputs/apk/debug/app-debug.apk`.
