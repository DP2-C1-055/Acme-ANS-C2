
package acme.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented

@Constraint(validatedBy = {})
@ReportAsSingleViolation

@Length(min = 6, max = 16)
@Pattern(regexp = "^\\+?\\d{6,15}$")
public @interface ValidPhone {

	String message() default "{acme.validation.ValidPhone.message}";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
