
package acme.features.customer.booking;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.booking.TravelClass;
import acme.entities.flight.Flight;
import acme.entities.passenger.Passenger;
import acme.features.customer.bookingRecord.BookingRecordRepository;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerBookingPublishService extends AbstractGuiService<Customer, Booking> {

	@Autowired
	private CustomerBookingRepository	repository;

	@Autowired
	private BookingRecordRepository		bookingRecordRepository;


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

		Collection<Passenger> passengers = this.bookingRecordRepository.findPassengenrsByBooking(object.getId());
		boolean allDraftMode = passengers.stream().allMatch(passenger -> !passenger.getDraftMode());

		if (!super.getBuffer().getErrors().hasErrors("passportNumber")) {
			isBookingCodeChange = !booking.getLocatorCode().equals(object.getLocatorCode());
			super.state(!isBookingCodeChange || !allLocatorCode.contains(object.getLocatorCode()), "locatorCode", "customer.booking.error.locatorCodeDuplicate");
		}

		if (!allDraftMode)
			super.state(allDraftMode, "*", "customer.booking.error.draftMode");

		if (passengers.size() == 0)
			super.state(passengers.size() != 0, "*", "customer.booking.error.noPassengers");
		if (object.getLastNibble().isEmpty())
			super.state(!object.getLastNibble().isEmpty(), "lastNibble", "customer.booking.error.lastNibble");
	}

	@Override
	public void perform(final Booking object) {
		assert object != null;

		object.setDraftMode(false);

		this.repository.save(object);
	}

	@Override
	public void unbind(final Booking object) {
		assert object != null;

		Dataset dataset;

		SelectChoices choices;
		Collection<Flight> flights = this.repository.getAllFlightWithDraftModeFalse();
		choices = SelectChoices.from(flights, "tag", object.getFlight());

		dataset = super.unbindObject(object, "locatorCode", "purchaseMoment", "travelClass", "price", "lastNibble", "draftMode");
		dataset.put("travelClassChoices", SelectChoices.from(TravelClass.class, object.getTravelClass()));
		dataset.put("flight", object.getFlight().getTag());
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
}
