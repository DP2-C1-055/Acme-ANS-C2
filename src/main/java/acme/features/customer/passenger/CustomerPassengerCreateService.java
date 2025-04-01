
package acme.features.customer.passenger;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.BookingRecord.BookingRecord;
import acme.entities.booking.Booking;
import acme.entities.passenger.Passenger;
import acme.features.customer.booking.CustomerBookingRepository;
import acme.features.customer.bookingRecord.BookingRecordRepository;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerPassengerCreateService extends AbstractGuiService<Customer, Passenger> {

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
		customerId = super.getRequest().getPrincipal().getActiveRealm().getId();

		Collection<Booking> customerBookings = this.customerBookingRepository.findBookingByCustomer(customerId);

		Booking booking = this.customerBookingRepository.findBookingById(super.getRequest().getData("bookingId", int.class));

		status = super.getRequest().getPrincipal().hasRealmOfType(Customer.class) && customerBookings.contains(booking) && booking.getDraftMode();

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int bookingId = super.getRequest().getData("bookingId", int.class);
		Booking booking = this.customerBookingRepository.findBookingById(bookingId);

		Passenger passenger = new Passenger();
		passenger.setFullName("");
		passenger.setEmail("");
		passenger.setPassportNumber("");
		passenger.setDateOfBirth(null);
		passenger.setSpecialNeeds("");
		passenger.setDraftMode(true);

		super.getBuffer().addData(passenger);
		super.getResponse().addGlobal("bookingId", booking.getId());
	}

	@Override
	public void perform(final Passenger passenger) {
		this.repository.save(passenger);
		Booking booking = this.customerBookingRepository.findBookingById(super.getRequest().getData("bookingId", int.class));

		BookingRecord bookingRecord = new BookingRecord();
		bookingRecord.setBooking(booking);
		bookingRecord.setPassenger(passenger);
		this.bookingRecordRepository.save(bookingRecord);
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
