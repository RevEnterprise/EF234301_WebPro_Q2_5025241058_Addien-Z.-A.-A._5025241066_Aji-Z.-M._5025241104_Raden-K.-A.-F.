<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.List, com.example.dao.TransactionModel, java.time.LocalDateTime, java.time.format.DateTimeFormatter"
%>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%
    String username = (String) session.getAttribute("user");
    Integer userId = (Integer) session.getAttribute("userId"); 
    if (username == null || userId == null) {
        response.sendRedirect("login.jsp"); 
        return;
    }

    String currencyCode = (String) request.getAttribute("currencyCode");
    if (currencyCode == null) currencyCode = "USD";
    
    Locale locale = new Locale("", currencyCode); 
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
    
    List<TransactionModel> transactions = (List<TransactionModel>) request.getAttribute("transactions");

    String message = (String) session.getAttribute("message");
    String error = (String) session.getAttribute("error");
    session.removeAttribute("message"); 
    session.removeAttribute("error"); 
%>
<!DOCTYPE html>
<html>
<head>
    <title>Transactions</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        /* CSS styles copied from the dashboard for sidebar and content */
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
        table th {
            text-align: left;
        }
        .form-group {
            margin-bottom: 1rem;
        }
        
        /* New styles for compact inline toggle */
        .amount-input-group {
            display: flex;
            align-items: center;
        }
        .toggle-btn {
            height: 42px; /* Match input height */
            width: 42px;
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 1.25rem; /* text-xl */
            font-weight: bold;
            margin-right: 8px;
            border-radius: 0.375rem; /* rounded-md */
            transition: background-color 0.15s, color 0.15s;
        }
        .income-style {
            background-color: #10b981; /* green-500 */
            color: white;
        }
        .payment-style {
            background-color: #ef4444; /* red-500 */
            color: white;
        }
    </style>
    <link rel="stylesheet" href="CSS/style.css">
</head>

