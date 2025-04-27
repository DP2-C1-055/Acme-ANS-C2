
package acme.features.customer.booking;

import java.util.Collection;
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
		boolean status;
		int bookingId;
		Booking booking;

		bookingId = super.getRequest().getData("id", int.class);
		booking = this.repository.findBookingById(bookingId);
		status = super.getRequest().getPrincipal().hasRealm(booking.getCustomer()) && booking.getDraftMode();

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

		if (object.getFlight() != null)
			if (object.getFlight().isDraftMode())
				super.state(false, "*", "customer.booking.error.FlightDraftMode");
			else
				super.state(object.getFlight().getScheduledDeparture().after(MomentHelper.getCurrentMoment()), "*", "customer.booking.error.flightTime");

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
		Collection<Flight> flights = this.repository.getAllFlightWithDraftModeFalse();
		dataset = super.unbindObject(object, "locatorCode", "purchaseMoment", "travelClass", "lastNibble", "draftMode");
		if (object.getFlight() != null && !object.getFlight().isDraftMode()) {
			choices = SelectChoices.from(flights, "customFlightText", object.getFlight());
			dataset.put("flight", object.getFlight().getTag());
			dataset.put("price", object.getBookingPrice());
		} else {
			dataset.put("flight", "");
			choices = SelectChoices.from(flights, "customFlightText", null);
			dataset.put("price", errorMessage);
		}

		dataset.put("travelClassChoices", SelectChoices.from(TravelClass.class, object.getTravelClass()));
		dataset.put("flights", choices);

		super.getResponse().addData(dataset);

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
