
package acme.features.customer.passenger;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.passenger.Passenger;
import acme.features.customer.bookingRecord.CustomerBookingRecordRepository;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerPassengerDeleteService extends AbstractGuiService<Customer, Passenger> {

	@Autowired
	private CustomerPassengerRepository		repository;

	@Autowired
	private CustomerBookingRecordRepository	bookingRecordRepository;


	@Override
	public void authorise() {

		int customerId;

		customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		Collection<Passenger> passengers = this.repository.findPassengenrsByCustomerId(customerId);

		Passenger passenger;
		int id;

		id = super.getRequest().getData("id", int.class);
		passenger = this.repository.findPassengerById(id);

		super.getResponse().setAuthorised(passengers.contains(passenger) && passenger.getDraftMode());

	}

	@Override
	public void load() {
		Passenger passenger;
		int id;

		id = super.getRequest().getData("id", int.class);
		passenger = this.repository.findPassengerById(id);

		super.getBuffer().addData(passenger);
	}

	@Override
	public void validate(final Passenger passenger) {
		;
	}

	@Override
	public void perform(final Passenger passenger) {
		this.repository.delete(passenger);

	}

	@Override
	public void unbind(final Passenger object) {
		assert object != null;

	}

	@Override
	public void bind(final Passenger object) {
		assert object != null;

		super.bindObject(object, "fullName", "email", "passportNumber", "dateOfBirth", "specialNeeds");

	}
}
