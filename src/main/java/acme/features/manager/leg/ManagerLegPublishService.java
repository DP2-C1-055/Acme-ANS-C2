
package acme.features.manager.leg;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.aircraft.Aircraft;
import acme.entities.airport.Airport;
import acme.entities.leg.Leg;
import acme.entities.leg.LegStatus;
import acme.realms.Manager;

@GuiService
public class ManagerLegPublishService extends AbstractGuiService<Manager, Leg> {

	@Autowired
	private ManagerLegRepository repository;


	@Override
	public void authorise() {
		int legId = super.getRequest().getData("id", int.class);
		Leg leg = this.repository.findLegById(legId);
		// Se permite publicar solo si la leg existe, está en modo borrador y el manager es el propietario del flight asociado.
		boolean status = leg != null && leg.isDraftMode() && super.getRequest().getPrincipal().hasRealm(leg.getFlight().getManager());
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int id = super.getRequest().getData("id", int.class);
		Leg leg = this.repository.findLegById(id);
		super.getBuffer().addData(leg);
	}

	@Override
	public void validate(final Leg leg) {
		boolean validDate;
		Date currentMoment = MomentHelper.getCurrentMoment();
		if (leg.getScheduledDeparture() != null) {
			validDate = MomentHelper.isAfterOrEqual(leg.getScheduledDeparture(), currentMoment);
			super.state(validDate, "scheduledDeparture", "acme.validation.leg.departure-after-current.message");
		}
		if (leg.getScheduledArrival() != null) {
			validDate = MomentHelper.isAfterOrEqual(leg.getScheduledArrival(), currentMoment);
			super.state(validDate, "scheduledArrival", "acme.validation.leg.arrival-after-current.message");
		}
		// Comprobar que no se solape en el tiempo con otros Legs del mismo vuelo.
		if (leg.getFlight() != null && leg.getScheduledDeparture() != null && leg.getScheduledArrival() != null) {

			Collection<Leg> flightLegs = this.repository.findLegsByFlightId(leg.getFlight().getId());
			boolean validLeg = true;

			if (flightLegs != null)
				for (Leg otherLeg : flightLegs) {
					// Evitar comparar consigo mismo (por ID)
					if (leg.getId() == otherLeg.getId())
						continue;
					// Si el otro leg no tiene fechas definidas, se omite
					if (otherLeg.getScheduledDeparture() == null || otherLeg.getScheduledArrival() == null)
						continue;
					// Se comprueba que los intervalos no se solapen:
					// El leg actual se solapa con otroLeg si:
					//   scheduledDeparture (actual) < scheduledArrival (otro) &&
					//   scheduledDeparture (otro)  < scheduledArrival (actual)
					if (leg.getScheduledDeparture().before(otherLeg.getScheduledArrival()) && otherLeg.getScheduledDeparture().before(leg.getScheduledArrival())) {

						validLeg = false;
						break;
					}
				}

			super.state(validLeg, "*", "acme.validation.leg.legs-overlap.message");
		}

	}

	@Override
	public void perform(final Leg leg) {
		leg.setDraftMode(false);
		this.repository.save(leg);
	}

	@Override
	public void unbind(final Leg leg) {
		Dataset dataset;
		SelectChoices choicesAircraft;
		Collection<Aircraft> aircrafts;
		Collection<Airport> airports;
		SelectChoices choicesStatus;
		SelectChoices choicesDepartureAirport;
		SelectChoices choicesDestinationAirport;
		aircrafts = this.repository.findAllAircrafts();
		airports = this.repository.findAllAirports();
		choicesAircraft = SelectChoices.from(aircrafts, "model", leg.getAircraft());
		choicesDepartureAirport = SelectChoices.from(airports, "name", leg.getDepartureAirport());
		choicesDestinationAirport = SelectChoices.from(airports, "name", leg.getArrivalAirport());
		choicesStatus = SelectChoices.from(LegStatus.class, leg.getStatus());
		dataset = super.unbindObject(leg, "flightNumber", "status", "scheduledDeparture", "scheduledArrival", "draftMode");
		dataset.put("aircraft", choicesAircraft.getSelected().getKey());
		dataset.put("aircraftChoices", choicesAircraft);
		dataset.put("validLeg", false);
		dataset.put("validDate", false);
		dataset.put("departureAirport", choicesDepartureAirport.getSelected().getKey());
		dataset.put("departureAirports", choicesDepartureAirport);
		dataset.put("arrivalAirport", choicesDestinationAirport.getSelected().getKey());
		dataset.put("arrivalAirports", choicesDestinationAirport);
		dataset.put("statuses", choicesStatus);

		super.getResponse().addData(dataset);
	}

	@Override
	public void bind(final Leg leg) {
		// Se enlazan los atributos editables y las relaciones necesarias.
		super.bindObject(leg, "flightNumber", "scheduledDeparture", "scheduledArrival", "status");
		super.bindObject(leg, "departureAirport", "arrivalAirport", "aircraft");
	}
}
