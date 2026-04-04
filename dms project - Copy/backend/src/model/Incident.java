package com.disaster.model;

import java.sql.Timestamp;

public class Incident {
    private int id;
    private String type;
    private String severity;
    private String location;
    private double latitude;
    private double longitude;
    private String description;
    private String reportedBy;
    private String contactNumber;
    private int deadCount;
    private int injuredCount;
    private int missingCount;
    private int displacedCount;
    private String status;   // ACTIVE, INVESTIGATING, RESOLVED, CLOSED
    private Timestamp reportedAt;
    private Timestamp updatedAt;

    // ---- Constructors ----
    public Incident() {}

    // ---- Getters & Setters ----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public int getDeadCount() { return deadCount; }
    public void setDeadCount(int deadCount) { this.deadCount = deadCount; }

    public int getInjuredCount() { return injuredCount; }
    public void setInjuredCount(int injuredCount) { this.injuredCount = injuredCount; }

    public int getMissingCount() { return missingCount; }
    public void setMissingCount(int missingCount) { this.missingCount = missingCount; }

    public int getDisplacedCount() { return displacedCount; }
    public void setDisplacedCount(int displacedCount) { this.displacedCount = displacedCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getReportedAt() { return reportedAt; }
    public void setReportedAt(Timestamp reportedAt) { this.reportedAt = reportedAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // ---- toJSON ----
    public String toJSON() {
        return String.format(
            "{\"id\":%d,\"type\":\"%s\",\"severity\":\"%s\",\"location\":\"%s\"," +
            "\"latitude\":%.4f,\"longitude\":%.4f,\"description\":\"%s\"," +
            "\"reportedBy\":\"%s\",\"contactNumber\":\"%s\"," +
            "\"deadCount\":%d,\"injuredCount\":%d,\"missingCount\":%d,\"displacedCount\":%d," +
            "\"status\":\"%s\",\"reportedAt\":\"%s\",\"updatedAt\":\"%s\"}",
            id, escape(type), escape(severity), escape(location),
            latitude, longitude, escape(description),
            escape(reportedBy), escape(contactNumber),
            deadCount, injuredCount, missingCount, displacedCount,
            escape(status),
            reportedAt != null ? reportedAt.toString().substring(0, 16) : "",
            updatedAt  != null ? updatedAt.toString().substring(0, 16)  : ""
        );
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}