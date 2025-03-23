
package acme.entities.flight;

import java.util.Collection;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.Valid;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.datatypes.Money;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.Optional;
import acme.client.components.validation.ValidMoney;
import acme.client.components.validation.ValidString;
import acme.client.helpers.SpringHelper;
import acme.entities.leg.Leg;
import acme.features.manager.leg.ManagerLegRepository;
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
	@ValidString(max = 50)
	@Automapped
	private String				tag;

	@Mandatory
	@Valid
	@Automapped
	private Boolean				selfTransfer;

	@Mandatory
	@ValidMoney
	@Automapped
	private Money				cost;

	@Optional
	@ValidString(min = 0, max = 255)
	@Automapped
	private String				description;

	// Derived attributes -----------------------------------------------------


	@Transient
	public Date getScheduledDeparture() {
		Date departure = null;
		ManagerLegRepository legRepo = SpringHelper.getBean(ManagerLegRepository.class);
		Collection<Leg> legs = legRepo.findLegsByFlightIdOrderByScheduledDepartureAsc(this.getId());
		if (legs != null && !legs.isEmpty())
			departure = legs.iterator().next().getScheduledDeparture();
		return departure;
	}

	@Transient
	public Date getScheduledArrival() {
		Date arrival = null;
		ManagerLegRepository legRepo = SpringHelper.getBean(ManagerLegRepository.class);
		Collection<Leg> legs = legRepo.findLegsByFlightIdOrderByScheduledArrivalDesc(this.getId());
		if (legs != null && !legs.isEmpty())
			arrival = legs.iterator().next().getScheduledArrival();
		return arrival;
	}

	@Transient
	public String getOriginCity() {
		String originCity = null;
		ManagerLegRepository legRepo = SpringHelper.getBean(ManagerLegRepository.class);
		Collection<Leg> legs = legRepo.findLegsByFlightIdOrderByScheduledDepartureAsc(this.getId());
		if (legs != null && !legs.isEmpty()) {
			Leg firstLeg = legs.iterator().next();
			if (firstLeg.getDepartureAirport() != null)
				originCity = firstLeg.getDepartureAirport().getCity();
		}
		return originCity;
	}

	@Transient
	public String getDestinationCity() {
		String destinationCity = null;
		ManagerLegRepository legRepo = SpringHelper.getBean(ManagerLegRepository.class);
		Collection<Leg> legs = legRepo.findLegsByFlightIdOrderByScheduledArrivalDesc(this.getId());
		if (legs != null && !legs.isEmpty()) {
			Leg lastLeg = legs.iterator().next();
			if (lastLeg.getArrivalAirport() != null)
				destinationCity = lastLeg.getArrivalAirport().getCity();
		}
		return destinationCity;
	}

	@Transient
	public int getNumberOfLayovers() {
		int layovers = 0;
		ManagerLegRepository legRepo = SpringHelper.getBean(ManagerLegRepository.class);
		Collection<Leg> legs = legRepo.findLegsByFlightId(this.getId());
		if (legs != null && !legs.isEmpty())
			layovers = Math.max(legs.size() - 1, 0);
		return layovers;
	}

	// Relationships ----------------------------------------------------------


	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Manager manager;

}
