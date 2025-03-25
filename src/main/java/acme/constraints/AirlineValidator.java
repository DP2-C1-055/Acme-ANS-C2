
package acme.constraints;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.validation.AbstractValidator;
import acme.client.components.validation.Validator;
import acme.entities.airline.Airline;
import acme.entities.airline.AirlineRepository;

@Validator
public class AirlineValidator extends AbstractValidator<ValidAirline, Airline> {

	@Autowired
	private AirlineRepository		repo;

	private static final Pattern	IATA_PATTERN	= Pattern.compile("^[A-Z]{3}$");


	@Override
	protected void initialise(final ValidAirline annotation) {
		assert annotation != null;
	}

	@Override
	public boolean isValid(final Airline airline, final ConstraintValidatorContext context) {
		assert context != null;

		if (airline == null) {
			super.state(context, false, "*", "javax.validation.constraints.NotNull.message");
			return false;
		}

		String iataCode = airline.getIataCode();
		if (iataCode == null)
			super.state(context, false, "iataCode", "javax.validation.constraints.NotNull.message");
		else {

			boolean matchesPattern = AirlineValidator.IATA_PATTERN.matcher(iataCode).matches();
			super.state(context, matchesPattern, "iataCode", "acme.validation.airline.invalid-iataCode.message");

			if (matchesPattern) {
				Airline existing = this.repo.findByIataCode(iataCode);
				boolean unique = true;
				if (existing != null)
					unique = airline.getId() == existing.getId();
				super.state(context, unique, "iataCode", "acme.validation.airline.duplicated-iataCode.message");
			}
		}
		return !super.hasErrors(context);
	}
}
