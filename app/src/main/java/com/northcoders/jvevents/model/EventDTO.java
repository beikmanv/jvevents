package com.northcoders.jvevents.model;

import java.util.List;

public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private String eventDate; // ISO 8601 format: "2025-04-23T10:00:00"
    private String location;
    private String createdAt;
    private String modifiedAt;
    private List<AppUserDTO> users;

    public EventDTO() {}

    public EventDTO(Long id, String title, String description, String eventDate,
                    String location, String createdAt, String modifiedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    // Getters and Setters

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getEventDate() { return eventDate; }

    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public String getCreatedAt() { return createdAt; }

    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getModifiedAt() { return modifiedAt; }

    public void setModifiedAt(String modifiedAt) { this.modifiedAt = modifiedAt; }

    public List<AppUserDTO> getUsers() { return users; }

    public void setUsers(List<AppUserDTO> users) { this.users = users; }

}
