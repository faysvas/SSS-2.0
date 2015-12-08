<%-- 
    Document   : login
    Created on : 7 Δεκ 2015, 10:59:50 μμ
    Author     : Fay
--%>


<%@page import="eu.stork.ss.Constants"%>
<%@page import="eu.stork.ss.SPUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%> 
<%@page import="eu.stork.ss.Monitoring" %>
<%@page import="java.util.Properties"%>
<html> 
    <head> <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"> 
        <title>Login Page</title> 

        <link href="css/login.css" rel="stylesheet" type="text/css" />
        <script src="js/jquery-1.11.0.min.js"></script>
    </head> <body> 
        <%
            Properties configs;
            configs = SPUtil.loadConfigs(Constants.SP_PROPERTIES);

            if (request.getParameter("username") != null && request.getParameter("password") != null) {
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                if ((username.equals(configs.getProperty("monitoring.username").trim()) && password.equals(configs.getProperty("monitoring.password").trim()))) {
                    session.setAttribute("username", username);
                    response.sendRedirect("monitoring.jsp");
                 } else {%>
        <script>
            $(function () {
                $("#login").append('<div id="error" class="alert-box error"><span>error: </span>Invalid username and/or password.</div>');
            });
        </script>

        <%
                }
            }
        %>  
        <div id="login">

            <h1>Log in</h1>
            <form action="login.jsp" method="post">
                <input name ="username" type="text" placeholder="Username" />
                <input name="password" type="password" placeholder="Password" />
                <input type="submit" value="Log in" />
            </form>
        </div>


    </body> 

</html>

