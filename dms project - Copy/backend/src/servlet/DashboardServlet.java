package com.disaster.servlet;

import com.disaster.dao.AlertDAO;
import com.disaster.dao.IncidentDAO;
import com.disaster.dao.ResourceDAO;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

@WebServlet("/DashboardServlet")
public class DashboardServlet extends HttpServlet {

    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final AlertDAO    alertDAO    = new AlertDAO();

    private void setCORS(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET,OPTIONS");
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
            int activeIncidents  = incidentDAO.countActive();
            int resolvedToday    = incidentDAO.countResolvedToday();
            int resourcesDeployed= resourceDAO.countDeployed();
            int alertsSent       = alertDAO.countSentToday();
            int activeAlerts     = alertDAO.countActive();

            String json = String.format(
                "{\"activeIncidents\":%d,\"resolvedToday\":%d,\"resourcesDeployed\":%d," +
                "\"alertsSent\":%d,\"activeAlerts\":%d}",
                activeIncidents, resolvedToday, resourcesDeployed, alertsSent, activeAlerts
            );
            resp.getWriter().write(json);
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}