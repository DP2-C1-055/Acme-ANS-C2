<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form>
    <acme:hidden-data path="bookingId"/>

    <jstl:choose>
        <jstl:when test="${_command == 'create'}">
            <acme:input-select code="customer.booking-record.list.label.passengerNew" path="passenger" choices="${passengers}"/>
            <acme:submit code="customer.booking-record.form.button.create" action="/customer/booking-record/create?bookingId=${bookingId}"/>
        </jstl:when>
        
         <jstl:when test="${_command == 'show' && draftMode == false}">
            <acme:input-textbox code="customer.booking-record.list.label.passenger" path="passenger" readonly="true"/>
            <acme:input-textbox code="customer.booking-record.list.label.bookingLocator" path="bookingLocator" readonly="true"/>
        </jstl:when>

        <jstl:when test="${_command == 'show' && draftMode == true}">
            <acme:input-textbox code="customer.booking-record.list.label.passenger" path="passenger" readonly="true"/>
            <acme:input-textbox code="customer.booking-record.list.label.bookingLocator" path="bookingLocator" readonly="true"/>
            <acme:submit code="customer.booking-record.form.button.delete" action="/customer/booking-record/delete"/>
        </jstl:when>
        
    </jstl:choose>
</acme:form>
