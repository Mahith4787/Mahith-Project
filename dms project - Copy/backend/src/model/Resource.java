package com.disaster.model;

import java.sql.Timestamp;

public class Resource {
    private int id;
    private String name;
    private String type;       // RESCUE_TEAM, VEHICLE, MEDICAL, SHELTER, FOOD, EQUIPMENT
    private int quantity;
    private String location;
    private String status;     // AVAILABLE, DEPLOYED, MAINTENANCE
    private Integer incidentId; // nullable
    private Timestamp createdAt;

    public Resource() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getIncidentId() { return incidentId; }
    public void setIncidentId(Integer incidentId) { this.incidentId = incidentId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String toJSON() {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"type\":\"%s\",\"quantity\":%d," +
            "\"location\":\"%s\",\"status\":\"%s\",\"incidentId\":%s}",
            id, escape(name), escape(type), quantity,
            escape(location), escape(status),
            incidentId != null ? incidentId.toString() : "null"
        );
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}