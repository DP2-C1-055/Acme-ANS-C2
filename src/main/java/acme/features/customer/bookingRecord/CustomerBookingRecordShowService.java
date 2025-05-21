
package acme.features.customer.bookingRecord;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.BookingRecord.BookingRecord;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerBookingRecordShowService extends AbstractGuiService<Customer, BookingRecord> {

	@Autowired
	private CustomerBookingRecordRepository repository;


	@Override
	public void authorise() {
		int bookingRecordId = super.getRequest().getData("id", int.class);
		Boolean status = false;

		BookingRecord bookingRecord = this.repository.findBookingRecordById(bookingRecordId);

		if (bookingRecord != null) {
			status = super.getRequest().getPrincipal().hasRealm(bookingRecord.getBooking().getCustomer());

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
	public void unbind(final BookingRecord bookingRecord) {
		Dataset dataset;
		dataset = super.unbindObject(bookingRecord, "passenger", "booking");
		dataset.put("passenger", bookingRecord.getPassenger().getFullName());
		dataset.put("bookingLocator", bookingRecord.getBooking().getLocatorCode());
		dataset.put("draftMode", bookingRecord.getBooking().getDraftMode());

		super.getResponse().addData(dataset);

	}

}
