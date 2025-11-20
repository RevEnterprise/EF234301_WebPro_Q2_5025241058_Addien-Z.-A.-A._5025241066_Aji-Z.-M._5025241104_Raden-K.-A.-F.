package com.example.servlet;

import com.example.dao.TransactionDAO;
import com.example.dao.TransactionModel;
import com.example.dao.UserDAO; 
import com.example.dao.DashboardData; 
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/transactions")
public class TransactionServlet extends HttpServlet {
    private TransactionDAO transactionDAO;
    private UserDAO userDAO; 

    @Override
    public void init() throws ServletException {
        this.transactionDAO = (TransactionDAO) getServletContext().getAttribute("transactionDAO");
        if (this.transactionDAO == null) {
            throw new ServletException("TransactionDAO not found in ServletContext.");
        }
        
        Object daoObject = getServletContext().getAttribute("userDAO");
        if (daoObject instanceof UserDAO) {
            this.userDAO = (UserDAO) daoObject;
            System.out.println("TransactionServlet: Successfully retrieved initialized UserDAO.");
        } else {
            throw new ServletException("UserDAO not found or not initialized in ServletContext.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        
        try {
            List<TransactionModel> transactions = transactionDAO.getAllTransactionsByUser(userId);
            
            DashboardData dashboardData = userDAO.getDashboardDataById(userId); 
            
            String currencyCode = "USD";
            if (dashboardData != null && dashboardData.currencyCode != null) {
                currencyCode = dashboardData.currencyCode;
            } else {
                System.out.println("Warning: DashboardData or Currency Code not found for userId: " + userId + ". Defaulting to " + currencyCode);
            }
            
            request.setAttribute("transactions", transactions);
            request.setAttribute("currencyCode", currencyCode); 
            
            request.getRequestDispatcher("/transactions.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Error fetching transaction or user data: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("error.jsp?msg=Could not load transactions due to a server error.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        
        try {
            String name = request.getParameter("name");
            String amountStr = request.getParameter("amount");
            String categoryIdStr = request.getParameter("category_id");
            String dateStr = request.getParameter("transaction_date");
            
            double amount = Double.parseDouble(amountStr);
            Integer categoryId = null;
            if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
                categoryId = Integer.parseInt(categoryIdStr);
            }
            
            String transactionDate;
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                transactionDate = dateStr; 
            } else {
                transactionDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            }

            TransactionModel newTransaction = new TransactionModel(userId, name, categoryId, amount, transactionDate);

            boolean success = transactionDAO.saveNewTransaction(newTransaction);

            if (success) {
                double amountToUpdate = amount * -1.0; 
                userDAO.incrementCurrentOverallSpending(userId, amountToUpdate);

                request.getSession().setAttribute("message", "Transaction added successfully and spending updated!");
            } else {
                request.getSession().setAttribute("error", "Failed to add transaction.");
            }

        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Invalid amount or category ID format.");
        } catch (Exception e) {
            request.getSession().setAttribute("error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/transactions");
    }
}
