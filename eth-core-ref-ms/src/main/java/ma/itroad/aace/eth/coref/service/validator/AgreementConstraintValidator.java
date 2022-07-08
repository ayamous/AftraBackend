package ma.itroad.aace.eth.coref.service.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import ma.itroad.aace.eth.coref.model.entity.Agreement;
import ma.itroad.aace.eth.coref.repository.AgreementRepository;

public class AgreementConstraintValidator implements ConstraintValidator<AgreementConstraints, String> {

	@Autowired
	AgreementRepository agreementRepository;

	@Override
	public void initialize(AgreementConstraints agreement) {
	}

	@Override
	public boolean isValid(String agreement, ConstraintValidatorContext context) {

		Agreement procedureValue = agreementRepository.findByCode(agreement);

		return procedureValue != null;
	}
}
