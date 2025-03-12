
package acme.constraints;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.validation.AbstractValidator;
import acme.entities.leg.Leg;
import acme.features.administrator.airline.AdministratorAirlineRepository;
import acme.features.manager.leg.ManagerLegRepository;

public class FlightNumberValidator extends AbstractValidator<ValidFlightNumber, Leg> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private ManagerLegRepository			legRepository;

	@Autowired
	private AdministratorAirlineRepository	airlineRepository;

	private static final Pattern			FLIGHT_PATTERN	= Pattern.compile("^[A-Z]{2}\\d{4}$");

	// ConstraintValidator interface ------------------------------------------


	@Override
	protected void initialise(final ValidFlightNumber annotation) {
		assert annotation != null;
	}

	@Override
	public boolean isValid(final Leg leg, final ConstraintValidatorContext context) {
		assert context != null;

		boolean result;

		if (leg == null)
			super.state(context, false, "*", "javax.validation.constraints.NotNull.message");
		else {
			{
				boolean matchesPattern = leg.getFlightNumber() != null && FlightNumberValidator.FLIGHT_PATTERN.matcher(leg.getFlightNumber()).matches();
				super.state(context, matchesPattern, "flightNumber", "acme.validation.leg.invalid-flight-number.message");
			}
			{
				boolean airlineExists = leg.getFlightNumber() != null && this.airlineRepository.existsByIataCode(leg.getFlightNumber().substring(0, 2));
				super.state(context, airlineExists, "flightNumber", "acme.validation.leg.nonexistent-airline.message");
			}
			{
				boolean uniqueFlightNumber;
				Leg existingLeg;

				existingLeg = this.legRepository.findLegByFlightNumber(leg.getFlightNumber());
				uniqueFlightNumber = existingLeg == null || existingLeg.equals(leg);

				super.state(context, uniqueFlightNumber, "flightNumber", "acme.validation.leg.duplicated-flight-number.message");
			}
		}

		result = !super.hasErrors(context);
		return result;
	}

}
