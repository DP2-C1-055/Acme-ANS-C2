
package acme.features.administrator.airline;

import java.util.Arrays;
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
public class AdministratorAirlineCreateService extends AbstractGuiService<Administrator, Airline> {

	@Autowired
	private AdministratorAirlineRepository repository;


	@Override
	public void authorise() {
		boolean status = false;
		status = super.getRequest().getPrincipal().hasRealmOfType(Administrator.class);
		if (super.getRequest().getMethod().equals("POST")) {
			String airlineType = super.getRequest().getData("airlineType", String.class);
			if (!airlineType.equals("0"))
				status = Arrays.stream(AirLineType.values()).anyMatch(tc -> tc.name().equalsIgnoreCase(airlineType));
		}

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Airline airline;

		airline = new Airline();

		super.getBuffer().addData(airline);
	}

	@Override
	public void validate(final Airline airline) {
		boolean confirmation;

		confirmation = super.getRequest().getData("confirmation", boolean.class);
		String newIataCode = super.getRequest().getData("iataCode", String.class);
		boolean existIataCode = this.repository.existsByIataCode(newIataCode);

		Date newDate = super.getRequest().getData("foundationMoment", Date.class);

		if (newDate != null && MomentHelper.isFuture(newDate))
			super.state(false, "foundationMoment", "administrator.airline.update.dateInTheFuture");

		if (existIataCode)
			super.state(false, "iataCode", "acme.validation.airport.duplicated-iataCode.message");

		if (!confirmation)
			super.state(confirmation, "confirmation", "acme.validation.confirmation.message");
	}

	@Override
	public void perform(final Airline object) {

		this.repository.save(object);
	}

	@Override
	public void unbind(final Airline object) {
		assert object != null;

		Dataset dataset;
		dataset = super.unbindObject(object, "name", "iataCode", "website", "airlineType", "foundationMoment", "email", "phoneNumber");
		dataset.put("airlineTypeChoices", SelectChoices.from(AirLineType.class, object.getAirlineType()));
		super.getResponse().addData(dataset);

	}

	@Override
	public void bind(final Airline object) {
		assert object != null;

		super.bindObject(object, "name", "iataCode", "website", "airlineType", "foundationMoment", "email", "phoneNumber");
	}

}
