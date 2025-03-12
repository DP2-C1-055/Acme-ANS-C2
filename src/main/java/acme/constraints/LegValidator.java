
package acme.constraints;

import java.util.Date;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.validation.AbstractValidator;
import acme.client.helpers.MomentHelper;
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
			// Validar que las fechas estén dentro del rango definido en el proyecto
			// TODO: ESTO QUIZÁ HAY QUE HACERLO DE OTRA FORMA. PERO FUNCIONA
			{
				// Se usa el formato "yyyy/MM/dd HH:mm" para parsear las fechas definidas en application.properties
				Date minMoment = MomentHelper.parse("2000/01/01 00:00", "yyyy/MM/dd HH:mm");
				Date maxMoment = MomentHelper.parse("2200/12/31 23:59", "yyyy/MM/dd HH:mm");
				if (leg.getScheduledDeparture() != null) {
					boolean departureInRange = !leg.getScheduledDeparture().before(minMoment) && !leg.getScheduledDeparture().after(maxMoment);
					super.state(context, departureInRange, "scheduledDeparture", "acme.validation.leg.departure-out-of-range.message");
				}
				if (leg.getScheduledArrival() != null) {
					boolean arrivalInRange = !leg.getScheduledArrival().before(minMoment) && !leg.getScheduledArrival().after(maxMoment);
					super.state(context, arrivalInRange, "scheduledArrival", "acme.validation.leg.arrival-out-of-range.message");
				}
			}
		}

		result = !super.hasErrors(context);
		return result;
	}
}
