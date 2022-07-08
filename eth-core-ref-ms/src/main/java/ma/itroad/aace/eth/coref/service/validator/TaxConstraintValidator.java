package ma.itroad.aace.eth.coref.service.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.entity.TaxRef;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.TaxRefRepository;

public class TaxConstraintValidator implements ConstraintValidator<TaxConstraints, String> {
   
	@Autowired
	private TaxRefRepository taxRefRepository;
	
    @Override
    public void initialize(TaxConstraints lang) {
    }
	
	@Override
    public boolean isValid(String tax, ConstraintValidatorContext context) {
		
		TaxRef taxValue = taxRefRepository.findByCode(tax);
		
        return taxValue != null;
    }
}
