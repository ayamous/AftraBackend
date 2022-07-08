package ma.itroad.aace.eth.coref.service.validator;

import ma.itroad.aace.eth.coref.model.entity.ChapterRef;
import ma.itroad.aace.eth.coref.model.entity.CityRef;
import ma.itroad.aace.eth.coref.repository.ChapterRefRepository;
import ma.itroad.aace.eth.coref.repository.CityRefRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CityRefConstraintValidator implements ConstraintValidator<CityRefConstraints, String> {
    @Autowired
    CityRefRepository cityRefRepository;

    @Override
    public void initialize(CityRefConstraints refConstraints) {
    }

    @Override
    public boolean isValid(String cityRef, ConstraintValidatorContext context) {

        CityRef codeValue = cityRefRepository.findByReference(cityRef);

        return codeValue != null;
    }

}
