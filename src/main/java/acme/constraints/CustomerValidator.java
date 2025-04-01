
package acme.constraints;

import java.util.List;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.validation.AbstractValidator;
import acme.realms.Customer.Customer;
import acme.realms.Customer.CustomerRepository;

public class CustomerValidator extends AbstractValidator<ValidCustomer, Customer> {

	// Internal state ---------------------------------------------------------

	@Override
	public void initialise(final ValidCustomer annotation) {
		assert annotation != null;
	}


	@Autowired
	private CustomerRepository repository;


	@Override
	public boolean isValid(final Customer customer, final ConstraintValidatorContext context) {

		List<String> identifiers = this.repository.findAllIdentifiers();
		assert context != null;

		boolean result = true;

		if (customer == null)
			return false;
		String name = customer.getIdentity().getName().trim();
		String surname = customer.getIdentity().getSurname().trim();
		String initials = name.substring(0, 1) + surname.subSequence(0, 1);

		if (!customer.getIdentifier().startsWith(initials)) {
			super.state(context, false, "customers", "acme.validation.customer.identifier.message");
			result = false;
		}

		if (!identifiers.contains(customer.getIdentifier())) {
			super.state(context, false, "customers", "acme.validation.customer.identifier.message");
			result = false;
		}

		return result;

	}

}
