package ma.itroad.aace.eth.coref.service.validator;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;

public class CountryConstraintValidator  implements ConstraintValidator<CountryConstraints, String> {

    @Autowired
    CountryRefRepository countryRefRepository;

    @Override
    public void initialize(CountryConstraints countryRef) {
    }

    @Override
    public boolean isValid(String countryRef, ConstraintValidatorContext context) {

        CountryRef codeValue = countryRefRepository.findByReference(countryRef);

        return codeValue != null;
    }
}

