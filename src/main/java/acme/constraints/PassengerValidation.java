
package acme.constraints;

import java.util.Collection;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.validation.AbstractValidator;
import acme.entities.passenger.Passenger;
import acme.features.customer.passenger.CustomerPassengerRepository;

public class PassengerValidation extends AbstractValidator<ValidPassenger, Passenger> {

	// Internal state ---------------------------------------------------------

	@Override
	public void initialise(final ValidPassenger annotation) {
		assert annotation != null;
	}


	@Autowired
	private CustomerPassengerRepository repository;


	@Override
	public boolean isValid(final Passenger passenger, final ConstraintValidatorContext context) {

		assert context != null;

		boolean result = true;

		Collection<String> passportNumbers = this.repository.getAllPassportNumber();

		if (passenger == null)
			return false;
		String passportNumber = passenger.getPassportNumber();

		if (passportNumbers.contains(passportNumber)) {
			super.state(context, false, "passengers", "acme.validation.passenger.passportNumber.message");
			result = false;
		}
		return result;

	}

}
