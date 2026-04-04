package com.disaster.model;

import java.sql.Timestamp;

public class Alert {
    private int id;
    private String title;
    private String level;      // INFO, WARNING, DANGER, EVACUATION
    private String region;
    private String message;
    private String status;     // ACTIVE, RESOLVED
    private Integer incidentId;
    private Timestamp sentAt;
    private Timestamp expiresAt;

    public Alert() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getIncidentId() { return incidentId; }
    public void setIncidentId(Integer incidentId) { this.incidentId = incidentId; }

    public Timestamp getSentAt() { return sentAt; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }

    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }

    public String toJSON() {
        return String.format(
            "{\"id\":%d,\"title\":\"%s\",\"level\":\"%s\",\"region\":\"%s\"," +
            "\"message\":\"%s\",\"status\":\"%s\",\"incidentId\":%s," +
            "\"sentAt\":\"%s\",\"expiresAt\":\"%s\"}",
            id, escape(title), escape(level), escape(region),
            escape(message), escape(status),
            incidentId != null ? incidentId.toString() : "null",
            sentAt    != null ? sentAt.toString().substring(0, 16)    : "",
            expiresAt != null ? expiresAt.toString().substring(0, 16) : ""
        );
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}