<%@page%>
<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form>
	<acme:input-moment code="crew.assignment.form.label.lastUpdate" path="lastUpdate" readonly="true"/>
	<acme:input-textbox code="crew.assignment.form.label.crew" path="member" readonly="true"/>
	<acme:input-select code="crew.assignment.form.label.duty" path="duty" choices= "${duties}"/>	
	<acme:input-select code="crew.assignment.form.label.leg" path="leg" choices= "${legs}"/>
	<acme:input-select code="crew.assignment.form.label.currentStatus" path="currentStatus" choices= "${statuses}"/>
	<acme:input-textarea code="crew.assignment.form.label.remarks" path="remarks"/>

	<jstl:choose>		
		<jstl:when test="${acme:anyOf(_command, 'show|update|publish') && draftMode == true}">
            <acme:submit code="crew.assignment.form.button.update" action="/crew/assignment/update"/>
            <acme:submit code="crew.assignment.form.button.delete" action="/crew/assignment/delete"/>
            <acme:submit code="crew.assignment.form.button.publish" action="/crew/assignment/publish"/>
        </jstl:when>
        
		<jstl:when test="${_command == 'create'}">
			<acme:submit code="crew.assignment.form.button.create" action="/crew/assignment/create"/>
		</jstl:when>
	</jstl:choose>
</acme:form>