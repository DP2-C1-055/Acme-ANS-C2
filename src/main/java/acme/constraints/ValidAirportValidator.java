
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

		// Validación del iataCode
		String iataCode = airport.getIataCode();
		if (iataCode == null)
			super.state(context, false, "iataCode", "javax.validation.constraints.NotNull.message");
		else {
			boolean matchesPattern = ValidAirportValidator.IATA_PATTERN.matcher(iataCode).matches();
			super.state(context, matchesPattern, "iataCode", "acme.validation.airport.invalid-iataCode.message");

			// Solo se valida la unicidad si el formato es correcto.
			if (matchesPattern) {
				Airport existing = this.airportRepository.findByIataCode(iataCode);
				boolean unique = true;
				if (existing != null)
					// Si existe otro aeropuerto con el mismo código y su ID es distinto, es duplicado.
					unique = airport.getId() == existing.getId();
				super.state(context, unique, "iataCode", "acme.validation.airport.duplicated-iataCode.message");
			}
		}

		// Validación de longitud para website: máximo 255 caracteres
		if (airport.getWebsite() != null && airport.getWebsite().length() > 255)
			super.state(context, false, "website", "acme.validation.airport.max-length-website.message");

		// Validación de longitud para email: máximo 255 caracteres
		if (airport.getEmail() != null && airport.getEmail().length() > 255)
			super.state(context, false, "email", "acme.validation.airport.max-length-email.message");

		if (airport.getName() != null && airport.getName().length() > 50)
			super.state(context, false, "name", "acme.validation.airport.max-length-name.message");
		if (airport.getCity() != null && airport.getCity().length() > 50)
			super.state(context, false, "city", "acme.validation.airport.max-length-city.message");
		if (airport.getCountry() != null && airport.getCountry().length() > 50)
			super.state(context, false, "country", "acme.validation.airport.max-length-country.message");

		return !super.hasErrors(context);
	}
}
