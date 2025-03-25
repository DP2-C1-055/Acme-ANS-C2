
package acme.features.administrator.airline;

import java.util.Collection;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Administrator;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.airline.Airline;

@GuiService
public class AdministratorAirlineListService extends AbstractGuiService<Administrator, Airline> {

	@Autowired
	private AdministratorAirlineRepository repository;


	@Override
	public void authorise() {
		boolean status;

		status = super.getRequest().getPrincipal().hasRealmOfType(Administrator.class);

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Collection<Airline> airlines;

		airlines = this.repository.findAllAirlines();

		super.getBuffer().addData(airlines);
	}

	@Override
	public void unbind(final Airline object) {
		assert object != null;

		Dataset dataset;
		dataset = super.unbindObject(object, "name", "iataCode", "website", "airlineType", "foundationMoment", "email", "phoneNumber", "draftMode");

		if (object.getDraftMode()) {
			final Locale local = super.getRequest().getLocale();
			String draftmodeText;
			if (local.equals(Locale.ENGLISH))
				draftmodeText = "Yes";
			else
				draftmodeText = "Sí";
			dataset.put("draftMode", draftmodeText);
		} else
			dataset.put("draftMode", "No");

		super.getResponse().addData(dataset);
	}

}
