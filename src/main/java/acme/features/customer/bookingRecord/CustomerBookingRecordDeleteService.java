
package acme.features.customer.bookingRecord;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.helpers.MomentHelper;
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

		boolean status = false;

		BookingRecord bookingRecord;
		int bookingRecordId;

		bookingRecordId = super.getRequest().getData("id", int.class);
		bookingRecord = this.repository.findBookingRecordById(bookingRecordId);

		if (bookingRecord != null) {
			boolean statusCustomer = super.getRequest().getPrincipal().hasRealm(bookingRecord.getBooking().getCustomer());

			status = bookingRecord.getBooking().getDraftMode() && statusCustomer;
			Date currentMoment = MomentHelper.getCurrentMoment();
			boolean datePast = currentMoment.after(bookingRecord.getBooking().getFlight().getScheduledDeparture());
			if (datePast == true)
				status = false;
		}
		super.getResponse().setAuthorised(status);

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
