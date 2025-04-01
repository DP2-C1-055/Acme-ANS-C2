
package acme.constraints;

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

		// Si el vuelo está publicado (draftMode == false)...
		if (!flight.isDraftMode()) {
			// Recuperamos todos los Legs asociados al vuelo.
			java.util.Collection<Leg> legs = this.legRepository.findLegsByFlightId(flight.getId());
			if (legs != null)
				for (Leg leg : legs)
					if (leg.isDraftMode()) {
						super.state(context, false, "draftMode", "acme.validation.flight.leg-published.message");
						// No hace falta seguir iterando si se encuentra alguno en draft.
						break;
					}
		}

		return !super.hasErrors(context);
	}
}
