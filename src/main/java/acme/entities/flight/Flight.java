
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
		ManagerLegRepository legRepo = SpringHelper.getBean(ManagerLegRepository.class);
		Collection<Leg> legs = legRepo.findLegsByFlightId(this.getId());
		if (legs == null || legs.isEmpty()) // He tenido que hacerlo así porque da error al usar el tipo Optional por incompatibilidad con @Optional
			return null;
		Date minDeparture = null;
		for (Leg leg : legs) {
			Date departure = leg.getScheduledDeparture();
			if (departure != null)
				if (minDeparture == null || departure.before(minDeparture))
					minDeparture = departure;
		}
		return minDeparture;
	}

	@Transient
	public Date getScheduledArrival() {
		ManagerLegRepository legRepo = SpringHelper.getBean(ManagerLegRepository.class);
		Collection<Leg> legs = legRepo.findLegsByFlightId(this.getId());
		if (legs == null || legs.isEmpty())
			return null;
		Date maxArrival = null;
		for (Leg leg : legs) {
			Date arrival = leg.getScheduledArrival();
			if (arrival != null)
				if (maxArrival == null || arrival.after(maxArrival))
					maxArrival = arrival;
		}
		return maxArrival;
	}

	@Transient
	public String getOriginCity() {
		ManagerLegRepository legRepo = SpringHelper.getBean(ManagerLegRepository.class);
		Collection<Leg> legs = legRepo.findLegsByFlightId(this.getId());
		if (legs == null || legs.isEmpty())
			return null;
		Date earliestDeparture = null;
		Leg firstLeg = null;
		for (Leg leg : legs) {
			Date departure = leg.getScheduledDeparture();
			if (departure != null)
				if (earliestDeparture == null || departure.before(earliestDeparture)) {
					earliestDeparture = departure;
					firstLeg = leg;
				}
		}
		if (firstLeg != null && firstLeg.getDepartureAirport() != null)
			return firstLeg.getDepartureAirport().getCity();
		return null;
	}

	@Transient
	public String getDestinationCity() {
		ManagerLegRepository legRepo = SpringHelper.getBean(ManagerLegRepository.class);
		Collection<Leg> legs = legRepo.findLegsByFlightId(this.getId());
		if (legs == null || legs.isEmpty())
			return null;
		Date latestArrival = null;
		Leg lastLeg = null;
		for (Leg leg : legs) {
			Date arrival = leg.getScheduledArrival();
			if (arrival != null)
				if (latestArrival == null || arrival.after(latestArrival)) {
					latestArrival = arrival;
					lastLeg = leg;
				}
		}
		if (lastLeg != null && lastLeg.getArrivalAirport() != null)
			return lastLeg.getArrivalAirport().getCity();
		return null;
	}

	@Transient
	public int getNumberOfLayovers() {
		ManagerLegRepository legRepo = SpringHelper.getBean(ManagerLegRepository.class);
		Collection<Leg> legs = legRepo.findLegsByFlightId(this.getId());
		if (legs == null || legs.isEmpty())
			return 0;
		// Número de layovers es igual al número de legs menos uno.
		return Math.max(legs.size() - 1, 0);
	}

	// Relationships ----------------------------------------------------------


	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Manager manager;

}
