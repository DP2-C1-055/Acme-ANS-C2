<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:list>
	<acme:list-column code="crew.assignment.list.label.name" path="duty"/>
	<acme:list-column code="crew.assignment.list.label.lastUpdate" path="lastUpdate"/>
	<acme:list-column code="crew.assignment.list.label.currentStatus" path="currentStatus"/>
	<acme:list-column code="crew.assignment.list.label.remarks" path="remarks"/>
</acme:list>

<jstl:if test="${_command == 'list'}">
	<acme:button code="crew.assignment.list.button.create" action="/crew/assignment/create"/>
</jstl:if>
