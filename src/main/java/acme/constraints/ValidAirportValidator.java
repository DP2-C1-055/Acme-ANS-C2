
package acme.constraints;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.validation.AbstractValidator;
import acme.entities.airport.Airport;
import acme.entities.airport.AirportRepository;

public class ValidAirportValidator extends AbstractValidator<ValidAirport, Airport> {

	@Autowired
	private AirportRepository		airportRepository;

	// Patrón para código IATA: 3 letras mayúsculas.
	private static final Pattern	IATA_PATTERN	= Pattern.compile("^[A-Z]{3}$");


	@Override
	protected void initialise(final ValidAirport annotation) {
		assert annotation != null;
	}

	@Override
	public boolean isValid(final Airport airport, final ConstraintValidatorContext context) {
		assert context != null;

		if (airport == null) {
			super.state(context, false, "*", "javax.validation.constraints.NotNull.message");
			return false;
		}

		String iataCode = airport.getIataCode();
		// Validar que el iataCode no sea nulo.
		if (iataCode == null)
			super.state(context, false, "iataCode", "javax.validation.constraints.NotNull.message");
		else {
			// Validar el formato del iataCode (3 letras mayúsculas).
			boolean matchesPattern = ValidAirportValidator.IATA_PATTERN.matcher(iataCode).matches();
			super.state(context, matchesPattern, "iataCode", "acme.validation.airport.invalid-iataCode.message");

			// Solo si el formato es correcto se valida la unicidad.
			if (matchesPattern) {
				Airport existing = this.airportRepository.findByIataCode(iataCode);
				boolean unique = true;
				if (existing != null)
					// Comparar IDs: si existe otro aeropuerto con el mismo código y su ID es distinto, es duplicado.
					unique = airport.getId() == existing.getId();
				super.state(context, unique, "iataCode", "acme.validation.airport.duplicated-iataCode.message");
			}
		}
		return !super.hasErrors(context);
	}
}
