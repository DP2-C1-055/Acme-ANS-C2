
package acme.features.customer.bookingRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.BookingRecord.BookingRecord;
import acme.entities.booking.Booking;
import acme.entities.passenger.Passenger;
import acme.features.customer.booking.CustomerBookingRepository;
import acme.features.customer.passenger.CustomerPassengerRepository;
import acme.realms.Customer.Customer;

@GuiService
public class CustomerBookingRecordCreateService extends AbstractGuiService<Customer, BookingRecord> {

	@Autowired
	private CustomerBookingRepository		customerBookingRepository;

	@Autowired
	private CustomerPassengerRepository		customerPassengerRepository;

	@Autowired
	private CustomerBookingRecordRepository	repository;


	@Override
	public void authorise() {
		Booking booking;
		boolean status = false;
		int customerId;
		customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		int bookingId = super.getRequest().getData("bookingId", int.class);
		booking = this.customerBookingRepository.findBookingById(bookingId);

		if (booking != null) {
			Collection<Booking> bookings = this.customerBookingRepository.findBookingByCustomer(customerId);
			status = booking.getDraftMode() && bookings.contains(booking);
			Date currentMoment = MomentHelper.getCurrentMoment();
			boolean datePast = currentMoment.after(booking.getFlight().getScheduledDeparture());
			if (datePast == true)
				status = false;
		}
		if (super.getRequest().getMethod().equals("POST")) {
			int passengerId = super.getRequest().getData("passenger", int.class);

			if (passengerId != 0) {
				Passenger passenger = this.customerPassengerRepository.findPassengerById(passengerId);

				if (passenger == null)
					status = false;
				else {
					if (passenger.getDraftMode())
						status = false;
					Collection<Passenger> myPassengers = this.customerPassengerRepository.findPassengenrsByCustomerId(customerId);
					if (!myPassengers.contains(passenger))
						status = false;
					Collection<Passenger> allPassengersIncluded = this.repository.findPassengenrsByBooking(bookingId);
					if (allPassengersIncluded.contains(passenger))
						status = false;
				}

			}
		}
		super.getResponse().setAuthorised(status);

	}

	@Override
	public void load() {
		BookingRecord bookingRecord;
		bookingRecord = new BookingRecord();
		super.getBuffer().addData(bookingRecord);

	}

	@Override
	public void bind(final BookingRecord bookingRecord) {
		int bookingId = super.getRequest().getData("bookingId", int.class);
		int passengerId = super.getRequest().getData("passenger", int.class);
		Booking booking = this.customerBookingRepository.findBookingById(bookingId);
		Passenger passenger = this.customerPassengerRepository.findPassengerById(passengerId);
		bookingRecord.setBooking(booking);
		bookingRecord.setPassenger(passenger);
		super.bindObject(bookingRecord);
	}

	@Override
	public void validate(final BookingRecord object) {
		Collection<Passenger> allPassenger = this.repository.findPassengenrsByBooking(object.getBooking().getId());

		if (allPassenger.contains(object.getPassenger()))
			super.state(!allPassenger.contains(object.getPassenger()), "*", "customer.bookingRecord.error.duplicatePassenger");
		if (object.getPassenger() != null && object.getPassenger().getDraftMode())
			super.state(false, "*", "customer.bookingRecord.error.passengerDraftMode");

	}

	@Override
	public void perform(final BookingRecord bookingRecord) {
		this.repository.save(bookingRecord);
	}

	@Override
	public void unbind(final BookingRecord bookingRecord) {
		int bookingId = super.getRequest().getData("bookingId", int.class);
		Dataset dataset;
		SelectChoices passengerChoices;

		int customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		Collection<Passenger> passengers = this.customerPassengerRepository.findPassengenrsDraftModeByCustomerId(customerId);
		Collection<Passenger> allPassengerIncluded = this.repository.findPassengenrsByBooking(bookingId);
		List<Passenger> passengerToDisplay = new ArrayList<>();
		for (Passenger passenger : passengers)
			if (!allPassengerIncluded.contains(passenger))
				passengerToDisplay.add(passenger);

		passengerChoices = SelectChoices.from(passengerToDisplay, "completeNamePassport", null);

		dataset = super.unbindObject(bookingRecord);
		dataset.put("passenger", passengerChoices.getSelected().getKey());
		dataset.put("passengers", passengerChoices);
		dataset.put("bookingId", bookingId);

		super.getResponse().addData(dataset);
		System.out.println(super.getResponse());
	}

}
