
package acme.features.customer.passenger;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.passenger.Passenger;
import acme.features.customer.booking.CustomerBookingRepository;
import acme.features.customer.bookingRecord.BookingRecordRepository;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerPassengerListService extends AbstractGuiService<Customer, Passenger> {

	@Autowired
	private CustomerBookingRepository	customerBookingRepository;

	@Autowired
	private BookingRecordRepository		bookingRecordRepository;


	@Override
	public void authorise() {
		boolean status;

		int customerId;

		customerId = super.getRequest().getPrincipal().getActiveRealm().getId();

		Collection<Booking> bookings = this.customerBookingRepository.findBookingByCustomer(customerId);

		Booking booking = this.customerBookingRepository.findBookingById(super.getRequest().getData("bookingId", int.class));

		status = super.getRequest().getPrincipal().hasRealmOfType(Customer.class) && bookings.contains(booking) && booking.getDraftMode();

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Collection<Passenger> passengers;
		int bookingId = super.getRequest().getData("bookingId", int.class);
		Booking booking = this.customerBookingRepository.findBookingById(bookingId);

		passengers = this.bookingRecordRepository.findPassengenrsByBooking(bookingId);

		super.getBuffer().addData(passengers);
		super.getResponse().addGlobal("bookingDraftMode", booking.getDraftMode());
		super.getResponse().addGlobal("bookingId", booking.getId());
	}

	@Override
	public void unbind(final Passenger object) {
		assert object != null;

		Dataset dataset;
		dataset = super.unbindObject(object, "fullName", "email", "passportNumber");

		super.getResponse().addData(dataset);
	}

}
