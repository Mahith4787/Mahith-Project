package com.disaster.dao;

import com.disaster.model.Resource;
import com.disaster.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {

    public int create(Resource r) throws SQLException {
        String sql = "INSERT INTO resources (name, type, quantity, location, status, incident_id) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getName());
            ps.setString(2, r.getType());
            ps.setInt(3, r.getQuantity());
            ps.setString(4, r.getLocation());
            ps.setString(5, r.getStatus() != null ? r.getStatus() : "AVAILABLE");
            if (r.getIncidentId() != null) ps.setInt(6, r.getIncidentId());
            else ps.setNull(6, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public List<Resource> getAll() throws SQLException {
        return getByFilter(null, null);
    }

    public List<Resource> getByFilter(String type, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM resources WHERE 1=1");
        if (type   != null && !type.isEmpty())   sql.append(" AND type = '").append(type).append("'");
        if (status != null && !status.isEmpty()) sql.append(" AND status = '").append(status).append("'");
        sql.append(" ORDER BY created_at DESC");

        List<Resource> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql.toString())) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public boolean updateStatus(int id, String status, Integer incidentId) throws SQLException {
        String sql = "UPDATE resources SET status = ?, incident_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (incidentId != null) ps.setInt(2, incidentId);
            else ps.setNull(2, Types.INTEGER);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM resources WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int countDeployed() throws SQLException {
        String sql = "SELECT COUNT(*) FROM resources WHERE status = 'DEPLOYED'";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Resource map(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setId(rs.getInt("id"));
        r.setName(rs.getString("name"));
        r.setType(rs.getString("type"));
        r.setQuantity(rs.getInt("quantity"));
        r.setLocation(rs.getString("location"));
        r.setStatus(rs.getString("status"));
        int incId = rs.getInt("incident_id");
        r.setIncidentId(rs.wasNull() ? null : incId);
        r.setCreatedAt(rs.getTimestamp("created_at"));
        return r;
    }
}