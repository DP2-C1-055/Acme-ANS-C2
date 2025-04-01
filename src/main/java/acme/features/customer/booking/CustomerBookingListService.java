
package acme.features.customer.booking;

import java.util.Collection;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerBookingListService extends AbstractGuiService<Customer, Booking> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CustomerBookingRepository repository;


	@Override
	public void authorise() {
		boolean status;

		status = super.getRequest().getPrincipal().hasRealmOfType(Customer.class);

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Collection<Booking> bookings;
		int customerId;

		customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		bookings = this.repository.findBookingByCustomer(customerId);

		super.getBuffer().addData(bookings);
	}

	@Override
	public void unbind(final Booking object) {
		assert object != null;

		Dataset dataset;
		dataset = super.unbindObject(object, "locatorCode", "purchaseMoment", "draftMode");

		if (object.getDraftMode()) {
			final Locale local = super.getRequest().getLocale();
			String draftmodeText;
			if (local.equals(Locale.ENGLISH))
				draftmodeText = "Yes";
			else
				draftmodeText = "Sí";
			dataset.put("draftMode", draftmodeText);
		} else
			dataset.put("draftMode", "No");

		super.getResponse().addData(dataset);
	}

}
