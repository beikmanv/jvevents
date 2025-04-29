package com.northcoders.jvevents.model;

import java.util.List;

public class AppUserDTO {
    private Long id;
    private String username;
    private String email;
    private List<Long> eventIds;
    private String createdAt;
    private String modifiedAt;

    public AppUserDTO() {}

    public AppUserDTO(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public List<Long> getEventIds() { return eventIds; }

    public void setEventIds(List<Long> eventIds) { this.eventIds = eventIds; }

    public String getCreatedAt() { return createdAt; }

    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getModifiedAt() { return modifiedAt; }

    public void setModifiedAt(String modifiedAt) { this.modifiedAt = modifiedAt; }
}
