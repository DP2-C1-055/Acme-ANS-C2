<%@page language="java"%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<table class="table table-sm">
 <tr>
        <th scope="row">
            <acme:print code="customer.clientDashboard.form.label.countOfPassengersInBookings"/>
        </th>
        <td class="align-right">
        	<acme:print value="${countOfPassengersInBookings == null?nullValues:countOfPassengersInBookings}"/>
        </td>
    </tr>
 <tr>
        <th scope="row">
            <acme:print code="customer.clientDashboard.form.label.minimumPassengersInBookings"/>
        </th>
        <td class="align-right">
            <acme:print value="${minimumPassengersInBookings == null?nullValues:minimumPassengersInBookings}"/>
        </td>
    </tr> 
 <tr>
        <th scope="row">
            <acme:print code="customer.clientDashboard.form.label.maximumPassengersInBookings"/>
        </th>
        <td class="align-right">
        	<acme:print value="${maximumPassengersInBookings == null?nullValues:maximumPassengersInBookings}"/>
        </td>
    </tr>
     <tr>
        <th scope="row">
            <acme:print code="customer.clientDashboard.form.label.averagePassengersInBookings"/>
        </th>
        <td class="align-right">
        	<acme:print value="${averagePassengersInBookings == null?nullValues:averagePassengersInBookings}"/>
        </td>
    </tr>    
         <tr>
        <th scope="row">
            <acme:print code="customer.clientDashboard.form.label.standardDeviationPassengersInBookings"/>
        </th>
        <td class="align-right">
        	<acme:print value="${standardDeviationPassengersInBookings == null?nullValues:standardDeviationPassengersInBookings}"/>
        </td>
    </tr>    
         <tr>
        <th scope="row">
            <acme:print code="customer.clientDashboard.form.label.lastFiveDestinations"/>
        </th>
        <td class="align-right">
        	<jstl:forEach var="lf" items="${lastFiveDestinations}">
        	<acme:print value="${lf}"/>,
        	</jstl:forEach>
        	
        </td>
    </tr> 
</table>
<jstl:forEach var="tc" items="${travelClass}">
	<table class="table table-sm">
        <tr>
            <th scope="row">
                <acme:print code="customer.customer-dashboard.form.label.numberOfBookingsByTravelClass"/> ${tc}
            </th>
            <td class="align-right">
                 <acme:print value="${numberOfBookingsByTravelClass[tc] == null? 0: numberOfBookingsByTravelClass[tc]}"/>
            </td>
        </tr>
            </table>
</jstl:forEach>

<jstl:forEach var="ce" items="${currency}">
    <h3>
        <acme:print code="customer.customer-dashboard.form.title.Booking"/>
        <acme:print code="${ce}"/>
    </h3>
    
    <table class="table table-sm">
        <tr>
            <th scope="row">
                <acme:print code="customer.customer-dashboard.form.label.countOfBookingsLastFiveYears"/>
            </th>
            <td class="align-right">
                <acme:print value="${countOfBookingsLastFiveYears[ce] == null? nullValues: countOfBookingsLastFiveYears[ce]}"/>
            </td>
        </tr>
        <tr>
            <th scope="row">
                <acme:print code="customer.customer-dashboard.form.label.averageCostOfBookingsLastFiveYears"/>
            </th>
            <td class="align-right">
                <acme:print value="${averageCostOfBookingsLastFiveYears[ce] == null? nullValues: averageCostOfBookingsLastFiveYears[ce]}"/>
            </td>
        </tr>
        <tr>
            <th scope="row">
                <acme:print code="customer.customer-dashboard.form.label.minimumCostOfBookingsLastFiveYears"/>
            </th>
            <td class="align-right">
                <acme:print value="${minimumCostOfBookingsLastFiveYears[ce] == null? nullValues: minimumCostOfBookingsLastFiveYears[ce]}"/>
            </td>
        </tr>
        <tr>
            <th scope="row">
                <acme:print code="customer.customer-dashboard.form.label.maximumCostOfBookingsLastFiveYears"/>
            </th>
            <td class="align-right">
                <acme:print value="${maximumCostOfBookingsLastFiveYears[ce] == null? nullValues: maximumCostOfBookingsLastFiveYears[ce]}"/>
            </td>
        </tr>
        <tr>
            <th scope="row">
                <acme:print code="customer.customer-dashboard.form.label.standardDeviationCostOfBookingsLastFiveYears"/>
            </th>
            <td class="align-right">
                <acme:print value="${standardDeviationCostOfBookingsLastFiveYears[ce] == null? nullValues: standardDeviationCostOfBookingsLastFiveYears[ce]}"/>
            </td>
        </tr>
        
                <tr>
            <th scope="row">
                <acme:print code="customer.customer-dashboard.form.label.moneySpentInBookingsLastYear"/>
            </th>
            <td class="align-right">
                <acme:print value="${moneySpentInBookingsLastYear[ce] == null? nullValues: moneySpentInBookingsLastYear[ce]}"/>
            </td>
        </tr>
    </table>
</jstl:forEach>
<style>
    .align-right {
        text-align: right;
    }
</style>