
package acme.constraints;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.ConstraintValidatorContext;

import acme.client.components.validation.AbstractValidator;
import acme.client.helpers.MomentHelper;
import acme.entities.service.Service;

public class ServiceValidator extends AbstractValidator<ValidPromotionCode, Service> {

	// Internal state
	// No additional state needed for this validator

	@Override
	public void initialize(final ValidPromotionCode annotation) {
		assert annotation != null;
	}

	@Override
	public boolean isValid(final Service service, final ConstraintValidatorContext context) {
		assert context != null;

		boolean result = true;

		if (service == null) {
			super.state(context, false, "**", "javax.validation.constraints.NotNull.message"); // Corrección aquí
			result = false;
		} else {
			// Validar que promotionCode, si existe, cumpla con el patrón
			String promotionCode = service.getPromotionCode();
			if (promotionCode != null) {
				boolean matchesPattern = promotionCode != null && promotionCode.matches("^[A-Z]{4}-[0-9]{2}$");
				if (!matchesPattern) {
					super.state(context, matchesPattern, "promotionCode", "acme.validation.service.promotionCode.invalidFormat.message");
					result = false;
				} else {
					// Extraer los últimos dos dígitos después del guion
					String[] parts = promotionCode.split("-");
					String lastTwoDigits = parts[1];

					// Obtener el año actual usando MomentHelper.currentMoment()
					Date currentMoment = MomentHelper.getCurrentMoment();
					SimpleDateFormat yearFormat = new SimpleDateFormat("yy"); // Formato de dos dígitos (ej. "25")
					String currentYear = yearFormat.format(currentMoment);

					boolean matchesYear = lastTwoDigits.equals(currentYear);
					if (!matchesYear) {
						super.state(context, matchesYear, "promotionCode", "acme.validation.service.promotionCode.invalidYear.message");
						result = false;
					}
				}
			}
		}

		return result && !super.hasErrors(context);
	}
}
