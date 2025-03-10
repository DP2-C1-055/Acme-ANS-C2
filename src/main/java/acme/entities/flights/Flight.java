
package acme.entities.flights;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.Valid;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.datatypes.Money;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.Optional;
import acme.client.components.validation.ValidMoney;
import acme.client.components.validation.ValidString;
import acme.realms.Manager;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Flight extends AbstractEntity {

	// Serialisation version --------------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------

	@Mandatory
	@ValidString(min = 1, max = 50)
	private String				tag;

	@Mandatory
	//@Valid
	@Automapped
	private boolean				selfTransfer;

	@Mandatory
	@ValidMoney
	@Automapped
	private Money				cost;

	@Optional
	@ValidString
	@Automapped
	private String				description;

	// Derived attributes -----------------------------------------------------


	@Transient
	public Date getScheduledDeparture() {
		//TO DO IN D03
		return null;
	}

	@Transient
	public Date getScheduledArrival() {
		//TO DO IN D03
		return null;
	}

	@Transient
	public String getOriginCity() {
		//TO DO IN D03
		return null;
	}

	@Transient
	public String getDestinationCity() {
		//TO DO IN D03
		return null;
	}

	@Transient
	public int getNumberOfLayovers() {
		//TO DO IN D03
		return 0;
	}

	// Relationships ----------------------------------------------------------


	@Mandatory
	@Valid
	@OneToOne(optional = false)
	private Manager airlineManager;

}
