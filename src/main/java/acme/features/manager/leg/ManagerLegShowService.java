
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
public class ManagerLegShowService extends AbstractGuiService<Manager, Leg> {

	@Autowired
	private ManagerLegRepository repository;


	@Override
	public void authorise() {
		int legId = super.getRequest().getData("id", int.class);
		Leg leg = this.repository.findLegById(legId);
		boolean status = leg != null && super.getRequest().getPrincipal().hasRealm(leg.getFlight().getManager());
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int id = super.getRequest().getData("id", int.class);
		Leg leg = this.repository.findLegById(id);
		super.getBuffer().addData(leg);
	}

	@Override
	public void unbind(final Leg leg) {
		// Unbind de los atributos básicos
		Dataset dataset = super.unbindObject(leg, "flightNumber", "scheduledDeparture", "scheduledArrival", "status", "draftMode");
		// Atributos derivados:
		dataset.put("durationInHours", leg.getDurationInHours());

		// Cargar opciones para las relaciones usando métodos del repositorio:
		Collection<Airport> airports = this.repository.findAllAirports();
		SelectChoices departureAirports = SelectChoices.from(airports, "iataCode", leg.getDepartureAirport());
		SelectChoices arrivalAirports = SelectChoices.from(airports, "iataCode", leg.getArrivalAirport());
		dataset.put("departureAirports", departureAirports);
		dataset.put("arrivalAirports", arrivalAirports);

		Collection<Aircraft> aircrafts = this.repository.findAllAircrafts();
		SelectChoices aircraftChoices = SelectChoices.from(aircrafts, "model", leg.getAircraft());
		dataset.put("aircraftChoices", aircraftChoices);

		// Opciones para el enumerado LegStatus:
		SelectChoices statusChoices = SelectChoices.from(LegStatus.class, leg.getStatus());
		dataset.put("statuses", statusChoices);

		// También, asignar el valor seleccionado para cada relación (para que se muestre en el select)
		dataset.put("departureAirport", departureAirports.getSelected().getKey());
		dataset.put("arrivalAirport", arrivalAirports.getSelected().getKey());
		dataset.put("aircraft", aircraftChoices.getSelected().getKey());
		dataset.put("status", statusChoices.getSelected().getKey());

		super.getResponse().addData(dataset);
	}
}
