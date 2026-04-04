package com.disaster.dao;

import com.disaster.model.Alert;
import com.disaster.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {

    public int create(Alert a) throws SQLException {
        String sql = "INSERT INTO alerts (title, level, region, message, status, incident_id, expires_at) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getLevel());
            ps.setString(3, a.getRegion());
            ps.setString(4, a.getMessage());
            ps.setString(5, "ACTIVE");
            if (a.getIncidentId() != null) ps.setInt(6, a.getIncidentId());
            else ps.setNull(6, Types.INTEGER);
            if (a.getExpiresAt() != null) ps.setTimestamp(7, a.getExpiresAt());
            else ps.setNull(7, Types.TIMESTAMP);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public List<Alert> getAll() throws SQLException {
        String sql = "SELECT * FROM alerts ORDER BY sent_at DESC";
        List<Alert> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Alert> getActive() throws SQLException {
        String sql = "SELECT * FROM alerts WHERE status = 'ACTIVE' ORDER BY sent_at DESC";
        List<Alert> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE alerts SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM alerts WHERE status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countSentToday() throws SQLException {
        String sql = "SELECT COUNT(*) FROM alerts WHERE DATE(sent_at) = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Alert map(ResultSet rs) throws SQLException {
        Alert a = new Alert();
        a.setId(rs.getInt("id"));
        a.setTitle(rs.getString("title"));
        a.setLevel(rs.getString("level"));
        a.setRegion(rs.getString("region"));
        a.setMessage(rs.getString("message"));
        a.setStatus(rs.getString("status"));
        int incId = rs.getInt("incident_id");
        a.setIncidentId(rs.wasNull() ? null : incId);
        a.setSentAt(rs.getTimestamp("sent_at"));
        a.setExpiresAt(rs.getTimestamp("expires_at"));
        return a;
    }
}