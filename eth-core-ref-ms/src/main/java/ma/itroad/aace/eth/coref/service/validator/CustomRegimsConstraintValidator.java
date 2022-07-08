package ma.itroad.aace.eth.coref.service.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import ma.itroad.aace.eth.coref.model.entity.CustomsRegimRef;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.repository.CustomsRegimRefRepository;
import ma.itroad.aace.eth.coref.repository.LangRepository;

public class CustomRegimsConstraintValidator implements ConstraintValidator<CustomRegimsConstraints, String> {
   
	@Autowired
	private CustomsRegimRefRepository customsRegimRefRepository;
	
    @Override
    public void initialize(CustomRegimsConstraints customRegim) {
    }
	
	@Override
    public boolean isValid(String customRegim, ConstraintValidatorContext context) {
		
		CustomsRegimRef customRegimValue = customsRegimRefRepository.findByCode(customRegim);
		
        return customRegimValue != null;
    }
}

