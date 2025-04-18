
package acme.features.customer.passenger;

import java.util.Collection;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.passenger.Passenger;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerPassengerListService extends AbstractGuiService<Customer, Passenger> {

	@Autowired
	private CustomerPassengerRepository customerPassengerRepository;


	@Override
	public void authorise() {
		boolean status;

		status = super.getRequest().getPrincipal().hasRealmOfType(Customer.class);

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Collection<Passenger> passengers;
		int customerId;

		customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		passengers = this.customerPassengerRepository.findPassengenrsByCustomerId(customerId);

		super.getBuffer().addData(passengers);

	}

	@Override
	public void unbind(final Passenger object) {
		assert object != null;

		Dataset dataset;
		dataset = super.unbindObject(object, "fullName", "email", "passportNumber", "draftMode");

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
