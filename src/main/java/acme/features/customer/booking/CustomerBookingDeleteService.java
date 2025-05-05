
package acme.features.customer.booking;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.BookingRecord.BookingRecord;
import acme.entities.booking.Booking;
import acme.features.customer.bookingRecord.CustomerBookingRecordRepository;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerBookingDeleteService extends AbstractGuiService<Customer, Booking> {

	@Autowired
	private CustomerBookingRepository		repository;

	@Autowired
	private CustomerBookingRecordRepository	bookingRecordRepository;


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
	public void validate(final Booking booking) {
		;
	}

	@Override
	public void perform(final Booking booking) {
		Collection<BookingRecord> bookingRecord = this.bookingRecordRepository.findBookingRecordByBooking(booking.getId());

		this.bookingRecordRepository.deleteAll(bookingRecord);

		this.repository.delete(booking);

	}

	@Override
	public void unbind(final Booking object) {
		assert object != null;
	}

	@Override
	public void bind(final Booking object) {
		assert object != null;

		super.bindObject(object, "locatorCode", "purchaseMoment", "travelClass", "lastNibble");

	}

}
