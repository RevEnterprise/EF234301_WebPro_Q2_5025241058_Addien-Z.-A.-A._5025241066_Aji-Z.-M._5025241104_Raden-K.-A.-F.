<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect("login.jsp?msg=Please log in first.");
        return;
    }
    String username = (String) session.getAttribute("user");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Edit Profile</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 p-6">

<div class="max-w-md mx-auto bg-white p-6 rounded shadow space-y-6">
    <div>
        <h2 class="text-xl font-bold mb-4">Edit Profile</h2>
        <form action="auth" method="POST" class="flex flex-col gap-4">
            <input type="hidden" name="action" value="updateProfile">

            <label>Username</label>
            <input type="text" name="username" value="<%= username %>" class="border p-2 rounded">

            <label>New Password</label>
            <input type="password" name="password" placeholder="Leave blank to keep current password" class="border p-2 rounded">

            <button type="submit" class="bg-blue-600 text-white py-2 rounded hover:bg-blue-700">
                Save Changes
            </button>
        </form>
    </div>

    <div class="mt-6 border-t pt-6">
        <h2 class="text-xl font-bold mb-2 text-red-600">Delete Account</h2>
        <p class="text-sm text-gray-600 mb-4">
            Once you delete your account, all data will be permanently removed. This action cannot be undone.
        </p>
        <form action="auth" method="POST">
            <input type="hidden" name="action" value="deleteUser">
            <button type="submit" class="w-full bg-red-600 text-white py-2 rounded hover:bg-red-700">
                Delete Account
            </button>
        </form>
    </div>

    <a href="dashboard.jsp" class="text-blue-600 block mt-4 hover:underline">Back to Dashboard</a>
</div>

</body>
</html>
