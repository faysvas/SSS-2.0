<%-- 
    Document   : Logout
    Created on : 7 Δεκ 2015, 11:08:39 μμ
    Author     : Fay
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%> <html> 
    <head> <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"> 
        <title>Logout Page</title> 
        <link href="css/login.css" rel="stylesheet" type="text/css" />
    </head> <body> <% session.removeAttribute("username");
    session.removeAttribute("password");
    session.invalidate();%> <h1>Logout was done successfully.</h1> </body> </html>

