package ma.itroad.aace.eth.coref.service.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.repository.LangRepository;

public class LangConstraintValidator implements ConstraintValidator<langConstraints, String> {
   
	@Autowired
	private LangRepository langRepository;
	
    @Override
    public void initialize(langConstraints lang) {
    }
	
	@Override
    public boolean isValid(String lang, ConstraintValidatorContext context) {
		
		Lang codeValue = langRepository.findByCode(lang);
		
        return codeValue != null;
    }
}
