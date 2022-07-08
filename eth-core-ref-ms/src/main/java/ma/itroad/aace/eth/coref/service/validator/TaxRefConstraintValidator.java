package ma.itroad.aace.eth.coref.service.validator;

import ma.itroad.aace.eth.coref.model.entity.TarifBookRef;
import ma.itroad.aace.eth.coref.model.entity.TaxRef;
import ma.itroad.aace.eth.coref.repository.TarifBookRefRepository;
import ma.itroad.aace.eth.coref.repository.TaxRefRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class TaxRefConstraintValidator implements ConstraintValidator<TaxRefConstraints, String> {
	   
	@Autowired
	private TaxRefRepository taxRefRepository;
	
    @Override
    public void initialize(TaxRefConstraints taxRefConstraints) {
    }
	
	@Override
    public boolean isValid(String tarifBook, ConstraintValidatorContext context) {
		
		TaxRef taxRef = taxRefRepository.findByCode(tarifBook);
		
        return taxRef != null;
    }
}