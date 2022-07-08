package ma.itroad.aace.eth.coref.service.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import ma.itroad.aace.eth.coref.model.entity.TarifBookRef;
import ma.itroad.aace.eth.coref.repository.TarifBookRefRepository;


public class TarifBookConstraintValidator implements ConstraintValidator<TarifBookConstraint, String> {
	   
	@Autowired
	private TarifBookRefRepository tarifBookRefRepository;
	
    @Override
    public void initialize(TarifBookConstraint tarifBook) {
    }
	
	@Override
    public boolean isValid(String tarifBook, ConstraintValidatorContext context) {
		
		TarifBookRef tarifBookValue = tarifBookRefRepository.findByReference(tarifBook);
		
        return tarifBookValue != null;
    }
}