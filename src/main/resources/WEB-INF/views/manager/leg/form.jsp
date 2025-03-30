<%@page%>
<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form>
    <acme:input-textbox code="manager.leg.form.label.flightNumber" path="flightNumber"/>
    <acme:input-moment code="manager.leg.form.label.scheduledDeparture" path="scheduledDeparture"/>
    <acme:input-moment code="manager.leg.form.label.scheduledArrival" path="scheduledArrival"/>
    <acme:input-select code="manager.leg.form.label.status" path="status" choices="${legStatusChoices}"/>
    <acme:input-select code="manager.leg.form.label.departureAirport" path="departureAirport" choices="${departureAirportChoices}"/>
    <acme:input-select code="manager.leg.form.label.arrivalAirport" path="arrivalAirport" choices="${arrivalAirportChoices}"/>
    <acme:input-select code="manager.leg.form.label.aircraft" path="aircraft" choices="${aircraftChoices}"/>
    
    <jstl:if test="${_command == 'create'}">
        <acme:submit code="manager.leg.form.button.create" action="/manager/leg/create?masterId=${masterId}"/>
    </jstl:if>
    
    <jstl:if test="${acme:anyOf(_command, 'show|update|delete|publish') && draftMode == true}">
        <acme:submit code="manager.leg.form.button.update" action="/manager/leg/update"/>
        <acme:submit code="manager.leg.form.button.delete" action="/manager/leg/delete"/>
        <acme:submit code="manager.leg.form.button.publish" action="/manager/leg/publish"/>
    </jstl:if>
</acme:form>
