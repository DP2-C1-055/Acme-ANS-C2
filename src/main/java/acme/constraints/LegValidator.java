
package acme.constraints;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.validation.AbstractValidator;
import acme.entities.leg.Leg;
import acme.features.administrator.airline.AdministratorAirlineRepository;
import acme.features.manager.leg.ManagerLegRepository;

public class LegValidator extends AbstractValidator<ValidLeg, Leg> {

	// Internal state ---------------------------------------------------------
	@Autowired
	private AdministratorAirlineRepository	airlineRepository;

	@Autowired
	private ManagerLegRepository			legRepository;


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
			// Validar que el flightNumber sea único mediante findByFlightNumber
			{
				String flightNumber = leg.getFlightNumber();
				if (flightNumber != null) {
					Leg existing = this.legRepository.findByFlightNumber(flightNumber);
					boolean unique = existing == null || existing.getId() == leg.getId();
					super.state(context, unique, "flightNumber", "acme.validation.leg.flightNumber.unique.message");
				}
			}
			// Validar que la salida (scheduledDeparture) es anterior a la llegada (scheduledArrival)
			{
				if (leg.getScheduledDeparture() != null && leg.getScheduledArrival() != null) {
					boolean validSchedule = leg.getScheduledDeparture().before(leg.getScheduledArrival());
					super.state(context, validSchedule, "scheduledDeparture", "acme.validation.leg.departure-before-arrival.message");
				}
			}
			// Validar que el aeropuerto de salida y el de llegada sean diferentes
			{
				if (leg.getDepartureAirport() != null && leg.getArrivalAirport() != null) {
					boolean differentAirports = leg.getDepartureAirport().getId() != leg.getArrivalAirport().getId();
					super.state(context, differentAirports, "arrivalAirport", "acme.validation.leg.airport-different.message");
				}
			}
		}

		result = !super.hasErrors(context);
		return result;
	}
}
