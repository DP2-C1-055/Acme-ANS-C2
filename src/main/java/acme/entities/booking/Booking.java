
package acme.entities.booking;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.datatypes.Money;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.Optional;
import acme.client.components.validation.ValidMoment;
import acme.client.components.validation.ValidMoney;
import acme.entities.customer.Customer;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Booking extends AbstractEntity {

	protected static final long	serialVersionUID	= 1L;

	@Mandatory
	@Pattern(regexp = "^[A-Z0-9]{6,8}$", message = "{validation.locatorCode}")
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

	@Mandatory
	@ValidMoney(min = 0, max = 1000000)
	@Automapped
	protected Money				price;

	@Optional
	@Valid
	@Automapped
	protected String			lastNibble;

	//protected boolean			draftMode;

	// Relationships ----------------------------------------------------------

	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Customer			customer;

	//@Mandatory
	//	@Valid
	//	@ManyToOne(optional = false)
	//	private Flight				fligth;

}
