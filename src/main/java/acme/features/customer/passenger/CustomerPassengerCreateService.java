
package acme.features.customer.passenger;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.passenger.Passenger;
import acme.features.customer.booking.CustomerBookingRepository;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerPassengerCreateService extends AbstractGuiService<Customer, Passenger> {

	@Autowired
	private CustomerPassengerRepository	repository;

	@Autowired
	private CustomerBookingRepository	customerBookingRepository;


	@Override
	public void authorise() {
		boolean status;

		status = super.getRequest().getPrincipal().hasRealmOfType(Customer.class);

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int customerId;
		customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		Customer customer = this.customerBookingRepository.getCustomerById(customerId);

		Passenger passenger = new Passenger();
		passenger.setFullName("");
		passenger.setEmail("");
		passenger.setPassportNumber("");
		passenger.setDateOfBirth(null);
		passenger.setSpecialNeeds("");
		passenger.setDraftMode(true);
		passenger.setCustomer(customer);

		super.getBuffer().addData(passenger);
	}

	@Override
	public void perform(final Passenger passenger) {
		this.repository.save(passenger);
	}

	@Override
	public void bind(final Passenger passenger) {
		super.bindObject(passenger, "fullName", "email", "passportNumber", "dateOfBirth", "specialNeeds");
	}

	@Override
	public void validate(final Passenger object) {
		Collection<String> allPassport = this.repository.getAllPassportNumber();

		if (!super.getBuffer().getErrors().hasErrors("passportNumber"))
			super.state(!allPassport.contains(object.getPassportNumber()), "passportNumber", "customer.passenger.error.passportDuplicate");

	}

	@Override
	public void unbind(final Passenger object) {
		assert object != null;

		Dataset dataset;

		dataset = super.unbindObject(object, "fullName", "email", "passportNumber", "dateOfBirth", "specialNeeds", "draftMode");

		super.getResponse().addData(dataset);

	}
}
