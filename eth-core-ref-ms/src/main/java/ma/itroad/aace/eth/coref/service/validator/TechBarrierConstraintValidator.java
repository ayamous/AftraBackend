package ma.itroad.aace.eth.coref.service.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.entity.TaxRef;
import ma.itroad.aace.eth.coref.model.entity.TechBarrierRef;
import ma.itroad.aace.eth.coref.repository.LangRepository;
import ma.itroad.aace.eth.coref.repository.TaxRefRepository;
import ma.itroad.aace.eth.coref.repository.TechBarrierRefRepository;

public class TechBarrierConstraintValidator implements ConstraintValidator<TechBarrierConstraints, String> {
   
	@Autowired
	private TechBarrierRefRepository techBarrierRefRepository;
	
    @Override
    public void initialize(TechBarrierConstraints tech) {
    }
	
	@Override
    public boolean isValid(String tech, ConstraintValidatorContext context) {
		
		TechBarrierRef techValue = techBarrierRefRepository.findByCode(tech);
		
        return techValue != null;
    }
}
