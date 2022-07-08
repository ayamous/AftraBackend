package ma.itroad.aace.eth.coref.service.validator;

import ma.itroad.aace.eth.coref.model.entity.CustomsRegimRef;
import ma.itroad.aace.eth.coref.repository.CustomsRegimRefRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CustomsRegimConstraintValidator implements ConstraintValidator<CustomsRegimConstraints, String> {

@Autowired
CustomsRegimRefRepository customsRegimRefRepository;

@Override
public void initialize(CustomsRegimConstraints customsRegimRef) {
        }

@Override
public boolean isValid(String customsRegimRef, ConstraintValidatorContext context) {

        CustomsRegimRef codeValue = customsRegimRefRepository.findByCode(customsRegimRef);

        return codeValue != null;
        }
}

