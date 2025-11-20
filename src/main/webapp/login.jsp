<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
    <link rel="stylesheet" href="CSS/style.css">
</head>
<body>

<div class="auth-container">

    <div class="auth-card">
        <h1 class="auth-title">Log in to your account</h1>
        <p class="auth-desc">Enter your username and password below to log in</p>

        <% if (request.getParameter("msg") != null) { %>
            <p class="msg-success"><%= request.getParameter("msg") %></p>
        <% } %>

        <% if (request.getAttribute("error") != null) { %>
            <p class="msg-error"><%= request.getAttribute("error") %></p>
        <% } %>

        <form method="POST" action="auth" class="form">
            <input type="hidden" name="action" value="login">

            <div class="field">
                <label>Username</label>
                <input type="text" name="username" required placeholder="Username">
            </div>

            <div class="field">
                <label>Password</label>
                <input type="password" name="password" required placeholder="Password">
            </div>

            <button type="submit" class="btn-primary">Log in</button>
        </form>

        <div class="bottom-text">
            <span>Don't have an account?</span>
            <a href="register.jsp">Sign up</a>
        </div>
    </div>

</div>

</body>
</html>
