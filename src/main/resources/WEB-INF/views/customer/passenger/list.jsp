<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:list>
	<acme:list-column code="customer.passenger.list.label.fullName" path="fullName" />
	<acme:list-column code="customer.passenger.list.label.email" path="email"/>
	<acme:list-column code="customer.passenger.list.label.passportNumber" path="passportNumber" />
</acme:list>
	   <jstl:choose>
		<jstl:when test="${acme:anyOf(_command, 'list') && bookingDraftMode == true}">
   			<acme:button code="customer.passenger.form.button.create" action="/customer/passenger/create?bookingId=${bookingId}"/>
   		</jstl:when>		
   			
   		</jstl:choose>	