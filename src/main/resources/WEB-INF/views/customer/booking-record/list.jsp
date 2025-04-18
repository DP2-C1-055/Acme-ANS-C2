<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:list>
	<acme:list-column code="customer.bookingRecord.list.label.passengerName" path="passengerName" />
	<acme:list-column code="customer.bookingRecord.list.label.bookingLocator" path="bookingLocator"/>
</acme:list>
	<jstl:choose>
			<jstl:when test="${acme:anyOf(_command, 'list') && draftModeBooking == true}">
			<acme:button code="customer.booking-record.form.button.create" action="/customer/booking-record/create?bookingId=${id}"/>  
				
			</jstl:when>
	</jstl:choose>

