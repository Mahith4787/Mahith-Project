package com.disaster.dao;

import com.disaster.model.Incident;
import com.disaster.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncidentDAO {

    // ---- CREATE ----
    public int create(Incident inc) throws SQLException {
        String sql = "INSERT INTO incidents (type, severity, location, latitude, longitude, " +
                     "description, reported_by, contact_number, dead_count, injured_count, " +
                     "missing_count, displaced_count, status) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, inc.getType());
            ps.setString(2, inc.getSeverity());
            ps.setString(3, inc.getLocation());
            ps.setDouble(4, inc.getLatitude());
            ps.setDouble(5, inc.getLongitude());
            ps.setString(6, inc.getDescription());
            ps.setString(7, inc.getReportedBy());
            ps.setString(8, inc.getContactNumber());
            ps.setInt(9,  inc.getDeadCount());
            ps.setInt(10, inc.getInjuredCount());
            ps.setInt(11, inc.getMissingCount());
            ps.setInt(12, inc.getDisplacedCount());
            ps.setString(13, inc.getStatus() != null ? inc.getStatus() : "ACTIVE");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    // ---- READ ALL ----
    public List<Incident> getAll(int limit) throws SQLException {
        String sql = "SELECT * FROM incidents ORDER BY reported_at DESC" + (limit > 0 ? " LIMIT " + limit : "");
        List<Incident> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ---- READ BY ID ----
    public Incident getById(int id) throws SQLException {
        String sql = "SELECT * FROM incidents WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    // ---- READ BY STATUS ----
    public List<Incident> getByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM incidents WHERE status = ? ORDER BY reported_at DESC";
        List<Incident> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // ---- UPDATE STATUS ----
    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE incidents SET status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ---- DELETE ----
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM incidents WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ---- COUNT ACTIVE ----
    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM incidents WHERE status IN ('ACTIVE','INVESTIGATING')";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ---- COUNT RESOLVED TODAY ----
    public int countResolvedToday() throws SQLException {
        String sql = "SELECT COUNT(*) FROM incidents WHERE status = 'RESOLVED' AND DATE(updated_at) = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ---- MAPPER ----
    private Incident map(ResultSet rs) throws SQLException {
        Incident i = new Incident();
        i.setId(rs.getInt("id"));
        i.setType(rs.getString("type"));
        i.setSeverity(rs.getString("severity"));
        i.setLocation(rs.getString("location"));
        i.setLatitude(rs.getDouble("latitude"));
        i.setLongitude(rs.getDouble("longitude"));
        i.setDescription(rs.getString("description"));
        i.setReportedBy(rs.getString("reported_by"));
        i.setContactNumber(rs.getString("contact_number"));
        i.setDeadCount(rs.getInt("dead_count"));
        i.setInjuredCount(rs.getInt("injured_count"));
        i.setMissingCount(rs.getInt("missing_count"));
        i.setDisplacedCount(rs.getInt("displaced_count"));
        i.setStatus(rs.getString("status"));
        i.setReportedAt(rs.getTimestamp("reported_at"));
        i.setUpdatedAt(rs.getTimestamp("updated_at"));
        return i;
    }
}