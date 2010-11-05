<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@page import="at.jku.semwiq.endpoint.Constants"%>
<%@page import="at.jku.semwiq.rmi.SpawnedEndpointMetadata"%>
<%
	SpawnedEndpointMetadata meta = (SpawnedEndpointMetadata) getServletContext().getAttribute(Constants.ENDPOINT_METADATA_ATTRIB);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	<link rel="stylesheet" title="default style" href="/styles.css"/>
	<title><%=meta.getTitle() %></title>
</head>
<body>

<h1><%=meta.getTitle() %></h1>
<p><%=meta.getDescription() %></p>

<h2>SPARQL Endpoint</h2>
<p>
	<a href="/<%=meta.getSparqlPath() %>">/<%=meta.getSparqlPath() %></a>
</p>

<h2>Human Interfaces</h2>
<p>
	<a href="/snorql">Snorql Browser</a> (known from <a href="http://www4.wiwiss.fu-berlin.de/bizer/d2r-server/">D2R-Server</a>)<br />
	<a href="/status/<%=meta.getSparqlPath() %>">State of XLWrapEngine and cache</a><br />
	<a href="/stats/<%=meta.getSparqlPath() %>">RDFStats Histograms</a><br />
	<a href="/snorql/namespaces.js">Namespaces (JSON)</a><br />
	<a href="/logs">Server Logs</a> (open logs in <a href="#" onclick="window.open('/logs', 'logs', 'width=800,height=600,menubar=no,location=no,scrollbars=yes,status=yes,toolbar=no')">new window</a>)
</p>

<h2>Resource Discovery</h2>
<p>
	<a href="/robots.txt">robots.txt</a><br />
	<a href="/sitemap.xml">sitemap.xml</a><br />
	<a href="/void/<%=meta.getSparqlPath() %>">voiD description</a> including RDFStats statistics<br />
</p>

<p class="footer">
&copy; Johannes Kepler University Linz, Andreas Langegger &lt;
<script type="text/javascript">
document.open();
document.write("&#97;n");
document.write("&#100;re&#97;s");
document.write("&#64;");
document.write("l&#97;nge");
document.write("gger.&#97;t&gt;");
</script>
</p>
</body>
</html>
