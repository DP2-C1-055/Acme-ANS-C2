
package acme.forms;

import java.util.List;
import java.util.Map;

import acme.client.components.basis.AbstractForm;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDashboard extends AbstractForm {

	private static final long	serialVersionUID	= 1L;

	List<String>				lastFiveDestinations;

	Map<String, Double>			moneySpentInBookingsLastYear;
	Map<String, Integer>		numberOfBookingsByTravelClass;
	Map<String, Integer>		countOfBookingsLastFiveYears;
	Map<String, Double>			averageCostOfBookingsLastFiveYears;
	Map<String, Double>			minimumCostOfBookingsLastFiveYears;
	Map<String, Double>			maximumCostOfBookingsLastFiveYears;
	Map<String, Double>			standardDeviationCostOfBookingsLastFiveYears;

	Integer						countOfPassengersInBookings;
	Double						averagePassengersInBookings;
	Integer						minimumPassengersInBookings;
	Integer						maximumPassengersInBookings;
	Double						standardDeviationPassengersInBookings;
}
