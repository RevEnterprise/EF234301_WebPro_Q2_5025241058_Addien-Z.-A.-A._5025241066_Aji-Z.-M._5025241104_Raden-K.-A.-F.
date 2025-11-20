package com.example.servlet;

import com.example.dao.UserDAO;
import com.example.dao.TransactionDAO;
import com.example.dao.CategoryDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletContext;

import java.io.File;

@WebServlet(urlPatterns = "/init", loadOnStartup = 1)
public class DatabaseInitServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void init() {
        ServletContext context = getServletContext();
        
        String webAppRootPath = context.getRealPath("/");
        String dbDirPath = webAppRootPath + "WEB-INF" + File.separator + "db" + File.separator;

        File dbDir = new File(dbDirPath);
        
        if (!dbDir.exists()) {
            if (dbDir.mkdirs()) {
                System.out.println("Created database directory: " + dbDirPath);
            } else {
                System.err.println("CRITICAL: Failed to create database directory: " + dbDirPath);
                
                throw new RuntimeException("Application startup failed: Cannot create DB storage directory.");
            }
        } else {
            System.out.println("Directory already exists: " + dbDirPath);
        }
        
        UserDAO userDAO = new UserDAO();
        userDAO.init(dbDirPath);
        context.setAttribute("userDAO", userDAO);

        CategoryDAO categoryDAO = new CategoryDAO(userDAO.getDbUrl());
        categoryDAO.init();
        context.setAttribute("categoryDAO", categoryDAO);
        
        TransactionDAO transactionDAO = new TransactionDAO(userDAO.getDbUrl());
        transactionDAO.init();
        context.setAttribute("transactionDAO", transactionDAO);
        
        System.out.println("Application Initialization Complete. Both DAOs available.");
    }
    
}
