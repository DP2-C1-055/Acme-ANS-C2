
package acme.constraints;

import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import acme.client.helpers.MomentHelper;
import acme.entities.activityLog.ActivityLog;

@Component
public class ActivityLogValidator implements ConstraintValidator<ValidActivityLog, ActivityLog> {

	@Value("${acme.data.moment.maximum}")
	private String maxMomentString;

	// ConstraintValidator interface ------------------------------------------


	@Override
	public void initialize(final ValidActivityLog annotation) {
		assert annotation != null;
	}

	@Override
	public boolean isValid(final ActivityLog activityLog, final ConstraintValidatorContext context) {

		if (activityLog == null) {
			context.buildConstraintViolationWithTemplate("javax.validation.constraints.NotNull.message").addConstraintViolation();
			return false;
		}

		Date minMoment = activityLog.getAssignment().getLeg().getScheduledArrival();
		Date maxMoment = MomentHelper.parse(this.maxMomentString, "yyyy/MM/dd HH:mm");

		if (activityLog.getRegistrationMoment() != null) {
			boolean inRange = !activityLog.getRegistrationMoment().before(minMoment) && activityLog.getRegistrationMoment().compareTo(maxMoment) <= 0;  // Asegura que el maxMoment sea válido

			if (!inRange) {
				context.buildConstraintViolationWithTemplate("acme.validation.activitylog.registration-out-of-range.message").addPropertyNode("registrationMoment").addConstraintViolation();
				return false;
			}
		}

		return true;
	}
}
