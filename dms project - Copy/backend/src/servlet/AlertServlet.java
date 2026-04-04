package com.disaster.servlet;

import com.disaster.dao.AlertDAO;
import com.disaster.model.Alert;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/AlertServlet")
public class AlertServlet extends HttpServlet {

    private final AlertDAO dao = new AlertDAO();

    private void setCORS(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp); resp.setStatus(200);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp);
        try {
            String filter = req.getParameter("status");
            List<Alert> list = "ACTIVE".equals(filter) ? dao.getActive() : dao.getAll();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i).toJSON());
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            resp.getWriter().write(sb.toString());
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp);
        try {
            String body = req.getReader().lines().collect(Collectors.joining());
            JSONObject json = new JSONObject(body);
            Alert a = new Alert();
            a.setTitle(json.optString("title"));
            a.setLevel(json.optString("level", "INFO"));
            a.setRegion(json.optString("region"));
            a.setMessage(json.optString("message"));
            a.setIncidentId(json.isNull("incidentId") ? null : json.optInt("incidentId"));
            String expires = json.optString("expiresAt", null);
            if (expires != null && !expires.isEmpty()) {
                a.setExpiresAt(Timestamp.valueOf(expires.replace("T", " ") + ":00"));
            }
            int id = dao.create(a);
            resp.setStatus(201);
            resp.getWriter().write("{\"id\":" + id + "}");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp);
        try {
            String body = req.getReader().lines().collect(Collectors.joining());
            JSONObject json = new JSONObject(body);
            dao.updateStatus(json.getInt("id"), json.getString("status"));
            resp.getWriter().write("{\"message\":\"Updated\"}");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}