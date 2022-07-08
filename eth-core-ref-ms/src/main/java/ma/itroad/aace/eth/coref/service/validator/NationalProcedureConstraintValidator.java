package ma.itroad.aace.eth.coref.service.validator;

import ma.itroad.aace.eth.coref.model.entity.NationalProcedureRef;
import ma.itroad.aace.eth.coref.repository.NationalProcedureRefRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NationalProcedureConstraintValidator implements ConstraintValidator<NationalProcedureConstraints, String> {

    @Autowired
    private NationalProcedureRefRepository nationalProcedureRefRepository;

    @Override
    public void initialize(NationalProcedureConstraints nationalProcedureRef) {
    }

    @Override
    public boolean isValid(String nationalProcedureRef, ConstraintValidatorContext context) {

        NationalProcedureRef codeValue = nationalProcedureRefRepository.findByCode(nationalProcedureRef);

        return codeValue != null;
    }
}
