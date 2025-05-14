<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form> 
	<acme:input-select code="crew.assignment.form.label.leg" path="leg" choices="${legs}"/>
	<acme:input-select code="crew.assignment.form.label.duty" path="duty" choices="${duty}"/>
	<acme:input-select code="crew.assignment.form.label.currentStatus" path="currentStatus" choices="${currentStatus}"/>
	<acme:input-textbox code="crew.assignment.form.label.remarks" path="remarks"/>
	<acme:input-moment code="crew.assignment.form.label.lastUpdate" path="lastUpdate" readonly="true"/>

	<jstl:choose>
		<jstl:when test="${_command == 'create'}">
			<acme:submit code="crew.assignment.form.button.create" action="/crew/assignment/create"/>
		</jstl:when>
		
		<jstl:when test="${acme:anyOf(_command, 'show|update|publish') && draftMode == true && isCompleted == false}">
			<acme:submit code="crew.assignment.form.button.publish" action="/crew/assignment/publish"/>
			<acme:submit code="crew.assignment.form.button.update" action="/crew/assignment/update"/>
			<acme:submit code="crew.assignment.form.button.delete" action="/crew/assignment/delete"/>
		</jstl:when>
		
		<jstl:when test="${acme:anyOf(_command, 'show|update|publish') && draftMode == true && isCompleted == true}">
			<acme:button code="crew.assignment.form.button.activityLog" action="/crew/activity-log/list?assignmentId=${id}"/>
			<acme:submit code="crew.assignment.form.button.update" action="/crew/assignment/update"/>
			<acme:submit code="crew.assignment.form.button.delete" action="/crew/assignment/delete"/>
		</jstl:when>
		
		<jstl:when test="${acme:anyOf(_command, 'show|update|publish') && draftMode == false && isCompleted == true}">
			<acme:button code="crew.assignment.form.button.activityLog" action="/crew/activity-log/list?assignmentId=${id}"/>
		</jstl:when>
		
		<jstl:when test="${acme:anyOf(_command, 'show|update|publish') && draftMode == false && isCompleted == false}">
		</jstl:when>
	</jstl:choose>
</acme:form>
