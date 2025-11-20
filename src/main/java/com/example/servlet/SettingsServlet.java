package com.example.servlet;

import com.example.dao.UserDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletConfig;

@WebServlet("/settings")
public class SettingsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        Object daoObject = config.getServletContext().getAttribute("userDAO");
        
        if (daoObject instanceof UserDAO) {
            this.userDAO = (UserDAO) daoObject;
            System.out.println("SettingsServlet: Successfully retrieved initialized UserDAO.");
        } else {
            throw new ServletException("UserDAO not found or not initialized in ServletContext.");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        Integer userId = (Integer) request.getSession().getAttribute("userId");

        if (userId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        if ("updateSpendSettings".equals(action)) {
            try {
                double newLimit = Double.parseDouble(request.getParameter("newLimit"));
                int newPeriod = Integer.parseInt(request.getParameter("period"));
                String newCurrency = request.getParameter("currency").toUpperCase();
                
                userDAO.updateOverallSpendLimit(userId, newLimit);
                userDAO.updateSpendPeriodSetting(userId, newPeriod);
                userDAO.updateCurrencySetting(userId, newCurrency);
                
                response.sendRedirect("dashboard?msg=Settings updated successfully!");
                
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect("dashboard?error=Error updating settings: " + e.getMessage());
            }
        } else {
            response.sendRedirect("dashboard?error=Invalid action specified.");
        }
    }
}
