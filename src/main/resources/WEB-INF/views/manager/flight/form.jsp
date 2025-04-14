<%@page%>
<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>
<acme:form>
    <jstl:choose>
        <jstl:when test="${_command == 'show' && draftMode == true}">
            <acme:input-textbox code="manager.flight.form.label.tag" path="tag" readonly="${draftMode == false}" />
            <acme:input-checkbox code="manager.flight.form.label.selfTransfer" path="selfTransfer" readonly="${draftMode == false}" />
            <acme:input-money code="manager.flight.form.label.cost" path="cost" readonly="${draftMode == false}" />
            <acme:input-textarea code="manager.flight.form.label.description" path="description" readonly="${draftMode == false}" />
            <acme:input-textbox code="manager.flight.form.label.manager" path="manager" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.scheduledDeparture" path="scheduledDeparture" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.scheduledArrival" path="scheduledArrival" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.originCity" path="originCity" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.destinationCity" path="destinationCity" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.numberOfLayovers" path="numberOfLayovers" readonly="true" />
            <acme:button code="manager.flight.form.button.legs" action="/manager/leg/list?masterId=${id}" />
            <acme:submit code="manager.flight.form.button.update" action="/manager/flight/update" />
            <acme:submit code="manager.flight.form.button.delete" action="/manager/flight/delete" />
            <acme:submit code="manager.flight.form.button.publish" action="/manager/flight/publish" />
        </jstl:when>
        <jstl:when test="${_command == 'show' && draftMode == false}">
            <acme:input-textbox code="manager.flight.form.label.tag" path="tag" readonly="${draftMode == false}" />
            <acme:input-checkbox code="manager.flight.form.label.selfTransfer" path="selfTransfer" readonly="${draftMode == false}" />
            <acme:input-money code="manager.flight.form.label.cost" path="cost" readonly="${draftMode == false}" />
            <acme:input-textarea code="manager.flight.form.label.description" path="description" readonly="${draftMode == false}" />
            <acme:input-textbox code="manager.flight.form.label.manager" path="manager" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.scheduledDeparture" path="scheduledDeparture" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.scheduledArrival" path="scheduledArrival" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.originCity" path="originCity" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.destinationCity" path="destinationCity" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.numberOfLayovers" path="numberOfLayovers" readonly="true" />
            <acme:button code="manager.flight.form.button.legs" action="/manager/leg/list?masterId=${id}" />
        </jstl:when>
        <jstl:when test="${acme:anyOf(_command, 'update|delete|publish')}">
            <acme:input-textbox code="manager.flight.form.label.tag" path="tag" readonly="false" />
            <acme:input-checkbox code="manager.flight.form.label.selfTransfer" path="selfTransfer" readonly="false" />
            <acme:input-money code="manager.flight.form.label.cost" path="cost" readonly="false" />
            <acme:input-textarea code="manager.flight.form.label.description" path="description" readonly="false" />
            <acme:input-textbox code="manager.flight.form.label.manager" path="manager" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.scheduledDeparture" path="scheduledDeparture" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.scheduledArrival" path="scheduledArrival" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.originCity" path="originCity" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.destinationCity" path="destinationCity" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.numberOfLayovers" path="numberOfLayovers" readonly="true" />
            <acme:button code="manager.flight.form.button.legs" action="/manager/leg/list?masterId=${id}" />
            <acme:submit code="manager.flight.form.button.update" action="/manager/flight/update" />
            <acme:submit code="manager.flight.form.button.delete" action="/manager/flight/delete" />
            <acme:submit code="manager.flight.form.button.publish" action="/manager/flight/publish" />
        </jstl:when>
        <jstl:when test="${_command == 'create'}">
            <acme:input-textbox code="manager.flight.form.label.tag" path="tag" />
            <acme:input-checkbox code="manager.flight.form.label.selfTransfer" path="selfTransfer" />
            <acme:input-money code="manager.flight.form.label.cost" path="cost" />
            <acme:input-textarea code="manager.flight.form.label.description" path="description" />
            <acme:input-textbox code="manager.flight.form.label.manager" path="manager" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.scheduledDeparture" path="scheduledDeparture" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.scheduledArrival" path="scheduledArrival" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.originCity" path="originCity" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.destinationCity" path="destinationCity" readonly="true" />
            <acme:input-textbox code="manager.flight.form.label.numberOfLayovers" path="numberOfLayovers" readonly="true" />
            <acme:submit code="manager.flight.form.button.create" action="/manager/flight/create" />
        </jstl:when>
    </jstl:choose>
</acme:form>
