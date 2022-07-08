package ma.itroad.aace.eth.coref.service.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import ma.itroad.aace.eth.coref.model.entity.RegulationRef;
import ma.itroad.aace.eth.coref.repository.RegulationRefRepository;

public class RegulationConstraintValidator implements ConstraintValidator<RegulationConstraints, String> {

	@Autowired
	RegulationRefRepository regulationRefRepository;

	@Override
	public void initialize(RegulationConstraints regulation) {
	}

	@Override
	public boolean isValid(String regulation, ConstraintValidatorContext context) {

		RegulationRef procedureValue = regulationRefRepository.findByCode(regulation);

		return procedureValue != null;
	}
}
