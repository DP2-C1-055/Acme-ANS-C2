
package acme.features.administrator.aircraft;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Administrator;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.aircraft.Aircraft;
import acme.entities.aircraft.ServiceStatus;
import acme.entities.airline.Airline;

@GuiService
public class AdministratorAircraftShowService extends AbstractGuiService<Administrator, Aircraft> {

	@Autowired
	private AdministratorAircraftRepository repository;


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		int id;
		Aircraft aircraft;

		id = super.getRequest().getData("id", int.class);
		aircraft = this.repository.findAircraftById(id);

		super.getBuffer().addData(aircraft);
	}

	@Override
	public void unbind(final Aircraft aircraft) {
		Dataset dataset;
		Collection<Airline> airlines;
		SelectChoices statusChoices, airlineChoices;

		airlines = this.repository.findAllAirlines();

		statusChoices = SelectChoices.from(ServiceStatus.class, aircraft.getStatus());
		airlineChoices = SelectChoices.from(airlines, "name", aircraft.getAirline());

		dataset = super.unbindObject(aircraft, "model", "registrationNumber", "capacity", "cargoWeight", "details");
		dataset.put("confirmation", false);
		dataset.put("readonly", true);
		dataset.put("statuses", statusChoices);
		dataset.put("status", statusChoices.getSelected().getKey());
		dataset.put("airlines", airlineChoices);
		dataset.put("airline", airlineChoices.getSelected().getKey());

		super.getResponse().addData(dataset);
	}

}
