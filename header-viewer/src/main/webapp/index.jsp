<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<h3>Headers</h3>
<% 
	java.util.Enumeration headerNames = request.getHeaderNames();
	StringBuilder oph = new StringBuilder();
	while(headerNames.hasMoreElements()){
		String name = (String)headerNames.nextElement();
		String value = request.getHeader(name);
		oph.append(name).append("=").append(value).append("<br/>");
	}
%>
<%= oph.toString() %>

<h3>Parameters</h3>
<% 
	java.util.Enumeration parmNames = request.getParameterNames();
	StringBuilder opp = new StringBuilder();
	while(parmNames.hasMoreElements()){
		String name = (String)parmNames.nextElement();
		String value = request.getParameter(name);
		opp.append(name).append("=").append(value).append("<br/>");
	}
%>
<%= opp.toString() %>

</body>
</html>