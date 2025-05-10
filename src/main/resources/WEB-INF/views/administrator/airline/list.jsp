<%@page language="java"%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:list>
	<acme:list-column code="administrator.airline.list.label.name" path="name"/>
	<acme:list-column code="administrator.airline.list.label.iataCode" path="iataCode"/>
	<acme:list-column code="administrator.airline.list.label.airlineType" path="airlineType"/>
</acme:list>
<acme:button code="administrator.airline.list.button.create" action="/administrator/airline/create"/>