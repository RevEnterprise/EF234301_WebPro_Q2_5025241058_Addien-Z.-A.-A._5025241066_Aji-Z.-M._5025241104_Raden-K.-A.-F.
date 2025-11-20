<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Register</title>
    <link rel="stylesheet" href="CSS/style.css">
</head>
<body>

<div class="auth-container">

    <div class="auth-card">
        <h1 class="auth-title">Create your account</h1>
        <p class="auth-desc">Fill the fields below to register</p>

        <% if (request.getAttribute("error") != null) { %>
            <p class="msg-error"><%= request.getAttribute("error") %></p>
        <% } %>

        <form method="POST" action="auth" class="form">
            <input type="hidden" name="action" value="register">

            <div class="field">
                <label>Username</label>
                <input type="text" name="username" required placeholder="Username">
            </div>

            <div class="field">
                <label>Password</label>
                <input type="password" name="password" required placeholder="Password">
            </div>

            <button type="submit" class="btn-primary">Register</button>
        </form>

        <div class="bottom-text">
            <span>Already have an account?</span>
            <a href="login.jsp">Log in</a>
        </div>
    </div>

</div>

</body>
</html>
