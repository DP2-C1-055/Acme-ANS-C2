
package acme.entities.records;

import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.datatypes.Money;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.ValidMoment;
import acme.client.components.validation.ValidMoney;
import acme.client.components.validation.ValidString;

public class MaintenanceRecord extends AbstractEntity {

	// Serialisation version --------------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------

	@Mandatory
	@ValidMoment
	@Temporal(TemporalType.TIMESTAMP)
	private Date				maintenanceMoment;

	@Mandatory
	@Valid
	@Automapped
	private Status				status;

	@Mandatory
	@ValidString(min = 6, max = 8, pattern = "^\\d{2}-\\d{1,2}-\\d{1,2}$")
	@Automapped
	private String				nextInspectionDue;

	@Mandatory
	@ValidMoney
	@Automapped
	protected Money				estimatedCost;

	@Mandatory
	@ValidString(min = 1, max = 255)
	@Automapped
	private String				notes;
}
