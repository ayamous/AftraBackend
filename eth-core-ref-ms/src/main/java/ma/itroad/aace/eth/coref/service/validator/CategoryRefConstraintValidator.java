package ma.itroad.aace.eth.coref.service.validator;

import ma.itroad.aace.eth.coref.model.entity.CategoryRef;
import ma.itroad.aace.eth.coref.model.entity.ChapterRef;
import ma.itroad.aace.eth.coref.repository.CategoryRefRepository;
import ma.itroad.aace.eth.coref.repository.ChapterRefRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CategoryRefConstraintValidator implements ConstraintValidator<CategoryRefConstraints, String> {
    @Autowired
    CategoryRefRepository categoryRefRepository;

    @Override
    public void initialize(CategoryRefConstraints refConstraints) {
    }

    @Override
    public boolean isValid(String categoryRef, ConstraintValidatorContext context) {

        CategoryRef codeValue = categoryRefRepository.findByCode(categoryRef);

        return codeValue != null;
    }

}
