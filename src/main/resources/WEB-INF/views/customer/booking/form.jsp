<%@page language="java"%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form>
	<acme:input-textbox code="customer.booking.form.label.locatorCode" path="locatorCode" />
	<acme:input-textbox code="customer.booking.form.label.purchaseMoment" path="purchaseMoment"	readonly="true" />
	<acme:input-select code="customer.booking.form.label.travelClass" path="travelClass" choices="${travelClassChoices}" />
	<acme:input-textbox code="customer.booking.form.label.lastNibble" path="lastNibble" />
	<acme:input-select code="customer.booking.form.label.flight" path="flight" choices="${flights}" />

	<jstl:choose>
		<jstl:when test="${acme:anyOf(_command, 'show') && datePast == true && draftMode == true}">
			<div class= "text-red">
			<acme:print code="customer.booking.selectedFlightPassed"/>
			</div>
		    <acme:submit code="customer.booking.form.button.delete" action="/customer/booking/delete" />
		</jstl:when>
		<jstl:when test="${acme:anyOf(_command, 'show|update|delete|publish') && draftMode == false}">
			<acme:input-money code="customer.booking.form.label.price" path="price" readonly="true" />
			<acme:button code="customer.booking.form.button.passenger" action="/customer/booking-record/list?bookingId=${id}" />
		</jstl:when>
		<jstl:when test="${acme:anyOf(_command, 'show|update|delete|publish') && draftMode == true}">
			<acme:input-money code="customer.booking.form.label.price" path="price" readonly="true" />
			<acme:button code="customer.booking.form.button.passenger" action="/customer/booking-record/list?bookingId=${id}" />
			<acme:submit code="customer.booking.form.button.update"	action="/customer/booking/update" />
			<acme:submit code="customer.booking.form.button.publish" action="/customer/booking/publish" />
			<acme:submit code="customer.booking.form.button.delete" action="/customer/booking/delete" />
		</jstl:when>
		<jstl:when test="${_command == 'create'}">
			<acme:submit code="customer.booking.form.button.create" action="/customer/booking/create" />
		</jstl:when>
	</jstl:choose>

</acme:form>

<style>
    .text-red {
        color: red;
    }
</style>