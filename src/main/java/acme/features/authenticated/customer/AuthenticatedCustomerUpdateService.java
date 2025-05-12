
package acme.features.authenticated.customer;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Authenticated;
import acme.client.helpers.PrincipalHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.realms.Customer.Customer;

@GuiService
public class AuthenticatedCustomerUpdateService extends AbstractGuiService<Authenticated, Customer> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AuthenticatedCustomerRepository repository;

	// AbstractService interface ----------------------------------------------ç


	@Override
	public void authorise() {
		boolean status;

		status = super.getRequest().getPrincipal().hasRealmOfType(Customer.class);

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Customer object;
		int userAccountId;

		userAccountId = super.getRequest().getPrincipal().getAccountId();
		object = this.repository.findCustomerByUserAccountId(userAccountId);

		super.getBuffer().addData(object);
	}

	@Override
	public void bind(final Customer object) {
		assert object != null;

		super.bindObject(object, "identifier", "phoneNumber", "address", "city", "country", "earnedPoints");
	}

	@Override
	public void validate(final Customer object) {
		boolean isCustomerIdentifierChange = false;
		int userAccountId;
		Collection<String> allIdentfier = this.repository.findAllIdentifier();
		userAccountId = super.getRequest().getPrincipal().getAccountId();
		Customer customer = this.repository.findCustomerByUserAccountId(userAccountId);

		if (!super.getBuffer().getErrors().hasErrors("allIdentfier")) {
			isCustomerIdentifierChange = !customer.getIdentifier().equals(object.getIdentifier());
			super.state(!isCustomerIdentifierChange || !allIdentfier.contains(object.getIdentifier()), "identifier", "customer.error.identifierCodeDuplicate");
			boolean var = !isCustomerIdentifierChange || !allIdentfier.contains(object.getIdentifier());
			System.out.println(var);
		}
	}

	@Override
	public void perform(final Customer object) {
		assert object != null;

		this.repository.save(object);
	}

	@Override
	public void unbind(final Customer object) {
		Dataset dataset;

		dataset = super.unbindObject(object, "identifier", "phoneNumber", "address", "city", "country", "earnedPoints");

		super.getResponse().addData(dataset);
	}

	@Override
	public void onSuccess() {
		if (super.getRequest().getMethod().equals("POST"))
			PrincipalHelper.handleUpdate();
	}

}
