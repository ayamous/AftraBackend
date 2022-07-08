package ma.itroad.aace.eth.coref.service.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CityRefConstraintValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CityRefConstraints {
    String message() default "Invalid ";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
