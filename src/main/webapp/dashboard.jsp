<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%
    Double spendLimitObj = (Double) request.getAttribute("spendLimit");
    Double currentSpendObj = (Double) request.getAttribute("currentSpend");
    Integer limitPeriodObj = (Integer) request.getAttribute("limitPeriod");
    String currencyCode = (String) request.getAttribute("currencyCode");
    String username = (String) session.getAttribute("user");
    Integer userId = (Integer) session.getAttribute("userId");
    
    double spendLimit = spendLimitObj != null ? spendLimitObj : 0.0;
    double currentSpend = currentSpendObj != null ? currentSpendObj : 0.0;
    int limitPeriod = limitPeriodObj != null ? limitPeriodObj : 1;
    if (currencyCode == null) currencyCode = "USD";
    if (username == null) username = "Guest";
    
    Locale locale = new Locale("", currencyCode); 
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
    
    String formattedSpendLimit = currencyFormat.format(spendLimit);
    String formattedCurrentSpend = currencyFormat.format(currentSpend);
    
    String periodText = "Unknown Period";
    switch (limitPeriod) {
        case 1: periodText = "per Day"; break;
        case 2: periodText = "per Month"; break;
        case 3: periodText = "per Year"; break;
        case 4: periodText = "All Time"; break;
    }
    
    double remaining = spendLimit - currentSpend;
    String formattedRemaining = currencyFormat.format(remaining);
    String remainingColor = (remaining < 0) ? "text-red-600" : "text-gray-900";
    
    double percentSpent = (spendLimit > 0) ? (currentSpend / spendLimit) * 100 : 0;
    if (percentSpent > 100) percentSpent = 100;
    
    String barColor = "bg-green-500";
    if (percentSpent > 75) barColor = "bg-yellow-500";
    if (percentSpent > 90) barColor = "bg-red-500";
%>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        .sidebar {
            width: 250px;
            height: 100vh;
            position: fixed;
            padding: 20px;
            background-color: #ffffff; 
            box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
        }
        .content {
            margin-left: 250px; 
            padding: 20px;
        }
        .header {
            margin-bottom: 30px;
        }
        .profile-menu {
            margin-top: 50px;
            padding-top: 20px;
            border-top: 1px solid #ccc;
        }
        .modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            display: none; 
            justify-content: center;
            align-items: center;
            z-index: 1000;
        }
        .modal-content {
            background-color: white;
            padding: 30px;
            border-radius: 8px;
            width: 90%;
            max-width: 500px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }
    </style>
    <link rel="stylesheet" href="CSS/style.css">
</head>

