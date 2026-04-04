package com.disaster.servlet;

import com.disaster.dao.ResourceDAO;
import com.disaster.model.Resource;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/ResourceServlet")
public class ResourceServlet extends HttpServlet {

    private final ResourceDAO dao = new ResourceDAO();

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
            String type   = req.getParameter("type");
            String status = req.getParameter("status");
            List<Resource> list = dao.getByFilter(type, status);
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
            Resource r = new Resource();
            r.setName(json.optString("name"));
            r.setType(json.optString("type"));
            r.setQuantity(json.optInt("quantity", 1));
            r.setLocation(json.optString("location"));
            r.setStatus(json.optString("status", "AVAILABLE"));
            r.setIncidentId(json.isNull("incidentId") ? null : json.optInt("incidentId"));
            int id = dao.create(r);
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
            int id = json.getInt("id");
            String status = json.getString("status");
            Integer incidentId = json.isNull("incidentId") ? null : json.optInt("incidentId");
            dao.updateStatus(id, status, incidentId);
            resp.getWriter().write("{\"message\":\"Updated\"}");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp);
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            dao.delete(id);
            resp.getWriter().write("{\"message\":\"Deleted\"}");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}