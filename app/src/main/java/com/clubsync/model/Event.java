package com.clubsync.model;

import com.clubsync.util.DateUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Event {
    private String id;
    private String title;
    private String description;
    private String location;
    private Timestamp eventDate;
    private int capacity;
    private int registeredCount;
    private int attendeesCount;
    private String createdBy;

    public Event() {
        // Constructeur vide requis pour Firestore
    }

    public Event(String id, String title, String description, String location,
                 Timestamp eventDate, int capacity, int registeredCount, int attendeesCount, String createdBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.capacity = capacity;
        this.registeredCount = registeredCount;
        this.attendeesCount = attendeesCount;
        this.createdBy = createdBy;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Timestamp getEventDate() {
        return eventDate;
    }

    public void setEventDate(Timestamp eventDate) {
        this.eventDate = eventDate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(int registeredCount) {
        this.registeredCount = registeredCount;
    }

    public int getAttendeesCount() {
        return attendeesCount;
    }

    public void setAttendeesCount(int attendeesCount) {
        this.attendeesCount = attendeesCount;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Exclude
    public boolean isFull() {
        return capacity > 0 && registeredCount >= capacity;
    }

    @Exclude
    public int getRemainingPlaces() {
        return Math.max(0, capacity - registeredCount);
    }

    @Exclude
    public String getFormattedDate() {
        return DateUtils.formatDateTime(eventDate);
    }

    @Exclude
    public String getDay() {
        return DateUtils.getDay(eventDate);
    }

    @Exclude
    public String getMonth() {
        return DateUtils.getMonthShort(eventDate);
    }

    @Exclude
    public String getTime() {
        return DateUtils.getTime(eventDate);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("description", description);
        map.put("location", location);
        map.put("eventDate", eventDate);
        map.put("capacity", capacity);
        map.put("registeredCount", registeredCount);
        map.put("attendeesCount", attendeesCount);
        map.put("createdBy", createdBy);
        return map;
    }
}
