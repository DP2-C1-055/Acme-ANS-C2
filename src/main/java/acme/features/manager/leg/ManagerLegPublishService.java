
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
import acme.entities.aircraft.ServiceStatus;
import acme.entities.airport.Airport;
import acme.entities.flight.Flight;
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
		Flight flight = this.repository.findFlightById(leg.getFlight().getId());
		// Se permite publicar solo si la leg existe, está en modo borrador y el manager es el propietario del flight asociado.
		boolean status = flight != null && flight.isDraftMode() && leg != null && leg.isDraftMode() && super.getRequest().getPrincipal().hasRealm(leg.getFlight().getManager());
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

		if (leg.getAircraft() != null) {
			boolean active = leg.getAircraft().getStatus() == ServiceStatus.ACTIVE;
			super.state(active, "aircraft", "acme.validation.leg.aircraft.status.active");
		}

	}

	@Override
	public void perform(final Leg leg) {
		leg.setDraftMode(false);
		this.repository.save(leg);
	}

	@Override
	public void unbind(final Leg leg) {
		// 1) Generamos los SelectChoices que necesita el JSP
		SelectChoices statusChoices = SelectChoices.from(LegStatus.class, leg.getStatus());

		Collection<Airport> airports = this.repository.findAllAirports();
		SelectChoices departureChoices = SelectChoices.from(airports, "iataCode", leg.getDepartureAirport());
		SelectChoices arrivalChoices = SelectChoices.from(airports, "iataCode", leg.getArrivalAirport());

		Collection<Aircraft> aircrafts = this.repository.findAllAircrafts();
		SelectChoices aircraftChoices = SelectChoices.from(aircrafts, "registrationNumber", leg.getAircraft());

		// 2) Desenlazamos los campos básicos del Leg
		Dataset dataset = super.unbindObject(leg, "flightNumber", "scheduledDeparture", "scheduledArrival", "status", "departureAirport", "arrivalAirport", "aircraft", "draftMode");

		// 3) Añadimos al dataset las claves y colecciones para los selects
		dataset.put("statuses", statusChoices);
		dataset.put("status", statusChoices.getSelected().getKey());

		dataset.put("departureAirports", departureChoices);
		dataset.put("departureAirport", departureChoices.getSelected().getKey());

		dataset.put("arrivalAirports", arrivalChoices);
		dataset.put("arrivalAirport", arrivalChoices.getSelected().getKey());

		dataset.put("aircraftChoices", aircraftChoices);
		dataset.put("aircraft", aircraftChoices.getSelected().getKey());

		// 4) Enviamos todo al response
		super.getResponse().addData(dataset);
	}

	@Override
	public void bind(final Leg leg) {
		// Se enlazan los atributos editables y las relaciones necesarias.
		super.bindObject(leg, "flightNumber", "scheduledDeparture", "scheduledArrival", "status");
		super.bindObject(leg, "departureAirport", "arrivalAirport", "aircraft");
	}
}
