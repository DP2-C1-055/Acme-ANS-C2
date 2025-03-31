
package acme.constraints;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.validation.AbstractValidator;
import acme.entities.technicians.Technician;
import acme.entities.technicians.TechnicianRepository;

public class ValidTechnicianValidator extends AbstractValidator<ValidTechnician, Technician> {

	@Autowired
	private TechnicianRepository	technicianRepository;

	// Patrón para código IATA: 3 letras mayúsculas.
	private static final Pattern	LICENSENUMBER_PATTERN	= Pattern.compile("^[A-Z]{2-3}\\d{6}$");


	@Override
	protected void initialise(final ValidTechnician annotation) {
		assert annotation != null;
	}

	@Override
	public boolean isValid(final Technician technician, final ConstraintValidatorContext context) {
		assert context != null;

		if (technician == null) {
			super.state(context, false, "*", "javax.validation.constraints.NotNull.message");
			return false;
		}

		String licenseNumber = technician.getLicenseNumber();
		// Validar que el licenseNumber no sea nulo.
		if (licenseNumber == null)
			super.state(context, false, "licenseNumber", "javax.validation.constraints.NotNull.message");
		else {
			// Validar el formato del licenseNumber.
			boolean matchesPattern = ValidTechnicianValidator.LICENSENUMBER_PATTERN.matcher(licenseNumber).matches();
			super.state(context, matchesPattern, "licenseNumber", "acme.validation.technician.invalid-licensenumber.message");

			// Solo si el formato es correcto se valida la unicidad.
			if (matchesPattern) {
				Technician existing = this.technicianRepository.findByLicenseNumber(licenseNumber);
				boolean unique = true;
				if (existing != null)
					// Comparar IDs: si existe otro aeropuerto con el mismo código y su ID es distinto, es duplicado.
					unique = technician.getId() == existing.getId();
				super.state(context, unique, "licenseNumber", "acme.validation.technician.duplicated-licensenumber.message");
			}
		}
		return !super.hasErrors(context);
	}
}
