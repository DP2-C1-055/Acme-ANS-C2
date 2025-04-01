
package acme.entities.passenger;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.Optional;
import acme.client.components.validation.ValidEmail;
import acme.client.components.validation.ValidMoment;
import acme.client.components.validation.ValidString;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
//@ValidPassenger
public class Passenger extends AbstractEntity {

	protected static final long	serialVersionUID	= 1L;

	@Mandatory
	@ValidString
	@Automapped
	protected String			fullName;

	@Mandatory
	@ValidEmail
	@Automapped
	protected String			email;

	@Mandatory
	@ValidString(pattern = "^[A-Z0-9]{6,9}$", message = "{acme.validation.passportNumber}")
	@Column(unique = true)
	protected String			passportNumber;

	@Mandatory
	@ValidMoment(past = true, message = "{acme.validation.dateOfBirth}")
	@Temporal(TemporalType.TIMESTAMP)
	protected Date				dateOfBirth;

	@Optional
	@ValidString(max = 50)
	@Automapped
	protected String			specialNeeds;

	protected Boolean			draftMode;

}
