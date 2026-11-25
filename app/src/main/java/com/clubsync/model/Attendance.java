package com.clubsync.model;

import com.clubsync.util.DateUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Attendance {
    private String id; // eventId_memberId
    private String eventId;
    private String memberId;
    private String memberName;
    private Timestamp scannedAt;
    private String verifiedBy;

    public Attendance() {
        // Constructeur vide requis pour Firestore
    }

    public Attendance(String id, String eventId, String memberId, String memberName, Timestamp scannedAt, String verifiedBy) {
        this.id = id;
        this.eventId = eventId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.scannedAt = scannedAt;
        this.verifiedBy = verifiedBy;
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

    public Timestamp getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(Timestamp scannedAt) {
        this.scannedAt = scannedAt;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    @Exclude
    public String getFormattedDate() {
        return DateUtils.formatDateTime(scannedAt);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("eventId", eventId);
        map.put("memberId", memberId);
        map.put("memberName", memberName);
        map.put("scannedAt", scannedAt);
        map.put("verifiedBy", verifiedBy);
        return map;
    }
}
