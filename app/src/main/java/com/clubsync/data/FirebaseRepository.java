package com.clubsync.data;

import com.clubsync.model.AppUser;
import com.clubsync.model.Attendance;
import com.clubsync.model.Event;
import com.clubsync.model.Member;
import com.clubsync.model.Registration;
import com.clubsync.util.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FirebaseRepository {
    private static volatile FirebaseRepository instance;

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    private FirebaseRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public static FirebaseRepository getInstance() {
        if (instance == null) {
            synchronized (FirebaseRepository.class) {
                if (instance == null) {
                    instance = new FirebaseRepository();
                }
            }
        }
        return instance;
    }

    // ==========================================
    // AUTHENTIFICATION & RÔLES
    // ==========================================

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void authenticate(String email, String password, Callback<String> callback) {
        if (email == null || password == null) {
            if (callback != null) callback.onError("Email ou mot de passe manquant");
            return;
        }

        auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        if (callback != null) callback.onSuccess(user.getUid());
                    } else {
                        if (callback != null) callback.onError("Utilisateur introuvable");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur de connexion");
                });
    }

    public void getUserRole(String uid, Callback<String> callback) {
        if (uid == null || uid.trim().isEmpty()) {
            if (callback != null) callback.onError("Identifiant utilisateur manquant");
            return;
        }

        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role != null && !role.trim().isEmpty()) {
                            if (callback != null) callback.onSuccess(role.trim().toUpperCase());
                        } else {
                            if (callback != null) callback.onError("Rôle utilisateur non défini");
                        }
                    } else {
                        if (callback != null) callback.onError("Document utilisateur introuvable");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur de récupération du rôle");
                });
    }

    public void getCurrentUserRole(Callback<String> callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onError("Non connecté");
            return;
        }
        getUserRole(user.getUid(), callback);
    }

    public void getUserProfile(String uid, Callback<AppUser> callback) {
        if (uid == null || uid.trim().isEmpty()) {
            if (callback != null) callback.onError("Identifiant utilisateur manquant");
            return;
        }

        db.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        AppUser user = documentSnapshot.toObject(AppUser.class);
                        if (user != null) {
                            user.setId(documentSnapshot.getId());
                            if (callback != null) callback.onSuccess(user);
                        } else {
                            if (callback != null) callback.onError("Format de données utilisateur invalide");
                        }
                    } else {
                        if (callback != null) callback.onError("Profil utilisateur introuvable");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur de récupération du profil");
                });
    }

    public void getCurrentMember(Callback<Member> callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onError("Non connecté");
            return;
        }

        getUserProfile(user.getUid(), new Callback<AppUser>() {
            @Override
            public void onSuccess(AppUser appUser) {
                if (appUser != null && appUser.getMemberId() != null && !appUser.getMemberId().trim().isEmpty()) {
                    getMemberById(appUser.getMemberId(), callback);
                } else {
                    // Recherche par email si memberId non renseigné
                    String email = user.getEmail();
                    if (email != null && !email.trim().isEmpty()) {
                        db.collection(Constants.COLLECTION_MEMBERS)
                                .whereEqualTo("email", email.trim())
                                .limit(1)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                                        Member m = doc.toObject(Member.class);
                                        if (m != null) {
                                            m.setId(doc.getId());
                                            if (callback != null) callback.onSuccess(m);
                                            return;
                                        }
                                    }
                                    // Membre par défaut basé sur l'AppUser
                                    Member fallback = new Member(user.getUid(), appUser != null ? appUser.getFullName() : "Membre",
                                            user.getEmail(), "", "Membre", Constants.DEPT_TECH, Constants.STATUS_ACTIVE,
                                            user.getUid(), Timestamp.now());
                                    if (callback != null) callback.onSuccess(fallback);
                                })
                                .addOnFailureListener(e -> {
                                    Member fallback = new Member(user.getUid(), appUser != null ? appUser.getFullName() : "Membre",
                                            user.getEmail(), "", "Membre", Constants.DEPT_TECH, Constants.STATUS_ACTIVE,
                                            user.getUid(), Timestamp.now());
                                    if (callback != null) callback.onSuccess(fallback);
                                });
                    } else {
                        if (callback != null) callback.onError("Email membre introuvable");
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) callback.onError(error);
            }
        });
    }

    public void logout() {
        auth.signOut();
    }

    // ==========================================
    // GESTION DES MEMBRES (PASSE 2)
    // ==========================================

    public void addMember(Member member, Callback<String> callback) {
        if (member == null) {
            if (callback != null) callback.onError("Données du membre manquantes");
            return;
        }

        DocumentReference docRef = db.collection(Constants.COLLECTION_MEMBERS).document();
        member.setId(docRef.getId());

        docRef.set(member.toMap())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(docRef.getId());
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur lors de l'ajout du membre");
                });
    }

    public void updateMember(Member member, Callback<Void> callback) {
        if (member == null || member.getId() == null) {
            if (callback != null) callback.onError("Identifiant du membre manquant pour la mise à jour");
            return;
        }

        db.collection(Constants.COLLECTION_MEMBERS)
                .document(member.getId())
                .update(member.toMap())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur lors de la mise à jour du membre");
                });
    }

    public void deleteMember(String memberId, Callback<Void> callback) {
        if (memberId == null || memberId.trim().isEmpty()) {
            if (callback != null) callback.onError("Identifiant du membre manquant pour la suppression");
            return;
        }

        db.collection(Constants.COLLECTION_MEMBERS)
                .document(memberId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur lors de la suppression du membre");
                });
    }

    public void getMemberById(String memberId, Callback<Member> callback) {
        if (memberId == null || memberId.trim().isEmpty()) {
            if (callback != null) callback.onError("Identifiant du membre manquant");
            return;
        }

        db.collection(Constants.COLLECTION_MEMBERS)
                .document(memberId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Member member = documentSnapshot.toObject(Member.class);
                        if (member != null) {
                            member.setId(documentSnapshot.getId());
                            if (callback != null) callback.onSuccess(member);
                        } else {
                            if (callback != null) callback.onError("Format de données invalide");
                        }
                    } else {
                        if (callback != null) callback.onError("Membre introuvable");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur de lecture du membre");
                });
    }

    public ListenerRegistration listenMembers(Callback<List<Member>> callback) {
        return db.collection(Constants.COLLECTION_MEMBERS)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        if (callback != null) callback.onError(error.getLocalizedMessage() != null ? error.getLocalizedMessage() : "Erreur d'écoute des membres");
                        return;
                    }

                    List<Member> members = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Member m = doc.toObject(Member.class);
                            if (m != null) {
                                m.setId(doc.getId());
                                members.add(m);
                            }
                        }
                    }
                    if (callback != null) {
                        callback.onSuccess(members);
                    }
                });
    }

    public void getMembers(Callback<List<Member>> callback) {
        db.collection(Constants.COLLECTION_MEMBERS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Member> members = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Member m = doc.toObject(Member.class);
                            if (m != null) {
                                m.setId(doc.getId());
                                members.add(m);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(members);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur de chargement des membres");
                });
    }

    public void getActiveMembersCount(Callback<Long> callback) {
        db.collection(Constants.COLLECTION_MEMBERS)
                .whereEqualTo("status", Constants.STATUS_ACTIVE)
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener(aggregateQuerySnapshot -> {
                    if (callback != null) callback.onSuccess(aggregateQuerySnapshot.getCount());
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur comptage membres actifs");
                });
    }

    public void getPendingMembersCount(Callback<Long> callback) {
        db.collection(Constants.COLLECTION_MEMBERS)
                .whereEqualTo("status", Constants.STATUS_PENDING)
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener(aggregateQuerySnapshot -> {
                    if (callback != null) callback.onSuccess(aggregateQuerySnapshot.getCount());
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur comptage recrues en attente");
                });
    }

    public void getMemberByQrToken(String qrToken, Callback<Member> callback) {
        if (qrToken == null || qrToken.trim().isEmpty()) {
            if (callback != null) callback.onError("Code QR invalide ou vide");
            return;
        }

        db.collection(Constants.COLLECTION_MEMBERS)
                .whereEqualTo("qrToken", qrToken.trim())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        Member member = doc.toObject(Member.class);
                        if (member != null) {
                            member.setId(doc.getId());
                            if (callback != null) callback.onSuccess(member);
                        } else {
                            if (callback != null) callback.onError("Données membre corrompues");
                        }
                    } else {
                        if (callback != null) callback.onError("Aucun membre associé à ce QR Code");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur de recherche par QR Code");
                });
    }

    // ==========================================
    // GESTION DES ÉVÉNEMENTS (PASSE 3)
    // ==========================================

    public void addEvent(Event event, Callback<String> callback) {
        if (event == null) {
            if (callback != null) callback.onError("Données de l'événement manquantes");
            return;
        }

        DocumentReference docRef = db.collection(Constants.COLLECTION_EVENTS).document();
        event.setId(docRef.getId());
        if (event.getCreatedBy() == null && getCurrentUser() != null) {
            event.setCreatedBy(getCurrentUser().getUid());
        }

        docRef.set(event.toMap())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(docRef.getId());
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur lors de la création de l'événement");
                });
    }

    public void updateEvent(Event event, Callback<Void> callback) {
        if (event == null || event.getId() == null) {
            if (callback != null) callback.onError("Identifiant d'événement manquant");
            return;
        }

        db.collection(Constants.COLLECTION_EVENTS)
                .document(event.getId())
                .update(event.toMap())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur de mise à jour de l'événement");
                });
    }

    public void deleteEvent(String eventId, Callback<Void> callback) {
        if (eventId == null || eventId.trim().isEmpty()) {
            if (callback != null) callback.onError("Identifiant d'événement manquant");
            return;
        }

        db.collection(Constants.COLLECTION_EVENTS)
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur lors de la suppression de l'événement");
                });
    }

    public ListenerRegistration listenEvents(Callback<List<Event>> callback) {
        return db.collection(Constants.COLLECTION_EVENTS)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        if (callback != null) callback.onError(error.getLocalizedMessage() != null ? error.getLocalizedMessage() : "Erreur d'écoute des événements");
                        return;
                    }

                    List<Event> events = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                event.setId(doc.getId());
                                events.add(event);
                            }
                        }
                    }

                    // Tri en mémoire par date d'événement
                    Collections.sort(events, (e1, e2) -> {
                        if (e1.getEventDate() == null && e2.getEventDate() == null) return 0;
                        if (e1.getEventDate() == null) return 1;
                        if (e2.getEventDate() == null) return -1;
                        return e1.getEventDate().compareTo(e2.getEventDate());
                    });

                    if (callback != null) {
                        callback.onSuccess(events);
                    }
                });
    }

    public void getEvents(Callback<List<Event>> callback) {
        db.collection(Constants.COLLECTION_EVENTS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                event.setId(doc.getId());
                                events.add(event);
                            }
                        }
                    }

                    Collections.sort(events, (e1, e2) -> {
                        if (e1.getEventDate() == null && e2.getEventDate() == null) return 0;
                        if (e1.getEventDate() == null) return 1;
                        if (e2.getEventDate() == null) return -1;
                        return e1.getEventDate().compareTo(e2.getEventDate());
                    });

                    if (callback != null) callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur de chargement des événements");
                });
    }

    // ==========================================
    // INSCRIPTIONS AVEC TRANSACTION (PASSE 3)
    // ==========================================

    public void registerToEvent(String eventId, String memberId, String memberName, Callback<Void> callback) {
        if (eventId == null || memberId == null) {
            if (callback != null) callback.onError("Données d'inscription incomplètes");
            return;
        }

        String regDocId = eventId + "_" + memberId;
        DocumentReference regRef = db.collection(Constants.COLLECTION_REGISTRATIONS).document(regDocId);
        DocumentReference eventRef = db.collection(Constants.COLLECTION_EVENTS).document(eventId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot regSnapshot = transaction.get(regRef);
            if (regSnapshot.exists()) {
                throw new FirebaseFirestoreException("Vous êtes déjà inscrit à cet événement",
                        FirebaseFirestoreException.Code.ALREADY_EXISTS);
            }

            DocumentSnapshot eventSnapshot = transaction.get(eventRef);
            if (!eventSnapshot.exists()) {
                throw new FirebaseFirestoreException("Événement introuvable",
                        FirebaseFirestoreException.Code.NOT_FOUND);
            }

            Long capacityLong = eventSnapshot.getLong("capacity");
            Long registeredLong = eventSnapshot.getLong("registeredCount");
            int capacity = capacityLong != null ? capacityLong.intValue() : 0;
            int registered = registeredLong != null ? registeredLong.intValue() : 0;

            if (capacity > 0 && registered >= capacity) {
                throw new FirebaseFirestoreException("Cet événement est complet",
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION);
            }

            // Création de l'inscription
            Registration registration = new Registration(regDocId, eventId, memberId, memberName, Timestamp.now());
            transaction.set(regRef, registration.toMap());

            // Incrémentation atomique du nombre d'inscrits
            transaction.update(eventRef, "registeredCount", registered + 1);

            return null;
        }).addOnSuccessListener(aVoid -> {
            if (callback != null) callback.onSuccess(null);
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur lors de l'inscription");
        });
    }

    public void unregisterFromEvent(String eventId, String memberId, Callback<Void> callback) {
        if (eventId == null || memberId == null) {
            if (callback != null) callback.onError("Données de désinscription incomplètes");
            return;
        }

        String regDocId = eventId + "_" + memberId;
        DocumentReference regRef = db.collection(Constants.COLLECTION_REGISTRATIONS).document(regDocId);
        DocumentReference eventRef = db.collection(Constants.COLLECTION_EVENTS).document(eventId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot regSnapshot = transaction.get(regRef);
            if (!regSnapshot.exists()) {
                return null;
            }

            DocumentSnapshot eventSnapshot = transaction.get(eventRef);
            if (eventSnapshot.exists()) {
                Long registeredLong = eventSnapshot.getLong("registeredCount");
                int registered = registeredLong != null ? registeredLong.intValue() : 0;
                int newRegistered = Math.max(0, registered - 1);
                transaction.update(eventRef, "registeredCount", newRegistered);
            }

            transaction.delete(regRef);
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (callback != null) callback.onSuccess(null);
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur lors de la désinscription");
        });
    }

    public ListenerRegistration listenMemberRegistrations(String memberId, Callback<List<Registration>> callback) {
        if (memberId == null || memberId.trim().isEmpty()) {
            if (callback != null) callback.onError("Identifiant membre manquant");
            return null;
        }

        return db.collection(Constants.COLLECTION_REGISTRATIONS)
                .whereEqualTo("memberId", memberId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        if (callback != null) callback.onError(error.getLocalizedMessage() != null ? error.getLocalizedMessage() : "Erreur écoute des inscriptions");
                        return;
                    }

                    List<Registration> list = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Registration reg = doc.toObject(Registration.class);
                            if (reg != null) {
                                reg.setId(doc.getId());
                                list.add(reg);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                });
    }

    public void checkRegistration(String eventId, String memberId, Callback<Boolean> callback) {
        if (eventId == null || memberId == null) {
            if (callback != null) callback.onSuccess(false);
            return;
        }

        String regDocId = eventId + "_" + memberId;
        db.collection(Constants.COLLECTION_REGISTRATIONS)
                .document(regDocId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isRegistered = documentSnapshot != null && documentSnapshot.exists();
                    if (callback != null) callback.onSuccess(isRegistered);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur vérification inscription");
                });
    }

    public void getRegistrationsForMember(String memberId, Callback<List<Registration>> callback) {
        if (memberId == null || memberId.trim().isEmpty()) {
            if (callback != null) callback.onSuccess(new ArrayList<>());
            return;
        }

        db.collection(Constants.COLLECTION_REGISTRATIONS)
                .whereEqualTo("memberId", memberId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Registration> list = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Registration reg = doc.toObject(Registration.class);
                            if (reg != null) {
                                reg.setId(doc.getId());
                                list.add(reg);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur de chargement des inscriptions");
                });
    }

    // ==========================================
    // MÉTHODES PRÉSENCES & STATISTIQUES (PASSE 6)
    // ==========================================

    public void recordAttendance(String eventId, String memberId, String memberName, String verifiedBy, Callback<Void> callback) {
        if (eventId == null || memberId == null) {
            if (callback != null) callback.onError("Données de pointage manquantes");
            return;
        }

        String attendanceId = eventId + "_" + memberId;
        DocumentReference attRef = db.collection(Constants.COLLECTION_ATTENDANCES).document(attendanceId);
        DocumentReference eventRef = db.collection(Constants.COLLECTION_EVENTS).document(eventId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot attSnapshot = transaction.get(attRef);
            if (attSnapshot.exists()) {
                throw new FirebaseFirestoreException("Présence déjà enregistrée pour cet événement",
                        FirebaseFirestoreException.Code.ALREADY_EXISTS);
            }

            Attendance attendance = new Attendance(attendanceId, eventId, memberId, memberName, Timestamp.now(), verifiedBy);
            transaction.set(attRef, attendance.toMap());

            DocumentSnapshot eventSnapshot = transaction.get(eventRef);
            if (eventSnapshot.exists()) {
                Long currentAttendees = eventSnapshot.getLong("attendeesCount");
                long attendees = currentAttendees != null ? currentAttendees : 0;
                transaction.update(eventRef, "attendeesCount", attendees + 1);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (callback != null) callback.onSuccess(null);
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur d'enregistrement de présence");
        });
    }

    public void getAttendancesForEvent(String eventId, Callback<List<Attendance>> callback) {
        if (eventId == null || eventId.trim().isEmpty()) {
            if (callback != null) callback.onSuccess(new ArrayList<>());
            return;
        }

        db.collection(Constants.COLLECTION_ATTENDANCES)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Attendance> list = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Attendance a = doc.toObject(Attendance.class);
                            if (a != null) {
                                a.setId(doc.getId());
                                list.add(a);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur chargement présences");
                });
    }

    public void getAttendancesForMember(String memberId, Callback<List<Attendance>> callback) {
        if (memberId == null || memberId.trim().isEmpty()) {
            if (callback != null) callback.onSuccess(new ArrayList<>());
            return;
        }

        db.collection(Constants.COLLECTION_ATTENDANCES)
                .whereEqualTo("memberId", memberId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Attendance> list = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Attendance a = doc.toObject(Attendance.class);
                            if (a != null) {
                                a.setId(doc.getId());
                                list.add(a);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur chargement présences membre");
                });
    }

    public void getAllAttendances(Callback<List<Attendance>> callback) {
        db.collection(Constants.COLLECTION_ATTENDANCES)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Attendance> list = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Attendance a = doc.toObject(Attendance.class);
                            if (a != null) {
                                a.setId(doc.getId());
                                list.add(a);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Erreur chargement total présences");
                });
    }

    // ==========================================
    // CRÉATION DES DONNÉES DE DÉMO (PASSE 6)
    // ==========================================

    public void populateDemoData(Callback<Void> callback) {
        String currentUid = getCurrentUser() != null ? getCurrentUser().getUid() : "admin_demo";

        // 6 Membres conformes à la maquette
        Member[] demoMembers = new Member[]{
                new Member(null, "Ahmed Ben Ali", "ahmed.benali@clubsync.tn", "20123456", "Développeur", Constants.DEPT_TECH, Constants.STATUS_ACTIVE, "qr-ahmed-01", Timestamp.now()),
                new Member(null, "Sarra Trabelsi", "sarra.trabelsi@clubsync.tn", "21456789", "Resp. Communication", Constants.DEPT_COM, Constants.STATUS_ACTIVE, "qr-sarra-02", Timestamp.now()),
                new Member(null, "Mehdi Jlassi", "mehdi.jlassi@clubsync.tn", "22789123", "Recrue", Constants.DEPT_EVENT, Constants.STATUS_PENDING, "qr-mehdi-03", Timestamp.now()),
                new Member(null, "Yasmine Gharbi", "yasmine.gharbi@clubsync.tn", "23987654", "Trésorière", Constants.DEPT_RH, Constants.STATUS_ACTIVE, "qr-yasmine-04", Timestamp.now()),
                new Member(null, "Omar Khelifi", "omar.khelifi@clubsync.tn", "24112233", "Recrue", Constants.DEPT_TECH, Constants.STATUS_PENDING, "qr-omar-05", Timestamp.now()),
                new Member(null, "Nour Ayari", "nour.ayari@clubsync.tn", "25556677", "Chargée RH", Constants.DEPT_RH, Constants.STATUS_ACTIVE, "qr-nour-06", Timestamp.now())
        };

        for (Member m : demoMembers) {
            addMember(m, null);
        }

        // 2 Événements conformes à la maquette
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        cal1.add(java.util.Calendar.DAY_OF_YEAR, 2);
        cal1.set(java.util.Calendar.HOUR_OF_DAY, 18);
        cal1.set(java.util.Calendar.MINUTE, 0);

        Event event1 = new Event(null, "Réunion hebdomadaire", "Points sur les projets en cours et accueil des recrues",
                "Salle B12", new Timestamp(cal1.getTime()), 30, 8, 4, currentUid);
        addEvent(event1, null);

        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal2.add(java.util.Calendar.DAY_OF_YEAR, 7);
        cal2.set(java.util.Calendar.HOUR_OF_DAY, 16);
        cal2.set(java.util.Calendar.MINUTE, 30);

        Event event2 = new Event(null, "Atelier Tech Android", "Prise en main de Firebase AI Logic et CameraX",
                "Labo informatique", new Timestamp(cal2.getTime()), 20, 12, 6, currentUid);
        addEvent(event2, null);

        if (callback != null) {
            callback.onSuccess(null);
        }
    }
}
