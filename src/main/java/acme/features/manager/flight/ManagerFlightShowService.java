
package acme.features.manager.flight;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.flight.Flight;
import acme.realms.Manager;

@GuiService
public class ManagerFlightShowService extends AbstractGuiService<Manager, Flight> {

	@Autowired
	private ManagerFlightRepository repository;


	@Override
	public void authorise() {
		int flightId = super.getRequest().getData("id", int.class);
		Flight flight = this.repository.findFlightById(flightId);
		Manager manager = flight == null ? null : flight.getManager();
		boolean status = flight != null && super.getRequest().getPrincipal().hasRealm(manager);
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int id = super.getRequest().getData("id", int.class);
		Flight flight = this.repository.findFlightById(id);
		super.getBuffer().addData(flight);
	}

	@Override
	public void unbind(final Flight flight) {
		// Incluye todos los campos, tanto “simples” como los derivados
		Dataset dataset = super.unbindObject(flight, "tag", "selfTransfer", "cost", "description", "draftMode", "scheduledDeparture", "scheduledArrival", "originCity", "destinationCity", "numberOfLayovers");

		// Meter el nombre del Manager a mano
		String managerName = "";
		if (flight.getManager() != null && flight.getManager().getIdentity() != null)
			managerName = flight.getManager().getIdentity().getFullName();
		dataset.put("manager", managerName);

		// Añadir el Dataset a la Response
		super.getResponse().addData(dataset);
	}

}
