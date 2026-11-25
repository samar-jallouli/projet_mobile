package com.clubsync.model;

import com.clubsync.util.DateUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Registration {
    private String id; // eventId_memberId
    private String eventId;
    private String memberId;
    private String memberName;
    private Timestamp registeredAt;

    public Registration() {
        // Constructeur vide requis pour Firestore
    }

    public Registration(String id, String eventId, String memberId, String memberName, Timestamp registeredAt) {
        this.id = id;
        this.eventId = eventId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.registeredAt = registeredAt;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Exclude
    public String getFormattedDate() {
        return DateUtils.formatDateTime(registeredAt);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("eventId", eventId);
        map.put("memberId", memberId);
        map.put("memberName", memberName);
        map.put("registeredAt", registeredAt);
        return map;
    }
}
