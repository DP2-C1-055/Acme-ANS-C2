
package acme.constraints;

import javax.validation.ConstraintValidatorContext;

import acme.client.components.validation.AbstractValidator;
import acme.entities.customer.Customer;

public class CustomerValidator extends AbstractValidator<ValidCustomer, Customer> {

	// Internal state ---------------------------------------------------------

	@Override
	public void initialise(final ValidCustomer annotation) {
		assert annotation != null;
	}

	@Override
	public boolean isValid(final Customer customer, final ConstraintValidatorContext context) {
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
		return result;

	}

}
