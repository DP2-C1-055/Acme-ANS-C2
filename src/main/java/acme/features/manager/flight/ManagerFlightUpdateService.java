
package acme.features.manager.flight;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.flight.Flight;
import acme.realms.Manager;

@GuiService
public class ManagerFlightUpdateService extends AbstractGuiService<Manager, Flight> {

	@Autowired
	private ManagerFlightRepository repository;


	@Override
	public void authorise() {
		int flightId = super.getRequest().getData("id", int.class);
		Flight flight = this.repository.findFlightById(flightId);
		Manager manager = flight == null ? null : flight.getManager();
		// Se permite actualizar solo si el flight existe, está en modo borrador y pertenece al manager.
		boolean status = flight != null && flight.isDraftMode() && super.getRequest().getPrincipal().hasRealm(manager);
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int id = super.getRequest().getData("id", int.class);
		Flight flight = this.repository.findFlightById(id);
		super.getBuffer().addData(flight);
	}

	@Override
	public void bind(final Flight flight) {
		// Se enlazan los atributos editables.
		super.bindObject(flight, "tag", "selfTransfer", "cost", "description");
	}

	@Override
	public void validate(final Flight flight) {
	}

	@Override
	public void perform(final Flight flight) {
		this.repository.save(flight);
	}

	@Override
	public void unbind(final Flight flight) {
		Dataset dataset = super.unbindObject(flight, "tag", "selfTransfer", "cost", "description", "draftMode", "scheduledDeparture", "scheduledArrival", "originCity", "destinationCity", "numberOfLayovers");
		String managerName = "";
		if (flight.getManager() != null && flight.getManager().getIdentity() != null)
			managerName = flight.getManager().getIdentity().getFullName();
		dataset.put("manager", managerName);
		super.getResponse().addData(dataset);
	}
}
