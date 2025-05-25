
package acme.features.manager.flight;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.flight.Flight;
import acme.entities.leg.Leg;
import acme.realms.Manager;

@GuiService
public class ManagerFlightDeleteService extends AbstractGuiService<Manager, Flight> {

	@Autowired
	private ManagerFlightRepository repository;


	@Override
	public void authorise() {
		int flightId = super.getRequest().getData("id", int.class);
		Flight flight = this.repository.findFlightById(flightId);
		Manager manager = flight == null ? null : flight.getManager();
		// Se permite eliminar solo si el flight existe, está en modo borrador y pertenece al manager.
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
		super.bindObject(flight, "tag", "selfTransfer", "cost", "description");
	}

	@Override
	public void validate(final Flight flight) {
	}

	@Override
	public void perform(final Flight flight) {
		// Primero se eliminan los legs asociados al flight (si existiesen)
		Collection<Leg> legs = this.repository.findLegsByFlightId(flight.getId());
		this.repository.deleteAll(legs);
		// Luego se elimina el flight
		this.repository.delete(flight);
	}

	@Override
	public void unbind(final Flight flight) {
		assert flight != null;
	}
}
