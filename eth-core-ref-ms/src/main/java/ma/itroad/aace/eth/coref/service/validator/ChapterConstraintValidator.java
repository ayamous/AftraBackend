package ma.itroad.aace.eth.coref.service.validator;

import ma.itroad.aace.eth.coref.model.entity.ChapterRef;
import ma.itroad.aace.eth.coref.repository.ChapterRefRepository;
import org.springframework.beans.factory.annotation.Autowired;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ChapterConstraintValidator  implements ConstraintValidator<ChapterRefConstraints, String> {
    @Autowired
    ChapterRefRepository chapterRefRepository;

    @Override
    public void initialize(ChapterRefConstraints countryRef) {
    }

    @Override
    public boolean isValid(String chapterRef, ConstraintValidatorContext context) {

        ChapterRef codeValue = chapterRefRepository.findByCode(chapterRef);

        return codeValue != null;
    }

}
