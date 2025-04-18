
package acme.features.customer.bookingRecord;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.BookingRecord.BookingRecord;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerBookingRecordDeleteService extends AbstractGuiService<Customer, BookingRecord> {

	@Autowired
	private CustomerBookingRecordRepository repository;


	@Override
	public void authorise() {
		boolean isCustomer = super.getRequest().getPrincipal().hasRealmOfType(Customer.class);
		super.getResponse().setAuthorised(isCustomer);

		BookingRecord bookingRecord;
		int bookingRecordId;
		int customerId;

		customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		bookingRecordId = super.getRequest().getData("id", int.class);
		bookingRecord = this.repository.findBookingRecordById(bookingRecordId);

		super.getResponse().setAuthorised(bookingRecord.getBooking().getDraftMode() && bookingRecord.getBooking().getCustomer().getId() == customerId);

	}

	@Override
	public void load() {
		BookingRecord bookingRecord;
		int bookingRecordId;

		bookingRecordId = super.getRequest().getData("id", int.class);
		bookingRecord = this.repository.findBookingRecordById(bookingRecordId);
		super.getBuffer().addData(bookingRecord);

	}

	@Override
	public void validate(final BookingRecord bookingRecord) {
		;
	}

	@Override
	public void perform(final BookingRecord bookingRecord) {

		this.repository.delete(bookingRecord);
	}

	@Override
	public void unbind(final BookingRecord bookingRecord) {
		assert bookingRecord != null;
	}

	@Override
	public void bind(final BookingRecord bookingRecord) {
		assert bookingRecord != null;
	}
}
