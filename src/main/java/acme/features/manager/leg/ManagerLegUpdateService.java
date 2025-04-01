
package acme.features.manager.leg;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
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
		// Se permite actualizar sólo si la leg existe, está en modo borrador y el manager es el propietario del flight asociado.
		boolean status = leg != null && leg.isDraftMode() && super.getRequest().getPrincipal().hasRealm(leg.getFlight().getManager());
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
		// Se enlazan los atributos editables y las relaciones necesarias.
		super.bindObject(leg, "flightNumber", "scheduledDeparture", "scheduledArrival", "status");
		super.bindObject(leg, "departureAirport", "arrivalAirport", "aircraft");
	}

	@Override
	public void validate(final Leg leg) {
		// La validación se realizará mediante las anotaciones y el validador custom de la entidad Leg.
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
