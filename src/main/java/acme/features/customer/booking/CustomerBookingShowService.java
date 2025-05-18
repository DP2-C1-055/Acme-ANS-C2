
package acme.features.customer.booking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.booking.TravelClass;
import acme.entities.flight.Flight;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerBookingShowService extends AbstractGuiService<Customer, Booking> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CustomerBookingRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean status = false;
		int bookingId;
		Booking booking;

		bookingId = super.getRequest().getData("id", int.class);
		booking = this.repository.findBookingById(bookingId);
		if (booking != null)
			status = super.getRequest().getPrincipal().hasRealm(booking.getCustomer());

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Booking booking;
		int id;

		id = super.getRequest().getData("id", int.class);
		booking = this.repository.findBookingById(id);

		super.getBuffer().addData(booking);
	}

	@Override
	public void unbind(final Booking object) {
		assert object != null;

		Date currentMoment = MomentHelper.getCurrentMoment();
		Collection<Flight> availableFlights;
		SelectChoices flightChoices;

		if (object.getDraftMode())
			availableFlights = this.getFutureFlightsIncludingCurrent(object, currentMoment);
		else
			availableFlights = this.repository.getOneFlightByBookingId(object.getId());

		flightChoices = SelectChoices.from(availableFlights, "customFlightText", object.getFlight());

		Dataset dataset = super.unbindObject(object, "locatorCode", "purchaseMoment", "travelClass", "lastNibble", "draftMode");

		dataset.put("travelClassChoices", SelectChoices.from(TravelClass.class, object.getTravelClass()));
		dataset.put("flight", object.getFlight().getTag());
		dataset.put("flights", flightChoices);
		dataset.put("price", object.getBookingPrice());

		super.getResponse().addData(dataset);
	}

	private Collection<Flight> getFutureFlightsIncludingCurrent(final Booking object, final Date currentMoment) {
		Collection<Flight> allFlights = this.repository.getAllFlightWithDraftModeFalse();
		List<Flight> flightsInTheFuture = new ArrayList<>();

		for (Flight flight : allFlights)
			if (flight.getScheduledDeparture().after(currentMoment))
				flightsInTheFuture.add(flight);

		Flight currentFlight = object.getFlight();
		if (!flightsInTheFuture.contains(currentFlight))
			flightsInTheFuture.add(currentFlight);

		return flightsInTheFuture;
	}

}
