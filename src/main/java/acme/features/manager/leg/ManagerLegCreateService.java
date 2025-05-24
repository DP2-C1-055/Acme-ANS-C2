
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
		boolean status = super.getRequest().getPrincipal().hasRealmOfType(Manager.class);

		int masterId = super.getRequest().getData("masterId", int.class);
		Flight flight = this.repository.findFlightById(masterId);
		if (masterId != 0)
			if (flight == null)
				status = false;
			else if (!flight.isDraftMode() || !super.getRequest().getPrincipal().hasRealm(flight.getManager()))
				status = false;

		if ("POST".equals(super.getRequest().getMethod())) {
			int aircraftId = super.getRequest().getData("aircraft", int.class);
			if (aircraftId != 0 && this.repository.findAircraftById(aircraftId) == null)
				status = false;

			int departureAirportId = super.getRequest().getData("departureAirport", int.class);
			if (departureAirportId != 0 && this.repository.findAirportById(departureAirportId) == null)
				status = false;
			int arrivalAirportId = super.getRequest().getData("arrivalAirport", int.class);
			if (arrivalAirportId != 0 && this.repository.findAirportById(arrivalAirportId) == null)
				status = false;
		}

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int flightId = super.getRequest().getData("masterId", int.class);
		super.getResponse().addGlobal("masterId", flightId);
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
		Date now = MomentHelper.getCurrentMoment();
		if (leg.getScheduledDeparture() != null)
			super.state(MomentHelper.isAfterOrEqual(leg.getScheduledDeparture(), now), "scheduledDeparture", "acme.validation.leg.departure-after-current.message");
		if (leg.getScheduledArrival() != null)
			super.state(MomentHelper.isAfterOrEqual(leg.getScheduledArrival(), now), "scheduledArrival", "acme.validation.leg.arrival-after-current.message");
	}

	@Override
	public void perform(final Leg leg) {
		this.repository.save(leg);
	}

	@Override
	public void unbind(final Leg leg) {
		Dataset dataset = super.unbindObject(leg, "flightNumber", "scheduledDeparture", "scheduledArrival", "status", "draftMode");

		SelectChoices statusChoices = SelectChoices.from(LegStatus.class, leg.getStatus());
		dataset.put("statuses", statusChoices);

		Collection<Airport> airports = this.repository.findAllAirports();
		SelectChoices departureChoices = SelectChoices.from(airports, "iataCode", leg.getDepartureAirport());
		SelectChoices arrivalChoices = SelectChoices.from(airports, "iataCode", leg.getArrivalAirport());

		dataset.put("masterId", super.getRequest().getData("masterId", int.class));

		dataset.put("departureAirport", departureChoices.getSelected().getKey());
		dataset.put("arrivalAirport", arrivalChoices.getSelected().getKey());

		dataset.put("departureAirports", departureChoices);
		dataset.put("arrivalAirports", arrivalChoices);

		Collection<Aircraft> aircrafts = this.repository.findAllAircrafts();
		SelectChoices aircraftChoices = SelectChoices.from(aircrafts, "registrationNumber", leg.getAircraft());

		dataset.put("aircraft", aircraftChoices.getSelected().getKey());
		dataset.put("aircraftChoices", aircraftChoices);

		super.getResponse().addData(dataset);
	}
}
