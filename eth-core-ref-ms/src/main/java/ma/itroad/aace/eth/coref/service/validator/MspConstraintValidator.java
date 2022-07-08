package ma.itroad.aace.eth.coref.service.validator;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import ma.itroad.aace.eth.coref.model.entity.SanitaryPhytosanitaryMeasuresRef;
import ma.itroad.aace.eth.coref.repository.SanitaryPhytosanitaryMeasuresRefRepository;

public class MspConstraintValidator  implements ConstraintValidator<MspConstraints, String> {

    @Autowired
    SanitaryPhytosanitaryMeasuresRefRepository sanitaryPhytosanitaryMeasuresRefRepository;

    @Override
    public void initialize(MspConstraints msp) {
    }

    @Override
    public boolean isValid(String msp, ConstraintValidatorContext context) {

    	SanitaryPhytosanitaryMeasuresRef codeValue = sanitaryPhytosanitaryMeasuresRefRepository.findByCode(msp);

        return codeValue != null;
    }
}

