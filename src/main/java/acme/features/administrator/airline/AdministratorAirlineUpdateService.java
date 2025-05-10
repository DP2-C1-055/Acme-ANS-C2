
package acme.features.administrator.airline;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Administrator;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.airline.AirLineType;
import acme.entities.airline.Airline;

@GuiService
public class AdministratorAirlineUpdateService extends AbstractGuiService<Administrator, Airline> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AdministratorAirlineRepository repository;

	// AbstractGuiService interfaced ------------------------------------------


	@Override
	public void authorise() {
		boolean status;

		status = super.getRequest().getPrincipal().hasRealmOfType(Administrator.class);

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Airline airline;
		int id;

		id = super.getRequest().getData("id", int.class);
		airline = this.repository.findAirlineById(id);

		super.getBuffer().addData(airline);
	}

	@Override
	public void bind(final Airline object) {
		assert object != null;

		super.bindObject(object, "name", "iataCode", "website", "airlineType", "foundationMoment", "email", "phoneNumber");
	}

	@Override
	public void validate(final Airline object) {
		boolean confirmation;

		confirmation = super.getRequest().getData("confirmation", boolean.class);
		boolean isIataCodeChange = false;
		Collection<String> allIataCode = this.repository.getAllIataCode();
		Airline airline = this.repository.findAirlineById(object.getId());

		Date newDate = super.getRequest().getData("foundationMoment", Date.class);

		if (newDate != null && MomentHelper.isFuture(newDate))
			super.state(false, "foundationMoment", "administrator.airline.update.dateInTheFuture");

		if (!super.getBuffer().getErrors().hasErrors("iataCode")) {
			isIataCodeChange = !airline.getIataCode().equals(object.getIataCode());
			super.state(!isIataCodeChange || !allIataCode.contains(object.getIataCode()), "iataCode", "administrator.airline.error.IataCodeDuplicate");
		}

		if (!confirmation)
			super.state(confirmation, "confirmation", "acme.validation.confirmation.message");
	}

	@Override
	public void perform(final Airline airline) {
		this.repository.save(airline);
	}

	@Override
	public void unbind(final Airline object) {
		assert object != null;

		Dataset dataset;
		dataset = super.unbindObject(object, "name", "iataCode", "website", "airlineType", "foundationMoment", "email", "phoneNumber");
		dataset.put("airlineTypeChoices", SelectChoices.from(AirLineType.class, object.getAirlineType()));
		super.getResponse().addData(dataset);

	}

}
