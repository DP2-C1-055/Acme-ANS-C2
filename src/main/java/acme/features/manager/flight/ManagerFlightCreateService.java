
package acme.features.manager.flight;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.flight.Flight;
import acme.realms.Manager;

@GuiService
public class ManagerFlightCreateService extends AbstractGuiService<Manager, Flight> {

	@Autowired
	private ManagerFlightRepository repository;


	@Override
	public void authorise() {
		// Se autoriza a cualquier manager para crear un flight.
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		Flight flight = new Flight();
		// El flight se crea en modo borrador.
		flight.setDraftMode(true);
		// Se asigna el manager actual como propietario.
		flight.setManager((Manager) super.getRequest().getPrincipal().getActiveRealm());
		super.getBuffer().addData(flight);
	}

	@Override
	public void bind(final Flight flight) {
		// Se enlazan los atributos editables del flight.
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
		Dataset dataset = super.unbindObject(flight, "tag", "selfTransfer", "cost", "description", "draftMode");
		super.getResponse().addData(dataset);
	}
}
