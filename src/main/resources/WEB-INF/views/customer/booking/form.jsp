<%@page language="java"%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form>
	<acme:input-textbox code="customer.booking.form.label.locatorCode" path="locatorCode"/>
	<acme:input-textbox code="customer.booking.form.label.purchaseMoment" path="purchaseMoment" readonly="true"/>
	<acme:input-select code="customer.booking.form.label.travelClass" path="travelClass" choices="${travelClassChoices}"/>
	<acme:input-money code="customer.booking.form.label.price" path="price" readonly = "true"/>
    <acme:input-textbox code="customer.booking.form.label.lastNibble" path="lastNibble"/>
    <acme:input-textbox code="customer.booking.form.label.flight" path="flight" readonly = "true"/>
    <acme:button code="customer.booking.form.button.passenger" action="/customer/passenger/list?bookingId=${id}"/>
</acme:form>