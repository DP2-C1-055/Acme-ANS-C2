<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:list>
	<acme:list-column code="crew.assignment.list.label.lastUpdate" path="lastUpdate" width="20%"/>
	<acme:list-column code="crew.assignment.list.label.currentStatus" path="currentStatus" width="20%"/>
	<acme:list-column code="crew.assignment.list.label.duty" path="duty" width="20%"/>
	<acme:list-column code="crew.assignment.list.label.draftMode" path="draftMode" width="10%"/>
	<acme:list-column code="crew.assignment.list.label.leg" path="leg" width="20%"/>
	<acme:list-column code="crew.assignment.list.label.assignmentStatus" path="assignmentStatus" width="10%"/>
	<acme:list-payload path="payload"/>
</acme:list>

<acme:button code="crew.assignment.list.button.create" action="/crew/assignment/create"/>
