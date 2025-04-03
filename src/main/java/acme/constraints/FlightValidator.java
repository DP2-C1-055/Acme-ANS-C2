
package acme.constraints;

import java.util.Collection;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.validation.AbstractValidator;
import acme.entities.flight.Flight;
import acme.entities.leg.Leg;
import acme.features.manager.leg.ManagerLegRepository;

public class FlightValidator extends AbstractValidator<ValidFlight, Flight> {

	@Autowired
	private ManagerLegRepository legRepository;


	@Override
	protected void initialise(final ValidFlight annotation) {
		assert annotation != null;
	}

	@Override
	public boolean isValid(final Flight flight, final ConstraintValidatorContext context) {
		assert context != null;

		if (flight == null) {
			super.state(context, false, "*", "javax.validation.constraints.NotNull.message");
			return false;
		}

		// Recuperamos todos los Legs asociados al vuelo.
		Collection<Leg> legs = this.legRepository.findLegsByFlightId(flight.getId());

		// Validación existente: si el vuelo está publicado (draftMode == false),
		// ningún leg asociado debe estar en modo borrador.
		if (!flight.isDraftMode() && legs != null)
			for (Leg leg : legs)
				if (leg.isDraftMode()) {
					super.state(context, false, "draftMode", "acme.validation.flight.leg-published.message");
					// No es necesario seguir iterando si se encuentra alguno en borrador.
					break;
				}

		return !super.hasErrors(context);
	}
}
