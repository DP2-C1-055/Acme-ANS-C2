
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
import acme.realms.Customer.Customer;

@GuiService
public class CustomerBookingShowService extends AbstractGuiService<Customer, Booking> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CustomerBookingRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean status;
		int bookingId;
		Booking booking;

		bookingId = super.getRequest().getData("id", int.class);
		booking = this.repository.findBookingById(bookingId);
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

		Dataset dataset;
		Collection<Flight> flights;

		SelectChoices choices;

		if (object.getDraftMode())
			flights = this.repository.getAllFlightWithDraftModeFalse();
		else
			flights = this.repository.getOneFlightByBookingId(object.getId());

		choices = SelectChoices.from(flights, "tag", object.getFlight());

		dataset = super.unbindObject(object, "locatorCode", "purchaseMoment", "travelClass", "price", "lastNibble", "draftMode");
		dataset.put("travelClassChoices", SelectChoices.from(TravelClass.class, object.getTravelClass()));
		dataset.put("flight", object.getFlight().getTag());
		dataset.put("flights", choices);

		super.getResponse().addData(dataset);

	}
}
