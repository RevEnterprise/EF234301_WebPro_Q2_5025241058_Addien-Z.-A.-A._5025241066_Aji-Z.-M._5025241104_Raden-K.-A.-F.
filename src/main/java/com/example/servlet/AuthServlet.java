package com.example.servlet;

import com.example.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = (UserDAO) getServletContext().getAttribute("userDAO");
        
        if (userDAO == null) {
            System.err.println("CRITICAL: UserDAO not found in ServletContext. Check AppInitServlet.");
            throw new ServletException("Application not initialized correctly. Missing UserDAO.");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(false);

        if ("updateProfile".equals(action) && session != null) {
            String oldUsername = (String) session.getAttribute("user");
            String newUsername = request.getParameter("username");
            String newPassword = request.getParameter("password");

            if (newUsername != null && !newUsername.isEmpty()) {
                userDAO.updateUsername(oldUsername, newUsername);
                session.setAttribute("user", newUsername);
            }
            if (newPassword != null && !newPassword.isEmpty()) {
                userDAO.updatePassword(newUsername != null ? newUsername : oldUsername, newPassword);
            }
            response.sendRedirect("profile.jsp?msg=Profile updated successfully");
        }

        else if ("deleteUser".equals(action) && session != null) {
            String username = (String) session.getAttribute("user");
            userDAO.deleteUser(username);
            session.invalidate();
            response.sendRedirect("login.jsp?msg=Account deleted successfully");
        }

        else if ("register".equals(action)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            if (userDAO.registerUser(username, password)) {
                response.sendRedirect("login.jsp?msg=Registration successful! Please log in.");
            } else {
                request.setAttribute("error", "Registration failed. Username may already exist.");
                request.getRequestDispatcher("register.jsp").forward(request, response);
            }
        }

        else if ("login".equals(action)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            int userId = userDAO.validateUser(username, password);

            if (userId > 0) {
                session.setAttribute("user", username);
                session.setAttribute("userId", userId); 
                response.sendRedirect("dashboard.jsp");
            } else {
                request.setAttribute("error", "Invalid username or password.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }
        }
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
           throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("login.jsp?msg=You have been logged out.");
    }
}
