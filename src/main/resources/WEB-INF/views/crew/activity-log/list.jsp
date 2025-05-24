<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:list>
	<acme:list-column code="crew.activityLog.list.label.number" path="flightNumber" width="25%"/>
	<acme:list-column code="crew.activityLog.list.label.typeIncident" path="typeIncident" width="30%"/>
	<acme:list-column code="crew.activityLog.list.label.severityLevel" path="severityLevel" width="30%"/>
	<acme:list-column code="crew.activityLog.list.label.registrationMoment" path="registrationMoment" width="25%"/>
	<acme:list-column code="crew.activityLog.list.label.draftMode" path="draftMode" width="25%"/>
	<acme:list-payload path="payload"/>
</acme:list>
<jstl:choose>
	<jstl:when test="${acme:anyOf(_command, 'list') && draftModeAssignment == true}">
	<acme:button code="crew.activityLog.list.button.create" action="/crew/activity-log/create?assignmentId=${id}"/>
	</jstl:when>
</jstl:choose>

