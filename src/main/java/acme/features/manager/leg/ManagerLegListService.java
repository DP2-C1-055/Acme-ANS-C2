
package acme.features.manager.leg;

import java.util.Collection;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.flight.Flight;
import acme.entities.leg.Leg;
import acme.features.manager.flight.ManagerFlightRepository;
import acme.realms.Manager;

@GuiService
public class ManagerLegListService extends AbstractGuiService<Manager, Leg> {

	@Autowired
	private ManagerLegRepository	repository;

	@Autowired
	private ManagerFlightRepository	flightRepository;


	@Override
	public void authorise() {
		int masterId = super.getRequest().getData("masterId", int.class);
		Flight flight = this.flightRepository.findFlightById(masterId);
		boolean status = flight != null && super.getRequest().getPrincipal().hasRealm(flight.getManager());
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int flightId = super.getRequest().getData("masterId", int.class);
		Collection<Leg> legs = this.repository.findLegsByFlightIdOrderByScheduledDepartureAsc(flightId);
		super.getBuffer().addData(legs);
	}

	@Override
	public void unbind(final Leg leg) {
		assert leg != null;

		Dataset dataset = super.unbindObject(leg, "flightNumber", "scheduledDeparture", "scheduledArrival");

		if (leg.isDraftMode()) {
			final Locale locale = super.getRequest().getLocale();
			String draftModeText;
			if (locale.equals(Locale.ENGLISH))
				draftModeText = "Yes";
			else
				draftModeText = "Sí";
			dataset.put("draftMode", draftModeText);
		} else
			dataset.put("draftMode", "No");

		super.getResponse().addData(dataset);
	}

	@Override
	public void unbind(final Collection<Leg> legs) {
		int masterId = super.getRequest().getData("masterId", int.class);
		Flight flight = this.flightRepository.findFlightById(masterId);
		boolean showCreate = flight.isDraftMode() && super.getRequest().getPrincipal().hasRealm(flight.getManager());
		// Agregamos los atributos globales al response
		super.getResponse().addGlobal("masterId", masterId);
		super.getResponse().addGlobal("showCreate", showCreate);
	}
}
