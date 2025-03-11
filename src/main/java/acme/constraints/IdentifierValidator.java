
package acme.constraints;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.principals.DefaultUserIdentity;
import acme.client.components.validation.AbstractValidator;
import acme.features.authenticated.manager.AuthenticatedManagerRepository;
import acme.realms.Manager;

public class IdentifierValidator extends AbstractValidator<ValidIdentifier, Manager> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AuthenticatedManagerRepository repository;


	@Override
	protected void initialise(final ValidIdentifier annotation) {
		assert annotation != null;
	}

	@Override
	public boolean isValid(final Manager manager, final ConstraintValidatorContext context) {
		assert context != null;

		boolean result;

		if (manager == null)
			super.state(context, false, "*", "javax.validation.constraints.NotNull.message");
		else {
			{
				// Validar que el identifier cumple con el patrón "^[A-Z]{2,3}\d{6}$"
				String identifier = manager.getIdentifier();
				boolean matchesPattern = identifier != null && identifier.matches("^[A-Z]{2,3}\\d{6}$");
				super.state(context, matchesPattern, "identifier", "acme.validation.manager.invalid-identifier.message");
			}
			{
				// Validar que el identifier sea único
				Manager existingManager = this.repository.findManagerByIdentifier(manager.getIdentifier());
				boolean uniqueIdentifier = existingManager == null || existingManager.equals(manager);
				super.state(context, uniqueIdentifier, "identifier", "acme.validation.manager.duplicated-identifier.message");
			}
			{
				// Validar que las dos primeras letras del identifier coinciden con las iniciales de la identidad.
				String identifier = manager.getIdentifier();
				DefaultUserIdentity identity = manager.getIdentity();
				if (identifier != null && identity != null && identifier.length() >= 2) {
					String name = identity.getName();
					String surname = identity.getSurname();
					// Se valida que la primera letra de identifier coincide con la del name,
					// y la segunda letra de identifier coincide con la del surname.
					boolean initialsValid = false;
					if (name != null && surname != null && !name.isEmpty() && !surname.isEmpty()) {
						char initialName = name.charAt(0);
						char initialSurname = surname.charAt(0);
						initialsValid = identifier.charAt(0) == initialName && identifier.charAt(1) == initialSurname;
					}
					super.state(context, initialsValid, "identifier", "acme.validation.manager.incorrect-initials.message");
				}
			}
		}

		result = !super.hasErrors(context);
		return result;
	}
}
