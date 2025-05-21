
package acme.features.customer.booking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.helpers.PrincipalHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.booking.TravelClass;
import acme.entities.flight.Flight;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerBookingUpdateService extends AbstractGuiService<Customer, Booking> {

	@Autowired
	private CustomerBookingRepository repository;


	@Override
	public void authorise() {
		boolean status = false;
		int bookingId;
		Booking booking;

		bookingId = super.getRequest().getData("id", int.class);
		booking = this.repository.findBookingById(bookingId);
		if (booking != null) {
			status = super.getRequest().getPrincipal().hasRealm(booking.getCustomer()) && booking.getDraftMode();
			Date currentMoment = MomentHelper.getCurrentMoment();
			boolean datePast = currentMoment.after(booking.getFlight().getScheduledDeparture());
			if (datePast == true)
				status = false;
		}

		if (super.getRequest().getMethod().equals("POST")) {
			String travelClass = super.getRequest().getData("travelClass", String.class);
			if (!travelClass.equals("0"))
				status = Arrays.stream(TravelClass.values()).anyMatch(tc -> tc.name().equalsIgnoreCase(travelClass));
			int flightId = super.getRequest().getData("flight", int.class);
			Flight flight = this.repository.getFlightById(flightId);
			if (flightId != 0 && flight == null)
				status = false;
			if (flight != null)
				if (flight.isDraftMode())
					status = false;
				else if (!flight.getScheduledDeparture().after(MomentHelper.getCurrentMoment()))
					status = false;

		}

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
	public void validate(final Booking object) {
		boolean isBookingCodeChange = false;
		Collection<String> allLocatorCode = this.repository.getAllLocatorCode();
		Booking booking = this.repository.findBookingById(object.getId());

		if (!super.getBuffer().getErrors().hasErrors("allLocatorCode")) {
			isBookingCodeChange = !booking.getLocatorCode().equals(object.getLocatorCode());
			super.state(!isBookingCodeChange || !allLocatorCode.contains(object.getLocatorCode()), "locatorCode", "customer.booking.error.locatorCodeDuplicate");
		}

		if (object.getFlight() != null) {
			if (object.getFlight().isDraftMode())
				super.state(false, "*", "customer.booking.error.FlightDraftMode");
			if (object.getFlight() != booking.getFlight() && object.getDraftMode() && !object.getFlight().isDraftMode())
				super.state(object.getFlight().getScheduledDeparture().after(MomentHelper.getCurrentMoment()), "*", "customer.booking.error.flightTime");
		}

	}

	@Override
	public void perform(final Booking object) {
		assert object != null;

		this.repository.save(object);
	}

	@Override
	public void unbind(final Booking object) {
		assert object != null;
		String errorMessage;
		final Locale local = super.getRequest().getLocale();
		if (local.equals(Locale.ENGLISH))
			errorMessage = "Cost can not be calculated";
		else
			errorMessage = "No se puede calcular el precio";

		Dataset dataset;
		SelectChoices choices = null;

		Collection<Flight> availableFlights;
		dataset = super.unbindObject(object, "locatorCode", "purchaseMoment", "travelClass", "lastNibble", "draftMode");

		Booking booking = this.repository.findBookingById(object.getId());

		Date currentMoment = MomentHelper.getCurrentMoment();
		availableFlights = this.getFutureFlightsIncludingCurrent(booking, currentMoment);

		if (object.getFlight() != null && !object.getFlight().isDraftMode()) {
			choices = SelectChoices.from(availableFlights, "customFlightText", booking.getFlight());
			dataset.put("flight", object.getFlight().getTag());
			dataset.put("price", object.getBookingPrice());
		} else {

			dataset.put("flight", "");
			choices = SelectChoices.from(availableFlights, "customFlightText", booking.getFlight());
			dataset.put("price", errorMessage);
		}

		dataset.put("travelClassChoices", SelectChoices.from(TravelClass.class, object.getTravelClass()));
		dataset.put("flights", choices);

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

	@Override
	public void bind(final Booking object) {
		assert object != null;

		int flightId;
		Flight flight;

		flightId = super.getRequest().getData("flight", int.class);
		flight = this.repository.getFlightById(flightId);

		super.bindObject(object, "locatorCode", "travelClass", "lastNibble");
		object.setFlight(flight);
	}

	@Override
	public void onSuccess() {
		if (super.getRequest().getMethod().equals("POST"))
			PrincipalHelper.handleUpdate();
	}

}
