package ma.itroad.aace.eth.coref.service.validator;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import ma.itroad.aace.eth.coref.model.entity.CountryGroupRef;
import ma.itroad.aace.eth.coref.repository.CountryGroupRefRepository;

public class CountryGroupConstraintValidator  implements ConstraintValidator<CountryGroupConstraints, String> {

    @Autowired
    CountryGroupRefRepository countryGroupRefRepository;

    @Override
    public void initialize(CountryGroupConstraints countryGroup) {
    }

    @Override
    public boolean isValid(String countryGroup, ConstraintValidatorContext context) {

    	CountryGroupRef CountryGroupValue = countryGroupRefRepository.findByCode(countryGroup);

        return CountryGroupValue != null;
    }
}

