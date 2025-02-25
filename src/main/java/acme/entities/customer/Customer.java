
package acme.entities.customer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.Optional;
import acme.client.components.validation.ValidNumber;
import acme.client.components.validation.ValidString;
import acme.entities.passenger.Passenger;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Customer extends AbstractEntity {

	protected static final long	serialVersionUID	= 1L;

	@Mandatory
	@Pattern(regexp = "^[A-Z]{2,3}\\d{6}$", message = "{validation.identifierCode}")
	@Column(unique = true)
	protected String			identifier;

	@Mandatory
	@Pattern(regexp = "^\\+?\\d{6,15}$", message = "{validation.phoneNumber}")
	@Automapped
	protected String			phoneNumber;

	@Mandatory
	@ValidString
	@Automapped
	protected String			address;

	@Mandatory
	@ValidString(max = 50)
	@Automapped
	protected String			city;

	@Mandatory
	@ValidString(max = 50)
	@Automapped
	protected String			country;

	@Optional
	@ValidNumber(max = 500)
	@Automapped
	protected Integer			earnedPoints;

	//protected boolean			draftMode;

	// Relationships ----------------------------------------------------------

	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Passenger			passenger;

}
