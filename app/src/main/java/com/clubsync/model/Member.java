package com.clubsync.model;

import com.clubsync.util.Constants;
import com.clubsync.util.DateUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Member {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String department;
    private String status; // ACTIF | EN_ATTENTE
    private String qrToken;
    private Timestamp joinedAt;

    public Member() {
        // Constructeur vide requis pour Firestore
    }

    public Member(String id, String fullName, String email, String phone, String role,
                  String department, String status, String qrToken, Timestamp joinedAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.department = department;
        this.status = status;
        this.qrToken = qrToken;
        this.joinedAt = joinedAt;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }

    @Exclude
    public boolean isPending() {
        return Constants.STATUS_PENDING.equalsIgnoreCase(status);
    }

    @Exclude
    public String getFormattedDate() {
        return DateUtils.formatDateTime(joinedAt);
    }

    @Exclude
    public String getInitials() {
        if (fullName == null || fullName.trim().isEmpty()) return "CS";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("fullName", fullName);
        map.put("email", email);
        map.put("phone", phone);
        map.put("role", role);
        map.put("department", department);
        map.put("status", status);
        map.put("qrToken", qrToken);
        map.put("joinedAt", joinedAt);
        return map;
    }
}
