
package acme.features.manager.flight;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.flight.Flight;
import acme.entities.leg.Leg;
import acme.realms.Manager;

@GuiService
public class ManagerFlightPublishService extends AbstractGuiService<Manager, Flight> {

	@Autowired
	private ManagerFlightRepository repository;


	@Override
	public void authorise() {
		int flightId = super.getRequest().getData("id", int.class);
		Flight flight = this.repository.findFlightById(flightId);
		Manager manager = flight == null ? null : flight.getManager();
		// Solo se permite publicar si el flight existe, está en modo borrador y pertenece al manager.
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
		// No es necesaria la vinculación de atributos adicionales para la publicación.
	}

	@Override
	public void validate(final Flight flight) {
		Collection<Leg> legs = this.repository.findLegsByFlightId(flight.getId());
		// Se debe tener al menos un leg para poder publicar el flight.
		super.state(!legs.isEmpty(), "*", "manager.flight.publish.no-leg");
		// Todos los legs deben estar publicados (draftMode == false).
		boolean allPublished = legs.stream().allMatch(leg -> !leg.isDraftMode());
		super.state(allPublished, "*", "manager.flight.publish.legs-not-published");
	}

	@Override
	public void perform(final Flight flight) {
		flight.setDraftMode(false);
		this.repository.save(flight);
	}

	@Override
	public void unbind(final Flight flight) {
	    Dataset dataset = super.unbindObject(flight, "tag", "selfTransfer", "cost", "description", "draftMode");
	    // Extraer manualmente el nombre del Manager
	    String managerName = "";
	    if (flight.getManager() != null && flight.getManager().getIdentity() != null) {
	        managerName = flight.getManager().getIdentity().getFullName();
	    }
	    dataset.put("manager", managerName);
	    super.getResponse().addData(dataset);
	}

}
