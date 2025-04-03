
package acme.constraints;

import java.util.Collection;

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
			// Validar que ninguno de los atributos obligatorios sea nulo
			super.state(context, leg.getFlightNumber() != null, "flightNumber", "acme.validation.leg.null.flightNumber");
			super.state(context, leg.getScheduledDeparture() != null, "scheduledDeparture", "acme.validation.leg.null.scheduledDeparture");
			super.state(context, leg.getScheduledArrival() != null, "scheduledArrival", "acme.validation.leg.null.scheduledArrival");
			super.state(context, leg.getStatus() != null, "status", "acme.validation.leg.null.status");
			super.state(context, leg.getDepartureAirport() != null, "departureAirport", "acme.validation.leg.null.departureAirport");
			super.state(context, leg.getArrivalAirport() != null, "arrivalAirport", "acme.validation.leg.null.arrivalAirport");
			super.state(context, leg.getAircraft() != null, "aircraft", "acme.validation.leg.null.aircraft");
			super.state(context, leg.getFlight() != null, "flight", "acme.validation.leg.null.flight");

			// Validar que el flightNumber comienza con el IATA code (los 3 primeros caracteres)
			{
				String flightNumber = leg.getFlightNumber();
				if (flightNumber != null && flightNumber.length() >= 3) {
					boolean airlineExists = this.airlineRepository.existsByIataCode(flightNumber.substring(0, 3));
					super.state(context, airlineExists, "flightNumber", "acme.validation.leg.nonexistent-airline.message");
				}
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

			// Comprobar que no se solape en el tiempo con otros Legs del mismo vuelo.
			if (leg.getFlight() != null && leg.getScheduledDeparture() != null && leg.getScheduledArrival() != null) {
				Collection<Leg> flightLegs = this.legRepository.findLegsByFlightId(leg.getFlight().getId());
				if (flightLegs != null)
					for (Leg otherLeg : flightLegs) {
						// Evitar comparar consigo mismo (por ID)
						if (leg.getId() == otherLeg.getId())
							continue;
						// Si el otro leg no tiene fechas definidas, se omite
						if (otherLeg.getScheduledDeparture() == null || otherLeg.getScheduledArrival() == null)
							continue;
						// Se comprueba que los intervalos no se solapen:
						// El leg actual se solapa con otroLeg si:
						// scheduledDeparture (actual) < scheduledArrival (otro) &&
						// scheduledDeparture (otro) < scheduledArrival (actual)
						if (leg.getScheduledDeparture().before(otherLeg.getScheduledArrival()) && otherLeg.getScheduledDeparture().before(leg.getScheduledArrival())) {
							super.state(context, false, "scheduledDeparture", "acme.validation.leg.legs-overlap.message");
							break;
						}
					}
			}

			// Nueva validación: el Aircraft no debe estar asignado en otro Leg que se solape en el tiempo.
			if (leg.getAircraft() != null && leg.getScheduledDeparture() != null && leg.getScheduledArrival() != null) {
				// Se asume que existe el método findLegsByAircraftId en legRepository.
				Collection<Leg> aircraftLegs = this.legRepository.findLegsByAircraftId(leg.getAircraft().getId());
				if (aircraftLegs != null)
					for (Leg otherLeg : aircraftLegs) {
						// Evitar comparar consigo mismo.
						if (leg.getId() == otherLeg.getId())
							continue;
						if (otherLeg.getScheduledDeparture() == null || otherLeg.getScheduledArrival() == null)
							continue;
						// Se comprueba el solapamiento en el uso del mismo Aircraft:
						if (leg.getScheduledDeparture().before(otherLeg.getScheduledArrival()) && otherLeg.getScheduledDeparture().before(leg.getScheduledArrival())) {
							super.state(context, false, "aircraft", "acme.validation.leg.aircraft-overlap.message");
							break;
						}
					}
			}
		}

		result = !super.hasErrors(context);
		return result;
	}
}
