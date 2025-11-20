package com.example.servlet;

import com.example.dao.UserDAO;
import com.example.dao.TransactionDAO;
import com.example.dao.DashboardData;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletConfig;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;
    private TransactionDAO transactionDAO;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        Object userDaoObject = config.getServletContext().getAttribute("userDAO");
        if (userDaoObject instanceof UserDAO) {
            this.userDAO = (UserDAO) userDaoObject;
            System.out.println("DashboardServlet: Successfully retrieved initialized UserDAO.");
        } else {
            throw new ServletException("UserDAO not found or not initialized in ServletContext.");
        }
        
        Object transactionDaoObject = config.getServletContext().getAttribute("transactionDAO");
        if (transactionDaoObject instanceof TransactionDAO) {
            this.transactionDAO = (TransactionDAO) transactionDaoObject;
            System.out.println("DashboardServlet: Successfully retrieved initialized TransactionDAO.");
        } else {
            throw new ServletException("TransactionDAO not found or not initialized in ServletContext.");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        
        Integer userId = (Integer) session.getAttribute("userId");
        String username = (String) session.getAttribute("user");
        
        if (userId == null || username == null) {
            response.sendRedirect("login.jsp?msg=Please log in to view this page.");
            return;
        }

        DashboardData data = null;
        try {
            data = userDAO.getDashboardDataById(userId);
            
            if (data == null) {
                throw new Exception("User data not found for ID: " + userId);
            }

            double currentSpend = 0.0;
            
            switch (data.limitPeriod) {
                case 1:
                    currentSpend = transactionDAO.calculateSpendingToday(userId);
                    break;
                case 2:
                    currentSpend = transactionDAO.calculateSpendingThisMonth(userId);
                    break;
                case 3:
                    currentSpend = transactionDAO.calculateSpendingThisYear(userId);
                    break;
                case 4:
                    currentSpend = transactionDAO.calculateSpendingAllTime(userId);
                    break;
                default:
                    System.err.println("Unknown limitPeriod: " + data.limitPeriod + ". Defaulting to all time spend.");
                    currentSpend = transactionDAO.calculateSpendingAllTime(userId);
                    break;
            }
            
            request.setAttribute("currentSpend", currentSpend);

            request.setAttribute("spendLimit", data.spendLimit);
            request.setAttribute("limitPeriod", data.limitPeriod);
            request.setAttribute("currencyCode", data.currencyCode);
            
            String errorMsg = request.getParameter("error");
            if (errorMsg != null) {
                request.setAttribute("errorMessage", errorMsg);
            }

            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("error.jsp?msg=Could not load dashboard data due to a server error.");
        }
    }
}
