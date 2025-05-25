
package acme.features.administrator.airport;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Administrator;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.airport.Airport;
import acme.entities.airport.OperationalScope;

@GuiService
public class AdministratorAirportUpdateService extends AbstractGuiService<Administrator, Airport> {

	@Autowired
	private AdministratorAirportRepository repository;


	@Override
	public void authorise() {
		boolean status = false;
		int airportId;
		airportId = super.getRequest().getData("id", int.class);
		Airport airport;

		airport = this.repository.findAirportById(airportId);

		if (airport != null)
			status = super.getRequest().getPrincipal().hasRealmOfType(Administrator.class);

		if (super.getRequest().getMethod().equals("POST")) {
			String operationalScope = super.getRequest().getData("operationalScope", String.class);
			if (!operationalScope.equals("0"))
				status = Arrays.stream(OperationalScope.values()).anyMatch(tc -> tc.name().equalsIgnoreCase(operationalScope));
		}

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int id = super.getRequest().getData("id", int.class);
		Airport airport = this.repository.findAirportById(id);
		super.getBuffer().addData(airport);
	}

	@Override
	public void bind(final Airport airport) {
		super.bindObject(airport, "name", "iataCode", "operationalScope", "city", "country", "website", "email", "contactPhone");
	}

	@Override
	public void validate(final Airport airport) {
		// Se exige confirmación para proceder con la actualización
		boolean confirmation = super.getRequest().getData("confirmation", boolean.class);
		super.state(confirmation, "confirmation", "administrator.airport.update.not-confirmed");
	}

	@Override
	public void perform(final Airport airport) {
		this.repository.save(airport);
	}

	@Override
	public void unbind(final Airport airport) {
		SelectChoices choices;
		Dataset dataset;

		choices = SelectChoices.from(OperationalScope.class, airport.getOperationalScope());

		dataset = super.unbindObject(airport, "name", "iataCode", "operationalScope", "city", "country", "website", "email", "contactPhone");
		dataset.put("confirmation", false);
		dataset.put("operationalScopes", choices);

		super.getResponse().addData(dataset);
	}
}
