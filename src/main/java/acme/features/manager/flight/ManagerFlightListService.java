
package acme.features.manager.flight;

import java.util.Collection;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.flight.Flight;
import acme.realms.Manager;

@GuiService
public class ManagerFlightListService extends AbstractGuiService<Manager, Flight> {

	@Autowired
	private ManagerFlightRepository repository;


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		int managerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		Collection<Flight> flights = this.repository.findFlightsByManagerId(managerId);
		super.getBuffer().addData(flights);
	}

	@Override
	public void unbind(final Flight flight) {
		assert flight != null;

		Dataset dataset = super.unbindObject(flight, "tag", "cost");

		if (flight.isDraftMode()) {
			final Locale locale = super.getRequest().getLocale();
			String draftModeText;
			if (locale.equals(Locale.ENGLISH))
				draftModeText = "Yes";
			else
				draftModeText = "Sí";
			dataset.put("draftMode", draftModeText);
		} else
			dataset.put("draftMode", "No");

		super.addPayload(dataset, flight, "description", "scheduledDeparture",//
			"scheduledArrival", "originCity", "destinationCity", "numberOfLayovers", "manager.identity.fullName");
		super.getResponse().addData(dataset);
	}
}
