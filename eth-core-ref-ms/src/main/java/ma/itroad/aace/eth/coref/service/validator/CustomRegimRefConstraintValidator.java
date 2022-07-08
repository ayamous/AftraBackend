package ma.itroad.aace.eth.coref.service.validator;


import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.model.entity.CustomsRegimRef;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.repository.CustomsRegimRefRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CustomRegimRefConstraintValidator implements ConstraintValidator<CustomRegimRefConstraints, String> {

    @Autowired
    CustomsRegimRefRepository customsRegimRefRepository;

    @Override
    public void initialize(CustomRegimRefConstraints refConstraints) {
    }

    @Override
    public boolean isValid(String customRegimRef, ConstraintValidatorContext context) {

        CustomsRegimRef codeValue = customsRegimRefRepository.findByCode(customRegimRef);

        return codeValue != null;
    }
}

