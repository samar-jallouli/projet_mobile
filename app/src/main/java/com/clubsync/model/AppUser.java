package com.clubsync.model;

import com.clubsync.util.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

public class AppUser {
    private String id;
    private String fullName;
    private String email;
    private String role; // ADMIN | MEMBRE
    private String memberId;
    private Timestamp createdAt;

    public AppUser() {
        // Constructeur vide requis pour Firestore
    }

    public AppUser(String id, String fullName, String email, String role, String memberId, Timestamp createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.memberId = memberId;
        this.createdAt = createdAt;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Exclude
    public boolean isAdmin() {
        return Constants.ROLE_ADMIN.equalsIgnoreCase(role);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("fullName", fullName);
        map.put("email", email);
        map.put("role", role);
        map.put("memberId", memberId);
        map.put("createdAt", createdAt);
        return map;
    }
}
