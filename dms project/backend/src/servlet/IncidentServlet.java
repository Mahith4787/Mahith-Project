package com.disaster.servlet;

import com.disaster.dao.IncidentDAO;
import com.disaster.model.Incident;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/IncidentServlet")
public class IncidentServlet extends HttpServlet {

    private final IncidentDAO dao = new IncidentDAO();

    private void setCORS(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp);
        resp.setStatus(200);
    }

    // GET — list incidents
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp);
        try {
            int limit = 0;
            String limitParam = req.getParameter("limit");
            if (limitParam != null) limit = Integer.parseInt(limitParam);

            String status = req.getParameter("status");
            List<Incident> list = status != null ? dao.getByStatus(status) : dao.getAll(limit);

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

    // POST — create incident
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp);
        try {
            String body = req.getReader().lines().collect(Collectors.joining());
            JSONObject json = new JSONObject(body);

            Incident inc = new Incident();
            inc.setType(json.optString("type"));
            inc.setSeverity(json.optString("severity"));
            inc.setLocation(json.optString("location"));
            inc.setLatitude(json.optDouble("lat", 0.0));
            inc.setLongitude(json.optDouble("lng", 0.0));
            inc.setDescription(json.optString("description"));
            inc.setReportedBy(json.optString("reporter"));
            inc.setContactNumber(json.optString("contact"));
            inc.setDeadCount(json.optInt("dead", 0));
            inc.setInjuredCount(json.optInt("injured", 0));
            inc.setMissingCount(json.optInt("missing", 0));
            inc.setDisplacedCount(json.optInt("displaced", 0));
            inc.setStatus("ACTIVE");

            int id = dao.create(inc);
            resp.setStatus(201);
            resp.getWriter().write("{\"id\":" + id + ",\"message\":\"Incident created\"}");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // PUT — update status
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp);
        try {
            String body = req.getReader().lines().collect(Collectors.joining());
            JSONObject json = new JSONObject(body);
            int id = json.getInt("id");
            String status = json.getString("status");
            boolean ok = dao.updateStatus(id, status);
            resp.getWriter().write(ok ? "{\"message\":\"Updated\"}" : "{\"error\":\"Not found\"}");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // DELETE
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp);
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            boolean ok = dao.delete(id);
            resp.getWriter().write(ok ? "{\"message\":\"Deleted\"}" : "{\"error\":\"Not found\"}");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}