<body class="bg-gray-100">

    <div class="sidebar">
        <h2 class="text-xl font-bold mb-4 text-gray-800">Menu</h2>

        <a href="dashboard" class="block py-2 hover:text-blue-600">Dashboard</a>
        <a href="transactions" class="block py-2 text-blue-600 font-semibold">Transactions</a>

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
                        <button class="mt-2 w-full bg-red-500 text-white py-1 rounded hover:bg-red-600 transition duration-150">
                            Logout
                        </button>
                    </form>
                </div>
            </details>
        </div>
    </div>

    <div class="content">

        <div class="header flex justify-between items-center">
            <h1 class="text-3xl font-bold text-gray-800">Transaction Manager</h1>
            <span class="text-gray-600">Logged in as: <b><%= username %></b></span>
        </div>

        <hr class="mb-6 border-gray-300">

        <% if (message != null) { %>
            <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4 rounded shadow" role="alert">
                <p class="font-bold">Success</p>
                <p><%= message %></p>
            </div>
        <% } %>
        <% if (error != null) { %>
            <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded shadow" role="alert">
                <p class="font-bold">Error</p>
                <p><%= error %></p>
            </div>
        <% } %>

        <div class="mt-8 bg-white p-6 rounded-lg shadow-xl">
            <h2 class="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">➕ Record New Transaction</h2>
            <form id="transactionForm" action="<%= request.getContextPath() %>/transactions" method="post" class="space-y-4" onsubmit="return applySignToAmount()">
                
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="form-group">
                        <label for="name" class="block text-sm font-medium text-gray-700">Description:</label>
                        <input type="text" id="name" name="name" required 
                               class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 focus:ring-blue-500 focus:border-blue-500">
                    </div>

                    <div class="form-group">
                        <label for="category_id" class="block text-sm font-medium text-gray-700">Category ID (Optional):</label>
                        <input type="number" id="category_id" name="category_id" 
                               class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 focus:ring-blue-500 focus:border-blue-500">
                    </div>
                </div>
                
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="form-group">
                        <label for="amount" class="block text-sm font-medium text-gray-700">Amount (Absolute Value):</label>
                        <div class="amount-input-group mt-1">
                            
                            <button type="button" id="toggle-btn" onclick="toggleTransactionType()" 
                                    class="toggle-btn income-style" title="Click to switch between Income (+) and Payment (-)">
                                +
                            </button>
                            
                            <input type="number" id="amount" name="amount" step="0.01" required 
                                   class="block w-full border border-gray-300 rounded-md shadow-sm p-2 focus:ring-blue-500 focus:border-blue-500">
                        </div>
                        
                        <input type="hidden" id="transaction_type" value="income"> 
                    </div>

                    <div class="form-group">
                        <label for="transaction_date" class="block text-sm font-medium text-gray-700">Date/Time:</label>
                        <%
                            String defaultDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                        %>
                        <input type="datetime-local" id="transaction_date" name="transaction_date" value="<%= defaultDateTime %>" 
                               class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 focus:ring-blue-500 focus:border-blue-500">
                    </div>
                </div>
                

                <div class="flex justify-end pt-2">
                    <button type="submit" class="px-6 py-2 bg-blue-500 text-white font-semibold rounded-lg shadow-md hover:bg-blue-600 transition duration-200">
                        Record Transaction
                    </button>
                </div>
            </form>
        </div>

        <hr class="my-6 border-gray-300">
        
        <div class="mt-8 bg-white p-6 rounded-lg shadow-xl">
            <h2 class="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">📋 Transaction History</h2>
            
            <% if (transactions != null && !transactions.isEmpty()) { %>
                <div class="overflow-x-auto">
                    <table class="min-w-full divide-y divide-gray-200">
                        <thead class="bg-gray-50">
                            <tr>
                                <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                                <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Date/Time</th>
                                <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                                <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider text-right">Amount (<%= currencyCode %>)</th>
                                <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Category ID</th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            <%
                                for (TransactionModel transaction : transactions) {
                                    String formattedAmount = currencyFormat.format(transaction.getAmount());
                                    String amountColor = (transaction.getAmount() < 0) ? "text-red-600" : "text-green-600";
                            %>
                                <tr>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"><%= transaction.getId() %></td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900"><%= transaction.getTransactionDate() %></td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900"><%= transaction.getName() %></td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm font-bold text-right <%= amountColor %>">
                                        <%= formattedAmount %>
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        <%
                                            Integer catId = transaction.getCategoryId();
                                            out.print(catId != null ? catId : "N/A");
                                        %>
                                    </td>
                                </tr>
                            <%
                                }
                            %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="p-4 text-center text-gray-500 bg-gray-50 rounded-md">
                    No transactions found for your account. Start by adding one above!
                </div>
            <% } %>
        </div>

    </div>
    
    <script>
        /**
         * Toggles the UI state and updates the hidden input field's value.
         */
        function toggleTransactionType() {
            const typeInput = document.getElementById('transaction_type');
            const toggleBtn = document.getElementById('toggle-btn');
            
            if (typeInput.value === 'income') {
                typeInput.value = 'payment';
                toggleBtn.textContent = '-';
                toggleBtn.classList.remove('income-style');
                toggleBtn.classList.add('payment-style');
            } else {
                typeInput.value = 'income';
                toggleBtn.textContent = '+';
                toggleBtn.classList.remove('payment-style');
                toggleBtn.classList.add('income-style');
            }
            
            const amountInput = document.getElementById('amount');
            if (amountInput.value.startsWith('-')) {
                amountInput.value = Math.abs(parseFloat(amountInput.value));
            }
        }
        
        function applySignToAmount() {
            const amountInput = document.getElementById('amount');
            const typeInput = document.getElementById('transaction_type');
            
            let amount = parseFloat(amountInput.value);
            
            if (isNaN(amount) || amount === 0) {
                return true; 
            }
            
            amount = Math.abs(amount);
            
            if (typeInput.value === 'payment') {
                amountInput.value = (-amount).toFixed(2);
            } else {
                amountInput.value = amount.toFixed(2);
            }
            
            return true;
        }

        document.addEventListener('DOMContentLoaded', () => {
        });
    </script>
</body>
</html>
