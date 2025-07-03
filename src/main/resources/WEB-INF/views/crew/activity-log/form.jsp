<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form>
    <acme:input-textbox code="crew.activityLog.form.label.typeIncident" path="typeIncident"/>    
    <acme:input-textbox code="crew.activityLog.form.label.description" path="description"/>    
    <acme:input-textbox code="crew.activityLog.form.label.severityLevel" path="severityLevel"/>
    <acme:input-moment code="crew.activityLog.form.label.registrationMoment" path="registrationMoment" readonly="true"/>
    
    <jstl:choose>
        <jstl:when test="${acme:anyOf(_command, 'show|update|delete|publish') && draftMode && not masterDraftMode && isCompleted}">
            <acme:submit code="crew.activityLog.form.button.update" action="/crew/activity-log/update"/>
            <acme:submit code="crew.activityLog.form.button.delete" action="/crew/activity-log/delete"/>
            <acme:submit code="crew.activityLog.form.button.publish" action="/crew/activity-log/publish"/>
        </jstl:when>
        
        <jstl:when test="${_command == 'create'}">
            <acme:submit code="crew.activityLog.form.button.create" action="/crew/activity-log/create?assignmentId=${assignmentId}"/>
        </jstl:when>
    </jstl:choose>
</acme:form>
