<%@page%>
<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form>
	<acme:input-select code="crew.assignment.form.label.duty" path="duty" choices="${duty}"/>
	<acme:input-textbox code="crew.assignment.form.label.lastUpdate" path="lastUpdate"/>
	<acme:input-select code="crew.assignment.form.label.currentStatus" path="currentStatus" choices="${currentStatus}"/>
	<acme:input-textbox code="crew.assignment.form.label.remarks" path="remarks"/>

	<jstl:choose>
		<jstl:when test="${_command == 'show' && draftMode == false}">
			<acme:input-textbox code="crew.assignment.form.label.crew" path="crew" readonly="true"/>
			<acme:button code="crew.assignment.form.button.legs" action="/crew/assignment/list?masterId=${id}"/>
		</jstl:when>
		
		<jstl:when test="${acme:anyOf(_command, 'show|update|delete|publish') && draftMode == true}">
            <acme:input-textbox code="crew.assignment.form.label.crew" path="manager" readonly="true"/>
        
            <acme:button code="crew.assignment.form.button.legs" action="/crew/assignment/list?masterId=${id}"/>
            <acme:submit code="crew.assignment.form.button.update" action="/crew/assignment/update"/>
            <acme:submit code="crew.assignment.form.button.delete" action="/crew/assignment/delete"/>
            <acme:submit code="crew.assignment.form.button.publish" action="/crew/assignment/publish"/>
        </jstl:when>
        
		<jstl:when test="${_command == 'create'}">
			<acme:submit code="crew.assignment.form.button.create" action="/crew/assignment/create"/>
		</jstl:when>
	</jstl:choose>
</acme:form>