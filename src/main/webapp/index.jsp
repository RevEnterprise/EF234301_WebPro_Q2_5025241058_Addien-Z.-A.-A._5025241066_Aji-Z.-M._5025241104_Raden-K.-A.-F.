<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Redirecting...</title>
</head>
<body>
    <%
        String loginPage = "login.jsp";
        response.sendRedirect(loginPage);
    %>
    <h1>Redirecting to Login Page...</h1>
</body>
</html>
