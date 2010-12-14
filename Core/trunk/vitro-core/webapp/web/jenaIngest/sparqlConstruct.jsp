<%-- $This file is distributed under the terms of the license in /doc/license.txt$ --%>

<%@ page import="com.hp.hpl.jena.ontology.Individual"%>
<%@ page import="com.hp.hpl.jena.ontology.OntModel"%>
<%@ page import="com.hp.hpl.jena.rdf.model.ModelMaker"%>
<%@ page import="com.hp.hpl.jena.shared.Lock"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="java.net.URLEncoder"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%

    ModelMaker maker = (ModelMaker) request.getSession().getAttribute("vitroJenaModelMaker");
    if (maker == null) {
        maker = (ModelMaker) getServletContext().getAttribute("vitroJenaModelMaker");
    }

%>


<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.ArrayList"%><h2>Execute SPARQL CONSTRUCT
Query</h2>

<h2><a href="ingest">Ingest Home</a></h2>

<c:if test="${requestScope.constructedStmtCount != null}">
	<h3 class="notice">${requestScope.constructedStmtCount} statements
	CONSTRUCTed</h3>
</c:if>

<c:if test="${errorMsg != null}">
	<h3 class="error">${requestScope.errorMsg}</h3>
</c:if>

<c:if test="${requestScope.validationMessage != null}">
	<h3 class="notice">${requestScope.validationMessage}</h3>
</c:if>

<form action="ingest" method="post"><input type="hidden"
	name="action" value="executeSparql" />

<h3>SPARQL Query <select name="savedQuery">
	<option value="">select saved query</option>
	<%
              OntModel jenaOntModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
              jenaOntModel.enterCriticalSection(Lock.READ);
              try {
                  List savedQueries = (List) request.getAttribute("savedQueries");
	          for (Iterator it = savedQueries.iterator(); it.hasNext();)  {
	              Individual savedQuery = (Individual) it.next();
                      String queryURI = savedQuery.getURI();
                      String queryLabel = savedQuery.getLabel(null);
                      %>
	<option value="<%=queryURI%>"><%=queryLabel%></option>
	<%
                  }
              } finally {
                  jenaOntModel.leaveCriticalSection();
	      }
        %>
</select> <textarea rows="16" cols="40" name="sparqlQueryStr"><c:choose>
    <c:when test="${param.sparqlQueryStr != null}">
        ${param.sparqlQueryStr}
    </c:when>
    <c:otherwise>
PREFIX rdf:   &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;
PREFIX rdfs:  &lt;http://www.w3.org/2000/01/rdf-schema#&gt;
PREFIX owl:   &lt;http://www.w3.org/2002/07/owl#&gt;
PREFIX xsd:   &lt;http://www.w3.org/2001/XMLSchema#&gt;
PREFIX vitro: &lt;http://vitro.mannlib.cornell.edu/ns/vitro/0.7#&gt;
PREFIX swrl:  &lt;http://www.w3.org/2003/11/swrl#&gt;
PREFIX swrlb: &lt;http://www.w3.org/2003/11/swrlb#&gt;<%List prefixes = (List)request.getAttribute("prefixList");
if(prefixes != null){
	Iterator prefixItr = prefixes.iterator();
	Integer count = 0;
	while (prefixItr.hasNext()){
		String prefixText = (String) prefixItr.next();
		if(prefixText.equals("(not yet specified)")){
			count++;
			prefixText = "j." + count.toString();		
		}
		String urlText = (String) prefixItr.next();%>
PREFIX <%=prefixText%>: <<%=urlText%>><%}}%>
</c:otherwise>
</c:choose>


</textarea>

<h3>Select Source Models</h3>

<ul>
	<%
	List<String> sourceModelNameList = new ArrayList<String>();
	String[] sourceModelParamVals = request.getParameterValues("sourceModelName");
	if (sourceModelParamVals != null) {
	    sourceModelNameList.addAll(Arrays.asList(sourceModelParamVals));
	}
%>

	<li><input type="checkbox" name="sourceModelName"
		value="vitro:jenaOntModel"
		<%
                        if (sourceModelNameList.contains("vitro:jenaOntModel")) {
                        	%>
		checked="checked" <%
                        }
                    %> />webapp
	model</li>
	<li><input type="checkbox" name="sourceModelName"
		value="vitro:baseOntModel"
		<%
                        if (sourceModelNameList.contains("vitro:baseOntModel")) {
                            %>
		checked="checked" <%
                        }
                    %> />webapp
	assertions</li>
	<%
    for (Iterator it = maker.listModels(); it.hasNext(); ) {
	String modelName = (String) it.next();
        %>
	<li><input type="checkbox" name="sourceModelName"
		value="<%=modelName%>"
		<%
                        if (sourceModelNameList.contains(modelName)) {
                        	%>
		checked="checked" <%
                        }
                    %> /><%=modelName%></li>
	<%    
    }
%>
</ul>

<h3>Select Destination Model</h3>

<select name="destinationModelName">
	<option value="vitro:jenaOntModel"
		<% if ("vitro:jenaOntModel".equals(request.getParameter("destinationModelName"))) {
            	  %>
		selected="selected" <%
            	 }
              %> />webapp
	model</option>
	<option value="vitro:baseOntModel"
		<% if ("vitro:baseOntModel".equals(request.getParameter("destinationModelName"))) {
                  %>
		selected="selected" <%
                 }
              %> />webapp
	assertions</option>
	<%
    for (Iterator it = maker.listModels(); it.hasNext(); ) {
    String modelName = (String) it.next();
        %>
	<option value="<%=modelName%>"
		<%
                 if (modelName.equals(request.getParameter("destinationModelName"))) {
                     %>
		selected="selected" <%
                 }
                %> /><%=modelName%></option>
	<%    
    }
%>
</select> <c:choose>
	<c:when test="${paramValues['reasoning'] != null}">
		<c:forEach var="paramValue" items="${paramValues['reasoning']}">
			<c:if test="${paramValue eq 'pellet'}">
				<p><input type="checkbox" name="reasoning" value="pellet"
					checked="checked" /> include pellet reasoning</p>
			</c:if>
		</c:forEach>
	</c:when>
	<c:otherwise>
		<p><input type="checkbox" name="reasoning" value="pellet" />
		include Pellet OWL-DL reasoning</p>
	</c:otherwise>
</c:choose> <input id="submit" type="submit" value="Execute CONSTRUCT" />