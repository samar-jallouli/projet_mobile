package com.clubsync.util;

public final class Constants {
    private Constants() {}

    // Rôles utilisateurs
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MEMBER = "MEMBRE";

    // Statuts membre
    public static final String STATUS_ACTIVE = "ACTIF";
    public static final String STATUS_PENDING = "EN_ATTENTE";

    // Départements
    public static final String DEPT_TECH = "Tech";
    public static final String DEPT_COM = "Com";
    public static final String DEPT_EVENT = "Event";
    public static final String DEPT_RH = "RH";

    // Modèle IA Gemini
    public static final String AI_MODEL = "gemini-3.5-flash";

    // Collections Firestore
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_MEMBERS = "members";
    public static final String COLLECTION_EVENTS = "events";
    public static final String COLLECTION_REGISTRATIONS = "registrations";
    public static final String COLLECTION_ATTENDANCES = "attendances";
}
