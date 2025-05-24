
package acme.entities.assignment;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.Optional;
import acme.client.components.validation.ValidMoment;
import acme.client.components.validation.ValidString;
import acme.entities.leg.Leg;
import acme.realms.crew.Crew;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(indexes = {
	@Index(columnList = "crew_id, draftMode"), //
	@Index(columnList = "duty"), //
	@Index(columnList = "crew_id, duty"), //
})
public class Assignment extends AbstractEntity {

	// Serialisation version --------------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------

	@Mandatory
	@Valid
	@Automapped
	private DutyCrew			duty;

	@Mandatory
	@ValidMoment(past = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date				lastUpdate;

	@Mandatory
	@Valid
	@Automapped
	private CurrentStatus		currentStatus;

	@Optional
	@ValidString
	@Automapped
	private String				remarks;

	@Mandatory
	//@Valid
	@Automapped
	private boolean				draftMode;

	// Relationships ----------------------------------------------------

	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Crew				crew;

	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Leg					leg;
}