<body class="bg-gray-100">

    <div class="sidebar">
        <h2 class="text-xl font-bold mb-4">Menu</h2>

        <a href="dashboard" class="block py-2 text-blue-600 font-semibold">Dashboard</a>
        <a href="transactions" class="block py-2 hover:text-blue-600">Transactions</a>

        <div class="profile-menu">
            <div class="mb-2 text-gray-700 font-semibold">
                <%= username %>
            </div>

            <details class="w-full">
                <summary class="cursor-pointer py-2 px-1 bg-gray-200 hover:bg-gray-300 rounded">
                    Profile Options
                </summary>

                <div class="mt-2 ml-2">
                    <a href="profile.jsp" class="block py-1 text-blue-600 hover:underline">View Profile</a>

                    <form action="auth" method="GET">
                        <input type="hidden" name="action" value="logout">
                        <button class="mt-2 w-full bg-red-500 text-white py-1 rounded hover:bg-red-600">
                            Logout
                        </button>
                    </form>
                </div>
            </details>
        </div>
    </div>

    <div class="content">

        <div class="header flex justify-between items-center">
            <h1 class="text-3xl font-bold text-gray-800">Financial Dashboard</h1>
            <span class="text-gray-600">Logged in as: <b><%= username %></b></span>
        </div>

        <hr class="mb-6 border-gray-300">

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">

            <div class="bg-white p-6 rounded-lg shadow-md border-l-4 border-blue-500">
                <p class="text-sm font-medium text-gray-500">Current Spending</p>
                <p class="text-3xl font-bold text-gray-900 mt-1"><%= formattedCurrentSpend %></p>
                <p class="text-sm text-gray-400 mt-2">Spent this period</p>
            </div>

            <div class="bg-white p-6 rounded-lg shadow-md border-l-4 border-green-500">
                <p class="text-sm font-medium text-gray-500">Spending Limit</p>
                <p class="text-3xl font-bold text-gray-900 mt-1"><%= formattedSpendLimit %></p>
                <p class="text-sm text-gray-400 mt-2"><%= periodText %> (<%= currencyCode %>)</p>
            </div>

            <div class="bg-white p-6 rounded-lg shadow-md border-l-4 border-purple-500">
                <p class="text-sm font-medium text-gray-500">Funds Remaining</p>
                <p class="text-3xl font-bold mt-1 <%= remainingColor %>"><%= formattedRemaining %></p>
                <p class="text-sm text-gray-400 mt-2"><%= periodText %></p>
            </div>
        </div>
        
        <div class="mt-8 bg-white p-6 rounded-lg shadow-xl">
            <h2 class="text-xl font-semibold mb-4 text-gray-700">Limit Progress</h2>
            
            <div class="flex justify-between mb-1">
                <span class="text-base font-medium text-blue-700">
                    <%= String.format("%.1f", percentSpent) %>% Spent
                </span>
                <span class="text-sm font-medium text-gray-700">
                    <%= formattedCurrentSpend %> / <%= formattedSpendLimit %>
                </span>
            </div>
            
            <div class="w-full bg-gray-200 rounded-full h-2.5">
                <div class="<%= barColor %> h-2.5 rounded-full" style="width: <%= String.format("%.1f", percentSpent) %>%"></div>
            </div>
            
            <button onclick="document.getElementById('limitModal').style.display='flex';"
                    class="mt-6 px-4 py-2 bg-blue-500 text-white font-semibold rounded-lg shadow-md hover:bg-blue-600 transition duration-200">
                Change Spending Limit & Settings
            </button>
        </div>
    </div>

    <div id="limitModal" class="modal-overlay">
        <div class="modal-content">
            <h3 class="text-2xl font-bold mb-4">Update Spending Settings</h3>

            <form action="settings" method="POST" class="space-y-4">
                <input type="hidden" name="action" value="updateSpendSettings">
                <input type="hidden" name="userId" value="<%= userId %>">
                
                <div>
                    <label for="newLimit" class="block text-sm font-medium text-gray-700">New Spend Limit (<%= currencyCode %>)</label>
                    <input type="number" id="newLimit" name="newLimit" value="<%= String.format("%.2f", spendLimit) %>" step="0.01" required
                           class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 focus:ring-blue-500 focus:border-blue-500">
                </div>

                <div>
                    <label for="period" class="block text-sm font-medium text-gray-700">Limit Period</label>
                    <select id="period" name="period" required
                             class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 focus:ring-blue-500 focus:border-blue-500">
                        <option value="1" <%= limitPeriod == 1 ? "selected" : "" %>>1 - Per Day</option>
                        <option value="2" <%= limitPeriod == 2 ? "selected" : "" %>>2 - Per Month</option>
                        <option value="3" <%= limitPeriod == 3 ? "selected" : "" %>>3 - Per Year</option>
                        <option value="4" <%= limitPeriod == 4 ? "selected" : "" %>>4 - All Time</option>
                    </select>
                </div>
                
                <div>
                    <label for="currency" class="block text-sm font-medium text-gray-700">Currency Code (ISO 4217)</label>
                    <input type="text" id="currency" name="currency" value="<%= currencyCode %>" maxlength="3" required
                           class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 focus:ring-blue-500 focus:border-blue-500">
                </div>

                <div class="flex justify-end space-x-3 pt-4">
                    <button type="button" onclick="document.getElementById('limitModal').style.display='none';"
                             class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-100 transition duration-150">
                        Cancel
                    </button>
                    <button type="submit"
                             class="px-4 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 transition duration-150">
                        Save Changes
                    </button>
                </div>
            </form>
        </div>
    </div>

</body>
</html>
