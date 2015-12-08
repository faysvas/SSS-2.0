<%-- 
    Document   : LoginCheck
    Created on : 7 Δεκ 2015, 11:01:19 μμ
    Author     : Fay
--%>




<%@page import="eu.stork.ss.Constants"%>
<%@page import="eu.stork.ss.SPUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%> 
<%@page import="eu.stork.ss.Monitoring" %>
<%@page import="java.util.Properties"%>



<html> <head> <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"> <title>JSP Page</title> </head> <body> 

        <%
            Properties configs;
            configs = SPUtil.loadConfigs(Constants.SP_PROPERTIES);

            String username = request.getParameter("username");
            String password = request.getParameter("password");
            if ((username.equals(configs.getProperty("monitoring.username").trim()) && password.equals(configs.getProperty("monitoring.password").trim()))) {
                session.setAttribute("username", username);
                response.sendRedirect("monitoring.jsp");
            } else {
                out.print("fail");
            }
       // response.sendRedirect("login.jsp");

        %>  
    </body> </html>

