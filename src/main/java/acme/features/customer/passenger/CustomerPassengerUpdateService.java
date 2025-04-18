
package acme.features.customer.passenger;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.helpers.PrincipalHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.passenger.Passenger;
import acme.features.customer.booking.CustomerBookingRepository;
import acme.features.customer.bookingRecord.CustomerBookingRecordRepository;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerPassengerUpdateService extends AbstractGuiService<Customer, Passenger> {

	@Autowired
	private CustomerPassengerRepository		repository;

	@Autowired
	private CustomerBookingRepository		customerBookingRepository;

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
	public void validate(final Passenger object) {
		boolean isPassportNumberChanged = false;
		Collection<String> allPassport = this.repository.getAllPassportNumber();
		Passenger passenger = this.repository.findPassengerById(object.getId());

		if (!super.getBuffer().getErrors().hasErrors("passportNumber")) {
			isPassportNumberChanged = !passenger.getPassportNumber().equals(object.getPassportNumber());
			super.state(!isPassportNumberChanged || !allPassport.contains(object.getPassportNumber()), "passportNumber", "customer.passenger.error.passportDuplicate");
		}

	}

	@Override
	public void perform(final Passenger object) {
		assert object != null;

		this.repository.save(object);
	}

	@Override
	public void unbind(final Passenger object) {
		assert object != null;

		Dataset dataset;

		dataset = super.unbindObject(object, "fullName", "email", "passportNumber", "dateOfBirth", "specialNeeds", "draftMode");

		super.getResponse().addData(dataset);

	}

	@Override
	public void bind(final Passenger object) {
		assert object != null;

		super.bindObject(object, "fullName", "email", "passportNumber", "dateOfBirth", "specialNeeds");
	}

	@Override
	public void onSuccess() {
		if (super.getRequest().getMethod().equals("POST"))
			PrincipalHelper.handleUpdate();
	}

}
