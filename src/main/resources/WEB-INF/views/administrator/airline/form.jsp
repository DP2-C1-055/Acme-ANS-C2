<%@page language="java"%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form>
	<acme:input-textbox code="administrator.airline.form.label.name" path="name"/>
	<acme:input-textbox code="administrator.airline.form.label.iataCode" path="iataCode" readonly="iataCode"/>
	<acme:input-textbox code="administrator.airline.form.label.website" path="website"/>
	<acme:input-select code="administrator.airline.form.label.airlineType" path="	" choices="${airlineClassChoices}"/>
    <acme:input-moment code="administrator.airline.form.label.foundationMoment" path="foundationMoment" readonly = "foundationMoment"/>
    <acme:input-textbox code="administrator.airline.form.label.email" path="email" readonly = "email"/>
    <acme:input-textbox code="administrator.airline.form.label.phoneNumber" path="phoneNumber" readonly = "phoneNumber"/>
</acme:form>