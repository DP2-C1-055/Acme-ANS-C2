
package acme.features.manager.leg;

import java.util.Arrays;
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
public class ManagerLegUpdateService extends AbstractGuiService<Manager, Leg> {

	@Autowired
	private ManagerLegRepository repository;


	@Override
	public void authorise() {
		int legId = super.getRequest().getData("id", int.class);
		Leg leg = this.repository.findLegById(legId);
		boolean status = leg != null && leg.isDraftMode() && super.getRequest().getPrincipal().hasRealm(leg.getFlight().getManager());

		if ("POST".equals(super.getRequest().getMethod())) {
			String legStatus = super.getRequest().getData("status", String.class);
			if (!legStatus.equals("0"))
				status = Arrays.stream(LegStatus.values()).anyMatch(tc -> tc.name().equalsIgnoreCase(legStatus));

			int aircraftId = super.getRequest().getData("aircraft", int.class);
			if (aircraftId != 0 && this.repository.findAircraftById(aircraftId) == null)
				status = false;

			int departureId = super.getRequest().getData("departureAirport", int.class);
			if (departureId != 0 && this.repository.findAirportById(departureId) == null)
				status = false;

			int arrivalId = super.getRequest().getData("arrivalAirport", int.class);
			if (arrivalId != 0 && this.repository.findAirportById(arrivalId) == null)
				status = false;
		}

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int legId = super.getRequest().getData("id", int.class);
		Leg leg = this.repository.findLegById(legId);
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
