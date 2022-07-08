package ma.itroad.aace.eth.coref.service.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import ma.itroad.aace.eth.coref.model.entity.NationalProcedureRef;
import ma.itroad.aace.eth.coref.repository.NationalProcedureRefRepository;

public class ProcedureConstraintValidator implements ConstraintValidator<ProcedureConstraints, String> {

	@Autowired
	NationalProcedureRefRepository nationalProcedureRefRepository;

	@Override
	public void initialize(ProcedureConstraints procedure) {
	}

	@Override
	public boolean isValid(String procedure, ConstraintValidatorContext context) {

		NationalProcedureRef procedureValue = nationalProcedureRefRepository.findByCode(procedure);

		return procedureValue != null;
	}
}
