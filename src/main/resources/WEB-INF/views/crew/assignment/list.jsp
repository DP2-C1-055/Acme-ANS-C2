<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:list>
	<acme:list-column code="crew.assignment.list.label.lastUpdate" path="lastUpdate" width="30%"/>
	<acme:list-column code="crew.assignment.list.label.currentStatus" path="currentStatus" width="30%"/>
	<acme:list-column code="crew.assignment.list.label.duty" path="duty" width="40%"/>
	<acme:list-payload path="payload"/>
</acme:list>

<jstl:if test="${acme:anyOf(_command, 'list-planned|list-completed')}">
	<acme:button code="crew.assignment.list.button.create" action="/crew/assignment/create"/>
</jstl:if>
