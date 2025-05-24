
package acme.entities.booking;

import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.Valid;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.datatypes.Money;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.Optional;
import acme.client.components.validation.ValidMoment;
import acme.client.components.validation.ValidString;
import acme.client.helpers.SpringHelper;
import acme.entities.flight.Flight;
import acme.entities.passenger.Passenger;
import acme.realms.Customer.Customer;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(indexes = {
	@Index(columnList = "draftmode"), @Index(columnList = "purchaseMoment"), @Index(columnList = "draftmode,customer_id"), @Index(columnList = "draftmode,customer_id,purchaseMoment")
})
public class Booking extends AbstractEntity {

	protected static final long	serialVersionUID	= 1L;

	@Mandatory
	@ValidString(pattern = "^[A-Z0-9]{6,8}$", message = "{validation.locatorCode}")
	@Column(unique = true)
	protected String			locatorCode;

	@Mandatory
	@ValidMoment
	@Temporal(TemporalType.TIMESTAMP)
	protected Date				purchaseMoment;

	@Mandatory
	@Valid
	@Automapped
	protected TravelClass		travelClass;

	@Optional
	@ValidString(max = 4, pattern = "^(?:[0-9]{0}|[0-9]{4,})$", message = "{validation.lastNibble}")
	@Automapped
	protected String			lastNibble;

	@Automapped
	protected Boolean			draftMode;

	// Relationships ----------------------------------------------------------

	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Customer			customer;

	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Flight				flight;

	// Derived attributes -----------------------------------------------------


	@Transient
	public Money getBookingPrice() {
		Flight flight = this.flight;

		Money money = new Money();
		money.setCurrency(flight.getCost().getCurrency());

		BookingRepository bookingRepository = SpringHelper.getBean(BookingRepository.class);

		Collection<Passenger> passengers = bookingRepository.findPassengerByBooking(this.getId());
		Double totalCost = flight.getCost().getAmount() * passengers.size();
		money.setAmount(totalCost);
		return money;

	}

}
