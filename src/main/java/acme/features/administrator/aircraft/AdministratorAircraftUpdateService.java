
package acme.features.administrator.aircraft;

import java.util.Arrays;
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
public class AdministratorAircraftUpdateService extends AbstractGuiService<Administrator, Aircraft> {

	// Internal state ---------------------------------------------------------
	@Autowired
	private AdministratorAircraftRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean status = false;
		int aircraftId;
		aircraftId = super.getRequest().getData("id", int.class);
		Aircraft aircraft;

		aircraft = this.repository.findAircraftById(aircraftId);
		if (aircraft != null)
			status = super.getRequest().getPrincipal().hasRealmOfType(Administrator.class);
		if (super.getRequest().getMethod().equals("POST")) {
			String statusForm = super.getRequest().getData("status", String.class);
			if (!statusForm.equals("0"))
				status = Arrays.stream(ServiceStatus.values()).anyMatch(tc -> tc.name().equalsIgnoreCase(statusForm));
			int airlineId = super.getRequest().getData("airline", int.class);
			Airline airline = this.repository.findAirlineById(airlineId);
			if (airlineId != 0 && airline == null)
				status = false;

		}

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int id = super.getRequest().getData("id", int.class);
		Aircraft aircraft = this.repository.findAircraftById(id);

		super.getBuffer().addData(aircraft);
	}

	@Override
	public void bind(final Aircraft aircraft) {
		super.bindObject(aircraft, "model", "registrationNumber", "capacity", "cargoWeight", "status", "details");

	}

	@Override
	public void validate(final Aircraft object) {
		boolean confirmation;
		boolean isRegistrationNumberChange;
		Collection<String> allRegistrationNumber = this.repository.getAllRegistrationNumber();
		Aircraft aircraft = this.repository.findAircraftById(object.getId());
		isRegistrationNumberChange = !aircraft.getRegistrationNumber().equals(object.getRegistrationNumber());

		if (isRegistrationNumberChange)
			super.state(!allRegistrationNumber.contains(object.getRegistrationNumber()), "registrationNumber", "administrator.aircraft.error.registrationNumber");

		confirmation = super.getRequest().getData("confirmation", boolean.class);
		super.state(confirmation, "confirmation", "acme.validation.confirmation.message");
	}

	@Override
	public void perform(final Aircraft aircraft) {
		this.repository.save(aircraft);
	}

	@Override
	public void unbind(final Aircraft aircraft) {
		Dataset dataset;
		SelectChoices choices;
		SelectChoices selectedAirlines;
		Collection<Airline> airlines;

		choices = SelectChoices.from(ServiceStatus.class, aircraft.getStatus());
		airlines = this.repository.findAllAirlines();
		selectedAirlines = SelectChoices.from(airlines, "name", aircraft.getAirline());

		dataset = super.unbindObject(aircraft, "model", "registrationNumber", "capacity", "cargoWeight", "status", "details");
		dataset.put("statuses", choices);
		dataset.put("confirmation", false);
		dataset.put("airline", selectedAirlines.getSelected().getKey());
		dataset.put("airlines", selectedAirlines);

		super.getResponse().addData(dataset);

	}
}
