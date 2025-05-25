
package acme.entities.aircraft;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.Valid;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.Optional;
import acme.client.components.validation.ValidNumber;
import acme.client.components.validation.ValidString;
import acme.entities.airline.Airline;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(indexes = {
	@Index(columnList = "registrationNumber")
})
public class Aircraft extends AbstractEntity {

	private static final long	serialVersionUID	= 1L;

	@Mandatory
	@ValidString(min = 1, max = 50)
	@Automapped
	protected String			model;

	@Mandatory
	@ValidString(min = 1, max = 50)
	@Column(unique = true)
	protected String			registrationNumber;

	@Mandatory
	@ValidNumber(min = 0, max = 255)
	@Automapped
	protected Integer			capacity;

	@Mandatory
	@ValidNumber(min = 2000, max = 50000)
	@Automapped
	protected Integer			cargoWeight;

	@Mandatory
	@Valid
	@Automapped
	protected ServiceStatus		status;

	@Optional
	@ValidString(min = 0, max = 255)
	@Automapped
	protected String			details;

	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Airline				airline;

}
