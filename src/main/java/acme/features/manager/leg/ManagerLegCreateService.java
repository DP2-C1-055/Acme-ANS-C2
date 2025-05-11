
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
import acme.entities.flight.Flight;
import acme.entities.leg.Leg;
import acme.entities.leg.LegStatus;
import acme.features.manager.flight.ManagerFlightRepository;
import acme.realms.Manager;

@GuiService
public class ManagerLegCreateService extends AbstractGuiService<Manager, Leg> {

	@Autowired
	private ManagerLegRepository	repository;

	@Autowired
	private ManagerFlightRepository	flightRepository;


	@Override
	public void authorise() {
		int flightId = super.getRequest().getData("masterId", int.class);
		Flight flight = this.flightRepository.findFlightById(flightId);
		boolean status = flight != null && super.getRequest().getPrincipal().hasRealm(flight.getManager());
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int flightId = super.getRequest().getData("masterId", int.class);
		// Se añade el masterId como dato global
		super.getResponse().addGlobal("masterId", flightId);
		// Se obtiene el Flight real desde el repositorio
		Flight flight = this.flightRepository.findFlightById(flightId);
		Leg leg = new Leg();
		leg.setFlight(flight);
		leg.setDraftMode(true);
		super.getBuffer().addData(leg);
	}

	@Override
	public void bind(final Leg leg) {
		super.bindObject(leg, "flightNumber", "scheduledDeparture", "scheduledArrival", "status");
		super.bindObject(leg, "departureAirport", "arrivalAirport", "aircraft");
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
	}

	@Override
	public void perform(final Leg leg) {
		this.repository.save(leg);
	}

	@Override
	public void unbind(final Leg leg) {
		Dataset dataset = super.unbindObject(leg, "flightNumber", "scheduledDeparture", "scheduledArrival", "status", "draftMode");

		// Opciones para el enumerado LegStatus:
		SelectChoices statusChoices = SelectChoices.from(LegStatus.class, leg.getStatus());
		dataset.put("statuses", statusChoices);

		// Opciones para las relaciones: departureAirport y arrivalAirport:
		Collection<Airport> airports = this.repository.findAllAirports();
		SelectChoices departureChoices = SelectChoices.from(airports, "iataCode", leg.getDepartureAirport());
		SelectChoices arrivalChoices = SelectChoices.from(airports, "iataCode", leg.getArrivalAirport());
		dataset.put("departureAirports", departureChoices);
		dataset.put("arrivalAirports", arrivalChoices);

		// Opciones para la relación Aircraft:
		Collection<Aircraft> aircrafts = this.repository.findAllAircrafts();
		SelectChoices aircraftChoices = SelectChoices.from(aircrafts, "model", leg.getAircraft());
		dataset.put("aircraftChoices", aircraftChoices);

		super.getResponse().addData(dataset);
	}
}
