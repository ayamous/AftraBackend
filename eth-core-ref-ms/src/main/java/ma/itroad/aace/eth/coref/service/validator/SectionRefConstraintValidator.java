package ma.itroad.aace.eth.coref.service.validator;

import ma.itroad.aace.eth.coref.model.entity.CategoryRef;
import ma.itroad.aace.eth.coref.model.entity.SectionRef;
import ma.itroad.aace.eth.coref.repository.CategoryRefRepository;
import ma.itroad.aace.eth.coref.repository.SectionRefRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SectionRefConstraintValidator implements ConstraintValidator<SectionRefConstraints, String> {
    @Autowired
    SectionRefRepository sectionRefRepository;

    @Override
    public void initialize(SectionRefConstraints refConstraints) {
    }

    @Override
    public boolean isValid(String sectionRef, ConstraintValidatorContext context) {

        SectionRef codeValue = sectionRefRepository.findByCode(sectionRef);

        return codeValue != null;
    }

}
