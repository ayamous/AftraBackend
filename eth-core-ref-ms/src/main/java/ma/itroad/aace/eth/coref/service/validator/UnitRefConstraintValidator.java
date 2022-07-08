package ma.itroad.aace.eth.coref.service.validator;

import ma.itroad.aace.eth.coref.model.entity.ChapterRef;
import ma.itroad.aace.eth.coref.model.entity.UnitRef;
import ma.itroad.aace.eth.coref.repository.ChapterRefRepository;
import ma.itroad.aace.eth.coref.repository.UnitRefRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UnitRefConstraintValidator implements ConstraintValidator<UnitRefConstraints, String> {
    @Autowired
    UnitRefRepository unitRefRepository;

    @Override
    public void initialize(UnitRefConstraints countryRef) {
    }

    @Override
    public boolean isValid(String chapterRef, ConstraintValidatorContext context) {

        UnitRef codeValue = unitRefRepository.findByCode(chapterRef);

        return codeValue != null;
    }

}
