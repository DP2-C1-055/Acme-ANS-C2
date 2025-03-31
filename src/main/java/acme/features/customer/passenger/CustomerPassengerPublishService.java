
package acme.features.customer.passenger;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.datatypes.Money;
import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.passenger.Passenger;
import acme.features.customer.booking.CustomerBookingRepository;
import acme.features.customer.bookingRecord.BookingRecordRepository;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerPassengerPublishService extends AbstractGuiService<Customer, Passenger> {

	@Autowired
	private CustomerBookingRepository	customerBookingRepository;

	@Autowired
	private BookingRecordRepository		bookingRecordRepository;

	@Autowired
	private CustomerPassengerRepository	repository;


	@Override
	public void authorise() {
		boolean status;

		int customerId;
		int passengerId = super.getRequest().getData("id", int.class);
		customerId = super.getRequest().getPrincipal().getActiveRealm().getId();

		Collection<Booking> customerBookings = this.customerBookingRepository.findBookingByCustomer(customerId);

		Booking booking = this.bookingRecordRepository.findBookingByPassengerId(passengerId);

		Passenger passenger = this.repository.findPassengerById(passengerId);

		status = super.getRequest().getPrincipal().hasRealmOfType(Customer.class) && customerBookings.contains(booking) && passenger.getDraftMode();

		super.getResponse().setAuthorised(status);
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
		Booking booking = this.bookingRecordRepository.findBookingByPassengerId(object.getId());
		Double flightCost = booking.getFlight().getCost().getAmount();

		double bookingCostUpdated = booking.getPrice().getAmount() + flightCost;

		Money money = new Money();
		money.setAmount(bookingCostUpdated);
		money.setCurrency(booking.getFlight().getCost().getCurrency());

		booking.setPrice(money);
		this.customerBookingRepository.save(booking);
		object.setDraftMode(false);
		this.repository.save(object);
	}

	@Override
	public void bind(final Passenger passenger) {
		super.bindObject(passenger, "fullName", "email", "passportNumber", "dateOfBirth", "specialNeeds");
	}

	@Override
	public void unbind(final Passenger object) {
		assert object != null;

		Dataset dataset;

		dataset = super.unbindObject(object, "fullName", "email", "passportNumber", "dateOfBirth", "specialNeeds", "draftMode");

		super.getResponse().addData(dataset);

	}

}
