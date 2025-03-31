<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:list>
	<acme:list-column code="crew.activityLog.list.label.registrationMoment" path="registrationMoment" width="20%"/>
	<acme:list-column code="crew.activityLog.list.label.typeIncident" path="typeIncident" width="20%"/>
	<acme:list-column code="crew.activityLog.list.label.severityLevel" path="severityLevel" width="20%"/>
	<acme:list-payload path="payload"/>
</acme:list>

<acme:button code="crew.activityLog.list.button.create" action="/crew/activity-log/create"/>