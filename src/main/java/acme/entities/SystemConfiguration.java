
package acme.entities;

import javax.persistence.Entity;
import javax.validation.constraints.Pattern;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SystemConfiguration extends AbstractEntity {

	// Serialisation identifier ---------------------------------------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// Attributes -----------------------------------------------------------------------------------------

	@Mandatory
	@Pattern(regexp = "^[A-Z]{3}$")
	@Automapped
	private String				systemCurrency;

	@Mandatory
	@Pattern(regexp = "([A-Z]{3},\\s)*([A-Z]{3})?", message = "{systemConfiguration.acceptedCurrency}")
	@Automapped
	private String				acceptedCurrency;

}
