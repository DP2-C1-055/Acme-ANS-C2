
package acme.constraints;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.validation.AbstractValidator;
import acme.entities.leg.Leg;
import acme.features.administrator.airline.AdministratorAirlineRepository;

public class LegValidator extends AbstractValidator<ValidLeg, Leg> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AdministratorAirlineRepository airlineRepository;

	// ConstraintValidator interface ------------------------------------------


	@Override
	protected void initialise(final ValidLeg annotation) {
		assert annotation != null;
	}

	@Override
	public boolean isValid(final Leg leg, final ConstraintValidatorContext context) {
		assert context != null;

		boolean result;

		if (leg == null)
			super.state(context, false, "*", "javax.validation.constraints.NotNull.message");
		else {
			// Validar que el flightNumber comienza con el IATA code (los 3 primeros caracteres)
			{
				String flightNumber = leg.getFlightNumber();
				boolean airlineExists = flightNumber != null && flightNumber.length() >= 3 && this.airlineRepository.existsByIataCode(flightNumber.substring(0, 3));
				super.state(context, airlineExists, "flightNumber", "acme.validation.leg.nonexistent-airline.message");
			}
			// Validar que la salida (scheduledDeparture) es anterior a la llegada (scheduledArrival)
			{
				if (leg.getScheduledDeparture() != null && leg.getScheduledArrival() != null) {
					boolean validSchedule = leg.getScheduledDeparture().before(leg.getScheduledArrival());
					super.state(context, validSchedule, "scheduledDeparture", "acme.validation.leg.departure-before-arrival.message");
				}
			}
		}

		result = !super.hasErrors(context);
		return result;
	}
}
